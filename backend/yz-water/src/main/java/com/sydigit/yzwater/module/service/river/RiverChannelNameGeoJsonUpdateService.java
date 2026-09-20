package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelNameGeoJsonUpdateRespVO;
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
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 河道 GeoJSON 更新服务（按 properties.name 匹配现有河道并更新几何）
 */
@Service
@Validated
@Slf4j
public class RiverChannelNameGeoJsonUpdateService {

    private static final Set<String> SUPPORTED_SUFFIX = Set.of(".geojson", ".json");
    private static final int TARGET_SRID = 4490;
    private static final int DEFAULT_SOURCE_SRID = 4326;
    private static final String FACILITY_TYPE_RIVER = "river";

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final Pattern EPSG_PATTERN = Pattern.compile("EPSG(?::|::)(\\d+)", Pattern.CASE_INSENSITIVE);

    private final GeometryFeatureFileReader featureFileReader;
    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzRiverChannelMapper riverChannelMapper;

    public RiverChannelNameGeoJsonUpdateService(GeometryFeatureFileReader featureFileReader,
                                               YzWaterFacilityBaseMapper baseMapper,
                                               YzRiverChannelMapper riverChannelMapper) {
        this.featureFileReader = featureFileReader;
        this.baseMapper = baseMapper;
        this.riverChannelMapper = riverChannelMapper;
    }

    /**
     * 免登录上传 GeoJSON，按 name 聚合后更新河道关联 geom。
     * <p>
     * 处理规则：
     * <ul>
     *   <li>先按 properties.name 分组聚合几何（同名要素 union），避免后者覆盖前者</li>
     *   <li>更新前先清空目标设施 geom/geomType/srid</li>
     *   <li>按 river_name 匹配 yz_river_channel，命中后更新其 facility_id 对应基础表</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public RiverChannelNameGeoJsonUpdateRespVO importRiverGeoJsonByName(MultipartFile file) {
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

        RiverChannelNameGeoJsonUpdateRespVO respVO = new RiverChannelNameGeoJsonUpdateRespVO();
        respVO.setFeatureCount(CollUtil.isEmpty(features) ? 0 : features.size());
        respVO.setNameGroupCount(0);
        respVO.setSuccessCount(0);
        respVO.setCreateCount(0);
        respVO.setUpdateCount(0);
        respVO.setClearCount(0);
        respVO.setFailCount(0);

        if (CollUtil.isEmpty(features)) {
            respVO.setMessage("文件中未读取到任何要素");
            return respVO;
        }

        int failCount = 0;
        Map<String, Geometry> mergedByRiverName = new LinkedHashMap<>();
        for (GeometryFeatureDTO feature : features) {
            RiverChannelNameGeoJsonUpdateRespVO.Item item = new RiverChannelNameGeoJsonUpdateRespVO.Item();
            item.setFeatureName(feature == null ? null : feature.getFeatureName());
            item.setAction("group");
            try {
                if (feature == null) {
                    throw new IllegalArgumentException("要素为空");
                }
                String riverName = resolveName(feature.getProperties());
                item.setRiverName(riverName);
                if (StrUtil.isBlank(riverName)) {
                    throw new IllegalArgumentException("properties.name 不能为空");
                }
                Geometry geometry = convertGeometry(feature.getGeometry(), sourceSrid);
                if (geometry == null || geometry.isEmpty()) {
                    throw new IllegalArgumentException("几何为空");
                }
                Geometry merged = mergeGeometry(mergedByRiverName.get(riverName), geometry);
                mergedByRiverName.put(riverName, merged);
            } catch (Exception ex) {
                item.setAction("skip");
                item.setSuccess(false);
                item.setMessage(StrUtil.blankToDefault(ex.getMessage(), "分组失败"));
                respVO.addItem(item);
                failCount++;
            }
        }

        respVO.setNameGroupCount(mergedByRiverName.size());
        if (mergedByRiverName.isEmpty()) {
            respVO.setFailCount(failCount);
            respVO.setMessage("未找到可导入的 name 分组");
            return respVO;
        }

        Map<String, List<YzRiverChannelDO>> channelMap = new LinkedHashMap<>();
        Set<Long> facilityIdsToClear = new LinkedHashSet<>();
        for (Map.Entry<String, Geometry> entry : mergedByRiverName.entrySet()) {
            String riverName = entry.getKey();
            List<YzRiverChannelDO> channels = riverChannelMapper.selectList(
                    new LambdaQueryWrapper<YzRiverChannelDO>().eq(YzRiverChannelDO::getRiverName, riverName));
            if (CollUtil.isEmpty(channels)) {
                RiverChannelNameGeoJsonUpdateRespVO.Item item = new RiverChannelNameGeoJsonUpdateRespVO.Item();
                item.setRiverName(riverName);
                item.setAction("skip");
                item.setSuccess(false);
                item.setMessage("未匹配到 river_name，已跳过");
                respVO.addItem(item);
                failCount++;
                continue;
            }
            channelMap.put(riverName, channels);
            for (YzRiverChannelDO channel : channels) {
                if (channel != null && channel.getFacilityId() != null) {
                    facilityIdsToClear.add(channel.getFacilityId());
                }
            }
        }

        int clearCount = clearFacilityGeometry(facilityIdsToClear);
        int successCount = 0;
        int createCount = 0;
        int updateCount = 0;
        for (Map.Entry<String, List<YzRiverChannelDO>> entry : channelMap.entrySet()) {
            String riverName = entry.getKey();
            Geometry mergedGeometry = mergedByRiverName.get(riverName);
            UpdateStat stat = applyMergedGeometry(riverName, mergedGeometry, entry.getValue());
            createCount += stat.getCreateCount();
            updateCount += stat.getUpdateCount();
            successCount++;
        }

        respVO.setClearCount(clearCount);
        respVO.setSuccessCount(successCount);
        respVO.setCreateCount(createCount);
        respVO.setUpdateCount(updateCount);
        respVO.setFailCount(failCount);
        respVO.setMessage(failCount > 0 ? "部分 name 分组处理失败，请查看 items" : "更新完成");
        return respVO;
    }

    private UpdateStat applyMergedGeometry(String riverName, Geometry mergedGeometry, List<YzRiverChannelDO> channels) {
        UpdateStat stat = new UpdateStat();
        if (CollUtil.isEmpty(channels) || mergedGeometry == null || mergedGeometry.isEmpty()) {
            return stat;
        }
        for (YzRiverChannelDO channel : channels) {
            if (channel == null || channel.getId() == null) {
                continue;
            }
            String riverCode = resolveExistingRiverCode(channel);
            Long facilityId = channel.getFacilityId();
            if (facilityId == null) {
                Long baseId = SNOWFLAKE.nextId();
                YzWaterFacilityBaseDO base = buildBaseRecord(baseId, riverCode, riverName, mergedGeometry);
                baseMapper.insert(base);

                YzRiverChannelDO updateChannel = new YzRiverChannelDO();
                updateChannel.setId(channel.getId());
                updateChannel.setFacilityId(baseId);
                if (StrUtil.isBlank(channel.getRiverCode())) {
                    updateChannel.setRiverCode(riverCode);
                }
                riverChannelMapper.updateById(updateChannel);
                stat.setCreateCount(stat.getCreateCount() + 1);
                stat.setUpdateCount(stat.getUpdateCount() + 1);
                continue;
            }

            YzWaterFacilityBaseDO update = new YzWaterFacilityBaseDO();
            update.setId(facilityId);
            update.setFacilityCode(riverCode);
            update.setFacilityType(FACILITY_TYPE_RIVER);
            update.setFacilityName(riverName);
            update.setGeomType(mergedGeometry.getGeometryType());
            update.setGeom(mergedGeometry);
            update.setSrid(TARGET_SRID);
            int updated = baseMapper.updateById(update);
            if (updated <= 0) {
                YzWaterFacilityBaseDO base = buildBaseRecord(facilityId, riverCode, riverName, mergedGeometry);
                baseMapper.insert(base);
                stat.setCreateCount(stat.getCreateCount() + 1);
            }
            stat.setUpdateCount(stat.getUpdateCount() + 1);
        }
        return stat;
    }

    /**
     * 导入前清空目标设施原有几何数据。
     */
    private int clearFacilityGeometry(Set<Long> facilityIds) {
        if (CollUtil.isEmpty(facilityIds)) {
            return 0;
        }
        int clearCount = 0;
        for (Long facilityId : facilityIds) {
            if (facilityId == null) {
                continue;
            }
            int updated = baseMapper.clearGeomById(facilityId);
            if (updated > 0) {
                clearCount++;
            }
        }
        return clearCount;
    }

