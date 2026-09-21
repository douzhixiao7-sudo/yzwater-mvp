package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 河长信息 Excel 导入服务（riverChiefInfo 页面）
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChiefInfoExcelImportService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzRiverChannelManagementMapper managementMapper;
    private final SystemAreaMapper systemAreaMapper;
    private final DictDataCommonApi dictDataApi;

    @Transactional(rollbackFor = Exception.class)
    public RiverChiefInfoImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (!isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<RiverChiefInfoImportExcelVO> rows;
        try {
            rows = ExcelUtils.readSheet(file, RiverChiefInfoImportExcelVO.class, 0);
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        RiverChiefInfoImportRespVO respVO = new RiverChiefInfoImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        List<RowCandidate> candidates = new ArrayList<>();
        Set<String> riverNames = new LinkedHashSet<>();
        Set<String> reservoirNames = new LinkedHashSet<>();
        Set<String> areaNames = new LinkedHashSet<>();
        Set<String> sectionNames = new LinkedHashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            int excelRowNo = i + 2;
            RiverChiefInfoImportExcelVO row = rows.get(i);
            if (row == null) {
                respVO.addFailure("第" + excelRowNo + "行失败：空行");
                continue;
            }
            String headName = normalizeText(row.getHeadName());
            String referenceName = normalizeText(row.getReferenceName());
            if (StrUtil.isBlank(headName)) {
                respVO.addFailure("第" + excelRowNo + "行失败：河长姓名为空");
                continue;
            }
            if (StrUtil.isBlank(referenceName)) {
                respVO.addFailure("第" + excelRowNo + "行失败：关联河道/水库为空");
                continue;
            }
            String normalizedArea = normalizeText(row.getAdministrativeRegionName());
            String normalizedSection = normalizeText(row.getSectionName());
            String normalizedHeadLevel = normalizeText(row.getHeadLevelLabel());
            String normalizedHeadPosition = normalizeText(row.getHeadPosition());
            String normalizedHeadContact = normalizeText(row.getHeadContact());
            boolean reservoir = isReservoirReference(referenceName);
            candidates.add(new RowCandidate(excelRowNo, headName, normalizedHeadLevel,
                    normalizedArea, referenceName, normalizedSection, normalizedHeadPosition, normalizedHeadContact,
                    reservoir));
            if (reservoir) {
                reservoirNames.add(referenceName);
            } else {
                riverNames.add(referenceName);
                if (StrUtil.isNotBlank(normalizedSection)) {
                    sectionNames.add(normalizedSection);
                }
            }
            if (StrUtil.isNotBlank(normalizedArea)) {
                areaNames.add(normalizedArea);
            }
        }
        if (candidates.isEmpty()) {
            return respVO;
        }

        Map<String, String> areaIdByName = loadAreaIdByNameMap(areaNames);
        Map<String, String> headLevelValueByLabel = loadHeadLevelValueByLabelMap();
        Map<String, YzRiverChannelDO> riverByName = loadRiverByNameMap(riverNames);
        Map<String, YzWaterReservoirDO> reservoirByName = loadReservoirByNameMap(reservoirNames);

        Set<Long> riverIds = new LinkedHashSet<>();
        for (YzRiverChannelDO river : riverByName.values()) {
            if (river != null && river.getId() != null) {
                riverIds.add(river.getId());
            }
        }
        Map<String, YzRiverSectionDO> sectionByRiverAndName = loadSectionByRiverAndNameMap(riverIds, sectionNames);

        Map<FacilityRef, List<ImportedHead>> importedHeadsByFacility = new LinkedHashMap<>();
        for (RowCandidate candidate : candidates) {
            FacilityRef facilityRef = resolveFacilityRef(candidate, riverByName, reservoirByName, sectionByRiverAndName, respVO);
            if (facilityRef == null) {
                continue;
            }
            String headLevelValue = resolveHeadLevelValue(candidate.headLevelLabel, headLevelValueByLabel);
            String areaId = resolveAreaId(candidate.administrativeRegionName, areaIdByName);
            ImportedHead importedHead = new ImportedHead(candidate.headName, headLevelValue,
                    candidate.headPosition, candidate.headContact,
                    StrUtil.isBlank(areaId) ? null : new String[]{areaId});
            importedHeadsByFacility.computeIfAbsent(facilityRef, k -> new ArrayList<>()).add(importedHead);
            respVO.addSuccess();
        }
        if (importedHeadsByFacility.isEmpty()) {
            return respVO;
        }

        for (Map.Entry<FacilityRef, List<ImportedHead>> entry : importedHeadsByFacility.entrySet()) {
            applyFacilityImport(entry.getKey(), entry.getValue());
        }
        return respVO;
    }

    private void applyFacilityImport(FacilityRef facilityRef, List<ImportedHead> importedHeads) {
        if (facilityRef == null || CollUtil.isEmpty(importedHeads)) {
            return;
        }
        List<YzRiverChannelManagementDO> currentRecords = managementMapper.selectList(buildCurrentFacilityQuery(facilityRef));
        List<HeadSnapshot> snapshots = new ArrayList<>();
        if (CollUtil.isNotEmpty(currentRecords)) {
            for (YzRiverChannelManagementDO current : currentRecords) {
                if (current == null) {
                    continue;
                }
                snapshots.add(new HeadSnapshot(
                        normalizeText(current.getHeadName()),
                        normalizeText(current.getHeadLevel()),
                        normalizeText(current.getHeadPosition()),
                        normalizeText(current.getHeadContact()),
                        normalizeArray(current.getAdministrativeRegion()),
                        normalizeText(current.getHeadUnit()),
                        normalizeText(current.getRemarks())
                ));
            }
        }
        for (ImportedHead imported : importedHeads) {
            snapshots.add(new HeadSnapshot(
                    imported.headName,
                    imported.headLevel,
                    imported.headPosition,
                    imported.headContact,
                    normalizeArray(imported.administrativeRegion),
                    null,
                    null
            ));
        }
        List<HeadSnapshot> deduplicated = deduplicateSnapshots(snapshots);
        if (deduplicated.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        expireCurrent(facilityRef, now);
        int nextVersionNo = selectNextVersionNo(facilityRef);
        for (HeadSnapshot snapshot : deduplicated) {
            YzRiverChannelManagementDO insert = new YzRiverChannelManagementDO();
            insert.setId(SNOWFLAKE.nextId());
            insert.setRiverChannelId(facilityRef.riverChannelId);
            insert.setRiverSectionId(facilityRef.riverSectionId);
            insert.setWaterReservoirId(facilityRef.waterReservoirId);
            insert.setReferenceType(facilityRef.referenceType);
            insert.setReferenceId(facilityRef.referenceId);
            insert.setSectionName(facilityRef.sectionName);
            insert.setHeadName(snapshot.headName);
            insert.setHeadLevel(snapshot.headLevel);
            insert.setHeadPosition(snapshot.headPosition);
            insert.setHeadContact(snapshot.headContact);
            insert.setAdministrativeRegion(snapshot.administrativeRegion);
            insert.setHeadUnit(snapshot.headUnit);
            insert.setRemarks(snapshot.remarks);
            insert.setVersionNo(nextVersionNo);
            insert.setEffectiveFrom(now);
            insert.setEffectiveTo(null);
            insert.setIsCurrent(1);
            managementMapper.insert(insert);
        }
    }

    private FacilityRef resolveFacilityRef(RowCandidate candidate,
                                           Map<String, YzRiverChannelDO> riverByName,
                                           Map<String, YzWaterReservoirDO> reservoirByName,
                                           Map<String, YzRiverSectionDO> sectionByRiverAndName,
                                           RiverChiefInfoImportRespVO respVO) {
        if (candidate.isReservoir) {
            YzWaterReservoirDO reservoir = reservoirByName.get(candidate.referenceName);
            if (reservoir == null || reservoir.getId() == null) {
                respVO.addSkip("第" + candidate.excelRowNo + "行跳过：未找到水库【" + candidate.referenceName + "】");
                return null;
            }
            return new FacilityRef(ReferenceTypeConstants.RESERVOIR, reservoir.getId(),
                    null, null, reservoir.getId(), reservoir.getReservoirName());
        }
        YzRiverChannelDO river = riverByName.get(candidate.referenceName);
        if (river == null || river.getId() == null) {
            respVO.addSkip("第" + candidate.excelRowNo + "行跳过：未找到河道【" + candidate.referenceName + "】");
            return null;
        }
        if (StrUtil.isBlank(candidate.sectionName)) {
            return new FacilityRef(ReferenceTypeConstants.RIVER, river.getId(),
                    river.getId(), null, null, river.getRiverName());
        }
        String key = buildSectionKey(river.getId(), candidate.sectionName);
        YzRiverSectionDO section = sectionByRiverAndName.get(key);
        if (section == null || section.getId() == null) {
            // 按需求回退：第六列河段未匹配时，直接绑定到第五列河道
            return new FacilityRef(ReferenceTypeConstants.RIVER, river.getId(),
                    river.getId(), null, null, river.getRiverName());
        }
        return new FacilityRef(ReferenceTypeConstants.RIVER_SECTION, section.getId(),
                river.getId(), section.getId(), null, section.getSectionName());
    }

    private List<HeadSnapshot> deduplicateSnapshots(List<HeadSnapshot> snapshots) {
        LinkedHashMap<String, HeadSnapshot> unique = new LinkedHashMap<>();
        for (HeadSnapshot snapshot : snapshots) {
            if (snapshot == null || StrUtil.isBlank(snapshot.headName)) {
                continue;
            }
            unique.putIfAbsent(snapshot.key(), snapshot);
        }
        return new ArrayList<>(unique.values());
    }

    private void expireCurrent(FacilityRef facilityRef, LocalDateTime now) {
        managementMapper.update(null, buildCurrentFacilityUpdate(facilityRef)
                .set(YzRiverChannelManagementDO::getEffectiveTo, now)
                .set(YzRiverChannelManagementDO::getIsCurrent, 0));
    }

    private int selectNextVersionNo(FacilityRef facilityRef) {
        YzRiverChannelManagementDO max = managementMapper.selectOne(buildFacilityVersionQuery(facilityRef)
                .orderByDesc(YzRiverChannelManagementDO::getVersionNo)
                .last("LIMIT 1"));
        Integer maxNo = (max == null || max.getVersionNo() == null) ? 0 : max.getVersionNo();
        return maxNo + 1;
    }

    private LambdaQueryWrapper<YzRiverChannelManagementDO> buildCurrentFacilityQuery(FacilityRef facilityRef) {
        LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper = buildFacilityVersionQuery(facilityRef);
        wrapper.isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1);
        return wrapper;
    }

    private LambdaUpdateWrapper<YzRiverChannelManagementDO> buildCurrentFacilityUpdate(FacilityRef facilityRef) {
        LambdaUpdateWrapper<YzRiverChannelManagementDO> wrapper = new LambdaUpdateWrapper<>();
        applyFacilityCondition(wrapper, facilityRef);
        wrapper.isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1);
        return wrapper;
    }

    private LambdaQueryWrapper<YzRiverChannelManagementDO> buildFacilityVersionQuery(FacilityRef facilityRef) {
        LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper = new LambdaQueryWrapper<>();
        applyFacilityCondition(wrapper, facilityRef);
        return wrapper;
    }

    private void applyFacilityCondition(LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper, FacilityRef facilityRef) {
        if (ReferenceTypeConstants.RESERVOIR.equals(facilityRef.referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getWaterReservoirId, facilityRef.waterReservoirId);
            return;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(facilityRef.referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getRiverSectionId, facilityRef.riverSectionId);
            return;
        }
        wrapper.eq(YzRiverChannelManagementDO::getRiverChannelId, facilityRef.riverChannelId)
                .isNull(YzRiverChannelManagementDO::getRiverSectionId)
                .isNull(YzRiverChannelManagementDO::getWaterReservoirId);
    }

    private void applyFacilityCondition(LambdaUpdateWrapper<YzRiverChannelManagementDO> wrapper, FacilityRef facilityRef) {
        if (ReferenceTypeConstants.RESERVOIR.equals(facilityRef.referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getWaterReservoirId, facilityRef.waterReservoirId);
            return;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(facilityRef.referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getRiverSectionId, facilityRef.riverSectionId);
            return;
        }
        wrapper.eq(YzRiverChannelManagementDO::getRiverChannelId, facilityRef.riverChannelId)
                .isNull(YzRiverChannelManagementDO::getRiverSectionId)
                .isNull(YzRiverChannelManagementDO::getWaterReservoirId);
    }

    private Map<String, YzRiverChannelDO> loadRiverByNameMap(Set<String> riverNames) {
        if (CollUtil.isEmpty(riverNames)) {
            return Map.of();
        }
        List<YzRiverChannelDO> rivers = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName)
                .eq(YzRiverChannelDO::getDeleted, 0)
                .in(YzRiverChannelDO::getRiverName, riverNames)
                .orderByAsc(YzRiverChannelDO::getId));
        Map<String, YzRiverChannelDO> result = new LinkedHashMap<>();
        for (YzRiverChannelDO river : rivers) {
            if (river == null || river.getId() == null) {
                continue;
            }
            String name = normalizeText(river.getRiverName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            result.putIfAbsent(name, river);
        }
        return result;
    }

    private Map<String, YzWaterReservoirDO> loadReservoirByNameMap(Set<String> reservoirNames) {
        if (CollUtil.isEmpty(reservoirNames)) {
            return Map.of();
        }
        List<YzWaterReservoirDO> reservoirs = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                .select(YzWaterReservoirDO::getId, YzWaterReservoirDO::getReservoirName)
                .eq(YzWaterReservoirDO::getDeleted, 0)
                .in(YzWaterReservoirDO::getReservoirName, reservoirNames)
                .orderByAsc(YzWaterReservoirDO::getId));
        Map<String, YzWaterReservoirDO> result = new LinkedHashMap<>();
        for (YzWaterReservoirDO reservoir : reservoirs) {
            if (reservoir == null || reservoir.getId() == null) {
                continue;
            }
            String name = normalizeText(reservoir.getReservoirName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            result.putIfAbsent(name, reservoir);
        }
        return result;
    }

    private Map<String, YzRiverSectionDO> loadSectionByRiverAndNameMap(Set<Long> riverIds, Set<String> sectionNames) {
        if (CollUtil.isEmpty(riverIds) || CollUtil.isEmpty(sectionNames)) {
            return Map.of();
        }
        List<YzRiverSectionDO> sections = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                .select(YzRiverSectionDO::getId, YzRiverSectionDO::getRiverChannelId, YzRiverSectionDO::getSectionName)
                .eq(YzRiverSectionDO::getDeleted, 0)
                .in(YzRiverSectionDO::getRiverChannelId, riverIds)
                .in(YzRiverSectionDO::getSectionName, sectionNames)
                .orderByAsc(YzRiverSectionDO::getId));
        Map<String, YzRiverSectionDO> result = new HashMap<>();
        for (YzRiverSectionDO section : sections) {
            if (section == null || section.getId() == null || section.getRiverChannelId() == null) {
                continue;
            }
            String name = normalizeText(section.getSectionName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            String key = buildSectionKey(section.getRiverChannelId(), name);
            result.putIfAbsent(key, section);
        }
        return result;
    }

    private String buildSectionKey(Long riverChannelId, String sectionName) {
        return riverChannelId + "#" + sectionName;
    }

    private Map<String, String> loadAreaIdByNameMap(Set<String> areaNames) {
        if (CollUtil.isEmpty(areaNames)) {
            return Map.of();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectList(new LambdaQueryWrapper<SystemAreaDO>()
                .select(SystemAreaDO::getId, SystemAreaDO::getName)
                .eq(SystemAreaDO::getDeleted, 0)
                .orderByAsc(SystemAreaDO::getId));
        Map<String, String> result = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String name = normalizeText(area.getName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            result.putIfAbsent(name, String.valueOf(area.getId()));
        }
        return result;
    }

    private Map<String, String> loadHeadLevelValueByLabelMap() {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(ZdConstants.ZD_HZJB);
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO item : list) {
            if (item == null || StrUtil.isBlank(item.getValue())) {
                continue;
            }
            String label = normalizeText(item.getLabel());
            if (StrUtil.isBlank(label)) {
                continue;
            }
            result.putIfAbsent(label, item.getValue());
        }
        return result;
    }

    private String resolveHeadLevelValue(String label, Map<String, String> valueByLabel) {
        String normalized = normalizeText(label);
        if (StrUtil.isBlank(normalized) || valueByLabel.isEmpty()) {
            return null;
        }
        String exact = valueByLabel.get(normalized);
        if (StrUtil.isNotBlank(exact)) {
            return exact;
        }
        for (Map.Entry<String, String> entry : valueByLabel.entrySet()) {
            if (StrUtil.contains(normalized, entry.getKey()) || StrUtil.contains(entry.getKey(), normalized)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String resolveAreaId(String areaName, Map<String, String> areaIdByName) {
        String normalized = normalizeText(areaName);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        return areaIdByName.get(normalized);
    }

    private boolean isReservoirReference(String referenceName) {
        return StrUtil.contains(referenceName, "水库");
    }

    private String normalizeText(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() >= 2) {
            if ((normalized.startsWith("{") && normalized.endsWith("}"))
                    || (normalized.startsWith("[") && normalized.endsWith("]"))) {
                normalized = StrUtil.trimToNull(normalized.substring(1, normalized.length() - 1));
            }
        }
        normalized = StrUtil.removePrefix(normalized, "\"");
        normalized = StrUtil.removeSuffix(normalized, "\"");
        normalized = StrUtil.removePrefix(normalized, "'");
        normalized = StrUtil.removeSuffix(normalized, "'");
        normalized = normalized.replace('\u3000', ' ');
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return StrUtil.trimToNull(normalized);
    }

    private String[] normalizeArray(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = normalizeText(value);
            if (StrUtil.isNotBlank(normalized)) {
                set.add(normalized);
            }
        }
        return set.isEmpty() ? null : set.toArray(new String[0]);
    }

    private boolean isExcelFile(String filename) {
        if (StrUtil.isBlank(filename)) {
            return false;
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private static final class RowCandidate {
        private final int excelRowNo;
        private final String headName;
        private final String headLevelLabel;
        private final String administrativeRegionName;
        private final String referenceName;
        private final String sectionName;
        private final String headPosition;
        private final String headContact;
        private final boolean isReservoir;

        private RowCandidate(int excelRowNo, String headName, String headLevelLabel, String administrativeRegionName,
                             String referenceName, String sectionName, String headPosition, String headContact,
                             boolean isReservoir) {
            this.excelRowNo = excelRowNo;
            this.headName = headName;
            this.headLevelLabel = headLevelLabel;
            this.administrativeRegionName = administrativeRegionName;
            this.referenceName = referenceName;
            this.sectionName = sectionName;
            this.headPosition = headPosition;
            this.headContact = headContact;
            this.isReservoir = isReservoir;
        }
    }

    private static final class ImportedHead {
        private final String headName;
        private final String headLevel;
        private final String headPosition;
        private final String headContact;
        private final String[] administrativeRegion;

        private ImportedHead(String headName, String headLevel, String headPosition, String headContact,
                             String[] administrativeRegion) {
            this.headName = headName;
            this.headLevel = headLevel;
            this.headPosition = headPosition;
            this.headContact = headContact;
            this.administrativeRegion = administrativeRegion;
        }
    }

    private static final class HeadSnapshot {
        private final String headName;
        private final String headLevel;
        private final String headPosition;
        private final String headContact;
        private final String[] administrativeRegion;
        private final String headUnit;
        private final String remarks;

        private HeadSnapshot(String headName, String headLevel, String headPosition, String headContact,
                             String[] administrativeRegion, String headUnit, String remarks) {
            this.headName = headName;
            this.headLevel = headLevel;
            this.headPosition = headPosition;
            this.headContact = headContact;
            this.administrativeRegion = administrativeRegion;
            this.headUnit = headUnit;
            this.remarks = remarks;
        }

        private String key() {
            List<String> regions = administrativeRegion == null ? List.of() : Arrays.asList(administrativeRegion);
            String regionText = regions.stream().filter(StrUtil::isNotBlank).sorted(Comparator.naturalOrder())
                    .reduce((a, b) -> a + "|" + b).orElse("");
            return StrUtil.blankToDefault(headName, "") + "#"
                    + StrUtil.blankToDefault(headLevel, "") + "#"
                    + StrUtil.blankToDefault(headPosition, "") + "#"
                    + StrUtil.blankToDefault(headContact, "") + "#"
                    + regionText;
        }
    }

    private static final class FacilityRef {
        private final String referenceType;
        private final Long referenceId;
        private final Long riverChannelId;
        private final Long riverSectionId;
        private final Long waterReservoirId;
        private final String sectionName;

        private FacilityRef(String referenceType, Long referenceId,
                            Long riverChannelId, Long riverSectionId, Long waterReservoirId, String sectionName) {
            this.referenceType = referenceType;
            this.referenceId = referenceId;
            this.riverChannelId = riverChannelId;
            this.riverSectionId = riverSectionId;
            this.waterReservoirId = waterReservoirId;
            this.sectionName = sectionName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FacilityRef other)) {
                return false;
            }
            return Objects.equals(referenceType, other.referenceType)
                    && Objects.equals(referenceId, other.referenceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(referenceType, referenceId);
        }
    }
}
