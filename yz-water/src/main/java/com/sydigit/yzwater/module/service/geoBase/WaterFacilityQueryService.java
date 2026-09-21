package com.sydigit.yzwater.module.service.geoBase;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityAreaCountRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityCustomizeCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityGeometryUpdateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityGeomMigrateRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityMapItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityPageItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityUpdateReqVO;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityGeometryDO;
import com.sydigit.yzwater.module.dal.dataobject.pump.YzPumpStationDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.mysql.embankment.YzEmbankmentMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityGeometryMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.pump.YzPumpStationMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardMapper;
import com.sydigit.yzwater.module.dal.mysql.irrigation.YzIrrigationDistrictMapper;
import com.sydigit.yzwater.module.dal.mysql.pond.YzWaterPondMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.service.area.SystemAreaService;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaNode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

/**
 * 水利设施查询服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class WaterFacilityQueryService {

    /**
     * 雪花算法 ID 生成器（全局唯一）
     */
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzWaterFacilityGeometryMapper geometryMapper;
    private final DictDataCommonApi dictDataApi;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzPumpStationMapper pumpStationMapper;
    private final YzEmbankmentMapper embankmentMapper;
    private final YzSignboardMapper signboardMapper;
    private final YzFloodPreventionMaterialWarehouseMapper floodWarehouseMapper;
    private final YzIrrigationDistrictMapper irrigationDistrictMapper;
    private final YzWaterPondMapper waterPondMapper;
    private final SystemAreaService systemAreaService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_SRID = 4490;
    private static final int DEFAULT_SEARCH_LIMIT = 100;
    private static final String FACILITY_TYPE_CUSTOMIZE = "customize";
    private Map<String, String> facilityTypeLabelMap;

    /**
     * 迁移空间表数据到基础表（geomType/geom）
     */
    @Transactional(rollbackFor = Exception.class)
    public WaterFacilityGeomMigrateRespVO migrateGeometryToBase() {
        List<YzWaterFacilityGeometryDO> geometryList = geometryMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityGeometryDO>()
                .select(YzWaterFacilityGeometryDO::getFacilityId, YzWaterFacilityGeometryDO::getGeomType, YzWaterFacilityGeometryDO::getGeom));
        int success = 0;
        int failed = 0;
        for (YzWaterFacilityGeometryDO geometry : geometryList) {
            if (geometry.getFacilityId() == null) {
                failed++;
                continue;
            }
            YzWaterFacilityBaseDO base = baseMapper.selectById(geometry.getFacilityId());
            if (base == null) {
                failed++;
                continue;
            }
            base.setGeomType(geometry.getGeomType());
            base.setGeom(geometry.getGeom());
            baseMapper.updateById(base);
            success++;
        }
        WaterFacilityGeomMigrateRespVO respVO = new WaterFacilityGeomMigrateRespVO();
        respVO.setSuccessCount(success);
        respVO.setFailedCount(failed);
        respVO.setMessage("迁移完成");
        return respVO;
    }

    /**
     * 分页查询水利设施
     */
    public PageResult<WaterFacilityPageItemRespVO> getFacilityPage(WaterFacilityPageReqVO reqVO) {
        LambdaQueryWrapper<YzWaterFacilityBaseDO> wrapper = new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .like(StrUtil.isNotBlank(reqVO.getFacilityName()), YzWaterFacilityBaseDO::getFacilityName, reqVO.getFacilityName())
                .eq(StrUtil.isNotBlank(reqVO.getFacilityType()), YzWaterFacilityBaseDO::getFacilityType, reqVO.getFacilityType())
                .apply(StrUtil.isNotBlank(reqVO.getEcoType()), "attributes ->> 'sthd' = {0}", reqVO.getEcoType())
                .orderByAsc(YzWaterFacilityBaseDO::getCreateTime);
        PageResult<YzWaterFacilityBaseDO> page = baseMapper.selectPage(reqVO, wrapper);
        Map<String, String> typeLabelMap = loadFacilityTypeLabelMap();
        List<WaterFacilityPageItemRespVO> list = page.getList().stream().map(item -> {
            WaterFacilityPageItemRespVO vo = BeanUtils.toBean(item, WaterFacilityPageItemRespVO.class);
            vo.setFacilityType(resolveFacilityTypeLabel(item.getFacilityType(), typeLabelMap));
            vo.setCreateTime(item.getCreateTime() != null ? DATE_TIME_FORMATTER.format(item.getCreateTime()) : null);
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 按行政区划统计某类设施数量。
     *
     * <p>说明：行政区划编码来自基础表 admin_region_code，对应 /system/area/tree 的 id。</p>
     */
    public List<WaterFacilityAreaCountRespVO> getFacilityAreaCount(String facilityType) {
        String type = normalizeFacilityType(facilityType);
        if (type == null) {
            return List.of();
        }
        // 河道：按河道表 town 字段统计
        if (isRiverFacilityType(type)) {
            return getRiverAreaCount();
        }
        // 水库：按水库表 township 字段统计
        if (StrUtil.equalsIgnoreCase(type, "reservoir")) {
            return buildAreaCountResp(waterReservoirMapper.selectTownshipCountGroup());
        }
        // 闸站：按泵站表 division_code 字段统计
        if (StrUtil.equalsIgnoreCase(type, "pump_station")) {
            return buildAreaCountResp(pumpStationMapper.selectDivisionCountGroup());
        }
        // 提防：按堤防表 division_code 字段统计
        if (StrUtil.equalsIgnoreCase(type, "dike")) {
            return buildAreaCountResp(embankmentMapper.selectDivisionCountGroup());
        }
        // 公示牌：按公示牌表 admin_region 字段统计
        if (StrUtil.equalsIgnoreCase(type, "signboard")
                || StrUtil.equalsIgnoreCase(type, "public_notice")
                || StrUtil.equalsIgnoreCase(type, "publicnotice")) {
            return buildAreaCountResp(signboardMapper.selectAdminRegionCountGroup());
        }
        // 防汛物资仓库：按仓库表 division_code 字段统计
        if (StrUtil.equalsIgnoreCase(type, "flood_prevention_material")) {
            return buildAreaCountResp(floodWarehouseMapper.selectDivisionCountGroup());
        }
        // 灌区：按灌区表 division_code 字段统计
        if (StrUtil.equalsIgnoreCase(type, "irrigation")) {
            return buildAreaCountResp(irrigationDistrictMapper.selectDivisionCountGroup());
        }
        // 坑塘：按坑塘表 village_code 字段统计
        if (StrUtil.equalsIgnoreCase(type, "pond")) {
            return buildAreaCountResp(waterPondMapper.selectVillageCodeCountGroup());
        }
        QueryWrapper<YzWaterFacilityBaseDO> wrapper = new QueryWrapper<YzWaterFacilityBaseDO>()
                .select("admin_region_code as area_code", "count(1) as cnt")
                .eq("facility_type", type)
                .isNotNull("admin_region_code")
                .groupBy("admin_region_code");
        List<Map<String, Object>> rows = baseMapper.selectMaps(wrapper);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<WaterFacilityAreaCountRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String areaCode = row == null ? null : Objects.toString(row.get("area_code"), null);
            if (StrUtil.isBlank(areaCode)) {
                continue;
            }
            Long areaId;
            try {
                areaId = Long.parseLong(StrUtil.trim(areaCode));
            } catch (Exception ignore) {
                continue;
            }
            WaterFacilityAreaCountRespVO vo = new WaterFacilityAreaCountRespVO();
            vo.setAreaId(areaId);
            vo.setCount(toLong(row.get("cnt")));
            result.add(vo);
        }
        return result;
    }

    /**
     * 将统计 SQL 返回的 (area_code,cnt) 转换为接口返回对象。
     */
    private List<WaterFacilityAreaCountRespVO> buildAreaCountResp(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<WaterFacilityAreaCountRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String areaCode = row == null ? null : Objects.toString(row.get("area_code"), null);
            if (StrUtil.isBlank(areaCode)) {
                continue;
            }
            Long areaId;
            try {
                areaId = Long.parseLong(StrUtil.trim(areaCode));
            } catch (Exception ignore) {
                continue;
            }
            WaterFacilityAreaCountRespVO vo = new WaterFacilityAreaCountRespVO();
            vo.setAreaId(areaId);
            vo.setCount(toLong(row.get("cnt")));
            result.add(vo);
        }
        return result;
    }

    /**
     * 按行政区划查询设施点位列表（用于首页地图/弹窗）。
     */
    public List<WaterFacilityMapItemRespVO> getFacilityListByArea(String facilityType, Long areaId) {
        String type = normalizeFacilityType(facilityType);
        if (type == null || areaId == null) {
            return List.of();
        }
        List<String> areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.isEmpty()) {
            return List.of();
        }
        // 河道：使用河道表 town 匹配行政区划（包含子级）
        if (isRiverFacilityType(type)) {
            return getRiverListByAreaCodes(String.valueOf(areaId), areaCodes);
        }
        // 闸站：使用泵站表 division_code 匹配行政区划（包含子级）
        if (StrUtil.equalsIgnoreCase(type, "pump_station")) {
            return getPumpStationListByAreaCodes(String.valueOf(areaId), areaCodes);
        }
        // 提防：使用堤防表 division_code 匹配行政区划（包含子级）
        if (StrUtil.equalsIgnoreCase(type, "dike")) {
            return getEmbankmentListByAreaCodes(String.valueOf(areaId), areaCodes);
        }
        List<YzWaterFacilityBaseDO> list = baseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .select(YzWaterFacilityBaseDO::getId,
                        YzWaterFacilityBaseDO::getFacilityCode,
                        YzWaterFacilityBaseDO::getFacilityName,
                        YzWaterFacilityBaseDO::getFacilityType,
                        YzWaterFacilityBaseDO::getAdminRegionCode,
                        YzWaterFacilityBaseDO::getGeomType,
                        YzWaterFacilityBaseDO::getGeom)
                .eq(YzWaterFacilityBaseDO::getFacilityType, type)
                .in(YzWaterFacilityBaseDO::getAdminRegionCode, areaCodes)
                .orderByAsc(YzWaterFacilityBaseDO::getFacilityName));
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<WaterFacilityMapItemRespVO> result = new ArrayList<>(list.size());
        for (YzWaterFacilityBaseDO item : list) {
            if (item == null || item.getId() == null) {
                continue;
            }
            WaterFacilityMapItemRespVO vo = new WaterFacilityMapItemRespVO();
            vo.setId(item.getId());
            vo.setFacilityCode(StrUtil.blankToDefault(item.getFacilityCode(), ""));
            vo.setFacilityName(StrUtil.blankToDefault(item.getFacilityName(), ""));
            vo.setFacilityType(StrUtil.blankToDefault(item.getFacilityType(), ""));
            vo.setAdminRegionCode(StrUtil.blankToDefault(item.getAdminRegionCode(), ""));
            vo.setGeomType(StrUtil.blankToDefault(item.getGeomType(), ""));

            Geometry geom = item.getGeom();
            if (geom != null && !geom.isEmpty()) {
                try {
                    Point centroid = geom.getCentroid();
                    if (centroid != null) {
                        vo.setLongitude(BigDecimal.valueOf(centroid.getX()));
                        vo.setLatitude(BigDecimal.valueOf(centroid.getY()));
                    }
                } catch (Exception ignored) {
                    // 质心计算失败不影响列表返回
                }
            }
            result.add(vo);
        }
        return result;
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

    /**
     * 新增自定义图层设施（仅写入设施基础表）。
     *
     * <p>入库规则：</p>
     * <ul>
     *     <li>facilityType 固定为 customize</li>
     *     <li>geom 使用 GeoJSON.geometry 解析后写入，SRID 固定为 4490</li>
     *     <li>attributes 存储自定义 key/value</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomizeFacility(WaterFacilityCustomizeCreateReqVO reqVO) {
        if (reqVO == null) {
            throw ServiceExceptionUtil.invalidParamException("参数不能为空");
        }
        String name = StrUtil.trimToNull(reqVO.getFacilityName());
        if (name == null) {
            throw ServiceExceptionUtil.invalidParamException("设施名称不能为空");
        }
        String geometryGeoJson = StrUtil.trimToNull(reqVO.getGeometryGeoJson());
        if (geometryGeoJson == null) {
            throw ServiceExceptionUtil.invalidParamException("请绘制点/线/面后再保存");
        }

        GeometryJSON geometryJSON = new GeometryJSON();
        Geometry geometry;
        try {
            geometry = geometryJSON.read(geometryGeoJson);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_GEOJSON_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "格式不正确"));
        }
        if (geometry == null || geometry.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_GEOJSON_PARSE_ERROR, "几何为空");
        }
        geometry.setSRID(DEFAULT_SRID);

        Long id = SNOWFLAKE.nextId();
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(id);
        base.setFacilityCode(String.valueOf(id));
        base.setFacilityName(name);
        base.setFacilityType(FACILITY_TYPE_CUSTOMIZE);
        base.setAdminRegion(StrUtil.blankToDefault(reqVO.getAdminRegion(), null));
        base.setAdminRegionCode(StrUtil.blankToDefault(reqVO.getAdminRegionCode(), null));
        base.setManageUnit(StrUtil.blankToDefault(reqVO.getManageUnit(), null));
        base.setAttributes(reqVO.getAttributes());
        base.setGeomType(geometry.getGeometryType());
        base.setSrid(DEFAULT_SRID);
        base.setGeom(geometry);
        base.setSourceType("system");
        baseMapper.insert(base);
        return id;
    }

    /**
     * 按设施名称模糊查询设施列表（用于首页搜索）。
     *
     * <p>逻辑：先根据 facilityType 确认业务类型，再到对应业务表按名称筛选，最终返回基础表主键与点位信息。</p>
     */
    public List<WaterFacilityMapItemRespVO> getFacilityListByName(String facilityType, String name, Integer limit) {
        String rawType = StrUtil.trimToNull(facilityType);
        String type = normalizeFacilityType(rawType);
        String keyword = StrUtil.trimToNull(name);
        if (type == null || keyword == null) {
            return List.of();
        }
        int size = normalizeLimit(limit);

        // 先确认基础表存在该 facilityType（避免无效类型走到业务查询）
        Long exists = baseMapper.selectCount(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .eq(YzWaterFacilityBaseDO::getFacilityType, type));
        if (exists == null || exists == 0) {
            return List.of();
        }

        // 查询业务表获取 facilityId + 名称 +（可选）经纬度回退值
        Map<Long, String> nameMap = new HashMap<>();
        Map<Long, BigDecimal> lonMap = new HashMap<>();
        Map<Long, BigDecimal> latMap = new HashMap<>();
        List<Long> facilityIds = new ArrayList<>();

        if (StrUtil.equalsIgnoreCase(type, "reservoir")) {
            List<YzWaterReservoirDO> list = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                    .select(YzWaterReservoirDO::getFacilityId,
                            YzWaterReservoirDO::getReservoirName,
                            YzWaterReservoirDO::getLongitude,
                            YzWaterReservoirDO::getLatitude)
                    .like(YzWaterReservoirDO::getReservoirName, keyword)
                    .orderByAsc(YzWaterReservoirDO::getReservoirName)
                    .last("limit " + size));
            fillSearchMaps(list, facilityIds, nameMap, lonMap, latMap,
                    YzWaterReservoirDO::getFacilityId,
                    YzWaterReservoirDO::getReservoirName,
                    YzWaterReservoirDO::getLongitude,
                    YzWaterReservoirDO::getLatitude);
        } else if (StrUtil.equalsIgnoreCase(type, "pump_station")) {
            List<YzPumpStationDO> list = pumpStationMapper.selectList(new LambdaQueryWrapper<YzPumpStationDO>()
                    .select(YzPumpStationDO::getFacilityId,
                            YzPumpStationDO::getPumpStationName,
                            YzPumpStationDO::getLongitude,
                            YzPumpStationDO::getLatitude)
                    .like(YzPumpStationDO::getPumpStationName, keyword)
                    .orderByAsc(YzPumpStationDO::getPumpStationName)
                    .last("limit " + size));
            fillSearchMaps(list, facilityIds, nameMap, lonMap, latMap,
                    YzPumpStationDO::getFacilityId,
                    YzPumpStationDO::getPumpStationName,
                    YzPumpStationDO::getLongitude,
                    YzPumpStationDO::getLatitude);
        } else if (isRiverFacilityType(type)) {
            List<YzRiverChannelDO> list = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                    .select(YzRiverChannelDO::getFacilityId,
                            YzRiverChannelDO::getRiverName,
                            YzRiverChannelDO::getCentroidLongitude,
                            YzRiverChannelDO::getCentroidLatitude)
                    .like(YzRiverChannelDO::getRiverName, keyword)
                    .orderByAsc(YzRiverChannelDO::getRiverName)
                    .last("limit " + size));
            fillSearchMaps(list, facilityIds, nameMap, lonMap, latMap,
                    YzRiverChannelDO::getFacilityId,
                    YzRiverChannelDO::getRiverName,
                    YzRiverChannelDO::getCentroidLongitude,
                    YzRiverChannelDO::getCentroidLatitude);
        } else if (StrUtil.equalsIgnoreCase(type, "river_section")) {
            List<YzRiverSectionDO> list = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                    .select(YzRiverSectionDO::getFacilityId, YzRiverSectionDO::getSectionName)
                    .like(YzRiverSectionDO::getSectionName, keyword)
                    .orderByAsc(YzRiverSectionDO::getSectionName)
                    .last("limit " + size));
            fillSearchMaps(list, facilityIds, nameMap, lonMap, latMap,
                    YzRiverSectionDO::getFacilityId,
                    YzRiverSectionDO::getSectionName,
                    null,
                    null);
        } else if (StrUtil.equalsIgnoreCase(type, "dike")) {
            List<YzEmbankmentDO> list = embankmentMapper.selectList(new LambdaQueryWrapper<YzEmbankmentDO>()
                    .select(YzEmbankmentDO::getFacilityId,
                            YzEmbankmentDO::getEmbankmentName,
                            YzEmbankmentDO::getLongitude,
                            YzEmbankmentDO::getLatitude)
                    .like(YzEmbankmentDO::getEmbankmentName, keyword)
                    .orderByAsc(YzEmbankmentDO::getEmbankmentName)
                    .last("limit " + size));
            fillSearchMaps(list, facilityIds, nameMap, lonMap, latMap,
                    YzEmbankmentDO::getFacilityId,
                    YzEmbankmentDO::getEmbankmentName,
                    YzEmbankmentDO::getLongitude,
                    YzEmbankmentDO::getLatitude);
        } else {
            // 未识别的类型：回退到基础表按名称模糊查询
            List<YzWaterFacilityBaseDO> baseList = baseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                    .select(YzWaterFacilityBaseDO::getId,
                            YzWaterFacilityBaseDO::getFacilityCode,
                            YzWaterFacilityBaseDO::getFacilityName,
                            YzWaterFacilityBaseDO::getFacilityType,
                            YzWaterFacilityBaseDO::getAdminRegionCode,
                            YzWaterFacilityBaseDO::getGeomType,
                            YzWaterFacilityBaseDO::getGeom)
                    .eq(YzWaterFacilityBaseDO::getFacilityType, type)
                    .like(YzWaterFacilityBaseDO::getFacilityName, keyword)
                    .orderByAsc(YzWaterFacilityBaseDO::getFacilityName)
                    .last("limit " + size));
            return buildMapItemsFromBase(baseList, Map.of(), Map.of(), Map.of());
        }

        if (facilityIds.isEmpty()) {
            return List.of();
        }
        List<YzWaterFacilityBaseDO> baseList = baseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .select(YzWaterFacilityBaseDO::getId,
                        YzWaterFacilityBaseDO::getFacilityCode,
                        YzWaterFacilityBaseDO::getFacilityName,
                        YzWaterFacilityBaseDO::getFacilityType,
                        YzWaterFacilityBaseDO::getAdminRegionCode,
                        YzWaterFacilityBaseDO::getGeomType,
                        YzWaterFacilityBaseDO::getGeom)
                .in(YzWaterFacilityBaseDO::getId, facilityIds));
        return buildMapItemsFromBase(baseList, nameMap, lonMap, latMap);
    }

    /**
     * 将 facilityType 统一到系统内部通用取值，避免前端/字典出现别名导致查询不到数据。
     */
    private String normalizeFacilityType(String facilityType) {
        String type = StrUtil.trimToNull(facilityType);
        if (type == null) {
            return null;
        }
        if (isRiverFacilityType(type)) {
            return "river";
        }
        return type;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) return DEFAULT_SEARCH_LIMIT;
        int val = limit;
        if (val <= 0) return DEFAULT_SEARCH_LIMIT;
        return Math.min(val, 500);
    }

    private <T> void fillSearchMaps(List<T> list,
                                   List<Long> facilityIds,
                                   Map<Long, String> nameMap,
                                   Map<Long, BigDecimal> lonMap,
                                   Map<Long, BigDecimal> latMap,
                                   java.util.function.Function<T, Long> facilityIdGetter,
                                   java.util.function.Function<T, String> nameGetter,
                                   java.util.function.Function<T, BigDecimal> lonGetter,
                                   java.util.function.Function<T, BigDecimal> latGetter) {
        if (list == null || list.isEmpty()) return;
        for (T item : list) {
            if (item == null) continue;
            Long fid = facilityIdGetter != null ? facilityIdGetter.apply(item) : null;
            if (fid == null) continue;
            if (!facilityIds.contains(fid)) {
                facilityIds.add(fid);
            }
            String n = nameGetter != null ? StrUtil.blankToDefault(nameGetter.apply(item), "").trim() : "";
            if (StrUtil.isNotBlank(n)) {
                nameMap.put(fid, n);
            }
            if (lonGetter != null) {
                BigDecimal lon = lonGetter.apply(item);
                if (lon != null) lonMap.put(fid, lon);
            }
            if (latGetter != null) {
                BigDecimal lat = latGetter.apply(item);
                if (lat != null) latMap.put(fid, lat);
            }
        }
    }

    private List<WaterFacilityMapItemRespVO> buildMapItemsFromBase(List<YzWaterFacilityBaseDO> baseList,
                                                                  Map<Long, String> nameMap,
                                                                  Map<Long, BigDecimal> lonMap,
                                                                  Map<Long, BigDecimal> latMap) {
        if (baseList == null || baseList.isEmpty()) {
            return List.of();
        }
        List<WaterFacilityMapItemRespVO> result = new ArrayList<>(baseList.size());
        for (YzWaterFacilityBaseDO base : baseList) {
            if (base == null || base.getId() == null) continue;
            WaterFacilityMapItemRespVO vo = new WaterFacilityMapItemRespVO();
            vo.setId(base.getId());
            vo.setFacilityCode(StrUtil.blankToDefault(base.getFacilityCode(), ""));
            vo.setFacilityType(StrUtil.blankToDefault(base.getFacilityType(), ""));
            vo.setAdminRegionCode(StrUtil.blankToDefault(base.getAdminRegionCode(), ""));
            vo.setGeomType(StrUtil.blankToDefault(base.getGeomType(), ""));

            String name = nameMap.get(base.getId());
            vo.setFacilityName(StrUtil.blankToDefault(name, StrUtil.blankToDefault(base.getFacilityName(), "")));

            Geometry geom = base.getGeom();
            if (geom != null && !geom.isEmpty()) {
                try {
                    Point centroid = geom.getCentroid();
                    if (centroid != null) {
                        vo.setLongitude(BigDecimal.valueOf(centroid.getX()));
                        vo.setLatitude(BigDecimal.valueOf(centroid.getY()));
                    }
                } catch (Exception ignored) {
                    // 质心计算失败不影响返回
                }
            }
            if (vo.getLongitude() == null && lonMap.containsKey(base.getId())) {
                vo.setLongitude(lonMap.get(base.getId()));
            }
            if (vo.getLatitude() == null && latMap.containsKey(base.getId())) {
                vo.setLatitude(latMap.get(base.getId()));
            }
            result.add(vo);
        }
        result.sort((a, b) -> String.valueOf(a.getFacilityName()).compareTo(String.valueOf(b.getFacilityName())));
        return result;
    }

    /**
     * 详情查询
     */
    public WaterFacilityDetailRespVO getFacilityDetail(Long id) {
        YzWaterFacilityBaseDO base = baseMapper.selectById(id);
        if (base == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
        }
        WaterFacilityDetailRespVO respVO = BeanUtils.toBean(base, WaterFacilityDetailRespVO.class);
        Map<String, String> typeLabelMap = loadFacilityTypeLabelMap();
        respVO.setFacilityType(resolveFacilityTypeLabel(base.getFacilityType(), typeLabelMap));
        respVO.setFacilityTypeValue(base.getFacilityType());
        respVO.setCreateTime(base.getCreateTime() != null ? DATE_TIME_FORMATTER.format(base.getCreateTime()) : null);

        // 仅从基础表 yz_water_facility_base.geom 读取几何（yz_water_facility_geometry 已弃用）
        Geometry baseGeom = base.getGeom();
        if (baseGeom != null) {
            GeometryJSON geometryJSON = new GeometryJSON();
            StringWriter writer = new StringWriter();
            try {
                geometryJSON.write(baseGeom, writer);
                respVO.setGeometryGeoJson(writer.toString());
            } catch (Exception ignored) {
                // 如果转换失败，不阻断详情查询
            }
            respVO.setGeomType(StrUtil.blankToDefault(base.getGeomType(), baseGeom.getGeometryType()));
            Integer srid = base.getSrid();
            if (srid == null && baseGeom.getSRID() > 0) {
                srid = baseGeom.getSRID();
            }
            respVO.setSrid(srid);
        }
        respVO.setAttributes(base.getAttributes());
        return respVO;
    }

    /**
     * 修改设施（仅基础信息和扩展属性）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFacility(WaterFacilityUpdateReqVO reqVO) {
        YzWaterFacilityBaseDO base = baseMapper.selectById(reqVO.getId());
        if (base == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
        }
        base.setFacilityName(reqVO.getFacilityName());
        base.setFacilityType(reqVO.getFacilityType());
        base.setAdminRegion(reqVO.getAdminRegion());
        base.setAdminRegionCode(reqVO.getAdminRegionCode());
        base.setManageUnit(reqVO.getManageUnit());
        base.setSourceType(reqVO.getSourceType());
        base.setAttributes(reqVO.getAttributes());
        baseMapper.updateById(base);

        YzWaterFacilityGeometryDO geometry = geometryMapper.selectFirstOne(YzWaterFacilityGeometryDO::getFacilityId, reqVO.getId());
        if (geometry != null) {
            geometry.setAdminRegion(reqVO.getAdminRegion());
            geometry.setAdminRegionCode(reqVO.getAdminRegionCode());
            geometryMapper.updateById(geometry);
        }
    }

    /**
     * 更新设施几何（仅写入基础表 geom/geomType/srid）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFacilityGeometry(Long id, WaterFacilityGeometryUpdateReqVO reqVO) {
        YzWaterFacilityBaseDO base = baseMapper.selectById(id);
        if (base == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
        }
        String geometryGeoJson = reqVO != null ? reqVO.getGeometryGeoJson() : null;
        if (StrUtil.isBlank(geometryGeoJson)) {
            YzWaterFacilityBaseDO update = new YzWaterFacilityBaseDO();
            update.setId(id);
            update.setGeomType(null);
            update.setSrid(null);
            update.setGeom(null);
            baseMapper.updateById(update);
            return;
        }
        GeometryJSON geometryJSON = new GeometryJSON();
        Geometry geometry;
        try {
            geometry = geometryJSON.read(geometryGeoJson);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_GEOJSON_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "格式不正确"));
        }
        if (geometry == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_GEOJSON_PARSE_ERROR, "几何为空");
        }
        int srid = reqVO != null && reqVO.getSrid() != null ? reqVO.getSrid() : DEFAULT_SRID;
        geometry.setSRID(srid);

        YzWaterFacilityBaseDO update = new YzWaterFacilityBaseDO();
        update.setId(id);
        update.setGeomType(geometry.getGeometryType());
        update.setSrid(srid);
        update.setGeom(geometry);
        baseMapper.updateById(update);
    }

    /**
     * 删除设施（含空间表）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFacility(Long id) {
        YzWaterFacilityBaseDO base = baseMapper.selectById(id);
        if (base == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
        }
        geometryMapper.delete(new LambdaQueryWrapper<YzWaterFacilityGeometryDO>()
                .eq(YzWaterFacilityGeometryDO::getFacilityId, id));
        baseMapper.deleteById(id);
    }

    private Map<String, String> loadFacilityTypeLabelMap() {
        if (facilityTypeLabelMap == null || facilityTypeLabelMap.isEmpty()) {
            List<DictDataRespDTO> dicts = dictDataApi.getDictDataList(ZdConstants.ZD_SSLB);
            facilityTypeLabelMap = dicts.stream()
                    .filter(d -> StrUtil.isNotBlank(d.getValue()))
                    .collect(Collectors.toMap(DictDataRespDTO::getValue, DictDataRespDTO::getLabel, (a, b) -> a));
        }
        return facilityTypeLabelMap;
    }

    private String resolveFacilityTypeLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        String hit = map.get(value);
        return StrUtil.blankToDefault(hit, value);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception ignore) {
            return 0L;
        }
    }

    private boolean isRiverFacilityType(String facilityType) {
        if (StrUtil.isBlank(facilityType)) {
            return false;
        }
        String val = StrUtil.trim(facilityType).toLowerCase();
        // 兼容：河道类设施字典 value 可能存在不同命名
        return "river".equals(val) || "river_channel".equals(val) || "riverchannel".equals(val);
    }

    /**
     * 河道：按 town（乡镇/行政区划编码）统计数量。
     */
    private List<WaterFacilityAreaCountRespVO> getRiverAreaCount() {
        List<Map<String, Object>> rows = riverChannelMapper.selectTownCountGroup();
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<WaterFacilityAreaCountRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String areaCode = row == null ? null : Objects.toString(row.get("area_code"), null);
            if (StrUtil.isBlank(areaCode)) {
                continue;
            }
            Long areaId;
            try {
                areaId = Long.parseLong(StrUtil.trim(areaCode));
            } catch (Exception ignore) {
                continue;
            }
            WaterFacilityAreaCountRespVO vo = new WaterFacilityAreaCountRespVO();
            vo.setAreaId(areaId);
            vo.setCount(toLong(row.get("cnt")));
            result.add(vo);
        }
        return result;
    }

    /**
     * 河道：按 areaCode（行政区划编码）查询河道列表，并返回基础设施 ID 用于前端详情高亮。
     */
    private List<WaterFacilityMapItemRespVO> getRiverListByAreaCodes(String selectedAreaCode, List<String> areaCodes) {
        if (StrUtil.isBlank(selectedAreaCode) || areaCodes == null || areaCodes.isEmpty()) {
            return List.of();
        }
        List<YzRiverChannelDO> rivers = riverChannelMapper.selectRiverListByTowns(areaCodes.toArray(new String[0]));
        if (rivers == null || rivers.isEmpty()) {
            return List.of();
        }
        Set<Long> facilityIds = rivers.stream()
                .map(YzRiverChannelDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (facilityIds.isEmpty()) {
            return List.of();
        }
        List<YzWaterFacilityBaseDO> bases = baseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .select(YzWaterFacilityBaseDO::getId, YzWaterFacilityBaseDO::getFacilityCode, YzWaterFacilityBaseDO::getFacilityName)
                .in(YzWaterFacilityBaseDO::getId, facilityIds));
        Map<Long, YzWaterFacilityBaseDO> baseMap = new HashMap<>();
        for (YzWaterFacilityBaseDO base : bases) {
            if (base != null && base.getId() != null) {
                baseMap.put(base.getId(), base);
            }
        }
        List<WaterFacilityMapItemRespVO> result = new ArrayList<>(rivers.size());
        for (YzRiverChannelDO river : rivers) {
            if (river == null || river.getFacilityId() == null) {
                continue;
            }
            YzWaterFacilityBaseDO base = baseMap.get(river.getFacilityId());
            WaterFacilityMapItemRespVO vo = new WaterFacilityMapItemRespVO();
            // 注意：这里 id 返回基础设施表 ID，保证前端可直接调用 /water/facility/{id} 获取几何并高亮
            vo.setId(river.getFacilityId());
            vo.setFacilityCode(base == null ? "" : StrUtil.blankToDefault(base.getFacilityCode(), ""));
            vo.setFacilityName(base == null ? StrUtil.blankToDefault(river.getRiverName(), "") : StrUtil.blankToDefault(base.getFacilityName(), ""));
            vo.setFacilityType("river");
            vo.setAdminRegionCode(selectedAreaCode);
            vo.setLongitude(toBigDecimal(river.getCentroidLongitude()));
            vo.setLatitude(toBigDecimal(river.getCentroidLatitude()));
            vo.setGeomType("LINESTRING");
            result.add(vo);
        }
        return result;
    }

    private List<WaterFacilityMapItemRespVO> getPumpStationListByAreaCodes(String selectedAreaCode, List<String> areaCodes) {
        if (StrUtil.isBlank(selectedAreaCode) || areaCodes == null || areaCodes.isEmpty()) {
            return List.of();
        }
        List<YzPumpStationDO> list = pumpStationMapper.selectListByDivisionCodes(areaCodes.toArray(new String[0]));
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Set<Long> facilityIds = list.stream()
                .map(YzPumpStationDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (facilityIds.isEmpty()) {
            return List.of();
        }
        List<YzWaterFacilityBaseDO> bases = baseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .select(YzWaterFacilityBaseDO::getId, YzWaterFacilityBaseDO::getFacilityCode, YzWaterFacilityBaseDO::getFacilityName)
                .in(YzWaterFacilityBaseDO::getId, facilityIds));
        Map<Long, YzWaterFacilityBaseDO> baseMap = new HashMap<>();
        for (YzWaterFacilityBaseDO base : bases) {
            if (base != null && base.getId() != null) {
                baseMap.put(base.getId(), base);
            }
        }
        List<WaterFacilityMapItemRespVO> result = new ArrayList<>(list.size());
        for (YzPumpStationDO item : list) {
            if (item == null || item.getFacilityId() == null) {
                continue;
            }
            YzWaterFacilityBaseDO base = baseMap.get(item.getFacilityId());
            WaterFacilityMapItemRespVO vo = new WaterFacilityMapItemRespVO();
            vo.setId(item.getFacilityId());
            vo.setFacilityCode(base == null ? "" : StrUtil.blankToDefault(base.getFacilityCode(), ""));
            vo.setFacilityName(base == null ? StrUtil.blankToDefault(item.getPumpStationName(), "") : StrUtil.blankToDefault(base.getFacilityName(), ""));
            vo.setFacilityType("pump_station");
            vo.setAdminRegionCode(selectedAreaCode);
            vo.setLongitude(toBigDecimal(item.getLongitude()));
            vo.setLatitude(toBigDecimal(item.getLatitude()));
            vo.setGeomType("POINT");
            result.add(vo);
        }
        return result;
    }

    private List<WaterFacilityMapItemRespVO> getEmbankmentListByAreaCodes(String selectedAreaCode, List<String> areaCodes) {
        if (StrUtil.isBlank(selectedAreaCode) || areaCodes == null || areaCodes.isEmpty()) {
            return List.of();
        }
        List<YzEmbankmentDO> list = embankmentMapper.selectListByDivisionCodes(areaCodes.toArray(new String[0]));
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Set<Long> facilityIds = list.stream()
                .map(YzEmbankmentDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (facilityIds.isEmpty()) {
            return List.of();
        }
        List<YzWaterFacilityBaseDO> bases = baseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .select(YzWaterFacilityBaseDO::getId, YzWaterFacilityBaseDO::getFacilityCode, YzWaterFacilityBaseDO::getFacilityName)
                .in(YzWaterFacilityBaseDO::getId, facilityIds));
        Map<Long, YzWaterFacilityBaseDO> baseMap = new HashMap<>();
        for (YzWaterFacilityBaseDO base : bases) {
            if (base != null && base.getId() != null) {
                baseMap.put(base.getId(), base);
            }
        }
        List<WaterFacilityMapItemRespVO> result = new ArrayList<>(list.size());
        for (YzEmbankmentDO item : list) {
            if (item == null || item.getFacilityId() == null) {
                continue;
            }
            YzWaterFacilityBaseDO base = baseMap.get(item.getFacilityId());
            WaterFacilityMapItemRespVO vo = new WaterFacilityMapItemRespVO();
            vo.setId(item.getFacilityId());
            vo.setFacilityCode(base == null ? "" : StrUtil.blankToDefault(base.getFacilityCode(), ""));
            vo.setFacilityName(base == null ? StrUtil.blankToDefault(item.getEmbankmentName(), "") : StrUtil.blankToDefault(base.getFacilityName(), ""));
            vo.setFacilityType("dike");
            vo.setAdminRegionCode(selectedAreaCode);
            vo.setLongitude(toBigDecimal(item.getLongitude()));
            vo.setLatitude(toBigDecimal(item.getLatitude()));
            vo.setGeomType("LINESTRING");
            result.add(vo);
        }
        return result;
    }

    private BigDecimal toBigDecimal(BigDecimal val) {
        return val == null ? null : val;
    }
}