    private YzWaterFacilityBaseDO buildBaseRecord(Long baseId, String riverCode, String riverName, Geometry geometry) {
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityCode(riverCode);
        base.setFacilityType(FACILITY_TYPE_RIVER);
        base.setFacilityName(riverName);
        base.setGeomType(geometry == null ? null : geometry.getGeometryType());
        base.setGeom(geometry);
        base.setSrid(geometry == null ? null : TARGET_SRID);
        base.setSourceType("import");
        return base;
    }

    private boolean isGeoJson(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return SUPPORTED_SUFFIX.stream().anyMatch(lower::endsWith);
    }

    private String resolveName(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        Object value = properties.get("name");
        if (value == null) {
            value = properties.get("Name");
        }
        if (value == null) {
            value = properties.get("NAME");
        }
        if (value == null) {
            value = getIgnoreCase(properties, "name");
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

    private String resolveExistingRiverCode(YzRiverChannelDO existing) {
        if (existing != null && StrUtil.isNotBlank(existing.getRiverCode())) {
            return existing.getRiverCode();
        }
        return String.valueOf(SNOWFLAKE.nextId());
    }

    private Geometry mergeGeometry(Geometry existing, Geometry incoming) {
        if (existing == null) {
            incoming.setSRID(TARGET_SRID);
            return incoming;
        }
        Geometry merged;
        try {
            List<Geometry> geometryList = new ArrayList<>();
            geometryList.add(existing);
            geometryList.add(incoming);
            merged = UnaryUnionOp.union(geometryList);
        } catch (Exception ex) {
            throw new IllegalArgumentException("同名要素几何聚合失败：" + ex.getMessage());
        }
        if (merged == null || merged.isEmpty()) {
            throw new IllegalArgumentException("同名要素几何聚合后为空");
        }
        merged.setSRID(TARGET_SRID);
        return merged;
    }

    /**
     * 将几何转换为 SRID=4490。
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

    private static class UpdateStat {
        private int createCount;
        private int updateCount;

        public int getCreateCount() {
            return createCount;
        }

        public void setCreateCount(int createCount) {
            this.createCount = createCount;
        }

        public int getUpdateCount() {
            return updateCount;
        }

        public void setUpdateCount(int updateCount) {
            this.updateCount = updateCount;
        }
    }
}
