package com.sydigit.yzwater.module.service.irrigation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.irrigation.YzIrrigationDistrictDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.irrigation.YzIrrigationDistrictMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 灌区导入服务
 *
 * 说明：
 * 1) 支持上传 .json/.geojson 文件；
 * 2) 支持两种结构：
 *    - GeoJSON FeatureCollection：从 feature.geometry 读取几何，从 feature.properties 读取属性；
 *    - JSON 数组：每个元素支持 geometry/type/coordinates 任一组合；
 * 3) 属性映射：
 *    - SJKJMJ -> actualIrrigableArea
 *    - SJJBNT -> basicFarmlandAreaKm2
 * 4) 名称规则：facilityName 与 irrigationDistrictName 按顺序生成：灌区1、灌区2……
 * 5) facilityType 固定写入 irrigation，几何写入 yz_water_facility_base.geom（SRID=4490）
 */
@Service
@Validated
@Slf4j
public class IrrigationDistrictImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final String FACILITY_TYPE_IRRIGATION = "irrigation";
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final ObjectMapper objectMapper;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzIrrigationDistrictMapper irrigationDistrictMapper;

    public IrrigationDistrictImportService(ObjectMapper objectMapper,
                                          YzWaterFacilityBaseMapper baseMapper,
                                          YzIrrigationDistrictMapper irrigationDistrictMapper) {
        this.objectMapper = objectMapper;
        this.baseMapper = baseMapper;
        this.irrigationDistrictMapper = irrigationDistrictMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public IrrigationDistrictImportRespVO importIrrigationDistricts(MultipartFile file) {
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
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, "文件未包含有效数据");
        }

        List<YzWaterFacilityBaseDO> baseList = new ArrayList<>(records.size());
        List<YzIrrigationDistrictDO> irrigationList = new ArrayList<>(records.size());
        int geometryCount = 0;

        for (int i = 0; i < records.size(); i++) {
            int seq = i + 1;
            String name = "灌区" + seq;

            JsonNode record = records.get(i);
            JsonNode propertiesNode = resolvePropertiesNode(record);

            BigDecimal actualIrrigableArea = toBigDecimal(firstNonNull(propertiesNode, record, "SJKJMJ"));
            BigDecimal basicFarmlandAreaKm2 = toBigDecimal(firstNonNull(propertiesNode, record, "SJJBNT"));

            Geometry geometry = readGeometry(record);
            if (geometry != null) {
                geometryCount++;
            }

            Long baseId = SNOWFLAKE.nextId();
            String facilityCode = String.valueOf(baseId);

            YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
            base.setId(baseId);
            base.setFacilityCode(facilityCode);
            base.setFacilityName(name);
            base.setFacilityType(FACILITY_TYPE_IRRIGATION);
            base.setSourceType("import");
            if (geometry != null) {
                base.setGeom(geometry);
                base.setGeomType(geometry.getGeometryType());
                base.setSrid(TARGET_SRID);
            }
            baseList.add(base);

            YzIrrigationDistrictDO irrigation = new YzIrrigationDistrictDO();
            irrigation.setId(SNOWFLAKE.nextId());
            irrigation.setFacilityId(baseId);
            irrigation.setIrrigationDistrictCode(facilityCode);
            irrigation.setIrrigationDistrictName(name);
            irrigation.setActualIrrigableArea(actualIrrigableArea);
            irrigation.setBasicFarmlandAreaKm2(basicFarmlandAreaKm2);
            irrigation.setManagementUnit(null);
            irrigation.setIrrigationDistrictType(null);
            irrigationList.add(irrigation);
        }

        baseMapper.insertBatch(baseList);
        irrigationDistrictMapper.insertBatch(irrigationList);

        IrrigationDistrictImportRespVO respVO = new IrrigationDistrictImportRespVO();
        respVO.setFacilityCount(baseList.size());
        respVO.setIrrigationDistrictCount(irrigationList.size());
        respVO.setGeometryCount(geometryCount);
        respVO.setMessage("导入成功");
        return respVO;
    }

    private boolean isSupported(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private List<JsonNode> extractRecords(JsonNode root) {
        if (root == null || root.isNull()) {
            return List.of();
        }
        // GeoJSON FeatureCollection
        JsonNode features = root.get("features");
        if (features != null && features.isArray()) {
            List<JsonNode> list = new ArrayList<>();
            features.forEach(list::add);
            return list;
        }
        // JSON 数组
        if (root.isArray()) {
            List<JsonNode> list = new ArrayList<>();
            root.forEach(list::add);
            return list;
        }
        // 单对象
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

    private JsonNode firstNonNull(JsonNode primary, JsonNode fallback, String fieldName) {
        JsonNode v1 = getIgnoreCase(primary, fieldName);
        if (v1 != null && !v1.isNull() && !(v1.isTextual() && StrUtil.isBlank(v1.asText()))) {
            return v1;
        }
        JsonNode v2 = getIgnoreCase(fallback, fieldName);
        if (v2 != null && !v2.isNull() && !(v2.isTextual() && StrUtil.isBlank(v2.asText()))) {
            return v2;
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

    private Geometry readGeometry(JsonNode record) {
        if (record == null) {
            return null;
        }
        JsonNode geometryNode = record.get("geometry");
        if (geometryNode == null || geometryNode.isNull()) {
            // 尝试直接使用 type/coordinates
            JsonNode coordinates = record.get("coordinates");
            if (coordinates == null || coordinates.isNull()) {
                return null;
            }
            String type = StrUtil.trimToNull(textOf(record, "type"));
            if (type == null) {
                type = inferGeoJsonType(coordinates);
            }
            if (type == null) {
                return null;
            }
            ObjectNode created = objectMapper.createObjectNode();
            created.put("type", type);
            created.set("coordinates", coordinates);
            geometryNode = created;
        }

        Integer sourceSrid = intOf(record, "srid");
        if (sourceSrid == null) {
            sourceSrid = DEFAULT_SOURCE_SRID;
        }

        Geometry geometry;
        try {
            geometry = new GeometryJSON().read(objectMapper.writeValueAsString(geometryNode));
        } catch (Exception ex) {
            log.warn("灌区导入：几何解析失败，将按空几何处理，原因：{}", ex.getMessage());
            return null;
        }
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        geometry.setSRID(sourceSrid);
        return transformToTargetSrid(geometry, sourceSrid);
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
                log.warn("灌区导入：坐标转换失败，将按原坐标写入，原因：{}", ex.getMessage());
            }
        }
        cloned.setSRID(TARGET_SRID);
        return cloned;
    }

    private String textOf(JsonNode node, String fieldName) {
        JsonNode v = getIgnoreCase(node, fieldName);
        if (v == null || v.isNull()) {
            return null;
        }
        String raw = v.asText();
        return StrUtil.trimToNull(raw);
    }

    private Integer intOf(JsonNode node, String fieldName) {
        JsonNode v = getIgnoreCase(node, fieldName);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isInt() || v.isLong()) {
            return v.asInt();
        }
        try {
            String raw = StrUtil.trimToNull(v.asText());
            if (raw == null) {
                return null;
            }
            return Integer.parseInt(raw);
        } catch (Exception ex) {
            return null;
        }
    }

    private String inferGeoJsonType(JsonNode coordinates) {
        if (coordinates == null || !coordinates.isArray() || coordinates.isEmpty()) {
            return null;
        }
        JsonNode first = coordinates.get(0);
        // [x, y]
        if (first != null && first.isNumber()) {
            return "Point";
        }
        // [[x, y], ...]
        if (first != null && first.isArray() && !first.isEmpty() && first.get(0).isNumber()) {
            return "LineString";
        }
        // [[[x, y], ...], ...]
        if (first != null && first.isArray() && !first.isEmpty()) {
            JsonNode second = first.get(0);
            if (second != null && second.isArray() && !second.isEmpty() && second.get(0).isNumber()) {
                return "Polygon";
            }
        }
        // 更深层按 MultiPolygon 处理
        return "MultiPolygon";
    }
}
