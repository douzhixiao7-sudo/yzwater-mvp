package com.sydigit.yzwater.module.service.reservoir;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirChiefOverviewChiefVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirChiefOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirHeadBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirHeadItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirHeadItemSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirTownshipNormalizeRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseBfDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseBfMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.service.river.RiverChiefUserAccountService;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 水库管理服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class WaterReservoirBfService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final String FACILITY_TYPE_RESERVOIR = ReferenceTypeConstants.RESERVOIR;
    private static final int DEFAULT_SRID = 4490;

    private final YzWaterReservoirBfMapper reservoirMapper;
    private final YzWaterFacilityBaseBfMapper baseMapper;
    private final YzRiverChannelManagementMapper managementMapper;
    private final DictDataCommonApi dictDataApi;
    private final RiverChiefUserAccountService riverChiefUserAccountService;
    private final SystemAreaMapper systemAreaMapper;

    /**
     * 分页查询水库
     */
    public PageResult<ReservoirPageRespVO> getReservoirPage(ReservoirPageReqVO reqVO) {
        LambdaQueryWrapper<YzWaterReservoirBfDO> wrapper = reservoirMapper.buildQueryWrapper(reqVO);
        PageResult<YzWaterReservoirBfDO> page = reservoirMapper.selectPage(reqVO, wrapper);
        List<YzWaterReservoirBfDO> records = page.getList() == null ? List.of() : page.getList();
        List<ReservoirPageRespVO> list = BeanUtils.toBean(records, ReservoirPageRespVO.class);
        Map<String, String> areaNameMapById = loadAreaNameMapById(records);
        for (int i = 0; i < list.size(); i++) {
            ReservoirPageRespVO vo = list.get(i);
            YzWaterReservoirBfDO item = i < records.size() ? records.get(i) : null;
            if (vo == null || item == null) {
                continue;
            }
            vo.setTownship(item.getTownship());
            vo.setTownshipName(buildTownshipDisplayName(item.getTownship(), areaNameMapById));
        }
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 查询全部水库（简要信息，用于下拉选择）
     */
    public List<ReservoirSimpleRespVO> getReservoirSimpleList() {
        List<YzWaterReservoirBfDO> list = reservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .select(YzWaterReservoirBfDO::getId, YzWaterReservoirBfDO::getReservoirName)
                .orderByDesc(YzWaterReservoirBfDO::getCreateTime));
        return list.stream().map(item -> {
            ReservoirSimpleRespVO vo = new ReservoirSimpleRespVO();
            vo.setId(item.getId());
            vo.setReservoirName(item.getReservoirName());
            return vo;
        }).toList();
    }

    /**
     * 导出水库数据（不分页），字典字段转换为对应的标签值
     */
    public List<ReservoirExportExcelVO> getReservoirExportList(ReservoirPageReqVO reqVO) {
        LambdaQueryWrapper<YzWaterReservoirBfDO> wrapper = reservoirMapper.buildQueryWrapper(reqVO);
        List<YzWaterReservoirBfDO> list = reservoirMapper.selectList(wrapper);
        Map<String, String> scaleMap = loadDictLabelMap(ZdConstants.ZD_SKGM);
        Map<String, String> natureMap = loadDictLabelMap(ZdConstants.ZD_SKXZ);
        Map<String, String> managementMap = loadDictLabelMap(ZdConstants.ZD_GLDW);
        Map<String, String> areaNameMapById = loadAreaNameMapById(list);
        return list.stream()
                .map(item -> buildExportExcelVO(item, scaleMap, natureMap, managementMap, areaNameMapById))
                .collect(Collectors.toList());
    }

    /**
     * 获取水库详情
     */
    public ReservoirSaveReqVO getReservoirDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        YzWaterReservoirBfDO reservoir = reservoirMapper.selectById(id);
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        ReservoirSaveReqVO vo = BeanUtils.toBean(reservoir, ReservoirSaveReqVO.class);
        vo.setFacilityId(reservoir.getFacilityId());
        vo.setManagementUnit(reservoir.getManagementUnit() == null ? List.of() : List.of(reservoir.getManagementUnit()));
        vo.setReservoirPhotos(reservoir.getReservoirPhotos() == null ? List.of() : List.of(reservoir.getReservoirPhotos()));

        if (reservoir.getFacilityId() != null) {
            YzWaterFacilityBaseBfDO base = baseMapper.selectById(reservoir.getFacilityId());
            if (base != null) {
                if (isTownshipEmpty(vo.getTownship()) && StrUtil.isNotBlank(base.getAdminRegionCode())) {
                    vo.setTownship(new String[]{base.getAdminRegionCode()});
                }
                if (StrUtil.isBlank(vo.getTownshipName()) && StrUtil.isNotBlank(base.getAdminRegion())) {
                    vo.setTownshipName(base.getAdminRegion());
                }
                vo.setGeomType(base.getGeomType());
                vo.setSrid(base.getSrid());
                Geometry geom = base.getGeom();
                if (geom != null) {
                    try {
                        StringWriter writer = new StringWriter();
                        new GeometryJSON().write(geom, writer);
                        vo.setGeometryGeoJson(writer.toString());
                    } catch (Exception ignored) {
                        // GeoJSON 转换失败时不影响详情查询
                    }
                }
            }
        }
        // 兼容历史数据：township 可能直接存的是名称
        if (StrUtil.isBlank(vo.getTownshipName())) {
            vo.setTownshipName(joinTownship(reservoir.getTownship()));
        }
        return vo;
    }

    /**
     * 查询水库河长信息（仅返回当前版本）
     */
    public List<ReservoirHeadItemRespVO> getReservoirManagement(Long reservoirId) {
        if (reservoirId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        YzWaterReservoirBfDO reservoir = reservoirMapper.selectById(reservoirId);
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        String responsibilities = StrUtil.trimToNull(reservoir.getResponsibilities());
        List<YzRiverChannelManagementDO> list = loadCurrentReservoirHeads(reservoirId);
        return list.stream().map(item -> {
            ReservoirHeadItemRespVO vo = new ReservoirHeadItemRespVO();
            vo.setHeadLevel(item.getHeadLevel());
            vo.setHeadPosition(item.getHeadPosition());
            vo.setHeadUnit(item.getHeadUnit());
            vo.setHeadName(item.getHeadName());
            vo.setHeadContact(item.getHeadContact());
            vo.setResponsibilities(responsibilities);
            vo.setAdministrativeRegion(toStringList(item.getAdministrativeRegion()));
            vo.setRemarks(item.getRemarks());
            return vo;
        }).toList();
    }

    /**
     * 水库页查看当前有效河长概览。
     */
    public ReservoirChiefOverviewRespVO getReservoirChiefOverview(Long reservoirId) {
        if (reservoirId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        YzWaterReservoirBfDO reservoir = reservoirMapper.selectById(reservoirId);
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }

        Map<String, String> headLevelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);
        List<YzRiverChannelManagementDO> chiefs = loadCurrentReservoirHeads(reservoirId);

        ReservoirChiefOverviewRespVO resp = new ReservoirChiefOverviewRespVO();
        resp.setReservoirId(reservoirId);
        resp.setReservoirName(StrUtil.blankToDefault(reservoir.getReservoirName(), "-"));
        resp.setReservoirChiefs(buildChiefOverviewItems(chiefs, headLevelMap));
        resp.setTotalCount(resp.getReservoirChiefs().size());
        return resp;
    }

    /**
     * 按设施ID查询水库详情
     */
    public ReservoirSaveReqVO getReservoirDetailByFacilityId(Long facilityId) {
        if (facilityId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        YzWaterReservoirBfDO reservoir = reservoirMapper.selectOne(new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .eq(YzWaterReservoirBfDO::getFacilityId, facilityId)
                .eq(YzWaterReservoirBfDO::getDeleted, 0));
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        return getReservoirDetail(reservoir.getId());
    }

    /**
     * 保存水库河长信息（覆盖式）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveReservoirManagement(ReservoirHeadBatchSaveReqVO reqVO) {
        YzWaterReservoirBfDO reservoir = reservoirMapper.selectById(reqVO.getWaterReservoirId());
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }

        List<YzRiverChannelManagementDO> currentHeads = managementMapper.selectCurrentByResolvedReference(
                ReferenceTypeConstants.RESERVOIR_BF, reqVO.getWaterReservoirId(), null);

        List<ReservoirHeadItemSaveReqVO> reqHeads = CollUtil.defaultIfEmpty(reqVO.getHeads(), List.of()).stream()
                .filter(Objects::nonNull)
                .filter(this::hasAnyHeadField)
                .toList();

        String currentSnapshot = buildHeadSnapshotFromCurrent(currentHeads);
        String reqSnapshot = buildHeadSnapshotFromReq(reqHeads);
        if (StrUtil.equals(currentSnapshot, reqSnapshot)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!currentHeads.isEmpty()) {
            managementMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                    .eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RESERVOIR_BF)
                    .eq(YzRiverChannelManagementDO::getReferenceId, reqVO.getWaterReservoirId())
                    .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                    .set(YzRiverChannelManagementDO::getIsCurrent, 0)
                    .set(YzRiverChannelManagementDO::getEffectiveTo, now));
        }

        if (reqHeads.isEmpty()) {
            return;
        }
        // 需要生成河长账号：河长级别/姓名/电话必填
        for (ReservoirHeadItemSaveReqVO head : reqHeads) {
            if (head == null) {
                continue;
            }
            if (StrUtil.isBlank(head.getHeadLevel())) {
                throw ServiceExceptionUtil.invalidParamException("河长级别不能为空");
            }
            if (StrUtil.isBlank(head.getHeadName())) {
                throw ServiceExceptionUtil.invalidParamException("河长姓名不能为空");
            }
        }

        int nextVersionNo = currentHeads.stream()
                .map(YzRiverChannelManagementDO::getVersionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        List<YzRiverChannelManagementDO> inserted = new ArrayList<>();
        for (ReservoirHeadItemSaveReqVO head : reqHeads) {
            YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
            record.setId(SNOWFLAKE.nextId());
            record.setRiverChannelId(null);
            record.setRiverSectionId(null);
            record.setWaterReservoirId(null);
            record.setReferenceId(reqVO.getWaterReservoirId());
            record.setReferenceType(ReferenceTypeConstants.RESERVOIR_BF);
            record.setSectionName(reservoir.getReservoirName());
            record.setHeadLevel(StrUtil.trimToNull(head.getHeadLevel()));
            record.setHeadPosition(StrUtil.trimToNull(head.getHeadPosition()));
            record.setHeadUnit(StrUtil.trimToNull(head.getHeadUnit()));
            record.setHeadName(StrUtil.trimToNull(head.getHeadName()));
            record.setHeadContact(null);
            record.setAdministrativeRegion(toStringArray(head.getAdministrativeRegion()));
            record.setRemarks(StrUtil.trimToNull(head.getRemarks()));
            record.setVersionNo(nextVersionNo);
            record.setEffectiveFrom(now);
            record.setEffectiveTo(null);
            record.setIsCurrent(1);
            managementMapper.insert(record);
            inserted.add(record);
        }

        // 按需求临时关闭：新增/编辑河长不再自动同步创建系统用户（AdminUserDO）
        // riverChiefUserAccountService.syncAndBindStrict(inserted);
    }

    public void syncReservoirManagementResponsibilities(Long reservoirId, String responsibilities) {
        if (reservoirId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        YzWaterReservoirBfDO reservoir = reservoirMapper.selectById(reservoirId);
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        // 管理表已移除 responsibilities 字段，保留该接口仅用于兼容旧调用链。
    }

    private List<YzRiverChannelManagementDO> loadCurrentReservoirHeads(Long reservoirId) {
        List<YzRiverChannelManagementDO> bfHeads = managementMapper.selectCurrentByResolvedReference(
                ReferenceTypeConstants.RESERVOIR_BF, reservoirId, null);
        if (CollUtil.isNotEmpty(bfHeads)) {
            return bfHeads;
        }
        return managementMapper.selectList(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getWaterReservoirId, reservoirId)
                .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                .orderByAsc(YzRiverChannelManagementDO::getId));
    }

    /**
     * 根据关联设施 geom 计算水库中心点并回写经纬度。
     */
    @Transactional(rollbackFor = Exception.class)
    public int syncLongitudeLatitudeByFacilityGeom() {
        return reservoirMapper.syncLongitudeLatitudeFromFacilityGeom();
    }

    /**
     * 规范化水库乡镇字段（中文名转区划编码）
     */
    public ReservoirTownshipNormalizeRespVO normalizeReservoirTownship() {
        List<YzWaterReservoirBfDO> list = reservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .select(YzWaterReservoirBfDO::getId,
                        YzWaterReservoirBfDO::getTownship,
                        YzWaterReservoirBfDO::getManagementUnit));
        ReservoirTownshipNormalizeRespVO resp = new ReservoirTownshipNormalizeRespVO();
        resp.setTotalCount((long) list.size());
        if (CollUtil.isEmpty(list)) {
            return resp;
        }

        Set<String> nameCandidates = new HashSet<>();
        for (YzWaterReservoirBfDO item : list) {
            List<String> townshipList = normalizeTownshipList(item == null ? null : item.getTownship());
            for (String township : townshipList) {
                if (!isPureNumber(township)) {
                    nameCandidates.add(township);
                }
            }
        }
        Map<String, Long> areaNameMap = loadAreaIdMapByName(nameCandidates);
        Map<String, String> managementUnitLabelMap = loadDictValueMapByLabel(ZdConstants.ZD_GLDW);
        Set<String> managementUnitValueSet = new HashSet<>(managementUnitLabelMap.values());

        long updated = 0L;
        long skipped = 0L;
        Set<String> unmatchedNames = new HashSet<>();
        Set<String> unmatchedManagementUnits = new HashSet<>();
        for (YzWaterReservoirBfDO item : list) {
            if (item == null) {
                skipped++;
                continue;
            }
            List<String> originList = normalizeTownshipList(item.getTownship());
            List<String> normalizedList = new ArrayList<>();
            boolean hasUnmatched = false;
            for (String township : originList) {
                if (isPureNumber(township)) {
                    normalizedList.add(township);
                    continue;
                }
                Long matchedId = areaNameMap.get(normalizeTownshipName(township));
                if (matchedId != null) {
                    normalizedList.add(String.valueOf(matchedId));
                } else {
                    hasUnmatched = true;
                    unmatchedNames.add(township);
                }
            }

            List<String> originManagementUnits = normalizeManagementUnitList(item.getManagementUnit());
            List<String> normalizedManagementUnits = new ArrayList<>();
            boolean hasUnmatchedManagementUnit = false;
            for (String unit : originManagementUnits) {
                String normalized = normalizeManagementUnitName(unit);
                if (StrUtil.isBlank(normalized)) {
                    continue;
                }
                String value = managementUnitLabelMap.get(normalized);
                if (StrUtil.isNotBlank(value)) {
                    normalizedManagementUnits.add(value);
                } else if (managementUnitValueSet.contains(normalized)) {
                    normalizedManagementUnits.add(normalized);
                } else {
                    hasUnmatchedManagementUnit = true;
                    unmatchedManagementUnits.add(normalized);
                }
            }

            List<String> deduped = deduplicatePreserveOrder(normalizedList);
            String[] newTownship = deduped.isEmpty() ? null : deduped.toArray(new String[0]);
            List<String> newManagementList = deduplicatePreserveOrder(normalizedManagementUnits);
            String[] newManagementUnit = newManagementList.isEmpty() ? null : newManagementList.toArray(new String[0]);

            boolean needUpdate = false;
            LambdaUpdateWrapper<YzWaterReservoirBfDO> updateWrapper = new LambdaUpdateWrapper<YzWaterReservoirBfDO>()
                    .eq(YzWaterReservoirBfDO::getId, item.getId());
            if (!hasUnmatched && !isSameTownship(item.getTownship(), newTownship)) {
                updateWrapper.set(YzWaterReservoirBfDO::getTownship, newTownship);
                needUpdate = true;
            }
            if (!hasUnmatchedManagementUnit && !isSameManagementUnit(item.getManagementUnit(), newManagementUnit)) {
                updateWrapper.set(YzWaterReservoirBfDO::getManagementUnit, newManagementUnit);
                needUpdate = true;
            }
            if (!needUpdate) {
                skipped++;
                continue;
            }
            reservoirMapper.update(null, updateWrapper);
            updated++;
        }
        resp.setUpdatedCount(updated);
        resp.setSkippedCount(skipped);
        List<String> unmatchedList = unmatchedNames.stream().sorted().toList();
        resp.setUnmatchedCount((long) unmatchedList.size());
        resp.setUnmatchedNames(unmatchedList);
        List<String> unmatchedManagementList = unmatchedManagementUnits.stream().sorted().toList();
        resp.setUnmatchedManagementUnitCount((long) unmatchedManagementList.size());
        resp.setUnmatchedManagementUnitNames(unmatchedManagementList);
        return resp;
    }

    private boolean hasAnyHeadField(ReservoirHeadItemSaveReqVO head) {
        if (head == null) {
            return false;
        }
        return StrUtil.isNotBlank(head.getHeadLevel())
                || StrUtil.isNotBlank(head.getHeadPosition())
                || StrUtil.isNotBlank(head.getHeadUnit())
                || StrUtil.isNotBlank(head.getHeadName())
                || CollUtil.isNotEmpty(head.getAdministrativeRegion())
                || StrUtil.isNotBlank(head.getRemarks());
    }

    private String buildHeadSnapshotFromCurrent(List<YzRiverChannelManagementDO> heads) {
        if (CollUtil.isEmpty(heads)) {
            return "";
        }
        return heads.stream()
                .filter(Objects::nonNull)
                .map(item -> String.join("|",
                        StrUtil.blankToDefault(item.getHeadLevel(), ""),
                        StrUtil.blankToDefault(item.getHeadPosition(), ""),
                        StrUtil.blankToDefault(item.getHeadUnit(), ""),
                        StrUtil.blankToDefault(item.getHeadName(), ""),
                        joinRegion(toStringList(item.getAdministrativeRegion())),
                        StrUtil.blankToDefault(item.getRemarks(), "")))
                .sorted()
                .collect(Collectors.joining(";"));
    }

    private String buildHeadSnapshotFromReq(List<ReservoirHeadItemSaveReqVO> heads) {
        if (CollUtil.isEmpty(heads)) {
            return "";
        }
        return heads.stream()
                .filter(Objects::nonNull)
                .map(item -> String.join("|",
                        StrUtil.blankToDefault(StrUtil.trimToNull(item.getHeadLevel()), ""),
                        StrUtil.blankToDefault(StrUtil.trimToNull(item.getHeadPosition()), ""),
                        StrUtil.blankToDefault(StrUtil.trimToNull(item.getHeadUnit()), ""),
                        StrUtil.blankToDefault(StrUtil.trimToNull(item.getHeadName()), ""),
                        joinRegion(normalizeRegionList(item.getAdministrativeRegion())),
                        StrUtil.blankToDefault(StrUtil.trimToNull(item.getRemarks()), "")))
                .sorted()
                .collect(Collectors.joining(";"));
    }

    private List<String> toStringList(String[] values) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        return Arrays.stream(values)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> normalizeRegionList(List<String> values) {
        return Optional.ofNullable(values).orElse(List.of()).stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private String joinRegion(List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return "";
        }
        return String.join(",", values);
    }

    /**
     * 新增水库，业务数据写入 BF 水库表，GIS 数据写入 BF 设施基础表。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createReservoir(ReservoirSaveReqVO reqVO) {
        // 水库编码统一由后端雪花算法生成，不接受前端录入
        reqVO.setReservoirCode(String.valueOf(SNOWFLAKE.nextId()));
        Long facilityId = createFacilityBase(reqVO);
        if (reqVO.getGeometryGeoJson() != null) {
            applyFacilityGeometry(facilityId, reqVO.getGeometryGeoJson(), reqVO.getSrid());
        }
        YzWaterReservoirBfDO reservoir = new YzWaterReservoirBfDO();
        reservoir.setId(SNOWFLAKE.nextId());
        reservoir.setFacilityId(facilityId);
        reservoir.setReservoirCode(reqVO.getReservoirCode());
        reservoir.setReservoirName(reqVO.getReservoirName());
        reservoir.setReservoirScale(reqVO.getReservoirScale());
        reservoir.setTownship(reqVO.getTownship());
        reservoir.setLocation(reqVO.getLocation());
        reservoir.setManagementUnit(toStringArray(reqVO.getManagementUnit()));
        reservoir.setReservoirPhotos(toStringArray(reqVO.getReservoirPhotos()));
        reservoir.setLongitude(reqVO.getLongitude());
        reservoir.setLatitude(reqVO.getLatitude());
        reservoir.setSupervisingDepartment(reqVO.getSupervisingDepartment());
        reservoir.setReservoirNature(reqVO.getReservoirNature());
        reservoir.setIrrigationArea(reqVO.getIrrigationArea());
        reservoir.setDesignIrrigationArea(reqVO.getDesignIrrigationArea());
        reservoir.setActualIrrigationArea(reqVO.getActualIrrigationArea());
        reservoir.setProtectionArea(reqVO.getProtectionArea());
        reservoir.setDownstreamFacilities(reqVO.getDownstreamFacilities());
        reservoir.setWaterSupplyTarget(reqVO.getWaterSupplyTarget());
        reservoir.setCatchmentArea(reqVO.getCatchmentArea());
        reservoir.setElevationDatum(reqVO.getElevationDatum());
        reservoir.setSeismicIntensity(reqVO.getSeismicIntensity());
        reservoir.setCompletionDate(reqVO.getCompletionDate());
        reservoir.setReinforcementStartDate(reqVO.getReinforcementStartDate());
        reservoir.setReinforcementEndDate(reqVO.getReinforcementEndDate());
        reservoir.setReinforcementDate(reqVO.getReinforcementDate());
        reservoir.setDesignFloodStandard(reqVO.getDesignFloodStandard());
        reservoir.setVerifiedFloodStandard(reqVO.getVerifiedFloodStandard());
        reservoir.setDesignReturnPeriod(reqVO.getDesignReturnPeriod());
        reservoir.setCheckReturnPeriod(reqVO.getCheckReturnPeriod());
        reservoir.setTotalCapacity(reqVO.getTotalCapacity());
        reservoir.setActiveCapacity(reqVO.getActiveCapacity());
        reservoir.setFloodControlCapacity(reqVO.getFloodControlCapacity());
        reservoir.setDeadCapacity(reqVO.getDeadCapacity());
        reservoir.setVerifiedFloodLevel(reqVO.getVerifiedFloodLevel());
        reservoir.setDesignFloodLevel(reqVO.getDesignFloodLevel());
        reservoir.setNormalOperatingLevel(reqVO.getNormalOperatingLevel());
        reservoir.setFloodLimitLevel(reqVO.getFloodLimitLevel());
        reservoir.setDeadLevel(reqVO.getDeadLevel());
        reservoir.setDamCrestElevation(reqVO.getDamCrestElevation());
        reservoir.setDamTopWidth(reqVO.getDamTopWidth());
        reservoir.setDamTopHeight(reqVO.getDamTopHeight());
        reservoir.setMaxDamHeight(reqVO.getMaxDamHeight());
        reservoir.setDamTopLength(reqVO.getDamTopLength());
        reservoir.setWaveWallCrestElevation(reqVO.getWaveWallCrestElevation());
        reservoir.setDamRoadSurfaceType(reqVO.getDamRoadSurfaceType());
        reservoir.setSeepageControlType(reqVO.getSeepageControlType());
        reservoir.setSeepagePileRange(reqVO.getSeepagePileRange());
        reservoir.setSeepageElevRange(reqVO.getSeepageElevRange());
        reservoir.setUpstreamSlopeType(reqVO.getUpstreamSlopeType());
        reservoir.setUpstreamSlopeElevation(reqVO.getUpstreamSlopeElevation());
        reservoir.setUpstreamSlopeRatio(reqVO.getUpstreamSlopeRatio());
        reservoir.setDownstreamSlopeRatio(reqVO.getDownstreamSlopeRatio());
        reservoir.setSlopeProtectionType(reqVO.getSlopeProtectionType());
        reservoir.setSlopeProtectionElevRange(reqVO.getSlopeProtectionElevRange());
        reservoir.setDownstreamSlopeElevation(reqVO.getDownstreamSlopeElevation());
        reservoir.setDownstreamSlopeWidth(reqVO.getDownstreamSlopeWidth());
        reservoir.setSpillwayType(reqVO.getSpillwayType());
        reservoir.setSpillwayControlType(reqVO.getSpillwayControlType());
        reservoir.setSpillwayHasBridge(reqVO.getSpillwayHasBridge());
        reservoir.setSpillwayCrestElevation(reqVO.getSpillwayCrestElevation());
        reservoir.setSpillwayBottomElevation(reqVO.getSpillwayBottomElevation());
        reservoir.setSpillwayBottomWidth(reqVO.getSpillwayBottomWidth());
        reservoir.setSpillwayMaxDischarge(reqVO.getSpillwayMaxDischarge());
        reservoir.setFloodChannelName(reqVO.getFloodChannelName());
        reservoir.setFloodChannelSafeDischarge(reqVO.getFloodChannelSafeDischarge());
        reservoir.setCulvertType(reqVO.getCulvertType());
        reservoir.setCulvertSectionSize(reqVO.getCulvertSectionSize());
        reservoir.setCulvertGateType(reqVO.getCulvertGateType());
        reservoir.setCulvertDesignDischarge(reqVO.getCulvertDesignDischarge());
        reservoir.setCulvertExitElevation(reqVO.getCulvertExitElevation());
        reservoir.setCulvertDiameter(reqVO.getCulvertDiameter());
        reservoir.setCulvertHeight(reqVO.getCulvertHeight());
        reservoir.setAnnualWaterSupply(reqVO.getAnnualWaterSupply());
        reservoir.setFisheryArea(reqVO.getFisheryArea());
        reservoir.setWaterSource(reqVO.getWaterSource());
        reservoir.setResponsibilities(reqVO.getResponsibilities());
        reservoir.setRemarks(reqVO.getRemarks());
        reservoir.setJumpUrl(reqVO.getJumpUrl());
        reservoirMapper.insert(reservoir);
        return reservoir.getId();
    }

    /**
     * 编辑水库，业务数据更新 BF 水库表，GIS 数据同步 BF 设施基础表。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateReservoir(ReservoirSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        YzWaterReservoirBfDO exists = reservoirMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        String inputReservoirCode = StrUtil.trimToNull(reqVO.getReservoirCode());
        // 兼容前端不传编码场景：不传则沿用历史编码，避免更新时清空
        if (StrUtil.isBlank(inputReservoirCode)) {
            inputReservoirCode = StrUtil.trimToNull(exists.getReservoirCode());
        }
        if (StrUtil.isNotBlank(inputReservoirCode)) {
            validateReservoirCodeUnique(inputReservoirCode, exists.getId());
            reqVO.setReservoirCode(inputReservoirCode);
        }

        Long facilityId = ensureFacilityBase(reqVO, exists.getFacilityId());
        if (reqVO.getGeometryGeoJson() != null) {
            applyFacilityGeometry(facilityId, reqVO.getGeometryGeoJson(), reqVO.getSrid());
        }
        reservoirMapper.updateEditFieldsById(
                exists.getId(),
                facilityId,
                reqVO,
                toStringArray(reqVO.getManagementUnit()),
                toStringArray(reqVO.getReservoirPhotos())
        );
    }

    /**
     * 删除 BF 水库业务记录及对应 BF GIS 设施记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReservoir(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        YzWaterReservoirBfDO reservoir = reservoirMapper.selectById(id);
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        managementMapper.delete(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getReferenceType, ReferenceTypeConstants.RESERVOIR_BF)
                .eq(YzRiverChannelManagementDO::getReferenceId, id));
        reservoirMapper.deleteById(id);
        if (reservoir.getFacilityId() != null) {
            baseMapper.deleteById(reservoir.getFacilityId());
        }
    }

    /**
     * 校验水库编码唯一性（排除指定ID）。
     */
    public void validateReservoirCodeUnique(String reservoirCode, Long excludeId) {
        if (StrUtil.isBlank(reservoirCode)) {
            return;
        }
        Long count = reservoirMapper.selectCount(new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .eq(YzWaterReservoirBfDO::getReservoirCode, reservoirCode)
                .ne(excludeId != null, YzWaterReservoirBfDO::getId, excludeId));
        if (count != null && count > 0) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_CODE_DUPLICATE);
        }
    }

    /**
     * 判断水库编码是否已存在（排除指定ID）。
     */
    public boolean isReservoirCodeExists(String reservoirCode, Long excludeId) {
        if (StrUtil.isBlank(reservoirCode)) {
            return false;
        }
        Long count = reservoirMapper.selectCount(new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .eq(YzWaterReservoirBfDO::getReservoirCode, reservoirCode)
                .ne(excludeId != null, YzWaterReservoirBfDO::getId, excludeId));
        return count != null && count > 0;
    }

    /**
     * 创建水库基础设施记录
     */
    /**
     * 创建 BF 水库设施记录。
     */
    private Long createFacilityBase(ReservoirSaveReqVO reqVO) {
        Long baseId = SNOWFLAKE.nextId();
        YzWaterFacilityBaseBfDO base = new YzWaterFacilityBaseBfDO();
        base.setId(baseId);
        base.setFacilityName(reqVO.getReservoirName());
        base.setFacilityType(FACILITY_TYPE_RESERVOIR);
        base.setFacilityCode(reqVO.getReservoirCode());
        base.setAdminRegionCode(firstTownshipCode(reqVO.getTownship()));
        base.setAdminRegion(reqVO.getTownshipName());
        base.setManageUnit(joinManagementUnit(reqVO.getManagementUnit()));
        base.setSourceType("bf");
        baseMapper.insert(base);
        return baseId;
    }

    /**
     * 确保 BF 水库设施存在并同步基础展示字段。
     */
    private Long ensureFacilityBase(ReservoirSaveReqVO reqVO, Long facilityId) {
        if (facilityId == null) {
            return createFacilityBase(reqVO);
        }
        YzWaterFacilityBaseBfDO base = baseMapper.selectById(facilityId);
        if (base == null) {
            return createFacilityBase(reqVO);
        }
        baseMapper.updateReservoirSyncFieldsById(
                base.getId(),
                reqVO.getReservoirName(),
                FACILITY_TYPE_RESERVOIR,
                reqVO.getReservoirCode(),
                firstTownshipCode(reqVO.getTownship()),
                reqVO.getTownshipName(),
                joinManagementUnit(reqVO.getManagementUnit())
        );
        return base.getId();
    }

    /**
     * 将 GeoJSON 几何写入 BF 设施基础表。
     */
    private void applyFacilityGeometry(Long facilityId, String geometryGeoJson, Integer srid) {
        if (facilityId == null) {
            return;
        }
        if (StrUtil.isBlank(geometryGeoJson)) {
            YzWaterFacilityBaseBfDO update = new YzWaterFacilityBaseBfDO();
            update.setId(facilityId);
            update.setGeomType(null);
            update.setSrid(null);
            update.setGeom(null);
            baseMapper.updateById(update);
            return;
        }

        Geometry geometry;
        try {
            geometry = new GeometryJSON().read(geometryGeoJson);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_GEOJSON_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "格式不正确"));
        }
        if (geometry == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_GEOJSON_PARSE_ERROR, "几何为空");
        }
        int finalSrid = srid != null ? srid : DEFAULT_SRID;
        geometry.setSRID(finalSrid);

        YzWaterFacilityBaseBfDO update = new YzWaterFacilityBaseBfDO();
        update.setId(facilityId);
        update.setGeomType(geometry.getGeometryType());
        update.setSrid(finalSrid);
        update.setGeom(geometry);
        baseMapper.updateById(update);
    }

    private ReservoirExportExcelVO buildExportExcelVO(YzWaterReservoirBfDO item,
                                                     Map<String, String> scaleMap,
                                                     Map<String, String> natureMap,
                                                     Map<String, String> managementMap,
                                                     Map<String, String> areaNameMapById) {
        ReservoirExportExcelVO vo = new ReservoirExportExcelVO();
        vo.setReservoirCode(item.getReservoirCode());
        vo.setReservoirName(item.getReservoirName());
        vo.setReservoirScaleLabel(resolveLabel(item.getReservoirScale(), scaleMap));
        vo.setManagementUnitLabel(formatManagementUnit(item.getManagementUnit(), managementMap));
        vo.setReservoirNatureLabel(resolveLabel(item.getReservoirNature(), natureMap));
        vo.setTotalCapacity(item.getTotalCapacity());
        vo.setActiveCapacity(item.getActiveCapacity());
        vo.setDeadLevel(item.getDeadLevel());
        vo.setLongitude(item.getLongitude());
        vo.setLatitude(item.getLatitude());
        vo.setTownshipName(buildTownshipDisplayName(item.getTownship(), areaNameMapById));
        vo.setCatchmentArea(item.getCatchmentArea());
        vo.setNormalOperatingLevel(item.getNormalOperatingLevel());
        vo.setFloodLimitLevel(item.getFloodLimitLevel());
        vo.setDesignFloodLevel(item.getDesignFloodLevel());
        vo.setVerifiedFloodLevel(item.getVerifiedFloodLevel());
        vo.setDamCrestElevation(item.getDamCrestElevation());
        vo.setMaxDamHeight(item.getMaxDamHeight());
        vo.setDamTopLength(item.getDamTopLength());
        return vo;
    }

    private List<ReservoirChiefOverviewChiefVO> buildChiefOverviewItems(List<YzRiverChannelManagementDO> records,
                                                                        Map<String, String> headLevelMap) {
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }
        return records.stream()
                .filter(Objects::nonNull)
                .map(record -> {
                    ReservoirChiefOverviewChiefVO item = new ReservoirChiefOverviewChiefVO();
                    item.setId(record.getId());
                    item.setHeadName(record.getHeadName());
                    item.setHeadLevel(record.getHeadLevel());
                    item.setHeadLevelLabel(resolveLabel(record.getHeadLevel(), headLevelMap));
                    item.setHeadPosition(record.getHeadPosition());
                    item.setHeadContact(record.getHeadContact());
                    item.setEffectiveFrom(record.getEffectiveFrom());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String resolveLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        return StrUtil.blankToDefault(map.get(value), value);
    }

    private String formatManagementUnit(String[] units, Map<String, String> map) {
        if (units == null || units.length == 0) {
            return null;
        }
        List<String> labels = Arrays.stream(units)
                .filter(StrUtil::isNotBlank)
                .map(v -> resolveLabel(v, map))
                .distinct()
                .toList();
        return labels.isEmpty() ? null : String.join("，", labels);
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

    private String joinManagementUnit(List<String> units) {
        List<String> cleaned = Optional.ofNullable(units).orElse(List.of()).stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return cleaned.isEmpty() ? null : String.join(",", cleaned);
    }

    private String[] toStringArray(List<String> values) {
        List<String> cleaned = Optional.ofNullable(values).orElse(List.of()).stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
    }

    private boolean isTownshipEmpty(String[] township) {
        if (township == null || township.length == 0) {
            return true;
        }
        for (String item : township) {
            if (StrUtil.isNotBlank(item)) {
                return false;
            }
        }
        return true;
    }

    private String firstTownshipCode(String[] township) {
        if (township == null || township.length == 0) {
            return null;
        }
        for (String item : township) {
            if (StrUtil.isNotBlank(item)) {
                return item;
            }
        }
        return null;
    }

    private String joinTownship(String[] township) {
        if (township == null || township.length == 0) {
            return null;
        }
        List<String> cleaned = Arrays.stream(township)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return cleaned.isEmpty() ? null : String.join("、", cleaned);
    }

    private Map<String, String> loadAreaNameMapById(List<YzWaterReservoirBfDO> reservoirs) {
        if (CollUtil.isEmpty(reservoirs)) {
            return Map.of();
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (YzWaterReservoirBfDO item : reservoirs) {
            List<String> townshipList = normalizeTownshipList(item == null ? null : item.getTownship());
            for (String township : townshipList) {
                if (!isPureNumber(township)) {
                    continue;
                }
                try {
                    ids.add(Long.valueOf(township));
                } catch (Exception ignored) {
                    // 忽略无效数字，后续按原值展示
                }
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectByIdsWithGemo(new ArrayList<>(ids));
        Map<String, String> result = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null || StrUtil.isBlank(area.getName())) {
                continue;
            }
            result.putIfAbsent(String.valueOf(area.getId()), area.getName());
        }
        return result;
    }

    private String buildTownshipDisplayName(String[] township, Map<String, String> areaNameMapById) {
        List<String> list = normalizeTownshipList(township);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (String codeOrName : list) {
            String mapped = areaNameMapById.get(codeOrName);
            names.add(StrUtil.blankToDefault(mapped, codeOrName));
        }
        return names.isEmpty() ? null : String.join("、", names);
    }

    private Map<String, Long> loadAreaIdMapByName(Set<String> names) {
        if (CollUtil.isEmpty(names)) {
            return Map.of();
        }
        List<String> nameList = names.stream()
                .map(this::normalizeTownshipName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (nameList.isEmpty()) {
            return Map.of();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectByNames(nameList);
        Map<String, Long> result = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String name = normalizeTownshipName(area.getName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            result.putIfAbsent(name, area.getId());
        }
        return result;
    }

    private Map<String, String> loadDictValueMapByLabel(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO dict : list) {
            String label = StrUtil.trimToNull(dict.getLabel());
            String value = StrUtil.trimToNull(dict.getValue());
            if (label == null || value == null) {
                continue;
            }
            result.putIfAbsent(label, value);
        }
        return result;
    }

    private boolean isPureNumber(String text) {
        if (StrUtil.isBlank(text)) {
            return false;
        }
        return StrUtil.isNumeric(text);
    }

    private List<String> normalizeTownshipList(String[] township) {
        if (township == null || township.length == 0) {
            return List.of();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String item : township) {
            String normalized = normalizeTownshipName(item);
            if (StrUtil.isNotBlank(normalized)) {
                set.add(normalized);
            }
        }
        return new ArrayList<>(set);
    }

    private String normalizeTownshipName(String text) {
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
        return normalized;
    }

    private List<String> normalizeManagementUnitList(String[] units) {
        if (units == null || units.length == 0) {
            return List.of();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String unit : units) {
            String normalized = normalizeManagementUnitName(unit);
            if (StrUtil.isNotBlank(normalized)) {
                set.add(normalized);
            }
        }
        return new ArrayList<>(set);
    }

    private String normalizeManagementUnitName(String text) {
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
        return StrUtil.trimToNull(normalized);
    }

    private List<String> deduplicatePreserveOrder(List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return List.of();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = StrUtil.trimToNull(value);
            if (StrUtil.isNotBlank(normalized)) {
                set.add(normalized);
            }
        }
        return new ArrayList<>(set);
    }

    private boolean isSameTownship(String[] origin, String[] target) {
        List<String> left = normalizeTownshipList(origin);
        List<String> right = normalizeTownshipList(target);
        return left.equals(right);
    }

    private boolean isSameManagementUnit(String[] origin, String[] target) {
        List<String> left = normalizeManagementUnitList(origin);
        List<String> right = normalizeManagementUnitList(target);
        return left.equals(right);
    }
}
