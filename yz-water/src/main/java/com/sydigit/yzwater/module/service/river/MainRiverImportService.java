package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.river.MainRiverImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.service.dto.GeometryFeatureDTO;
import com.sydigit.yzwater.module.service.file.GeometryFeatureFileReader;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 省级河道 GeoJSON 导入服务
 */
@Service
@Validated
@Slf4j
public class MainRiverImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final int MAX_JSON_NESTING = 20;
    private static final String DEPTH_TRUNCATED_PLACEHOLDER = "[嵌套层级过深已截断]";
    /** 省级主要河道默认等级 */
    private static final String RIVER_LEVEL_PROVINCE = "shjzy";
    private static final String FACILITY_TYPE_RIVER = "river";
    private static final String FACILITY_TYPE_RIVER_SECTION = "river_section";
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final GeometryFeatureFileReader featureFileReader;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;

    public MainRiverImportService(GeometryFeatureFileReader featureFileReader,
                                  YzWaterFacilityBaseMapper baseMapper,
                                  YzRiverChannelMapper riverChannelMapper,
                                  YzRiverSectionMapper riverSectionMapper) {
        this.featureFileReader = featureFileReader;
        this.baseMapper = baseMapper;
        this.riverChannelMapper = riverChannelMapper;
        this.riverSectionMapper = riverSectionMapper;
    }

    /**
     * 上传省级河道 GeoJSON 并批量写入基础表、河道表、河段表
     */
    @Transactional(rollbackFor = Exception.class)
    public MainRiverImportRespVO importMainRiverGeoJson(MultipartFile file) {
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
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, "文件未包含有效要素");
        }

        Map<String, List<GeometryFeatureDTO>> grouped = groupByName(features);
        List<YzWaterFacilityBaseDO> baseList = new ArrayList<>();
        List<YzRiverChannelDO> riverChannelList = new ArrayList<>();
        List<YzRiverSectionDO> riverSectionList = new ArrayList<>();

        for (Map.Entry<String, List<GeometryFeatureDTO>> entry : grouped.entrySet()) {
            String riverName = entry.getKey();
            if (StrUtil.isBlank(riverName)) {
                continue;
            }
            List<Geometry> geometries = new ArrayList<>();
            Map<String, Object> attributes = null;
            for (GeometryFeatureDTO feature : entry.getValue()) {
                Geometry geometry = convertGeometry(feature.getGeometry());
                if (geometry == null || geometry.isEmpty()) {
                    continue;
                }
                geometries.add(geometry);
                if (attributes == null) {
                    attributes = sanitizeProperties(feature.getProperties());
                }
            }
            Geometry mergedGeometry = mergeGeometries(geometries);
            if (mergedGeometry == null || mergedGeometry.isEmpty()) {
                continue;
            }
            Long channelBaseId = SNOWFLAKE.nextId();
            String channelFacilityCode = String.valueOf(channelBaseId);

            YzWaterFacilityBaseDO channelBase = new YzWaterFacilityBaseDO();
            channelBase.setId(channelBaseId);
            channelBase.setFacilityCode(channelFacilityCode);
            channelBase.setFacilityName(riverName);
            channelBase.setFacilityType(FACILITY_TYPE_RIVER);
            channelBase.setAttributes(attributes == null ? new LinkedHashMap<>() : attributes);
            channelBase.setGeom(mergedGeometry);
            channelBase.setGeomType(mergedGeometry.getGeometryType());
            channelBase.setSrid(mergedGeometry.getSRID() > 0 ? mergedGeometry.getSRID() : TARGET_SRID);
            channelBase.setSourceType("import");
            baseList.add(channelBase);

            Long channelId = SNOWFLAKE.nextId();
            int sectionCount = entry.getValue().size() == 1 ? 0 : entry.getValue().size();

            YzRiverChannelDO channel = new YzRiverChannelDO();
            channel.setId(channelId);
            channel.setFacilityId(channelBaseId);
            channel.setRiverCode(String.valueOf(channelId));
            channel.setRiverName(riverName);
            channel.setRiverLevel(RIVER_LEVEL_PROVINCE);
            channel.setRiverSectionCount(sectionCount);
            riverChannelList.add(channel);

            // 仅当同名多条要素时才创建河段与河段基础表
            if (sectionCount > 0) {
                for (GeometryFeatureDTO feature : entry.getValue()) {
                    Geometry sectionGeometry = convertGeometry(feature.getGeometry());
                    if (sectionGeometry == null || sectionGeometry.isEmpty()) {
                        continue;
                    }
                    Long sectionBaseId = SNOWFLAKE.nextId();
                    String sectionFacilityCode = String.valueOf(sectionBaseId);

                    YzWaterFacilityBaseDO sectionBase = new YzWaterFacilityBaseDO();
                    sectionBase.setId(sectionBaseId);
                    sectionBase.setFacilityCode(sectionFacilityCode);
                    sectionBase.setFacilityName(riverName);
                    sectionBase.setFacilityType(FACILITY_TYPE_RIVER_SECTION);
                    sectionBase.setAttributes(sanitizeProperties(feature.getProperties()));
                    sectionBase.setGeom(sectionGeometry);
                    sectionBase.setGeomType(sectionGeometry.getGeometryType());
                    sectionBase.setSrid(sectionGeometry.getSRID() > 0 ? sectionGeometry.getSRID() : TARGET_SRID);
                    sectionBase.setSourceType("import");
                    baseList.add(sectionBase);

                    YzRiverSectionDO section = new YzRiverSectionDO();
                    section.setId(SNOWFLAKE.nextId());
                    section.setFacilityId(sectionBaseId);
                    section.setRiverChannelId(channelId);
                    section.setSectionName(riverName);
                    riverSectionList.add(section);
                }
            }
        }

        if (CollUtil.isEmpty(baseList)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, "文件无有效河道数据");
        }

        baseMapper.insertBatch(baseList);
        riverChannelMapper.insertBatch(riverChannelList);
        riverSectionMapper.insertBatch(riverSectionList);

        MainRiverImportRespVO respVO = new MainRiverImportRespVO();
        respVO.setFacilityCount(baseList.size());
        respVO.setRiverChannelCount(riverChannelList.size());
        respVO.setRiverSectionCount(riverSectionList.size());
        respVO.setMessage("导入成功");
        return respVO;
    }

    private boolean isGeoJson(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private Map<String, List<GeometryFeatureDTO>> groupByName(List<GeometryFeatureDTO> features) {
        return features.stream()
                .filter(item -> item != null && item.getGeometry() != null)
                .collect(Collectors.groupingBy(this::resolveName, LinkedHashMap::new, Collectors.toList()));
    }

    private String resolveName(GeometryFeatureDTO feature) {
        Map<String, Object> properties = feature.getProperties();
        if (properties == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if ("name".equalsIgnoreCase(StrUtil.blankToDefault(entry.getKey(), ""))) {
                String name = entry.getValue() == null ? "" : entry.getValue().toString();
                return StrUtil.blankToDefault(name, "").trim();
            }
        }
        return null;
    }

    private Geometry mergeGeometries(List<Geometry> geometries) {
        if (CollUtil.isEmpty(geometries)) {
            return null;
        }
        Geometry merged = null;
        for (Geometry geometry : geometries) {
            if (merged == null) {
                merged = geometry;
            } else {
                merged = merged.union(geometry);
            }
        }
        if (merged != null) {
            merged.setSRID(TARGET_SRID);
        }
        return merged;
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

    private Map<String, Object> sanitizeProperties(Map<String, Object> properties) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (properties == null) {
            return result;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = StrUtil.blankToDefault(entry.getKey(), "").trim();
            Object safeValue = sanitizeValue(entry.getValue(), 1);
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
}
