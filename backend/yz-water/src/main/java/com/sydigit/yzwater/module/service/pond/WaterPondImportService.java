package com.sydigit.yzwater.module.service.pond;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.pond.YzWaterPondDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.pond.YzWaterPondMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 坑塘 GeoJSON 导入服务
 *
 * <p>按 resource_code(CHBH) upsert：已存在则覆盖属性与几何；不存在则新增。
 * 同一文件内 CHBH 重复则跳过后续条。</p>
 * <p>每条 Feature：基础表 yz_water_facility_base（facility_type=pond，geom=4490）
 * + 业务表 yz_water_pond（geom=4326）。</p>
 */
@Service
@Validated
@Slf4j
public class WaterPondImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int SOURCE_SRID = 4326;
    private static final String FACILITY_TYPE_POND = "pond";
    private static final int BATCH_SIZE = 200;
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final ObjectMapper objectMapper;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzWaterPondMapper pondMapper;

    public WaterPondImportService(ObjectMapper objectMapper,
                                  YzWaterFacilityBaseMapper baseMapper,
                                  YzWaterPondMapper pondMapper) {
        this.objectMapper = objectMapper;
        this.baseMapper = baseMapper;
        this.pondMapper = pondMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public WaterPondImportRespVO importGeoJson(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isSupported(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_TYPE_INVALID);
        }

        JsonNode root;
        try (InputStream inputStream = file.getInputStream()) {
            root = objectMapper.readTree(inputStream);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, ex.getMessage());
        }

        List<JsonNode> records = extractRecords(root);
        if (CollUtil.isEmpty(records)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, "文件未包含有效 Feature");
        }

        Map<String, ExistingPondRef> existingByCode = loadExistingPondRefs();

        List<YzWaterFacilityBaseDO> insertBaseList = new ArrayList<>();
        List<YzWaterPondDO> insertPondList = new ArrayList<>();
        int insertedCount = 0;
        int updatedCount = 0;
        int geometryCount = 0;
        int skippedCount = 0;
        Set<String> seenInFile = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();

        for (JsonNode record : records) {
            JsonNode propertiesNode = resolvePropertiesNode(record);
            String resourceCode = StrUtil.trimToNull(textOf(propertiesNode, "CHBH"));
            if (resourceCode == null) {
                resourceCode = firstText(propertiesNode, "以图管地编", "以图管");
            }

            // 同文件内重复 CHBH：跳过后续条
            if (resourceCode != null && !seenInFile.add(resourceCode)) {
                skippedCount++;
                continue;
            }

            ParsedPondFeature parsed = parseFeature(record, propertiesNode, resourceCode);
            if (parsed.sourceGeom != null) {
                geometryCount++;
            }

            ExistingPondRef existing = resourceCode == null ? null : existingByCode.get(resourceCode);
            if (existing != null) {
                updateExisting(existing, parsed, now);
                updatedCount++;
                continue;
            }

            Long baseId = SNOWFLAKE.nextId();
            String facilityCode = resourceCode != null ? resourceCode : String.valueOf(baseId);

            YzWaterFacilityBaseDO base = buildNewBase(baseId, facilityCode, parsed);
            YzWaterPondDO pond = buildNewPond(baseId, facilityCode, parsed, now);
            insertBaseList.add(base);
            insertPondList.add(pond);
            insertedCount++;

            if (resourceCode != null) {
                existingByCode.put(resourceCode, new ExistingPondRef(pond.getId(), baseId, resourceCode));
            }
        }

        insertInBatches(insertBaseList, insertPondList);

        WaterPondImportRespVO respVO = new WaterPondImportRespVO();
        respVO.setTotalCount(records.size());
        respVO.setFacilityCount(insertedCount + updatedCount);
        respVO.setPondCount(insertedCount + updatedCount);
        respVO.setInsertedCount(insertedCount);
        respVO.setUpdatedCount(updatedCount);
        respVO.setGeometryCount(geometryCount);
        respVO.setSkippedCount(skippedCount);
        respVO.setMessage(String.format(
                "导入完成：新增 %d 条，更新 %d 条，跳过 %d 条（文件内重复），几何 %d 条",
                insertedCount, updatedCount, skippedCount, geometryCount));
        return respVO;
    }

    private Map<String, ExistingPondRef> loadExistingPondRefs() {
        List<YzWaterPondDO> list = pondMapper.selectList(new LambdaQueryWrapper<YzWaterPondDO>()
                .select(YzWaterPondDO::getId, YzWaterPondDO::getResourceCode, YzWaterPondDO::getFacilityId)
                .isNotNull(YzWaterPondDO::getResourceCode));
        Map<String, ExistingPondRef> map = new HashMap<>();
        if (CollUtil.isEmpty(list)) {
            return map;
        }
        for (YzWaterPondDO pond : list) {
            if (pond == null) {
                continue;
            }
            String code = StrUtil.trimToNull(pond.getResourceCode());
            if (code == null || pond.getId() == null) {
                continue;
            }
            map.putIfAbsent(code, new ExistingPondRef(pond.getId(), pond.getFacilityId(), code));
        }
        return map;
    }

    private ParsedPondFeature parseFeature(JsonNode record, JsonNode propertiesNode, String resourceCode) {
        ParsedPondFeature parsed = new ParsedPondFeature();
        parsed.resourceCode = resourceCode;
        parsed.resourceName = StrUtil.blankToDefault(
                textOf(propertiesNode, "ZYMC"),
                resourceCode != null ? resourceCode : "未命名坑塘");
        parsed.villageCode = StrUtil.trimToNull(textOf(propertiesNode, "XZQDM"));
        if (parsed.villageCode == null) {
            parsed.villageCode = StrUtil.trimToNull(textOf(propertiesNode, "打印代码"));
        }
        String townName = firstText(propertiesNode, "镇");
        parsed.villageName = firstText(propertiesNode, "XZQMC", "村", "村名");
        if (parsed.villageName == null) {
            parsed.villageName = townName;
        }
        parsed.locationDesc = firstText(propertiesNode, "ZLWZ", "位置");
        if (parsed.locationDesc == null && (townName != null || parsed.villageName != null)) {
            parsed.locationDesc = StrUtil.nullToEmpty(townName) + StrUtil.nullToEmpty(parsed.villageName);
        }
        parsed.ownerUnit = textOf(propertiesNode, "QSDWMC");
        parsed.ownershipType = firstText(propertiesNode, "QSXZ", "土地权属", "土地权");
        parsed.resourceType = firstText(propertiesNode, "ZYLX", "资源类型");
        parsed.landType = firstText(propertiesNode, "国土地类", "国土地");
        parsed.remark = firstText(propertiesNode, "备注", "BZ");
        parsed.areaSqm = toBigDecimal(firstNonNull(propertiesNode, "Shape_Area", "Shape_Ar_1"));
        parsed.areaMu = toBigDecimal(getIgnoreCase(propertiesNode, "SCMJ"));
        parsed.occupyFarmArea = toBigDecimal(firstNonNull(propertiesNode, "占农经权面", "占农经权面积", "占农经"));
        parsed.eastTo = textOf(propertiesNode, "SZD");
        parsed.southTo = textOf(propertiesNode, "SZN");
        parsed.westTo = textOf(propertiesNode, "SZX");
        parsed.northTo = textOf(propertiesNode, "SZB");
        parsed.usageStatus = textOf(propertiesNode, "SYZT");
        parsed.resourceNature = textOf(propertiesNode, "ZYXZ");
        parsed.occupationStatus = textOf(propertiesNode, "ZYQK");
        parsed.surveyor = textOf(propertiesNode, "DCYXM");
        parsed.surveyorPhone = textOf(propertiesNode, "LXFS");

        parsed.sourceGeom = readSourceGeometry(record);
        if (parsed.sourceGeom != null) {
            parsed.baseGeom = transformToTargetSrid((Geometry) parsed.sourceGeom.copy(), SOURCE_SRID);
            Geometry pondGeom = ensureMultiPolygon((Geometry) parsed.sourceGeom.copy());
            pondGeom.setSRID(SOURCE_SRID);
            parsed.pondGeom = pondGeom;
        }
        return parsed;
    }

    private void updateExisting(ExistingPondRef existing, ParsedPondFeature parsed, LocalDateTime now) {
        YzWaterPondDO pondUpdate = new YzWaterPondDO();
        pondUpdate.setId(existing.pondId);
        pondUpdate.setResourceName(parsed.resourceName);
        pondUpdate.setLocationDesc(parsed.locationDesc);
        pondUpdate.setVillageName(parsed.villageName);
        pondUpdate.setVillageCode(parsed.villageCode);
        pondUpdate.setOwnerUnit(parsed.ownerUnit);
        pondUpdate.setOwnershipType(parsed.ownershipType);
        pondUpdate.setResourceType(parsed.resourceType);
        pondUpdate.setLandType(parsed.landType);
        pondUpdate.setRemark(parsed.remark);
        pondUpdate.setAreaSqm(parsed.areaSqm);
        pondUpdate.setAreaMu(parsed.areaMu);
        pondUpdate.setOccupyFarmArea(parsed.occupyFarmArea);
        pondUpdate.setEastTo(parsed.eastTo);
        pondUpdate.setSouthTo(parsed.southTo);
        pondUpdate.setWestTo(parsed.westTo);
        pondUpdate.setNorthTo(parsed.northTo);
        pondUpdate.setUsageStatus(parsed.usageStatus);
        pondUpdate.setResourceNature(parsed.resourceNature);
        pondUpdate.setOccupationStatus(parsed.occupationStatus);
        pondUpdate.setSurveyor(parsed.surveyor);
        pondUpdate.setSurveyorPhone(parsed.surveyorPhone);
        pondUpdate.setUpdateTime(now);
        if (parsed.pondGeom != null) {
            pondUpdate.setGeom(parsed.pondGeom);
            WaterPondGeometryHelper.applyCenterToPond(pondUpdate, parsed.pondGeom);
        }
        pondMapper.updateById(pondUpdate);

        if (existing.facilityId != null) {
            YzWaterFacilityBaseDO baseUpdate = new YzWaterFacilityBaseDO();
            baseUpdate.setId(existing.facilityId);
            baseUpdate.setFacilityName(parsed.resourceName);
            baseUpdate.setAdminRegion(parsed.villageName);
            baseUpdate.setAdminRegionCode(parsed.villageCode);
            baseUpdate.setManageUnit(parsed.ownerUnit);
            baseUpdate.setUpdateTime(now);
            if (parsed.baseGeom != null) {
                baseUpdate.setGeom(parsed.baseGeom);
                baseUpdate.setGeomType(parsed.baseGeom.getGeometryType());
                baseUpdate.setSrid(TARGET_SRID);
            }
            baseMapper.updateById(baseUpdate);
        }
    }

    private YzWaterFacilityBaseDO buildNewBase(Long baseId, String facilityCode, ParsedPondFeature parsed) {
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityCode(facilityCode);
        base.setFacilityName(parsed.resourceName);
        base.setFacilityType(FACILITY_TYPE_POND);
        base.setAdminRegion(parsed.villageName);
        base.setAdminRegionCode(parsed.villageCode);
        base.setManageUnit(parsed.ownerUnit);
        base.setSourceType("import");
        if (parsed.baseGeom != null) {
            base.setGeom(parsed.baseGeom);
            base.setGeomType(parsed.baseGeom.getGeometryType());
            base.setSrid(TARGET_SRID);
        }
        return base;
    }

    private YzWaterPondDO buildNewPond(Long baseId, String facilityCode, ParsedPondFeature parsed, LocalDateTime now) {
        YzWaterPondDO pond = new YzWaterPondDO();
        pond.setId(SNOWFLAKE.nextId());
        pond.setFacilityId(baseId);
        pond.setResourceCode(parsed.resourceCode != null ? parsed.resourceCode : facilityCode);
        pond.setResourceName(parsed.resourceName);
        pond.setLocationDesc(parsed.locationDesc);
        pond.setVillageName(parsed.villageName);
        pond.setVillageCode(parsed.villageCode);
        pond.setOwnerUnit(parsed.ownerUnit);
        pond.setOwnershipType(parsed.ownershipType);
        pond.setResourceType(parsed.resourceType);
        pond.setLandType(parsed.landType);
        pond.setRemark(parsed.remark);
        pond.setAreaSqm(parsed.areaSqm);
        pond.setAreaMu(parsed.areaMu);
        pond.setOccupyFarmArea(parsed.occupyFarmArea);
        pond.setEastTo(parsed.eastTo);
        pond.setSouthTo(parsed.southTo);
        pond.setWestTo(parsed.westTo);
        pond.setNorthTo(parsed.northTo);
        pond.setUsageStatus(parsed.usageStatus);
        pond.setResourceNature(parsed.resourceNature);
        pond.setOccupationStatus(parsed.occupationStatus);
        pond.setSurveyor(parsed.surveyor);
        pond.setSurveyorPhone(parsed.surveyorPhone);
        pond.setCreateTime(now);
        pond.setUpdateTime(now);
        if (parsed.pondGeom != null) {
            pond.setGeom(parsed.pondGeom);
            WaterPondGeometryHelper.applyCenterToPond(pond, parsed.pondGeom);
        }
        return pond;
    }

    private void insertInBatches(List<YzWaterFacilityBaseDO> baseList, List<YzWaterPondDO> pondList) {
        if (baseList.isEmpty()) {
            return;
        }
        for (int i = 0; i < baseList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, baseList.size());
            baseMapper.insertBatch(baseList.subList(i, end), BATCH_SIZE);
            pondMapper.insertBatch(pondList.subList(i, end), BATCH_SIZE);
        }
    }

    private boolean isSupported(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private List<JsonNode> extractRecords(JsonNode root) {
        if (root == null || root.isNull()) {
            return List.of();
        }
        JsonNode features = root.get("features");
        if (features != null && features.isArray()) {
            List<JsonNode> list = new ArrayList<>();
            features.forEach(list::add);
            return list;
        }
        if (root.isArray()) {
            List<JsonNode> list = new ArrayList<>();
            root.forEach(list::add);
            return list;
        }
        return List.of(root);
    }

    private JsonNode resolvePropertiesNode(JsonNode record) {
        if (record == null) {
            return null;
        }
        JsonNode props = record.get("properties");
        if (props != null && props.isObject()) {
            return props;
        }
        return null;
    }

    private Geometry readSourceGeometry(JsonNode record) {
        if (record == null) {
            return null;
        }
        JsonNode geometryNode = record.get("geometry");
        if (geometryNode == null || geometryNode.isNull()) {
            return null;
        }
        try {
            JsonNode normalized = stripZFromGeometry(geometryNode);
            Geometry geometry = new GeometryJSON().read(objectMapper.writeValueAsString(normalized));
            if (geometry == null || geometry.isEmpty()) {
                return null;
            }
            geometry = ensureMultiPolygon(geometry);
            geometry.setSRID(SOURCE_SRID);
            return geometry;
        } catch (Exception ex) {
            log.warn("坑塘导入：几何解析失败，将按空几何处理，原因：{}", ex.getMessage());
            return null;
        }
    }

    /**
     * GeoJSON 坐标常带 Z（如 [x,y,0]），GeometryJSON 对 3 维坐标不稳定，导入前压成二维。
     */
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
        return geometry;
    }

    private Geometry transformToTargetSrid(Geometry geometry, int sourceSrid) {
        if (geometry == null) {
            return null;
        }
        Geometry cloned = (Geometry) geometry.copy();
        if (sourceSrid != TARGET_SRID) {
            try {
                MathTransform transform = CRS.findMathTransform(
                        CRS.decode("EPSG:" + sourceSrid, true),
                        CRS.decode("EPSG:" + TARGET_SRID, true),
                        true);
                cloned = JTS.transform(cloned, transform);
            } catch (Exception ex) {
                log.warn("坑塘导入：坐标转换失败，将按原坐标写入基础表，原因：{}", ex.getMessage());
            }
        }
        cloned.setSRID(TARGET_SRID);
        return cloned;
    }

    private String firstText(JsonNode node, String... fieldNames) {
        if (fieldNames == null) {
            return null;
        }
        for (String fieldName : fieldNames) {
            String value = textOf(node, fieldName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private JsonNode firstNonNull(JsonNode node, String... fieldNames) {
        if (fieldNames == null) {
            return null;
        }
        for (String fieldName : fieldNames) {
            JsonNode value = getIgnoreCase(node, fieldName);
            if (value != null && !value.isNull() && !(value.isTextual() && StrUtil.isBlank(value.asText()))) {
                return value;
            }
        }
        return null;
    }

    private JsonNode getIgnoreCase(JsonNode node, String fieldName) {
        if (node == null || !node.isObject() || StrUtil.isBlank(fieldName)) {
            return null;
        }
        JsonNode direct = node.get(fieldName);
        if (direct != null) {
            return direct;
        }
        String target = fieldName.toLowerCase(Locale.ROOT);
        for (var it = node.fieldNames(); it.hasNext(); ) {
            String key = it.next();
            if (key != null && key.toLowerCase(Locale.ROOT).equals(target)) {
                return node.get(key);
            }
        }
        return null;
    }

    private String textOf(JsonNode node, String fieldName) {
        JsonNode v = getIgnoreCase(node, fieldName);
        if (v == null || v.isNull()) {
            return null;
        }
        return StrUtil.trimToNull(v.asText());
    }

    private BigDecimal toBigDecimal(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            String raw = node.isNumber() ? node.numberValue().toString() : StrUtil.trimToNull(node.asText());
            if (raw == null) {
                return null;
            }
            return new BigDecimal(raw);
        } catch (Exception ex) {
            return null;
        }
    }

    private static final class ExistingPondRef {
        private final Long pondId;
        private final Long facilityId;
        private final String resourceCode;

        private ExistingPondRef(Long pondId, Long facilityId, String resourceCode) {
            this.pondId = pondId;
            this.facilityId = facilityId;
            this.resourceCode = resourceCode;
        }
    }

    private static final class ParsedPondFeature {
        private String resourceCode;
        private String resourceName;
        private String villageCode;
        private String villageName;
        private String locationDesc;
        private String ownerUnit;
        private String ownershipType;
        private String resourceType;
        private String landType;
        private String remark;
        private BigDecimal areaSqm;
        private BigDecimal areaMu;
        private BigDecimal occupyFarmArea;
        private String eastTo;
        private String southTo;
        private String westTo;
        private String northTo;
        private String usageStatus;
        private String resourceNature;
        private String occupationStatus;
        private String surveyor;
        private String surveyorPhone;
        private Geometry sourceGeom;
        private Geometry baseGeom;
        private Geometry pondGeom;
    }
}
