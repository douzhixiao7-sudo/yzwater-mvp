package com.sydigit.yzwater.module.system.service.area;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.ip.core.Area;
import com.sydigit.yzwater.framework.ip.core.utils.AreaUtils;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaGeoJsonImportResult;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.system.enums.ErrorCodeConstants.*;

@Service
public class SystemAreaServiceImpl implements SystemAreaService {

    /**
     * GeoJSON 导入新增时默认父级（仪征市 321081）
     */
    private static final Long IMPORT_DEFAULT_PARENT_ID = 321081L;

    /**
     * 最多返回的错误条数，避免响应过大
     */
    private static final int MAX_ERROR_SIZE = 50;

    /**
     * 默认 GeoJSON 坐标系：CGCS2000 高斯克吕格投影（EPSG:4550）。
     *
     * <p>说明：本项目的行政区划几何最终统一写入 EPSG:4490（CGCS2000 经纬度）。</p>
     */
    private static final int DEFAULT_GEOJSON_SOURCE_SRID = 4550;

    @Resource
    private SystemAreaMapper systemAreaMapper;

    @Override
    public List<SystemAreaNode> getAreaTreeChildren(Long rootId) {
        Long rid = rootId == null ? (long) Area.ID_CHINA : rootId;
        List<SystemAreaDO> all = systemAreaMapper.selectAll();
        // 兜底：未刷库时，沿用框架内置数据，避免返回空树影响业务
        if (CollUtil.isEmpty(all)) {
            if (rid > Integer.MAX_VALUE || rid < Integer.MIN_VALUE) {
                return List.of();
            }
            Area root = AreaUtils.getArea(rid.intValue());
            Assert.notNull(root, "获取不到地区根节点");
            return buildTreeFromBuiltin(root.getChildren());
        }

        Map<Long, SystemAreaNode> nodeMap = new HashMap<>(all.size());
        for (SystemAreaDO area : all) {
            if (area == null || area.getId() == null) {
                continue;
            }
            SystemAreaNode node = new SystemAreaNode();
            node.setId(area.getId());
            node.setName(area.getName());
            node.setType(area.getType());
            node.setSort(area.getSort() == null ? 0 : area.getSort());
            node.setChildren(new ArrayList<>());
            nodeMap.put(area.getId(), node);
        }

        for (SystemAreaDO area : all) {
            if (area == null || area.getId() == null) {
                continue;
            }
            Long parentId = area.getParentId();
            if (parentId == null) {
                continue;
            }
            SystemAreaNode parent = nodeMap.get(parentId);
            if (parent == null) {
                continue;
            }
            SystemAreaNode child = nodeMap.get(area.getId());
            if (child != null) {
                parent.getChildren().add(child);
            }
        }

        SystemAreaNode root = nodeMap.get(rid);
        if (root == null) {
            return List.of();
        }
        sortTree(root);
        return root.getChildren() == null ? List.of() : root.getChildren();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshFromBuiltin() {
        Area root = AreaUtils.getArea(Area.ID_CHINA);
        Assert.notNull(root, "获取不到中国");

        List<SystemAreaDO> list = new ArrayList<>(4096);
        // 为了保持树结构完整，将中国节点一并写入，parentId 统一设置为 0
        flattenBuiltinArea(root, 0L, 0, list);

        systemAreaMapper.deleteAllPhysical();
        systemAreaMapper.insertBatch(list, 1000);
        return list.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemAreaGeoJsonImportResult importGeoJson(byte[] geoJsonBytes) {
        if (geoJsonBytes == null || geoJsonBytes.length == 0) {
            throw exception(AREA_PARAM_INVALID);
        }

        JsonNode root = JsonUtils.parseTree(geoJsonBytes);
        JsonNode features = root.path("features");
        if (!features.isArray()) {
            throw exception(AREA_PARAM_INVALID);
        }

        // 预加载，减少循环内的数据库查询次数
        List<SystemAreaDO> all = systemAreaMapper.selectAll();
        Map<String, Long> nameToId = new HashMap<>(all.size());
        for (SystemAreaDO item : all) {
            if (item == null || item.getId() == null || StrUtil.isBlank(item.getName())) {
                continue;
            }
            nameToId.putIfAbsent(item.getName(), item.getId());
        }

        AtomicInteger total = new AtomicInteger();
        AtomicInteger created = new AtomicInteger();
        AtomicInteger updated = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        List<String> errors = new ArrayList<>();

        for (JsonNode feature : features) {
            total.incrementAndGet();

            JsonNode properties = feature.path("properties");
            String name = StrUtil.trimToNull(properties.path("name").asText(null));
            if (name == null) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少 properties.name，已跳过");
                continue;
            }

            JsonNode geometry = feature.get("geometry");
            if (geometry == null || geometry.isNull()) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少 geometry，已跳过（name=" + name + "）");
                continue;
            }
            String geometryGeoJson = geometry.toString();

            Long existId = nameToId.get(name);
            if (existId != null) {
                systemAreaMapper.updateGemoByIdFromGeoJson(existId, geometryGeoJson);
                updated.incrementAndGet();
                continue;
            }

            // 未按 name 匹配到，则按 properties.id 新增（或按 id 更新名称）
            Long id = parseFeatureId(properties, feature);
            if (id == null) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少可用的 id，已跳过（name=" + name + "）");
                continue;
            }

            SystemAreaDO existById = systemAreaMapper.selectById(id);
            if (existById != null) {
                // 以文件为准修正名称，避免后续无法按 name 匹配
                SystemAreaDO updateObj = new SystemAreaDO();
                updateObj.setId(id);
                updateObj.setName(name);
                systemAreaMapper.updateById(updateObj);
                systemAreaMapper.updateGemoByIdFromGeoJson(id, geometryGeoJson);
                nameToId.putIfAbsent(name, id);
                updated.incrementAndGet();
                continue;
            }

            SystemAreaDO insertObj = new SystemAreaDO();
            insertObj.setId(id);
            insertObj.setParentId(IMPORT_DEFAULT_PARENT_ID);
            insertObj.setName(name);
            insertObj.setType(0);
            insertObj.setSort(0);
            systemAreaMapper.insert(insertObj);
            systemAreaMapper.updateGemoByIdFromGeoJson(id, geometryGeoJson);
            nameToId.putIfAbsent(name, id);
            created.incrementAndGet();
        }

