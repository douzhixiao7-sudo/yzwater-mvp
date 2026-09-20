package com.sydigit.yzwater.module.controller.app;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.app.vo.signboard.AppSignboardInfoRespVO;
import com.sydigit.yzwater.module.controller.app.vo.signboard.AppSignboardReservoirHeadRespVO;
import com.sydigit.yzwater.module.controller.app.vo.signboard.AppSignboardReservoirRespVO;
import com.sydigit.yzwater.module.controller.app.vo.signboard.AppSignboardRiverInfoRespVO;
import com.sydigit.yzwater.module.controller.app.vo.signboard.AppSignboardRiverSectionRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelQrSnapshotVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadQrVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionQrVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseBfDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardBfDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseBfMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardBfMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.service.river.RiverChannelBfService;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.service.area.SystemAreaService;
import com.sydigit.yzwater.module.util.GeometryWktUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 手机端 - 公示牌二维码（仅查询 {@code yz_signboard_bf} 及 BF 河段/水库/设施；河长管理数据仍取自共享业务表）。
 */
@Tag(name = "仪征手机端 - 公示牌二维码")
@RestController
@RequestMapping("signboard")
@Validated
public class AppSignboardQrController {

    /** App 扫码仅读 yz_signboard_bf，与后台正式公示牌数据隔离 */
    private final YzSignboardBfMapper signboardBfMapper;
    private final YzRiverSectionBfMapper riverSectionBfMapper;
    private final YzWaterReservoirBfMapper waterReservoirBfMapper;
    private final YzRiverChannelManagementMapper riverChannelManagementMapper;
    private final YzWaterFacilityBaseBfMapper facilityBaseBfMapper;
    private final RiverChannelBfService riverChannelBfService;
    private final DictDataCommonApi dictDataApi;
    private final SystemAreaService systemAreaService;

    public AppSignboardQrController(YzSignboardBfMapper signboardBfMapper,
                                    YzRiverSectionBfMapper riverSectionBfMapper,
                                    YzWaterReservoirBfMapper waterReservoirBfMapper,
                                    YzRiverChannelManagementMapper riverChannelManagementMapper,
                                    YzWaterFacilityBaseBfMapper facilityBaseBfMapper,
                                    RiverChannelBfService riverChannelBfService,
                                    DictDataCommonApi dictDataApi,
                                    SystemAreaService systemAreaService) {
        this.signboardBfMapper = signboardBfMapper;
        this.riverSectionBfMapper = riverSectionBfMapper;
        this.waterReservoirBfMapper = waterReservoirBfMapper;
        this.riverChannelManagementMapper = riverChannelManagementMapper;
        this.facilityBaseBfMapper = facilityBaseBfMapper;
        this.riverChannelBfService = riverChannelBfService;
        this.dictDataApi = dictDataApi;
        this.systemAreaService = systemAreaService;
    }

