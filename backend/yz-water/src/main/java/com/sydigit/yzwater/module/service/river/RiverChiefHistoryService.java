package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryTimelineItemRespVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.dto.RiverChiefFacilityGroupRow;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 历史河长信息服务（只读）
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChiefHistoryService {

    private final YzRiverChannelManagementMapper managementMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final DictDataCommonApi dictDataApi;
    private final SystemAreaMapper systemAreaMapper;

    /**
     * 分页查询（按设施聚合，仅查询“当前河长”）
     */
    public PageResult<RiverChiefHistoryPageRespVO> getPage(RiverChiefHistoryPageReqVO reqVO) {
        String referenceType = normalizeReferenceType(reqVO.getReferenceType());
        String headLevel = StrUtil.trimToNull(reqVO.getHeadLevel());
        String headName = StrUtil.trimToNull(reqVO.getHeadName());
        String keyword = StrUtil.trimToNull(reqVO.getReferenceName());

        FacilityFilter filter = buildFacilityFilter(referenceType, keyword);
        if (filter.filtered && filter.ids.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }

        long total = managementMapper.countCurrentFacilityGroup(referenceType, headLevel, headName, filter.ids);
        if (total <= 0) {
            return new PageResult<>(List.of(), 0L);
        }

        int offset = Math.max(0, (reqVO.getPageNo() - 1) * reqVO.getPageSize());
        List<RiverChiefFacilityGroupRow> groups = managementMapper.selectCurrentFacilityGroupPage(
                referenceType, headLevel, headName, filter.ids, offset, reqVO.getPageSize());
        if (groups.isEmpty()) {
            return new PageResult<>(List.of(), total);
        }

        Map<String, String> headLevelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);
        Map<String, String> areaNameMap = new HashMap<>();
        List<RiverChiefHistoryPageRespVO> result = new ArrayList<>();
        for (RiverChiefFacilityGroupRow group : groups) {
            if (group == null || StrUtil.isBlank(group.getReferenceType()) || group.getReferenceId() == null) {
                continue;
            }
            List<YzRiverChannelManagementDO> currentRecords = managementMapper.selectCurrentByResolvedReference(
                    group.getReferenceType(), group.getReferenceId(), headLevel);
            if (StrUtil.isNotBlank(headName)) {
                currentRecords = filterByHeadName(currentRecords, headName);
            }
            if (currentRecords.isEmpty()) {
                continue;
            }
            result.add(buildPageItem(group.getReferenceType(), group.getReferenceId(), group.getEffectiveFrom(), currentRecords, headLevelMap, areaNameMap));
        }
        return new PageResult<>(result, total);
    }

    /**
     * 导出（按设施聚合，仅导出“当前河长”）
     */
    public List<RiverChiefHistoryExportExcelVO> getExportList(RiverChiefHistoryPageReqVO reqVO) {
        String referenceType = normalizeReferenceType(reqVO.getReferenceType());
        String headLevel = StrUtil.trimToNull(reqVO.getHeadLevel());
        String headName = StrUtil.trimToNull(reqVO.getHeadName());
        String keyword = StrUtil.trimToNull(reqVO.getReferenceName());

        FacilityFilter filter = buildFacilityFilter(referenceType, keyword);
        if (filter.filtered && filter.ids.isEmpty()) {
            return List.of();
        }

        List<RiverChiefFacilityGroupRow> groups = managementMapper.selectCurrentFacilityGroupList(referenceType, headLevel, headName, filter.ids);
        if (groups.isEmpty()) {
            return List.of();
        }
        Map<String, String> headLevelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);
        Map<String, String> areaNameMap = new HashMap<>();
        List<RiverChiefHistoryExportExcelVO> result = new ArrayList<>();
        for (RiverChiefFacilityGroupRow group : groups) {
            if (group == null || StrUtil.isBlank(group.getReferenceType()) || group.getReferenceId() == null) {
                continue;
            }
            List<YzRiverChannelManagementDO> currentRecords = managementMapper.selectCurrentByResolvedReference(
                    group.getReferenceType(), group.getReferenceId(), headLevel);
            if (StrUtil.isNotBlank(headName)) {
                currentRecords = filterByHeadName(currentRecords, headName);
            }
            if (currentRecords.isEmpty()) {
                continue;
            }
            RiverChiefHistoryPageRespVO pageItem = buildPageItem(group.getReferenceType(), group.getReferenceId(), group.getEffectiveFrom(), currentRecords, headLevelMap, areaNameMap);
            RiverChiefHistoryExportExcelVO excel = new RiverChiefHistoryExportExcelVO();
            excel.setReferenceTypeLabel(pageItem.getReferenceTypeLabel());
            excel.setReferenceName(pageItem.getReferenceName());
            excel.setCurrentHeadNames(pageItem.getCurrentHeadNames());
            excel.setHeadLevelLabel(pageItem.getHeadLevelLabel());
            excel.setEffectiveFrom(pageItem.getEffectiveFrom());
            excel.setAdministrativeRegion(joinAdministrativeRegion(pageItem.getAdministrativeRegion()));
            result.add(excel);
        }
        return result;
    }

    /**
     * 查询某个设施下的历史河长时间轴（包含当前与历史记录）
     */
    public List<RiverChiefHistoryTimelineItemRespVO> getTimeline(String referenceType, Long referenceId) {
        String type = normalizeReferenceType(referenceType);
        if (StrUtil.isBlank(type) || referenceId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
        }
        validateFacilityExists(type, referenceId);

        List<YzRiverChannelManagementDO> records = managementMapper.selectTimelineByResolvedReference(type, referenceId);
        if (records.isEmpty()) {
            return List.of();
        }
        Map<String, String> headLevelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);
        String referenceName = resolveFacilityName(type, referenceId);
        String referenceTypeLabel = resolveReferenceTypeLabel(type);
        Map<String, String> areaNameMap = new HashMap<>();
        Set<String> areaCodes = new HashSet<>();
        for (YzRiverChannelManagementDO record : records) {
            areaCodes.addAll(toStringList(record.getAdministrativeRegion()));
        }
        fillAreaNameMap(areaCodes, areaNameMap);

        return records.stream().map(item -> {
            RiverChiefHistoryTimelineItemRespVO vo = new RiverChiefHistoryTimelineItemRespVO();
            vo.setId(item.getId());
            vo.setHeadName(item.getHeadName());
            vo.setHeadLevel(item.getHeadLevel());
            vo.setHeadLevelLabel(resolveLabel(item.getHeadLevel(), headLevelMap));
            vo.setHeadPosition(item.getHeadPosition());
            vo.setHeadUnit(item.getHeadUnit());
            vo.setHeadContact(item.getHeadContact());
            vo.setEffectiveFrom(item.getEffectiveFrom());
            vo.setEffectiveTo(item.getEffectiveTo());
            vo.setAdministrativeRegion(resolveAreaNames(toStringList(item.getAdministrativeRegion()), areaNameMap));
            vo.setReferenceType(type);
            vo.setReferenceTypeLabel(referenceTypeLabel);
            vo.setReferenceId(referenceId);
            vo.setReferenceName(referenceName);
            return vo;
        }).toList();
    }

    private RiverChiefHistoryPageRespVO buildPageItem(String referenceType,
                                                      Long referenceId,
                                                      LocalDateTime effectiveFrom,
                                                      List<YzRiverChannelManagementDO> currentRecords,
                                                      Map<String, String> headLevelMap,
                                                      Map<String, String> areaNameMap) {
        RiverChiefHistoryPageRespVO vo = new RiverChiefHistoryPageRespVO();
        vo.setReferenceType(referenceType);
        vo.setReferenceTypeLabel(resolveReferenceTypeLabel(referenceType));
        vo.setReferenceId(referenceId);
        vo.setReferenceName(resolveFacilityName(referenceType, referenceId));

        Set<String> headNames = new HashSet<>();
        Set<String> headLevels = new HashSet<>();
        Set<String> regionCodes = new HashSet<>();
        for (YzRiverChannelManagementDO item : currentRecords) {
            if (item == null) {
                continue;
            }
            if (StrUtil.isNotBlank(item.getHeadName())) {
                headNames.add(item.getHeadName().trim());
            }
            if (StrUtil.isNotBlank(item.getHeadLevel())) {
                headLevels.add(item.getHeadLevel().trim());
            }
            List<String> codes = toStringList(item.getAdministrativeRegion());
            for (String code : codes) {
                if (StrUtil.isNotBlank(code)) {
                    regionCodes.add(code.trim());
                }
            }
        }
        vo.setCurrentHeadNames(String.join("、", headNames.stream().sorted().toList()));

        List<String> levelList = headLevels.stream().sorted().toList();
        vo.setHeadLevel(levelList.isEmpty() ? null : String.join(",", levelList));
        vo.setHeadLevelLabel(levelList.isEmpty()
                ? null
                : levelList.stream().map(l -> resolveLabel(l, headLevelMap)).distinct().collect(Collectors.joining("、")));

        vo.setEffectiveFrom(effectiveFrom);
        List<String> regionList = regionCodes.stream().sorted().toList();
        if (areaNameMap == null) {
            vo.setAdministrativeRegion(regionList);
        } else {
            fillAreaNameMap(regionList, areaNameMap);
            vo.setAdministrativeRegion(resolveAreaNames(regionList, areaNameMap));
        }
        return vo;
    }

    private String normalizeReferenceType(String referenceType) {
        String type = StrUtil.trimToNull(referenceType);
        if (type == null) {
            return null;
        }
        return type.toLowerCase();
    }

    private void validateFacilityExists(String referenceType, Long referenceId) {
        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            if (riverChannelMapper.selectById(referenceId) == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }
            return;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            if (riverSectionMapper.selectById(referenceId) == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            return;
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            if (waterReservoirMapper.selectById(referenceId) == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
            }
            return;
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
    }

    private FacilityFilter buildFacilityFilter(String referenceType, String keyword) {
        FacilityFilter result = new FacilityFilter();
        if (StrUtil.isBlank(keyword)) {
            result.filtered = false;
            result.ids = List.of();
            return result;
        }
        result.filtered = true;
        result.ids = lookupFacilityIds(referenceType, keyword);
        return result;
    }

    private List<Long> lookupFacilityIds(String referenceType, String keyword) {
        String k = StrUtil.trimToNull(keyword);
        if (k == null) {
            return List.of();
        }
        if (StrUtil.isBlank(referenceType) || ReferenceTypeConstants.RIVER.equals(referenceType)) {
            List<Long> ids = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                            .select(YzRiverChannelDO::getId)
                            .like(YzRiverChannelDO::getRiverName, k)
                            .orderByDesc(YzRiverChannelDO::getId)
                            .last("LIMIT 5000"))
                    .stream().map(YzRiverChannelDO::getId).filter(Objects::nonNull).toList();
            if (StrUtil.isBlank(referenceType)) {
                // 不指定类型时，继续累加其他类型
                List<Long> secIds = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                                .select(YzRiverSectionDO::getId)
                                .like(YzRiverSectionDO::getSectionName, k)
                                .orderByDesc(YzRiverSectionDO::getId)
                                .last("LIMIT 5000"))
                        .stream().map(YzRiverSectionDO::getId).filter(Objects::nonNull).toList();
                List<Long> resIds = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                                .select(YzWaterReservoirDO::getId)
                                .like(YzWaterReservoirDO::getReservoirName, k)
                                .orderByDesc(YzWaterReservoirDO::getId)
                                .last("LIMIT 5000"))
                        .stream().map(YzWaterReservoirDO::getId).filter(Objects::nonNull).toList();
                Set<Long> merged = new HashSet<>(ids);
                merged.addAll(secIds);
                merged.addAll(resIds);
                return merged.stream().sorted(Comparator.reverseOrder()).toList();
            }
            return ids;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            return riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                            .select(YzRiverSectionDO::getId)
                            .like(YzRiverSectionDO::getSectionName, k)
                            .orderByDesc(YzRiverSectionDO::getId)
                            .last("LIMIT 5000"))
                    .stream().map(YzRiverSectionDO::getId).filter(Objects::nonNull).toList();
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            return waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                            .select(YzWaterReservoirDO::getId)
                            .like(YzWaterReservoirDO::getReservoirName, k)
                            .orderByDesc(YzWaterReservoirDO::getId)
                            .last("LIMIT 5000"))
                    .stream().map(YzWaterReservoirDO::getId).filter(Objects::nonNull).toList();
        }
        return List.of();
    }

    private String resolveFacilityName(String referenceType, Long referenceId) {
        if (referenceId == null || StrUtil.isBlank(referenceType)) {
            return "-";
        }
        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(referenceId);
            return channel != null ? StrUtil.blankToDefault(channel.getRiverName(), String.valueOf(referenceId)) : "-";
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            YzRiverSectionDO section = riverSectionMapper.selectById(referenceId);
            if (section == null) {
                return "-";
            }
            String sectionName = StrUtil.blankToDefault(section.getSectionName(), String.valueOf(referenceId));
            if (section.getRiverChannelId() == null) {
                return sectionName;
            }
            YzRiverChannelDO channel = riverChannelMapper.selectById(section.getRiverChannelId());
            String riverName = channel != null ? StrUtil.trimToEmpty(channel.getRiverName()) : "";
            if (StrUtil.isNotBlank(riverName)) {
                return riverName + "/" + sectionName;
            }
            return sectionName;
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(referenceId);
            return reservoir != null ? StrUtil.blankToDefault(reservoir.getReservoirName(), String.valueOf(referenceId)) : "-";
        }
        return "-";
    }

    private String resolveReferenceTypeLabel(String type) {
        if (ReferenceTypeConstants.RIVER.equals(type)) {
            return "河道";
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            return "河段";
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            return "水库";
        }
        return StrUtil.blankToDefault(type, "-");
    }

    private String resolveLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        return StrUtil.blankToDefault(map.get(value), value);
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

    private List<String> toStringList(String[] values) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        return java.util.Arrays.stream(values)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private void fillAreaNameMap(Collection<String> areaCodes, Map<String, String> areaNameMap) {
        if (areaCodes == null || areaCodes.isEmpty() || areaNameMap == null) {
            return;
        }
        Set<Long> ids = new HashSet<>();
        for (String code : areaCodes) {
            String normalized = StrUtil.trimToNull(code);
            if (normalized == null) {
                continue;
            }
            if (areaNameMap.containsKey(normalized)) {
                continue;
            }
            Long id = parseAreaId(normalized);
            if (id == null) {
                areaNameMap.putIfAbsent(normalized, normalized);
                continue;
            }
            ids.add(id);
        }
        if (ids.isEmpty()) {
            return;
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectBatchIds(ids);
        Set<Long> foundIds = new HashSet<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            foundIds.add(area.getId());
            String key = String.valueOf(area.getId());
            String name = StrUtil.trimToNull(area.getName());
            areaNameMap.putIfAbsent(key, name == null ? key : name);
        }
        for (Long id : ids) {
            if (!foundIds.contains(id)) {
                areaNameMap.putIfAbsent(String.valueOf(id), String.valueOf(id));
            }
        }
    }

    private List<String> resolveAreaNames(List<String> areaCodes, Map<String, String> areaNameMap) {
        if (areaCodes == null || areaCodes.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String code : areaCodes) {
            String normalized = StrUtil.trimToNull(code);
            if (normalized == null) {
                continue;
            }
            String name = areaNameMap == null ? null : areaNameMap.get(normalized);
            String value = StrUtil.blankToDefault(name, normalized);
            if (seen.add(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private Long parseAreaId(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        try {
            return Long.parseLong(code.trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    private String joinAdministrativeRegion(List<String> values) {
        List<String> cleaned = Optional.ofNullable(values).orElse(List.of()).stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        return cleaned.isEmpty() ? null : String.join("、", cleaned);
    }

    private List<YzRiverChannelManagementDO> filterByHeadName(List<YzRiverChannelManagementDO> records, String headName) {
        if (CollUtil.isEmpty(records) || StrUtil.isBlank(headName)) {
            return records == null ? List.of() : records;
        }
        return records.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.containsIgnoreCase(StrUtil.blankToDefault(item.getHeadName(), ""), headName))
                .toList();
    }

    @Data
    private static class FacilityFilter {
        /**
         * 是否启用了设施名称过滤
         */
        private boolean filtered;
        /**
         * 匹配到的设施ID列表（用于 SQL IN 过滤）
         */
        private List<Long> ids;
    }
}
