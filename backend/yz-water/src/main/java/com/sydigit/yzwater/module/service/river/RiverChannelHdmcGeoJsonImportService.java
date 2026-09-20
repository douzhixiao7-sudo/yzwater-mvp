package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelHdmcGeoJsonImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.service.dto.GeometryFeatureDTO;
import com.sydigit.yzwater.module.service.file.GeometryFeatureFileReader;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 河道 GeoJSON 导入服务（按 properties.hdmc 匹配，存在则更新，不存在则新增）
 */
@Service
@Validated
@Slf4j
public class RiverChannelHdmcGeoJsonImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final String FACILITY_TYPE_RIVER = ReferenceTypeConstants.RIVER;
    private static final String ECOLOGY_TYPE_DEFAULT = "sthd";
    private static final int IS_PROVINCIAL_BACKBONE_DEFAULT = 0;

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final Pattern EPSG_PATTERN = Pattern.compile("EPSG(?::|::)(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern AREA_CODE_PATTERN = Pattern.compile("^(\\d+)(?:\\.0+)?$");

    private final GeometryFeatureFileReader featureFileReader;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final DictDataCommonApi dictDataApi;
    private final SystemAreaMapper systemAreaMapper;

    public RiverChannelHdmcGeoJsonImportService(GeometryFeatureFileReader featureFileReader,
                                                YzWaterFacilityBaseMapper baseMapper,
                                                YzRiverChannelMapper riverChannelMapper,
                                                DictDataCommonApi dictDataApi,
                                                SystemAreaMapper systemAreaMapper) {
        this.featureFileReader = featureFileReader;
        this.baseMapper = baseMapper;
        this.riverChannelMapper = riverChannelMapper;
        this.dictDataApi = dictDataApi;
        this.systemAreaMapper = systemAreaMapper;
    }

    /**
     * 上传 GeoJSON 并按 hdmc 进行更新/新增。
     */
    @Transactional(rollbackFor = Exception.class)
    public RiverChannelHdmcGeoJsonImportRespVO importRiverGeoJsonByHdmc(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isGeoJson(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_TYPE_INVALID);
        }

        List<GeometryFeatureDTO> features;
        Integer sourceSrid;
        byte[] bytes;
        try {
            bytes = file.getBytes();
            sourceSrid = detectGeoJsonSrid(bytes);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, ex.getMessage());
        }

        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            features = featureFileReader.readGeoJson(inputStream, filename, filename);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DATASET_IMPORT_FAIL, ex.getMessage());
        }

        RiverChannelHdmcGeoJsonImportRespVO respVO = new RiverChannelHdmcGeoJsonImportRespVO();
        respVO.setFeatureCount(CollUtil.isEmpty(features) ? 0 : features.size());
        respVO.setSuccessCount(0);
        respVO.setCreateCount(0);
        respVO.setUpdateCount(0);
        respVO.setFailCount(0);

        if (CollUtil.isEmpty(features)) {
            respVO.setMessage("文件中未读取到任何要素");
            return respVO;
        }

        List<DictDataRespDTO> riverTypeDict = safeDictDataList(ZdConstants.ZD_HDLX);
        List<DictDataRespDTO> riverLevelDict = safeDictDataList(ZdConstants.ZD_HLJB);
        Map<String, List<YzRiverChannelDO>> channelsByRiverName = loadExistingChannelsByRiverName(features);
        Set<String> existingAreaIdSet = loadExistingAreaIdSet(collectAreaIds(features));

        int successCount = 0;
        int createCount = 0;
        int updateCount = 0;
        int failCount = 0;

        for (GeometryFeatureDTO feature : features) {
            RiverChannelHdmcGeoJsonImportRespVO.Item item = new RiverChannelHdmcGeoJsonImportRespVO.Item();
            item.setFeatureName(feature == null ? null : feature.getFeatureName());
            item.setAction("skip");

            try {
                if (feature == null) {
                    throw new IllegalArgumentException("要素为空");
                }
                Map<String, Object> properties = feature.getProperties() == null ? new HashMap<>() : feature.getProperties();
                String riverName = resolveHdmc(properties);
                item.setRiverName(riverName);
                if (StrUtil.isBlank(riverName)) {
                    throw new IllegalArgumentException("properties.hdmc 不能为空");
                }

                Geometry geometry = convertGeometry(feature.getGeometry(), sourceSrid);
                if (geometry == null || geometry.isEmpty()) {
                    throw new IllegalArgumentException("geometry 为空");
                }

                String riverTypeText = resolveProperty(properties, "gn");
                String rawRiverLevel = resolveProperty(properties, "hddj");
                String riverLevelValue = resolveRiverLevelValueByRule(riverLevelDict, rawRiverLevel);
                String[] riverTypeValues = resolveRiverTypeValuesByRule(riverTypeDict, riverTypeText);
                String areaId = resolveTownIdByAreaCode(resolveProperty(properties, "areacode"), existingAreaIdSet);
                String[] town = StrUtil.isBlank(areaId) ? null : new String[]{areaId};

                List<YzRiverChannelDO> matched = channelsByRiverName.getOrDefault(riverName, List.of());
                if (CollUtil.isEmpty(matched)) {
                    YzRiverChannelDO created = createRiverChannel(properties, riverName, geometry, riverTypeValues,
                            riverLevelValue, town);
                    channelsByRiverName.computeIfAbsent(riverName, key -> new ArrayList<>()).add(created);
                    createCount++;
                    successCount++;
                    item.setAction("create");
                    item.setSuccess(true);
                    item.setMessage("新增成功");
                } else {
                    for (YzRiverChannelDO channel : matched) {
                        if (channel == null || channel.getId() == null) {
                            continue;
                        }
                        updateRiverChannelGeometryAndFields(channel, riverName, geometry, riverTypeValues,
                                riverLevelValue, town);
                    }
                    updateCount++;
                    successCount++;
                    item.setAction("update");
                    item.setSuccess(true);
                    item.setMessage("更新成功");
                }
            } catch (Exception ex) {
                item.setSuccess(false);
                item.setMessage(StrUtil.blankToDefault(ex.getMessage(), "处理失败"));
                respVO.addItem(item);
                failCount++;
            }
        }

        respVO.setSuccessCount(successCount);
        respVO.setCreateCount(createCount);
        respVO.setUpdateCount(updateCount);
        respVO.setFailCount(failCount);
        respVO.setMessage(failCount > 0 ? "部分要素处理失败，请查看 items" : "导入完成");
        return respVO;
    }

    private YzRiverChannelDO createRiverChannel(Map<String, Object> properties,
                                                String riverName,
                                                Geometry geometry,
                                                String[] riverTypeValues,
                                                String riverLevelValue,
                                                String[] town) {
        String riverCode = resolveNewRiverCode(resolveRiverCode(properties));
        Long facilityId = SNOWFLAKE.nextId();
        baseMapper.insert(buildBaseRecord(facilityId, riverCode, riverName, geometry));

        YzRiverChannelDO channel = new YzRiverChannelDO();
        channel.setId(SNOWFLAKE.nextId());
        channel.setFacilityId(facilityId);
        channel.setRiverCode(riverCode);
        channel.setRiverName(riverName);
        channel.setEcologyType("bfsthd");//fsthd,bfsthd,sthd
        channel.setRiverType(riverTypeValues);
        channel.setRiverLevel(riverLevelValue);
        channel.setTown(town);
        channel.setIsProvincialBackbone(IS_PROVINCIAL_BACKBONE_DEFAULT);
        channel.setRiverSectionCount(0);
        riverChannelMapper.insert(channel);
        return channel;
    }

    private void updateRiverChannelGeometryAndFields(YzRiverChannelDO channel,
                                                     String riverName,
                                                     Geometry geometry,
                                                     String[] riverTypeValues,
                                                     String riverLevelValue,
                                                     String[] town) {
        String riverCode = resolveExistingRiverCode(channel);
        Long facilityId = channel.getFacilityId();

        YzRiverChannelDO updateChannel = new YzRiverChannelDO();
        updateChannel.setId(channel.getId());
        boolean needUpdateChannel = false;

        if (facilityId == null) {
            facilityId = SNOWFLAKE.nextId();
            baseMapper.insert(buildBaseRecord(facilityId, riverCode, riverName, geometry));
            updateChannel.setFacilityId(facilityId);
            needUpdateChannel = true;
            channel.setFacilityId(facilityId);
        } else {
            YzWaterFacilityBaseDO updateBase = buildBaseRecord(facilityId, riverCode, riverName, geometry);
            int updated = baseMapper.updateById(updateBase);
            if (updated <= 0) {
                baseMapper.insert(updateBase);
            }
        }

        if (StrUtil.isBlank(channel.getRiverCode())) {
            updateChannel.setRiverCode(riverCode);
            needUpdateChannel = true;
            channel.setRiverCode(riverCode);
        }
        if (riverTypeValues != null && riverTypeValues.length > 0) {
            updateChannel.setRiverType(riverTypeValues);
            needUpdateChannel = true;
            channel.setRiverType(riverTypeValues);
        }
        if (StrUtil.isNotBlank(riverLevelValue)) {
            updateChannel.setRiverLevel(riverLevelValue);
            needUpdateChannel = true;
            channel.setRiverLevel(riverLevelValue);
        }
        if (town != null && town.length > 0) {
            updateChannel.setTown(town);
            needUpdateChannel = true;
            channel.setTown(town);
        }

        if (needUpdateChannel) {
            riverChannelMapper.updateById(updateChannel);
        }
    }

    private Map<String, List<YzRiverChannelDO>> loadExistingChannelsByRiverName(List<GeometryFeatureDTO> features) {
        Set<String> riverNames = new LinkedHashSet<>();
        if (CollUtil.isNotEmpty(features)) {
            for (GeometryFeatureDTO feature : features) {
                if (feature == null || feature.getProperties() == null) {
                    continue;
                }
                String riverName = resolveHdmc(feature.getProperties());
                if (StrUtil.isNotBlank(riverName)) {
                    riverNames.add(riverName);
                }
            }
        }
        Map<String, List<YzRiverChannelDO>> result = new HashMap<>();
        if (riverNames.isEmpty()) {
            return result;
        }
        List<YzRiverChannelDO> channelList = riverChannelMapper.selectList(
                new LambdaQueryWrapper<YzRiverChannelDO>().in(YzRiverChannelDO::getRiverName, riverNames));
        for (YzRiverChannelDO channel : channelList) {
            if (channel == null || StrUtil.isBlank(channel.getRiverName())) {
                continue;
            }
            result.computeIfAbsent(channel.getRiverName(), key -> new ArrayList<>()).add(channel);
        }
        return result;
    }

    private Set<Long> collectAreaIds(List<GeometryFeatureDTO> features) {
        Set<Long> areaIds = new LinkedHashSet<>();
        if (CollUtil.isEmpty(features)) {
            return areaIds;
        }
        for (GeometryFeatureDTO feature : features) {
            if (feature == null || feature.getProperties() == null) {
                continue;
            }
            Long id = parseAreaCodeToLong(resolveProperty(feature.getProperties(), "areacode"));
            if (id != null) {
                areaIds.add(id);
            }
        }
        return areaIds;
    }

    private Set<String> loadExistingAreaIdSet(Set<Long> areaIds) {
        Set<String> result = new LinkedHashSet<>();
        if (CollUtil.isEmpty(areaIds)) {
            return result;
        }
        List<SystemAreaDO> areaList = systemAreaMapper.selectBatchIds(new ArrayList<>(areaIds));
        for (SystemAreaDO area : areaList) {
            if (area != null && area.getId() != null) {
                result.add(String.valueOf(area.getId()));
            }
        }
        return result;
    }

    private String resolveTownIdByAreaCode(String areaCodeText, Set<String> existingAreaIdSet) {
        Long areaId = parseAreaCodeToLong(areaCodeText);
        if (areaId == null) {
            return null;
        }
        String areaIdText = String.valueOf(areaId);
        return existingAreaIdSet.contains(areaIdText) ? areaIdText : null;
    }

    private Long parseAreaCodeToLong(String areaCodeText) {
        String normalized = StrUtil.trimToNull(areaCodeText);
        if (normalized == null) {
            return null;
        }
        Matcher matcher = AREA_CODE_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private List<DictDataRespDTO> safeDictDataList(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        return list == null ? List.of() : list;
    }

    private String resolveRiverLevelValueByRule(List<DictDataRespDTO> riverLevelDict, String hddj) {
        String normalized = normalizeRiverLevelForDictMatch(hddj);
        return resolveDictValueByFirstTwoChars(riverLevelDict, normalized);
    }

    /**
     * 口径：hddj=乡级时，按“村级”匹配河流级别字典。
     */
    private String normalizeRiverLevelForDictMatch(String hddj) {
        String trimmed = StrUtil.trimToNull(hddj);
        if (trimmed == null) {
            return null;
        }
        if ("乡级".equals(trimmed)) {
            return "村级";
        }
        return trimmed;
    }

    private String[] resolveRiverTypeValuesByRule(List<DictDataRespDTO> riverTypeDict, String gn) {
        if (StrUtil.isBlank(gn) || CollUtil.isEmpty(riverTypeDict)) {
            return null;
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String token : splitGnTokens(gn)) {
            String value = resolveDictValueByFirstTwoChars(riverTypeDict, token);
            if (StrUtil.isNotBlank(value)) {
                values.add(value);
            }
        }
        return values.isEmpty() ? null : values.toArray(new String[0]);
    }

    private List<String> splitGnTokens(String text) {
        String normalized = StrUtil.blankToDefault(text, "")
                .replace("；", ";")
                .replace("，", ";")
                .replace("、", ";")
                .replace("|", ";")
                .replace("/", ";");
        String[] parts = normalized.split("[;]+");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String token = StrUtil.trimToNull(part);
            if (token != null) {
                result.add(token);
            }
        }
        return result;
    }

    /**
     * 规则：按前两个中文匹配字典 label，命中后返回 value。
     */
    private String resolveDictValueByFirstTwoChars(List<DictDataRespDTO> dictDataList, String inputText) {
        if (StrUtil.isBlank(inputText) || CollUtil.isEmpty(dictDataList)) {
            return null;
        }
        String inputPrefix = extractFirstTwoChinese(inputText);
        if (StrUtil.isBlank(inputPrefix)) {
            return null;
        }

        for (DictDataRespDTO item : dictDataList) {
            if (item == null || StrUtil.isBlank(item.getLabel()) || StrUtil.isBlank(item.getValue())) {
                continue;
            }
            String labelPrefix = extractFirstTwoChinese(item.getLabel());
            if (StrUtil.isNotBlank(labelPrefix) && StrUtil.equals(inputPrefix, labelPrefix)) {
                return item.getValue();
            }
        }

        String normalizedInput = normalizeMatchText(inputText);
        for (DictDataRespDTO item : dictDataList) {
            if (item == null || StrUtil.isBlank(item.getLabel()) || StrUtil.isBlank(item.getValue())) {
                continue;
            }
            String normalizedLabel = normalizeMatchText(item.getLabel());
            if (StrUtil.contains(normalizedLabel, normalizedInput)
                    || StrUtil.contains(normalizedInput, normalizedLabel)) {
                return item.getValue();
            }
        }
        return null;
    }

    private String extractFirstTwoChinese(String text) {
        String normalized = normalizeMatchText(text);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(2);
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (isChineseChar(c)) {
                builder.append(c);
                if (builder.length() >= 2) {
                    break;
                }
            }
        }
        if (builder.length() == 0) {
            return normalized.substring(0, Math.min(2, normalized.length()));
        }
        return builder.toString();
    }

    private boolean isChineseChar(char c) {
        Character.UnicodeScript script = Character.UnicodeScript.of(c);
        return script == Character.UnicodeScript.HAN;
    }

    private String normalizeMatchText(String text) {
        return StrUtil.blankToDefault(text, "").replaceAll("\\s+", "");
    }

    private String resolveHdmc(Map<String, Object> properties) {
        return resolveProperty(properties, "hdmc");
    }

    private String resolveRiverCode(Map<String, Object> properties) {
        String value = resolveProperty(properties, "riverCode");
        if (StrUtil.isNotBlank(value)) {
            return value;
        }
        value = resolveProperty(properties, "river_code");
        if (StrUtil.isNotBlank(value)) {
            return value;
        }
        return resolveProperty(properties, "hdbh");
    }

    private String resolveProperty(Map<String, Object> properties, String key) {
        if (properties == null || properties.isEmpty() || StrUtil.isBlank(key)) {
            return null;
        }
        Object value = properties.get(key);
        if (value == null) {
            value = getIgnoreCase(properties, key);
        }
        return StrUtil.trimToNull(value == null ? null : String.valueOf(value));
    }

    private Object getIgnoreCase(Map<String, Object> properties, String key) {
        if (properties == null || StrUtil.isBlank(key)) {
            return null;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String resolveNewRiverCode(String rawCode) {
        String candidate = StrUtil.trimToNull(rawCode);
        if (candidate == null) {
            candidate = String.valueOf(SNOWFLAKE.nextId());
        }
        while (isRiverCodeUsed(candidate)) {
            candidate = String.valueOf(SNOWFLAKE.nextId());
        }
        return candidate;
    }

    private String resolveExistingRiverCode(YzRiverChannelDO channel) {
        if (channel != null && StrUtil.isNotBlank(channel.getRiverCode())) {
            return channel.getRiverCode();
        }
        return resolveNewRiverCode(null);
    }

    private boolean isRiverCodeUsed(String riverCode) {
        if (StrUtil.isBlank(riverCode)) {
            return false;
        }
        return riverChannelMapper.selectCount(YzRiverChannelDO::getRiverCode, riverCode) > 0
                || baseMapper.selectCount(YzWaterFacilityBaseDO::getFacilityCode, riverCode) > 0;
    }

    private YzWaterFacilityBaseDO buildBaseRecord(Long facilityId, String riverCode, String riverName, Geometry geometry) {
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(facilityId);
        base.setFacilityCode(riverCode);
        base.setFacilityType(FACILITY_TYPE_RIVER);
        base.setFacilityName(riverName);
        base.setGeom(geometry);
        base.setGeomType(geometry == null ? null : geometry.getGeometryType());
        base.setSrid(TARGET_SRID);
        base.setSourceType("import");
        return base;
    }

    private boolean isGeoJson(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private Geometry convertGeometry(Geometry geometry, Integer sourceSridFromFile) {
        if (geometry == null) {
            return null;
        }
        Geometry cloned = (Geometry) geometry.copy();
        int sourceSrid = sourceSridFromFile != null && sourceSridFromFile > 0
                ? sourceSridFromFile
                : (cloned.getSRID() > 0 ? cloned.getSRID() : DEFAULT_SOURCE_SRID);
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

    private Integer detectGeoJsonSrid(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            JsonNode root = new ObjectMapper().readTree(bytes);
            String crsName = root == null ? null : root.path("crs").path("properties").path("name").asText(null);
            if (StrUtil.isBlank(crsName)) {
                return null;
            }
            Matcher matcher = EPSG_PATTERN.matcher(crsName);
            if (!matcher.find()) {
                return null;
            }
            return Integer.parseInt(matcher.group(1));
        } catch (Exception ex) {
            return null;
        }
    }
}
