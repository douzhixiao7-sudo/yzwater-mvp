package com.sydigit.yzwater.module.service.embankment;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentExportExcelVO;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.embankment.YzEmbankmentMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 堤防信息服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class EmbankmentService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzEmbankmentMapper embankmentMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final DictDataCommonApi dictDataApi;

    /**
     * 分页查询堤防信息
     */
    public PageResult<EmbankmentPageRespVO> getEmbankmentPage(EmbankmentPageReqVO reqVO) {
        LambdaQueryWrapper<YzEmbankmentDO> wrapper = embankmentMapper.buildQueryWrapper(reqVO);
        PageResult<YzEmbankmentDO> page = embankmentMapper.selectPage(reqVO, wrapper);
        Map<String, String> bankSideMap = loadDictLabelMap(ZdConstants.ZD_HLAB);
        Map<String, String> levelMap = loadDictLabelMap(ZdConstants.ZD_DFJB);
        Map<String, String> typeMap = loadDictLabelMap(ZdConstants.ZD_DFLX);
        Map<String, String> formMap = loadDictLabelMap(ZdConstants.ZD_DFXS);
        List<EmbankmentPageRespVO> list = page.getList().stream()
                .map(item -> buildPageResp(item, bankSideMap, levelMap, typeMap, formMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 导出堤防数据（不分页），字典字段转换为对应的标签值
     */
    public List<EmbankmentExportExcelVO> getEmbankmentExportList(EmbankmentPageReqVO reqVO) {
        LambdaQueryWrapper<YzEmbankmentDO> wrapper = embankmentMapper.buildQueryWrapper(reqVO);
        List<YzEmbankmentDO> list = embankmentMapper.selectList(wrapper);
        Map<String, String> levelMap = loadDictLabelMap(ZdConstants.ZD_DFJB);
        Map<String, String> typeMap = loadDictLabelMap(ZdConstants.ZD_DFLX);
        Map<String, String> formMap = loadDictLabelMap(ZdConstants.ZD_DFXS);
        return list.stream()
                .map(item -> buildExportExcelVO(item, levelMap, typeMap, formMap))
                .collect(Collectors.toList());
    }

    private EmbankmentPageRespVO buildPageResp(YzEmbankmentDO item,
                                              Map<String, String> bankSideMap,
                                              Map<String, String> levelMap,
                                              Map<String, String> typeMap,
                                              Map<String, String> formMap) {
        EmbankmentPageRespVO resp = BeanUtils.toBean(item, EmbankmentPageRespVO.class);
        resp.setRiverBankSideLabel(resolveLabel(item.getRiverBankSide(), bankSideMap));
        resp.setEmbankmentLevelLabel(resolveLabel(item.getEmbankmentLevel(), levelMap));
        resp.setEmbankmentTypeLabel(resolveLabel(item.getEmbankmentType(), typeMap));
        resp.setEmbankmentFormLabel(resolveLabel(item.getEmbankmentForm(), formMap));
        return resp;
    }

    private EmbankmentExportExcelVO buildExportExcelVO(YzEmbankmentDO item,
                                                      Map<String, String> levelMap,
                                                      Map<String, String> typeMap,
                                                      Map<String, String> formMap) {
        EmbankmentExportExcelVO vo = new EmbankmentExportExcelVO();
        vo.setEmbankmentCode(item.getEmbankmentCode());
        vo.setEmbankmentName(item.getEmbankmentName());
        vo.setEmbankmentLevelLabel(resolveLabel(item.getEmbankmentLevel(), levelMap));
        vo.setEmbankmentFormLabel(resolveLabel(item.getEmbankmentForm(), formMap));
        vo.setLengthM(item.getLengthM());
        vo.setEmbankmentTypeLabel(resolveLabel(item.getEmbankmentType(), typeMap));
        vo.setManagementDepartment(item.getManagementDepartment());
        vo.setElevationSystem(item.getElevationSystem());
        vo.setProjectTask(item.getProjectTask());
        vo.setConstructionStatus(item.getConstructionStatus());
        return vo;
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

    /**
     * 获取堤防详情
     */
    public EmbankmentSaveReqVO getEmbankmentDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_NOT_EXISTS);
        }
        YzEmbankmentDO embankment = embankmentMapper.selectById(id);
        if (embankment == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_NOT_EXISTS);
        }
        EmbankmentSaveReqVO vo = BeanUtils.toBean(embankment, EmbankmentSaveReqVO.class);
        vo.setDivisionCode(embankment.getDivisionCode() == null ? List.of() : List.of(embankment.getDivisionCode()));
        vo.setEmbankmentImages(embankment.getEmbankmentImages() == null ? List.of() : List.of(embankment.getEmbankmentImages()));
        return vo;
    }

    /**
     * 新增堤防
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createEmbankment(EmbankmentSaveReqVO reqVO) {
        return createEmbankmentInternal(reqVO);
    }

    /**
     * 导入场景新增堤防。
     *
     * <p>允许所在河道/河段为空，便于先落库基础信息，后续再补充关联。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createEmbankmentForImport(EmbankmentSaveReqVO reqVO) {
        return createEmbankmentInternal(reqVO);
    }

    private Long createEmbankmentInternal(EmbankmentSaveReqVO reqVO) {
        reqVO.setEmbankmentCode(resolveEmbankmentCode(reqVO.getEmbankmentCode(), null));
        Long facilityId = createFacilityBase(reqVO);
        YzEmbankmentDO embankment = new YzEmbankmentDO();
        embankment.setId(SNOWFLAKE.nextId());
        embankment.setFacilityId(facilityId);
        embankment.setEmbankmentCode(reqVO.getEmbankmentCode());
        embankment.setEmbankmentName(reqVO.getEmbankmentName());
        embankment.setDivisionCode(validateAndConvertDivisionCodes(reqVO.getDivisionCode()));
        embankment.setLongitude(reqVO.getLongitude());
        embankment.setLatitude(reqVO.getLatitude());
        embankment.setRiverBankSide(reqVO.getRiverBankSide());
        embankment.setCrossBoundaryStatus(reqVO.getCrossBoundaryStatus());
        embankment.setEmbankmentType(reqVO.getEmbankmentType());
        embankment.setEmbankmentForm(reqVO.getEmbankmentForm());
        embankment.setEmbankmentLevel(reqVO.getEmbankmentLevel());
        embankment.setFloodStandard(reqVO.getFloodStandard());
        embankment.setDesignReturnPeriod(reqVO.getDesignReturnPeriod());
        embankment.setLengthM(reqVO.getLengthM());
        embankment.setStandardLengthM(reqVO.getStandardLengthM());
        embankment.setElevationSystem(reqVO.getElevationSystem());
        embankment.setDesignHighTide(reqVO.getDesignHighTide());
        embankment.setMaxHeight(reqVO.getMaxHeight());
        embankment.setMinHeight(reqVO.getMinHeight());
        embankment.setMaxWidth(reqVO.getMaxWidth());
        embankment.setMinWidth(reqVO.getMinWidth());
        embankment.setCrestElevation(reqVO.getCrestElevation());
        embankment.setStartPoint(reqVO.getStartPoint());
        embankment.setEndPoint(reqVO.getEndPoint());
        embankment.setProjectTask(reqVO.getProjectTask());
        embankment.setEndLocation(reqVO.getEndLocation());
        embankment.setConstructionStatus(reqVO.getConstructionStatus());
        embankment.setManagementDepartment(reqVO.getManagementDepartment());
        embankment.setEmbankmentImages(validateAndConvertImages(reqVO.getEmbankmentImages()));
        embankment.setRemarks(reqVO.getRemarks());

        fillRiverRelation(embankment, reqVO.getRiverChannelId(), reqVO.getRiverSectionId());
        embankmentMapper.insert(embankment);
        return embankment.getId();
    }

    /**
     * 编辑堤防（若修改了堤防名称、堤防代码、区划代码，需要同步更新基础表）
     *
     * <p>允许手工清空所在河道/河段并直接保存。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateEmbankment(EmbankmentSaveReqVO reqVO) {
        updateEmbankmentInternal(reqVO, false);
    }

    /**
     * 导入场景编辑堤防。
     *
     * <p>允许所在河道/河段为空，此时保留现有关联关系不变。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateEmbankmentForImport(EmbankmentSaveReqVO reqVO) {
        updateEmbankmentInternal(reqVO, true);
    }

    /**
     * 为现有堤防补齐或修复关联基础表，并在需要时回写 facility_id / embankment_code。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long ensureFacilityBaseForExisting(YzEmbankmentDO embankment) {
        if (embankment == null || embankment.getId() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_NOT_EXISTS);
        }
        String embankmentCode = resolveEmbankmentCode(embankment.getEmbankmentCode(), embankment.getEmbankmentCode());
        EmbankmentSaveReqVO reqVO = new EmbankmentSaveReqVO();
        reqVO.setEmbankmentName(embankment.getEmbankmentName());
        reqVO.setEmbankmentCode(embankmentCode);
        reqVO.setDivisionCode(embankment.getDivisionCode() == null ? List.of() : List.of(embankment.getDivisionCode()));
        Long facilityId = ensureFacilityBase(reqVO, embankment.getFacilityId());
        if (!Objects.equals(facilityId, embankment.getFacilityId()) || !StrUtil.equals(embankmentCode, embankment.getEmbankmentCode())) {
            YzEmbankmentDO update = new YzEmbankmentDO();
            update.setId(embankment.getId());
            update.setFacilityId(facilityId);
            update.setEmbankmentCode(embankmentCode);
            embankmentMapper.updateById(update);
        }
        return facilityId;
    }

    private void updateEmbankmentInternal(EmbankmentSaveReqVO reqVO, boolean preserveRelationWhenEmpty) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_NOT_EXISTS);
        }
        YzEmbankmentDO exists = embankmentMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_NOT_EXISTS);
        }

        reqVO.setEmbankmentCode(resolveEmbankmentCode(reqVO.getEmbankmentCode(), exists.getEmbankmentCode()));
        Long facilityId = ensureFacilityBase(reqVO, exists.getFacilityId());
        YzEmbankmentDO update = new YzEmbankmentDO();
        update.setId(exists.getId());
        update.setFacilityId(facilityId);
        update.setEmbankmentCode(reqVO.getEmbankmentCode());
        update.setEmbankmentName(reqVO.getEmbankmentName());
        update.setDivisionCode(validateAndConvertDivisionCodes(reqVO.getDivisionCode()));
        update.setLongitude(reqVO.getLongitude());
        update.setLatitude(reqVO.getLatitude());
        update.setRiverBankSide(reqVO.getRiverBankSide());
        update.setCrossBoundaryStatus(reqVO.getCrossBoundaryStatus());
        update.setEmbankmentType(reqVO.getEmbankmentType());
        update.setEmbankmentForm(reqVO.getEmbankmentForm());
        update.setEmbankmentLevel(reqVO.getEmbankmentLevel());
        update.setFloodStandard(reqVO.getFloodStandard());
        update.setDesignReturnPeriod(reqVO.getDesignReturnPeriod());
        update.setLengthM(reqVO.getLengthM());
        update.setStandardLengthM(reqVO.getStandardLengthM());
        update.setElevationSystem(reqVO.getElevationSystem());
        update.setDesignHighTide(reqVO.getDesignHighTide());
        update.setMaxHeight(reqVO.getMaxHeight());
        update.setMinHeight(reqVO.getMinHeight());
        update.setMaxWidth(reqVO.getMaxWidth());
        update.setMinWidth(reqVO.getMinWidth());
        update.setCrestElevation(reqVO.getCrestElevation());
        update.setStartPoint(reqVO.getStartPoint());
        update.setEndPoint(reqVO.getEndPoint());
        update.setProjectTask(reqVO.getProjectTask());
        update.setEndLocation(reqVO.getEndLocation());
        update.setConstructionStatus(reqVO.getConstructionStatus());
        update.setManagementDepartment(reqVO.getManagementDepartment());
        update.setEmbankmentImages(validateAndConvertImages(reqVO.getEmbankmentImages()));
        update.setRemarks(reqVO.getRemarks());

        if (preserveRelationWhenEmpty && reqVO.getRiverChannelId() == null && reqVO.getRiverSectionId() == null) {
            update.setRiverChannelId(exists.getRiverChannelId());
            update.setRiverSectionId(exists.getRiverSectionId());
        } else {
            fillRiverRelation(update, reqVO.getRiverChannelId(), reqVO.getRiverSectionId());
        }
        embankmentMapper.updateById(update);

        // updateById 默认不更新 null 字段，这里确保清空关联或“仅选择河道（无河段）”时能正确落库
        if (update.getRiverChannelId() == null) {
            embankmentMapper.update(null, new LambdaUpdateWrapper<YzEmbankmentDO>()
                    .eq(YzEmbankmentDO::getId, exists.getId())
                    .setSql("river_channel_id = NULL, river_section_id = NULL"));
        } else if (update.getRiverSectionId() == null) {
            embankmentMapper.update(null, new LambdaUpdateWrapper<YzEmbankmentDO>()
                    .eq(YzEmbankmentDO::getId, exists.getId())
                    .setSql("river_section_id = NULL"));
        }
    }

    /**
     * 删除堤防（同时删除对应的基础表记录，避免遗留孤儿数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmbankment(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_NOT_EXISTS);
        }
        YzEmbankmentDO embankment = embankmentMapper.selectById(id);
        if (embankment == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_NOT_EXISTS);
        }
        embankmentMapper.deleteById(id);
        if (embankment.getFacilityId() != null) {
            facilityBaseMapper.deleteById(embankment.getFacilityId());
        }
    }

    /**
     * 创建堤防基础设施记录
     */
    private Long createFacilityBase(EmbankmentSaveReqVO reqVO) {
        Long baseId = SNOWFLAKE.nextId();
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityName(reqVO.getEmbankmentName());
        base.setFacilityType("dike");
        base.setFacilityCode(reqVO.getEmbankmentCode());
        base.setAdminRegionCode(getPrimaryDivisionCode(reqVO.getDivisionCode()));
        base.setSourceType("system");
        facilityBaseMapper.insert(base);
        return baseId;
    }

    /**
     * 确保堤防基础设施存在并同步基础信息
     */
    private Long ensureFacilityBase(EmbankmentSaveReqVO reqVO, Long facilityId) {
        if (facilityId == null) {
            return createFacilityBase(reqVO);
        }
        YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(facilityId);
        if (base == null) {
            return createFacilityBase(reqVO);
        }
        base.setFacilityName(reqVO.getEmbankmentName());
        base.setFacilityType("dike");
        base.setFacilityCode(reqVO.getEmbankmentCode());
        base.setAdminRegionCode(getPrimaryDivisionCode(reqVO.getDivisionCode()));
        facilityBaseMapper.updateById(base);
        return base.getId();
    }

    private String getPrimaryDivisionCode(List<String> divisionCodes) {
        String[] cleaned = validateAndConvertDivisionCodes(divisionCodes);
        return cleaned != null && cleaned.length > 0 ? cleaned[0] : null;
    }

    private String resolveEmbankmentCode(String embankmentCode, String fallbackCode) {
        if (StrUtil.isNotBlank(embankmentCode)) {
            return embankmentCode;
        }
        if (StrUtil.isNotBlank(fallbackCode)) {
            return fallbackCode;
        }
        return String.valueOf(SNOWFLAKE.nextId());
    }

    private String[] validateAndConvertDivisionCodes(List<String> divisionCodes) {
        List<String> cleaned = Optional.ofNullable(divisionCodes).orElse(List.of()).stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
    }

    private String[] validateAndConvertImages(List<String> embankmentImages) {
        List<String> images = Optional.ofNullable(embankmentImages).orElse(List.of());
        if (images.size() > 5) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_IMAGES_TOO_MANY);
        }
        List<String> cleaned = images.stream()
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        if (cleaned.size() > 5) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_IMAGES_TOO_MANY);
        }
        return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
    }

    private void fillRiverRelation(YzEmbankmentDO embankment,
                                   Long riverChannelId,
                                   Long riverSectionId) {
        if (riverChannelId == null && riverSectionId == null) {
            embankment.setRiverChannelId(null);
            embankment.setRiverSectionId(null);
            return;
        }

        if (riverSectionId != null) {
            YzRiverSectionDO section = riverSectionMapper.selectById(riverSectionId);
            if (section == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            Long channelId = section.getRiverChannelId();
            if (riverChannelId != null && !Objects.equals(riverChannelId, channelId)) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EMBANKMENT_RELATION_INVALID);
            }
            YzRiverChannelDO channel = channelId == null ? null : riverChannelMapper.selectById(channelId);
            if (channel == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }
            embankment.setRiverChannelId(channel.getId());
            embankment.setRiverSectionId(section.getId());
            return;
        }

        YzRiverChannelDO channel = riverChannelMapper.selectById(riverChannelId);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        embankment.setRiverChannelId(channel.getId());
        embankment.setRiverSectionId(null);
    }
}

