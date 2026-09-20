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
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefUserSyncRespVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 河长管理服务（统一维护河道/河段/水库的河长信息）
 *
 * <p>设计说明：</p>
 * <ul>
 *     <li>只查询当前有效记录：effectiveTo 为空（并兼容 isCurrent=1）</li>
 *     <li>新增/编辑/删除会按“关联对象”生成新版本：失效旧版本（effectiveTo=now），插入新版本记录</li>
 *     <li>兼容历史数据：referenceType/referenceId 为空时，根据 riverChannelId/riverSectionId/waterReservoirId 推导关联对象</li>
 * </ul>
 */
@Service
@Validated
public class RiverChiefManagementService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzRiverChannelManagementMapper managementMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final DictDataCommonApi dictDataApi;
    private final RiverChiefUserAccountService riverChiefUserAccountService;

    public RiverChiefManagementService(YzRiverChannelManagementMapper managementMapper,
                                       YzRiverChannelMapper riverChannelMapper,
                                       YzRiverSectionMapper riverSectionMapper,
                                       YzWaterReservoirMapper waterReservoirMapper,
                                       DictDataCommonApi dictDataApi,
                                       RiverChiefUserAccountService riverChiefUserAccountService) {
        this.managementMapper = managementMapper;
        this.riverChannelMapper = riverChannelMapper;
        this.riverSectionMapper = riverSectionMapper;
        this.waterReservoirMapper = waterReservoirMapper;
        this.dictDataApi = dictDataApi;
        this.riverChiefUserAccountService = riverChiefUserAccountService;
    }

    /**
     * 分页查询（仅当前有效）
     */
    public PageResult<RiverChiefManagementPageRespVO> getPage(RiverChiefManagementPageReqVO reqVO) {
        LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper = new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                .like(StrUtil.isNotBlank(reqVO.getHeadName()), YzRiverChannelManagementDO::getHeadName, reqVO.getHeadName())
                .eq(StrUtil.isNotBlank(reqVO.getHeadLevel()), YzRiverChannelManagementDO::getHeadLevel, reqVO.getHeadLevel())
                .orderByDesc(YzRiverChannelManagementDO::getEffectiveFrom)
                .orderByDesc(YzRiverChannelManagementDO::getId);
        if (reqVO.getAdministrativeRegion() != null && !reqVO.getAdministrativeRegion().isEmpty()) {
            wrapper.apply("administrative_region && {0}::text[]", toPostgresTextArrayLiteral(reqVO.getAdministrativeRegion()));
        }
        applyReferenceFilter(wrapper, reqVO.getReferenceType(), reqVO.getReferenceName());

        PageResult<YzRiverChannelManagementDO> page = managementMapper.selectPage(reqVO, wrapper);
        List<RiverChiefManagementPageRespVO> list = page.getList().stream().map(this::toPageRespVO).collect(Collectors.toList());
        fillReferenceDisplay(list, page.getList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 详情
     */
    public RiverChiefManagementDetailRespVO getDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        YzRiverChannelManagementDO record = managementMapper.selectById(id);
        if (record == null || record.getEffectiveTo() != null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }

        RiverChiefManagementDetailRespVO vo = new RiverChiefManagementDetailRespVO();
        vo.setId(record.getId());
        vo.setRiverChannelId(record.getRiverChannelId());
        vo.setRiverSectionId(record.getRiverSectionId());
        vo.setWaterReservoirId(record.getWaterReservoirId());
        vo.setHeadName(record.getHeadName());
        vo.setHeadLevel(record.getHeadLevel());
        vo.setHeadPosition(record.getHeadPosition());
        vo.setHeadUnit(record.getHeadUnit());
        vo.setHeadContact(record.getHeadContact());
        String responsibilities = null;
        if (record.getRiverChannelId() != null) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(record.getRiverChannelId());
            responsibilities = channel == null ? null : channel.getResponsibilities();
        } else if (record.getWaterReservoirId() != null) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(record.getWaterReservoirId());
            responsibilities = reservoir == null ? null : reservoir.getResponsibilities();
        }
        vo.setResponsibilities(responsibilities);
        vo.setRemarks(record.getRemarks());
        vo.setEffectiveFrom(record.getEffectiveFrom());
        vo.setAdministrativeRegion(toStringList(record.getAdministrativeRegion()));

        ResolvedReference ref = resolveReference(record);
        vo.setReferenceType(ref.referenceType);
        vo.setReferenceId(ref.referenceId);
        vo.setReferenceTypeLabel(resolveReferenceTypeLabel(ref.referenceType));
        vo.setReferenceName(resolveReferenceName(ref));
        return vo;
    }

    /**
     * 新增（按关联对象生成新版本）
     *
     * @return 新增河长记录在新版本中的主键 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(@Valid RiverChiefManagementSaveReqVO reqVO) {
        return create(reqVO, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(@Valid RiverChiefManagementSaveReqVO reqVO, LocalDateTime batchEffectiveFrom) {
        FacilityKey key = resolveFacilityKey(reqVO.getReferenceType(), reqVO.getReferenceId());
        List<HeadItem> current = selectCurrentHeads(key);

        List<HeadItem> next = new ArrayList<>(current);
        next.add(buildHeadItemFromReq(reqVO));

        LocalDateTime effectiveFrom = resolveEffectiveFrom(reqVO.getEffectiveFrom(), batchEffectiveFrom);
        int nextVersionNo = selectNextVersionNo(key);
        expireCurrent(key, effectiveFrom);
        if (next.isEmpty()) {
            return null;
        }
        Long createdId = insertNewVersion(key, next, effectiveFrom, nextVersionNo, reqVO.getHeadName());
        // 按需求临时关闭：新增/编辑河长不再自动同步创建系统用户（AdminUserDO）
        // riverChiefUserAccountService.syncAndBindStrict(selectCurrentRecords(key));
        return createdId;
    }

    /**
     * 编辑（支持重新关联）
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(@Valid RiverChiefManagementSaveReqVO reqVO) {
        update(reqVO, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(@Valid RiverChiefManagementSaveReqVO reqVO, LocalDateTime batchEffectiveFrom) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        YzRiverChannelManagementDO record = managementMapper.selectById(reqVO.getId());
        if (record == null || record.getEffectiveTo() != null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }

        FacilityKey oldKey = resolveFacilityKeyFromRecord(record);
        FacilityKey newKey = resolveFacilityKey(reqVO.getReferenceType(), reqVO.getReferenceId());
        HeadItem updated = buildHeadItemFromReq(reqVO);
        LocalDateTime effectiveFrom = resolveEffectiveFrom(reqVO.getEffectiveFrom(), batchEffectiveFrom);

        if (oldKey.equals(newKey)) {
            updateWithinSameFacility(oldKey, record.getId(), updated, effectiveFrom);
            return;
        }

        // 重新关联：旧关联对象移除该河长，新关联对象追加该河长（各自生成新版本）
        removeFromFacility(oldKey, record.getId());
        appendToFacility(newKey, updated, effectiveFrom);
        // 按需求临时关闭：新增/编辑河长不再自动同步创建系统用户（AdminUserDO）
        // riverChiefUserAccountService.syncAndBindStrict(selectCurrentRecords(oldKey));
        // riverChiefUserAccountService.syncAndBindStrict(selectCurrentRecords(newKey));
    }

    /**
     * 删除（仅删除当前有效记录，按关联对象生成新版本）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        YzRiverChannelManagementDO record = managementMapper.selectById(id);
        if (record == null || record.getEffectiveTo() != null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }
        FacilityKey key = resolveFacilityKeyFromRecord(record);
        removeFromFacility(key, id);
    }

    /**
     * 导出（仅当前有效）
     */
    public List<RiverChiefManagementExportExcelVO> getExportList(RiverChiefManagementPageReqVO reqVO) {
        LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper = new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                .like(StrUtil.isNotBlank(reqVO.getHeadName()), YzRiverChannelManagementDO::getHeadName, reqVO.getHeadName())
                .eq(StrUtil.isNotBlank(reqVO.getHeadLevel()), YzRiverChannelManagementDO::getHeadLevel, reqVO.getHeadLevel())
                .orderByDesc(YzRiverChannelManagementDO::getEffectiveFrom)
                .orderByDesc(YzRiverChannelManagementDO::getId);
        if (reqVO.getAdministrativeRegion() != null && !reqVO.getAdministrativeRegion().isEmpty()) {
            wrapper.apply("administrative_region && {0}::text[]", toPostgresTextArrayLiteral(reqVO.getAdministrativeRegion()));
        }
        applyReferenceFilter(wrapper, reqVO.getReferenceType(), reqVO.getReferenceName());
        List<YzRiverChannelManagementDO> list = managementMapper.selectList(wrapper);

        Map<String, String> headLevelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);
        Map<String, String> referenceNameMap = buildReferenceNameMap(list);
        Map<Long, String> riverResponsibilitiesMap = buildRiverResponsibilitiesMap(list);
        Map<Long, String> reservoirResponsibilitiesMap = buildReservoirResponsibilitiesMap(list);

        return list.stream().map(item -> {
            ResolvedReference ref = resolveReference(item);
            RiverChiefManagementExportExcelVO vo = new RiverChiefManagementExportExcelVO();
            vo.setHeadName(item.getHeadName());
            vo.setHeadLevelLabel(resolveLabel(item.getHeadLevel(), headLevelMap));
            vo.setReferenceTypeLabel(resolveReferenceTypeLabel(ref.referenceType));
            vo.setReferenceName(referenceNameMap.getOrDefault(ref.key(), "-"));
            vo.setHeadPosition(StrUtil.blankToDefault(item.getHeadPosition(), "-"));
            vo.setHeadUnit(StrUtil.blankToDefault(item.getHeadUnit(), "-"));
            vo.setHeadContact(StrUtil.blankToDefault(item.getHeadContact(), "-"));
            String responsibilities = item.getWaterReservoirId() != null
                    ? reservoirResponsibilitiesMap.get(item.getWaterReservoirId())
                    : riverResponsibilitiesMap.get(item.getRiverChannelId());
            vo.setResponsibilities(StrUtil.blankToDefault(responsibilities, "-"));
            return vo;
        }).toList();
    }

    /**
     * 同步当前生效的河长账号：
     * <ul>
     *     <li>按河长联系电话去重生成账号（username/mobile）</li>
     *     <li>按 headLevel -> system_role.code 匹配角色并赋权</li>
     *     <li>新建/更新均重置密码为 123456</li>
     *     <li>回写 yz_river_channel_management.user_id 绑定到 system_users.id</li>
     * </ul>
     *
     */
    public RiverChiefUserSyncRespVO syncCurrentChiefUsers() {
        return riverChiefUserAccountService.syncAndBindCurrentChiefUsers();
    }

    // region 内部方法：分页/显示

    private RiverChiefManagementPageRespVO toPageRespVO(YzRiverChannelManagementDO record) {
        RiverChiefManagementPageRespVO vo = new RiverChiefManagementPageRespVO();
        vo.setId(record.getId());
        vo.setHeadName(record.getHeadName());
        vo.setHeadLevel(record.getHeadLevel());
        vo.setEffectiveFrom(record.getEffectiveFrom());
        vo.setHeadContact(record.getHeadContact());
        vo.setAdministrativeRegion(record.getAdministrativeRegion() == null ? null : Arrays.asList(record.getAdministrativeRegion()));
        return vo;
    }

    private static String toPostgresTextArrayLiteral(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "{}";
        }
        List<String> cleaned = values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .toList();
        if (cleaned.isEmpty()) {
            return "{}";
        }
        String joined = cleaned.stream()
                .map(v -> v.replace("\\", "\\\\").replace("\"", "\\\""))
                .map(v -> "\"" + v + "\"")
                .collect(Collectors.joining(","));
        return "{" + joined + "}";
    }

    private void applyReferenceFilter(LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper,
                                      String referenceType,
                                      String referenceName) {
        String type = StrUtil.trimToNull(referenceType);
        String name = StrUtil.trimToNull(referenceName);
        if (StrUtil.isNotBlank(type) && StrUtil.isBlank(name)) {
            applyReferenceTypeOnly(wrapper, type);
            return;
        }
        if (StrUtil.isBlank(name)) {
            return;
        }
        Map<String, List<Long>> idsByType = loadReferenceIdsByName(type, name);
        if (StrUtil.isNotBlank(type)) {
            if (ReferenceTypeConstants.RIVER.equals(type)) {
                List<Long> riverIds = idsByType.getOrDefault(ReferenceTypeConstants.RIVER, List.of());
                List<Long> sectionIds = idsByType.getOrDefault(ReferenceTypeConstants.RIVER_SECTION, List.of());
                if (riverIds.isEmpty() && sectionIds.isEmpty()) {
                    wrapper.apply("1=0");
                    return;
                }
                wrapper.and(w -> {
                    boolean appended = false;
                    if (!riverIds.isEmpty()) {
                        applyTypeIdsCondition(w, ReferenceTypeConstants.RIVER, riverIds);
                        appended = true;
                    }
                    if (!sectionIds.isEmpty()) {
                        if (appended) {
                            w.or();
                        }
                        applyTypeIdsCondition(w, ReferenceTypeConstants.RIVER_SECTION, sectionIds);
                    }
                });
                return;
            }
            List<Long> ids = idsByType.getOrDefault(type, List.of());
            if (ids.isEmpty()) {
                wrapper.apply("1=0");
                return;
            }
            applyReferenceTypeWithIds(wrapper, type, ids);
            return;
        }
        List<Long> riverIds = idsByType.getOrDefault(ReferenceTypeConstants.RIVER, List.of());
        List<Long> sectionIds = idsByType.getOrDefault(ReferenceTypeConstants.RIVER_SECTION, List.of());
        List<Long> reservoirIds = idsByType.getOrDefault(ReferenceTypeConstants.RESERVOIR, List.of());
        if (riverIds.isEmpty() && sectionIds.isEmpty() && reservoirIds.isEmpty()) {
            wrapper.apply("1=0");
            return;
        }
        wrapper.and(w -> {
            boolean appended = false;
            if (!riverIds.isEmpty()) {
                applyTypeIdsCondition(w, ReferenceTypeConstants.RIVER, riverIds);
                appended = true;
            }
            if (!sectionIds.isEmpty()) {
                if (appended) {
                    w.or();
                }
                applyTypeIdsCondition(w, ReferenceTypeConstants.RIVER_SECTION, sectionIds);
                appended = true;
            }
            if (!reservoirIds.isEmpty()) {
                if (appended) {
                    w.or();
                }
                applyTypeIdsCondition(w, ReferenceTypeConstants.RESERVOIR, reservoirIds);
            }
        });
    }

    private void applyReferenceTypeOnly(LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper, String referenceType) {
        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            wrapper.and(w -> w.eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RIVER)
                    .isNotNull(YzRiverChannelManagementDO::getReferenceId)
                    .or()
                    .isNull(YzRiverChannelManagementDO::getReferenceId)
                    .isNotNull(YzRiverChannelManagementDO::getRiverChannelId));
            return;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            wrapper.and(w -> w.eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RIVER_SECTION)
                    .isNotNull(YzRiverChannelManagementDO::getReferenceId)
                    .or()
                    .isNull(YzRiverChannelManagementDO::getReferenceId)
                    .isNotNull(YzRiverChannelManagementDO::getRiverSectionId));
            return;
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            wrapper.and(w -> w.eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RESERVOIR)
                    .isNotNull(YzRiverChannelManagementDO::getReferenceId)
                    .or()
                    .isNull(YzRiverChannelManagementDO::getReferenceId)
                    .isNotNull(YzRiverChannelManagementDO::getWaterReservoirId));
        }
    }

    private void applyReferenceTypeWithIds(LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper,
                                           String referenceType,
                                           List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            wrapper.apply("1=0");
            return;
        }
        wrapper.and(w -> applyTypeIdsCondition(w, referenceType, ids));
    }

    private void applyTypeIdsCondition(LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper,
                                       String referenceType,
                                       List<Long> ids) {
        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RIVER)
                    .in(YzRiverChannelManagementDO::getReferenceId, ids)
                    .or()
                    .isNull(YzRiverChannelManagementDO::getReferenceId)
                    .in(YzRiverChannelManagementDO::getRiverChannelId, ids);
            return;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RIVER_SECTION)
                    .in(YzRiverChannelManagementDO::getReferenceId, ids)
                    .or()
                    .isNull(YzRiverChannelManagementDO::getReferenceId)
                    .in(YzRiverChannelManagementDO::getRiverSectionId, ids);
            return;
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RESERVOIR)
                    .in(YzRiverChannelManagementDO::getReferenceId, ids)
                    .or()
                    .isNull(YzRiverChannelManagementDO::getReferenceId)
                    .in(YzRiverChannelManagementDO::getWaterReservoirId, ids);
        }
    }

    private Map<String, List<Long>> loadReferenceIdsByName(String referenceType, String referenceName) {
        Map<String, List<Long>> result = new HashMap<>();
        String type = StrUtil.trimToNull(referenceType);
        String keyword = StrUtil.trimToNull(referenceName);
        if (StrUtil.isBlank(keyword)) {
            return result;
        }
        if (StrUtil.isNotBlank(type)) {
            if (ReferenceTypeConstants.RIVER.equals(type)) {
                List<Long> riverIds = queryReferenceIds(ReferenceTypeConstants.RIVER, keyword);
                result.put(ReferenceTypeConstants.RIVER, riverIds);
                result.put(ReferenceTypeConstants.RIVER_SECTION, querySectionIdsByRiverIds(riverIds));
                return result;
            }
            result.put(type, queryReferenceIds(type, keyword));
            return result;
        }
        result.put(ReferenceTypeConstants.RIVER, queryReferenceIds(ReferenceTypeConstants.RIVER, keyword));
        result.put(ReferenceTypeConstants.RIVER_SECTION, queryReferenceIds(ReferenceTypeConstants.RIVER_SECTION, keyword));
        result.put(ReferenceTypeConstants.RESERVOIR, queryReferenceIds(ReferenceTypeConstants.RESERVOIR, keyword));
        return result;
    }

    /**
     * 依据河道ID集合查询其下全部河段ID。
     */
    private List<Long> querySectionIdsByRiverIds(List<Long> riverIds) {
        if (CollUtil.isEmpty(riverIds)) {
            return List.of();
        }
        return riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                        .select(YzRiverSectionDO::getId)
                        .in(YzRiverSectionDO::getRiverChannelId, riverIds))
                .stream()
                .map(YzRiverSectionDO::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Long> queryReferenceIds(String referenceType, String keyword) {
        if (StrUtil.isBlank(referenceType) || StrUtil.isBlank(keyword)) {
            return List.of();
        }
        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            return riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                            .select(YzRiverChannelDO::getId)
                            .like(YzRiverChannelDO::getRiverName, keyword))
                    .stream()
                    .map(YzRiverChannelDO::getId)
                    .filter(Objects::nonNull)
                    .toList();
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            return riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                            .select(YzRiverSectionDO::getId)
                            .like(YzRiverSectionDO::getSectionName, keyword))
                    .stream()
                    .map(YzRiverSectionDO::getId)
                    .filter(Objects::nonNull)
                    .toList();
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            return waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                            .select(YzWaterReservoirDO::getId)
                            .like(YzWaterReservoirDO::getReservoirName, keyword))
                    .stream()
                    .map(YzWaterReservoirDO::getId)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    private void fillReferenceDisplay(List<RiverChiefManagementPageRespVO> voList,
                                      List<YzRiverChannelManagementDO> records) {
        if (voList == null || voList.isEmpty() || records == null || records.isEmpty()) {
            return;
        }
        Map<String, String> referenceNameMap = buildReferenceNameMap(records);
        for (int i = 0; i < voList.size(); i++) {
            RiverChiefManagementPageRespVO vo = voList.get(i);
            YzRiverChannelManagementDO record = records.get(i);
            ResolvedReference ref = resolveReference(record);
            vo.setReferenceType(ref.referenceType);
            vo.setReferenceTypeLabel(resolveReferenceTypeLabel(ref.referenceType));
            vo.setReferenceName(referenceNameMap.getOrDefault(ref.key(), "-"));
        }
    }

    private Map<String, String> buildReferenceNameMap(List<YzRiverChannelManagementDO> records) {
        Map<String, List<Long>> idsByType = new HashMap<>();
        for (YzRiverChannelManagementDO record : records) {
            ResolvedReference ref = resolveReference(record);
            if (ref.referenceId == null || StrUtil.isBlank(ref.referenceType)) {
                continue;
            }
            idsByType.computeIfAbsent(ref.referenceType, k -> new ArrayList<>()).add(ref.referenceId);
        }

        Map<String, String> result = new HashMap<>();
        List<Long> riverIds = distinct(idsByType.get(ReferenceTypeConstants.RIVER));
        if (!riverIds.isEmpty()) {
            List<YzRiverChannelDO> rivers = riverChannelMapper.selectBatchIds(riverIds);
            for (YzRiverChannelDO river : rivers) {
                if (river == null || river.getId() == null) {
                    continue;
                }
                result.put(ResolvedReference.key(ReferenceTypeConstants.RIVER, river.getId()),
                        StrUtil.blankToDefault(river.getRiverName(), "-"));
            }
        }

        List<Long> sectionIds = distinct(idsByType.get(ReferenceTypeConstants.RIVER_SECTION));
        if (!sectionIds.isEmpty()) {
            List<YzRiverSectionDO> sections = riverSectionMapper.selectBatchIds(sectionIds);
            List<Long> sectionRiverIds = sections.stream()
                    .map(YzRiverSectionDO::getRiverChannelId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Map<Long, String> sectionRiverNameMap = new HashMap<>();
            if (!sectionRiverIds.isEmpty()) {
                List<YzRiverChannelDO> sectionRivers = riverChannelMapper.selectBatchIds(sectionRiverIds);
                for (YzRiverChannelDO river : sectionRivers) {
                    if (river == null || river.getId() == null) {
                        continue;
                    }
                    sectionRiverNameMap.put(river.getId(), StrUtil.blankToDefault(river.getRiverName(), "-"));
                }
            }
            for (YzRiverSectionDO section : sections) {
                if (section == null || section.getId() == null) {
                    continue;
                }
                String sectionName = StrUtil.blankToDefault(section.getSectionName(), "-");
                String riverName = sectionRiverNameMap.get(section.getRiverChannelId());
                result.put(ResolvedReference.key(ReferenceTypeConstants.RIVER_SECTION, section.getId()),
                        buildSectionDisplayName(riverName, sectionName));
            }
        }

        List<Long> reservoirIds = distinct(idsByType.get(ReferenceTypeConstants.RESERVOIR));
        if (!reservoirIds.isEmpty()) {
            List<YzWaterReservoirDO> reservoirs = waterReservoirMapper.selectBatchIds(reservoirIds);
            for (YzWaterReservoirDO reservoir : reservoirs) {
                if (reservoir == null || reservoir.getId() == null) {
                    continue;
                }
                result.put(ResolvedReference.key(ReferenceTypeConstants.RESERVOIR, reservoir.getId()),
                        StrUtil.blankToDefault(reservoir.getReservoirName(), "-"));
            }
        }

        return result;
    }

    private static List<Long> distinct(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().filter(Objects::nonNull).distinct().toList();
    }

    // endregion

    // region 内部方法：版本化保存

    private void updateWithinSameFacility(FacilityKey key, Long id, HeadItem updated, LocalDateTime effectiveFrom) {
        List<YzRiverChannelManagementDO> currentRecords = selectCurrentRecords(key);
        boolean exists = currentRecords.stream().anyMatch(it -> Objects.equals(it.getId(), id));
        if (!exists) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }

        List<HeadItem> next = currentRecords.stream()
                .map(this::buildHeadItemFromRecord)
                .collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < next.size(); i++) {
            if (Objects.equals(currentRecords.get(i).getId(), id)) {
                next.set(i, updated);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        int nextVersionNo = selectNextVersionNo(key);
        expireCurrent(key, now);
        if (next.isEmpty()) {
            return;
        }
        insertNewVersion(key, next, effectiveFrom, nextVersionNo, null);
        // 按需求临时关闭：新增/编辑河长不再自动同步创建系统用户（AdminUserDO）
        // riverChiefUserAccountService.syncAndBindStrict(selectCurrentRecords(key));
    }

    private void removeFromFacility(FacilityKey key, Long id) {
        List<YzRiverChannelManagementDO> currentRecords = selectCurrentRecords(key);
        boolean exists = currentRecords.stream().anyMatch(it -> Objects.equals(it.getId(), id));
        if (!exists) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHIEF_NOT_EXISTS);
        }

        List<HeadItem> next = new ArrayList<>();
        for (YzRiverChannelManagementDO it : currentRecords) {
            if (Objects.equals(it.getId(), id)) {
                continue;
            }
            next.add(buildHeadItemFromRecord(it));
        }

        LocalDateTime now = LocalDateTime.now();
        int nextVersionNo = selectNextVersionNo(key);
        expireCurrent(key, now);
        if (next.isEmpty()) {
            return;
        }
        insertNewVersion(key, next, now, nextVersionNo, null);
    }

    private void appendToFacility(FacilityKey key, HeadItem item, LocalDateTime effectiveFrom) {
        List<HeadItem> current = selectCurrentHeads(key);
        List<HeadItem> next = new ArrayList<>(current);
        next.add(item);

        LocalDateTime now = LocalDateTime.now();
        int nextVersionNo = selectNextVersionNo(key);
        expireCurrent(key, now);
        if (next.isEmpty()) {
            return;
        }
        insertNewVersion(key, next, effectiveFrom, nextVersionNo, null);
    }

    private List<HeadItem> selectCurrentHeads(FacilityKey key) {
        return selectCurrentRecords(key).stream().map(this::buildHeadItemFromRecord).toList();
    }

    private List<YzRiverChannelManagementDO> selectCurrentRecords(FacilityKey key) {
        return managementMapper.selectList(buildFacilityWrapper(key)
                .isNull(YzRiverChannelManagementDO::getEffectiveTo)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                .orderByAsc(YzRiverChannelManagementDO::getId));
    }

    private void expireCurrent(FacilityKey key, LocalDateTime now) {
        List<YzRiverChannelManagementDO> currentRecords = selectCurrentRecords(key);
        if (currentRecords.isEmpty()) {
            return;
        }
        managementMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                .set(YzRiverChannelManagementDO::getEffectiveTo, now)
                .set(YzRiverChannelManagementDO::getIsCurrent, 0)
                .in(YzRiverChannelManagementDO::getId, currentRecords.stream().map(YzRiverChannelManagementDO::getId).toList()));
    }

    private int selectNextVersionNo(FacilityKey key) {
        YzRiverChannelManagementDO max = managementMapper.selectOne(buildFacilityWrapper(key)
                .select(YzRiverChannelManagementDO::getVersionNo)
                .orderByDesc(YzRiverChannelManagementDO::getVersionNo)
                .last("LIMIT 1"));
        int maxVersion = max == null || max.getVersionNo() == null ? 0 : max.getVersionNo();
        return maxVersion + 1;
    }

    /**
     * 插入新版本记录
     *
     * @param newCreatedHeadName 用于定位新增的河长（创建场景），可为空
     * @return 若传入 newCreatedHeadName，则返回匹配的记录 ID，否则返回 null
     */
    private Long insertNewVersion(FacilityKey key, List<HeadItem> heads, LocalDateTime effectiveFrom, int versionNo, String newCreatedHeadName) {
        ReferenceDetail detail = loadReferenceDetail(key);
        Long createdId = null;
        for (HeadItem head : heads) {
            YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
            record.setId(SNOWFLAKE.nextId());
            record.setRiverChannelId(detail.riverChannelId);
            record.setRiverSectionId(detail.riverSectionId);
            record.setWaterReservoirId(detail.waterReservoirId);
            record.setReferenceType(detail.referenceType);
            record.setReferenceId(detail.referenceId);
            record.setSectionName(detail.referenceName);
            record.setHeadLevel(StrUtil.trimToNull(head.headLevel));
            record.setHeadPosition(StrUtil.trimToNull(head.headPosition));
            record.setHeadUnit(StrUtil.trimToNull(head.headUnit));
            record.setHeadName(StrUtil.trimToNull(head.headName));
            record.setHeadContact(StrUtil.trimToNull(head.headContact));
            record.setRemarks(StrUtil.trimToNull(head.remarks));
            record.setAdministrativeRegion(toStringArray(head.administrativeRegion));
            record.setVersionNo(versionNo);
            record.setEffectiveFrom(effectiveFrom);
            record.setEffectiveTo(null);
            record.setIsCurrent(1);
            managementMapper.insert(record);

            if (createdId == null && StrUtil.isNotBlank(newCreatedHeadName) && StrUtil.equals(newCreatedHeadName, record.getHeadName())) {
                createdId = record.getId();
            }
        }
        return createdId;
    }

    private LocalDateTime resolveEffectiveFrom(LocalDate effectiveFrom, LocalDateTime batchEffectiveFrom) {
        return batchEffectiveFrom != null ? batchEffectiveFrom : LocalDateTime.now();
    }

    private HeadItem buildHeadItemFromReq(RiverChiefManagementSaveReqVO reqVO) {
        HeadItem item = new HeadItem();
        item.headName = StrUtil.trimToNull(reqVO.getHeadName());
        item.headLevel = StrUtil.trimToNull(reqVO.getHeadLevel());
        item.headPosition = StrUtil.trimToNull(reqVO.getHeadPosition());
        item.headUnit = StrUtil.trimToNull(reqVO.getHeadUnit());
        item.headContact = null;
        item.remarks = StrUtil.trimToNull(reqVO.getRemarks());
        item.administrativeRegion = normalizeRegionList(reqVO.getAdministrativeRegion());
        return item;
    }

    /**
     * 构建“河道 -> 河长职责”映射（用于导出时避免逐条查询）。
     */
    private Map<Long, String> buildRiverResponsibilitiesMap(List<YzRiverChannelManagementDO> records) {
        if (CollUtil.isEmpty(records)) {
            return Map.of();
        }
        java.util.Set<Long> channelIds = records.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getWaterReservoirId() == null)
                .map(YzRiverChannelManagementDO::getRiverChannelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (channelIds.isEmpty()) {
            return Map.of();
        }
        return riverChannelMapper.selectBatchIds(channelIds).stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzRiverChannelDO::getId, YzRiverChannelDO::getResponsibilities, (a, b) -> a));
    }

    /**
     * 构建“水库 -> 河长职责”映射（导出时使用，职责来源改为水库基础信息）。 
     */
    private Map<Long, String> buildReservoirResponsibilitiesMap(List<YzRiverChannelManagementDO> records) {
        if (CollUtil.isEmpty(records)) {
            return Map.of();
        }
        java.util.Set<Long> reservoirIds = records.stream()
                .filter(Objects::nonNull)
                .map(YzRiverChannelManagementDO::getWaterReservoirId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (reservoirIds.isEmpty()) {
            return Map.of();
        }
        return waterReservoirMapper.selectBatchIds(reservoirIds).stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzWaterReservoirDO::getId, YzWaterReservoirDO::getResponsibilities, (a, b) -> a));
    }

    private HeadItem buildHeadItemFromRecord(YzRiverChannelManagementDO record) {
        HeadItem item = new HeadItem();
        item.headName = record.getHeadName();
        item.headLevel = record.getHeadLevel();
        item.headPosition = record.getHeadPosition();
        item.headUnit = record.getHeadUnit();
        item.headContact = null;
        item.remarks = record.getRemarks();
        item.administrativeRegion = toStringList(record.getAdministrativeRegion());
        return item;
    }

    private LambdaQueryWrapper<YzRiverChannelManagementDO> buildFacilityWrapper(FacilityKey key) {
        LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper = new LambdaQueryWrapper<>();
        if (ReferenceTypeConstants.RESERVOIR.equals(key.referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getWaterReservoirId, key.referenceId)
                    .isNull(YzRiverChannelManagementDO::getRiverChannelId)
                    .isNull(YzRiverChannelManagementDO::getRiverSectionId);
            return wrapper;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(key.referenceType)) {
            wrapper.eq(YzRiverChannelManagementDO::getRiverChannelId, key.riverChannelId)
                    .eq(YzRiverChannelManagementDO::getRiverSectionId, key.referenceId)
                    .isNull(YzRiverChannelManagementDO::getWaterReservoirId);
            return wrapper;
        }
        wrapper.eq(YzRiverChannelManagementDO::getRiverChannelId, key.referenceId)
                .isNull(YzRiverChannelManagementDO::getRiverSectionId)
                .isNull(YzRiverChannelManagementDO::getWaterReservoirId);
        return wrapper;
    }

    private FacilityKey resolveFacilityKeyFromRecord(YzRiverChannelManagementDO record) {
        ResolvedReference ref = resolveReference(record);
        String type = StrUtil.trimToNull(ref.referenceType);
        if (type == null || ref.referenceId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
        }
        type = type.toLowerCase();

        FacilityKey key = new FacilityKey();
        key.referenceType = type;
        key.referenceId = ref.referenceId;

        if (ReferenceTypeConstants.RIVER.equals(type)) {
            key.riverChannelId = record.getRiverChannelId() != null ? record.getRiverChannelId() : ref.referenceId;
            return key;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            if (record.getRiverChannelId() != null) {
                key.riverChannelId = record.getRiverChannelId();
                return key;
            }
            return resolveFacilityKey(type, ref.referenceId);
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            return key;
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
    }

    private FacilityKey resolveFacilityKey(String referenceType, Long referenceId) {
        String type = StrUtil.trimToNull(referenceType);
        if (type == null || referenceId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
        }
        type = type.toLowerCase();
        FacilityKey key = new FacilityKey();
        key.referenceType = type;
        key.referenceId = referenceId;

        if (ReferenceTypeConstants.RIVER.equals(type)) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(referenceId);
            if (channel == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }
            key.riverChannelId = channel.getId();
            return key;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            YzRiverSectionDO section = riverSectionMapper.selectById(referenceId);
            if (section == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            key.riverChannelId = section.getRiverChannelId();
            return key;
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(referenceId);
            if (reservoir == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
            }
            return key;
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
    }

    private ReferenceDetail loadReferenceDetail(FacilityKey key) {
        ReferenceDetail detail = new ReferenceDetail();
        detail.referenceType = key.referenceType;
        detail.referenceId = key.referenceId;

        if (ReferenceTypeConstants.RIVER.equals(key.referenceType)) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(key.referenceId);
            if (channel == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }
            detail.riverChannelId = channel.getId();
            detail.referenceName = StrUtil.blankToDefault(channel.getRiverName(), "");
            return detail;
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(key.referenceType)) {
            YzRiverSectionDO section = riverSectionMapper.selectById(key.referenceId);
            if (section == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            detail.riverChannelId = section.getRiverChannelId();
            detail.riverSectionId = section.getId();
            detail.referenceName = StrUtil.blankToDefault(section.getSectionName(), "");
            return detail;
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(key.referenceType)) {
            YzWaterReservoirDO reservoir = waterReservoirMapper == null ? null : waterReservoirMapper.selectById(key.referenceId);
            detail.waterReservoirId = key.referenceId;
            detail.referenceName = reservoir == null
                    ? ""
                    : StrUtil.blankToDefault(reservoir.getReservoirName(), "");
            return detail;
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_WATER_FACILITY_NOT_EXISTS);
    }

    private static class FacilityKey {
        private String referenceType;
        private Long referenceId;
        private Long riverChannelId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FacilityKey that = (FacilityKey) o;
            return Objects.equals(referenceType, that.referenceType)
                    && Objects.equals(referenceId, that.referenceId)
                    && Objects.equals(riverChannelId, that.riverChannelId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(referenceType, referenceId, riverChannelId);
        }
    }

    private static class ReferenceDetail {
        private String referenceType;
        private Long referenceId;
        private String referenceName;
        private Long riverChannelId;
        private Long riverSectionId;
        private Long waterReservoirId;
    }

    private static class HeadItem {
        private String headLevel;
        private String headPosition;
        private String headUnit;
        private String headName;
        private String headContact;
        private String remarks;
        private List<String> administrativeRegion;
    }

    // endregion

    // region 内部方法：历史数据兼容 + 显示辅助

    private static class ResolvedReference {
        private String referenceType;
        private Long referenceId;

        private String key() {
            return key(referenceType, referenceId);
        }

        private static String key(String type, Long id) {
            if (StrUtil.isBlank(type) || id == null) {
                return "";
            }
            return type + ":" + id;
        }
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

    private String resolveReferenceTypeLabel(String referenceType) {
        String type = StrUtil.trimToEmpty(referenceType).toLowerCase();
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
        List<String> normalized = values.stream().filter(StrUtil::isNotBlank).distinct().toList();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.toArray(new String[0]);
    }

    private List<String> normalizeRegionList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().filter(StrUtil::isNotBlank).distinct().toList();
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

    private String resolveLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return "-";
        }
        if (map == null || map.isEmpty()) {
            return value;
        }
        return StrUtil.blankToDefault(map.get(value), value);
    }

    private String buildSectionDisplayName(String riverName, String sectionName) {
        String section = StrUtil.blankToDefault(StrUtil.trimToNull(sectionName), "-");
        String river = StrUtil.trimToNull(riverName);
        if (StrUtil.isBlank(river)) {
            return section;
        }
        return river + "/" + section;
    }

    // endregion
}
