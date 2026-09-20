package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelONameGeoJsonImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 河道 GeoJSON 导入服务（按 properties.O_Name 映射入库）。
 * <p>
 * 规则：
 * <ul>
 *   <li>facilityType 固定写入 river</li>
 *   <li>facilityName / riverName 读取 properties.O_Name</li>
 *   <li>若数据库已存在同名河道（riverName 相等），仅更新对应基础表的 geom</li>
 * </ul>
 */
@Service
@Validated
@Slf4j
public class RiverChannelONameGeoJsonImportService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final String FACILITY_TYPE_RIVER = "river";

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final Pattern EPSG_PATTERN = Pattern.compile("EPSG(?::|::)(\\d+)", Pattern.CASE_INSENSITIVE);

    private final GeometryFeatureFileReader featureFileReader;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzRiverChannelMapper riverChannelMapper;

    public RiverChannelONameGeoJsonImportService(GeometryFeatureFileReader featureFileReader,
                                                YzWaterFacilityBaseMapper baseMapper,
                                                YzRiverChannelMapper riverChannelMapper) {
        this.featureFileReader = featureFileReader;
        this.baseMapper = baseMapper;
        this.riverChannelMapper = riverChannelMapper;
    }

    /**
     * 上传 GeoJSON 并写入基础表、河道表；若同名河道已存在则更新基础表几何。
     */
    @Transactional(rollbackFor = Exception.class)
    public RiverChannelONameGeoJsonImportRespVO importRiverGeoJsonByOName(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isGeoJson(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_TYPE_INVALID);
        }

        List<GeometryFeatureDTO> features;
        Integer sourceSrid = null;
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

        RiverChannelONameGeoJsonImportRespVO respVO = new RiverChannelONameGeoJsonImportRespVO();
        respVO.setFeatureCount(CollUtil.isEmpty(features) ? 0 : features.size());
        respVO.setSuccessCount(0);
        respVO.setCreateCount(0);
        respVO.setUpdateCount(0);
        respVO.setFailCount(0);

        if (CollUtil.isEmpty(features)) {
            respVO.setMessage("文件中未读取到任何要素");
            return respVO;
        }

        int successCount = 0;
        int createCount = 0;
        int updateCount = 0;
        int failCount = 0;

        for (GeometryFeatureDTO feature : features) {
            RiverChannelONameGeoJsonImportRespVO.Item item = new RiverChannelONameGeoJsonImportRespVO.Item();
            item.setFeatureName(feature == null ? null : feature.getFeatureName());
            item.setAction("skip");

            try {
                if (feature == null) {
                    throw new IllegalArgumentException("要素为空");
                }
                String riverName = resolveOName(feature.getProperties());
                item.setRiverName(riverName);
                if (StrUtil.isBlank(riverName)) {
                    throw new IllegalArgumentException("properties.O_Name 不能为空");
                }
                String riverCodeFromFile = resolveRiverCode(feature.getProperties());
                Geometry geometry = convertGeometry(feature.getGeometry(), sourceSrid);
                if (geometry == null) {
                    throw new IllegalArgumentException("几何为空");
                }

                List<YzRiverChannelDO> existing = riverChannelMapper.selectList(
                        new LambdaQueryWrapper<YzRiverChannelDO>().eq(YzRiverChannelDO::getRiverName, riverName));
                if (CollUtil.isEmpty(existing)) {
                    String riverCode = StrUtil.blankToDefault(riverCodeFromFile, String.valueOf(SNOWFLAKE.nextId()));
                    Long baseId = SNOWFLAKE.nextId();
                    YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
                    base.setId(baseId);
                    base.setFacilityCode(riverCode);
                    base.setFacilityType(FACILITY_TYPE_RIVER);
                    base.setFacilityName(riverName);
                    base.setGeom(geometry);
                    base.setGeomType(geometry.getGeometryType());
                    base.setSrid(TARGET_SRID);
                    base.setSourceType("import");
                    baseMapper.insert(base);

                    Long channelId = SNOWFLAKE.nextId();
                    YzRiverChannelDO channel = new YzRiverChannelDO();
                    channel.setId(channelId);
                    channel.setFacilityId(baseId);
                    channel.setRiverCode(riverCode);
                    channel.setRiverName(riverName);
                    channel.setRiverSectionCount(0);
                    riverChannelMapper.insert(channel);

                    item.setAction("create");
                    createCount++;
                } else if (existing.size() == 1) {
                    YzRiverChannelDO channel = existing.get(0);
                    String riverCode = resolveExistingRiverCode(channel, riverCodeFromFile);
                    if (channel.getFacilityId() == null) {
                        // 兜底：历史数据缺 facilityId 时补齐基础表记录
                        Long baseId = SNOWFLAKE.nextId();
                        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
                        base.setId(baseId);
                        base.setFacilityCode(riverCode);
                        base.setFacilityType(FACILITY_TYPE_RIVER);
                        base.setFacilityName(riverName);
                        base.setGeom(geometry);
                        base.setGeomType(geometry.getGeometryType());
                        base.setSrid(TARGET_SRID);
                        base.setSourceType("import");
                        baseMapper.insert(base);

                        YzRiverChannelDO update = new YzRiverChannelDO();
                        update.setId(channel.getId());
                        update.setFacilityId(baseId);
                        update.setRiverCode(riverCode);
                        riverChannelMapper.updateById(update);
                    } else {
                        // 同名河道存在时：更新基础表几何，并确保基础表编码与河道编码一致
                        YzWaterFacilityBaseDO update = new YzWaterFacilityBaseDO();
                        update.setId(channel.getFacilityId());
                        update.setFacilityCode(riverCode);
                        update.setFacilityType(FACILITY_TYPE_RIVER);
                        update.setFacilityName(riverName);
                        update.setGeom(geometry);
                        update.setGeomType(geometry.getGeometryType());
                        update.setSrid(TARGET_SRID);
                        int updated = baseMapper.updateById(update);
                        if (updated <= 0) {
                            // 兜底：基础表记录缺失时重建并回填 facilityId
                            Long baseId = SNOWFLAKE.nextId();
                            YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
                            base.setId(baseId);
                            base.setFacilityCode(riverCode);
                            base.setFacilityType(FACILITY_TYPE_RIVER);
                            base.setFacilityName(riverName);
                            base.setGeom(geometry);
                            base.setGeomType(geometry.getGeometryType());
                            base.setSrid(TARGET_SRID);
                            base.setSourceType("import");
                            baseMapper.insert(base);

                            YzRiverChannelDO updateChannel = new YzRiverChannelDO();
                            updateChannel.setId(channel.getId());
                            updateChannel.setFacilityId(baseId);
                            if (StrUtil.isBlank(channel.getRiverCode())) {
                                updateChannel.setRiverCode(riverCode);
                            }
                            riverChannelMapper.updateById(updateChannel);
                        }
                    }

                    item.setAction("update");
                    updateCount++;
                } else {
                    throw new IllegalStateException("存在多个同名河道，无法确定更新目标");
                }

                item.setSuccess(true);
                item.setMessage("处理成功");
                successCount++;
            } catch (Exception ex) {
                item.setSuccess(false);
                item.setMessage(StrUtil.blankToDefault(ex.getMessage(), "处理失败"));
                respVO.addItem(item);
                failCount++;
                continue;
            }
        }

        respVO.setSuccessCount(successCount);
        respVO.setCreateCount(createCount);
        respVO.setUpdateCount(updateCount);
        respVO.setFailCount(failCount);
        respVO.setMessage(failCount > 0 ? "部分要素处理失败，请查看 items" : "导入完成");
        return respVO;
    }

    private boolean isGeoJson(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private String resolveOName(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        Object value = properties.get("o_name");
        if (value == null) {
            value = properties.get("O_Name");
        }
        if (value == null) {
            value = getIgnoreCase(properties, "O_Name");
        }
        if (value == null) {
            value = getIgnoreCase(properties, "name");
        }
        if (value == null) {
            value = getIgnoreCase(properties, "NAME");
        }
        return StrUtil.trimToNull(value == null ? null : String.valueOf(value));
    }

    /**
     * 从属性中解析河道编码（若存在），常见字段如 riverCode/river_code/hdbh 等。
     */
    private String resolveRiverCode(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        Object value = getIgnoreCase(properties, "riverCode");
        if (value == null) {
            value = getIgnoreCase(properties, "river_code");
        }
        if (value == null) {
            value = getIgnoreCase(properties, "hdbh");
        }
        return StrUtil.trimToNull(value == null ? null : String.valueOf(value));
    }

    /**
     * 按规则确保 riverCode 与 facilityCode 一致：
     * <ul>
     *   <li>优先使用数据库已有 riverCode</li>
     *   <li>其次使用文件提供的编码</li>
     *   <li>两者都没有则生成雪花编码</li>
     * </ul>
     */
    private String resolveExistingRiverCode(YzRiverChannelDO existing, String riverCodeFromFile) {
        if (existing != null && StrUtil.isNotBlank(existing.getRiverCode())) {
            return existing.getRiverCode();
        }
        if (StrUtil.isNotBlank(riverCodeFromFile)) {
            return riverCodeFromFile;
        }
        return String.valueOf(SNOWFLAKE.nextId());
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

    /**
     * 将几何转换为 SRID=4490。
     * <p>
     * 优先使用文件 crs 中声明的 SRID；未声明时按 4326 处理。
     */
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

    /**
     * 解析 GeoJSON 顶层 crs，提取 EPSG 编号（如 urn:ogc:def:crs:EPSG::4490）。
     */
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
            // 解析失败时忽略，按默认 SRID 处理
            return null;
        }
    }
}
