package com.sydigit.yzwater.module.service.problem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisAssetRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisChiefRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisStatusStatRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisTypeStatRespVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.mysql.gis.YzGisBufferQueryMapper;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisFacilityBaseRangeRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisFacilityTypeCountRow;
import com.sydigit.yzwater.module.dal.mysql.problem.YzProblemFeedbackMapper;
import com.sydigit.yzwater.module.dal.mysql.problem.dto.ProblemFeedbackStatusCountRow;
import com.sydigit.yzwater.module.dal.mysql.problem.dto.ProblemFeedbackTypeCountRow;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 问题反馈空间分析服务
 */
@Service
@RequiredArgsConstructor
public class ProblemFeedbackSpatialAnalysisService {

    private static final int STATUS_WAIT_AUDIT = 0;//待受理
    private static final int STATUS_PROCESSING = 2;//处理中
    private static final int STATUS_PENDING_VERIFY = 3;//待核验
    private static final int STATUS_FINISHED = 4;//已办结

    private static final List<String> RIVER_TYPES = List.of("river", "river_channel", "riverchannel");
    private static final List<String> RIVER_SECTION_TYPES = List.of("river_section", "riversection", "section");
    private static final List<String> RESERVOIR_TYPES = List.of("reservoir", "water_reservoir", "waterreservoir");
    private static final List<String> PUMP_STATION_TYPES = List.of("pump_station", "pumpstation");
    private static final List<String> EMBANKMENT_TYPES = List.of("dike", "embankment");
    private static final List<String> IRRIGATION_TYPES = List.of("irrigation", "irrigation_district", "irrigationdistrict");
    private static final List<String> FLOOD_MATERIAL_TYPES = List.of("flood_prevention_material");

    private final YzProblemFeedbackMapper feedbackMapper;
    private final YzGisBufferQueryMapper gisBufferQueryMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzRiverChannelManagementMapper riverChannelManagementMapper;
    private final DictDataCommonApi dictDataApi;

    public ProblemFeedbackSpatialAnalysisRespVO analyze(ProblemFeedbackSpatialAnalysisReqVO reqVO) {
        BigDecimal longitude = reqVO.getLongitude();
        BigDecimal latitude = reqVO.getLatitude();
        BigDecimal radius = reqVO.getRadiusM();

        ProblemFeedbackSpatialAnalysisRespVO respVO = new ProblemFeedbackSpatialAnalysisRespVO();
        respVO.setLongitude(longitude);
        respVO.setLatitude(latitude);
        respVO.setRadiusM(radius);
        respVO.setStatsTime(LocalDateTime.now());

        respVO.setProblemTypeStats(buildProblemTypeStats(longitude, latitude, radius));
        respVO.setStatusStat(buildStatusStats(longitude, latitude, radius));
        respVO.setAssetStats(buildAssetStats(longitude, latitude, radius));
        respVO.setManagementList(buildManagementList(longitude, latitude, radius));
        return respVO;
    }

    private List<ProblemFeedbackSpatialAnalysisTypeStatRespVO> buildProblemTypeStats(BigDecimal longitude,
                                                                                     BigDecimal latitude,
                                                                                     BigDecimal radius) {
        List<ProblemFeedbackTypeCountRow> rows = feedbackMapper.selectTypeCountInRange(longitude, latitude, radius);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> labelMap = loadDictLabelMap(ZdConstants.ZD_FKLX);
        List<ProblemFeedbackSpatialAnalysisTypeStatRespVO> result = new ArrayList<>(rows.size());
        for (ProblemFeedbackTypeCountRow row : rows) {
            if (row == null) {
                continue;
            }
            ProblemFeedbackSpatialAnalysisTypeStatRespVO item = new ProblemFeedbackSpatialAnalysisTypeStatRespVO();
            String type = StrUtil.blankToDefault(row.getFeedbackType(), "");
            item.setFeedbackType(type);
            item.setFeedbackTypeLabel(labelMap.getOrDefault(type, type));
            item.setCount(row.getCount() == null ? 0L : row.getCount());
            result.add(item);
        }
        result.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return result;
    }

