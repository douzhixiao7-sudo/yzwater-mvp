package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelGeoJsonImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 河道 GeoJSON 导入服务（按 doc/xuqiu.md 的字段映射入库）
 */
@Service
@Validated
@Slf4j
public class RiverChannelGeoJsonImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final int MAX_JSON_NESTING = 20;
    private static final String DEPTH_TRUNCATED_PLACEHOLDER = "[嵌套层级过深已截断]";

    private static final String FACILITY_TYPE_RIVER = ReferenceTypeConstants.RIVER;
    /**
     * 按需求：河道生态类型固定写入 fsthd（不是生态河道）
     */
    private static final String ECOLOGY_TYPE_FIXED = "fsthd";

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final GeometryFeatureFileReader featureFileReader;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverChannelManagementMapper managementMapper;
    private final DictDataCommonApi dictDataApi;

    public RiverChannelGeoJsonImportService(GeometryFeatureFileReader featureFileReader,
                                           YzWaterFacilityBaseMapper baseMapper,
                                           YzRiverChannelMapper riverChannelMapper,
                                           YzRiverChannelManagementMapper managementMapper,
                                           DictDataCommonApi dictDataApi) {
        this.featureFileReader = featureFileReader;
        this.baseMapper = baseMapper;
        this.riverChannelMapper = riverChannelMapper;
        this.managementMapper = managementMapper;
        this.dictDataApi = dictDataApi;
    }

    /**
     * 上传 GeoJSON 并批量写入基础表、河道表、河长表
     */
    @Transactional(rollbackFor = Exception.class)
    public RiverChannelGeoJsonImportRespVO importRiverChannelGeoJson(MultipartFile file) {
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

        RiverChannelGeoJsonImportRespVO respVO = new RiverChannelGeoJsonImportRespVO();
        respVO.setFeatureCount(CollUtil.isEmpty(features) ? 0 : features.size());

        if (CollUtil.isEmpty(features)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, "文件未包含有效要素");
        }

        List<DictDataRespDTO> riverTypeDict = dictDataApi.getDictDataList(ZdConstants.ZD_HDLX);
        List<DictDataRespDTO> riverLevelDict = dictDataApi.getDictDataList(ZdConstants.ZD_HLJB);
        List<DictDataRespDTO> headLevelDict = dictDataApi.getDictDataList(ZdConstants.ZD_HZJB);

        List<YzWaterFacilityBaseDO> baseList = new ArrayList<>();
        List<YzRiverChannelDO> channelList = new ArrayList<>();
        List<YzRiverChannelManagementDO> managementList = new ArrayList<>();

        int successCount = 0;
        LocalDateTime now = LocalDateTime.now();
        for (GeometryFeatureDTO feature : features) {
            RiverChannelGeoJsonImportRespVO.Item item = new RiverChannelGeoJsonImportRespVO.Item();
            try {
                if (feature == null || feature.getGeometry() == null) {
                    throw new IllegalArgumentException("要素缺少 geometry");
                }
                Geometry geometry = convertGeometry(feature.getGeometry());
                if (geometry == null || geometry.isEmpty()) {
                    throw new IllegalArgumentException("要素 geometry 为空");
                }

                Map<String, Object> properties = feature.getProperties() == null ? new LinkedHashMap<>() : feature.getProperties();
                String riverCode = asTrimmedText(properties.get("hdbh"));
                String riverName = asTrimmedText(properties.get("hdmc"));
                String headName = asTrimmedText(properties.get("hzr"));
                String headContact = asTrimmedText(properties.get("dh"));
                String adminRegionCode = asTrimmedText(properties.get("areacode"));
                String adminRegion = asTrimmedText(properties.get("areaname"));
                String managementUnit = asTrimmedText(properties.get("gljg"));
                String riverTypeText = asTrimmedText(properties.get("gn"));
                String rawLevelText = asTrimmedText(properties.get("hddj"));
                String riverLevelText = normalizeRiverLevelForDictMatch(rawLevelText);
                String headLevelText = normalizeHeadLevelForDictMatch(rawLevelText);

                item.setRiverCode(riverCode);
                item.setRiverName(riverName);

                if (StrUtil.isBlank(riverCode)) {
                    throw new IllegalArgumentException("缺少必填字段 hdbh（河道编号）");
                }
                if (StrUtil.isBlank(riverName)) {
                    throw new IllegalArgumentException("缺少必填字段 hdmc（河道名称）");
                }
                if (riverChannelMapper.selectCount(YzRiverChannelDO::getRiverCode, riverCode) > 0) {
                    throw new IllegalArgumentException("河道编号已存在，已跳过导入");
                }
                if (baseMapper.selectCount(YzWaterFacilityBaseDO::getFacilityCode, riverCode) > 0) {
                    throw new IllegalArgumentException("基础设施编码已存在，已跳过导入");
                }

                String riverLevelValue = resolveDictValueByLabelContains(riverLevelDict, riverLevelText);
                String headLevelValue = resolveDictValueByLabelContains(headLevelDict, headLevelText);
                String[] riverTypes = resolveMultiDictValuesByLabelContains(riverTypeDict, riverTypeText);
                if (StrUtil.isNotBlank(rawLevelText) && StrUtil.isBlank(riverLevelValue)) {
                    throw new IllegalArgumentException("河流级别(hddj)无法匹配字典 " + ZdConstants.ZD_HLJB);
                }
                if (StrUtil.isNotBlank(rawLevelText) && StrUtil.isBlank(headLevelValue)) {
                    throw new IllegalArgumentException("河长级别(hddj)无法匹配字典 " + ZdConstants.ZD_HZJB);
                }
                if (StrUtil.isNotBlank(riverTypeText) && (riverTypes == null || riverTypes.length == 0)) {
                    throw new IllegalArgumentException("河道类型(gn)无法匹配字典 " + ZdConstants.ZD_HDLX);
                }

                Long baseId = SNOWFLAKE.nextId();
                YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
                base.setId(baseId);
                base.setFacilityCode(riverCode);
                base.setFacilityName(riverName);
                base.setFacilityType(FACILITY_TYPE_RIVER);
                base.setAdminRegion(adminRegion);
                base.setAdminRegionCode(adminRegionCode);
                base.setManageUnit(managementUnit);
                base.setAttributes(sanitizeProperties(properties));
                base.setGeom(geometry);
                base.setGeomType(geometry.getGeometryType());
                base.setSrid(geometry.getSRID() > 0 ? geometry.getSRID() : TARGET_SRID);
                base.setSourceType("import");
                baseList.add(base);

                Long channelId = SNOWFLAKE.nextId();
                YzRiverChannelDO channel = new YzRiverChannelDO();
                channel.setId(channelId);
                channel.setFacilityId(baseId);
                channel.setRiverCode(riverCode);
                channel.setRiverName(riverName);
                channel.setEcologyType(ECOLOGY_TYPE_FIXED);
                channel.setRiverType(riverTypes);
                channel.setRiverLevel(riverLevelValue);
                channel.setManagementUnit(managementUnit);
                channel.setRiverSectionCount(0);
                channelList.add(channel);

                Long managementId = SNOWFLAKE.nextId();
                YzRiverChannelManagementDO management = new YzRiverChannelManagementDO();
                management.setId(managementId);
                management.setRiverChannelId(channelId);
                management.setRiverSectionId(null);
                management.setReferenceId(channelId);
                management.setReferenceType(ReferenceTypeConstants.RIVER);
                management.setSectionName(riverName);
                management.setHeadLevel(headLevelValue);
                management.setHeadName(headName);
                management.setHeadContact(headContact);
                management.setVersionNo(1);
                management.setEffectiveFrom(now);
                management.setEffectiveTo(null);
                management.setIsCurrent(1);
                managementList.add(management);

                successCount++;
            } catch (Exception ex) {
                item.setSuccess(false);
                item.setMessage(StrUtil.blankToDefault(ex.getMessage(), "导入失败"));
                respVO.addItem(item);
                continue;
            }
        }

        if (successCount <= 0) {
            respVO.setSuccessCount(0);
            respVO.setFailCount(respVO.getFeatureCount());
            respVO.setFacilityCount(0);
            respVO.setRiverChannelCount(0);
            respVO.setManagementCount(0);
            respVO.setMessage("未导入任何数据，请检查文件内容与字段映射");
            return respVO;
        }

        baseMapper.insertBatch(baseList);
        riverChannelMapper.insertBatch(channelList);
        managementMapper.insertBatch(managementList);

        respVO.setSuccessCount(successCount);
        respVO.setFailCount(respVO.getFeatureCount() - successCount);
        respVO.setFacilityCount(baseList.size());
        respVO.setRiverChannelCount(channelList.size());
        respVO.setManagementCount(managementList.size());
        respVO.setMessage("导入完成");
        return respVO;
    }

    private boolean isGeoJson(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
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

    private String asTrimmedText(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        if (text == null) {
            return null;
        }
        return StrUtil.trimToNull(text);
    }

    /**
     * 特殊口径：hddj=乡级 时，河流级别按“乡村级”进行字典匹配
     */
    private String normalizeRiverLevelForDictMatch(String hddj) {
        if (StrUtil.isBlank(hddj)) {
            return hddj;
        }
        String trimmed = hddj.trim();
        if ("乡级".equals(trimmed)) {
            return "乡村级";
        }
        return trimmed;
    }

    /**
     * 特殊口径：hddj=乡级 时，河长级别按“乡镇级”进行字典匹配
     */
    private String normalizeHeadLevelForDictMatch(String hddj) {
        if (StrUtil.isBlank(hddj)) {
            return hddj;
        }
        String trimmed = hddj.trim();
        if ("乡级".equals(trimmed)) {
            return "乡镇级";
        }
        return trimmed;
    }

    /**
     * 按需求规则：字典项 label 包含输入值，即视为匹配，返回其 value
     */
    private String resolveDictValueByLabelContains(List<DictDataRespDTO> dictDataList, String input) {
        if (StrUtil.isBlank(input) || CollUtil.isEmpty(dictDataList)) {
            return null;
        }
        String needle = input.trim();
        List<DictDataRespDTO> matched = dictDataList.stream()
                .filter(item -> item != null && StrUtil.isNotBlank(item.getValue()))
                .filter(item -> StrUtil.isNotBlank(item.getLabel()) && item.getLabel().contains(needle))
                .collect(Collectors.toList());
        if (matched.isEmpty()) {
            return null;
        }
        // 选择 label 最短的匹配项，避免“更长的上级描述”抢占
        matched.sort((a, b) -> {
            int la = a.getLabel() == null ? Integer.MAX_VALUE : a.getLabel().length();
            int lb = b.getLabel() == null ? Integer.MAX_VALUE : b.getLabel().length();
            if (la != lb) {
                return Integer.compare(la, lb);
            }
            return String.valueOf(a.getValue()).compareTo(String.valueOf(b.getValue()));
        });
        return matched.get(0).getValue();
    }

    private String[] resolveMultiDictValuesByLabelContains(List<DictDataRespDTO> dictDataList, String input) {
        if (StrUtil.isBlank(input) || CollUtil.isEmpty(dictDataList)) {
            return null;
        }
        List<String> tokens = splitTokens(input);
        if (tokens.isEmpty()) {
            return null;
        }
        List<String> values = new ArrayList<>();
        for (String token : tokens) {
            String value = resolveDictValueByLabelContains(dictDataList, token);
            if (StrUtil.isBlank(value)) {
                continue;
            }
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        if (values.isEmpty()) {
            return null;
        }
        return values.toArray(new String[0]);
    }

    private List<String> splitTokens(String text) {
        if (StrUtil.isBlank(text)) {
            return List.of();
        }
        String normalized = text.replace("，", ",")
                .replace("；", ";")
                .replace("、", ",")
                .replace("|", ",")
                .replace("/", ",")
                .replace(" ", "");
        String[] parts = normalized.split("[,;]+");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String token = StrUtil.trimToNull(part);
            if (token != null) {
                result.add(token);
            }
        }
        return result;
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