        SystemAreaGeoJsonImportResult result = new SystemAreaGeoJsonImportResult();
        result.setTotal(total.get());
        result.setCreated(created.get());
        result.setUpdated(updated.get());
        result.setSkipped(skipped.get());
        result.setErrors(errors);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemAreaGeoJsonImportResult importGeoJsonByAdcode(byte[] geoJsonBytes) {
        if (geoJsonBytes == null || geoJsonBytes.length == 0) {
            throw exception(AREA_PARAM_INVALID);
        }

        JsonNode root = JsonUtils.parseTree(geoJsonBytes);
        JsonNode features = root.path("features");
        if (!features.isArray()) {
            throw exception(AREA_PARAM_INVALID);
        }

        // 预加载，减少循环内的数据库查询次数
        List<SystemAreaDO> all = systemAreaMapper.selectAll();
        Map<Long, Boolean> existIdMap = new HashMap<>(all.size());
        for (SystemAreaDO item : all) {
            if (item == null || item.getId() == null) {
                continue;
            }
            existIdMap.put(item.getId(), Boolean.TRUE);
        }

        AtomicInteger total = new AtomicInteger();
        AtomicInteger created = new AtomicInteger();
        AtomicInteger updated = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        List<String> errors = new ArrayList<>();

        for (JsonNode feature : features) {
            total.incrementAndGet();

            JsonNode properties = feature.path("properties");
            Long adcode = parseAdcode(properties);
            if (adcode == null) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少 properties.adcode，已跳过");
                continue;
            }

            JsonNode geometry = feature.get("geometry");
            if (geometry == null || geometry.isNull()) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少 geometry，已跳过（adcode=" + adcode + "）");
                continue;
            }
            String geometryGeoJson = geometry.toString();