    private ProblemFeedbackSpatialAnalysisStatusStatRespVO buildStatusStats(BigDecimal longitude,
                                                                            BigDecimal latitude,
                                                                            BigDecimal radius) {
        List<ProblemFeedbackStatusCountRow> rows = feedbackMapper.selectStatusCountInRange(longitude, latitude, radius);
        long pending = 0L;
        long processing = 0L;
        long pendingVerify = 0L;
        long finished = 0L;
        if (rows != null && !rows.isEmpty()) {
            for (ProblemFeedbackStatusCountRow row : rows) {
                if (row == null || row.getStatus() == null || row.getCount() == null) {
                    continue;
                }
                int status = row.getStatus();
                long count = row.getCount();
                if (status == STATUS_WAIT_AUDIT) {
                    pending += count;
                } else if (status == STATUS_PROCESSING) {
                    processing += count;
                } else if (status == STATUS_PENDING_VERIFY) {
                    pendingVerify += count;
                } else if (status == STATUS_FINISHED) {
                    finished += count;
                }
            }
        }
        ProblemFeedbackSpatialAnalysisStatusStatRespVO stats = new ProblemFeedbackSpatialAnalysisStatusStatRespVO();
        stats.setPendingCount(pending);
        stats.setProcessingCount(processing);
        stats.setPendingVerifyCount(pendingVerify);
        stats.setFinishedCount(finished);
        return stats;
    }

    private ProblemFeedbackSpatialAnalysisAssetRespVO buildAssetStats(BigDecimal longitude,
                                                                      BigDecimal latitude,
                                                                      BigDecimal radius) {
        List<String> facilityTypes = new ArrayList<>();
        facilityTypes.addAll(RIVER_TYPES);
        facilityTypes.addAll(RESERVOIR_TYPES);
        facilityTypes.addAll(EMBANKMENT_TYPES);
        facilityTypes.addAll(PUMP_STATION_TYPES);
        facilityTypes.addAll(IRRIGATION_TYPES);
        facilityTypes.addAll(FLOOD_MATERIAL_TYPES);
        List<GisFacilityTypeCountRow> rows = gisBufferQueryMapper.selectFacilityTypeCountInRange(
                longitude, latitude, radius, facilityTypes
        );

        ProblemFeedbackSpatialAnalysisAssetRespVO stats = new ProblemFeedbackSpatialAnalysisAssetRespVO();
        stats.setRiverCount(sumByTypes(rows, RIVER_TYPES));
        stats.setReservoirCount(sumByTypes(rows, RESERVOIR_TYPES));
        stats.setEmbankmentCount(sumByTypes(rows, EMBANKMENT_TYPES));
        stats.setPumpStationCount(sumByTypes(rows, PUMP_STATION_TYPES));
        stats.setIrrigationDistrictCount(sumByTypes(rows, IRRIGATION_TYPES));
        stats.setFloodMaterialCount(sumByTypes(rows, FLOOD_MATERIAL_TYPES));
        return stats;
    }

