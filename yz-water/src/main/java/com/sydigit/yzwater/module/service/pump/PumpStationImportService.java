package com.sydigit.yzwater.module.service.pump;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.pump.YzPumpStationDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.pump.YzPumpStationMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.service.dto.GeometryFeatureDTO;
import com.sydigit.yzwater.module.service.file.GeometryFeatureFileReader;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 泵站 GeoJSON 导入服务
 */
@Service
@Validated
@Slf4j
public class PumpStationImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final int MAX_JSON_NESTING = 20;
    private static final String DEPTH_TRUNCATED_PLACEHOLDER = "[嵌套层级过深已截断]";
    private static final String FACILITY_TYPE_PUMP_STATION = "pump_station";
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final GeometryFeatureFileReader featureFileReader;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzPumpStationMapper pumpStationMapper;

    public PumpStationImportService(GeometryFeatureFileReader featureFileReader,
                                    YzWaterFacilityBaseMapper baseMapper,
                                    YzPumpStationMapper pumpStationMapper) {
        this.featureFileReader = featureFileReader;
        this.baseMapper = baseMapper;
        this.pumpStationMapper = pumpStationMapper;
    }

    /**
     * 上传泵站 GeoJSON 并导入基础表和泵站表
     */
    @Transactional(rollbackFor = Exception.class)
    public PumpStationImportRespVO importPumpStations(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isGeoJson(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_TYPE_INVALID);
        }
        List<GeometryFeatureDTO> features;
        try (InputStream inputStream = file.getInputStream()) {
            features = featureFileReader.readGeoJson(inputStream, filename, filename);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, ex.getMessage());
        }
        if (CollUtil.isEmpty(features)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, "文件未包含有效泵站要素");
        }

        List<YzWaterFacilityBaseDO> baseList = new ArrayList<>();
        List<YzPumpStationDO> pumpList = new ArrayList<>();

        for (GeometryFeatureDTO feature : features) {
            Geometry geometry = convertGeometry(feature.getGeometry());
            if (geometry == null || geometry.isEmpty()) {
                continue;
            }
            Map<String, Object> properties = sanitizeProperties(feature.getProperties());
            String name = resolveName(properties);
            Long baseId = SNOWFLAKE.nextId();
            String facilityCode = String.valueOf(baseId);

            YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
            base.setId(baseId);
            base.setFacilityCode(facilityCode);
            base.setFacilityName(StrUtil.blankToDefault(name, "未命名泵站"));
            base.setFacilityType(FACILITY_TYPE_PUMP_STATION);
            base.setAttributes(properties);
            base.setGeom(geometry);
            base.setGeomType(geometry.getGeometryType());
            base.setSrid(geometry.getSRID() > 0 ? geometry.getSRID() : TARGET_SRID);
            base.setSourceType("import");
            baseList.add(base);

            YzPumpStationDO pump = new YzPumpStationDO();
            pump.setId(SNOWFLAKE.nextId());
            pump.setFacilityId(baseId);
            pump.setPumpStationCode(facilityCode);
            pump.setPumpStationName(StrUtil.blankToDefault(name, "未命名泵站"));

            // GeoJSON 中 geometry.coordinates：[longitude, latitude]，写入泵站经纬度字段
            Coordinate coordinate = resolveCoordinate(geometry);
            if (coordinate != null) {
                pump.setLongitude(toCoordinateDecimal(coordinate.getX()));
                pump.setLatitude(toCoordinateDecimal(coordinate.getY()));
            }

            // GeoJSON properties 字段映射
            //pump.setActualInstalledCapacity(toDecimal(properties.get("total_flow")));
            pump.setSelfFlow(toDecimal(properties.get("self_flow")));
            pump.setPumpingFlow(toDecimal(properties.get("pump_export_flow")));
            pump.setInstalledFlow(toDecimal(properties.get("pump_import_flow")));
            pumpList.add(pump);
        }

        if (CollUtil.isEmpty(baseList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, "文件无有效泵站数据");
        }

        baseMapper.insertBatch(baseList);
        pumpStationMapper.insertBatch(pumpList);

        PumpStationImportRespVO respVO = new PumpStationImportRespVO();
        respVO.setFacilityCount(baseList.size());
        respVO.setPumpStationCount(pumpList.size());
        respVO.setMessage("导入成功");
        return respVO;
    }

    private boolean isGeoJson(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private String resolveName(Map<String, Object> properties) {
        Object value = properties.get("name");
        if (value == null) {
            return null;
        }
        return StrUtil.blankToDefault(value.toString(), "").trim();
    }

    private Geometry convertGeometry(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        Geometry cloned = (Geometry) geometry.copy();
        int sourceSrid = cloned.getSRID() > 0 ? cloned.getSRID() : DEFAULT_SOURCE_SRID;
        if (sourceSrid != TARGET_SRID) {
            try {
                MathTransform transform = CRS.findMathTransform(
                        CRS.decode("EPSG:" + sourceSrid, true),
                        CRS.decode("EPSG:" + TARGET_SRID, true),
                        true);
                cloned = JTS.transform(cloned, transform);
            } catch (Exception ex) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_CONVERT_FAIL, ex.getMessage());
            }
        }
        cloned.setSRID(TARGET_SRID);
        return cloned;
    }

    /**
     * 获取坐标点：Point 取自身坐标；非 Point 取质心坐标。
     */
    private Coordinate resolveCoordinate(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Point point = geometry instanceof Point ? (Point) geometry : geometry.getCentroid();
        if (point == null || point.isEmpty()) {
            return null;
        }
        return point.getCoordinate();
    }

    private BigDecimal toCoordinateDecimal(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    private Map<String, Object> sanitizeProperties(Map<String, Object> properties) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (properties == null) {
            return result;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey().toLowerCase(Locale.ROOT);
            if ("geometry".equals(key)) {
                continue;
            }
            Object value = entry.getValue();
            Object safeValue = sanitizeValue(value, 1);
            result.put(key, safeValue);
        }
        return result;
    }

    private Object sanitizeValue(Object value, int depth) {
        if (value == null) {
            return null;
        }
        if (depth > MAX_JSON_NESTING) {
            return DEPTH_TRUNCATED_PLACEHOLDER;
        }
        if (value instanceof Double d) {
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return null;
            }
            return d;
        }
        if (value instanceof Float f) {
            if (Float.isNaN(f) || Float.isInfinite(f)) {
                return null;
            }
            return f;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();
            map.forEach((k, v) -> nested.put(String.valueOf(k), sanitizeValue(v, depth + 1)));
            return nested;
        }
        if (value instanceof List<?> list) {
            List<Object> sanitized = new ArrayList<>(list.size());
            for (Object item : list) {
                sanitized.add(sanitizeValue(item, depth + 1));
            }
            return sanitized;
        }
        if (value instanceof CharSequence || value instanceof Boolean || value instanceof Number) {
            return value;
        }
        return String.valueOf(value);
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            String raw = value.toString().trim();
            if (raw.isEmpty()) {
                return null;
            }
            return new BigDecimal(raw);
        } catch (Exception ex) {
            return null;
        }
    }
}