    @GetMapping("info/by-qr/{qrCode}")
    @Operation(summary = "扫码查询公示牌关联的河道/河段/水库信息（支持多河道、多河段、多水库）")
    @PermitAll
    public CommonResult<AppSignboardRiverInfoRespVO> getByQr(@Valid @PathVariable("qrCode") String qrCode) {
        if (StrUtil.isBlank(qrCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }

        List<YzSignboardBfDO> signboards = signboardBfMapper.selectList(new LambdaQueryWrapper<YzSignboardBfDO>()
                .eq(YzSignboardBfDO::getQrCode, qrCode)
                .orderByDesc(YzSignboardBfDO::getId));
        if (signboards == null || signboards.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }

        YzSignboardBfDO primary = signboards.get(0);
        Set<Long> signboardIds = new LinkedHashSet<>();
        for (YzSignboardBfDO signboard : signboards) {
            if (signboard != null && signboard.getId() != null) {
                signboardIds.add(signboard.getId());
            }
        }

        String referenceType = resolvePrimaryReferenceType(primary);
        Long referenceId = resolvePrimaryReferenceId(primary, referenceType);

        List<RiverChannelQrSnapshotVO> rivers = List.of();
        List<YzRiverSectionBfDO> relatedSections = List.of();
        Set<Long> reservoirIds = new LinkedHashSet<>();
        Long resolvedRiverChannelId = primary.getRiverChannelId();
        Long resolvedRiverSectionId = primary.getRiverSectionId();
        Long resolvedWaterReservoirId = primary.getWaterReservoirId();

        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            resolvedRiverChannelId = referenceId;
            rivers = List.of(riverChannelBfService.getRiverChannelQrSnapshotById(referenceId));
        } else if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            resolvedRiverSectionId = referenceId;
            relatedSections = loadAndCheckRiverSections(Set.of(referenceId));
            YzRiverSectionBfDO section = relatedSections.get(0);
            if (section.getRiverChannelId() == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
            }
            resolvedRiverChannelId = section.getRiverChannelId();
            rivers = List.of(riverChannelBfService.getRiverChannelQrSnapshotById(section.getRiverChannelId()));
        } else if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            resolvedWaterReservoirId = referenceId;
            reservoirIds.add(referenceId);
        }
        Map<String, String> maintenanceUnitMap = loadDictLabelMap(ZdConstants.ZD_WHDW);
        List<String> maintenanceLabels = resolveLabels(primary.getMaintenanceUnit(), maintenanceUnitMap);
        formatRiverChiefDisplayForSignboard(rivers, resolvedRiverSectionId, maintenanceLabels,
                primary.getRiverSectionName());
        sortRiverChannelHeads(rivers);

        AppSignboardRiverInfoRespVO respVO = new AppSignboardRiverInfoRespVO();
        respVO.setSignboardId(primary.getId());
        respVO.setSignboardIds(new ArrayList<>(signboardIds));
        respVO.setQrCode(primary.getQrCode());
        respVO.setSignboardName(primary.getSignboardName());
        respVO.setSignboardCode(primary.getSignboardCode());
        respVO.setAdminRegion(resolveAreaName(primary.getAdminRegion()));
        respVO.setMaintenanceUnitLabels(maintenanceLabels);
        respVO.setRiverChannelId(resolvedRiverChannelId);
        respVO.setRiverSectionId(resolvedRiverSectionId);
        respVO.setWaterReservoirId(resolvedWaterReservoirId);
        respVO.setRiverChannelName(primary.getRiverChannelName());
        respVO.setRiverSectionName(primary.getRiverSectionName());
        respVO.setLongitude(primary.getLongitude());
        respVO.setLatitude(primary.getLatitude());
        respVO.setSpecificLocation(primary.getSpecificLocation());
        respVO.setSignboardImages(primary.getSignboardImages() == null ? List.of() : List.of(primary.getSignboardImages()));
      //  respVO.setRiver(rivers.isEmpty() ? null : rivers.get(0));
        respVO.setRivers(rivers);
        respVO.setRiverSections(buildRiverSectionRespList(relatedSections));
        respVO.setWaterReservoirs(buildReservoirRespList(reservoirIds));
        return success(respVO);
    }

    /**
     * 解析公示牌主关联类型（优先 reference_type，兼容旧字段）。
     */
    private String resolvePrimaryReferenceType(YzSignboardBfDO signboard) {
        if (signboard == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        String type = StrUtil.trimToNull(signboard.getReferenceType());
        if (StrUtil.isNotBlank(type)) {
            String normalized = type.toLowerCase();
            if (ReferenceTypeConstants.RIVER.equals(normalized)
                    || ReferenceTypeConstants.RIVER_SECTION.equals(normalized)
                    || ReferenceTypeConstants.RESERVOIR.equals(normalized)) {
                return normalized;
            }
        }
        if (signboard.getWaterReservoirId() != null) {
            return ReferenceTypeConstants.RESERVOIR;
        }
        if (signboard.getRiverSectionId() != null) {
            return ReferenceTypeConstants.RIVER_SECTION;
        }
        if (signboard.getRiverChannelId() != null) {
            return ReferenceTypeConstants.RIVER;
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
    }

    /**
     * 解析公示牌主关联ID（优先 reference_id，兼容旧字段）。
     */
    private Long resolvePrimaryReferenceId(YzSignboardBfDO signboard, String referenceType) {
        if (signboard == null || StrUtil.isBlank(referenceType)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        Long referenceId = signboard.getReferenceId();
        if (referenceId != null) {
            return referenceId;
        }
        if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
            if (signboard.getRiverChannelId() != null) {
                return signboard.getRiverChannelId();
            }
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
            if (signboard.getRiverSectionId() != null) {
                return signboard.getRiverSectionId();
            }
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
            if (signboard.getWaterReservoirId() != null) {
                return signboard.getWaterReservoirId();
            }
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
    }

    @GetMapping("/issue/by-qr/{qrCode}")
    @Operation(summary = "扫码查询公示牌信息")
    @PermitAll
    public CommonResult<AppSignboardInfoRespVO> getSignboardInfoByQr(@Valid @PathVariable("qrCode") String qrCode) {
        if (StrUtil.isBlank(qrCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }

        List<YzSignboardBfDO> signboards = signboardBfMapper.selectList(new LambdaQueryWrapper<YzSignboardBfDO>()
                .eq(YzSignboardBfDO::getQrCode, qrCode)
                .orderByDesc(YzSignboardBfDO::getId));
        if (signboards == null || signboards.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_NOT_EXISTS);
        }
        YzSignboardBfDO signboard = signboards.get(0);

        Map<String, String> signboardTypeMap = loadDictLabelMap(ZdConstants.ZD_GSPLX);
        Map<String, String> maintenanceUnitMap = loadDictLabelMap(ZdConstants.ZD_WHDW);
        Map<String, String> managementUnitMap = loadDictLabelMap(ZdConstants.ZD_GLDW);
        Map<String, String> ownershipUnitMap = loadDictLabelMap(ZdConstants.ZD_QSDW);

        AppSignboardInfoRespVO respVO = new AppSignboardInfoRespVO();
        respVO.setSignboardId(signboard.getId());
        respVO.setQrCode(signboard.getQrCode());
        respVO.setSignboardCode(signboard.getSignboardCode());
        respVO.setSignboardName(signboard.getSignboardName());
        respVO.setSignboardTypeLabel(resolveLabel(signboard.getSignboardType(), signboardTypeMap));
        respVO.setRiverChannelName(signboard.getRiverChannelName());
        respVO.setRiverSectionName(signboard.getRiverSectionName());
        respVO.setLongitude(signboard.getLongitude());
        respVO.setLatitude(signboard.getLatitude());
        respVO.setSpecificLocation(signboard.getSpecificLocation());
        respVO.setAdminRegion(signboard.getAdminRegion());
        respVO.setMaintenanceUnitLabels(resolveLabels(signboard.getMaintenanceUnit(), maintenanceUnitMap));
        respVO.setResponsiblePerson(signboard.getResponsiblePerson());
        respVO.setManagementUnitLabels(resolveLabels(signboard.getManagementUnit(), managementUnitMap));
        respVO.setOwnershipUnitLabels(resolveLabels(signboard.getOwnershipUnit(), ownershipUnitMap));
        respVO.setContent(signboard.getContent());
        respVO.setRemarks(signboard.getRemarks());
        return success(respVO);
    }

    /**
     * 生成固定二维码内容（仅承载访问入口，避免信息变更导致二维码失效）。
     */
    private String buildContentUrl(String qrCode) {
        return "/signboard/by-qr/" + qrCode;
    }

    /**
     * 生成二维码 PNG 图片并输出为 Base64（UTF-8 编码，避免中文乱码）。
     */
    private String buildQrcodeBase64(String content) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            QrConfig config = new QrConfig(360, 360);
            config.setCharset(StandardCharsets.UTF_8);
            BufferedImage image = QrCodeUtil.generate(content, config);
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_SIGNBOARD_QRCODE_FAIL);
        }
    }

    private List<YzRiverSectionBfDO> loadAndCheckRiverSections(Set<Long> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty()) {
            return List.of();
        }
        List<YzRiverSectionBfDO> sectionList = riverSectionBfMapper.selectBatchIds(sectionIds);
        Map<Long, YzRiverSectionBfDO> sectionMap = sectionList.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzRiverSectionBfDO::getId, item -> item, (a, b) -> a));
        List<YzRiverSectionBfDO> ordered = new ArrayList<>(sectionIds.size());
        for (Long id : sectionIds) {
            YzRiverSectionBfDO section = sectionMap.get(id);
            if (section == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
            }
            ordered.add(section);
        }
        return ordered;
    }

    private List<AppSignboardRiverSectionRespVO> buildRiverSectionRespList(List<YzRiverSectionBfDO> sections) {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }
        Set<Long> facilityIds = sections.stream()
                .map(YzRiverSectionBfDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, YzWaterFacilityBaseBfDO> facilityMap = facilityIds.isEmpty()
                ? Map.of()
                : facilityBaseBfMapper.selectBatchIds(facilityIds).stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzWaterFacilityBaseBfDO::getId, item -> item, (a, b) -> a));

        return sections.stream().map(section -> {
            AppSignboardRiverSectionRespVO vo = new AppSignboardRiverSectionRespVO();
            vo.setId(section.getId());
            vo.setRiverChannelId(section.getRiverChannelId());
            vo.setFacilityId(section.getFacilityId());
            vo.setSectionName(section.getSectionName());
            vo.setStartPoint(section.getStartPoint());
            vo.setEndPoint(section.getEndPoint());
            vo.setStartLongitude(section.getStartLongitude());
            vo.setStartLatitude(section.getStartLatitude());
            vo.setEndLongitude(section.getEndLongitude());
            vo.setEndLatitude(section.getEndLatitude());

            YzWaterFacilityBaseBfDO facility = facilityMap.get(section.getFacilityId());
            if (facility != null && facility.getGeom() != null) {
                vo.setGeomType(facility.getGeomType());
                vo.setSrid(facility.getSrid());
                vo.setGeomWkt(GeometryWktUtils.toWktWithSrid(facility.getGeom()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private List<AppSignboardReservoirRespVO> buildReservoirRespList(Set<Long> reservoirIds) {
        if (reservoirIds == null || reservoirIds.isEmpty()) {
            return List.of();
        }
        Map<String, String> reservoirScaleLabelMap = loadDictLabelMap(ZdConstants.ZD_SKGM);
        Map<String, String> managementUnitLabelMap = loadDictLabelMap(ZdConstants.ZD_GLDW);
        Map<String, String> reservoirNatureLabelMap = loadDictLabelMap(ZdConstants.ZD_SKXZ);
        Map<String, String> headLevelLabelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);

        List<YzWaterReservoirBfDO> reservoirs = waterReservoirBfMapper.selectBatchIds(reservoirIds);
        Map<Long, YzWaterReservoirBfDO> reservoirMap = reservoirs.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzWaterReservoirBfDO::getId, item -> item, (a, b) -> a));

        Set<Long> facilityIds = reservoirs.stream()
                .map(YzWaterReservoirBfDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, YzWaterFacilityBaseBfDO> facilityMap = facilityIds.isEmpty()
                ? Map.of()
                : facilityBaseBfMapper.selectBatchIds(facilityIds).stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzWaterFacilityBaseBfDO::getId, item -> item, (a, b) -> a));

        // 查询水库下的河长信息（可能多条），并按水库ID分组
        Map<Long, List<AppSignboardReservoirHeadRespVO>> headsByReservoirId = new HashMap<>();
        List<YzRiverChannelManagementDO> managementList = riverChannelManagementMapper.selectList(
                new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                        .in(YzRiverChannelManagementDO::getWaterReservoirId, reservoirIds)
                        .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                        .orderByAsc(YzRiverChannelManagementDO::getWaterReservoirId)
                        .orderByAsc(YzRiverChannelManagementDO::getId));
        for (YzRiverChannelManagementDO management : managementList) {
            if (management == null || management.getWaterReservoirId() == null) {
                continue;
            }
            AppSignboardReservoirHeadRespVO head = new AppSignboardReservoirHeadRespVO();
            head.setId(management.getId());
            head.setHeadLevel(management.getHeadLevel());
            head.setHeadLevelLabel(resolveLabel(management.getHeadLevel(), headLevelLabelMap));
            head.setHeadName(management.getHeadName());
            head.setHeadPosition(management.getHeadPosition());
            head.setHeadUnit(management.getHeadUnit());
            head.setHeadContact(management.getHeadContact());
            YzWaterReservoirBfDO reservoir = reservoirMap.get(management.getWaterReservoirId());
            head.setResponsibilities(reservoir == null ? null : reservoir.getResponsibilities());
            head.setRemarks(management.getRemarks());
            headsByReservoirId
                    .computeIfAbsent(management.getWaterReservoirId(), ignored -> new ArrayList<>())
                    .add(head);
        }

        List<AppSignboardReservoirRespVO> result = new ArrayList<>(reservoirIds.size());
        for (Long reservoirId : reservoirIds) {
            YzWaterReservoirBfDO reservoir = reservoirMap.get(reservoirId);
            if (reservoir == null) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RESERVOIR_NOT_EXISTS);
            }
            AppSignboardReservoirRespVO vo = new AppSignboardReservoirRespVO();
            vo.setId(reservoir.getId());
            vo.setReservoirCode(reservoir.getReservoirCode());
            vo.setReservoirName(reservoir.getReservoirName());
            vo.setReservoirScale(resolveLabel(reservoir.getReservoirScale(), reservoirScaleLabelMap));
            vo.setTownship(reservoir.getTownship() == null ? null : String.join("、", reservoir.getTownship()));
            vo.setLocation(reservoir.getLocation());
            vo.setManagementUnit(resolveLabelArray(reservoir.getManagementUnit(), managementUnitLabelMap));
            List<AppSignboardReservoirHeadRespVO> heads = headsByReservoirId.get(reservoirId);
            if (heads == null || heads.isEmpty()) {
                vo.setHeads(List.of());
            } else {
                // 按河长级别正序返回水库河长信息
                sortReservoirHeads(heads);
                vo.setHeads(heads);
            }
           // vo.setReservoirPhotos(reservoir.getReservoirPhotos() == null ? List.of() : List.of(reservoir.getReservoirPhotos()));
            vo.setLongitude(reservoir.getLongitude());
            vo.setLatitude(reservoir.getLatitude());
            vo.setSupervisingDepartment(reservoir.getSupervisingDepartment());
            vo.setReservoirNature(resolveLabel(reservoir.getReservoirNature(), reservoirNatureLabelMap));
            vo.setIrrigationArea(reservoir.getIrrigationArea());
            vo.setDesignIrrigationArea(reservoir.getDesignIrrigationArea());
            vo.setActualIrrigationArea(reservoir.getActualIrrigationArea());
            vo.setProtectionArea(reservoir.getProtectionArea());
            vo.setDownstreamFacilities(reservoir.getDownstreamFacilities());
            vo.setWaterSupplyTarget(reservoir.getWaterSupplyTarget());
            vo.setCatchmentArea(reservoir.getCatchmentArea());
            vo.setElevationDatum(reservoir.getElevationDatum());
            vo.setSeismicIntensity(reservoir.getSeismicIntensity());
            vo.setCompletionDate(reservoir.getCompletionDate());
            vo.setReinforcementStartDate(reservoir.getReinforcementStartDate());
            vo.setReinforcementEndDate(reservoir.getReinforcementEndDate());

            vo.setDesignFloodStandard(reservoir.getDesignFloodStandard());
            vo.setVerifiedFloodStandard(reservoir.getVerifiedFloodStandard());
            vo.setDesignReturnPeriod(reservoir.getDesignReturnPeriod());
            vo.setCheckReturnPeriod(reservoir.getCheckReturnPeriod());
            vo.setTotalCapacity(reservoir.getTotalCapacity());
            vo.setActiveCapacity(reservoir.getActiveCapacity());
            vo.setFloodControlCapacity(reservoir.getFloodControlCapacity());
            vo.setDeadCapacity(reservoir.getDeadCapacity());
            vo.setVerifiedFloodLevel(reservoir.getVerifiedFloodLevel());
            vo.setDesignFloodLevel(reservoir.getDesignFloodLevel());
            vo.setNormalOperatingLevel(reservoir.getNormalOperatingLevel());
            vo.setFloodLimitLevel(reservoir.getFloodLimitLevel());
            vo.setDeadLevel(reservoir.getDeadLevel());
            vo.setDamCrestElevation(reservoir.getDamCrestElevation());
            vo.setDamTopWidth(reservoir.getDamTopWidth());
            vo.setDamTopHeight(reservoir.getDamTopHeight());
            vo.setMaxDamHeight(reservoir.getMaxDamHeight());
            vo.setDamTopLength(reservoir.getDamTopLength());
            vo.setWaveWallCrestElevation(reservoir.getWaveWallCrestElevation());
            vo.setDamRoadSurfaceType(reservoir.getDamRoadSurfaceType());
            vo.setSeepageControlType(reservoir.getSeepageControlType());
            vo.setSeepagePileRange(reservoir.getSeepagePileRange());
            vo.setSeepageElevRange(reservoir.getSeepageElevRange());
            vo.setUpstreamSlopeType(reservoir.getUpstreamSlopeType());
            vo.setUpstreamSlopeElevation(reservoir.getUpstreamSlopeElevation());
            vo.setUpstreamSlopeRatio(reservoir.getUpstreamSlopeRatio());
            vo.setDownstreamSlopeRatio(reservoir.getDownstreamSlopeRatio());
            vo.setSlopeProtectionType(reservoir.getSlopeProtectionType());
            vo.setSlopeProtectionElevRange(reservoir.getSlopeProtectionElevRange());
            vo.setDownstreamSlopeElevation(reservoir.getDownstreamSlopeElevation());
            vo.setDownstreamSlopeWidth(reservoir.getDownstreamSlopeWidth());
            vo.setSpillwayType(reservoir.getSpillwayType());
            vo.setSpillwayControlType(reservoir.getSpillwayControlType());
            vo.setSpillwayHasBridge(reservoir.getSpillwayHasBridge());
            vo.setSpillwayCrestElevation(reservoir.getSpillwayCrestElevation());
            vo.setSpillwayBottomElevation(reservoir.getSpillwayBottomElevation());
            vo.setSpillwayBottomWidth(reservoir.getSpillwayBottomWidth());
            vo.setSpillwayMaxDischarge(reservoir.getSpillwayMaxDischarge());
            vo.setFloodChannelName(reservoir.getFloodChannelName());
            vo.setFloodChannelSafeDischarge(reservoir.getFloodChannelSafeDischarge());
            vo.setCulvertType(reservoir.getCulvertType());
            vo.setCulvertSectionSize(reservoir.getCulvertSectionSize());
            vo.setCulvertGateType(reservoir.getCulvertGateType());
            vo.setCulvertDesignDischarge(reservoir.getCulvertDesignDischarge());
            vo.setCulvertExitElevation(reservoir.getCulvertExitElevation());
            vo.setCulvertDiameter(reservoir.getCulvertDiameter());
            vo.setCulvertHeight(reservoir.getCulvertHeight());
            vo.setAnnualWaterSupply(reservoir.getAnnualWaterSupply());
            vo.setFisheryArea(reservoir.getFisheryArea());
            vo.setWaterSource(reservoir.getWaterSource());
            vo.setRemarks(reservoir.getRemarks());


            YzWaterFacilityBaseBfDO facility = facilityMap.get(reservoir.getFacilityId());
            if (facility != null && facility.getGeom() != null) {
                vo.setGeomType(facility.getGeomType());
                vo.setSrid(facility.getSrid());
                vo.setGeomWkt(GeometryWktUtils.toWktWithSrid(facility.getGeom()));
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 公示牌扫码河长展示：河道级仅省/市/县/镇（各档一人），村级仅在对应河段（区划）下且可多人；
     * 公示牌关联河段或维护单位命中河段时，只返回该河段村级，其它区划（如真州、青山）村级不混入。
     * <p>
     * 兜底：若村级河长直接挂在河道上（riverSectionId 为空，未关联河段），
     * 则不会被丢弃，而是追加到河道级河长列表末尾（避免「无河段的河道」村级河长彻底丢失）。
     */
    private void formatRiverChiefDisplayForSignboard(List<RiverChannelQrSnapshotVO> rivers,
                                                     Long focusSectionId,
                                                     List<String> maintenanceLabels,
                                                     String signboardSectionName) {
        if (rivers == null || rivers.isEmpty()) {
            return;
        }
        for (RiverChannelQrSnapshotVO river : rivers) {
            if (river == null) {
                continue;
            }
            List<RiverHeadQrVO> originalChannelHeads = river.getChannelHeads();
            List<RiverHeadQrVO> villageHeadsOnChannel = extractVillageHeadsOnChannel(originalChannelHeads);
            List<RiverHeadQrVO> upstream = resolveUpstreamHeadsForDisplay(river, focusSectionId);
            if (!villageHeadsOnChannel.isEmpty()) {
                List<RiverHeadQrVO> merged = new ArrayList<>(upstream.size() + villageHeadsOnChannel.size());
                merged.addAll(upstream);
                merged.addAll(villageHeadsOnChannel);
                upstream = merged;
            }
            river.setChannelHeads(upstream);

            List<RiverSectionQrVO> sections = river.getSections();
            if (sections == null || sections.isEmpty()) {
                continue;
            }
            List<RiverSectionQrVO> visibleSections = new ArrayList<>();
            for (RiverSectionQrVO section : sections) {
                if (section == null) {
                    continue;
                }
                if (!shouldIncludeSectionForSignboard(section, focusSectionId, maintenanceLabels, signboardSectionName)) {
                    continue;
                }
                section.setHeads(filterVillageLevelHeads(section.getHeads()));
                visibleSections.add(section);
            }
            if (visibleSections.isEmpty() && focusSectionId == null
                    && maintenanceLabels != null && !maintenanceLabels.isEmpty()) {
                for (RiverSectionQrVO section : sections) {
                    if (section == null) {
                        continue;
                    }
                    section.setHeads(filterVillageLevelHeads(section.getHeads()));
                    visibleSections.add(section);
                }
            }
            river.setSections(visibleSections);
        }
    }

    private List<RiverHeadQrVO> extractVillageHeadsOnChannel(List<RiverHeadQrVO> heads) {
        if (heads == null || heads.isEmpty()) {
            return List.of();
        }
        List<RiverHeadQrVO> result = new ArrayList<>();
        for (RiverHeadQrVO head : heads) {
            if (head == null) {
                continue;
            }
            if (!isVillageLevelHead(head)) {
                continue;
            }
            if (head.getRiverSectionId() != null) {
                continue;
            }
            result.add(head);
        }
        return result;
    }

    /**
     * 省/市/县/镇：河道级优先；镇级等若只录在河段上，在关联河段时补全到 channelHeads（避免有省/市/县却无镇）。
     */
    private List<RiverHeadQrVO> resolveUpstreamHeadsForDisplay(RiverChannelQrSnapshotVO river, Long focusSectionId) {
        Map<String, RiverHeadQrVO> byLevel = new LinkedHashMap<>();
        mergeUpstreamHeadsByLevel(byLevel, filterUpstreamLevelHeads(river.getChannelHeads()));

        if (focusSectionId != null) {
            RiverSectionQrVO section = findSectionById(river, focusSectionId);
            if (section != null) {
                mergeUpstreamHeadsByLevel(byLevel, filterUpstreamLevelHeads(section.getHeads()));
            }
        }
        if (!byLevel.isEmpty()) {
            return sortUpstreamHeadMap(byLevel);
        }
        List<RiverHeadQrVO> fromAllSections = new ArrayList<>();
        if (river.getSections() != null) {
            for (RiverSectionQrVO section : river.getSections()) {
                if (section == null) {
                    continue;
                }
                fromAllSections.addAll(filterUpstreamLevelHeads(section.getHeads()));
            }
        }
        mergeUpstreamHeadsByLevel(byLevel, fromAllSections);
        return sortUpstreamHeadMap(byLevel);
    }

    private void mergeUpstreamHeadsByLevel(Map<String, RiverHeadQrVO> byLevel, List<RiverHeadQrVO> heads) {
        if (byLevel == null || heads == null || heads.isEmpty()) {
            return;
        }
        for (RiverHeadQrVO head : heads) {
            if (head == null || StrUtil.isBlank(head.getHeadLevel())) {
                continue;
            }
            String levelKey = StrUtil.trim(head.getHeadLevel()).toLowerCase(Locale.ROOT);
            byLevel.putIfAbsent(levelKey, head);
        }
    }

    private List<RiverHeadQrVO> sortUpstreamHeadMap(Map<String, RiverHeadQrVO> byLevel) {
        if (byLevel == null || byLevel.isEmpty()) {
            return List.of();
        }
        List<RiverHeadQrVO> result = new ArrayList<>(byLevel.values());
        sortRiverHeadList(result);
        return result;
    }

    private RiverSectionQrVO findSectionById(RiverChannelQrSnapshotVO river, Long sectionId) {
        if (river == null || sectionId == null || river.getSections() == null) {
            return null;
        }
        for (RiverSectionQrVO section : river.getSections()) {
            if (section != null && sectionId.equals(section.getId())) {
                return section;
            }
        }
        return null;
    }

    private boolean shouldIncludeSectionForSignboard(RiverSectionQrVO section,
                                                     Long focusSectionId,
                                                     List<String> maintenanceLabels,
                                                     String signboardSectionName) {
        if (section == null) {
            return false;
        }
        if (focusSectionId != null) {
            return focusSectionId.equals(section.getId());
        }
        String sectionNorm = normalizeMatchToken(section.getSectionName());
        String hintNorm = normalizeMatchToken(signboardSectionName);
        if (StrUtil.isNotBlank(hintNorm) && StrUtil.isNotBlank(sectionNorm)) {
            return sectionNorm.contains(hintNorm) || hintNorm.contains(sectionNorm);
        }
        if (maintenanceLabels != null && !maintenanceLabels.isEmpty() && StrUtil.isNotBlank(sectionNorm)) {
            for (String label : maintenanceLabels) {
                String labelNorm = normalizeMatchToken(label);
                if (StrUtil.isNotBlank(labelNorm)
                        && (sectionNorm.contains(labelNorm) || labelNorm.contains(sectionNorm))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private List<RiverHeadQrVO> filterUpstreamLevelHeads(List<RiverHeadQrVO> heads) {
        if (heads == null || heads.isEmpty()) {
            return List.of();
        }
        return heads.stream()
                .filter(Objects::nonNull)
                .filter(h -> !isVillageLevelHead(h))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<RiverHeadQrVO> filterVillageLevelHeads(List<RiverHeadQrVO> heads) {
        if (heads == null || heads.isEmpty()) {
            return List.of();
        }
        return heads.stream()
                .filter(Objects::nonNull)
                .filter(this::isVillageLevelHead)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean isVillageLevelHead(RiverHeadQrVO head) {
        return head != null && StrUtil.isNotBlank(head.getHeadLevel())
                && "village".equalsIgnoreCase(StrUtil.trim(head.getHeadLevel()));
    }

    private String normalizeMatchToken(String raw) {
        if (StrUtil.isBlank(raw)) {
            return "";
        }
        return StrUtil.trim(raw).replaceAll("\\s+", "");
    }

    private void sortRiverChannelHeads(List<RiverChannelQrSnapshotVO> rivers) {
        if (rivers == null || rivers.isEmpty()) {
            return;
        }
        for (RiverChannelQrSnapshotVO river : rivers) {
            if (river == null) {
                continue;
            }
            sortRiverHeadList(river.getChannelHeads());
            if (river.getSections() == null || river.getSections().isEmpty()) {
                continue;
            }
            for (var section : river.getSections()) {
                if (section == null) {
                    continue;
                }
                sortRiverHeadList(section.getHeads());
            }
        }
    }

    private void sortReservoirHeads(List<AppSignboardReservoirHeadRespVO> heads) {
        if (heads == null || heads.size() <= 1) {
            return;
        }
        heads.sort(Comparator.comparingInt(item -> resolveHeadLevelOrder(item == null ? null : item.getHeadLevel())));
    }

    private void sortRiverHeadList(List<RiverHeadQrVO> heads) {
        if (heads == null || heads.size() <= 1) {
            return;
        }
        heads.sort(Comparator.comparingInt(item -> resolveHeadLevelOrder(item == null ? null : item.getHeadLevel())));
    }

    private int resolveHeadLevelOrder(String headLevel) {
        if (StrUtil.isBlank(headLevel)) {
            return Integer.MAX_VALUE;
        }
        String value = StrUtil.trim(headLevel);
        if ("provincial".equalsIgnoreCase(value)) {
            return 0;
        }
        if ("city".equalsIgnoreCase(value)) {
            return 1;
        }
        if ("county".equalsIgnoreCase(value)) {
            return 2;
        }
        if ("township".equalsIgnoreCase(value)) {
            return 3;
        }
        if ("village".equalsIgnoreCase(value)) {
            return 4;
        }
        return Integer.MAX_VALUE;
    }

    private String resolveLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return StrUtil.blankToDefault(map.get(value), value);
    }

    private String resolveAreaName(String areaCode) {
        if (StrUtil.isBlank(areaCode)) {
            return null;
        }
        String code = StrUtil.trim(areaCode);
        try {
            Long areaId = Long.parseLong(code);
            SystemAreaDO area = systemAreaService.get(areaId);
            if (area != null && StrUtil.isNotBlank(area.getName())) {
                return area.getName();
            }
        } catch (Exception ignored) {
            // 兼容历史数据：若不是标准区划编码，直接返回原值
        }
        return code;
    }

    private List<String> resolveLabels(String[] values, Map<String, String> map) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        return List.of(values).stream()
                .filter(StrUtil::isNotBlank)
                .map(value -> StrUtil.blankToDefault(map.get(value), value))
                .distinct()
                .collect(Collectors.toList());
    }

    private String[] resolveLabelArray(String[] values, Map<String, String> map) {
        if (values == null || values.length == 0) {
            return new String[0];
        }
        List<String> labels = Arrays.stream(values)
                .filter(StrUtil::isNotBlank)
                .map(value -> StrUtil.blankToDefault(map.get(value), value))
                .distinct()
                .collect(Collectors.toList());
        return labels.toArray(new String[0]);
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