    private List<ProblemFeedbackSpatialAnalysisChiefRespVO> buildManagementList(BigDecimal longitude,
                                                                                BigDecimal latitude,
                                                                                BigDecimal radius) {
        List<String> facilityTypes = new ArrayList<>();
        facilityTypes.addAll(RIVER_TYPES);
        facilityTypes.addAll(RIVER_SECTION_TYPES);
        facilityTypes.addAll(RESERVOIR_TYPES);
        List<GisFacilityBaseRangeRow> baseRows = gisBufferQueryMapper.selectFacilityBaseInRange(
                longitude, latitude, radius, facilityTypes
        );
        if (baseRows == null || baseRows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> adminRegionMap = baseRows.stream()
                .filter(row -> row != null && row.getFacilityId() != null)
                .collect(Collectors.toMap(GisFacilityBaseRangeRow::getFacilityId,
                        row -> StrUtil.blankToDefault(row.getAdminRegionCode(), ""),
                        (a, b) -> a));

        List<Long> riverFacilityIds = new ArrayList<>();
        List<Long> riverSectionFacilityIds = new ArrayList<>();
        List<Long> reservoirFacilityIds = new ArrayList<>();
        for (GisFacilityBaseRangeRow row : baseRows) {
            if (row == null || row.getFacilityId() == null) {
                continue;
            }
            String type = normalizeType(row.getFacilityType());
            if (matchesType(type, RIVER_TYPES)) {
                riverFacilityIds.add(row.getFacilityId());
            } else if (matchesType(type, RIVER_SECTION_TYPES)) {
                riverSectionFacilityIds.add(row.getFacilityId());
            } else if (matchesType(type, RESERVOIR_TYPES)) {
                reservoirFacilityIds.add(row.getFacilityId());
            }
        }

        Map<String, FacilityRefInfo> refInfoMap = new LinkedHashMap<>();
        if (!riverFacilityIds.isEmpty()) {
            List<YzRiverChannelDO> rivers = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                    .in(YzRiverChannelDO::getFacilityId, riverFacilityIds)
                    .eq(YzRiverChannelDO::getDeleted, 0));
            for (YzRiverChannelDO river : rivers) {
                if (river == null || river.getId() == null) {
                    continue;
                }
                String adminRegion = adminRegionMap.get(river.getFacilityId());
                FacilityRefInfo info = new FacilityRefInfo(ReferenceTypeConstants.RIVER,
                        river.getId(),
                        StrUtil.blankToDefault(river.getRiverName(), "-"),
                        adminRegion);
                refInfoMap.put(info.key(), info);
            }
        }

        if (!riverSectionFacilityIds.isEmpty()) {
            List<YzRiverSectionDO> sections = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                    .in(YzRiverSectionDO::getFacilityId, riverSectionFacilityIds)
                    .eq(YzRiverSectionDO::getDeleted, 0));
            for (YzRiverSectionDO section : sections) {
                if (section == null || section.getId() == null) {
                    continue;
                }
                String adminRegion = adminRegionMap.get(section.getFacilityId());
                FacilityRefInfo info = new FacilityRefInfo(ReferenceTypeConstants.RIVER_SECTION,
                        section.getId(),
                        StrUtil.blankToDefault(section.getSectionName(), "-"),
                        adminRegion);
                refInfoMap.put(info.key(), info);
            }
        }