            if (!existIdMap.containsKey(adcode)) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素的 adcode 未匹配到 system_area.id，已跳过（adcode=" + adcode + "）");
                continue;
            }

            try {
                int rows = systemAreaMapper.updateGemoByIdFromGeoJson(adcode, geometryGeoJson);
                if (rows > 0) {
                    updated.incrementAndGet();
                } else {
                    skipped.incrementAndGet();
                    addError(errors, "第 " + total.get() + " 个要素写入失败（可能已被逻辑删除），已跳过（adcode=" + adcode + "）");
                }
            } catch (Exception ex) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素写入异常，已跳过（adcode=" + adcode + "）：" + ex.getMessage());
            }
        }

        SystemAreaGeoJsonImportResult result = new SystemAreaGeoJsonImportResult();
        result.setTotal(total.get());
        result.setCreated(created.get());
        result.setUpdated(updated.get());
        result.setSkipped(skipped.get());
        result.setErrors(errors);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemAreaGeoJsonImportResult importGeoJsonChildrenByFromEntiId(byte[] geoJsonBytes) {
        if (geoJsonBytes == null || geoJsonBytes.length == 0) {
            throw exception(AREA_PARAM_INVALID);
        }

        JsonNode root = JsonUtils.parseTree(geoJsonBytes);
        JsonNode features = root.path("features");
        if (!features.isArray()) {
            throw exception(AREA_PARAM_INVALID);
        }

        Integer srid = detectGeoJsonSrid(root);
        if (srid == null) {
            // 未声明 crs 时，默认按 CGCS2000 高斯克吕格投影（EPSG:4550）解析
            srid = DEFAULT_GEOJSON_SOURCE_SRID;
        }

        // 预加载，减少循环内的数据库查询次数
        List<SystemAreaDO> all = systemAreaMapper.selectAll();
        Map<Long, Boolean> existIdMap = new HashMap<>(all.size());
        for (SystemAreaDO item : all) {
            if (item == null || item.getId() == null) {
                continue;
            }
            existIdMap.put(item.getId(), Boolean.TRUE);
        }

        AtomicInteger total = new AtomicInteger();
        AtomicInteger created = new AtomicInteger();
        AtomicInteger updated = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        List<String> errors = new ArrayList<>();

        for (JsonNode feature : features) {
            total.incrementAndGet();

            JsonNode properties = feature.path("properties");
            Long parentId = parseLongProperty(properties, "FROMENTIID");
            if (parentId == null) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少 properties.FROMENTIID，已跳过");
                continue;
            }
            if (!existIdMap.containsKey(parentId)) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素的 FROMENTIID 未匹配到 system_area.id，已跳过（FROMENTIID=" + parentId + "）");
                continue;
            }

            Long id = parseLongProperty(properties, "ENTIID");
            if (id == null) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少 properties.ENTIID，已跳过（FROMENTIID=" + parentId + "）");
                continue;
            }

            String name = StrUtil.trimToNull(properties.path("NAME").asText(null));
            if (name == null) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少 properties.NAME，已跳过（ENTIID=" + id + "）");
                continue;
            }

            String geometryGeoJson = buildGeometryGeoJson(feature, properties);
            if (geometryGeoJson == null) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素缺少可用的几何坐标，已跳过（ENTIID=" + id + "）");
                continue;
            }

            try {
                SystemAreaDO exist = systemAreaMapper.selectById(id);
                if (exist == null) {
                    SystemAreaDO insertObj = new SystemAreaDO();
                    insertObj.setId(id);
                    insertObj.setParentId(parentId);
                    insertObj.setName(name);
                    insertObj.setType(6);
                    insertObj.setSort(0);
                    systemAreaMapper.insert(insertObj);
                    created.incrementAndGet();
                } else {
                    SystemAreaDO updateObj = new SystemAreaDO();
                    updateObj.setId(id);
                    updateObj.setParentId(parentId);
                    updateObj.setName(name);
                    updateObj.setType(6);
                    systemAreaMapper.updateById(updateObj);
                    updated.incrementAndGet();
                }

                int rows = systemAreaMapper.updateGemoByIdFromGeoJsonWithTransform(id, geometryGeoJson, srid);
                if (rows <= 0) {
                    skipped.incrementAndGet();
                    addError(errors, "第 " + total.get() + " 个要素几何写入失败（可能已被逻辑删除），已跳过（ENTIID=" + id + "）");
                }
            } catch (Exception ex) {
                skipped.incrementAndGet();
                addError(errors, "第 " + total.get() + " 个要素导入异常，已跳过（ENTIID=" + id + "）：" + ex.getMessage());
            }
        }

        SystemAreaGeoJsonImportResult result = new SystemAreaGeoJsonImportResult();
        result.setTotal(total.get());
        result.setCreated(created.get());
        result.setUpdated(updated.get());
        result.setSkipped(skipped.get());
        result.setErrors(errors);
        return result;
    }

    /**
     * 从 GeoJSON 顶层 crs 中识别 EPSG 编号。
     *
     * <p>示例：</p>
     * <ul>
     *     <li>{"crs":{"type":"name","properties":{"name":"urn:ogc:def:crs:EPSG::4490"}}}</li>
     *     <li>{"crs":{"properties":{"name":"EPSG:4549"}}}</li>
     * </ul>
     */
    private Integer detectGeoJsonSrid(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        JsonNode crs = root.get("crs");
        if (crs == null || crs.isNull()) {
            return null;
        }
        JsonNode nameNode = crs.path("properties").path("name");
        String name = StrUtil.trimToNull(nameNode.asText(null));
        if (name == null) {
            name = StrUtil.trimToNull(crs.path("name").asText(null));
        }
        if (name == null) {
            return null;
        }

        // 提取字符串中的 EPSG 数字（兼容 urn:ogc:def:crs:EPSG::4490 / EPSG:4490 等）
        String digits = name.replaceAll(".*?(\\d+)$", "$1");
        if (StrUtil.isBlank(digits) || !digits.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public Long create(SystemAreaDO area) {
        if (area == null || area.getId() == null || StrUtil.isBlank(area.getName())) {
            throw exception(AREA_PARAM_INVALID);
        }
        // 校验主键唯一
        if (systemAreaMapper.selectById(area.getId()) != null) {
            throw exception(AREA_ID_DUPLICATE);
        }
        // 校验父节点
        validateParent(area.getId(), area.getParentId());

        normalizeArea(area);
        systemAreaMapper.insert(area);
        // gemo 为可选字段，只有传入时才更新
        if (area.getGemo() != null) {
            systemAreaMapper.updateGemoById(area.getId(), area.getGemo());
        }
        return area.getId();
    }

    @Override
    public void update(SystemAreaDO area) {
        if (area == null || area.getId() == null) {
            throw exception(AREA_PARAM_INVALID);
        }
        if (systemAreaMapper.selectById(area.getId()) == null) {
            throw exception(AREA_NOT_EXISTS);
        }
        validateParent(area.getId(), area.getParentId());

        normalizeArea(area);
        // 只更新基础字段，gemo 通过自定义 SQL 更新（避免引入几何依赖）
        SystemAreaDO updateObj = new SystemAreaDO();
        updateObj.setId(area.getId());
        updateObj.setParentId(area.getParentId());
        updateObj.setName(area.getName());
        updateObj.setType(area.getType());
        updateObj.setSort(area.getSort());
        systemAreaMapper.updateById(updateObj);

        // gemo：null 表示不修改；空字符串表示清空；非空表示更新
        if (area.getGemo() != null) {
            systemAreaMapper.updateGemoById(area.getId(), area.getGemo());
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        if (systemAreaMapper.selectById(id) == null) {
            throw exception(AREA_NOT_EXISTS);
        }
        if (systemAreaMapper.selectCountByParentId(id) > 0) {
            throw exception(AREA_EXISTS_CHILDREN);
        }
        systemAreaMapper.deleteByIdLogical(id);
    }

    @Override
    public SystemAreaDO get(Long id) {
        if (id == null) {
            return null;
        }
        SystemAreaDO area = systemAreaMapper.selectByIdWithGemo(id);
        if (area == null) {
            throw exception(AREA_NOT_EXISTS);
        }
        return area;
    }

    private void validateParent(Long id, Long parentId) {
        if (parentId == null) {
            return;
        }
        if (Objects.equals(id, parentId)) {
            throw exception(AREA_PARENT_ERROR);
        }
        // 约定：parentId=0 表示根节点
        if (parentId == 0L) {
            return;
        }
        if (systemAreaMapper.selectById(parentId) == null) {
            throw exception(AREA_PARENT_NOT_EXISTS);
        }
    }

    private void normalizeArea(SystemAreaDO area) {
        if (area.getParentId() == null) {
            area.setParentId(0L);
        }
        if (area.getSort() == null) {
            area.setSort(0);
        }
        if (area.getType() == null) {
            area.setType(0);
        }
        area.setName(StrUtil.trimToEmpty(area.getName()));
    }

    private void flattenBuiltinArea(Area area, Long parentId, int sort, List<SystemAreaDO> result) {
        if (area == null || area.getId() == null) {
            return;
        }
        SystemAreaDO row = new SystemAreaDO();
        row.setId((long) area.getId());
        row.setParentId(parentId);
        row.setName(area.getName());
        row.setType(area.getType());
        row.setSort(sort);
        result.add(row);

        if (CollUtil.isEmpty(area.getChildren())) {
            return;
        }
        for (int i = 0; i < area.getChildren().size(); i++) {
            flattenBuiltinArea(area.getChildren().get(i), (long) area.getId(), i, result);
        }
    }

    private void sortTree(SystemAreaNode node) {
        if (node == null || CollUtil.isEmpty(node.getChildren())) {
            return;
        }
        node.getChildren().sort(Comparator
                .comparingInt((SystemAreaNode n) -> n.getSort() == null ? 0 : n.getSort())
                .thenComparing(SystemAreaNode::getId, Comparator.nullsLast(Long::compareTo)));
        node.getChildren().forEach(this::sortTree);
    }

    private List<SystemAreaNode> buildTreeFromBuiltin(List<Area> children) {
        if (CollUtil.isEmpty(children)) {
            return List.of();
        }
        List<SystemAreaNode> result = new ArrayList<>(children.size());
        for (Area child : children) {
            if (child == null) {
                continue;
            }
            SystemAreaNode node = new SystemAreaNode();
            node.setId(child.getId() == null ? null : (long) child.getId());
            node.setName(child.getName());
            node.setSort(0);
            node.setChildren(buildTreeFromBuiltin(child.getChildren()));
            result.add(node);
        }
        return result;
    }

    private Long parseAdcode(JsonNode properties) {
        if (properties == null || properties.isNull()) {
            return null;
        }
        JsonNode node = properties.get("adcode");
        if (node == null || node.isNull()) {
            return null;
        }

        // 兼容 adcode 为数字或字符串两种情况
        try {
            if (node.isNumber()) {
                long v = node.asLong();
                return v <= 0 ? null : v;
            }
            String text = StrUtil.trimToNull(node.asText(null));
            if (text == null) {
                return null;
            }
            long v = Long.parseLong(text);
            return v <= 0 ? null : v;
        } catch (Exception ignore) {
            return null;
        }
    }

    private Long parseLongProperty(JsonNode properties, String key) {
        if (properties == null || properties.isNull() || StrUtil.isBlank(key)) {
            return null;
        }
        JsonNode node = properties.get(key);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            if (node.isNumber()) {
                long v = node.asLong();
                return v <= 0 ? null : v;
            }
            String text = StrUtil.trimToNull(node.asText(null));
            if (text == null) {
                return null;
            }
            long v = Long.parseLong(text);
            return v <= 0 ? null : v;
        } catch (Exception ignore) {
            return null;
        }
    }

    private String buildGeometryGeoJson(JsonNode feature, JsonNode properties) {
        if (feature != null) {
            JsonNode geometry = feature.get("geometry");
            if (geometry != null && !geometry.isNull()) {
                return geometry.toString();
            }
        }

        // 兼容：若文件未提供 geometry，则尝试使用 properties.coordinates 作为 Point
        if (properties == null || properties.isNull()) {
            return null;
        }
        JsonNode coordinates = properties.get("coordinates");
        if (coordinates == null || !coordinates.isArray() || coordinates.size() < 2) {
            return null;
        }

        // 这里不主动裁剪三维坐标，交给数据库侧 ST_Force2D 统一降维
        return "{\"type\":\"Point\",\"coordinates\":" + coordinates.toString() + "}";
    }

    private Long parseFeatureId(JsonNode properties, JsonNode feature) {
        String idText = StrUtil.trimToNull(properties.path("id").asText(null));
        if (idText == null) {
            idText = StrUtil.trimToNull(feature.path("id").asText(null));
        }
        if (idText == null) {
            return null;
        }
        try {
            return Long.parseLong(idText);
        } catch (Exception ignore) {
            return null;
        }
    }

    private void addError(List<String> errors, String message) {
        if (errors == null || message == null) {
            return;
        }
        if (errors.size() >= MAX_ERROR_SIZE) {
            return;
        }
        errors.add(message);
    }
}
