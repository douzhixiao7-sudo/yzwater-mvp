package com.sydigit.yzwater.module.service.geoBase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.service.dto.GeometryFeatureDTO;
import com.sydigit.yzwater.module.service.file.GeometryFeatureFileReader;
import com.sydigit.yzwater.module.service.file.GeometryFileUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 水利对象 GeoJSON 导入服务
 */
@Service
@Validated
@Slf4j
public class WaterFacilityImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    /**
     * 属性 JSON 最大允许嵌套层级，超过后做截断，避免 Jackson 写入 jsonb 时触发深度限制
     */
    private static final int MAX_JSON_NESTING = 20;
    private static final String DEPTH_TRUNCATED_PLACEHOLDER = "[嵌套层级过深已截断]";

    private final GeometryFeatureFileReader featureFileReader;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final DictDataCommonApi dictDataApi;
    private Map<String, String> facilityTypeDict;

    public WaterFacilityImportService(GeometryFeatureFileReader featureFileReader,
                                      YzWaterFacilityBaseMapper baseMapper,
                                      DictDataCommonApi dictDataApi) {
        this.featureFileReader = featureFileReader;
        this.baseMapper = baseMapper;
        this.dictDataApi = dictDataApi;
        this.facilityTypeDict = loadFacilityTypeDict();
    }

    /**
     * 上传 ZIP 并导入水利基础/空间信息
     */
    @Transactional(rollbackFor = Exception.class)
    public WaterFacilityImportRespVO importZip(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_TYPE_INVALID);
        }
        Path tempDir = null;
        WaterFacilityImportRespVO respVO = new WaterFacilityImportRespVO();
        try {
            tempDir = GeometryFileUtils.unzipToTemp(file, "water-geojson-");
            List<Path> geoJsonFiles;
            try (Stream<Path> stream = Files.walk(tempDir)) {
                geoJsonFiles = stream.filter(Files::isRegularFile)
                        .filter(path -> isGeoJson(path.getFileName().toString()))
                        .collect(Collectors.toList());
            }
            for (Path jsonFile : geoJsonFiles) {
                if (isEmptyGeoJsonFile(jsonFile)) {
                    log.info("GeoJSON 文件为空或仅包含空对象，已跳过 {}", jsonFile.getFileName());
                    continue;
                }
                respVO.addItem(handleSingleFile(jsonFile));
            }
            respVO.setFileCount(respVO.getItems().size());
            respVO.setFacilityCount(respVO.getItems().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getSuccess()))
                    .mapToLong(item -> item.getSuccessCount() == null ? 0 : item.getSuccessCount())
                    .sum());
            // 几何数据已存入基础表，计数与设施数量一致
            respVO.setGeometryCount(respVO.getFacilityCount());
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, ex.getMessage());
        } finally {
            GeometryFileUtils.deleteQuietly(tempDir);
        }
        return respVO;
    }

    private WaterFacilityImportRespVO.Item handleSingleFile(Path jsonFile) {
        WaterFacilityImportRespVO.Item item = new WaterFacilityImportRespVO.Item();
        String fileName = jsonFile.getFileName().toString();
        item.setFileName(fileName);
        Set<String> usedFacilityCodes = new HashSet<>();
        try (InputStream inputStream = Files.newInputStream(jsonFile)) {
            List<GeometryFeatureDTO> features = featureFileReader.readGeoJson(inputStream, fileName, fileName);
            if (CollUtil.isEmpty(features)) {
                item.setSuccess(true);
                item.setSuccessCount(0);
                item.setMessage("未解析到空间要素，已跳过");
                return item;
            }

            List<YzWaterFacilityBaseDO> baseList = new ArrayList<>();
            for (GeometryFeatureDTO feature : features) {
                Geometry geometry = convertGeometry(feature.getGeometry());
                if (geometry == null || geometry.isEmpty()) {
                    continue;
                }
                Map<String, Object> properties = sanitizeProperties(feature.getProperties());
                String facilityType = resolveFacilityType(properties, fileName);
                String facilityName = resolveFacilityName(properties, fileName);
                String adminRegion = resolveAdminRegion(properties);
                String adminRegionCode = resolveAdminRegionCode(properties);
                String manageUnit = resolveManageUnit(properties);
                Long baseId = SNOWFLAKE.nextId();
                String facilityCode = ensureUniqueFacilityCode(resolveFacilityCode(properties, baseId), usedFacilityCodes);

                YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
                base.setId(baseId);
                base.setFacilityCode(facilityCode);
                base.setFacilityName(facilityName);
                base.setFacilityType(facilityType);
                base.setAdminRegion(adminRegion);
                base.setAdminRegionCode(adminRegionCode);
                base.setManageUnit(manageUnit);
                base.setAttributes(properties);
                base.setGeom(geometry);
                base.setGeomType(geometry.getGeometryType());
                base.setSrid(geometry.getSRID() > 0 ? geometry.getSRID() : TARGET_SRID);
                base.setSourceType("import");
                baseList.add(base);
            }

            if (CollUtil.isEmpty(baseList)) {
                item.setSuccess(true);
                item.setSuccessCount(0);
                item.setMessage("文件无有效几何数据，已跳过");
                return item;
            }
            baseMapper.insertBatch(baseList);
            item.setSuccess(true);
            item.setSuccessCount(baseList.size());
        } catch (Exception ex) {
            log.warn("导入文件 {} 失败", fileName, ex);
            item.setSuccess(false);
            item.setMessage(StrUtil.blankToDefault(ex.getMessage(), "解析失败"));
        }
        return item;
    }

    private boolean isGeoJson(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private boolean isEmptyGeoJsonFile(Path jsonFile) {
        try {
            long size = Files.size(jsonFile);
            if (size == 0) {
                return true;
            }
            if (size <= 1024) {
                String content = Files.readString(jsonFile, StandardCharsets.UTF_8);
                String trimmed = content.trim();
                return trimmed.isEmpty() || "{}".equals(trimmed);
            }
        } catch (IOException ex) {
            log.warn("读取 GeoJSON 文件大小失败，按非空处理: {}", jsonFile.getFileName(), ex);
        }
        return false;
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
            result.put(key, convertSthdIfNeeded(key, safeValue));
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

    private String resolveFacilityType(Map<String, Object> properties, String fileName) {
        String fileTypeKey = mapFacilityTypeKeyByFileName(fileName);
        if (StrUtil.isNotBlank(fileTypeKey)) {
            return mapToFacilityTypeValue(fileTypeKey);
        }
        String typeKey = resolveFacilityTypeKey(properties, fileName);
        return mapToFacilityTypeValue(typeKey);
    }

    private String resolveFacilityTypeKey(Map<String, Object> properties, String fileName) {
        List<String> candidates = List.of("type", "category");
        for (String key : candidates) {
            Object value = properties.get(key);
            if (value != null && StrUtil.isNotBlank(value.toString()) && !isHtmlValue(value.toString())) {
                return value.toString().trim();
            }
        }
        String baseName = fileName.replaceAll("\\.[^.]+$", "");
        String lower = baseName.toLowerCase(Locale.ROOT);
        for (String dictKey : facilityTypeDict.keySet()) {
            if (lower.contains(dictKey.toLowerCase(Locale.ROOT))) {
                return dictKey;
            }
        }
        return lower;
    }

    private String mapToFacilityTypeValue(String rawKey) {
        if (StrUtil.isBlank(rawKey) || facilityTypeDict.isEmpty()) {
            return StrUtil.blankToDefault(rawKey, null);
        }
        String normalized = normalizeDictKey(rawKey);
        String matched = facilityTypeDict.get(normalized);
        if (matched != null) {
            return matched;
        }
        for (Map.Entry<String, String> entry : facilityTypeDict.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return rawKey.trim();
    }

    private String mapFacilityTypeKeyByFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return null;
        }
        String baseName = fileName.replaceAll("\\.[^.]+$", "");
        String lower = baseName.toLowerCase(Locale.ROOT);
        if (lower.contains("water-level-station")) {
            return "sluice";
        }
        if (lower.contains("water-station")) {
            return "gate_station";
        }
        if (lower.contains("rivers") || lower.contains("river")) {
            return "river";
        }
        if (lower.contains("water") && !lower.contains("station")) {
            return "river";
        }
        if (lower.contains("border")) {
            return "mgmt_boundary";
        }
        if (lower.contains("irrigation")) {
            return "irrigation";
        }
        if (lower.contains("lake")) {
            return "lake";
        }
        if (lower.contains("rain-station")) {
            return "rain_station";
        }
        if (lower.contains("reservoir")) {
            return "reservoir";
        }
        if (lower.contains("video-points")) {
            return "camera";
        }
        return null;
    }

    private String resolveFacilityName(Map<String, Object> properties, String fileName) {
        List<String> nameKeys = List.of("name", "hdmc", "hhmc", "skmc", "mc", "o_name", "reservoirname");
        for (String key : nameKeys) {
            Object value = properties.get(key);
            if (value != null && StrUtil.isNotBlank(value.toString()) && !isHtmlValue(value.toString())) {
                return value.toString().trim();
            }
        }
        String baseName = fileName.replaceAll("\\.[^.]+$", "");
        return StrUtil.blankToDefault(baseName, "未命名设施");
    }

    private String resolveFacilityCode(Map<String, Object> properties, Long generatedId) {
        return String.valueOf(generatedId);
    }

    private String ensureUniqueFacilityCode(String rawCode, Set<String> usedFacilityCodes) {
        String candidate = StrUtil.blankToDefault(rawCode, String.valueOf(SNOWFLAKE.nextId()));
        int suffix = 1;
        while (usedFacilityCodes.contains(candidate) || facilityCodeExists(candidate)) {
            candidate = candidate + "-" + suffix;
            suffix++;
        }
        usedFacilityCodes.add(candidate);
        return candidate;
    }

    private boolean facilityCodeExists(String facilityCode) {
        return baseMapper.selectCount(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .eq(YzWaterFacilityBaseDO::getFacilityCode, facilityCode)) > 0;
    }

    private Map<String, String> loadFacilityTypeDict() {
        Map<String, String> dict = new LinkedHashMap<>();
        List<DictDataRespDTO> dictDataList = dictDataApi.getDictDataList(ZdConstants.DICT_TYPE_FACILITY_TYPE);
        for (DictDataRespDTO dictData : dictDataList) {
            String value = StrUtil.blankToDefault(dictData.getValue(), "").trim();
            if (StrUtil.isBlank(value)) {
                continue;
            }
            String label = StrUtil.blankToDefault(dictData.getLabel(), "").trim();
            dict.put(normalizeDictKey(value), value);
            if (StrUtil.isNotBlank(label)) {
                dict.put(normalizeDictKey(label), value);
            }
        }
        return dict;
    }

    private String resolveAdminRegion(Map<String, Object> properties) {
        return findFirstText(properties, List.of("areaname"));
    }

    private String resolveAdminRegionCode(Map<String, Object> properties) {
        return findFirstText(properties, List.of("areacode", "adcode"));
    }

    private String resolveManageUnit(Map<String, Object> properties) {
        return findFirstText(properties, List.of("gldw", "gljg"));
    }

    private String findFirstText(Map<String, Object> properties, List<String> keys) {
        for (String key : keys) {
            Object value = properties.get(key);
            if (value != null && StrUtil.isNotBlank(value.toString())) {
                return value.toString().trim();
            }
        }
        return null;
    }

    private boolean isHtmlValue(String value) {
        if (StrUtil.isBlank(value)) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.contains("<") && trimmed.contains(">");
    }

    private String normalizeDictKey(String text) {
        return StrUtil.blankToDefault(text, "").trim().toLowerCase(Locale.ROOT);
    }

    /**
     * st河道标记转换：部分/全部/不是生态河道 -> bfsthd/sthd/fsthd
     */
    private Object convertSthdIfNeeded(String key, Object value) {
        if (!"sthd".equals(key) || value == null) {
            return value;
        }
        String text = value.toString().trim();
        return switch (text) {
            case "部分是生态河道" -> "bfsthd";
            case "全部是生态河道" -> "sthd";
            case "不是生态河道" -> "fsthd";
            default -> text;
        };
    }
}
