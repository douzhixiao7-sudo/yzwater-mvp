package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoFacilitySaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoFacilityRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoTotalChiefRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoTotalChiefSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoUpdateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.dto.RiverChiefInfoGroupRow;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 河长信息服务（按河长维度）
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChiefInfoService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzRiverChannelManagementMapper managementMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final RiverChiefManagementService riverChiefManagementService;

    /**
     * 分页查询（按河长维度聚合）
     */
    public PageResult<RiverChiefInfoPageRespVO> getPage(RiverChiefInfoPageReqVO reqVO) {
        String headName = StrUtil.trimToNull(reqVO.getHeadName());
        List<String> headLevel = reqVO.getHeadLevel() == null ? List.of() : reqVO.getHeadLevel().stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        String referenceType = StrUtil.trimToNull(reqVO.getReferenceType());
        String referenceName = StrUtil.trimToNull(reqVO.getReferenceName());
        String administrativeRegionLiteral = toPostgresTextArrayLiteral(reqVO.getAdministrativeRegion());

        long total = managementMapper.countCurrentChiefGroup(headName, headLevel.isEmpty() ? null : headLevel, administrativeRegionLiteral,
                referenceType, referenceName);
        if (total <= 0) {
            return new PageResult<>(List.of(), 0L);
        }

        int offset = Math.max(0, (reqVO.getPageNo() - 1) * reqVO.getPageSize());
        List<RiverChiefInfoGroupRow> groups = managementMapper.selectCurrentChiefGroupPage(
                headName, headLevel.isEmpty() ? null : headLevel, administrativeRegionLiteral, referenceType, referenceName, offset,
                reqVO.getPageSize());
        if (groups.isEmpty()) {
            return new PageResult<>(List.of(), total);
        }
        Map<String, String> facilitySummaryMap = buildFacilitySummaryMap(groups);
        List<RiverChiefInfoPageRespVO> list = new ArrayList<>(groups.size());
        for (RiverChiefInfoGroupRow group : groups) {
            RiverChiefInfoPageRespVO vo = toPageRespVO(group,
                    facilitySummaryMap.getOrDefault(buildChiefGroupKey(group), ""));
            List<Long> memberIds = managementMapper
                    .selectCurrentByChiefDimension(group.getHeadName(), group.getHeadLevel(), group.getHeadPosition())
                    .stream()
                    .map(YzRiverChannelManagementDO::getId)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();
            vo.setMemberIds(memberIds);
            list.add(vo);
        }
        return new PageResult<>(list, total);
    }

    /**
     * 查询总河长列表（固定展示，不分页）
     */
    public List<RiverChiefInfoTotalChiefRespVO> getTotalChiefList() {
        return managementMapper.selectCurrentTotalChiefs().stream()
                .filter(this::isTotalChiefRecord)
                .sorted((left, right) -> {
                    LocalDateTime leftTime = left == null ? null : left.getEffectiveFrom();
                    LocalDateTime rightTime = right == null ? null : right.getEffectiveFrom();
                    if (Objects.equals(leftTime, rightTime)) {
                        Long leftId = left == null ? null : left.getId();
                        Long rightId = right == null ? null : right.getId();
                        return Comparator.nullsLast(Long::compareTo).reversed().compare(leftId, rightId);
                    }
                    return Comparator.nullsLast(LocalDateTime::compareTo).reversed().compare(leftTime, rightTime);
                })
                .map(this::toTotalChiefRespVO)
                .toList();
    }

    /**
     * 详情（包含该河长当前关联的全部设施）
     */
    public RiverChiefInfoDetailRespVO getDetail(Long id) {
        YzRiverChannelManagementDO record = getCurrentRecord(id);
        List<YzRiverChannelManagementDO> groupRecords = managementMapper
                .selectCurrentByChiefDimension(record.getHeadName(), record.getHeadLevel(), record.getHeadPosition());
        if (groupRecords.isEmpty()) {
            groupRecords = List.of(record);
        }

        RiverChiefInfoDetailRespVO vo = new RiverChiefInfoDetailRespVO();
        vo.setId(record.getId());
        vo.setHeadName(record.getHeadName());
        vo.setHeadLevel(record.getHeadLevel());
        String positionNorm = normalizeChiefPosition(record.getHeadPosition());
        vo.setHeadPosition(StrUtil.isBlank(positionNorm) ? "-" : positionNorm);
        vo.setHeadUnit(record.getHeadUnit());
        vo.setHeadContact(record.getHeadContact());
        vo.setResponsibilities(resolveResponsibilitiesForRecord(record));
        vo.setRemarks(record.getRemarks());
        vo.setAdministrativeRegion(toStringList(record.getAdministrativeRegion()));
        vo.setEffectiveFrom(record.getEffectiveFrom());
        vo.setFacilities(buildFacilities(groupRecords));
        vo.setMemberIds(groupRecords.stream()
                .map(YzRiverChannelManagementDO::getId)
                .filter(Objects::nonNull)
                .sorted()
                .toList());
        return vo;
    }

    /**
     * 新增（按河长维度，可一次关联多个设施）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(@Valid RiverChiefInfoCreateReqVO reqVO) {
        String headName = normalizeRequired(reqVO.getHeadName(), "河长姓名不能为空");
        String headLevel = normalizeRequired(reqVO.getHeadLevel(), "河长级别不能为空");
        String responsibilities = StrUtil.trimToNull(reqVO.getResponsibilities());
        String headPosition = StrUtil.trimToNull(reqVO.getHeadPosition());
        String headUnit = StrUtil.trimToNull(reqVO.getHeadUnit());
        String remarks = StrUtil.trimToNull(reqVO.getRemarks());
        LocalDateTime effectiveFrom = LocalDateTime.now();

        List<FacilityRef> targetFacilities = normalizeFacilities(reqVO.getFacilities(), true);
        Long createdId = null;
        for (FacilityRef facility : targetFacilities) {
            RiverChiefManagementSaveReqVO saveReqVO = new RiverChiefManagementSaveReqVO();
            saveReqVO.setReferenceType(facility.referenceType);
            saveReqVO.setReferenceId(facility.referenceId);
            saveReqVO.setHeadName(headName);
            saveReqVO.setHeadLevel(headLevel);
            saveReqVO.setHeadPosition(headPosition);
            saveReqVO.setHeadUnit(headUnit);
            saveReqVO.setAdministrativeRegion(reqVO.getAdministrativeRegion());
            saveReqVO.setRemarks(remarks);
            saveReqVO.setResponsibilities(resolveResponsibilitiesForFacility(facility, responsibilities, null));
            Long itemCreatedId = riverChiefManagementService.create(saveReqVO, effectiveFrom);
            if (createdId == null) {
                createdId = itemCreatedId;
            }
        }
        return createdId;
    }

    /**
     * 新增总河长
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTotalChief(@Valid RiverChiefInfoTotalChiefSaveReqVO reqVO) {
        YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
        record.setId(SNOWFLAKE.nextId());
        record.setVersionNo(1);
        record.setIsCurrent(1);
        record.setEffectiveTo(null);
        fillTotalChiefRecord(record, reqVO);
        managementMapper.insert(record);
        return record.getId();
    }

    /**
     * 编辑（按河长维度同步更新其全部当前关联设施）
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(@Valid RiverChiefInfoUpdateReqVO reqVO) {
        YzRiverChannelManagementDO record = getCurrentRecord(reqVO.getId());
        String headName = normalizeRequired(reqVO.getHeadName(), "河长姓名不能为空");
        String headLevel = normalizeRequired(reqVO.getHeadLevel(), "河长级别不能为空");
        String headPosition = StrUtil.trimToNull(reqVO.getHeadPosition());
        String headUnit = StrUtil.trimToNull(reqVO.getHeadUnit());
        String responsibilities = StrUtil.trimToNull(reqVO.getResponsibilities());
        String remarks = StrUtil.trimToNull(reqVO.getRemarks());
        LocalDateTime effectiveFrom = LocalDateTime.now();

        List<YzRiverChannelManagementDO> groupRecords = managementMapper
                .selectCurrentByChiefDimension(record.getHeadName(), record.getHeadLevel(), record.getHeadPosition());
        if (groupRecords.isEmpty()) {
            groupRecords = List.of(record);
        }

        Map<String, YzRiverChannelManagementDO> currentMap = new LinkedHashMap<>();
        for (YzRiverChannelManagementDO item : groupRecords) {
            if (item == null || item.getId() == null) {
                continue;
            }
            ResolvedReference ref = resolveReference(item);
            if (StrUtil.isBlank(ref.referenceType) || ref.referenceId == null) {
                continue;
            }
            currentMap.put(toFacilityKey(ref.referenceType, ref.referenceId), item);
        }

        boolean facilitiesProvided = reqVO.getFacilities() != null;
        List<FacilityRef> targetFacilities = facilitiesProvided
                ? normalizeFacilities(reqVO.getFacilities(), false)
                : currentMap.values().stream()
                .map(this::resolveReference)
                .filter(ref -> StrUtil.isNotBlank(ref.referenceType) && ref.referenceId != null)
                .map(ref -> new FacilityRef(ref.referenceType, ref.referenceId))
                .toList();

        // 河长制-河长信息页：仅存「无任何有效关联设施」的占位行时，允许显式传入空列表，仅更新河长基础字段（职务等），不走设施版本化链路
        if (facilitiesProvided && targetFacilities.isEmpty() && currentMap.isEmpty()) {
            applyBaseFieldsOnlyToOrphanChiefRecords(groupRecords, headName, headLevel, headPosition, headUnit, remarks,
                    reqVO.getAdministrativeRegion(), reqVO.getHeadContact());
            syncChiefIdentityAcrossMergedGroup(groupRecords, headName, headLevel, headPosition);
            return;
        }

        // 编辑：显式传空列表且当前已有设施 → 取消全部关联（与 RiverChiefInfoUpdateReqVO 说明一致），保留编辑入口 id 对应一条占位行
        if (facilitiesProvided && targetFacilities.isEmpty() && !currentMap.isEmpty()) {
            collapseAllFacilitiesToSingleOrphanRow(reqVO.getId(), groupRecords, headName, headLevel, headPosition,
                    headUnit, remarks, reqVO.getAdministrativeRegion(), reqVO.getHeadContact());
            syncChiefIdentityAcrossMergedGroup(groupRecords, headName, headLevel, headPosition);
            return;
        }

        ResolvedReference anchorRef = resolveReference(record);
        boolean anchorIsOrphan = StrUtil.isBlank(anchorRef.referenceType) || anchorRef.referenceId == null;
        boolean anchorEligibleForInPlaceBind = anchorIsOrphan
                && isSingleOrphanChiefRowGroup(groupRecords, record)
                && currentMap.isEmpty();
        boolean anchorOrphanFacilityBindConsumed = false;

        Set<String> targetKeys = new LinkedHashSet<>();
        for (FacilityRef target : targetFacilities) {
            String key = toFacilityKey(target.referenceType, target.referenceId);
            targetKeys.add(key);
            YzRiverChannelManagementDO current = currentMap.get(key);

            RiverChiefManagementSaveReqVO saveReqVO = new RiverChiefManagementSaveReqVO();
            if (current != null) {
                saveReqVO.setId(current.getId());
            }
            saveReqVO.setReferenceType(target.referenceType);
            saveReqVO.setReferenceId(target.referenceId);
            saveReqVO.setHeadName(headName);
            saveReqVO.setHeadLevel(headLevel);
            saveReqVO.setHeadPosition(headPosition);
            saveReqVO.setHeadUnit(headUnit);
            saveReqVO.setAdministrativeRegion(reqVO.getAdministrativeRegion());
            saveReqVO.setRemarks(remarks);
            saveReqVO.setResponsibilities(resolveResponsibilitiesForFacility(
                    target, responsibilities, null));

            if (current != null) {
                riverChiefManagementService.update(saveReqVO, effectiveFrom);
            } else if (anchorEligibleForInPlaceBind && !anchorOrphanFacilityBindConsumed) {
                upgradeOrphanRowWithFirstFacilityBinding(reqVO.getId(), target, headName, headLevel, headPosition,
                        headUnit, remarks, reqVO.getAdministrativeRegion(), reqVO.getHeadContact());
                anchorOrphanFacilityBindConsumed = true;
            } else {
                riverChiefManagementService.create(saveReqVO, effectiveFrom);
            }
        }

        if (!facilitiesProvided) {
            syncChiefIdentityAcrossMergedGroup(groupRecords, headName, headLevel, headPosition);
            return;
        }
        for (Map.Entry<String, YzRiverChannelManagementDO> entry : currentMap.entrySet()) {
            if (targetKeys.contains(entry.getKey())) {
                continue;
            }
            YzRiverChannelManagementDO item = entry.getValue();
            if (item != null && item.getId() != null) {
                riverChiefManagementService.delete(item.getId());
            }
        }
        // 合并组内：姓名/级别/职务 与设施独立；设施走版本化后，对仍为「当前」的成员行统一三字段（含未参与本次设施循环的占位行等）
        syncChiefIdentityAcrossMergedGroup(groupRecords, headName, headLevel, headPosition);
    }

    /**
     * 编辑总河长
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTotalChief(@Valid RiverChiefInfoTotalChiefSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        YzRiverChannelManagementDO record = getCurrentTotalChiefRecord(reqVO.getId());
        fillTotalChiefRecord(record, reqVO);
        // PostgreSQL 当前库 deleted 实际存 smallint 0/1，避免按 Boolean 回写
        record.setDeleted(null);
        managementMapper.updateById(record);
    }

    /**
     * 删除（按河长维度删除其全部当前关联设施）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        YzRiverChannelManagementDO record = getCurrentRecord(id);
        List<YzRiverChannelManagementDO> groupRecords = managementMapper
                .selectCurrentByChiefDimension(record.getHeadName(), record.getHeadLevel(), record.getHeadPosition());
        if (groupRecords.isEmpty()) {
            deleteSingleManagementRow(id, record);
            return;
        }
        // 同一设施版本化删除会一次性失效该设施下全部当前行：按设施去重，避免重复行二次删除报「不存在」
        LinkedHashSet<String> processedFacilityKeys = new LinkedHashSet<>();
        for (YzRiverChannelManagementDO item : groupRecords) {
            if (item == null || item.getId() == null) {
                continue;
            }
            ResolvedReference ref = resolveReference(item);
            if (StrUtil.isBlank(ref.referenceType) || ref.referenceId == null) {
                retireCurrentManagementRow(item.getId());
                continue;
            }
            String facilityKey = toFacilityKey(ref.referenceType, ref.referenceId);
            if (processedFacilityKeys.add(facilityKey)) {
                riverChiefManagementService.delete(item.getId());
            }
        }
    }

    /**
     * 删除单条当前记录：有设施走版本化删除；无设施占位行直接失效（不走 RiverChiefManagementService#delete）
     */
    private void deleteSingleManagementRow(Long id, YzRiverChannelManagementDO record) {
        if (record == null) {
            return;
        }
        ResolvedReference ref = resolveReference(record);
        if (StrUtil.isBlank(ref.referenceType) || ref.referenceId == null) {
            retireCurrentManagementRow(id);
            return;
        }
        riverChiefManagementService.delete(id);
    }

    /**
     * 删除总河长
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTotalChief(Long id) {
        YzRiverChannelManagementDO record = getCurrentTotalChiefRecord(id);
        managementMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                .set(YzRiverChannelManagementDO::getIsCurrent, 0)
                .set(YzRiverChannelManagementDO::getEffectiveTo, LocalDateTime.now())
                .setSql("deleted = 1")
                .eq(YzRiverChannelManagementDO::getId, record.getId()));
    }

    /**
     * 合并展示维度下，将「河长姓名 / 河长级别 / 河长职务」同步到组内仍为当前有效的成员行。
     * <p>与「关联设施」解耦：设施增删改仍由 {@link RiverChiefManagementService} 版本化处理；本方法仅补齐同一逻辑人下
     * 未经过设施循环或仍需与表单一致的三字段。</p>
     */
    private void syncChiefIdentityAcrossMergedGroup(List<YzRiverChannelManagementDO> groupRecords,
                                                    String headName,
                                                    String headLevel,
                                                    String headPosition) {
        if (groupRecords == null || groupRecords.isEmpty()) {
            return;
        }
        for (YzRiverChannelManagementDO item : groupRecords) {
            if (item == null || item.getId() == null || isTotalChiefRecord(item)) {
                continue;
            }
            YzRiverChannelManagementDO current = managementMapper.selectById(item.getId());
            if (current == null || Boolean.TRUE.equals(current.getDeleted())
                    || current.getEffectiveTo() != null || !Objects.equals(current.getIsCurrent(), 1)) {
                continue;
            }
            String rowPos = StrUtil.trimToNull(current.getHeadPosition());
            String targetPos = StrUtil.trimToNull(headPosition);
            if (Objects.equals(current.getHeadName(), headName)
                    && Objects.equals(current.getHeadLevel(), headLevel)
                    && Objects.equals(rowPos, targetPos)) {
                continue;
            }
            managementMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                    .eq(YzRiverChannelManagementDO::getId, item.getId())
                    .set(YzRiverChannelManagementDO::getHeadName, headName)
                    .set(YzRiverChannelManagementDO::getHeadLevel, headLevel)
                    .set(YzRiverChannelManagementDO::getHeadPosition, headPosition));
        }
    }

    /**
     * 更新「无任何可解析关联设施」的当前河长占位记录（与同维度下 FacilityKey 链路隔离，避免 resolveFacilityKey 抛错）
     */
    private void applyBaseFieldsOnlyToOrphanChiefRecords(List<YzRiverChannelManagementDO> groupRecords,
                                                         String headName,
                                                         String headLevel,
                                                         String headPosition,
                                                         String headUnit,
                                                         String remarks,
                                                         List<String> administrativeRegion,
                                                         String headContact) {
        String[] adminArr = toStringArray(administrativeRegion);
        String contact = StrUtil.trimToNull(headContact);
        for (YzRiverChannelManagementDO item : groupRecords) {
            if (item == null || item.getId() == null || isTotalChiefRecord(item)) {
                continue;
            }
            ResolvedReference ref = resolveReference(item);
            if (StrUtil.isNotBlank(ref.referenceType) && ref.referenceId != null) {
                continue;
            }
            managementMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                    .eq(YzRiverChannelManagementDO::getId, item.getId())
                    .set(YzRiverChannelManagementDO::getHeadName, headName)
                    .set(YzRiverChannelManagementDO::getHeadLevel, headLevel)
                    .set(YzRiverChannelManagementDO::getHeadPosition, headPosition)
                    .set(YzRiverChannelManagementDO::getHeadUnit, headUnit)
                    .set(YzRiverChannelManagementDO::getRemarks, remarks)
                    .set(YzRiverChannelManagementDO::getHeadContact, contact)
                    .set(YzRiverChannelManagementDO::getAdministrativeRegion, adminArr));
        }
    }

    /**
     * 取消全部关联设施：对带设施的行走版本化删除；编辑入口行清空关联字段并更新河长基础信息；
     * 同维度下其它无设施占位行直接失效（无法走 RiverChiefManagementService.delete，因其依赖 FacilityKey）。
     */
    private void collapseAllFacilitiesToSingleOrphanRow(Long anchorId,
                                                       List<YzRiverChannelManagementDO> groupRecords,
                                                       String headName,
                                                       String headLevel,
                                                       String headPosition,
                                                       String headUnit,
                                                       String remarks,
                                                       List<String> administrativeRegion,
                                                       String headContact) {
        boolean anchorInGroup = groupRecords.stream()
                .filter(Objects::nonNull)
                .anyMatch(it -> Objects.equals(it.getId(), anchorId));
        if (!anchorInGroup) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        for (YzRiverChannelManagementDO item : groupRecords) {
            if (item == null || item.getId() == null || isTotalChiefRecord(item)) {
                continue;
            }
            Long id = item.getId();
            ResolvedReference ref = resolveReference(item);
            boolean hasFacility = StrUtil.isNotBlank(ref.referenceType) && ref.referenceId != null;
            if (Objects.equals(id, anchorId)) {
                if (hasFacility) {
                    clearFacilityBindingOnRow(id, headName, headLevel, headPosition, headUnit, remarks,
                            administrativeRegion, headContact);
                } else {
                    LambdaUpdateWrapper<YzRiverChannelManagementDO> uw = new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                            .eq(YzRiverChannelManagementDO::getId, id);
                    applyChiefBaseFieldsToUpdateWrapper(uw, headName, headLevel, headPosition, headUnit, remarks,
                            administrativeRegion, headContact);
                    managementMapper.update(null, uw);
                }
                continue;
            }
            if (hasFacility) {
                riverChiefManagementService.delete(id);
            } else {
                retireCurrentManagementRow(id);
            }
        }
    }

    /** 清空关联对象与外键，保留河长基础字段（占位行） */
    private void clearFacilityBindingOnRow(Long rowId,
                                           String headName,
                                           String headLevel,
                                           String headPosition,
                                           String headUnit,
                                           String remarks,
                                           List<String> administrativeRegion,
                                           String headContact) {
        LambdaUpdateWrapper<YzRiverChannelManagementDO> uw = new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getId, rowId)
                .setSql("river_channel_id = null, river_section_id = null, water_reservoir_id = null, "
                        + "reference_type = null, reference_id = null, section_name = null");
        applyChiefBaseFieldsToUpdateWrapper(uw, headName, headLevel, headPosition, headUnit, remarks,
                administrativeRegion, headContact);
        managementMapper.update(null, uw);
    }

    private void retireCurrentManagementRow(Long rowId) {
        managementMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getId, rowId)
                .set(YzRiverChannelManagementDO::getIsCurrent, 0)
                .set(YzRiverChannelManagementDO::getEffectiveTo, LocalDateTime.now())
                .setSql("deleted = 1"));
    }

    /**
     * 当前河长维度下仅一条非总河长记录，且为占位（无任何可解析关联设施），且与编辑入口 id 一致。
     * 用于首次绑定设施时原地更新，避免多条异常占位被误合并。
     */
    private boolean isSingleOrphanChiefRowGroup(List<YzRiverChannelManagementDO> groupRecords,
                                                YzRiverChannelManagementDO anchor) {
        if (anchor == null || anchor.getId() == null) {
            return false;
        }
        List<YzRiverChannelManagementDO> relevant = groupRecords.stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getId() != null)
                .filter(r -> !isTotalChiefRecord(r))
                .toList();
        if (relevant.size() != 1) {
            return false;
        }
        YzRiverChannelManagementDO only = relevant.get(0);
        if (!Objects.equals(only.getId(), anchor.getId())) {
            return false;
        }
        ResolvedReference ref = resolveReference(only);
        return StrUtil.isBlank(ref.referenceType) || ref.referenceId == null;
    }

    /**
     * 占位行首次绑定关联设施：在同一主键上写入与 {@code RiverChiefManagementService#insertNewVersion} 一致的关联字段，
     * 不走路径版本化 insert，避免列表 id 变化、同一人出现两条当前记录。
     */
    private void upgradeOrphanRowWithFirstFacilityBinding(Long rowId,
                                                          FacilityRef target,
                                                          String headName,
                                                          String headLevel,
                                                          String headPosition,
                                                          String headUnit,
                                                          String remarks,
                                                          List<String> administrativeRegion,
                                                          String headContact) {
        String type = StrUtil.trimToEmpty(target.referenceType).toLowerCase();
        Long refId = target.referenceId;
        if (refId == null) {
            throw ServiceExceptionUtil.invalidParamException("关联设施ID不能为空");
        }

        LambdaUpdateWrapper<YzRiverChannelManagementDO> uw = new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getId, rowId);
        applyChiefBaseFieldsToUpdateWrapper(uw, headName, headLevel, headPosition, headUnit, remarks,
                administrativeRegion, headContact);

        if (ReferenceTypeConstants.RIVER.equals(type)) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(refId);
            if (channel == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }
            uw.set(YzRiverChannelManagementDO::getRiverChannelId, channel.getId())
                    .set(YzRiverChannelManagementDO::getRiverSectionId, null)
                    .set(YzRiverChannelManagementDO::getWaterReservoirId, null)
                    .set(YzRiverChannelManagementDO::getReferenceType, type)
                    .set(YzRiverChannelManagementDO::getReferenceId, refId)
                    .set(YzRiverChannelManagementDO::getSectionName, StrUtil.blankToDefault(channel.getRiverName(), ""));
            managementMapper.update(null, uw);
            return;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            YzRiverSectionDO section = riverSectionMapper.selectById(refId);
            if (section == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            uw.set(YzRiverChannelManagementDO::getRiverChannelId, section.getRiverChannelId())
                    .set(YzRiverChannelManagementDO::getRiverSectionId, section.getId())
                    .set(YzRiverChannelManagementDO::getWaterReservoirId, null)
                    .set(YzRiverChannelManagementDO::getReferenceType, type)
                    .set(YzRiverChannelManagementDO::getReferenceId, refId)
                    .set(YzRiverChannelManagementDO::getSectionName, StrUtil.blankToDefault(section.getSectionName(), ""));
            managementMapper.update(null, uw);
            return;
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(refId);
            if (reservoir == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
            }
            uw.set(YzRiverChannelManagementDO::getRiverChannelId, null)
                    .set(YzRiverChannelManagementDO::getRiverSectionId, null)
                    .set(YzRiverChannelManagementDO::getWaterReservoirId, refId)
                    .set(YzRiverChannelManagementDO::getReferenceType, type)
                    .set(YzRiverChannelManagementDO::getReferenceId, refId)
                    .set(YzRiverChannelManagementDO::getSectionName, StrUtil.blankToDefault(reservoir.getReservoirName(), ""));
            managementMapper.update(null, uw);
            return;
        }
        throw ServiceExceptionUtil.invalidParamException("关联设施类型不合法");
    }

    private void applyChiefBaseFieldsToUpdateWrapper(LambdaUpdateWrapper<YzRiverChannelManagementDO> uw,
                                                     String headName,
                                                     String headLevel,
                                                     String headPosition,
                                                     String headUnit,
                                                     String remarks,
                                                     List<String> administrativeRegion,
                                                     String headContact) {
        String[] adminArr = toStringArray(administrativeRegion);
        String contact = StrUtil.trimToNull(headContact);
        uw.set(YzRiverChannelManagementDO::getHeadName, headName)
                .set(YzRiverChannelManagementDO::getHeadLevel, headLevel)
                .set(YzRiverChannelManagementDO::getHeadPosition, headPosition)
                .set(YzRiverChannelManagementDO::getHeadUnit, headUnit)
                .set(YzRiverChannelManagementDO::getRemarks, remarks)
                .set(YzRiverChannelManagementDO::getHeadContact, contact)
                .set(YzRiverChannelManagementDO::getAdministrativeRegion, adminArr);
    }

    private YzRiverChannelManagementDO getCurrentRecord(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        YzRiverChannelManagementDO record = managementMapper.selectById(id);
        if (record == null || Boolean.TRUE.equals(record.getDeleted())
                || record.getEffectiveTo() != null || !Objects.equals(record.getIsCurrent(), 1)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        return record;
    }

    private YzRiverChannelManagementDO getCurrentTotalChiefRecord(Long id) {
        YzRiverChannelManagementDO record = getCurrentRecord(id);
        if (!isTotalChiefRecord(record)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        return record;
    }

    private RiverChiefInfoPageRespVO toPageRespVO(RiverChiefInfoGroupRow row, String facilitySummary) {
        RiverChiefInfoPageRespVO vo = new RiverChiefInfoPageRespVO();
        vo.setId(row.getId());
        vo.setHeadName(row.getHeadName());
        vo.setHeadLevel(row.getHeadLevel());
        String positionNorm = normalizeChiefPosition(row.getHeadPosition());
        vo.setHeadPosition(StrUtil.isBlank(positionNorm) ? "-" : positionNorm);
        vo.setHeadUnit(row.getHeadUnit());
        vo.setFacilitySummary(facilitySummary);
        vo.setAdministrativeRegion(toStringList(row.getAdministrativeRegion()));
        vo.setEffectiveFrom(row.getEffectiveFrom());
        return vo;
    }

    private Map<String, String> buildFacilitySummaryMap(List<RiverChiefInfoGroupRow> groups) {
        List<String> headNames = groups.stream()
                .map(RiverChiefInfoGroupRow::getHeadName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (headNames.isEmpty()) {
            return Map.of();
        }
        List<YzRiverChannelManagementDO> records = managementMapper.selectCurrentByHeadNames(headNames);
        if (records.isEmpty()) {
            return Map.of();
        }
        Map<String, List<YzRiverChannelManagementDO>> groupedRecords = records.stream()
                .filter(item -> !isTotalChiefRecord(item))
                .filter(item -> StrUtil.isNotBlank(item.getHeadName()))
                .collect(Collectors.groupingBy(
                        this::buildChiefGroupKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        Map<String, String> summaryMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<YzRiverChannelManagementDO>> entry : groupedRecords.entrySet()) {
            String summary = buildFacilities(entry.getValue()).stream()
                    .map(item -> StrUtil.blankToDefault(item.getReferenceName(), "-"))
                    .collect(Collectors.joining("、"));
            summaryMap.put(entry.getKey(), summary);
        }
        return summaryMap;
    }

    private boolean isTotalChiefRecord(YzRiverChannelManagementDO record) {
        if (record == null) {
            return false;
        }
        return ReferenceTypeConstants.TOTAL_CHIEF.equals(StrUtil.trimToEmpty(record.getReferenceType()));
    }

    private RiverChiefInfoTotalChiefRespVO toTotalChiefRespVO(YzRiverChannelManagementDO record) {
        RiverChiefInfoTotalChiefRespVO vo = new RiverChiefInfoTotalChiefRespVO();
        vo.setId(record.getId());
        vo.setHeadName(record.getHeadName());
        vo.setHeadLevel(record.getHeadLevel());
        vo.setHeadPosition(record.getHeadPosition());
        return vo;
    }

    private String buildChiefGroupKey(RiverChiefInfoGroupRow row) {
        if (row == null) {
            return "";
        }
        return buildChiefGroupKey(row.getHeadName(), row.getHeadLevel(), row.getHeadPosition());
    }

    private String buildChiefGroupKey(YzRiverChannelManagementDO record) {
        if (record == null) {
            return "";
        }
        return buildChiefGroupKey(record.getHeadName(), record.getHeadLevel(), record.getHeadPosition());
    }

    /** 与河长信息分页 SQL 的 GROUP BY 维度一致：规范化姓名 + 河长级别 + 规范化职务（不按 effective_from 拆分，避免多设施分批写入拆行） */
    private String buildChiefGroupKey(String headName, String headLevel, String headPosition) {
        String normalizedHeadName = normalizeChiefName(headName);
        String normalizedHeadLevel = StrUtil.trimToEmpty(headLevel);
        String normalizedHeadPosition = normalizeChiefPosition(headPosition);
        return normalizedHeadName + "||" + normalizedHeadLevel + "||" + normalizedHeadPosition;
    }

    private String normalizeChiefName(String headName) {
        if (headName == null) {
            return "";
        }
        return headName.replace('\u3000', ' ')
                .replaceAll("\\s+", "");
    }

    /** 与库内 NORMALIZED_HEAD_POSITION 表达式语义一致，用于 Java 侧分组键与 SQL 对齐 */
    private String normalizeChiefPosition(String headPosition) {
        if (headPosition == null) {
            return "";
        }
        return headPosition.replace('\u3000', ' ')
                .replaceAll("\\s+", "");
    }

    private List<RiverChiefInfoFacilityRespVO> buildFacilities(List<YzRiverChannelManagementDO> records) {
        Map<String, RiverChiefInfoFacilityRespVO> map = new LinkedHashMap<>();
        for (YzRiverChannelManagementDO record : records) {
            ResolvedReference ref = resolveReference(record);
            if (StrUtil.isBlank(ref.referenceType) || ref.referenceId == null) {
                continue;
            }
            String key = ref.referenceType + ":" + ref.referenceId;
            if (map.containsKey(key)) {
                continue;
            }
            RiverChiefInfoFacilityRespVO item = new RiverChiefInfoFacilityRespVO();
            item.setReferenceType(ref.referenceType);
            item.setReferenceTypeLabel(resolveReferenceTypeLabel(ref.referenceType));
            item.setReferenceId(ref.referenceId);
            item.setReferenceName(resolveReferenceName(ref));
            map.put(key, item);
        }
        return new ArrayList<>(map.values());
    }

    /**
     * 兼容历史数据：优先 referenceType/referenceId，否则根据 riverSectionId/waterReservoirId/riverChannelId 推导
     */
    private ResolvedReference resolveReference(YzRiverChannelManagementDO record) {
        ResolvedReference ref = new ResolvedReference();
        String type = StrUtil.trimToNull(record.getReferenceType());
        Long id = record.getReferenceId();
        if (type != null && id != null) {
            ref.referenceType = type.toLowerCase();
            ref.referenceId = id;
            return ref;
        }
        if (record.getRiverSectionId() != null) {
            ref.referenceType = ReferenceTypeConstants.RIVER_SECTION;
            ref.referenceId = record.getRiverSectionId();
            return ref;
        }
        if (record.getWaterReservoirId() != null) {
            ref.referenceType = ReferenceTypeConstants.RESERVOIR;
            ref.referenceId = record.getWaterReservoirId();
            return ref;
        }
        if (record.getRiverChannelId() != null) {
            ref.referenceType = ReferenceTypeConstants.RIVER;
            ref.referenceId = record.getRiverChannelId();
            return ref;
        }
        return ref;
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
        return "-";
    }

    private String resolveReferenceName(ResolvedReference ref) {
        if (ref == null || StrUtil.isBlank(ref.referenceType) || ref.referenceId == null) {
            return "-";
        }
        if (ReferenceTypeConstants.RIVER.equals(ref.referenceType)) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(ref.referenceId);
            return channel == null ? "-" : StrUtil.blankToDefault(channel.getRiverName(), "-");
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(ref.referenceType)) {
            YzRiverSectionDO section = riverSectionMapper.selectById(ref.referenceId);
            if (section == null) {
                return "-";
            }
            String sectionName = StrUtil.blankToDefault(section.getSectionName(), "-");
            String riverName = null;
            if (section.getRiverChannelId() != null) {
                YzRiverChannelDO channel = riverChannelMapper.selectById(section.getRiverChannelId());
                riverName = channel == null ? null : StrUtil.trimToNull(channel.getRiverName());
            }
            return buildSectionDisplayName(riverName, sectionName);
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(ref.referenceType)) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(ref.referenceId);
            return reservoir == null ? "-" : StrUtil.blankToDefault(reservoir.getReservoirName(), "-");
        }
        return "-";
    }

    private String buildSectionDisplayName(String riverName, String sectionName) {
        String section = StrUtil.blankToDefault(StrUtil.trimToNull(sectionName), "-");
        String river = StrUtil.trimToNull(riverName);
        if (StrUtil.isBlank(river)) {
            return section;
        }
        return river + "/" + section;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = StrUtil.trimToNull(value);
        if (normalized == null) {
            throw ServiceExceptionUtil.invalidParamException(message);
        }
        return normalized;
    }

    private List<FacilityRef> normalizeFacilities(List<RiverChiefInfoFacilitySaveReqVO> facilities, boolean required) {
        if (facilities == null) {
            if (required) {
                throw ServiceExceptionUtil.invalidParamException("请至少选择一个关联设施");
            }
            return List.of();
        }
        LinkedHashMap<String, FacilityRef> unique = new LinkedHashMap<>();
        for (RiverChiefInfoFacilitySaveReqVO item : facilities) {
            if (item == null || item.getReferenceId() == null) {
                throw ServiceExceptionUtil.invalidParamException("关联设施ID不能为空");
            }
            String type = StrUtil.trimToNull(item.getReferenceType());
            if (type == null) {
                throw ServiceExceptionUtil.invalidParamException("关联设施类型不能为空");
            }
            type = type.toLowerCase();
            if (!ReferenceTypeConstants.RIVER.equals(type)
                    && !ReferenceTypeConstants.RIVER_SECTION.equals(type)
                    && !ReferenceTypeConstants.RESERVOIR.equals(type)) {
                throw ServiceExceptionUtil.invalidParamException("关联设施类型不合法");
            }
            String key = toFacilityKey(type, item.getReferenceId());
            unique.putIfAbsent(key, new FacilityRef(type, item.getReferenceId()));
        }
        List<FacilityRef> result = new ArrayList<>(unique.values());
        if (required && result.isEmpty()) {
            throw ServiceExceptionUtil.invalidParamException("请至少选择一个关联设施");
        }
        return result;
    }

    private String toFacilityKey(String referenceType, Long referenceId) {
        return referenceType + ":" + referenceId;
    }

    private List<String> toStringList(String[] values) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        return Arrays.stream(values).filter(StrUtil::isNotBlank).toList();
    }

    private String[] toStringArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> cleaned = values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.toArray(String[]::new);
    }

    private void fillTotalChiefRecord(YzRiverChannelManagementDO record, RiverChiefInfoTotalChiefSaveReqVO reqVO) {
        record.setHeadName(normalizeRequired(reqVO.getHeadName(), "河长姓名不能为空"));
        record.setHeadLevel(StrUtil.trimToNull(reqVO.getHeadLevel()));
        record.setHeadPosition(StrUtil.trimToNull(reqVO.getHeadPosition()));
        record.setHeadUnit(null);
        record.setHeadContact(null);
        record.setAdministrativeRegion(null);
        record.setRemarks(null);
        record.setReferenceType(ReferenceTypeConstants.TOTAL_CHIEF);
        record.setReferenceId(null);
        record.setRiverChannelId(null);
        record.setRiverSectionId(null);
        record.setWaterReservoirId(null);
        record.setSectionName(null);
        record.setUserId(null);
        record.setEffectiveFrom(LocalDateTime.now());
    }

    private String toPostgresTextArrayLiteral(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<String> cleaned = values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (cleaned.isEmpty()) {
            return null;
        }
        String joined = cleaned.stream()
                .map(v -> v.replace("\\", "\\\\").replace("\"", "\\\""))
                .map(v -> "\"" + v + "\"")
                .collect(Collectors.joining(","));
        return "{" + joined + "}";
    }

    private String resolveResponsibilitiesForRecord(YzRiverChannelManagementDO record) {
        if (record == null) {
            return null;
        }
        ResolvedReference ref = resolveReference(record);
        if (ref == null || StrUtil.isBlank(ref.referenceType) || ref.referenceId == null) {
            return null;
        }
        FacilityRef facilityRef = new FacilityRef(ref.referenceType, ref.referenceId);
        try {
            return resolveResponsibilitiesForFacility(facilityRef, null, null);
        } catch (Exception ex) {
            return null;
        }
    }

    private String resolveResponsibilitiesForFacility(FacilityRef facility,
                                                      String reservoirResponsibilities,
                                                      String fallbackResponsibilities) {
        if (facility == null || StrUtil.isBlank(facility.referenceType)) {
            return StrUtil.trimToNull(fallbackResponsibilities);
        }
        String type = StrUtil.trimToEmpty(facility.referenceType).toLowerCase();
        if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            return reservoirResponsibilities != null
                    ? StrUtil.trimToNull(reservoirResponsibilities)
                    : StrUtil.trimToNull(fallbackResponsibilities);
        }
        if (ReferenceTypeConstants.RIVER.equals(type)) {
            return resolveRiverResponsibilitiesByChannelId(facility.referenceId, true);
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            return resolveRiverResponsibilitiesBySectionId(facility.referenceId, true);
        }
        return StrUtil.trimToNull(fallbackResponsibilities);
    }

    private String resolveRiverResponsibilitiesByChannelId(Long riverChannelId, boolean strict) {
        if (riverChannelId == null) {
            return null;
        }
        YzRiverChannelDO channel = riverChannelMapper.selectById(riverChannelId);
        if (channel == null) {
            if (strict) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }
            return null;
        }
        return StrUtil.trimToNull(channel.getResponsibilities());
    }

    private String resolveRiverResponsibilitiesBySectionId(Long sectionId, boolean strict) {
        if (sectionId == null) {
            return null;
        }
        YzRiverSectionDO section = riverSectionMapper.selectById(sectionId);
        if (section == null) {
            if (strict) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            return null;
        }
        return resolveRiverResponsibilitiesByChannelId(section.getRiverChannelId(), strict);
    }

    private static class ResolvedReference {
        private String referenceType;
        private Long referenceId;
    }

    private static class FacilityRef {
        private final String referenceType;
        private final Long referenceId;

        private FacilityRef(String referenceType, Long referenceId) {
            this.referenceType = referenceType;
            this.referenceId = referenceId;
        }
    }
}