        if (!reservoirFacilityIds.isEmpty()) {
            List<YzWaterReservoirDO> reservoirs = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                    .in(YzWaterReservoirDO::getFacilityId, reservoirFacilityIds)
                    .eq(YzWaterReservoirDO::getDeleted, 0));
            for (YzWaterReservoirDO reservoir : reservoirs) {
                if (reservoir == null || reservoir.getId() == null) {
                    continue;
                }
                String adminRegion = adminRegionMap.get(reservoir.getFacilityId());
                FacilityRefInfo info = new FacilityRefInfo(ReferenceTypeConstants.RESERVOIR,
                        reservoir.getId(),
                        StrUtil.blankToDefault(reservoir.getReservoirName(), "-"),
                        adminRegion);
                refInfoMap.put(info.key(), info);
            }
        }

        if (refInfoMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> headLevelLabelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);
        List<ProblemFeedbackSpatialAnalysisChiefRespVO> result = new ArrayList<>();

        collectChiefsByType(result, ReferenceTypeConstants.RIVER,
                refInfoMap, headLevelLabelMap);
        collectChiefsByType(result, ReferenceTypeConstants.RIVER_SECTION,
                refInfoMap, headLevelLabelMap);
        collectChiefsByType(result, ReferenceTypeConstants.RESERVOIR,
                refInfoMap, headLevelLabelMap);
        return result;
    }

    private void collectChiefsByType(List<ProblemFeedbackSpatialAnalysisChiefRespVO> result,
                                     String referenceType,
                                     Map<String, FacilityRefInfo> refInfoMap,
                                     Map<String, String> headLevelLabelMap) {
        List<Long> ids = refInfoMap.values().stream()
                .filter(info -> Objects.equals(info.referenceType, referenceType))
                .map(info -> info.referenceId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        List<YzRiverChannelManagementDO> chiefs = riverChannelManagementMapper
                .selectCurrentByResolvedReferenceIds(referenceType, ids);
        if (chiefs == null || chiefs.isEmpty()) {
            return;
        }
        for (YzRiverChannelManagementDO chief : chiefs) {
            ResolvedReference ref = resolveReference(chief);
            if (ref.referenceId == null || StrUtil.isBlank(ref.referenceType)) {
                continue;
            }
            FacilityRefInfo refInfo = refInfoMap.get(ResolvedReference.key(ref.referenceType, ref.referenceId));
            if (refInfo == null) {
                continue;
            }
            ProblemFeedbackSpatialAnalysisChiefRespVO item = new ProblemFeedbackSpatialAnalysisChiefRespVO();
            String level = StrUtil.blankToDefault(chief.getHeadLevel(), "");
            item.setHeadName(StrUtil.blankToDefault(chief.getHeadName(), "-"));
            item.setHeadLevel(level);
            item.setHeadLevelLabel(headLevelLabelMap.getOrDefault(level, level));
            item.setReferenceType(ref.referenceType);
            item.setReferenceTypeLabel(resolveReferenceTypeLabel(ref.referenceType));
            item.setReferenceName(refInfo.referenceName);
            item.setAdministrativeRegion(resolveAdministrativeRegion(chief.getAdministrativeRegion(), refInfo.adminRegionCode));
            result.add(item);
        }
    }

    private Long sumByTypes(List<GisFacilityTypeCountRow> rows, List<String> types) {
        if (rows == null || rows.isEmpty()) {
            return 0L;
        }
        long total = 0L;
        for (GisFacilityTypeCountRow row : rows) {
            if (row == null || row.getCount() == null) {
                continue;
            }
            String type = normalizeType(row.getFacilityType());
            if (matchesType(type, types)) {
                total += row.getCount();
            }
        }
        return total;
    }

    private boolean matchesType(String type, List<String> types) {
        if (StrUtil.isBlank(type) || types == null || types.isEmpty()) {
            return false;
        }
        String lower = type.toLowerCase();
        for (String item : types) {
            if (lower.equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeType(String type) {
        return StrUtil.blankToDefault(type, "").trim().toLowerCase();
    }

    private Map<String, String> loadDictLabelMap(String dictType) {
        List<DictDataRespDTO> dicts = dictDataApi.getDictDataList(dictType);
        if (CollUtil.isEmpty(dicts)) {
            return Collections.emptyMap();
        }
        return dicts.stream()
                .filter(item -> StrUtil.isNotBlank(item.getValue()))
                .collect(Collectors.toMap(DictDataRespDTO::getValue, DictDataRespDTO::getLabel, (a, b) -> a));
    }

    private String resolveReferenceTypeLabel(String referenceType) {
        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            return "河道";
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            return "河段";
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            return "水库";
        }
        return referenceType == null ? "-" : referenceType;
    }

    private List<String> resolveAdministrativeRegion(String[] administrativeRegion, String fallbackRaw) {
        List<String> list = toStringList(administrativeRegion);
        if (!list.isEmpty()) {
            return list;
        }
        return parseAdminRegion(fallbackRaw);
    }

    private List<String> toStringList(String[] arr) {
        if (arr == null || arr.length == 0) {
            return Collections.emptyList();
        }
        List<String> result = Arrays.stream(arr)
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    private List<String> parseAdminRegion(String raw) {
        String text = StrUtil.trimToEmpty(raw);
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        String cleaned = text;
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = cleaned.split(",");
        List<String> list = Arrays.stream(parts)
                .map(item -> item.replace("\"", "").trim())
                .filter(item -> !item.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        return list.isEmpty() ? Collections.emptyList() : list;
    }

    private ResolvedReference resolveReference(YzRiverChannelManagementDO record) {
        if (record == null) {
            return new ResolvedReference(null, null);
        }
        String type = StrUtil.trimToNull(record.getReferenceType());
        Long id = record.getReferenceId();
        if (StrUtil.isNotBlank(type) && id != null) {
            return new ResolvedReference(type, id);
        }
        if (record.getWaterReservoirId() != null) {
            return new ResolvedReference(ReferenceTypeConstants.RESERVOIR, record.getWaterReservoirId());
        }
        if (record.getRiverSectionId() != null) {
            return new ResolvedReference(ReferenceTypeConstants.RIVER_SECTION, record.getRiverSectionId());
        }
        if (record.getRiverChannelId() != null) {
            return new ResolvedReference(ReferenceTypeConstants.RIVER, record.getRiverChannelId());
        }
        return new ResolvedReference(type, id);
    }

    private record ResolvedReference(String referenceType, Long referenceId) {
        private static String key(String referenceType, Long referenceId) {
            return String.format("%s:%s",
                    StrUtil.blankToDefault(referenceType, ""),
                    referenceId == null ? "" : referenceId);
        }
    }

    private record FacilityRefInfo(String referenceType, Long referenceId, String referenceName, String adminRegionCode) {
        private String key() {
            return ResolvedReference.key(referenceType, referenceId);
        }
    }
}
