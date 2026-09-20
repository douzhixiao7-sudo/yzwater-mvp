package com.sydigit.yzwater.module.service.pond;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondFilterOptionsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondStatsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenWaterPondAreaOverviewRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.pond.YzWaterPondDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.pond.YzWaterPondMapper;
import com.sydigit.yzwater.module.system.service.area.SystemAreaService;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 坑塘业务服务
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class WaterPondService {

    private static final int POND_SRID = 4326;
    private static final int BASE_SRID = 4490;
    private static final String FACILITY_TYPE_POND = "pond";
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzWaterPondMapper pondMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final SystemAreaService systemAreaService;
    private final ObjectMapper objectMapper;

    public PageResult<WaterPondPageRespVO> getPage(WaterPondPageReqVO reqVO) {
        List<String> villageCodes = buildVillageCodeScope(reqVO.getVillageCode());
        PageResult<YzWaterPondDO> page = pondMapper.selectPage(reqVO, pondMapper.buildQueryWrapper(reqVO, villageCodes));
        return BeanUtils.toBean(page, WaterPondPageRespVO.class);
    }

    public WaterPondFilterOptionsRespVO getFilterOptions() {
        WaterPondFilterOptionsRespVO respVO = new WaterPondFilterOptionsRespVO();
        respVO.setUsageStatusList(pondMapper.selectDistinctUsageStatus());
        respVO.setResourceNatureList(pondMapper.selectDistinctResourceNature());
        respVO.setOwnershipTypeList(pondMapper.selectDistinctOwnershipType());
        respVO.setOccupationStatusList(pondMapper.selectDistinctOccupationStatus());
        respVO.setResourceTypeList(pondMapper.selectDistinctResourceType());
        return respVO;
    }

    /**
     * 一张图 - 坑塘总览（按行政区划 + 业务筛选条件，返回面几何 GeoJSON）。
     */
    public BigScreenWaterPondAreaOverviewRespVO getMapAreaOverviewByArea(Long areaId, WaterPondPageReqVO reqVO) {
        WaterPondPageReqVO query = reqVO == null ? new WaterPondPageReqVO() : reqVO;
        List<String> villageCodes = areaId == null ? List.of() : buildAreaCodeScope(areaId);

        BigScreenWaterPondAreaOverviewRespVO resp = new BigScreenWaterPondAreaOverviewRespVO();
        Map<String, Object> overview = pondMapper.selectOverviewStats(query, villageCodes);
        if (overview != null) {
            resp.setTotalCount(toLong(mapValue(overview, "total_count", "totalCount")));
            resp.setTotalAreaSqm(toBigDecimal(mapValue(overview, "total_area_sqm", "totalAreaSqm")));
            resp.setTotalAreaMu(toBigDecimal(mapValue(overview, "total_area_mu", "totalAreaMu")));
        } else {
            resp.setTotalCount(0L);
            resp.setTotalAreaSqm(BigDecimal.ZERO);
            resp.setTotalAreaMu(BigDecimal.ZERO);
        }

        List<YzWaterPondDO> ponds = pondMapper.selectList(pondMapper.buildFilterWrapper(query, villageCodes));
        if (ponds == null || ponds.isEmpty()) {
            resp.setList(List.of());
            return resp;
        }

        List<BigScreenWaterPondAreaOverviewRespVO.Item> items = new ArrayList<>(ponds.size());
        for (YzWaterPondDO pond : ponds) {
            if (pond == null || pond.getId() == null) {
                continue;
            }
            BigScreenWaterPondAreaOverviewRespVO.Item item = new BigScreenWaterPondAreaOverviewRespVO.Item();
            item.setPondId(pond.getId());
            item.setFacilityBaseId(pond.getFacilityId());
            item.setResourceName(StrUtil.blankToDefault(pond.getResourceName(), ""));
            item.setResourceCode(StrUtil.blankToDefault(pond.getResourceCode(), ""));
            item.setOwnershipType(pond.getOwnershipType());
            item.setResourceType(pond.getResourceType());
            item.setUsageStatus(pond.getUsageStatus());
            item.setAreaSqm(pond.getAreaSqm());
            item.setAreaMu(pond.getAreaMu());
            item.setLongitude(pond.getCenterLon());
            item.setLatitude(pond.getCenterLat());
            item.setGeometryGeoJson(resolveGeometryGeoJson(pond));
            items.add(item);
        }
        resp.setList(items);
        return resp;
    }

    private String resolveGeometryGeoJson(YzWaterPondDO pond) {
        if (pond == null) {
            return "";
        }
        Geometry geom = pond.getGeom();
        if ((geom == null || geom.isEmpty()) && pond.getFacilityId() != null) {
            YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(pond.getFacilityId());
            if (base != null) {
                geom = base.getGeom();
            }
        }
        if (geom == null || geom.isEmpty()) {
            return "";
        }
        try {
            StringWriter writer = new StringWriter();
            new GeometryJSON().write(geom, writer);
            return writer.toString();
        } catch (Exception ex) {
            log.warn("坑塘几何转 GeoJSON 失败 id={}: {}", pond.getId(), ex.getMessage());
            return "";
        }
    }

    public WaterPondStatsRespVO getStats(WaterPondPageReqVO reqVO) {
        WaterPondPageReqVO query = reqVO == null ? new WaterPondPageReqVO() : reqVO;
        List<String> villageCodes = buildVillageCodeScope(query.getVillageCode());

        WaterPondStatsRespVO resp = new WaterPondStatsRespVO();
        Map<String, Object> overview = pondMapper.selectOverviewStats(query, villageCodes);
        if (overview != null) {
            resp.setTotalCount(toLong(mapValue(overview, "total_count", "totalCount")));
            resp.setTotalAreaSqm(toBigDecimal(mapValue(overview, "total_area_sqm", "totalAreaSqm")));
            resp.setTotalAreaMu(toBigDecimal(mapValue(overview, "total_area_mu", "totalAreaMu")));
            resp.setVillageCount(toLong(mapValue(overview, "village_code_count", "villageCodeCount")));
        }

        AreaIndex areaIndex = buildAreaIndex();
        List<Map<String, Object>> villageRows = pondMapper.selectGroupByVillageCode(query, villageCodes);
        if (villageRows == null) {
            villageRows = List.of();
        }
        List<WaterPondStatsRespVO.NameCountItem> villageStats = new ArrayList<>();
        Map<String, WaterPondStatsRespVO.NameCountItem> townAgg = new LinkedHashMap<>();

        for (Map<String, Object> row : villageRows) {
            if (row == null) {
                continue;
            }
            String code = StrUtil.trimToEmpty(String.valueOf(mapValue(row, "code") == null ? "" : mapValue(row, "code")));
            String name = StrUtil.trimToNull(mapValue(row, "name") == null ? null : String.valueOf(mapValue(row, "name")));
            long count = toLong(mapValue(row, "cnt"));
            BigDecimal areaSqm = toBigDecimal(mapValue(row, "area_sqm", "areaSqm"));

            String resolvedName = areaIndex.nameById.getOrDefault(code,
                    StrUtil.blankToDefault(name, StrUtil.blankToDefault(code, "未挂区划")));
            String townCode = resolveTownCode(code, areaIndex);
            String townName = resolveTownName(code, resolvedName, areaIndex);

            String villageDisplayName = resolvedName;
            if (isTownName(townName) && !isTownName(resolvedName) && !StrUtil.equals(townName, resolvedName)) {
                villageDisplayName = townName + "/" + resolvedName;
            }

            WaterPondStatsRespVO.NameCountItem villageItem = new WaterPondStatsRespVO.NameCountItem();
            villageItem.setCode(StrUtil.blankToDefault(code, null));
            villageItem.setName(villageDisplayName);
            villageItem.setCount(count);
            villageItem.setAreaSqm(areaSqm);
            villageStats.add(villageItem);

            String townKey = StrUtil.isNotBlank(townCode) ? townCode : townName;
            WaterPondStatsRespVO.NameCountItem townItem = townAgg.computeIfAbsent(townKey, key -> {
                WaterPondStatsRespVO.NameCountItem item = new WaterPondStatsRespVO.NameCountItem();
                item.setCode(StrUtil.blankToDefault(townCode, null));
                item.setName(townName);
                item.setCount(0L);
                item.setAreaSqm(BigDecimal.ZERO);
                return item;
            });
            townItem.setCount(townItem.getCount() + count);
            townItem.setAreaSqm(townItem.getAreaSqm().add(areaSqm));
        }

        List<WaterPondStatsRespVO.NameCountItem> townStats = new ArrayList<>(townAgg.values());
        townStats.sort(Comparator.comparing(WaterPondStatsRespVO.NameCountItem::getCount).reversed());
        villageStats.sort(Comparator.comparing(WaterPondStatsRespVO.NameCountItem::getCount).reversed());

        resp.setTownStats(townStats);
        resp.setTownCount(townStats.stream()
                .filter(item -> item.getCount() != null && item.getCount() > 0)
                .filter(item -> isTownName(item.getName()))
                .count());
        resp.setVillageStats(villageStats.size() > 15 ? villageStats.subList(0, 15) : villageStats);
        resp.setOwnershipStats(toNameCountItems(pondMapper.selectGroupByColumn("ownership_type", query, villageCodes)));
        resp.setUsageStatusStats(toNameCountItems(pondMapper.selectGroupByColumn("usage_status", query, villageCodes)));
        resp.setResourceTypeStats(toNameCountItems(pondMapper.selectGroupByColumn("resource_type", query, villageCodes)));
        return resp;
    }

    private List<WaterPondStatsRespVO.NameCountItem> toNameCountItems(List<Map<String, Object>> rows) {
        List<WaterPondStatsRespVO.NameCountItem> list = new ArrayList<>();
        if (rows == null) {
            return list;
        }
        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }
            WaterPondStatsRespVO.NameCountItem item = new WaterPondStatsRespVO.NameCountItem();
            item.setName(StrUtil.blankToDefault(mapValue(row, "name") == null ? null : String.valueOf(mapValue(row, "name")), "未填写"));
            item.setCount(toLong(mapValue(row, "cnt")));
            item.setAreaSqm(toBigDecimal(mapValue(row, "area_sqm", "areaSqm")));
            list.add(item);
        }
        return list;
    }

    private static class AreaIndex {
        private final Map<String, String> nameById = new HashMap<>();
        private final Map<String, String> parentById = new HashMap<>();
    }

    private AreaIndex buildAreaIndex() {
        AreaIndex index = new AreaIndex();
        try {
            // 与管理后台区划树根一致：江苏节点下的市/县/镇/村
            List<SystemAreaNode> roots = systemAreaService.getAreaTreeChildren(320000L);
            fillAreaIndex(roots, null, index);
        } catch (Exception ex) {
            log.warn("加载行政区划树失败，统计将按坑塘表原名称展示：{}", ex.getMessage());
        }
        return index;
    }

    private void fillAreaIndex(List<SystemAreaNode> nodes, String parentId, AreaIndex index) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (SystemAreaNode node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            String id = String.valueOf(node.getId());
            index.nameById.put(id, StrUtil.blankToDefault(node.getName(), id));
            if (StrUtil.isNotBlank(parentId)) {
                index.parentById.put(id, parentId);
            }
            fillAreaIndex(node.getChildren(), id, index);
        }
    }

    /** 名称后缀判断镇/街道（与前端台账展示约定一致） */
    private boolean isTownName(String name) {
        String n = StrUtil.trimToEmpty(name);
        return n.endsWith("镇") || n.endsWith("街道") || n.endsWith("乡") || n.endsWith("办事处");
    }

    /**
     * 从区划编码向上找镇级节点。
     * 依据：区划树 parent 链 + 名称后缀（镇/街道/乡/办事处）；找不到则返回空，绝不把村编码当成镇。
     */
    private String resolveTownCode(String areaCode, AreaIndex index) {
        String code = StrUtil.trimToEmpty(areaCode);
        if (StrUtil.isBlank(code)) {
            return "";
        }
        String cursor = code;
        Set<String> visited = new HashSet<>();
        while (StrUtil.isNotBlank(cursor) && visited.add(cursor)) {
            String name = index.nameById.get(cursor);
            if (isTownName(name)) {
                return cursor;
            }
            cursor = index.parentById.get(cursor);
        }
        return "";
    }

    private String resolveTownName(String areaCode, String fallbackName, AreaIndex index) {
        String townCode = resolveTownCode(areaCode, index);
        if (StrUtil.isNotBlank(townCode)) {
            String name = index.nameById.get(townCode);
            if (isTownName(name)) {
                return name;
            }
        }
        // 台账挂的是镇级编码，但区划树未命中时，仍可用名称后缀兜底
        if (isTownName(fallbackName)) {
            return fallbackName;
        }
        return "未归属到镇";
    }

    private Object mapValue(Map<String, Object> map, String... keys) {
        if (map == null || map.isEmpty() || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key != null && map.containsKey(key)) {
                return map.get(key);
            }
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String actual = entry.getKey().replace("_", "");
            for (String key : keys) {
                if (key != null && actual.equalsIgnoreCase(key.replace("_", ""))) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    public WaterPondSaveReqVO getDetail(Long id) {
        YzWaterPondDO pond = requirePond(id);
        WaterPondSaveReqVO vo = BeanUtils.toBean(pond, WaterPondSaveReqVO.class);
        Geometry geom = pond.getGeom();
        if (geom == null && pond.getFacilityId() != null) {
            YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(pond.getFacilityId());
            if (base != null) {
                geom = base.getGeom();
            }
        }
        if (geom != null && !geom.isEmpty()) {
            try {
                StringWriter writer = new StringWriter();
                new GeometryJSON().write(geom, writer);
                vo.setGeometryGeoJson(writer.toString());
            } catch (Exception ignored) {
                // 几何转换失败不影响属性详情
            }
            if (vo.getCenterLon() == null || vo.getCenterLat() == null) {
                vo.setCenterLon(WaterPondGeometryHelper.computeCenterLon(geom));
                vo.setCenterLat(WaterPondGeometryHelper.computeCenterLat(geom));
            }
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(WaterPondSaveReqVO reqVO) {
        if (reqVO == null) {
            throw ServiceExceptionUtil.invalidParamException("请求不能为空");
        }
        if (StrUtil.isBlank(reqVO.getResourceName())) {
            throw ServiceExceptionUtil.invalidParamException("资源名称不能为空");
        }

        String resourceCode = StrUtil.trimToNull(reqVO.getResourceCode());
        if (resourceCode != null && !pondMapper.selectByResourceCodes(List.of(resourceCode)).isEmpty()) {
            throw ServiceExceptionUtil.invalidParamException("资源编号已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        Long pondId = SNOWFLAKE.nextId();
        Long baseId = SNOWFLAKE.nextId();
        if (resourceCode == null) {
            resourceCode = String.valueOf(pondId);
        }
        String resourceName = StrUtil.trim(reqVO.getResourceName());

        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityCode(resourceCode);
        base.setFacilityName(resourceName);
        base.setFacilityType(FACILITY_TYPE_POND);
        base.setAdminRegion(StrUtil.trimToNull(reqVO.getVillageName()));
        base.setAdminRegionCode(StrUtil.trimToNull(reqVO.getVillageCode()));
        base.setManageUnit(StrUtil.trimToNull(reqVO.getOwnerUnit()));
        base.setSourceType("manual");
        base.setCreateTime(now);
        base.setUpdateTime(now);

        YzWaterPondDO pond = new YzWaterPondDO();
        pond.setId(pondId);
        pond.setFacilityId(baseId);
        pond.setResourceCode(resourceCode);
        pond.setResourceName(resourceName);
        applyPondFields(pond, reqVO);
        pond.setCreateTime(now);
        pond.setUpdateTime(now);

        if (StrUtil.isNotBlank(reqVO.getGeometryGeoJson())) {
            Geometry sourceGeom = parseEditorGeometry(reqVO.getGeometryGeoJson());
            Geometry pondGeom = ensureMultiPolygon((Geometry) sourceGeom.copy());
            pondGeom.setSRID(POND_SRID);
            pond.setGeom(pondGeom);
            WaterPondGeometryHelper.applyCenterToPond(pond, pondGeom);
            Geometry baseGeom = transformToBaseSrid((Geometry) pondGeom.copy());
            base.setGeom(baseGeom);
            base.setGeomType(baseGeom.getGeometryType());
            base.setSrid(BASE_SRID);
        }

        facilityBaseMapper.insert(base);
        pondMapper.insert(pond);
        return pondId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(WaterPondSaveReqVO reqVO) {
        if (reqVO == null || reqVO.getId() == null) {
            throw ServiceExceptionUtil.invalidParamException("坑塘ID不能为空");
        }
        YzWaterPondDO exists = requirePond(reqVO.getId());
        LocalDateTime now = LocalDateTime.now();

        YzWaterPondDO update = new YzWaterPondDO();
        update.setId(reqVO.getId());
        applyPondFields(update, reqVO);
        update.setUpdateTime(now);
        pondMapper.updateById(update);

        if (exists.getFacilityId() != null && StrUtil.isNotBlank(reqVO.getResourceName())) {
            YzWaterFacilityBaseDO baseUpdate = new YzWaterFacilityBaseDO();
            baseUpdate.setId(exists.getFacilityId());
            baseUpdate.setFacilityName(StrUtil.trim(reqVO.getResourceName()));
            baseUpdate.setAdminRegion(StrUtil.trimToNull(reqVO.getVillageName()));
            baseUpdate.setAdminRegionCode(StrUtil.trimToNull(reqVO.getVillageCode()));
            baseUpdate.setManageUnit(StrUtil.trimToNull(reqVO.getOwnerUnit()));
            baseUpdate.setUpdateTime(now);
            facilityBaseMapper.updateById(baseUpdate);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        YzWaterPondDO exists = requirePond(id);
        pondMapper.deleteById(id);
        if (exists.getFacilityId() != null) {
            facilityBaseMapper.deleteById(exists.getFacilityId());
        }
    }

    /**
     * 更新坑塘面几何：坑塘表存 4326，基础表同步写入 4490。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGeometry(Long id, String geometryGeoJson) {
        YzWaterPondDO exists = requirePond(id);
        LocalDateTime now = LocalDateTime.now();
        if (StrUtil.isBlank(geometryGeoJson)) {
            pondMapper.update(null, new LambdaUpdateWrapper<YzWaterPondDO>()
                    .eq(YzWaterPondDO::getId, id)
                    .set(YzWaterPondDO::getGeom, null)
                    .set(YzWaterPondDO::getCenterLon, null)
                    .set(YzWaterPondDO::getCenterLat, null)
                    .set(YzWaterPondDO::getUpdateTime, now));
            if (exists.getFacilityId() != null) {
                facilityBaseMapper.clearGeomById(exists.getFacilityId());
            }
            return;
        }

        Geometry sourceGeom = parseEditorGeometry(geometryGeoJson);
        Geometry pondGeom = ensureMultiPolygon((Geometry) sourceGeom.copy());
        pondGeom.setSRID(POND_SRID);

        YzWaterPondDO pondUpdate = new YzWaterPondDO();
        pondUpdate.setId(id);
        pondUpdate.setGeom(pondGeom);
        WaterPondGeometryHelper.applyCenterToPond(pondUpdate, pondGeom);
        pondUpdate.setUpdateTime(now);
        pondMapper.updateById(pondUpdate);

        if (exists.getFacilityId() != null) {
            Geometry baseGeom = transformToBaseSrid((Geometry) pondGeom.copy());
            YzWaterFacilityBaseDO baseUpdate = new YzWaterFacilityBaseDO();
            baseUpdate.setId(exists.getFacilityId());
            baseUpdate.setGeom(baseGeom);
            baseUpdate.setGeomType(baseGeom.getGeometryType());
            baseUpdate.setSrid(BASE_SRID);
            baseUpdate.setUpdateTime(now);
            facilityBaseMapper.updateById(baseUpdate);
        }
    }

    private Geometry parseEditorGeometry(String geometryGeoJson) {
        try {
            JsonNode root = objectMapper.readTree(geometryGeoJson);
            JsonNode geometryNode = root;
            if (root != null && root.isObject()) {
                String type = StrUtil.trimToNull(root.path("type").asText(null));
                if ("Feature".equalsIgnoreCase(type) && root.has("geometry")) {
                    geometryNode = root.get("geometry");
                } else if ("FeatureCollection".equalsIgnoreCase(type)
                        && root.has("features")
                        && root.get("features").isArray()
                        && !root.get("features").isEmpty()) {
                    geometryNode = root.get("features").get(0).get("geometry");
                }
            }
            if (geometryNode == null || geometryNode.isNull()) {
                throw ServiceExceptionUtil.invalidParamException("几何为空");
            }
            JsonNode normalized = stripZFromGeometry(geometryNode);
            Geometry geometry = new GeometryJSON().read(objectMapper.writeValueAsString(normalized));
            if (geometry == null || geometry.isEmpty()) {
                throw ServiceExceptionUtil.invalidParamException("几何为空");
            }
            return geometry;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ServiceExceptionUtil.invalidParamException(
                    StrUtil.blankToDefault(ex.getMessage(), "GeoJSON 格式不正确"));
        }
    }

    private JsonNode stripZFromGeometry(JsonNode geometryNode) {
        if (geometryNode == null || !geometryNode.isObject()) {
            return geometryNode;
        }
        ObjectNode copy = geometryNode.deepCopy();
        JsonNode coordinates = copy.get("coordinates");
        if (coordinates != null) {
            copy.set("coordinates", stripZCoordinates(coordinates));
        }
        return copy;
    }

    private JsonNode stripZCoordinates(JsonNode node) {
        if (node == null || !node.isArray()) {
            return node;
        }
        ArrayNode result = objectMapper.createArrayNode();
        if (!node.isEmpty() && node.get(0).isNumber()) {
            result.add(node.get(0));
            if (node.size() > 1) {
                result.add(node.get(1));
            }
            return result;
        }
        for (JsonNode child : node) {
            result.add(stripZCoordinates(child));
        }
        return result;
    }

    private Geometry ensureMultiPolygon(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof MultiPolygon) {
            return geometry;
        }
        if (geometry instanceof Polygon polygon) {
            return geometry.getFactory().createMultiPolygon(new Polygon[]{polygon});
        }
        throw ServiceExceptionUtil.invalidParamException("坑塘几何仅支持 Polygon / MultiPolygon");
    }

    private Geometry transformToBaseSrid(Geometry geometry) {
        Geometry cloned = (Geometry) geometry.copy();
        try {
            MathTransform transform = CRS.findMathTransform(
                    CRS.decode("EPSG:" + POND_SRID, true),
                    CRS.decode("EPSG:" + BASE_SRID, true),
                    true);
            cloned = JTS.transform(cloned, transform);
        } catch (Exception ex) {
            log.warn("坑塘几何转 4490 失败，按原坐标写入基础表：{}", ex.getMessage());
        }
        cloned.setSRID(BASE_SRID);
        return cloned;
    }

    /**
     * 按所选区划及其全部下属子级构建筛选范围（选镇可查该镇下全部村的坑塘）。
     */
    private List<String> buildVillageCodeScope(String villageCode) {
        if (StrUtil.isBlank(villageCode)) {
            return List.of();
        }
        String code = StrUtil.trim(villageCode);
        try {
            return buildAreaCodeScope(Long.parseLong(code));
        } catch (NumberFormatException ex) {
            return List.of(code);
        }
    }

    private List<String> buildAreaCodeScope(Long areaId) {
        if (areaId == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        result.add(String.valueOf(areaId));
        try {
            List<SystemAreaNode> children = systemAreaService.getAreaTreeChildren(areaId);
            collectAreaIds(children, result);
        } catch (Exception ignored) {
            // 行政区划子树加载失败时，降级为仅按当前节点查询
        }
        return result.stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
    }

    private void collectAreaIds(List<SystemAreaNode> nodes, List<String> result) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (SystemAreaNode node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            result.add(String.valueOf(node.getId()));
            collectAreaIds(node.getChildren(), result);
        }
    }

    private void applyPondFields(YzWaterPondDO target, WaterPondSaveReqVO reqVO) {
        target.setResourceName(StrUtil.trimToNull(reqVO.getResourceName()));
        target.setLocationDesc(StrUtil.trimToNull(reqVO.getLocationDesc()));
        target.setVillageName(StrUtil.trimToNull(reqVO.getVillageName()));
        target.setVillageCode(StrUtil.trimToNull(reqVO.getVillageCode()));
        target.setOwnerUnit(StrUtil.trimToNull(reqVO.getOwnerUnit()));
        target.setOwnerPerson(StrUtil.trimToNull(reqVO.getOwnerPerson()));
        target.setOwnershipType(StrUtil.trimToNull(reqVO.getOwnershipType()));
        target.setLandType(StrUtil.trimToNull(reqVO.getLandType()));
        target.setAreaSqm(reqVO.getAreaSqm());
        target.setAreaMu(reqVO.getAreaMu());
        target.setOccupyFarmArea(reqVO.getOccupyFarmArea());
        target.setEastTo(StrUtil.trimToNull(reqVO.getEastTo()));
        target.setSouthTo(StrUtil.trimToNull(reqVO.getSouthTo()));
        target.setWestTo(StrUtil.trimToNull(reqVO.getWestTo()));
        target.setNorthTo(StrUtil.trimToNull(reqVO.getNorthTo()));
        target.setUsageStatus(StrUtil.trimToNull(reqVO.getUsageStatus()));
        target.setResourceNature(StrUtil.trimToNull(reqVO.getResourceNature()));
        target.setOccupationStatus(StrUtil.trimToNull(reqVO.getOccupationStatus()));
        target.setSurveyor(StrUtil.trimToNull(reqVO.getSurveyor()));
        target.setSurveyorPhone(StrUtil.trimToNull(reqVO.getSurveyorPhone()));
        target.setRemark(StrUtil.trimToNull(reqVO.getRemark()));
        target.setResourceType(StrUtil.trimToNull(reqVO.getResourceType()));
        target.setCenterLon(reqVO.getCenterLon());
        target.setCenterLat(reqVO.getCenterLat());
    }

    private YzWaterPondDO requirePond(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("坑塘ID不能为空");
        }
        YzWaterPondDO pond = pondMapper.selectById(id);
        if (pond == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_091, "坑塘不存在或已被删除");
        }
        return pond;
    }
}
