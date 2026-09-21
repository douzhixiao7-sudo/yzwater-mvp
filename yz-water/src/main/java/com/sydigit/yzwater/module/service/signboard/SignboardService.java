package com.sydigit.yzwater.module.service.signboard;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardSaveReqVO;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 公示牌信息服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class SignboardService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final YzSignboardMapper signboardMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final DictDataCommonApi dictDataApi;

    /**
     * 分页查询公示牌
     */
    public PageResult<SignboardPageRespVO> getSignboardPage(SignboardPageReqVO reqVO) {
        LambdaQueryWrapper<YzSignboardDO> wrapper = signboardMapper.buildQueryWrapper(reqVO);
        PageResult<YzSignboardDO> page = signboardMapper.selectPage(reqVO, wrapper);
        Map<String, String> signboardLevelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        Map<String, String> whdwMap = loadDictLabelMap(ZdConstants.ZD_WHDW);
        Map<String, String> gldwMap = loadDictLabelMap(ZdConstants.ZD_GLDW);
        Map<String, String> qsdwMap = loadDictLabelMap(ZdConstants.ZD_QSDW);
        Map<Long, String> reservoirNameMap = loadReservoirNameMap(page.getList());
        List<SignboardPageRespVO> list = page.getList().stream()
                .map(item -> buildPageResp(item, signboardLevelMap, whdwMap, gldwMap, qsdwMap, reservoirNameMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 获取公示牌详情
     */
    public SignboardSaveReqVO getSignboardDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        YzSignboardDO signboard = getSignboardById(id);
        if (signboard == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        // 历史数据可能存在二维码为空的情况，这里补齐一次，保证前端展示二维码正常
        if (StrUtil.isBlank(signboard.getQrCode())) {
            signboard.setQrCode(generateUniqueQrCode());
            signboardMapper.updateById(signboard);
        }
        SignboardSaveReqVO vo = new SignboardSaveReqVO();
        vo.setId(signboard.getId());
        vo.setSignboardCode(signboard.getSignboardCode());
        vo.setSignboardName(signboard.getSignboardName());
        vo.setSignboardType(signboard.getSignboardType());
        vo.setSignboardLevel(signboard.getSignboardLevel());
        vo.setIsScreenDisplay(signboard.getIsScreenDisplay() == null ? 0 : signboard.getIsScreenDisplay());
        vo.setRiverChannelId(signboard.getRiverChannelId());
        vo.setRiverSectionId(signboard.getRiverSectionId());
        vo.setWaterReservoirId(signboard.getWaterReservoirId());
        vo.setLongitude(signboard.getLongitude());
        vo.setLatitude(signboard.getLatitude());
        vo.setSpecificLocation(signboard.getSpecificLocation());
        vo.setAdminRegion(signboard.getAdminRegion());
        vo.setQrCode(signboard.getQrCode());
        vo.setMaintenanceUnit(toList(signboard.getMaintenanceUnit()));
        vo.setResponsiblePerson(signboard.getResponsiblePerson());
        vo.setManagementUnit(toList(signboard.getManagementUnit()));
        vo.setOwnershipUnit(toList(signboard.getOwnershipUnit()));
        vo.setSignboardImages(signboard.getSignboardImages() == null ? List.of() : List.of(signboard.getSignboardImages()));
        vo.setContent(signboard.getContent());
        vo.setRemarks(signboard.getRemarks());
        vo.setWaterReservoirName(resolveReservoirName(signboard.getWaterReservoirId()));
        return vo;
    }

    /**
     * 新增公示牌
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSignboard(SignboardSaveReqVO reqVO) {
        validateExclusiveRelation(reqVO);
        LocalDateTime now = LocalDateTime.now();
        YzSignboardDO signboard = new YzSignboardDO();
        signboard.setId(SNOWFLAKE.nextId());
        signboard.setSignboardCode(reqVO.getSignboardCode());
        signboard.setSignboardName(reqVO.getSignboardName());
        signboard.setSignboardType(reqVO.getSignboardType());
        signboard.setSignboardLevel(reqVO.getSignboardLevel());
        Integer isScreenDisplay = Objects.equals(reqVO.getIsScreenDisplay(), 1) ? 1 : 0;
        signboard.setIsScreenDisplay(isScreenDisplay);
        signboard.setUpdateTime(now);
        signboard.setLongitude(reqVO.getLongitude());
        signboard.setLatitude(reqVO.getLatitude());
        signboard.setSpecificLocation(reqVO.getSpecificLocation());
        signboard.setAdminRegion(reqVO.getAdminRegion());
        signboard.setMaintenanceUnit(toArray(reqVO.getMaintenanceUnit()));
        signboard.setResponsiblePerson(reqVO.getResponsiblePerson());
        signboard.setManagementUnit(toArray(reqVO.getManagementUnit()));
        signboard.setOwnershipUnit(toArray(reqVO.getOwnershipUnit()));
        signboard.setSignboardImages(validateAndConvertImages(reqVO.getSignboardImages()));
        signboard.setContent(reqVO.getContent());
        signboard.setRemarks(reqVO.getRemarks());
        fillRiverRelation(signboard, reqVO.getRiverChannelId(), reqVO.getRiverSectionId());
        fillReservoirRelation(signboard, reqVO.getWaterReservoirId());
        fillReferenceRelation(signboard);
        signboard.setQrCode(generateUniqueQrCode());
        signboardMapper.insert(signboard);
        return signboard.getId();
    }

    /**
     * 更新公示牌
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSignboard(SignboardSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        validateExclusiveRelation(reqVO);
        LocalDateTime now = LocalDateTime.now();
        YzSignboardDO signboard = getSignboardById(reqVO.getId());
        if (signboard == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        signboard.setSignboardCode(reqVO.getSignboardCode());
        signboard.setSignboardName(reqVO.getSignboardName());
        signboard.setSignboardType(reqVO.getSignboardType());
        signboard.setSignboardLevel(reqVO.getSignboardLevel());
        Integer isScreenDisplay = Objects.equals(reqVO.getIsScreenDisplay(), 1) ? 1 : 0;
        signboard.setIsScreenDisplay(isScreenDisplay);
        signboard.setUpdateTime(now);
        signboard.setLongitude(reqVO.getLongitude());
        signboard.setLatitude(reqVO.getLatitude());
        signboard.setSpecificLocation(reqVO.getSpecificLocation());
        signboard.setAdminRegion(reqVO.getAdminRegion());
        signboard.setMaintenanceUnit(toArray(reqVO.getMaintenanceUnit()));
        signboard.setResponsiblePerson(reqVO.getResponsiblePerson());
        signboard.setManagementUnit(toArray(reqVO.getManagementUnit()));
        signboard.setOwnershipUnit(toArray(reqVO.getOwnershipUnit()));
        signboard.setSignboardImages(validateAndConvertImages(reqVO.getSignboardImages()));
        signboard.setContent(reqVO.getContent());
        signboard.setRemarks(reqVO.getRemarks());
        fillRiverRelation(signboard, reqVO.getRiverChannelId(), reqVO.getRiverSectionId());
        fillReservoirRelation(signboard, reqVO.getWaterReservoirId());
        fillReferenceRelation(signboard);
        // qrCode 为固定标识，不允许被更新；如历史数据为空，则补齐一次
        if (StrUtil.isBlank(signboard.getQrCode())) {
            signboard.setQrCode(generateUniqueQrCode());
        }
        signboardMapper.updateById(signboard);
        // 兜底：确保“是否大屏展示”字段在任意策略配置下都能稳定更新
        signboardMapper.update(null, new LambdaUpdateWrapper<YzSignboardDO>()
                .eq(YzSignboardDO::getId, signboard.getId())
                .set(YzSignboardDO::getIsScreenDisplay, isScreenDisplay)
                .set(YzSignboardDO::getUpdateTime, now));

        // updateById 默认不更新 null 字段，清空公示牌图片时须显式将 signboard_images 置 NULL
        if (signboard.getSignboardImages() == null) {
            signboardMapper.update(null, new LambdaUpdateWrapper<YzSignboardDO>()
                    .eq(YzSignboardDO::getId, signboard.getId())
                    .set(YzSignboardDO::getUpdateTime, now)
                    .setSql("signboard_images = NULL"));
        }

        // updateById 默认不更新 null 字段，这里确保清空关联信息时能正确把对应字段置空
        if (signboard.getRiverChannelId() == null && signboard.getRiverSectionId() == null) {
            signboardMapper.update(null, new LambdaUpdateWrapper<YzSignboardDO>()
                    .eq(YzSignboardDO::getId, signboard.getId())
                    .set(YzSignboardDO::getUpdateTime, now)
                    .setSql("river_channel_id = NULL, river_channel_name = NULL, river_section_id = NULL, river_section_name = NULL"));
        } else if (signboard.getRiverSectionId() == null) {
            signboardMapper.update(null, new LambdaUpdateWrapper<YzSignboardDO>()
                    .eq(YzSignboardDO::getId, signboard.getId())
                    .set(YzSignboardDO::getUpdateTime, now)
                    .setSql("river_section_id = NULL, river_section_name = NULL"));
        }
        // updateById 默认不更新 null 字段，这里确保水库关联置空时能把 water_reservoir_id 置空
        if (signboard.getWaterReservoirId() == null) {
            signboardMapper.update(null, new LambdaUpdateWrapper<YzSignboardDO>()
                    .eq(YzSignboardDO::getId, signboard.getId())
                    .set(YzSignboardDO::getUpdateTime, now)
                    .setSql("water_reservoir_id = NULL"));
        }
        // updateById 默认不更新 null 字段，这里确保关联对象置空时能把 reference_id/reference_type 置空
        if (signboard.getRiverChannelId() == null && signboard.getRiverSectionId() == null && signboard.getWaterReservoirId() == null) {
            signboardMapper.update(null, new LambdaUpdateWrapper<YzSignboardDO>()
                    .eq(YzSignboardDO::getId, signboard.getId())
                    .set(YzSignboardDO::getUpdateTime, now)
                    .setSql("reference_id = NULL, reference_type = NULL"));
        }
    }

    /**
     * 删除公示牌
     */
    public void deleteSignboard(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        YzSignboardDO signboard = getSignboardById(id);
        if (signboard == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        signboardMapper.deleteById(id);
    }

    /**
     * 历史数据可能存在重复主键脏数据，这里优先取第一条，避免详情/编辑直接抛出 selectOne 异常。
     */
    private YzSignboardDO getSignboardById(Long id) {
        if (id == null) {
            return null;
        }
        return signboardMapper.selectFirstOne(YzSignboardDO::getId, id);
    }

    /**
     * 导出公示牌数据（不分页）
     */
    public List<SignboardExportExcelVO> getSignboardExportList(SignboardPageReqVO reqVO) {
        LambdaQueryWrapper<YzSignboardDO> wrapper = signboardMapper.buildQueryWrapper(reqVO);
        List<YzSignboardDO> list = signboardMapper.selectList(wrapper);
        Map<String, String> whdwMap = loadDictLabelMap(ZdConstants.ZD_WHDW);
        Map<String, String> gldwMap = loadDictLabelMap(ZdConstants.ZD_GLDW);
        Map<String, String> qsdwMap = loadDictLabelMap(ZdConstants.ZD_QSDW);
        return list.stream()
                .map(item -> buildExportVO(item, whdwMap, gldwMap, qsdwMap))
                .collect(Collectors.toList());
    }

    private String[] validateAndConvertImages(List<String> signboardImages) {
        List<String> images = Optional.ofNullable(signboardImages).orElse(List.of());
        if (images.size() > 5) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_IMAGES_TOO_MANY);
        }
        List<String> cleaned = images.stream()
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        if (cleaned.size() > 5) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_IMAGES_TOO_MANY);
        }
        return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
    }

    private String[] toArray(List<String> list) {
        List<String> values = Optional.ofNullable(list).orElse(List.of()).stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return values.isEmpty() ? null : values.toArray(new String[0]);
    }

    private List<String> toList(String[] array) {
        return array == null ? List.of() : List.of(array);
    }

    private void validateExclusiveRelation(SignboardSaveReqVO reqVO) {
        if (reqVO == null) {
            return;
        }
        boolean hasRiver = reqVO.getRiverChannelId() != null || reqVO.getRiverSectionId() != null;
        boolean hasReservoir = reqVO.getWaterReservoirId() != null;
        if (hasRiver && hasReservoir) {
            // 公示牌只允许关联河道/河段/水库中的一种，避免 reference 字段出现歧义
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_RELATION_INVALID);
        }
    }

    private void fillRiverRelation(YzSignboardDO signboard, Long riverChannelId, Long riverSectionId) {
        if (riverChannelId == null && riverSectionId == null) {
            signboard.setRiverChannelId(null);
            signboard.setRiverChannelName(null);
            signboard.setRiverSectionId(null);
            signboard.setRiverSectionName(null);
            return;
        }

        if (riverSectionId != null) {
            YzRiverSectionDO section = riverSectionMapper.selectById(riverSectionId);
            if (section == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            Long channelId = section.getRiverChannelId();
            if (riverChannelId != null && !Objects.equals(riverChannelId, channelId)) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_RELATION_INVALID);
            }
            YzRiverChannelDO channel = channelId == null ? null : riverChannelMapper.selectById(channelId);
            if (channel == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }

            signboard.setRiverChannelId(channel.getId());
            signboard.setRiverChannelName(channel.getRiverName());
            signboard.setRiverSectionId(section.getId());
            signboard.setRiverSectionName(section.getSectionName());
            return;
        }

        YzRiverChannelDO channel = riverChannelMapper.selectById(riverChannelId);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        signboard.setRiverChannelId(channel.getId());
        signboard.setRiverChannelName(channel.getRiverName());
        signboard.setRiverSectionId(null);
        signboard.setRiverSectionName(null);
    }

    private void fillReservoirRelation(YzSignboardDO signboard, Long waterReservoirId) {
        if (waterReservoirId == null) {
            signboard.setWaterReservoirId(null);
            return;
        }
        YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(waterReservoirId);
        if (reservoir == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        signboard.setWaterReservoirId(reservoir.getId());
    }

    private void fillReferenceRelation(YzSignboardDO signboard) {
        if (signboard == null) {
            return;
        }
        // 关联水库优先级最高（理论上与河道/河段互斥，这里仅做兜底）
        if (signboard.getWaterReservoirId() != null) {
            signboard.setReferenceId(signboard.getWaterReservoirId());
            signboard.setReferenceType(ReferenceTypeConstants.RESERVOIR);
            return;
        }
        if (signboard.getRiverSectionId() != null) {
            signboard.setReferenceId(signboard.getRiverSectionId());
            signboard.setReferenceType(ReferenceTypeConstants.RIVER_SECTION);
            return;
        }
        if (signboard.getRiverChannelId() != null) {
            signboard.setReferenceId(signboard.getRiverChannelId());
            signboard.setReferenceType(ReferenceTypeConstants.RIVER);
            return;
        }
        signboard.setReferenceId(null);
        signboard.setReferenceType(null);
    }

    private String resolveReservoirName(Long waterReservoirId) {
        if (waterReservoirId == null) {
            return null;
        }
        YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(waterReservoirId);
        return reservoir == null ? null : reservoir.getReservoirName();
    }

    /**
     * 生成公示牌固定二维码标识：SecureRandom 生成 20~32 字节，再 Base64Url 编码（不带 padding）
     */
    private String generateUniqueQrCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            int size = 20 + SECURE_RANDOM.nextInt(13);
            byte[] bytes = new byte[size];
            SECURE_RANDOM.nextBytes(bytes);
            String qrCode = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            YzSignboardDO exists = signboardMapper.selectOne(YzSignboardDO::getQrCode, qrCode);
            if (exists == null) {
                return qrCode;
            }
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_QR_CODE_GENERATE_FAIL);
    }

    private SignboardPageRespVO buildPageResp(YzSignboardDO item,
                                              Map<String, String> signboardLevelMap,
                                              Map<String, String> maintenanceUnitMap,
                                              Map<String, String> managementUnitMap,
                                              Map<String, String> ownershipUnitMap,
                                              Map<Long, String> reservoirNameMap) {
        SignboardPageRespVO resp = new SignboardPageRespVO();
        resp.setId(item.getId());
        resp.setSignboardCode(item.getSignboardCode());
        resp.setSignboardName(item.getSignboardName());
        resp.setSignboardLevel(item.getSignboardLevel());
        resp.setSignboardLevelLabel(StrUtil.blankToDefault(signboardLevelMap.get(item.getSignboardLevel()), item.getSignboardLevel()));
        resp.setIsScreenDisplay(item.getIsScreenDisplay() == null ? 0 : item.getIsScreenDisplay());
        resp.setReferenceType(item.getReferenceType());
        resp.setRiverChannelName(item.getRiverChannelName());
        resp.setRiverSectionName(item.getRiverSectionName());
        resp.setSpecificLocation(item.getSpecificLocation());
        resp.setAdminRegion(item.getAdminRegion());
        resp.setMaintenanceUnit(joinDisplay(item.getMaintenanceUnit(), maintenanceUnitMap));
        resp.setResponsiblePerson(item.getResponsiblePerson());
        resp.setManagementUnit(joinDisplay(item.getManagementUnit(), managementUnitMap));
        resp.setOwnershipUnit(joinDisplay(item.getOwnershipUnit(), ownershipUnitMap));
        resp.setRiverName(resolveRiverName(item.getRiverChannelName(), item.getRiverSectionName()));
        Long waterReservoirId = item.getWaterReservoirId();
        resp.setWaterReservoirName(waterReservoirId == null ? null : reservoirNameMap.get(waterReservoirId));
        return resp;
    }

    private Map<Long, String> loadReservoirNameMap(List<YzSignboardDO> signboards) {
        if (signboards == null || signboards.isEmpty()) {
            return Map.of();
        }
        List<Long> reservoirIds = signboards.stream()
                .map(YzSignboardDO::getWaterReservoirId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (reservoirIds.isEmpty()) {
            return Map.of();
        }
        List<YzWaterReservoirDO> reservoirs = waterReservoirMapper.selectBatchIds(reservoirIds);
        if (reservoirs == null || reservoirs.isEmpty()) {
            return Map.of();
        }
        return reservoirs.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(YzWaterReservoirDO::getId,
                        item -> StrUtil.blankToDefault(item.getReservoirName(), ""),
                        (left, right) -> left));
    }

    private SignboardExportExcelVO buildExportVO(YzSignboardDO item,
                                                 Map<String, String> maintenanceUnitMap,
                                                 Map<String, String> managementUnitMap,
                                                 Map<String, String> ownershipUnitMap) {
        SignboardExportExcelVO vo = new SignboardExportExcelVO();
        vo.setSignboardCode(item.getSignboardCode());
        vo.setSignboardName(item.getSignboardName());
        vo.setSpecificLocation(item.getSpecificLocation());
        vo.setAdminRegion(item.getAdminRegion());
        vo.setMaintenanceUnit(joinDisplay(item.getMaintenanceUnit(), maintenanceUnitMap));
        vo.setResponsiblePerson(item.getResponsiblePerson());
        vo.setManagementUnit(joinDisplay(item.getManagementUnit(), managementUnitMap));
        vo.setOwnershipUnit(joinDisplay(item.getOwnershipUnit(), ownershipUnitMap));
        vo.setRiverName(resolveRiverName(item.getRiverChannelName(), item.getRiverSectionName()));
        return vo;
    }

    private String resolveRiverName(String riverChannelName, String riverSectionName) {
        if (StrUtil.isNotBlank(riverChannelName)) {
            return riverChannelName;
        }
        return riverSectionName;
    }

    private String joinDisplay(String[] values, Map<String, String> labelMap) {
        if (values == null || values.length == 0) {
            return null;
        }
        List<String> labels = List.of(values).stream()
                .filter(StrUtil::isNotBlank)
                .map(value -> StrUtil.blankToDefault(labelMap.get(value), value))
                .distinct()
                .collect(Collectors.toList());
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
}
