package com.sydigit.yzwater.module.service.geoBase;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirBaseSyncRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzBaseReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzBaseReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.util.ManagementUnitUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import lombok.extern.slf4j.Slf4j;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * yz_base_reservoir 数据同步服务
 */
@Service
@Validated
@Slf4j
public class ReservoirBaseSyncService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final int TARGET_SRID = 4490;
    private static final String FACILITY_TYPE_RESERVOIR = "reservoir";
    private static final String FILTER_NAME_KEYWORD_DAM = "大坝";

    private final ObjectMapper objectMapper;
    private final DictDataCommonApi dictDataApi;
    private final YzBaseReservoirMapper baseReservoirMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;

    public ReservoirBaseSyncService(ObjectMapper objectMapper,
                                    DictDataCommonApi dictDataApi,
                                    YzBaseReservoirMapper baseReservoirMapper,
                                    YzWaterFacilityBaseMapper facilityBaseMapper,
                                    YzWaterReservoirMapper waterReservoirMapper) {
        this.objectMapper = objectMapper;
        this.dictDataApi = dictDataApi;
        this.baseReservoirMapper = baseReservoirMapper;
        this.facilityBaseMapper = facilityBaseMapper;
        this.waterReservoirMapper = waterReservoirMapper;
    }

    /**
     * 将 yz_base_reservoir 数据同步到水利设施与水库业务表
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservoirBaseSyncRespVO syncFromBase() {
        List<YzBaseReservoirDO> baseList = baseReservoirMapper.selectList();
        if (baseList == null || baseList.isEmpty()) {
            return new ReservoirBaseSyncRespVO();
        }
        Map<String, String> scaleDict = buildDictValueMap(dictDataApi.getDictDataList(ZdConstants.ZD_SKGM));
        ReservoirBaseSyncRespVO resp = new ReservoirBaseSyncRespVO();
        resp.setTotal(baseList.size());
        for (YzBaseReservoirDO base : baseList) {
            if (shouldSkipByName(base == null ? null : base.getName())) {
                resp.addNameFiltered();
                continue;
            }
            if (base == null || StrUtil.isBlank(base.getCode()) || StrUtil.isBlank(base.getName())) {
                resp.addDataSkipped();
                continue;
            }
            Geometry geometry = parseGeometry(base.getGeo());
            String geomType = geometry != null ? geometry.getGeometryType() : null;
            if (geometry != null) {
                geometry.setSRID(TARGET_SRID);
            }
            if (geometry == null) {
                resp.addGeometrySkipped();
            }
            Long existingFacilityId = findExistingFacilityId(base);
            if (existingFacilityId != null) {
                updateFacility(existingFacilityId, base, geometry, geomType);
                upsertReservoir(existingFacilityId, base, scaleDict);
                if (geometry != null) {
                    resp.addUpdated();
                }
                continue;
            }
            Long facilityId = SNOWFLAKE.nextId();
            YzWaterFacilityBaseDO facility = buildFacility(base, facilityId, geometry, geomType);
            YzWaterReservoirDO reservoir = buildReservoir(base, facilityId, scaleDict);
            facilityBaseMapper.insert(facility);
            waterReservoirMapper.insert(reservoir);
            resp.addInserted();
        }
        return resp;
    }

    private Long findExistingFacilityId(YzBaseReservoirDO base) {
        // 优先按编码匹配，避免同名导致重复
        if (StrUtil.isNotBlank(base.getCode())) {
            YzWaterFacilityBaseDO byCode = facilityBaseMapper.selectOne(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                    .eq(YzWaterFacilityBaseDO::getFacilityCode, base.getCode())
                    .last("limit 1"));
            if (byCode != null) {
                return byCode.getId();
            }
        }
        // 兼容历史数据：按名称兜底匹配
        if (StrUtil.isNotBlank(base.getName())) {
            YzWaterFacilityBaseDO byName = facilityBaseMapper.selectOne(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                    .eq(YzWaterFacilityBaseDO::getFacilityName, base.getName())
                    .last("limit 1"));
            if (byName != null) {
                return byName.getId();
            }
        }
        return null;
    }

    private void updateFacility(Long facilityId, YzBaseReservoirDO base, Geometry geometry, String geomType) {
        YzWaterFacilityBaseDO update = new YzWaterFacilityBaseDO();
        update.setId(facilityId);
        update.setFacilityCode(base.getCode());
        update.setFacilityName(base.getName());
        update.setFacilityType(FACILITY_TYPE_RESERVOIR);
        update.setAdminRegion(base.getSzxz());
        update.setAdminRegionCode(base.getDivisionCode());
        update.setManageUnit(base.getGldw());
        if (geometry != null) {
            update.setGeomType(geomType);
            update.setSrid(TARGET_SRID);
            update.setGeom(geometry);
        }
        update.setSourceType("import");
        facilityBaseMapper.updateById(update);
    }

    private void upsertReservoir(Long facilityId, YzBaseReservoirDO base, Map<String, String> scaleDict) {
        YzWaterReservoirDO existing = waterReservoirMapper.selectOne(new LambdaQueryWrapper<YzWaterReservoirDO>()
                .eq(YzWaterReservoirDO::getFacilityId, facilityId)
                .last("limit 1"));
        YzWaterReservoirDO reservoir = buildReservoir(base, facilityId, scaleDict);
        if (existing != null) {
            reservoir.setId(existing.getId());
            waterReservoirMapper.updateById(reservoir);
        } else {
            waterReservoirMapper.insert(reservoir);
        }
    }

    static boolean shouldSkipByName(String name) {
        return StrUtil.isNotBlank(name) && name.contains(FILTER_NAME_KEYWORD_DAM);
    }

    static Map<String, String> buildDictValueMap(List<DictDataRespDTO> dictDataList) {
        Map<String, String> map = new HashMap<>();
        if (dictDataList == null || dictDataList.isEmpty()) {
            return map;
        }
        for (DictDataRespDTO dictData : dictDataList) {
            if (dictData == null) {
                continue;
            }
            String value = StrUtil.blankToDefault(dictData.getValue(), "").trim();
            String label = StrUtil.blankToDefault(dictData.getLabel(), "").trim();
            if (StrUtil.isBlank(value)) {
                continue;
            }
            map.put(normalizeDictKey(value), value);
            if (StrUtil.isNotBlank(label)) {
                map.put(normalizeDictKey(label), value);
            }
        }
        return map;
    }

    static String mapDictValueOrNull(Map<String, String> dict, String input) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        if (dict == null || dict.isEmpty()) {
            return null;
        }
        return dict.get(normalizeDictKey(input));
    }

    private static String normalizeDictKey(String text) {
        return StrUtil.blankToDefault(text, "").trim().toLowerCase(Locale.ROOT);
    }

    private YzWaterFacilityBaseDO buildFacility(YzBaseReservoirDO base,
                                                Long facilityId,
                                                Geometry geometry,
                                                String geomType) {
        YzWaterFacilityBaseDO facility = new YzWaterFacilityBaseDO();
        facility.setId(facilityId);
        facility.setFacilityCode(base.getCode());
        facility.setFacilityName(base.getName());
        facility.setFacilityType(FACILITY_TYPE_RESERVOIR);
        facility.setAdminRegion(base.getSzxz());
        facility.setAdminRegionCode(base.getDivisionCode());
        facility.setManageUnit(base.getGldw());
        if (geometry != null) {
            facility.setGeomType(geomType);
            facility.setSrid(TARGET_SRID);
            facility.setGeom(geometry);
        }
        facility.setSourceType("import");
        return facility;
    }

    private YzWaterReservoirDO buildReservoir(YzBaseReservoirDO base, Long facilityId, Map<String, String> scaleDict) {
        YzWaterReservoirDO reservoir = new YzWaterReservoirDO();
        reservoir.setId(SNOWFLAKE.nextId());
        reservoir.setFacilityId(facilityId);
        reservoir.setReservoirCode(base.getCode());
        reservoir.setReservoirName(base.getName());
        reservoir.setReservoirScale(mapDictValueOrNull(scaleDict, base.getGm()));
        String townshipCode = StrUtil.trimToNull(base.getDivisionCode());
        String townshipName = StrUtil.trimToNull(base.getSzxz());
        if (StrUtil.isNotBlank(townshipCode)) {
            reservoir.setTownship(new String[]{townshipCode});
        } else if (StrUtil.isNotBlank(townshipName)) {
            reservoir.setTownship(new String[]{townshipName});
        } else {
            reservoir.setTownship(null);
        }
        // managementUnit 已升级为 text[]，这里将基础表的字符串拆分后按数组存储
        reservoir.setManagementUnit(ManagementUnitUtils.parseManagementUnit(base.getGldw()));
        reservoir.setReservoirNature(base.getType());
        reservoir.setLongitude(toBigDecimal(base.getLongitude()));
        reservoir.setLatitude(toBigDecimal(base.getLatitude()));
        reservoir.setCatchmentArea(base.getJsmj());
        reservoir.setTotalCapacity(base.getAmount());
        reservoir.setActiveCapacity(base.getXlkr());
        reservoir.setFloodControlCapacity(base.getThkr());
        reservoir.setDeadCapacity(base.getSkr());
        reservoir.setDesignFloodLevel(base.getSjsw());
        reservoir.setNormalOperatingLevel(base.getXlsw());
        reservoir.setFloodLimitLevel(base.getXxsw());
        reservoir.setDeadLevel(base.getSsw());
        reservoir.setDesignIrrigationArea(base.getSjggmj());
        reservoir.setActualIrrigationArea(base.getGgmj() == null ? null : base.getGgmj().toPlainString());
        reservoir.setAnnualWaterSupply(base.getNgsl());
        reservoir.setFisheryArea(base.getYymj());
        reservoir.setDamTopHeight(base.getHeight());
        reservoir.setDamTopLength(base.getLength() == null ? null : base.getLength().toPlainString());
        return reservoir;
    }

    private BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }

    private Geometry parseGeometry(String geoJson) {
        if (StrUtil.isBlank(geoJson)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(geoJson);
            JsonNode geoNode = root.has("geo") ? root.get("geo") : root;
            JsonNode geomNode = null;
            if (geoNode.has("geometry")) {
                geomNode = geoNode.get("geometry");
            }
            if (geomNode == null && geoNode.has("features")) {
                JsonNode features = geoNode.get("features");
                if (features.isArray() && features.size() > 0) {
                    JsonNode first = features.get(0);
                    if (first != null && first.has("geometry")) {
                        geomNode = first.get("geometry");
                    }
                }
            }
            if (geomNode == null && geoNode.has("type") && geoNode.has("coordinates")) {
                geomNode = geoNode;
            }
            if (geomNode == null || geomNode.isNull()) {
                return null;
            }
            GeometryJSON geometryJSON = new GeometryJSON();
            Geometry geometry = geometryJSON.read(geomNode.toString());
            if (geometry != null) {
                geometry.setSRID(TARGET_SRID);
            }
            return geometry;
        } catch (Exception ex) {
            log.warn("解析 GeoJSON 失败，已跳过", ex);
            return null;
        }
    }
}
