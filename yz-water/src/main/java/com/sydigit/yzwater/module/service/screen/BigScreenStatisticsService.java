package com.sydigit.yzwater.module.service.screen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.util.GuestRoleUtils;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.framework.security.core.LoginUser;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictLabelValueRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictCountItemVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFacilityGeomReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFacilityGeomRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenReservoirAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelSearchRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelStatsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverHeadStatsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelWithSectionsListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelWithSectionsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardLocationItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardLocationRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardProblemItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardProblemListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardReferenceDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenPumpStationAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenEmbankmentAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardLayerRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialWarehouseAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenIrrigationDistrictAreaOverviewRespVO;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import com.sydigit.yzwater.module.dal.dataobject.pump.YzPumpStationDO;
import com.sydigit.yzwater.module.dal.dataobject.problem.YzProblemFeedbackDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.mysql.embankment.YzEmbankmentMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import com.sydigit.yzwater.module.dal.mysql.irrigation.YzIrrigationDistrictMapper;
import com.sydigit.yzwater.module.dal.mysql.pump.YzPumpStationMapper;
import com.sydigit.yzwater.module.dal.mysql.problem.YzProblemFeedbackMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardBfMapper;
import com.sydigit.yzwater.module.dal.mysql.system.SystemUserSimpleMapper;
import com.sydigit.yzwater.module.service.flood.FxTaskScreenLayerService;
import com.sydigit.yzwater.module.system.service.area.SystemAreaService;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaNode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 大屏统计服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class BigScreenStatisticsService {

    private final YzRiverChannelManagementMapper managementMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    /** 大屏河道统计口径：与 yz/rivers-bf 管理页一致，读 yz_river_channel_bf */
    private final YzRiverChannelBfMapper riverChannelBfMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzSignboardBfMapper signboardBfMapper;
    private final YzProblemFeedbackMapper problemFeedbackMapper;
    private final SystemUserSimpleMapper systemUserSimpleMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzEmbankmentMapper embankmentMapper;
    private final YzPumpStationMapper pumpStationMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final YzFloodPreventionMaterialWarehouseMapper floodWarehouseMapper;
    private final YzIrrigationDistrictMapper irrigationDistrictMapper;
    private final DictDataCommonApi dictDataApi;
    private final SystemAreaService systemAreaService;
    private final FxTaskScreenLayerService fxTaskScreenLayerService;

    private static final int STATUS_FINISHED = 4;//已办结
    private static final int STATUS_REJECTED = 1;//已驳回
    private static final String ROLE_GUEST = "guest";
    private static final String VILLAGE_RIVER_LEVEL = "7j";
    private static final List<String> ADMIN_ROLE_CODES = List.of("super_admin", "tenant_admin", "crm_admin");
    /**
     * 河长姓名归一化表达式：去除半角/全角空白，空串视为 null。
     * 与 riverChiefInfo 页面统计口径保持一致。
     */
    private static final String NORMALIZED_HEAD_NAME_SQL =
            "NULLIF(replace(regexp_replace(COALESCE(head_name, ''), '[[:space:]]+', '', 'g'), chr(12288), ''), '')";
    /**
     * 河长级别归一化表达式：首尾空白清理后，空串视为 null。
     * 与 riverChiefInfo 页面“姓名 + 级别”聚合口径保持一致。
     */
    private static final String NORMALIZED_HEAD_LEVEL_SQL =
            "NULLIF(BTRIM(COALESCE(head_level, '')), '')";

    /**
     * 统计河长总数（按河长维度聚合口径）
     */
    public BigScreenRiverHeadStatsRespVO getRiverHeadStats() {
        // 与 yz/headLevel/riverChiefInfo 页面保持一致：按“归一化河长姓名 + 归一化河长级别”聚合统计
        Map<String, Long> countMap = selectCurrentChiefCountByHeadLevel();
        Map<String, String> labelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);
        List<BigScreenDictCountItemVO> list = buildDictCountList(countMap, labelMap);

        BigScreenRiverHeadStatsRespVO resp = new BigScreenRiverHeadStatsRespVO();
        resp.setLevelStats(list);
        // totalCount 口径与 riverChiefInfo 页面总数一致：按“归一化河长姓名 + 归一化河长级别”全量去重
        resp.setTotalCount(selectCurrentChiefTotalCount());
        resp.setTotalChiefCount(selectCurrentTotalChiefCount());
        resp.setProvince(pickByLabelKeywords(list, "省"));
        resp.setCity(pickByLabelKeywords(list, "市"));
        resp.setCounty(pickByLabelKeywords(list, "县", "区"));
        resp.setTown(pickByLabelKeywords(list, "乡", "镇"));
        resp.setVillage(pickByLabelKeywords(list, "村"));
        return resp;
    }

    /**
     * 统计河道总数、总长度、总流域面积。
     * <p>口径：读 {@code yz_river_channel_bf}（与河道管理 rivers-bf 一致），排除村级 {@code 7j}。</p>
     */
    public BigScreenRiverChannelStatsRespVO getRiverChannelStats() {
        Map<String, Long> countMap = selectGroupCount(
                riverChannelBfMapper,
                "river_level",
                buildExcludeVillageRiverBfWrapper()
        );
        Map<String, String> labelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        List<BigScreenDictCountItemVO> list = buildDictCountList(countMap, labelMap);

        QueryWrapper<YzRiverChannelBfDO> totalWrapper = buildExcludeVillageRiverBfWrapper()
                .select(
                        "count(1) as totalcount",
                        "coalesce(sum(length_km), 0) as totallengthkm",
                        "coalesce(sum(catchment_km2), 0) as totalcatchmentkm2"
                );
        Map<String, Object> totalMap = riverChannelBfMapper.selectMaps(totalWrapper).stream().findFirst().orElseGet(HashMap::new);

        BigScreenRiverChannelStatsRespVO resp = new BigScreenRiverChannelStatsRespVO();
        resp.setLevelStats(list);
        resp.setTotalCount(toLong(totalMap.get("totalcount")));
        resp.setTotalLengthKm(toBigDecimal(totalMap.get("totallengthkm")));
        resp.setTotalCatchmentKm2(toBigDecimal(totalMap.get("totalcatchmentkm2")));
     /*   resp.setProvince(pickByLabelKeywords(list, "省"));
        resp.setCity(pickByLabelKeywords(list, "市"));
        resp.setCounty(pickByLabelKeywords(list, "县", "区"));
        resp.setTown(pickByLabelKeywords(list, "乡", "镇"));
        resp.setVillage(pickByLabelKeywords(list, "村"));*/
        return resp;
    }

    private QueryWrapper<YzRiverChannelDO> buildExcludeVillageRiverWrapper() {
        return new QueryWrapper<YzRiverChannelDO>()
                .ne("river_level", VILLAGE_RIVER_LEVEL);
    }

    private QueryWrapper<YzRiverChannelBfDO> buildExcludeVillageRiverBfWrapper() {
        return new QueryWrapper<YzRiverChannelBfDO>()
                .ne("river_level", VILLAGE_RIVER_LEVEL);
    }

    /**
     * 查询设施类别字典（zd_sslb），仅返回 label/value。
     */
    public List<BigScreenDictLabelValueRespVO> getFacilityTypeDictList() {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(ZdConstants.ZD_SSLB);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(it -> it != null && StrUtil.isNotBlank(it.getValue()))
                .map(it -> {
                    BigScreenDictLabelValueRespVO vo = new BigScreenDictLabelValueRespVO();
                    vo.setValue(it.getValue());
                    vo.setLabel(StrUtil.blankToDefault(it.getLabel(), it.getValue()));
                    return vo;
                })
                .sorted((a, b) -> StrUtil.compare(a.getLabel(), b.getLabel(), true))
                .collect(Collectors.toList());
    }

    /**
     * 根据设施类别查询几何信息（WKT）与设备名称/主键。
     * <p>
     * 说明：
     * <ul>
     *     <li>几何数据来自 yz_water_facility_base.geom</li>
     *     <li>设备名称与主键来自各设施业务表，按 facility_id 与基础表 id 关联</li>
     * </ul>
     */
    public List<BigScreenFacilityGeomRespVO> getFacilityGeomList(BigScreenFacilityGeomReqVO reqVO) {
        if (reqVO == null || reqVO.getValue() == null || reqVO.getValue().isEmpty()) {
            return List.of();
        }
        List<String> types = reqVO.getValue().stream()
                .filter(StrUtil::isNotBlank)
                .map(StrUtil::trim)
                .distinct()
                .collect(Collectors.toList());
        if (types.isEmpty()) {
            return List.of();
        }

        boolean hasRiverType = types.stream().anyMatch(it -> StrUtil.equalsIgnoreCase(it, "river"));
        String riverLevelValue = StrUtil.trimToEmpty(reqVO.getRiverLevel());
        boolean filterRiverByLevel = hasRiverType && StrUtil.isNotBlank(riverLevelValue);

        // 仅当入参包含河道类型时，才加载河道等级字典映射，避免无谓的字典查询
        Map<String, String> riverLevelLabelMap = hasRiverType
                ? loadDictLabelMap(ZdConstants.ZD_HLJB)
                : Map.of();

        Set<Long> matchedRiverFacilityIds = Collections.emptySet();
        if (filterRiverByLevel) {
            // 河道级别不为空时，先筛出匹配的河道设施 ID
            List<YzRiverChannelDO> matchedRivers = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                    .select(YzRiverChannelDO::getFacilityId)
                    .eq(YzRiverChannelDO::getRiverLevel, riverLevelValue)
                    .isNotNull(YzRiverChannelDO::getFacilityId));
            if (matchedRivers != null && !matchedRivers.isEmpty()) {
                matchedRiverFacilityIds = matchedRivers.stream()
                        .map(YzRiverChannelDO::getFacilityId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
            }
        }

        List<YzWaterFacilityBaseDO> baseList = new ArrayList<>();
        List<String> nonRiverTypes = types.stream()
                .filter(it -> !StrUtil.equalsIgnoreCase(it, "river"))
                .collect(Collectors.toList());
        if (!nonRiverTypes.isEmpty()) {
            baseList.addAll(facilityBaseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                    .select(YzWaterFacilityBaseDO::getId, YzWaterFacilityBaseDO::getFacilityType, YzWaterFacilityBaseDO::getGeom)
                    .in(YzWaterFacilityBaseDO::getFacilityType, nonRiverTypes)
                    .isNotNull(YzWaterFacilityBaseDO::getGeom)));
        }
        if (hasRiverType) {
            if (!filterRiverByLevel) {
                baseList.addAll(facilityBaseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                        .select(YzWaterFacilityBaseDO::getId, YzWaterFacilityBaseDO::getFacilityType, YzWaterFacilityBaseDO::getGeom)
                        .eq(YzWaterFacilityBaseDO::getFacilityType, "river")
                        .isNotNull(YzWaterFacilityBaseDO::getGeom)));
            } else if (!matchedRiverFacilityIds.isEmpty()) {
                baseList.addAll(facilityBaseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                        .select(YzWaterFacilityBaseDO::getId, YzWaterFacilityBaseDO::getFacilityType, YzWaterFacilityBaseDO::getGeom)
                        .eq(YzWaterFacilityBaseDO::getFacilityType, "river")
                        .in(YzWaterFacilityBaseDO::getId, matchedRiverFacilityIds)
                        .isNotNull(YzWaterFacilityBaseDO::getGeom)));
            }
        }
        if (baseList.isEmpty()) {
            return List.of();
        }

        Map<String, List<Long>> baseIdsByType = baseList.stream()
                .filter(it -> it != null && it.getId() != null && StrUtil.isNotBlank(it.getFacilityType()))
                .collect(Collectors.groupingBy(YzWaterFacilityBaseDO::getFacilityType,
                        Collectors.mapping(YzWaterFacilityBaseDO::getId, Collectors.toList())));

        Map<String, Map<Long, DeviceInfo>> deviceMapByType = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : baseIdsByType.entrySet()) {
            String facilityType = entry.getKey();
            List<Long> ids = entry.getValue();
            if (StrUtil.isBlank(facilityType) || ids == null || ids.isEmpty()) {
                continue;
            }
            Map<Long, DeviceInfo> deviceMap = loadDeviceInfoByType(facilityType, ids);
            if (!deviceMap.isEmpty()) {
                deviceMapByType.put(facilityType, deviceMap);
            }
        }

        WKTWriter wktWriter = new WKTWriter();
        List<BigScreenFacilityGeomRespVO> result = new ArrayList<>(baseList.size());
        for (YzWaterFacilityBaseDO base : baseList) {
            if (base == null || base.getId() == null || StrUtil.isBlank(base.getFacilityType()) || base.getGeom() == null) {
                continue;
            }
            Map<Long, DeviceInfo> map = deviceMapByType.get(base.getFacilityType());
            if (map == null) {
                continue;
            }
            DeviceInfo device = map.get(base.getId());
            if (device == null || device.deviceId == null || StrUtil.isBlank(device.deviceName)) {
                continue;
            }

            BigScreenFacilityGeomRespVO vo = new BigScreenFacilityGeomRespVO();
            vo.setFacilityType(base.getFacilityType());
            vo.setFacilityBaseId(base.getId());
            vo.setDeviceId(device.deviceId);
            vo.setDeviceName(device.deviceName);
            if (StrUtil.equalsIgnoreCase(base.getFacilityType(), "river") && StrUtil.isNotBlank(device.riverLevel)) {
                String levelValue = StrUtil.trimToEmpty(device.riverLevel);
                vo.setRiverLevel(StrUtil.blankToDefault(riverLevelLabelMap.get(levelValue), levelValue));
            }
            vo.setGeomWkt(wktWriter.write(base.getGeom()));
            result.add(vo);
        }
        fillTodoProblemFlag(result);
        return result;
    }

    /**
     * 河道总览（不分页，统计全量河道）。
     */
    public BigScreenRiverAreaOverviewRespVO getRiverAreaOverview() {
        Map<String, Object> summary = riverChannelMapper.selectAllSummary();
        long totalCount = toLong(summary == null ? null : summary.get("total_count"));

        BigScreenRiverAreaOverviewRespVO resp = new BigScreenRiverAreaOverviewRespVO();
        resp.setTotalCount(totalCount);
        resp.setTotalCatchmentKm2(toBigDecimal(summary == null ? null : summary.get("total_catchment_km2")));
        resp.setTotalLengthKm(toBigDecimal(summary == null ? null : summary.get("total_length_km")));

        // 河道级别数量（统计全部河道）
        List<Map<String, Object>> levelRows = riverChannelMapper.selectAllRiverLevelCount();
        Map<String, Long> levelCountMap = new HashMap<>();
        if (levelRows != null) {
            for (Map<String, Object> row : levelRows) {
                if (row == null) {
                    continue;
                }
                String level = Objects.toString(row.get("river_level"), "");
                if (StrUtil.isBlank(level)) {
                    continue;
                }
                levelCountMap.put(level, toLong(row.get("cnt")));
            }
        }
        resp.setRiverLevelCountMap(levelCountMap);

        if (totalCount <= 0) {
            resp.setList(List.of());
            return resp;
        }

        List<YzRiverChannelDO> rivers = riverChannelMapper.selectAllRiverList();
        if (rivers == null || rivers.isEmpty()) {
            resp.setList(List.of());
            return resp;
        }

        Map<String, String> levelLabelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        List<BigScreenRiverAreaOverviewRespVO.Item> items = rivers.stream()
                .filter(it -> it != null && it.getFacilityId() != null)
                .map(it -> {
                    BigScreenRiverAreaOverviewRespVO.Item item = new BigScreenRiverAreaOverviewRespVO.Item();
                    item.setRiverId(it.getId());
                    item.setFacilityBaseId(it.getFacilityId());
                    item.setRiverName(StrUtil.blankToDefault(it.getRiverName(), ""));
                    String levelValue = StrUtil.trimToEmpty(it.getRiverLevel());
                    item.setRiverLevelLabel(StrUtil.blankToDefault(levelLabelMap.get(levelValue), levelValue));
                    item.setLengthKm(it.getLengthKm());
                    item.setLongitude(it.getCentroidLongitude());
                    item.setLatitude(it.getCentroidLatitude());
                    return item;
                })
                .collect(Collectors.toList());
        resp.setList(items);
        return resp;
    }

    /**
     * 河道总览（不分页，按行政区划筛选，包含子级行政区划匹配）。
     *
     * <p>说明：入参为 /system/area/tree 的 id；若选择父级（如乡镇 type=5），也会统计其下子级（如村 type=6）关联的河道。</p>
     */
    public BigScreenRiverAreaOverviewRespVO getRiverAreaOverviewByArea(Long areaId) {
        if (areaId == null) {
            BigScreenRiverAreaOverviewRespVO empty = new BigScreenRiverAreaOverviewRespVO();
            empty.setTotalCount(0L);
            empty.setTotalCatchmentKm2(BigDecimal.ZERO);
            empty.setTotalLengthKm(BigDecimal.ZERO);
            empty.setRiverLevelCountMap(Collections.emptyMap());
            empty.setList(List.of());
            return empty;
        }

        String[] areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.length == 0) {
            BigScreenRiverAreaOverviewRespVO empty = new BigScreenRiverAreaOverviewRespVO();
            empty.setTotalCount(0L);
            empty.setTotalCatchmentKm2(BigDecimal.ZERO);
            empty.setTotalLengthKm(BigDecimal.ZERO);
            empty.setRiverLevelCountMap(Collections.emptyMap());
            empty.setList(List.of());
            return empty;
        }

        Map<String, Object> summary = riverChannelMapper.selectSummaryByTowns(areaCodes);
        long totalCount = toLong(summary == null ? null : summary.get("total_count"));

        BigScreenRiverAreaOverviewRespVO resp = new BigScreenRiverAreaOverviewRespVO();
        resp.setTotalCount(totalCount);
        resp.setTotalCatchmentKm2(toBigDecimal(summary == null ? null : summary.get("total_catchment_km2")));
        resp.setTotalLengthKm(toBigDecimal(summary == null ? null : summary.get("total_length_km")));

        // 河道级别数量（按行政区划筛选）
        List<Map<String, Object>> levelRows = riverChannelMapper.selectRiverLevelCountByTowns(areaCodes);
        Map<String, Long> levelCountMap = new HashMap<>();
        if (levelRows != null) {
            for (Map<String, Object> row : levelRows) {
                if (row == null) {
                    continue;
                }
                String level = Objects.toString(row.get("river_level"), "");
                if (StrUtil.isBlank(level)) {
                    continue;
                }
                levelCountMap.put(level, toLong(row.get("cnt")));
            }
        }
        resp.setRiverLevelCountMap(levelCountMap);

        if (totalCount <= 0) {
            resp.setList(List.of());
            return resp;
        }

        List<YzRiverChannelDO> rivers = riverChannelMapper.selectRiverListByTowns(areaCodes);
        if (rivers == null || rivers.isEmpty()) {
            resp.setList(List.of());
            return resp;
        }

        Map<String, String> levelLabelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        List<BigScreenRiverAreaOverviewRespVO.Item> items = rivers.stream()
                .filter(it -> it != null && it.getFacilityId() != null)
                .map(it -> {
                    BigScreenRiverAreaOverviewRespVO.Item item = new BigScreenRiverAreaOverviewRespVO.Item();
                    item.setRiverId(it.getId());
                    item.setFacilityBaseId(it.getFacilityId());
                    item.setRiverName(StrUtil.blankToDefault(it.getRiverName(), ""));
                    String levelValue = StrUtil.trimToEmpty(it.getRiverLevel());
                    item.setRiverLevelLabel(StrUtil.blankToDefault(levelLabelMap.get(levelValue), levelValue));
                    item.setLengthKm(it.getLengthKm());
                    item.setLongitude(it.getCentroidLongitude());
                    item.setLatitude(it.getCentroidLatitude());
                    return item;
                })
                .collect(Collectors.toList());
        resp.setList(items);
        return resp;
    }

    /**
     * 水库总览（不分页，按行政区划筛选，包含子级行政区划匹配）。
     *
     * <p>说明：入参为 /system/area/tree 的 id；若选择父级（如乡镇 type=5），也会统计其下子级（如村 type=6）关联的水库。</p>
     */
    public BigScreenReservoirAreaOverviewRespVO getReservoirAreaOverviewByArea(Long areaId) {
        if (areaId == null) {
            BigScreenReservoirAreaOverviewRespVO empty = new BigScreenReservoirAreaOverviewRespVO();
            empty.setTotalCount(0L);
            empty.setTotalCapacity(BigDecimal.ZERO);
            empty.setTotalDamTopLength(BigDecimal.ZERO);
            empty.setTotalActiveCapacity(BigDecimal.ZERO);
            empty.setReservoirScaleCountMap(Collections.emptyMap());
            empty.setList(List.of());
            return empty;
        }

        String[] areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.length == 0) {
            BigScreenReservoirAreaOverviewRespVO empty = new BigScreenReservoirAreaOverviewRespVO();
            empty.setTotalCount(0L);
            empty.setTotalCapacity(BigDecimal.ZERO);
            empty.setTotalDamTopLength(BigDecimal.ZERO);
            empty.setTotalActiveCapacity(BigDecimal.ZERO);
            empty.setReservoirScaleCountMap(Collections.emptyMap());
            empty.setList(List.of());
            return empty;
        }

        Map<String, Object> summary = waterReservoirMapper.selectSummaryByTowns(areaCodes);
        long totalCount = toLong(summary == null ? null : summary.get("total_count"));

        BigScreenReservoirAreaOverviewRespVO resp = new BigScreenReservoirAreaOverviewRespVO();
        resp.setTotalCount(totalCount);
        resp.setTotalCapacity(toBigDecimal(summary == null ? null : summary.get("total_capacity")));
        resp.setTotalActiveCapacity(toBigDecimal(summary == null ? null : summary.get("total_active_capacity")));

        // 水库规模数量（按行政区划筛选）
        List<Map<String, Object>> scaleRows = waterReservoirMapper.selectReservoirScaleCountByTowns(areaCodes);
        Map<String, Long> scaleCountMap = new HashMap<>();
        if (scaleRows != null) {
            for (Map<String, Object> row : scaleRows) {
                if (row == null) {
                    continue;
                }
                String scale = Objects.toString(row.get("reservoir_scale"), "");
                if (StrUtil.isBlank(scale)) {
                    continue;
                }
                scaleCountMap.put(scale, toLong(row.get("cnt")));
            }
        }
        resp.setReservoirScaleCountMap(scaleCountMap);

        if (totalCount <= 0) {
            resp.setTotalDamTopLength(BigDecimal.ZERO);
            resp.setList(List.of());
            return resp;
        }

        List<Map<String, Object>> list = waterReservoirMapper.selectReservoirListByTowns(areaCodes);
        if (list == null || list.isEmpty()) {
            resp.setTotalDamTopLength(BigDecimal.ZERO);
            resp.setList(List.of());
            return resp;
        }

        BigDecimal totalDamTopLength = BigDecimal.ZERO;
        for (Map<String, Object> row : list) {
            if (row == null) {
                continue;
            }
            totalDamTopLength = totalDamTopLength.add(toBigDecimal(row.get("dam_top_length")));
        }
        List<BigScreenReservoirAreaOverviewRespVO.Item> items = list.stream()
                .filter(Objects::nonNull)
                .map(row -> {
                    BigScreenReservoirAreaOverviewRespVO.Item item = new BigScreenReservoirAreaOverviewRespVO.Item();
                    item.setReservoirId(toLongNullable(row.get("reservoir_id")));
                    item.setFacilityBaseId(toLongNullable(row.get("facility_base_id")));
                    item.setReservoirName(Objects.toString(row.get("reservoir_name"), ""));
                    item.setReservoirScale(Objects.toString(row.get("reservoir_scale"), ""));
                    item.setTotalCapacity(toBigDecimal(row.get("total_capacity")));
                    item.setLongitude(toBigDecimal(row.get("longitude")));
                    item.setLatitude(toBigDecimal(row.get("latitude")));
                    item.setGeometryGeoJson(Objects.toString(row.get("geometry_geojson"), ""));
                    return item;
                })
                .filter(it -> it.getReservoirId() != null && it.getFacilityBaseId() != null)
                .collect(Collectors.toList());
        resp.setTotalDamTopLength(totalDamTopLength);
        resp.setList(items);
        return resp;
    }

    /**
     * 公示牌总览（按行政区划筛选，包含子级行政区划匹配）。
     *
     * <p>展示口径：</p>
     * <ul>
     *     <li>河道总数/水库总数：按业务表关联行政区划统计（包含子级），并与首页“设施列表”口径保持一致</li>
     *     <li>公示牌列表：按 yz_signboard_bf.admin_region 匹配当前行政区及其子级，返回公示牌点位与关联设施名称</li>
     * </ul>
     */
    public BigScreenSignboardAreaOverviewRespVO getSignboardAreaOverviewByArea(Long areaId) {
        BigScreenSignboardAreaOverviewRespVO resp = new BigScreenSignboardAreaOverviewRespVO();
        if (areaId == null) {
            resp.setRiverCount(0L);
            resp.setReservoirCount(0L);
            resp.setTotalCount(0L);
            resp.setProblemTotalCount(0L);
            resp.setList(List.of());
            return resp;
        }

        String[] areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.length == 0) {
            resp.setRiverCount(0L);
            resp.setReservoirCount(0L);
            resp.setTotalCount(0L);
            resp.setProblemTotalCount(0L);
            resp.setList(List.of());
            return resp;
        }

        Map<String, Object> riverSummary = riverChannelMapper.selectSummaryByTowns(areaCodes);
        resp.setRiverCount(toLong(riverSummary == null ? null : riverSummary.get("total_count")));

        Map<String, Object> reservoirSummary = waterReservoirMapper.selectSummaryByTowns(areaCodes);
        resp.setReservoirCount(toLong(reservoirSummary == null ? null : reservoirSummary.get("total_count")));

        List<YzSignboardBfDO> signboards = signboardBfMapper.selectListByAreaCodes(areaCodes);
        if (signboards == null || signboards.isEmpty()) {
            resp.setTotalCount(0L);
            resp.setProblemTotalCount(0L);
            resp.setList(List.of());
            return resp;
        }

        Map<String, String> signboardTypeLabelMap = loadDictLabelMap(ZdConstants.ZD_GSPLX);
        Map<String, String> maintenanceUnitLabelMap = loadDictLabelMap(ZdConstants.ZD_WHDW);
        Map<Long, String> riverNameMap = loadRiverNameMap(signboards);
        Map<Long, String> riverSectionNameMap = loadRiverSectionNameMap(signboards);
        Map<Long, String> reservoirNameMap = loadReservoirNameMap(signboards);

        Map<String, Set<Long>> referenceIdsByType = new HashMap<>();
        for (YzSignboardBfDO signboard : signboards) {
            if (signboard == null) {
                continue;
            }
            ResolvedReference ref = resolveSignboardReference(signboard);
            if (ref == null || ref.referenceId == null || StrUtil.isBlank(ref.referenceType)) {
                continue;
            }
            referenceIdsByType
                    .computeIfAbsent(ref.referenceType, k -> new HashSet<>())
                    .add(ref.referenceId);
        }
        if (referenceIdsByType.isEmpty()) {
            resp.setProblemTotalCount(0L);
        } else {
            long totalCount = 0L;
            for (Map.Entry<String, Set<Long>> entry : referenceIdsByType.entrySet()) {
                String referenceType = entry.getKey();
                Set<Long> referenceIds = entry.getValue();
                if (StrUtil.isBlank(referenceType) || CollUtil.isEmpty(referenceIds)) {
                    continue;
                }
                Long count = problemFeedbackMapper.selectCount(new LambdaQueryWrapper<YzProblemFeedbackDO>()
                        .eq(YzProblemFeedbackDO::getReferenceType, referenceType)
                        .in(YzProblemFeedbackDO::getReferenceId, referenceIds));
                totalCount += count == null ? 0L : count;
            }
            resp.setProblemTotalCount(totalCount);
        }

        List<BigScreenSignboardAreaOverviewRespVO.Item> list = new ArrayList<>(signboards.size());
        for (YzSignboardBfDO sb : signboards) {
            if (sb == null || sb.getId() == null) {
                continue;
            }
            BigScreenSignboardAreaOverviewRespVO.Item item = new BigScreenSignboardAreaOverviewRespVO.Item();
            item.setSignboardId(sb.getId());
            item.setSignboardName(StrUtil.blankToDefault(sb.getSignboardName(), ""));
            String typeValue = StrUtil.trimToEmpty(sb.getSignboardType());
            item.setSignboardType(typeValue);
            item.setSignboardTypeLabel(StrUtil.blankToDefault(signboardTypeLabelMap.get(typeValue), typeValue));

            String refType = StrUtil.trimToEmpty(sb.getReferenceType());
            item.setReferenceType(refType);
            item.setReferenceTypeLabel(resolveReferenceTypeLabel(refType));
            item.setReferenceId(sb.getReferenceId());

            String refName = null;
            if (StrUtil.equalsIgnoreCase(refType, "river") && sb.getReferenceId() != null) {
                refName = riverNameMap.get(sb.getReferenceId());
            } else if (StrUtil.equalsIgnoreCase(refType, "river_section") && sb.getReferenceId() != null) {
                refName = riverSectionNameMap.get(sb.getReferenceId());
            } else if (StrUtil.equalsIgnoreCase(refType, "reservoir") && sb.getReferenceId() != null) {
                refName = reservoirNameMap.get(sb.getReferenceId());
            }
            item.setReferenceName(StrUtil.blankToDefault(refName, ""));

            item.setSpecificLocation(StrUtil.blankToDefault(sb.getSpecificLocation(), ""));
            item.setMaintenanceUnit(StrUtil.blankToDefault(joinDictLabels(sb.getMaintenanceUnit(), maintenanceUnitLabelMap), ""));
            item.setLongitude(sb.getLongitude());
            item.setLatitude(sb.getLatitude());
            list.add(item);
        }

        resp.setTotalCount((long) list.size());
        resp.setList(list);
        return resp;
    }

    /**
     * 泵站总览（按行政区划筛选，包含子级行政区划匹配）。
     */
    public BigScreenPumpStationAreaOverviewRespVO getPumpStationAreaOverviewByArea(Long areaId) {
        BigScreenPumpStationAreaOverviewRespVO resp = new BigScreenPumpStationAreaOverviewRespVO();
        if (areaId == null) {
            resp.setTotalCount(0L);
            resp.setTotalSelfFlow(BigDecimal.ZERO);
            resp.setTotalInstalledFlow(BigDecimal.ZERO);
            resp.setTotalPumpingFlow(BigDecimal.ZERO);
            resp.setPumpStationTypeCountMap(Collections.emptyMap());
            resp.setList(List.of());
            return resp;
        }

        String[] areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.length == 0) {
            resp.setTotalCount(0L);
            resp.setTotalSelfFlow(BigDecimal.ZERO);
            resp.setTotalInstalledFlow(BigDecimal.ZERO);
            resp.setTotalPumpingFlow(BigDecimal.ZERO);
            resp.setPumpStationTypeCountMap(Collections.emptyMap());
            resp.setList(List.of());
            return resp;
        }

        Map<String, Object> summary = pumpStationMapper.selectSummaryByDivisionCodes(areaCodes);
        long totalCount = toLong(summary == null ? null : summary.get("total_count"));
        resp.setTotalCount(totalCount);
        resp.setTotalSelfFlow(toBigDecimal(summary == null ? null : summary.get("total_self_flow")));
        resp.setTotalInstalledFlow(toBigDecimal(summary == null ? null : summary.get("total_installed_flow")));
        resp.setTotalPumpingFlow(toBigDecimal(summary == null ? null : summary.get("total_pumping_flow")));

        List<Map<String, Object>> typeRows = pumpStationMapper.selectPumpStationTypeCountByDivisionCodes(areaCodes);
        Map<String, Long> typeCountMap = new HashMap<>();
        if (typeRows != null) {
            for (Map<String, Object> row : typeRows) {
                if (row == null) {
                    continue;
                }
                String type = Objects.toString(row.get("pump_station_type"), "");
                if (StrUtil.isBlank(type)) {
                    continue;
                }
                typeCountMap.put(type, toLong(row.get("cnt")));
            }
        }
        resp.setPumpStationTypeCountMap(typeCountMap);

        if (totalCount <= 0) {
            resp.setList(List.of());
            return resp;
        }

        List<YzPumpStationDO> list = pumpStationMapper.selectPumpStationListByDivisionCodes(areaCodes);
        if (list == null || list.isEmpty()) {
            resp.setList(List.of());
            return resp;
        }

        List<BigScreenPumpStationAreaOverviewRespVO.Item> items = list.stream()
                .filter(it -> it != null && it.getId() != null && it.getFacilityId() != null)
                .map(it -> {
                    BigScreenPumpStationAreaOverviewRespVO.Item item = new BigScreenPumpStationAreaOverviewRespVO.Item();
                    item.setPumpStationId(it.getId());
                    item.setFacilityBaseId(it.getFacilityId());
                    item.setPumpStationName(StrUtil.blankToDefault(it.getPumpStationName(), ""));
                    item.setPumpStationType(StrUtil.blankToDefault(it.getPumpStationType(), ""));
                    item.setEngineeringGrade(StrUtil.blankToDefault(it.getEngineeringGrade(), ""));
                    item.setLongitude(it.getLongitude());
                    item.setLatitude(it.getLatitude());
                    return item;
                })
                .collect(Collectors.toList());
        resp.setList(items);
        return resp;
    }

    /**
     * 提防总览（按行政区划筛选，包含子级行政区划匹配）。
     */
    public BigScreenEmbankmentAreaOverviewRespVO getEmbankmentAreaOverviewByArea(Long areaId) {
        BigScreenEmbankmentAreaOverviewRespVO resp = new BigScreenEmbankmentAreaOverviewRespVO();
        if (areaId == null) {
            resp.setTotalCount(0L);
            resp.setTotalLengthM(BigDecimal.ZERO);
            resp.setTotalStandardLengthM(BigDecimal.ZERO);
            resp.setList(List.of());
            return resp;
        }

        String[] areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.length == 0) {
            resp.setTotalCount(0L);
            resp.setTotalLengthM(BigDecimal.ZERO);
            resp.setTotalStandardLengthM(BigDecimal.ZERO);
            resp.setList(List.of());
            return resp;
        }

        Map<String, Object> summary = embankmentMapper.selectSummaryByDivisionCodes(areaCodes);
        long totalCount = toLong(summary == null ? null : summary.get("total_count"));
        resp.setTotalCount(totalCount);
        resp.setTotalLengthM(toBigDecimal(summary == null ? null : summary.get("total_length_m")));
        resp.setTotalStandardLengthM(toBigDecimal(summary == null ? null : summary.get("total_standard_length_m")));
        if (totalCount <= 0) {
            resp.setList(List.of());
            return resp;
        }

        List<YzEmbankmentDO> list = embankmentMapper.selectEmbankmentListByDivisionCodes(areaCodes);
        if (list == null || list.isEmpty()) {
            resp.setList(List.of());
            return resp;
        }

        List<BigScreenEmbankmentAreaOverviewRespVO.Item> items = list.stream()
                .filter(it -> it != null && it.getId() != null && it.getFacilityId() != null)
                .map(it -> {
                    BigScreenEmbankmentAreaOverviewRespVO.Item item = new BigScreenEmbankmentAreaOverviewRespVO.Item();
                    item.setEmbankmentId(it.getId());
                    item.setFacilityBaseId(it.getFacilityId());
                    item.setEmbankmentName(StrUtil.blankToDefault(it.getEmbankmentName(), ""));
                    item.setEmbankmentForm(StrUtil.blankToDefault(it.getEmbankmentForm(), ""));
                    item.setLongitude(it.getLongitude());
                    item.setLatitude(it.getLatitude());
                    return item;
                })
                .collect(Collectors.toList());
        resp.setList(items);
        return resp;
    }

    /**
     * 防汛物资仓库总览（按行政区划筛选，包含子级行政区划匹配）。
     */
    public BigScreenFloodMaterialWarehouseAreaOverviewRespVO getFloodMaterialWarehouseAreaOverviewByArea(Long areaId) {
        BigScreenFloodMaterialWarehouseAreaOverviewRespVO resp = new BigScreenFloodMaterialWarehouseAreaOverviewRespVO();
        if (areaId == null) {
            resp.setTotalCount(0L);
            resp.setMaterialTypes("");
            resp.setList(List.of());
            return resp;
        }

        String[] areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.length == 0) {
            resp.setTotalCount(0L);
            resp.setMaterialTypes("");
            resp.setList(List.of());
            return resp;
        }

        Map<String, Object> summary = floodWarehouseMapper.selectSummaryByDivisionCodes(areaCodes);
        long totalCount = toLong(summary == null ? null : summary.get("total_count"));
        resp.setTotalCount(totalCount);
        if (totalCount <= 0) {
            resp.setMaterialTypes("");
            resp.setList(List.of());
            return resp;
        }

        List<YzFloodPreventionMaterialWarehouseDO> list = floodWarehouseMapper.selectWarehouseListByDivisionCodes(areaCodes);
        if (list == null || list.isEmpty()) {
            resp.setMaterialTypes("");
            resp.setList(List.of());
            return resp;
        }

        Set<String> materialTypes = new LinkedHashSet<>();
        for (YzFloodPreventionMaterialWarehouseDO item : list) {
            if (item == null) {
                continue;
            }
            String raw = StrUtil.trimToEmpty(item.getMaterialType());
            if (StrUtil.isBlank(raw)) {
                continue;
            }
            String[] parts = raw.split("[,，、;；/\\s]+");
            for (String part : parts) {
                String label = StrUtil.trimToEmpty(part);
                if (StrUtil.isNotBlank(label)) {
                    materialTypes.add(label);
                }
            }
        }
        resp.setMaterialTypes(materialTypes.isEmpty() ? "" : String.join("、", materialTypes));

        List<BigScreenFloodMaterialWarehouseAreaOverviewRespVO.Item> items = list.stream()
                .filter(it -> it != null && it.getId() != null)
                .map(it -> {
                    BigScreenFloodMaterialWarehouseAreaOverviewRespVO.Item item = new BigScreenFloodMaterialWarehouseAreaOverviewRespVO.Item();
                    item.setWarehouseId(it.getId());
                    item.setWarehouseName(StrUtil.blankToDefault(it.getWarehouseName(), ""));
                    item.setBelongUnit(StrUtil.blankToDefault(it.getBelongUnit(), ""));
                    item.setLongitude(it.getLongitude());
                    item.setLatitude(it.getLatitude());
                    return item;
                })
                .collect(Collectors.toList());
        resp.setList(items);
        return resp;
    }

    /**
     * 灌区总览（按行政区划筛选，包含子级行政区划匹配）。
     */
    public BigScreenIrrigationDistrictAreaOverviewRespVO getIrrigationDistrictAreaOverviewByArea(Long areaId) {
        BigScreenIrrigationDistrictAreaOverviewRespVO resp = new BigScreenIrrigationDistrictAreaOverviewRespVO();
        if (areaId == null) {
            resp.setTotalCount(0L);
            resp.setTotalActualIrrigableArea(BigDecimal.ZERO);
            resp.setTotalBasicFarmlandAreaKm2(BigDecimal.ZERO);
            resp.setTotalMainCanalLengthM(BigDecimal.ZERO);
            resp.setList(List.of());
            return resp;
        }

        String[] areaCodes = buildAreaCodeScope(areaId);
        if (areaCodes.length == 0) {
            resp.setTotalCount(0L);
            resp.setTotalActualIrrigableArea(BigDecimal.ZERO);
            resp.setTotalBasicFarmlandAreaKm2(BigDecimal.ZERO);
            resp.setTotalMainCanalLengthM(BigDecimal.ZERO);
            resp.setList(List.of());
            return resp;
        }

        Map<String, Object> summary = irrigationDistrictMapper.selectSummaryByDivisionCodes(areaCodes);
        long totalCount = toLong(summary == null ? null : summary.get("total_count"));
        resp.setTotalCount(totalCount);
        resp.setTotalActualIrrigableArea(toBigDecimal(summary == null ? null : summary.get("total_actual_irrigable_area")));
        resp.setTotalBasicFarmlandAreaKm2(toBigDecimal(summary == null ? null : summary.get("total_basic_farmland_area_km2")));
        resp.setTotalMainCanalLengthM(toBigDecimal(summary == null ? null : summary.get("total_main_canal_length_m")));
        if (totalCount <= 0) {
            resp.setList(List.of());
            return resp;
        }

        List<Map<String, Object>> list = irrigationDistrictMapper.selectIrrigationDistrictListByDivisionCodes(areaCodes);
        if (list == null || list.isEmpty()) {
            resp.setList(List.of());
            return resp;
        }

        List<BigScreenIrrigationDistrictAreaOverviewRespVO.Item> items = list.stream()
                .filter(Objects::nonNull)
                .map(row -> {
                    BigScreenIrrigationDistrictAreaOverviewRespVO.Item item = new BigScreenIrrigationDistrictAreaOverviewRespVO.Item();
                    item.setIrrigationDistrictId(toLongNullable(row.get("irrigation_district_id")));
                    item.setFacilityBaseId(toLongNullable(row.get("facility_base_id")));
                    item.setIrrigationDistrictName(Objects.toString(row.get("irrigation_district_name"), ""));
                    item.setActualIrrigableArea(toBigDecimal(row.get("actual_irrigable_area")));
                    item.setBasicFarmlandAreaKm2(toBigDecimal(row.get("basic_farmland_area_km2")));
                    item.setGeometryGeoJson(Objects.toString(row.get("geometry_geojson"), ""));
                    return item;
                })
                .filter(it -> it.getIrrigationDistrictId() != null && it.getFacilityBaseId() != null)
                .collect(Collectors.toList());
        resp.setList(items);
        return resp;
    }

    private Map<Long, String> loadRiverNameMap(List<YzSignboardBfDO> signboards) {
        Set<Long> ids = signboards.stream()
                .filter(it -> it != null && it.getReferenceId() != null && StrUtil.equalsIgnoreCase(it.getReferenceType(), "river"))
                .map(YzSignboardBfDO::getReferenceId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<YzRiverChannelDO> list = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName)
                .in(YzRiverChannelDO::getId, ids));
        Map<Long, String> map = new HashMap<>();
        for (YzRiverChannelDO item : list) {
            if (item != null && item.getId() != null) {
                map.put(item.getId(), StrUtil.blankToDefault(item.getRiverName(), ""));
            }
        }
        return map;
    }

    private Map<Long, String> loadRiverSectionNameMap(List<YzSignboardBfDO> signboards) {
        Set<Long> ids = signboards.stream()
                .filter(it -> it != null && it.getReferenceId() != null && StrUtil.equalsIgnoreCase(it.getReferenceType(), "river_section"))
                .map(YzSignboardBfDO::getReferenceId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<YzRiverSectionDO> list = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                .select(YzRiverSectionDO::getId, YzRiverSectionDO::getSectionName)
                .in(YzRiverSectionDO::getId, ids));
        Map<Long, String> map = new HashMap<>();
        for (YzRiverSectionDO item : list) {
            if (item != null && item.getId() != null) {
                map.put(item.getId(), StrUtil.blankToDefault(item.getSectionName(), ""));
            }
        }
        return map;
    }

    private Map<Long, String> loadReservoirNameMap(List<YzSignboardBfDO> signboards) {
        Set<Long> ids = signboards.stream()
                .filter(it -> it != null && it.getReferenceId() != null && StrUtil.equalsIgnoreCase(it.getReferenceType(), "reservoir"))
                .map(YzSignboardBfDO::getReferenceId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<YzWaterReservoirDO> list = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                .select(YzWaterReservoirDO::getId, YzWaterReservoirDO::getReservoirName)
                .in(YzWaterReservoirDO::getId, ids));
        Map<Long, String> map = new HashMap<>();
        for (YzWaterReservoirDO item : list) {
            if (item != null && item.getId() != null) {
                map.put(item.getId(), StrUtil.blankToDefault(item.getReservoirName(), ""));
            }
        }
        return map;
    }

    private String resolveReferenceTypeLabel(String referenceType) {
        String type = StrUtil.trimToEmpty(referenceType);
        if (StrUtil.equalsIgnoreCase(type, "river")) {
            return "河道";
        }
        if (StrUtil.equalsIgnoreCase(type, "river_section")) {
            return "河段";
        }
        if (StrUtil.equalsIgnoreCase(type, "reservoir")) {
            return "水库";
        }
        return "";
    }

    private String joinDictLabels(String[] values, Map<String, String> labelMap) {
        if (values == null || values.length == 0) {
            return null;
        }
        List<String> labels = List.of(values).stream()
                .filter(StrUtil::isNotBlank)
                .map(value -> StrUtil.blankToDefault(labelMap.get(value), value))
                .distinct()
                .collect(Collectors.toList());
        return labels.isEmpty() ? null : String.join("、", labels);
    }

    private String[] buildAreaCodeScope(Long areaId) {
        List<SystemAreaNode> children = systemAreaService.getAreaTreeChildren(areaId);
        List<String> result = new ArrayList<>();
        result.add(String.valueOf(areaId));
        collectAreaIds(children, result);
        return result.stream().filter(StrUtil::isNotBlank).distinct().toArray(String[]::new);
    }

    private void collectAreaIds(List<SystemAreaNode> nodes, List<String> result) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (SystemAreaNode node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            result.add(String.valueOf(node.getId()));
            collectAreaIds(node.getChildren(), result);
        }
    }

    /**
     * 为设施几何列表填充“是否存在待办问题”标识。
     *
     * <p>待办口径：问题反馈表 status != 4（已办结）。</p>
     * <p>关联链路：设施(facilityType, deviceId) -> 问题反馈(referenceType, referenceId)。</p>
     */
    private void fillTodoProblemFlag(List<BigScreenFacilityGeomRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<String, List<Long>> deviceIdsByType = new HashMap<>();
        for (BigScreenFacilityGeomRespVO item : list) {
            if (item == null || item.getDeviceId() == null || StrUtil.isBlank(item.getFacilityType())) {
                continue;
            }
            String type = StrUtil.trim(item.getFacilityType()).toLowerCase();
            deviceIdsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(item.getDeviceId());
        }
        if (deviceIdsByType.isEmpty()) {
            for (BigScreenFacilityGeomRespVO item : list) {
                if (item != null) {
                    item.setHasTodoProblem(false);
                }
            }
            return;
        }

        Map<String, Boolean> keyHasTodoMap = new HashMap<>();
        for (BigScreenFacilityGeomRespVO item : list) {
            if (item == null || item.getDeviceId() == null || StrUtil.isBlank(item.getFacilityType())) {
                continue;
            }
            String type = StrUtil.trim(item.getFacilityType()).toLowerCase();
            keyHasTodoMap.put(buildFacilityKey(type, item.getDeviceId()), false);
        }

        for (Map.Entry<String, List<Long>> entry : deviceIdsByType.entrySet()) {
            String referenceType = entry.getKey();
            List<Long> ids = entry.getValue();
            if (StrUtil.isBlank(referenceType) || CollUtil.isEmpty(ids)) {
                continue;
            }
            List<YzProblemFeedbackDO> pendingList = problemFeedbackMapper.selectList(new LambdaQueryWrapper<YzProblemFeedbackDO>()
                    .select(YzProblemFeedbackDO::getReferenceType, YzProblemFeedbackDO::getReferenceId)
                    .eq(YzProblemFeedbackDO::getReferenceType, referenceType)
                    .in(YzProblemFeedbackDO::getReferenceId, ids)
                    .ne(YzProblemFeedbackDO::getStatus, STATUS_FINISHED));
            if (CollUtil.isEmpty(pendingList)) {
                continue;
            }
            for (YzProblemFeedbackDO feedback : pendingList) {
                if (feedback == null || feedback.getReferenceId() == null) {
                    continue;
                }
                String key = buildFacilityKey(referenceType, feedback.getReferenceId());
                keyHasTodoMap.put(key, true);
            }
        }

        for (BigScreenFacilityGeomRespVO item : list) {
            if (item == null || item.getDeviceId() == null || StrUtil.isBlank(item.getFacilityType())) {
                continue;
            }
            String type = StrUtil.trim(item.getFacilityType()).toLowerCase();
            item.setHasTodoProblem(Boolean.TRUE.equals(keyHasTodoMap.get(buildFacilityKey(type, item.getDeviceId()))));
        }
    }

    private String buildFacilityKey(String facilityType, Long deviceId) {
        return StrUtil.trimToEmpty(facilityType) + ":" + (deviceId == null ? "" : deviceId);
    }

    /**
     * 根据河道名称模糊查询河道列表，默认查询全部，按创建时间倒序。
     * <p>
     * 返回字段：河道 id、facilityId、riverName、riverLevel（按 zd_hljb 转中文标签）。
     */
    public List<BigScreenRiverChannelSearchRespVO> searchRiverChannels(String riverName) {
        String keyword = StrUtil.trimToNull(riverName);
        List<YzRiverChannelDO> list = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getFacilityId, YzRiverChannelDO::getRiverName, YzRiverChannelDO::getRiverLevel, YzRiverChannelDO::getCreateTime)
                .like(keyword != null, YzRiverChannelDO::getRiverName, keyword)
                .orderByDesc(YzRiverChannelDO::getCreateTime));
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Map<String, String> levelLabelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        return list.stream()
                .filter(it -> it != null && it.getId() != null)
                .map(it -> {
                    BigScreenRiverChannelSearchRespVO vo = new BigScreenRiverChannelSearchRespVO();
                    vo.setId(it.getId());
                    vo.setFacilityId(it.getFacilityId());
                    vo.setRiverName(StrUtil.blankToDefault(it.getRiverName(), ""));
                    String levelValue = StrUtil.trimToEmpty(it.getRiverLevel());
                    vo.setRiverLevel(StrUtil.blankToDefault(levelLabelMap.get(levelValue), levelValue));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 按公示牌等级统计公示牌数量。
     */
    public List<BigScreenDictCountItemVO> getSignboardCountByRiverLevel() {
        List<Map<String, Object>> rows = signboardBfMapper.selectSignboardLevelCount();
        Map<String, Long> countMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                if (row == null || row.isEmpty()) {
                    continue;
                }
                String level = Objects.toString(row.get("signboard_level"), "");
                if (StrUtil.isBlank(level)) {
                    continue;
                }
                countMap.put(level, toLong(row.get("cnt")));
            }
        }

        Map<String, String> labelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        for (String level : labelMap.keySet()) {
            if (StrUtil.isBlank(level)) {
                continue;
            }
            countMap.putIfAbsent(level, 0L);
        }
        return buildDictCountList(countMap, labelMap);
    }

    /**
     * 统计各级河道数量（按河道等级分组）
     */
    public List<BigScreenDictCountItemVO> getRiverChannelCountByRiverLevel() {
        List<Map<String, Object>> rows = riverChannelMapper.selectAllRiverLevelCount();
        Map<String, Long> countMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                if (row == null || row.isEmpty()) {
                    continue;
                }
                String level = Objects.toString(row.get("river_level"), "");
                if (StrUtil.isBlank(level)) {
                    continue;
                }
                countMap.put(level, toLong(row.get("cnt")));
            }
        }

        Map<String, String> labelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        for (String level : labelMap.keySet()) {
            if (StrUtil.isBlank(level)) {
                continue;
            }
            countMap.putIfAbsent(level, 0L);
        }
        return buildDictCountList(countMap, labelMap);
    }

    /**
     * 公示牌位置列表（可按河道级别筛选）。
     */
    public BigScreenSignboardLocationRespVO getSignboardLocations(String riverLevel) {
        String levelValue = StrUtil.trimToNull(riverLevel);
        List<YzSignboardBfDO> signboards = signboardBfMapper.selectList(new LambdaQueryWrapper<YzSignboardBfDO>()
                .select(YzSignboardBfDO::getId,
                        YzSignboardBfDO::getSignboardName,
                        YzSignboardBfDO::getLongitude,
                        YzSignboardBfDO::getLatitude,
                        YzSignboardBfDO::getReferenceType,
                        YzSignboardBfDO::getReferenceId,
                        YzSignboardBfDO::getQrCode,
                        YzSignboardBfDO::getRiverChannelId,
                        YzSignboardBfDO::getRiverSectionId,
                        YzSignboardBfDO::getWaterReservoirId)
                // 以 reference_id 非空作为“有效公示牌”口径，避免因经纬度为空导致统计不一致
                .isNotNull(YzSignboardBfDO::getReferenceId)
                .orderByDesc(YzSignboardBfDO::getCreateTime));
        if (signboards == null || signboards.isEmpty()) {
            BigScreenSignboardLocationRespVO empty = new BigScreenSignboardLocationRespVO();
            empty.setTotalCount(0L);
            empty.setList(List.of());
            return empty;
        }

        Map<Long, Long> unfinishedProblemCountMap = loadUnfinishedProblemCountBySignboardIds(signboards);
        List<ResolvedReference> references = signboards.stream()
                .map(this::resolveSignboardReference)
                .collect(Collectors.toList());

        // 未传河道级别：直接返回全部
        if (levelValue == null) {
            List<BigScreenSignboardLocationItemRespVO> list = new ArrayList<>(signboards.size());
            for (int i = 0; i < signboards.size(); i++) {
                YzSignboardBfDO sb = signboards.get(i);
                ResolvedReference ref = references.get(i);
                BigScreenSignboardLocationItemRespVO item = buildSignboardLocationItem(sb, ref);
                item.setUnfinishedProblemCount(unfinishedProblemCountMap.getOrDefault(sb.getId(), 0L));
                list.add(item);
            }
            BigScreenSignboardLocationRespVO resp = new BigScreenSignboardLocationRespVO();
            resp.setList(list);
            resp.setTotalCount((long) list.size());
            return resp;
        }

        // 传了河道级别：仅返回能匹配到河道级别的河道/河段关联公示牌
        Set<Long> riverIds = references.stream()
                .filter(it -> it != null && StrUtil.equalsIgnoreCase(it.referenceType, "river") && it.referenceId != null)
                .map(it -> it.referenceId)
                .collect(Collectors.toSet());
        Set<Long> sectionIds = references.stream()
                .filter(it -> it != null && StrUtil.equalsIgnoreCase(it.referenceType, "river_section") && it.referenceId != null)
                .map(it -> it.referenceId)
                .collect(Collectors.toSet());

        Set<Long> matchedRiverIds = resolveMatchedRiverIds(riverIds, levelValue);
        Set<Long> matchedSectionIds = resolveMatchedSectionIds(sectionIds, levelValue);

        List<BigScreenSignboardLocationItemRespVO> list = new ArrayList<>();
        for (int i = 0; i < signboards.size(); i++) {
            YzSignboardBfDO sb = signboards.get(i);
            ResolvedReference ref = references.get(i);
            if (ref == null || ref.referenceId == null) {
                continue;
            }
            if (StrUtil.equalsIgnoreCase(ref.referenceType, "river") && matchedRiverIds.contains(ref.referenceId)) {
                BigScreenSignboardLocationItemRespVO item = buildSignboardLocationItem(sb, ref);
                item.setUnfinishedProblemCount(unfinishedProblemCountMap.getOrDefault(sb.getId(), 0L));
                list.add(item);
                continue;
            }
            if (StrUtil.equalsIgnoreCase(ref.referenceType, "river_section") && matchedSectionIds.contains(ref.referenceId)) {
                BigScreenSignboardLocationItemRespVO item = buildSignboardLocationItem(sb, ref);
                item.setUnfinishedProblemCount(unfinishedProblemCountMap.getOrDefault(sb.getId(), 0L));
                list.add(item);
            }
        }
        BigScreenSignboardLocationRespVO resp = new BigScreenSignboardLocationRespVO();
        resp.setList(list);
        resp.setTotalCount((long) list.size());
        return resp;
    }

    /**
     * 根据公示牌关联对象（referenceType/referenceId）查询对应设施详情（名称、几何WKT、ID）。
     */
    public BigScreenSignboardReferenceDetailRespVO getSignboardReferenceDetail(String referenceType, Long referenceId) {
        String type = StrUtil.trimToNull(referenceType);
        if (type == null || referenceId == null) {
            return null;
        }

        BigScreenSignboardReferenceDetailRespVO resp = new BigScreenSignboardReferenceDetailRespVO();
        resp.setReferenceType(type);
        resp.setReferenceId(referenceId);

        if (StrUtil.equalsIgnoreCase(type, "river")) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(referenceId);
            if (channel == null) {
                return resp;
            }
            resp.setName(StrUtil.blankToDefault(channel.getRiverName(), ""));
            resp.setGeomWkt(getGeomWktByFacilityId(channel.getFacilityId()));
            return resp;
        }
        if (StrUtil.equalsIgnoreCase(type, "river_section")) {
            YzRiverSectionDO section = riverSectionMapper.selectById(referenceId);
            if (section == null) {
                return resp;
            }
            resp.setName(StrUtil.blankToDefault(section.getSectionName(), ""));
            resp.setGeomWkt(getGeomWktByFacilityId(section.getFacilityId()));
            return resp;
        }
        if (StrUtil.equalsIgnoreCase(type, "reservoir")) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(referenceId);
            if (reservoir == null) {
                return resp;
            }
            resp.setName(StrUtil.blankToDefault(reservoir.getReservoirName(), ""));
            resp.setGeomWkt(getGeomWktByFacilityId(reservoir.getFacilityId()));
            return resp;
        }
        return resp;
    }

    /**
     * 查询公示牌扫码反馈问题（未办结），支持按“天”范围筛选。
     * <p>
     * 权限规则：
     * <ul>
     *     <li>管理员：可查看全部</li>
     *     <li>普通用户（非 guest）：仅查看指派给自己的问题</li>
     *     <li>纯游客：不返回数据</li>
     * </ul>
     */
    public BigScreenSignboardProblemListRespVO getSignboardProblems(Long signboardId, String createTime) {
        BigScreenSignboardProblemListRespVO resp = new BigScreenSignboardProblemListRespVO();
        resp.setTotalCount(0L);
        resp.setList(List.of());
        if (signboardId == null) {
            return resp;
        }

        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (isGuest(loginUser)) {
            return resp;
        }

        YzSignboardBfDO signboard = signboardBfMapper.selectById(signboardId);
        if (signboard == null) {
            return resp;
        }
        ResolvedReference ref = resolveSignboardReference(signboard);
        if (ref == null || ref.referenceId == null || StrUtil.isBlank(ref.referenceType)) {
            return resp;
        }
        String facilityName = resolveFacilityNameBySignboardId(signboardId);
        Map<String, String> feedbackTypeLabelMap = loadDictLabelMap(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabelMap(ZdConstants.ZD_WTJD);

        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .select(YzProblemFeedbackDO::getId,
                        YzProblemFeedbackDO::getFeedbackType,
                        YzProblemFeedbackDO::getStatus,
                        YzProblemFeedbackDO::getCreateTime)
                .eq(YzProblemFeedbackDO::getReferenceType, ref.referenceType)
                .eq(YzProblemFeedbackDO::getReferenceId, ref.referenceId)
                .ne(YzProblemFeedbackDO::getStatus, STATUS_FINISHED)
                .orderByDesc(YzProblemFeedbackDO::getCreateTime);
        applyCreateDateRange(wrapper, normalizeCreateTimeParam(createTime));

        if (!isAdmin(loginUser)) {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            if (userId == null) {
                return resp;
            }
            wrapper.inSql(YzProblemFeedbackDO::getId,
                    "select distinct problem_feedback from yz_problem_status_task where assigned_person_id = " + userId);
        }

        List<YzProblemFeedbackDO> list = problemFeedbackMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return resp;
        }

        List<BigScreenSignboardProblemItemRespVO> result = new ArrayList<>(list.size());
        for (YzProblemFeedbackDO item : list) {
            if (item == null || item.getId() == null) {
                continue;
            }
            BigScreenSignboardProblemItemRespVO vo = new BigScreenSignboardProblemItemRespVO();
            vo.setId(item.getId());
            vo.setFacilityName(StrUtil.blankToDefault(facilityName, ""));
            vo.setFeedbackTime(item.getCreateTime());

            String typeValue = StrUtil.trimToEmpty(item.getFeedbackType());
            vo.setFeedbackTypeLabel(StrUtil.blankToDefault(feedbackTypeLabelMap.get(typeValue), typeValue));

            Integer status = item.getStatus();
            String statusValue = status == null ? "" : String.valueOf(status);
            vo.setStatusLabel(StrUtil.blankToDefault(progressLabelMap.get(statusValue), fallbackStatusLabel(status)));
            result.add(vo);
        }

        resp.setList(result);
        resp.setTotalCount((long) result.size());
        return resp;
    }

    private String[] normalizeCreateTimeParam(String createTime) {
        if (StrUtil.isBlank(createTime)) {
            return null;
        }
        return new String[]{createTime};
    }

    /**
     * 查询河道列表（按河道级别正序），支持按河道名称/河段名称模糊筛选，以及生态类型筛选（zd_stlx 的 value）。
     * <p>
     * 说明：
     * <ul>
     *     <li>河段名称筛选：先按河段名称查询匹配河段，再回溯其 riverChannelId 作为河道筛选范围</li>
     *     <li>返回河段名称：若河道下存在河段，则返回该河道下所有河段名称</li>
     * </ul>
     */
    public BigScreenRiverChannelWithSectionsListRespVO getRiverChannelsWithSections(String riverName, String sectionName, String ecologyType) {
        BigScreenRiverChannelListReqVO reqVO = new BigScreenRiverChannelListReqVO();
        reqVO.setRiverName(riverName);
        reqVO.setSectionName(sectionName);
        reqVO.setEcologyType(ecologyType);
        return getRiverChannelsWithSections(reqVO);
    }

    /**
     * 查询河道列表（按河道级别正序，不分页）。
     *
     * <p>公示牌关联口径：</p>
     * <ul>
     *     <li>referenceType=river：使用公示牌 riverChannelId（兼容 referenceId）关联河道主键 id</li>
     *     <li>referenceType=river_section：使用公示牌 riverChannelId（兼容 referenceId）关联河段主键 id，再回溯到河道</li>
     * </ul>
     */
    public BigScreenRiverChannelWithSectionsListRespVO getRiverChannelsWithSections(BigScreenRiverChannelListReqVO reqVO) {
        String riverKeyword = reqVO == null ? null : StrUtil.trimToNull(reqVO.getRiverName());
        String sectionKeyword = reqVO == null ? null : StrUtil.trimToNull(reqVO.getSectionName());
        String ecologyValue = reqVO == null ? null : StrUtil.trimToNull(reqVO.getEcologyType());

        Set<Long> channelIdScope = null;
        if (sectionKeyword != null) {
            List<YzRiverSectionDO> matchedSections = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                    .select(YzRiverSectionDO::getRiverChannelId)
                    .like(YzRiverSectionDO::getSectionName, sectionKeyword));
            Set<Long> ids = matchedSections.stream()
                    .map(YzRiverSectionDO::getRiverChannelId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (ids.isEmpty()) {
                BigScreenRiverChannelWithSectionsListRespVO empty = new BigScreenRiverChannelWithSectionsListRespVO();
                empty.setTotalCount(0L);
                empty.setList(List.of());
                return empty;
            }
            channelIdScope = ids;
        }

        LambdaQueryWrapper<YzRiverChannelDO> wrapper = new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName, YzRiverChannelDO::getRiverLevel, YzRiverChannelDO::getLengthKm, YzRiverChannelDO::getCreateTime)
                .like(riverKeyword != null, YzRiverChannelDO::getRiverName, riverKeyword)
                .eq(ecologyValue != null, YzRiverChannelDO::getEcologyType, ecologyValue)
                .in(channelIdScope != null, YzRiverChannelDO::getId, channelIdScope);

        List<YzRiverChannelDO> channels = riverChannelMapper.selectList(wrapper);
        if (channels == null || channels.isEmpty()) {
            BigScreenRiverChannelWithSectionsListRespVO empty = new BigScreenRiverChannelWithSectionsListRespVO();
            empty.setTotalCount(0L);
            empty.setList(List.of());
            return empty;
        }
        // 按河道级别前缀数字正序，缺失级别的放在末尾
        channels.sort((a, b) -> {
            int levelCompare = Integer.compare(parseRiverLevelOrder(a == null ? null : a.getRiverLevel()),
                    parseRiverLevelOrder(b == null ? null : b.getRiverLevel()));
            if (levelCompare != 0) {
                return levelCompare;
            }
            LocalDateTime aTime = a == null ? null : a.getCreateTime();
            LocalDateTime bTime = b == null ? null : b.getCreateTime();
            if (aTime == null && bTime == null) {
                return 0;
            }
            if (aTime == null) {
                return 1;
            }
            if (bTime == null) {
                return -1;
            }
            return bTime.compareTo(aTime);
        });

        List<Long> channelIds = channels.stream()
                .map(YzRiverChannelDO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 河段名称与河段ID（用于公示牌回溯）
        Map<Long, List<String>> sectionNamesByChannelId = new HashMap<>();
        Map<Long, List<Long>> sectionIdsByChannelId = new HashMap<>();
        Map<Long, String> sectionNameById = new HashMap<>();
        List<Long> allSectionIds = new ArrayList<>();
        List<YzRiverSectionDO> sections = loadSectionsByChannelIds(channelIds);
        if (sections != null) {
            for (YzRiverSectionDO section : sections) {
                if (section == null || section.getRiverChannelId() == null || section.getId() == null) {
                    continue;
                }
                Long chId = section.getRiverChannelId();
                allSectionIds.add(section.getId());
                sectionIdsByChannelId.computeIfAbsent(chId, k -> new ArrayList<>()).add(section.getId());
                String name = StrUtil.trimToNull(section.getSectionName());
                if (name != null) {
                    sectionNameById.put(section.getId(), name);
                    sectionNamesByChannelId.computeIfAbsent(chId, k -> new ArrayList<>()).add(name);
                }
            }
        }

        // 字典：河道级别（zd_hljb）
        Map<String, String> riverLevelLabelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);

        // 公示牌坐标：返回全部河道直属公示牌及河段公示牌
        Map<Long, List<YzSignboardBfDO>> riverSignboardsByBizId = loadSignboardsGroupedByBizId(ReferenceTypeConstants.RIVER, channelIds);
        Map<Long, List<YzSignboardBfDO>> sectionSignboardsByBizId = loadSignboardsGroupedByBizId(ReferenceTypeConstants.RIVER_SECTION, allSectionIds);

        List<BigScreenRiverChannelWithSectionsRespVO> list = channels.stream().map(ch -> {
            BigScreenRiverChannelWithSectionsRespVO vo = new BigScreenRiverChannelWithSectionsRespVO();
            vo.setId(ch.getId());
            String channelName = StrUtil.blankToDefault(ch.getRiverName(), "");
            vo.setRiverName(channelName);
            String levelValue = StrUtil.trimToEmpty(ch.getRiverLevel());
            vo.setRiverLevel(levelValue);
            vo.setRiverLevelLabel(StrUtil.blankToDefault(riverLevelLabelMap.get(levelValue), levelValue));
            vo.setLengthKm(ch.getLengthKm());
            vo.setSectionNames(sectionNamesByChannelId.getOrDefault(ch.getId(), List.of()));
            vo.setSignboardLocations(buildSignboardLocations(channelName,
                    ch.getId(),
                    sectionIdsByChannelId.getOrDefault(ch.getId(), List.of()),
                    sectionNameById,
                    riverSignboardsByBizId,
                    sectionSignboardsByBizId));
            return vo;
        }).collect(Collectors.toList());

        BigScreenRiverChannelWithSectionsListRespVO resp = new BigScreenRiverChannelWithSectionsListRespVO();
        resp.setTotalCount((long) list.size());
        resp.setList(list);
        return resp;
    }

    private List<YzRiverSectionDO> loadSectionsByChannelIds(List<Long> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            return List.of();
        }
        return riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                .select(YzRiverSectionDO::getId, YzRiverSectionDO::getRiverChannelId, YzRiverSectionDO::getSectionName, YzRiverSectionDO::getCreateTime)
                .in(YzRiverSectionDO::getRiverChannelId, channelIds)
                .orderByAsc(YzRiverSectionDO::getCreateTime));
    }

    private int parseRiverLevelOrder(String riverLevel) {
        if (StrUtil.isBlank(riverLevel)) {
            return Integer.MAX_VALUE;
        }
        String value = StrUtil.trim(riverLevel);
        int length = value.length();
        int index = 0;
        while (index < length && Character.isDigit(value.charAt(index))) {
            index++;
        }
        if (index == 0) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(value.substring(0, index));
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private Map<Long, List<YzSignboardBfDO>> loadSignboardsGroupedByBizId(String referenceType, List<Long> bizIds) {
        if (StrUtil.isBlank(referenceType) || bizIds == null || bizIds.isEmpty()) {
            return Map.of();
        }
        List<YzSignboardBfDO> signboards = signboardBfMapper.selectList(new LambdaQueryWrapper<YzSignboardBfDO>()
                .select(YzSignboardBfDO::getId, YzSignboardBfDO::getReferenceType, YzSignboardBfDO::getRiverChannelId,
                        YzSignboardBfDO::getRiverChannelName, YzSignboardBfDO::getReferenceId, YzSignboardBfDO::getRiverSectionId,
                        YzSignboardBfDO::getRiverSectionName, YzSignboardBfDO::getLongitude, YzSignboardBfDO::getLatitude,
                        YzSignboardBfDO::getCreateTime)
                .eq(YzSignboardBfDO::getReferenceType, referenceType)
                .and(w -> w.in(YzSignboardBfDO::getRiverChannelId, bizIds)
                        .or().in(YzSignboardBfDO::getReferenceId, bizIds)
                        .or().in(YzSignboardBfDO::getRiverSectionId, bizIds))
                .orderByDesc(YzSignboardBfDO::getCreateTime));
        if (signboards == null || signboards.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<YzSignboardBfDO>> map = new HashMap<>();
        for (YzSignboardBfDO sb : signboards) {
            if (!hasValidLongitudeLatitude(sb)) {
                continue;
            }
            Long bizId = resolveSignboardBizId(referenceType, sb);
            if (bizId == null) {
                continue;
            }
            map.computeIfAbsent(bizId, k -> new ArrayList<>()).add(sb);
        }
        return map;
    }

    private Long resolveSignboardBizId(String referenceType, YzSignboardBfDO signboard) {
        if (signboard == null) {
            return null;
        }
        if (StrUtil.equals(referenceType, ReferenceTypeConstants.RIVER_SECTION)) {
            if (signboard.getRiverSectionId() != null) {
                return signboard.getRiverSectionId();
            }
            if (signboard.getReferenceId() != null) {
                return signboard.getReferenceId();
            }
            return signboard.getRiverChannelId();
        }
        if (signboard.getRiverChannelId() != null) {
            return signboard.getRiverChannelId();
        }
        return signboard.getReferenceId();
    }

    private List<BigScreenRiverChannelWithSectionsRespVO.SignboardLocationItem> buildSignboardLocations(
            String channelName,
            Long channelId,
            List<Long> sectionIds,
            Map<Long, String> sectionNameById,
            Map<Long, List<YzSignboardBfDO>> riverSignboardsByBizId,
            Map<Long, List<YzSignboardBfDO>> sectionSignboardsByBizId) {
        List<BigScreenRiverChannelWithSectionsRespVO.SignboardLocationItem> locations = new ArrayList<>();

        appendSignboardLocations(locations,
                riverSignboardsByBizId.get(channelId),
                ReferenceTypeConstants.RIVER,
                channelId,
                channelName);

        for (Long sectionId : sectionIds) {
            String sectionName = StrUtil.blankToDefault(sectionNameById.get(sectionId), "");
            String ownerName = StrUtil.isBlank(sectionName) ? channelName : channelName + "/" + sectionName;
            appendSignboardLocations(locations,
                    sectionSignboardsByBizId.get(sectionId),
                    ReferenceTypeConstants.RIVER_SECTION,
                    sectionId,
                    ownerName);
        }
        return locations;
    }

    private void appendSignboardLocations(List<BigScreenRiverChannelWithSectionsRespVO.SignboardLocationItem> locations,
                                          List<YzSignboardBfDO> signboards,
                                          String ownerType,
                                          Long ownerId,
                                          String ownerName) {
        if (signboards == null || signboards.isEmpty()) {
            return;
        }
        for (YzSignboardBfDO signboard : signboards) {
            if (!hasValidLongitudeLatitude(signboard)) {
                continue;
            }
            BigScreenRiverChannelWithSectionsRespVO.SignboardLocationItem item =
                    new BigScreenRiverChannelWithSectionsRespVO.SignboardLocationItem();
            item.setSignboardId(signboard.getId());
            item.setLongitude(signboard.getLongitude());
            item.setLatitude(signboard.getLatitude());
            item.setOwnerType(ownerType);
            item.setOwnerId(ownerId);
            item.setOwnerName(ownerName);
            locations.add(item);
        }
    }

    private boolean hasValidLongitudeLatitude(YzSignboardBfDO signboard) {
        return signboard != null && signboard.getLongitude() != null && signboard.getLatitude() != null;
    }

    private String getGeomWktByFacilityId(Long facilityId) {
        if (facilityId == null) {
            return null;
        }
        YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(facilityId);
        if (base == null || base.getGeom() == null) {
            return null;
        }
        // WKTWriter 为无状态对象，这里按需创建即可
        return new WKTWriter().write(base.getGeom());
    }

    private String resolveFacilityNameBySignboardId(Long signboardId) {
        YzSignboardBfDO signboard = signboardBfMapper.selectById(signboardId);
        if (signboard == null) {
            return "";
        }

        ResolvedReference ref = resolveSignboardReference(signboard);
        String referenceType = ref == null ? null : StrUtil.trimToNull(ref.referenceType);
        Long referenceId = ref == null ? null : ref.referenceId;

        // referenceType 为空但 referenceId 有值时，尝试根据旧字段推断类型
        if (StrUtil.isBlank(referenceType) && referenceId != null) {
            if (Objects.equals(signboard.getRiverSectionId(), referenceId)) {
                referenceType = "river_section";
            } else if (Objects.equals(signboard.getRiverChannelId(), referenceId)) {
                referenceType = "river";
            } else if (Objects.equals(signboard.getWaterReservoirId(), referenceId)) {
                referenceType = "reservoir";
            }
        }

        if (StrUtil.isNotBlank(referenceType) && referenceId != null) {
            BigScreenSignboardReferenceDetailRespVO detail = getSignboardReferenceDetail(referenceType, referenceId);
            if (detail != null && StrUtil.isNotBlank(detail.getName())) {
                return detail.getName();
            }
        }

        if (signboard.getRiverSectionId() != null) {
            YzRiverSectionDO section = riverSectionMapper.selectById(signboard.getRiverSectionId());
            if (section != null && StrUtil.isNotBlank(section.getSectionName())) {
                return section.getSectionName();
            }
        }
        if (signboard.getRiverChannelId() != null) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(signboard.getRiverChannelId());
            if (channel != null && StrUtil.isNotBlank(channel.getRiverName())) {
                return channel.getRiverName();
            }
        }
        if (signboard.getWaterReservoirId() != null) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(signboard.getWaterReservoirId());
            if (reservoir != null && StrUtil.isNotBlank(reservoir.getReservoirName())) {
                return reservoir.getReservoirName();
            }
        }
        return StrUtil.blankToDefault(signboard.getSignboardName(), "");
    }

    private void applyCreateDateRange(LambdaQueryWrapper<YzProblemFeedbackDO> wrapper, String[] rawCreateTime) {
        LocalDate[] range = parseCreateDateRange(rawCreateTime);
        if (range == null || range.length != 2 || (range[0] == null && range[1] == null)) {
            return;
        }
        LocalDate startDate = range[0];
        LocalDate endDate = range[1];
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("开始时间不能晚于结束时间");
            }
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
            wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDateTime)
                    .lt(YzProblemFeedbackDO::getCreateTime, endExclusive);
            return;
        }
        if (startDate != null) {
            wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDate.atStartOfDay());
            return;
        }
        wrapper.lt(YzProblemFeedbackDO::getCreateTime, endDate.plusDays(1).atStartOfDay());
    }

    /**
     * 解析按天范围参数，兼容以下传参方式：
     * <ul>
     *     <li>createTime=2025-12-02&amp;createTime=2025-12-03</li>
     *     <li>createTime[0]=2025-12-02&amp;createTime[1]=2025-12-03</li>
     *     <li>createTime[]=2025-12-02&amp;createTime[]=2025-12-03</li>
     *     <li>createTime=[2025-12-02,2025-12-03]</li>
     * </ul>
     *
     * @param rawCreateTime 原始入参
     * @return 固定长度为 2 的日期数组（开始、结束），元素允许为空
     */
    private LocalDate[] parseCreateDateRange(String[] rawCreateTime) {
        if (rawCreateTime == null || rawCreateTime.length == 0) {
            return null;
        }

        // 兼容 Spring 对 String[] 的逗号分隔拆分：createTime=[2025-12-02,2025-12-03] 可能被拆成 ["[2025-12-02", "2025-12-03]"]
        if (rawCreateTime.length >= 2) {
            String first = StrUtil.trimToEmpty(rawCreateTime[0]);
            String last = StrUtil.trimToEmpty(rawCreateTime[rawCreateTime.length - 1]);
            if (first.startsWith("[") && !first.endsWith("]") && last.endsWith("]")) {
                rawCreateTime = new String[]{String.join(",", rawCreateTime)};
            }
        }

        List<String> candidates = new ArrayList<>();
        for (String item : rawCreateTime) {
            if (StrUtil.isBlank(item)) {
                continue;
            }
            String value = StrUtil.trim(item);
            // 兼容：createTime=[2025-12-02,2025-12-03] 或 createTime=["2025-12-02","2025-12-03"]
            if (value.startsWith("[") && value.endsWith("]")) {
                value = value.substring(1, value.length() - 1);
            }
            value = value.replace("\"", "");
            if (value.contains(",")) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    if (StrUtil.isNotBlank(part)) {
                        candidates.add(StrUtil.trim(part));
                    }
                }
            } else {
                candidates.add(value);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        LocalDate start = null;
        LocalDate end = null;
        if (candidates.size() >= 1) {
            start = parseDateOrNull(candidates.get(0));
        }
        if (candidates.size() >= 2) {
            end = parseDateOrNull(candidates.get(1));
        }
        return new LocalDate[]{start, end};
    }

    private LocalDate parseDateOrNull(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String value = StrUtil.trim(text);
        // 仅取日期部分，避免前端误传 2025-12-02 00:00:00 导致解析失败
        if (value.length() > 10) {
            value = value.substring(0, 10);
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("创建时间格式不正确，要求 yyyy-MM-dd，例如 2025-12-02", ex);
        }
    }

    private boolean isAdmin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            return false;
        }
        Set<String> roleCodes = getRoleCodeSetLower(loginUser);
        if (roleCodes.contains(ROLE_GUEST)) {
            return false;
        }
        for (String roleCode : ADMIN_ROLE_CODES) {
            if (roleCodes.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGuest(LoginUser loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            return false;
        }
        Set<String> roleCodes = getRoleCodeSetLower(loginUser);
        for (String roleCode : ADMIN_ROLE_CODES) {
            if (roleCodes.contains(roleCode)) {
                return false;
            }
        }
        return GuestRoleUtils.isPureGuest(roleCodes);
    }

    private Set<String> getRoleCodeSetLower(LoginUser loginUser) {
        @SuppressWarnings("unchecked")
        List<String> cached = loginUser.getContext("roleCodes", List.class);
        if (cached != null) {
            return cached.stream()
                    .filter(StrUtil::isNotBlank)
                    .map(it -> StrUtil.trim(it).toLowerCase())
                    .collect(Collectors.toSet());
        }
        List<String> roleCodes = systemUserSimpleMapper.selectRoleCodesByUserId(loginUser.getId());
        loginUser.setContext("roleCodes", roleCodes == null ? Collections.emptyList() : roleCodes);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Collections.emptySet();
        }
        return roleCodes.stream()
                .filter(StrUtil::isNotBlank)
                .map(it -> StrUtil.trim(it).toLowerCase())
                .collect(Collectors.toSet());
    }

    private String fallbackStatusLabel(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case 0 -> "待受理";
            case 1 -> "已驳回";
            case 2 -> "处理中";
            case 3 -> "待核验";
            case 4 -> "已办结";
            default -> String.valueOf(status);
        };
    }

    private BigScreenSignboardLocationItemRespVO buildSignboardLocationItem(YzSignboardBfDO sb, ResolvedReference ref) {
        BigScreenSignboardLocationItemRespVO vo = new BigScreenSignboardLocationItemRespVO();
        vo.setId(sb.getId());
        vo.setName(StrUtil.blankToDefault(sb.getSignboardName(), ""));
        vo.setLongitude(sb.getLongitude());
        vo.setSpecificLocation(StrUtil.blankToDefault(sb.getSpecificLocation(), ""));
        vo.setQrCode(sb.getQrCode());
        vo.setLatitude(sb.getLatitude());
        vo.setReferenceType(ref == null ? "" : StrUtil.blankToDefault(ref.referenceType, ""));
        vo.setReferenceId(ref == null ? null : ref.referenceId);
        return vo;
    }

    private Map<Long, Long> loadUnfinishedProblemCountBySignboardIds(List<YzSignboardBfDO> signboards) {
        if (signboards == null || signboards.isEmpty()) {
            return Map.of();
        }
        Map<Long, ResolvedReference> refBySignboardId = new HashMap<>();
        Map<String, Set<Long>> referenceIdsByType = new HashMap<>();
        for (YzSignboardBfDO signboard : signboards) {
            if (signboard == null || signboard.getId() == null) {
                continue;
            }
            ResolvedReference ref = resolveSignboardReference(signboard);
            if (ref == null || ref.referenceId == null || StrUtil.isBlank(ref.referenceType)) {
                continue;
            }
            refBySignboardId.put(signboard.getId(), ref);
            referenceIdsByType.computeIfAbsent(ref.referenceType, k -> new HashSet<>()).add(ref.referenceId);
        }
        if (referenceIdsByType.isEmpty()) {
            return Map.of();
        }

        Map<String, Long> countMap = new HashMap<>();
        for (Map.Entry<String, Set<Long>> entry : referenceIdsByType.entrySet()) {
            String referenceType = entry.getKey();
            Set<Long> referenceIds = entry.getValue();
            if (StrUtil.isBlank(referenceType) || CollUtil.isEmpty(referenceIds)) {
                continue;
            }
            List<Map<String, Object>> rows = problemFeedbackMapper.selectUnfinishedCountGroupByReferenceIds(
                    referenceType, referenceIds, STATUS_FINISHED, STATUS_REJECTED);
            if (rows == null || rows.isEmpty()) {
                continue;
            }
            for (Map<String, Object> row : rows) {
                if (row == null || row.isEmpty()) {
                    continue;
                }
                Long referenceId = toLong(row.get("reference_id"));
                if (referenceId == null || referenceId <= 0) {
                    continue;
                }
                countMap.put(referenceType + ":" + referenceId, toLong(row.get("cnt")));
            }
        }

        Map<Long, Long> result = new HashMap<>();
        for (Map.Entry<Long, ResolvedReference> entry : refBySignboardId.entrySet()) {
            Long signboardId = entry.getKey();
            ResolvedReference ref = entry.getValue();
            if (signboardId == null || ref == null || ref.referenceId == null || StrUtil.isBlank(ref.referenceType)) {
                continue;
            }
            String key = ref.referenceType + ":" + ref.referenceId;
            if (countMap.containsKey(key)) {
                result.put(signboardId, countMap.get(key));
            }
        }
        return result;
    }

    /**
     * 解析公示牌关联对象（优先使用 referenceType/referenceId，缺失时兼容旧字段）。
     */
    private ResolvedReference resolveSignboardReference(YzSignboardBfDO signboard) {
        if (signboard == null) {
            return new ResolvedReference("", null);
        }
        String type = StrUtil.trimToNull(signboard.getReferenceType());
        Long id = signboard.getReferenceId();
        // 优先使用 referenceType/referenceId；即使 referenceType 为空，也需要返回原始 referenceId，便于前端定位关联设施
        if (id != null) {
            return new ResolvedReference(StrUtil.blankToDefault(type, ""), id);
        }
        if (signboard.getRiverSectionId() != null) {
            return new ResolvedReference("river_section", signboard.getRiverSectionId());
        }
        if (signboard.getRiverChannelId() != null) {
            return new ResolvedReference("river", signboard.getRiverChannelId());
        }
        if (signboard.getWaterReservoirId() != null) {
            return new ResolvedReference("reservoir", signboard.getWaterReservoirId());
        }
        return new ResolvedReference("", null);
    }

    private Set<Long> resolveMatchedRiverIds(Set<Long> riverIds, String riverLevelValue) {
        if (riverIds == null || riverIds.isEmpty() || StrUtil.isBlank(riverLevelValue)) {
            return Set.of();
        }
        List<YzRiverChannelDO> list = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverLevel)
                .in(YzRiverChannelDO::getId, riverIds));
        if (list == null || list.isEmpty()) {
            return Set.of();
        }
        return list.stream()
                .filter(it -> it != null && it.getId() != null && StrUtil.equals(StrUtil.trimToEmpty(it.getRiverLevel()), riverLevelValue))
                .map(YzRiverChannelDO::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> resolveMatchedSectionIds(Set<Long> sectionIds, String riverLevelValue) {
        if (sectionIds == null || sectionIds.isEmpty() || StrUtil.isBlank(riverLevelValue)) {
            return Set.of();
        }
        List<YzRiverSectionDO> sections = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                .select(YzRiverSectionDO::getId, YzRiverSectionDO::getRiverChannelId)
                .in(YzRiverSectionDO::getId, sectionIds));
        if (sections == null || sections.isEmpty()) {
            return Set.of();
        }
        Map<Long, Long> channelIdBySectionId = sections.stream()
                .filter(it -> it != null && it.getId() != null)
                .collect(Collectors.toMap(YzRiverSectionDO::getId, YzRiverSectionDO::getRiverChannelId, (a, b) -> a));
        Set<Long> channelIds = channelIdBySectionId.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (channelIds.isEmpty()) {
            return Set.of();
        }
        List<YzRiverChannelDO> channels = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverLevel)
                .in(YzRiverChannelDO::getId, channelIds));
        Map<Long, String> levelByChannelId = channels.stream()
                .filter(it -> it != null && it.getId() != null)
                .collect(Collectors.toMap(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverLevel, (a, b) -> a));

        Set<Long> matchedSectionIds = new java.util.HashSet<>();
        for (Map.Entry<Long, Long> entry : channelIdBySectionId.entrySet()) {
            Long sectionId = entry.getKey();
            Long channelId = entry.getValue();
            if (channelId == null) {
                continue;
            }
            String level = StrUtil.trimToEmpty(levelByChannelId.get(channelId));
            if (StrUtil.equals(level, riverLevelValue)) {
                matchedSectionIds.add(sectionId);
            }
        }
        return matchedSectionIds;
    }

    private static final class ResolvedReference {
        private final String referenceType;
        private final Long referenceId;

        private ResolvedReference(String referenceType, Long referenceId) {
            this.referenceType = referenceType;
            this.referenceId = referenceId;
        }
    }

    /**
     * 按设施类别加载业务表的设备主键/名称，key 为 facility_id（基础表 id）。
     */
    private Map<Long, DeviceInfo> loadDeviceInfoByType(String facilityType, List<Long> facilityBaseIds) {
        Set<Long> idSet = facilityBaseIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (idSet.isEmpty()) {
            return Map.of();
        }
        if (StrUtil.equalsIgnoreCase(facilityType, "river")) {
            List<YzRiverChannelDO> list = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                    .select(YzRiverChannelDO::getId, YzRiverChannelDO::getFacilityId, YzRiverChannelDO::getRiverName, YzRiverChannelDO::getRiverLevel)
                    .in(YzRiverChannelDO::getFacilityId, idSet));
            return list.stream()
                    .filter(it -> it != null && it.getFacilityId() != null && it.getId() != null)
                    .collect(Collectors.toMap(YzRiverChannelDO::getFacilityId,
                            it -> new DeviceInfo(it.getId(), StrUtil.blankToDefault(it.getRiverName(), ""), it.getRiverLevel()),
                            (a, b) -> a));
        }
        if (StrUtil.equalsIgnoreCase(facilityType, "river_section")) {
            List<YzRiverSectionDO> list = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                    .select(YzRiverSectionDO::getId, YzRiverSectionDO::getFacilityId, YzRiverSectionDO::getSectionName)
                    .in(YzRiverSectionDO::getFacilityId, idSet));
            return list.stream()
                    .filter(it -> it != null && it.getFacilityId() != null && it.getId() != null)
                    .collect(Collectors.toMap(YzRiverSectionDO::getFacilityId,
                            it -> new DeviceInfo(it.getId(), StrUtil.blankToDefault(it.getSectionName(), ""), null),
                            (a, b) -> a));
        }
        if (StrUtil.equalsIgnoreCase(facilityType, "reservoir")) {
            List<YzWaterReservoirDO> list = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                    .select(YzWaterReservoirDO::getId, YzWaterReservoirDO::getFacilityId, YzWaterReservoirDO::getReservoirName)
                    .in(YzWaterReservoirDO::getFacilityId, idSet));
            return list.stream()
                    .filter(it -> it != null && it.getFacilityId() != null && it.getId() != null)
                    .collect(Collectors.toMap(YzWaterReservoirDO::getFacilityId,
                            it -> new DeviceInfo(it.getId(), StrUtil.blankToDefault(it.getReservoirName(), ""), null),
                            (a, b) -> a));
        }
        if (StrUtil.equalsIgnoreCase(facilityType, "dike")) {
            List<YzEmbankmentDO> list = embankmentMapper.selectList(new LambdaQueryWrapper<YzEmbankmentDO>()
                    .select(YzEmbankmentDO::getId, YzEmbankmentDO::getFacilityId, YzEmbankmentDO::getEmbankmentName)
                    .in(YzEmbankmentDO::getFacilityId, idSet));
            return list.stream()
                    .filter(it -> it != null && it.getFacilityId() != null && it.getId() != null)
                    .collect(Collectors.toMap(YzEmbankmentDO::getFacilityId,
                            it -> new DeviceInfo(it.getId(), StrUtil.blankToDefault(it.getEmbankmentName(), ""), null),
                            (a, b) -> a));
        }
        if (StrUtil.equalsIgnoreCase(facilityType, "pump_station")) {
            List<YzPumpStationDO> list = pumpStationMapper.selectList(new LambdaQueryWrapper<YzPumpStationDO>()
                    .select(YzPumpStationDO::getId, YzPumpStationDO::getFacilityId, YzPumpStationDO::getPumpStationName)
                    .in(YzPumpStationDO::getFacilityId, idSet));
            return list.stream()
                    .filter(it -> it != null && it.getFacilityId() != null && it.getId() != null)
                    .collect(Collectors.toMap(YzPumpStationDO::getFacilityId,
                            it -> new DeviceInfo(it.getId(), StrUtil.blankToDefault(it.getPumpStationName(), ""), null),
                            (a, b) -> a));
        }
        return Map.of();
    }

    private static final class DeviceInfo {
        private final Long deviceId;
        private final String deviceName;
        private final String riverLevel;

        private DeviceInfo(Long deviceId, String deviceName, String riverLevel) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.riverLevel = riverLevel;
        }
    }

    private Map<String, String> loadDictLabelMap(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO dict : list) {
            if (StrUtil.isBlank(dict.getValue())) {
                continue;
            }
            result.put(dict.getValue(), StrUtil.blankToDefault(dict.getLabel(), dict.getValue()));
        }
        return result;
    }

    private <T> Map<String, Long> selectGroupCount(com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX<T> mapper,
                                                   String groupColumn,
                                                   QueryWrapper<T> baseWrapper) {
        QueryWrapper<T> wrapper = baseWrapper.select(groupColumn + " as level", "count(1) as cnt").groupBy(groupColumn);
        List<Map<String, Object>> maps = mapper.selectMaps(wrapper);
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> map : maps) {
            String level = Objects.toString(map.get("level"), "");
            if (StrUtil.isBlank(level)) {
                continue;
            }
            result.put(level, toLong(map.get("cnt")));
        }
        return result;
    }

    /**
     * 统计当前有效河长，按“归一化姓名 + 归一化级别”聚合后再按级别计数。
     */
    private Map<String, Long> selectCurrentChiefCountByHeadLevel() {
        String latestChiefIdSubSql = "SELECT MAX(id) FROM yz_river_channel_management"
                + " WHERE effective_to IS NULL AND is_current = 1"
                + " AND COALESCE(deleted, 0) = 0"
                + " AND " + NORMALIZED_HEAD_NAME_SQL + " IS NOT NULL"
                + " GROUP BY " + NORMALIZED_HEAD_NAME_SQL + ", COALESCE(" + NORMALIZED_HEAD_LEVEL_SQL + ", '')";
        QueryWrapper<YzRiverChannelManagementDO> wrapper = new QueryWrapper<YzRiverChannelManagementDO>()
                .select("head_level as level", "count(1) as cnt")
                .inSql("id", latestChiefIdSubSql)
                .groupBy("head_level");
        List<Map<String, Object>> maps = managementMapper.selectMaps(wrapper);
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> map : maps) {
            String level = Objects.toString(map.get("level"), "");
            if (StrUtil.isBlank(level)) {
                continue;
            }
            result.put(level, toLong(map.get("cnt")));
        }
        return result;
    }

    /**
     * 统计当前有效河长总数（按“归一化姓名 + 归一化级别”全量去重）。
     */
    private long selectCurrentChiefTotalCount() {
        String latestChiefIdSubSql = "SELECT MAX(id) FROM yz_river_channel_management"
                + " WHERE effective_to IS NULL AND is_current = 1"
                + " AND COALESCE(deleted, 0) = 0"
                + " AND " + NORMALIZED_HEAD_NAME_SQL + " IS NOT NULL"
                + " GROUP BY " + NORMALIZED_HEAD_NAME_SQL + ", COALESCE(" + NORMALIZED_HEAD_LEVEL_SQL + ", '')";
        QueryWrapper<YzRiverChannelManagementDO> wrapper = new QueryWrapper<YzRiverChannelManagementDO>()
                .select("count(1) as cnt")
                .inSql("id", latestChiefIdSubSql);
        List<Map<String, Object>> maps = managementMapper.selectMaps(wrapper);
        if (CollUtil.isEmpty(maps)) {
            return 0L;
        }
        Map<String, Object> row = maps.get(0);
        return toLong(row.get("cnt"));
    }

    /**
     * 统计当前有效总河长人数。
     */
    private long selectCurrentTotalChiefCount() {
        QueryWrapper<YzRiverChannelManagementDO> wrapper = new QueryWrapper<YzRiverChannelManagementDO>()
                .isNull("effective_to")
                .eq("is_current", 1)
                .eq("reference_type", ReferenceTypeConstants.TOTAL_CHIEF)
                .apply("COALESCE(deleted, 0) = 0");
        return Optional.ofNullable(managementMapper.selectCount(wrapper)).orElse(0L);
    }

    private List<BigScreenDictCountItemVO> buildDictCountList(Map<String, Long> countMap, Map<String, String> labelMap) {
        List<BigScreenDictCountItemVO> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            BigScreenDictCountItemVO item = new BigScreenDictCountItemVO();
            item.setValue(entry.getKey());
            item.setLabel(StrUtil.blankToDefault(labelMap.get(entry.getKey()), entry.getKey()));
            item.setCount(entry.getValue());
            list.add(item);
        }
        return list.stream()
                .sorted((a, b) -> StrUtil.compare(a.getLabel(), b.getLabel(), true))
                .collect(Collectors.toList());
    }

    private BigScreenDictCountItemVO pickByLabelKeywords(List<BigScreenDictCountItemVO> list, String... keywords) {
        if (list == null || list.isEmpty()) {
            return buildEmptyCountItem();
        }
        for (BigScreenDictCountItemVO item : list) {
            String label = StrUtil.blankToDefault(item.getLabel(), "");
            for (String keyword : keywords) {
                if (StrUtil.isBlank(keyword)) {
                    continue;
                }
                if (label.contains(keyword)) {
                    return item;
                }
            }
        }
        return buildEmptyCountItem();
    }

    private BigScreenDictCountItemVO buildEmptyCountItem() {
        BigScreenDictCountItemVO item = new BigScreenDictCountItemVO();
        item.setCount(0L);
        item.setValue("");
        item.setLabel("");
        return item;
    }

    private Long toLongNullable(Object val) {
        if (val == null) {
            return null;
        }
        Long parsed;
        if (val instanceof Number) {
            parsed = ((Number) val).longValue();
        } else {
            try {
                parsed = Long.parseLong(val.toString());
            } catch (Exception ignore) {
                parsed = null;
            }
        }
        if (parsed == null || parsed <= 0) {
            return null;
        }
        return parsed;
    }

    /**
     * 风险隐患点 GIS 图层（业务信息 + 隐患点坐标 + 物资调运路线，只读）
     */
    public BigScreenFxRiskHazardLayerRespVO getFxRiskHazardLayer() {
        return fxTaskScreenLayerService.getRiskHazardLayer();
    }

    private Long toLong(Object val) {
        if (val == null) {
            return 0L;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.parseLong(val.toString());
        } catch (Exception ignore) {
            return 0L;
        }
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) {
            return BigDecimal.ZERO;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        try {
            return new BigDecimal(val.toString());
        } catch (Exception ignore) {
            return BigDecimal.ZERO;
        }
    }
}
