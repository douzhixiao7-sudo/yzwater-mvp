package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelManagementDetailVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelQrRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelQrSnapshotVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelQrSummaryVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelLevelFixRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSupervisionDetailVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefOverviewChiefVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefOverviewSectionVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadItemReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadSupervisionItemReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadQrVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadSectionItemReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverManagementSectionDetailVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionQrVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionWithChannelSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSupervisionQrVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelSupervisionDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelSupervisionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * 河道基础信息服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChannelService {
    private static final String FACILITY_TYPE_RIVER = ReferenceTypeConstants.RIVER;
    private static final String FACILITY_TYPE_RIVER_SECTION = ReferenceTypeConstants.RIVER_SECTION;

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final WKTWriter WKT_WRITER = new WKTWriter();
    /**
     * 二维码内容长度上限（靠近 QR 版本 40 的容量，预留纠错与编码开销）
     */
    private static final int MAX_QR_CONTENT_LENGTH = 2800;

    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzRiverChannelManagementMapper managementMapper;
    private final YzRiverChannelSupervisionMapper supervisionMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final DictDataCommonApi dictDataApi;
    private final RiverChiefUserAccountService riverChiefUserAccountService;

    /**
     * 修复河道级别字段取值（批量更新）。
     * <p>
     * 规则：
     * <ul>
     *     <li>riverLevel=xcjhd → 7j</li>
     *     <li>riverLevel=xjhl → 6j</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public RiverChannelLevelFixRespVO fixRiverLevelValues() {
        long xcjhdTo7j = riverChannelMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelDO>()
                .eq(YzRiverChannelDO::getRiverLevel, "xcjhd")
                .set(YzRiverChannelDO::getRiverLevel, "7j"));
        long xjhlTo6j = riverChannelMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelDO>()
                .eq(YzRiverChannelDO::getRiverLevel, "xjhl")
                .set(YzRiverChannelDO::getRiverLevel, "6j"));

        RiverChannelLevelFixRespVO resp = new RiverChannelLevelFixRespVO();
        resp.setXcjhdTo7jCount(xcjhdTo7j);
        resp.setXjhlTo6jCount(xjhlTo6j);
        resp.setTotalCount(xcjhdTo7j + xjhlTo6j);
        return resp;
    }

    /**
     * 分页查询河道
     */
    public PageResult<RiverChannelPageRespVO> getRiverChannelPage(RiverChannelPageReqVO reqVO) {
        LambdaQueryWrapper<YzRiverChannelDO> wrapper = riverChannelMapper.buildQueryWrapper(reqVO);
        PageResult<YzRiverChannelDO> page = riverChannelMapper.selectPage(reqVO, wrapper);
        Map<String, String> levelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        Map<String, String> basinMap = loadDictLabelMap(ZdConstants.ZD_SZLY);
        Map<String, String> typeMap = loadDictLabelMap(ZdConstants.ZD_HDLX);
        List<RiverChannelPageRespVO> list = page.getList().stream()
                .map(item -> buildPageResp(item, levelMap, basinMap, typeMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 查询全部河道（简要信息，用于下拉选择）
     */
    public List<RiverChannelSimpleRespVO> getRiverChannelSimpleList() {
        List<YzRiverChannelDO> list = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .orderByDesc(YzRiverChannelDO::getUpdateTime)
                .orderByDesc(YzRiverChannelDO::getId));
        return list.stream().map(item -> {
            RiverChannelSimpleRespVO vo = new RiverChannelSimpleRespVO();
            vo.setId(item.getId());
            vo.setRiverCode(item.getRiverCode());
            vo.setRiverName(item.getRiverName());
            vo.setUpdateTime(item.getUpdateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 查询全部河段（简要，包含所属河道名称）
     *
     * <p>用于前端“河长管理”中关联河段的下拉选择：直接展示“河道名称/河段名称”。</p>
     * <p>仅返回 riverSectionCount 不为 0 的河道下的河段，避免展示无河段的河道。</p>
     */
    public List<RiverSectionWithChannelSimpleRespVO> getSectionWithChannelSimpleList() {
        // 1) 查询“河段数不为 0”的河道
        List<YzRiverChannelDO> channels = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName, YzRiverChannelDO::getRiverSectionCount, YzRiverChannelDO::getUpdateTime)
                .gt(YzRiverChannelDO::getRiverSectionCount, 0)
                .orderByDesc(YzRiverChannelDO::getUpdateTime)
                .orderByDesc(YzRiverChannelDO::getId));
        if (CollUtil.isEmpty(channels)) {
            return List.of();
        }
        Map<Long, String> riverNameMap = new HashMap<>();
        for (YzRiverChannelDO channel : channels) {
            if (channel == null || channel.getId() == null) {
                continue;
            }
            riverNameMap.put(channel.getId(), StrUtil.blankToDefault(channel.getRiverName(), ""));
        }
        if (riverNameMap.isEmpty()) {
            return List.of();
        }
        List<Long> channelIds = new ArrayList<>(riverNameMap.keySet());

        // 2) 查询这些河道下的河段
        List<YzRiverSectionDO> sections = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                .select(YzRiverSectionDO::getId, YzRiverSectionDO::getRiverChannelId, YzRiverSectionDO::getSectionName, YzRiverSectionDO::getUpdateTime)
                .in(YzRiverSectionDO::getRiverChannelId, channelIds)
                .orderByDesc(YzRiverSectionDO::getUpdateTime)
                .orderByDesc(YzRiverSectionDO::getId));
        if (CollUtil.isEmpty(sections)) {
            return List.of();
        }

        List<RiverSectionWithChannelSimpleRespVO> result = new ArrayList<>();
        for (YzRiverSectionDO section : sections) {
            if (section == null || section.getId() == null) {
                continue;
            }
            RiverSectionWithChannelSimpleRespVO vo = new RiverSectionWithChannelSimpleRespVO();
            vo.setId(section.getId());
            vo.setRiverChannelId(section.getRiverChannelId());
            vo.setRiverName(StrUtil.blankToDefault(riverNameMap.get(section.getRiverChannelId()), ""));
            vo.setSectionName(section.getSectionName());
            vo.setUpdateTime(section.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    /**
     * 导出河道数据（不分页），字典字段转换为对应的标签值
     */
    public List<RiverChannelExportExcelVO> getRiverChannelExportList(RiverChannelPageReqVO reqVO) {
        LambdaQueryWrapper<YzRiverChannelDO> wrapper = riverChannelMapper.buildQueryWrapper(reqVO);
        List<YzRiverChannelDO> list = riverChannelMapper.selectList(wrapper);
        Map<String, String> levelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        Map<String, String> basinMap = loadDictLabelMap(ZdConstants.ZD_SZLY);
        Map<String, String> typeMap = loadDictLabelMap(ZdConstants.ZD_HDLX);
        Map<String, String> ecologyMap = loadDictLabelMap(ZdConstants.ZD_STLX);
        Map<String, String> transboundaryMap = loadDictLabelMap(ZdConstants.ZD_KJLB);
        Map<String, String> floodMap = loadDictLabelMap(ZdConstants.ZD_FHBZ);
        Map<String, String> embankmentMap = loadDictLabelMap(ZdConstants.ZD_DFDJ);
        Map<String, String> waterQualityMap = loadDictLabelMap(ZdConstants.ZD_HDSZQK);
        return list.stream()
                .map(item -> buildExportExcelVO(item, levelMap, basinMap, typeMap, ecologyMap, transboundaryMap,
                        floodMap, embankmentMap, waterQualityMap))
                .collect(Collectors.toList());
    }

    /**
     * 新增河道
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRiverChannel(RiverChannelSaveReqVO reqVO) {
        // 河道编码统一由后端雪花算法生成，不接受前端录入
        reqVO.setRiverCode(String.valueOf(SNOWFLAKE.nextId()));
        Long facilityId = createFacilityBase(reqVO);
        YzRiverChannelDO entity = BeanUtils.toBean(reqVO, YzRiverChannelDO.class);
        entity.setId(SNOWFLAKE.nextId());
        entity.setFacilityId(facilityId);
        List<RiverSectionSaveReqVO> sections = reqVO.getSections();
        entity.setRiverSectionCount(CollUtil.isEmpty(sections) ? 0 : sections.size());
        riverChannelMapper.insert(entity);
        insertSections(entity.getId(), sections);
        return entity.getId();
    }

    /**
     * 更新河道（含基础表、河段增删改）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRiverChannel(RiverChannelSaveReqVO reqVO) {
        YzRiverChannelDO exists = riverChannelMapper.selectById(reqVO.getId());
        if (exists == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        String inputRiverCode = StrUtil.trimToNull(reqVO.getRiverCode());
        // 允许前端不传河道编码：不传则保持原值，避免更新时把编码清空
        if (StrUtil.isBlank(inputRiverCode)) {
            inputRiverCode = StrUtil.trimToNull(exists.getRiverCode());
            if (StrUtil.isBlank(inputRiverCode)) {
                inputRiverCode = exists.getFacilityId() == null ? null : String.valueOf(exists.getFacilityId());
            }
        }
        if (StrUtil.isNotBlank(inputRiverCode)) {
            validateRiverCodeUnique(inputRiverCode, exists.getId());
            reqVO.setRiverCode(inputRiverCode);
        }
        Long facilityId = ensureChannelFacility(reqVO, exists.getFacilityId());
        // 兜底：若历史数据 riverCode 为空且基础表为空，确保更新后河道编码有值
        if (StrUtil.isBlank(reqVO.getRiverCode())) {
            reqVO.setRiverCode(String.valueOf(facilityId));
        }
        YzRiverChannelDO entity = BeanUtils.toBean(reqVO, YzRiverChannelDO.class);
        entity.setFacilityId(facilityId);
        List<RiverSectionSaveReqVO> sections = reqVO.getSections();
        entity.setRiverSectionCount(CollUtil.isEmpty(sections) ? 0 : sections.size());
        riverChannelMapper.updateEditFieldsById(entity);
        upsertSections(reqVO.getId(), sections);
    }

    /**
     * 物理删除河道及关联信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRiverChannel(Long id) {
        YzRiverChannelDO exists = riverChannelMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        Long channelFacilityId = exists.getFacilityId();

        List<YzRiverSectionDO> sectionList = riverSectionMapper.selectList(
                new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, id));
        List<Long> sectionIds = sectionList.stream().map(YzRiverSectionDO::getId).collect(Collectors.toList());
        List<Long> sectionFacilityIds = sectionList.stream()
                .map(YzRiverSectionDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 删除河长和监督信息
        supervisionMapper.delete(new LambdaQueryWrapper<YzRiverChannelSupervisionDO>()
                .eq(YzRiverChannelSupervisionDO::getRiverChannelId, id));
        managementMapper.delete(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getRiverChannelId, id));

        if (!sectionIds.isEmpty()) {
            riverSectionMapper.delete(
                    new LambdaQueryWrapper<YzRiverSectionDO>().in(YzRiverSectionDO::getId, sectionIds));
        }
        if (!sectionFacilityIds.isEmpty()) {
            facilityBaseMapper.deleteBatchIds(sectionFacilityIds);
        }

        riverChannelMapper.delete(new LambdaQueryWrapper<YzRiverChannelDO>().eq(YzRiverChannelDO::getId, id));
        if (channelFacilityId != null) {
            facilityBaseMapper.deleteById(channelFacilityId);
        }
    }

    /**
     * 查询河道详情（附带河段列表）
     */
    public RiverChannelDetailRespVO getRiverChannelDetail(Long id) {
        YzRiverChannelDO entity = riverChannelMapper.selectById(id);
        if (entity == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        RiverChannelDetailRespVO resp = BeanUtils.toBean(entity, RiverChannelDetailRespVO.class);
        resp.setSections(getSectionDetailsByChannelId(id));
        return resp;
    }

    /**
     * 按设施ID查询河道详情（附带河段列表）
     */
    public RiverChannelDetailRespVO getRiverChannelDetailByFacilityId(Long facilityId) {
        if (facilityId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        YzRiverChannelDO entity = riverChannelMapper.selectOne(new LambdaQueryWrapper<YzRiverChannelDO>()
                .eq(YzRiverChannelDO::getFacilityId, facilityId)
                .eq(YzRiverChannelDO::getDeleted, 0));
        if (entity == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        RiverChannelDetailRespVO resp = BeanUtils.toBean(entity, RiverChannelDetailRespVO.class);
        resp.setSections(getSectionDetailsByChannelId(entity.getId()));
        return resp;
    }

    /**
     * 按设施ID查询河段详情
     */
    public RiverSectionDetailRespVO getRiverSectionDetailByFacilityId(Long facilityId) {
        if (facilityId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
        }
        YzRiverSectionDO section = riverSectionMapper.selectOne(new LambdaQueryWrapper<YzRiverSectionDO>()
                .eq(YzRiverSectionDO::getFacilityId, facilityId)
                .eq(YzRiverSectionDO::getDeleted, 0));
        if (section == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_SECTION_NOT_EXISTS);
        }
        return BeanUtils.toBean(section, RiverSectionDetailRespVO.class);
    }

    /**
     * 查询河段列表（简要）
     */
    public List<RiverSectionSimpleRespVO> getSectionsByChannelId(Long channelId) {
        List<YzRiverSectionDO> list = riverSectionMapper.selectList(
                new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, channelId));
        return list.stream().map(item -> {
            RiverSectionSimpleRespVO vo = new RiverSectionSimpleRespVO();
            vo.setId(item.getId());
            vo.setSectionName(item.getSectionName());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 查询河段详情列表
     */
    public List<RiverSectionDetailRespVO> getSectionDetailsByChannelId(Long channelId) {
        List<YzRiverSectionDO> list = riverSectionMapper.selectList(
                new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, channelId));
        return list.stream().map(item -> BeanUtils.toBean(item, RiverSectionDetailRespVO.class)).collect(Collectors.toList());
    }

    /**
     * 查询河长与监督详情
     */
    public List<RiverManagementSectionDetailVO> getRiverManagement(Long channelId) {
        YzRiverChannelDO channel = riverChannelMapper.selectById(channelId);
        String channelName = channel == null ? "" : StrUtil.blankToDefault(channel.getRiverName(), "");
        String channelResponsibilities = channel == null ? null : StrUtil.trimToNull(channel.getResponsibilities());
        List<YzRiverSectionDO> sections = riverSectionMapper.selectList(
                new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, channelId));
        Map<Long, String> sectionNameMap = sections.stream()
                .collect(Collectors.toMap(YzRiverSectionDO::getId, YzRiverSectionDO::getSectionName, (a, b) -> a));

        List<Long> sectionIds = sections.stream()
                .map(YzRiverSectionDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<YzRiverChannelManagementDO> heads = new ArrayList<>();
        if (sections.isEmpty()) {
            heads = managementMapper.selectCurrentByResolvedReference(ReferenceTypeConstants.RIVER, channelId, null);
        } else if (!sectionIds.isEmpty()) {
            heads = managementMapper.selectCurrentByResolvedReferenceIds(ReferenceTypeConstants.RIVER_SECTION, sectionIds);
        }
        // 监督单位按河道/河段关联，不再按河长ID过滤
        List<YzRiverChannelSupervisionDO> supervisions = supervisionMapper.selectList(
                new LambdaQueryWrapper<YzRiverChannelSupervisionDO>()
                        .eq(YzRiverChannelSupervisionDO::getRiverChannelId, channelId));

        // 允许 riverSectionId 为空，表示监督单位直接关联河道；Collectors.groupingBy 不支持 null key，这里手动分组
        Map<Long, List<YzRiverChannelSupervisionDO>> supervisionBySection = new HashMap<>();
        for (YzRiverChannelSupervisionDO item : supervisions) {
            if (item == null) {
                continue;
            }
            Long sectionId = item.getRiverSectionId();
            supervisionBySection.computeIfAbsent(sectionId, k -> new ArrayList<>()).add(item);
        }

        Map<Long, RiverManagementSectionDetailVO> sectionDetailMap = new HashMap<>();
        if (sections.isEmpty()) {
            RiverManagementSectionDetailVO vo = new RiverManagementSectionDetailVO();
            vo.setSectionId(null);
            vo.setSectionName(channelName);
            vo.setHeads(List.of());
            vo.setSupervisions(List.of());
            sectionDetailMap.put(null, vo);
        } else {
            for (YzRiverSectionDO sec : sections) {
                RiverManagementSectionDetailVO vo = new RiverManagementSectionDetailVO();
                vo.setSectionId(sec.getId());
                vo.setSectionName(sec.getSectionName());
                vo.setHeads(List.of());
                vo.setSupervisions(List.of());
                sectionDetailMap.put(sec.getId(), vo);
            }
        }

        for (YzRiverChannelManagementDO head : heads) {
            Long secId = head.getRiverSectionId();
            if (secId == null && ReferenceTypeConstants.RIVER_SECTION.equalsIgnoreCase(head.getReferenceType())) {
                secId = head.getReferenceId();
            }
            final Long resolvedSectionId = secId;
            RiverManagementSectionDetailVO secVO = sectionDetailMap.computeIfAbsent(resolvedSectionId, k -> {
                RiverManagementSectionDetailVO vo = new RiverManagementSectionDetailVO();
                vo.setSectionId(resolvedSectionId);
                vo.setSectionName(StrUtil.blankToDefault(sectionNameMap.getOrDefault(resolvedSectionId, ""), channelName));
                vo.setHeads(List.of());
                vo.setSupervisions(List.of());
                return vo;
            });
            List<RiverChannelManagementDetailVO> headList = secVO.getHeads() == null ? new ArrayList<>() : new ArrayList<>(secVO.getHeads());
            RiverChannelManagementDetailVO headVO = new RiverChannelManagementDetailVO();
            headVO.setId(head.getId());
            headVO.setRiverSectionId(resolvedSectionId);
            headVO.setSectionName(sectionNameMap.getOrDefault(resolvedSectionId, head.getSectionName()));
            headVO.setHeadLevel(head.getHeadLevel());
            headVO.setHeadPosition(head.getHeadPosition());
            headVO.setHeadName(head.getHeadName());
            headVO.setHeadContact(head.getHeadContact());
            headVO.setResponsibilities(channelResponsibilities);
            headVO.setAdministrativeRegion(toStringList(head.getAdministrativeRegion()));
            headList.add(headVO);
            secVO.setHeads(headList);
        }

        for (Map.Entry<Long, List<YzRiverChannelSupervisionDO>> entry : supervisionBySection.entrySet()) {
            Long sectionId = entry.getKey();
            List<YzRiverChannelSupervisionDO> supList = CollUtil.defaultIfEmpty(entry.getValue(), List.of());
            RiverManagementSectionDetailVO secVO = sectionDetailMap.computeIfAbsent(sectionId, k -> {
                RiverManagementSectionDetailVO vo = new RiverManagementSectionDetailVO();
                vo.setSectionId(sectionId);
                vo.setSectionName(StrUtil.blankToDefault(sectionNameMap.getOrDefault(sectionId, ""), channelName));
                vo.setHeads(List.of());
                vo.setSupervisions(List.of());
                return vo;
            });
            List<RiverChannelSupervisionDetailVO> supVOList = supList.stream().map(item -> {
                RiverChannelSupervisionDetailVO vo = new RiverChannelSupervisionDetailVO();
                vo.setId(item.getId());
                vo.setRiverSectionId(item.getRiverSectionId());
                vo.setSupervisionUnit(item.getSupervisionUnit());
                vo.setSupervisionContact(item.getSupervisionContact());
                return vo;
            }).collect(Collectors.toList());
            secVO.setSupervisions(supVOList);
        }

        return new ArrayList<>(sectionDetailMap.values());
    }

    /**
     * 河道页查看当前有效河长概览（河道直属 + 河段分组）。
     */
    public RiverChiefOverviewRespVO getRiverChiefOverview(Long channelId) {
        YzRiverChannelDO channel = riverChannelMapper.selectById(channelId);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }

        List<YzRiverSectionDO> sections = riverSectionMapper.selectList(
                new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, channelId));
        List<Long> sectionIds = sections.stream()
                .map(YzRiverSectionDO::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<String, String> headLevelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);

        List<YzRiverChannelManagementDO> riverChiefs = managementMapper
                .selectCurrentByResolvedReference(ReferenceTypeConstants.RIVER, channelId, null);
        List<YzRiverChannelManagementDO> sectionChiefs = sectionIds.isEmpty()
                ? List.of()
                : managementMapper.selectCurrentByResolvedReferenceIds(ReferenceTypeConstants.RIVER_SECTION, sectionIds);

        RiverChiefOverviewRespVO resp = new RiverChiefOverviewRespVO();
        resp.setRiverId(channelId);
        resp.setRiverName(StrUtil.blankToDefault(channel.getRiverName(), "-"));
        resp.setRiverChiefs(buildChiefOverviewItems(riverChiefs, headLevelMap));
        resp.setSectionChiefGroups(buildSectionChiefOverview(sections, sectionChiefs, headLevelMap));
        resp.setTotalCount(resp.getRiverChiefs().size()
                + resp.getSectionChiefGroups().stream().mapToInt(item -> item.getChiefs().size()).sum());
        return resp;
    }

    public void syncRiverManagementResponsibilities(Long channelId, String responsibilities) {
        if (channelId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        YzRiverChannelDO channel = riverChannelMapper.selectById(channelId);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        // 管理表已移除 responsibilities 字段，保留该接口仅用于兼容旧调用链。
    }

    /**
     * 校验河道编码唯一性（排除指定ID）。
     */
    public void validateRiverCodeUnique(String riverCode, Long excludeId) {
        if (StrUtil.isBlank(riverCode)) {
            return;
        }
        Long count = riverChannelMapper.selectCount(new LambdaQueryWrapper<YzRiverChannelDO>()
                .eq(YzRiverChannelDO::getRiverCode, riverCode)
                .ne(excludeId != null, YzRiverChannelDO::getId, excludeId));
        if (count != null && count > 0) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_CODE_DUPLICATE);
        }
    }

    /**
     * 判断河道编码是否已存在（排除指定ID）。
     */
    public boolean isRiverCodeExists(String riverCode, Long excludeId) {
        if (StrUtil.isBlank(riverCode)) {
            return false;
        }
        Long count = riverChannelMapper.selectCount(new LambdaQueryWrapper<YzRiverChannelDO>()
                .eq(YzRiverChannelDO::getRiverCode, riverCode)
                .ne(excludeId != null, YzRiverChannelDO::getId, excludeId));
        return count != null && count > 0;
    }



    /**
     * 生成河道信息二维码（基于河道编码，二维码固定不变）
     */
    public RiverChannelQrRespVO generateRiverChannelQrcodeByCode(String riverCode) {
        if (StrUtil.isBlank(riverCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        YzRiverChannelDO channel = riverChannelMapper.selectOne(YzRiverChannelDO::getRiverCode, riverCode);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        RiverChannelQrRespVO respVO = new RiverChannelQrRespVO();
        String contentUrl = buildContentUrl(riverCode, respVO);
        respVO.setContentUrl(contentUrl);
        respVO.setQrcodeImageBase64(buildQrcode(contentUrl));
        return respVO;
    }

    /**
     * 根据河道编码获取二维码快照数据（公开接口使用）
     */
    public RiverChannelQrSnapshotVO getRiverChannelQrSnapshotByCode(String riverCode) {
        if (StrUtil.isBlank(riverCode)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        YzRiverChannelDO channel = riverChannelMapper.selectOne(YzRiverChannelDO::getRiverCode, riverCode);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        return buildRiverChannelQrSnapshot(channel, null);
    }

    /**
     * 根据河道ID获取二维码快照数据（公开接口可用）
     */
    public RiverChannelQrSnapshotVO getRiverChannelQrSnapshotById(Long channelId) {
        if (channelId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        YzRiverChannelDO channel = riverChannelMapper.selectById(channelId);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        return buildRiverChannelQrSnapshot(channel, null);
    }

    /**
     * 根据河道ID获取二维码快照数据（支持按河段过滤，仅返回指定河段）
     */
    public RiverChannelQrSnapshotVO getRiverChannelQrSnapshotById(Long channelId, Set<Long> includeSectionIds) {
        if (channelId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        YzRiverChannelDO channel = riverChannelMapper.selectById(channelId);
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }
        return buildRiverChannelQrSnapshot(channel, includeSectionIds);
    }

    /**
     * 构建河道二维码快照数据
     */
    private RiverChannelQrSnapshotVO buildRiverChannelQrSnapshot(YzRiverChannelDO channel, Set<Long> includeSectionIds) {
        Long channelId = channel.getId();
        Map<Long, YzWaterFacilityBaseDO> facilityMap = new HashMap<>();
        if (channel.getFacilityId() != null) {
            YzWaterFacilityBaseDO facility = facilityBaseMapper.selectById(channel.getFacilityId());
            if (facility != null) {
                facilityMap.put(facility.getId(), facility);
            }
        }
        List<YzRiverSectionDO> sections = riverSectionMapper.selectList(
                new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, channelId));
        if (CollUtil.isNotEmpty(includeSectionIds)) {
            sections = sections.stream()
                    .filter(item -> item != null && item.getId() != null && includeSectionIds.contains(item.getId()))
                    .collect(Collectors.toList());
        }
        Set<Long> sectionFacilityIds = sections.stream()
                .map(YzRiverSectionDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!sectionFacilityIds.isEmpty()) {
            facilityBaseMapper.selectBatchIds(sectionFacilityIds)
                    .forEach(item -> facilityMap.put(item.getId(), item));
        }

        List<YzRiverChannelManagementDO> heads = managementMapper.selectList(
                new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                        .eq(YzRiverChannelManagementDO::getRiverChannelId, channelId)
                        .eq(YzRiverChannelManagementDO::getIsCurrent, 1));
        // 允许 riverSectionId 为空，表示河长直接关联河道；Collectors.groupingBy 不支持 null key，这里手工分组
        Map<Long, List<YzRiverChannelManagementDO>> headBySection = new HashMap<>();
        for (YzRiverChannelManagementDO head : heads) {
            if (head == null) {
                continue;
            }
            Long sectionId = head.getRiverSectionId();
            headBySection.computeIfAbsent(sectionId, k -> new ArrayList<>()).add(head);
        }
        // 监督单位按河道/河段关联，不再按河长ID过滤
        List<YzRiverChannelSupervisionDO> supervisionList = supervisionMapper.selectList(
                new LambdaQueryWrapper<YzRiverChannelSupervisionDO>()
                        .eq(YzRiverChannelSupervisionDO::getRiverChannelId, channelId));
        // 允许 riverSectionId 为空，表示监督单位直接关联河道；Collectors.groupingBy 不支持 null key，这里手动分组
        Map<Long, List<YzRiverChannelSupervisionDO>> supervisionBySection = new HashMap<>();
        for (YzRiverChannelSupervisionDO item : supervisionList) {
            if (item == null) {
                continue;
            }
            Long sectionId = item.getRiverSectionId();
            supervisionBySection.computeIfAbsent(sectionId, k -> new ArrayList<>()).add(item);
        }

        Map<String, String> basinMap = loadDictLabelMap(ZdConstants.ZD_SZLY);
        Map<String, String> riverTypeMap = loadDictLabelMap(ZdConstants.ZD_HDLX);
        Map<String, String> ecologyMap = loadDictLabelMap(ZdConstants.ZD_STLX);
        Map<String, String> riverLevelMap = loadDictLabelMap(ZdConstants.ZD_HLJB);
        Map<String, String> headLevelMap = loadDictLabelMap(ZdConstants.ZD_HZJB);

        RiverChannelQrSnapshotVO snapshot = new RiverChannelQrSnapshotVO();
        snapshot.setChannel(buildChannelQrSummary(channel, facilityMap.get(channel.getFacilityId()),
                basinMap, riverTypeMap, ecologyMap, riverLevelMap));
        String channelResponsibilities = StrUtil.trimToNull(channel.getResponsibilities());
        List<YzRiverChannelManagementDO> channelHeads = headBySection.getOrDefault(null, List.of());
        snapshot.setChannelHeads(buildHeadVos(channelHeads, supervisionBySection.getOrDefault(null, List.of()), headLevelMap, channelResponsibilities));
        snapshot.setSections(buildSectionQrList(sections, facilityMap, headBySection, supervisionBySection, headLevelMap, channelResponsibilities));
        return snapshot;
    }

    /**
     * 保存河长与监督信息（覆盖式）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRiverManagement(RiverHeadBatchSaveReqVO reqVO) {
        YzRiverChannelDO channel = riverChannelMapper.selectById(reqVO.getRiverChannelId());
        if (channel == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_NOT_EXISTS);
        }

        List<Long> sectionIds = riverSectionMapper.selectList(
                        new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, reqVO.getRiverChannelId()))
                .stream().map(YzRiverSectionDO::getId).collect(Collectors.toList());
        boolean noSectionDivided = CollUtil.isEmpty(sectionIds);

        // 查询当前有效的河长与监督信息，用于版本对比
        List<YzRiverChannelManagementDO> currentHeads = managementMapper.selectList(
                new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                        .eq(YzRiverChannelManagementDO::getRiverChannelId, reqVO.getRiverChannelId())
                        .eq(YzRiverChannelManagementDO::getIsCurrent, 1));
        // 允许 riverSectionId 为空，表示河长直接关联河道；手工分组避免 null key 异常
        Map<Long, List<YzRiverChannelManagementDO>> currentHeadBySection = new HashMap<>();
        for (YzRiverChannelManagementDO head : currentHeads) {
            if (head == null) {
                continue;
            }
            Long sectionId = head.getRiverSectionId();
            currentHeadBySection.computeIfAbsent(sectionId, k -> new ArrayList<>()).add(head);
        }
        List<YzRiverChannelSupervisionDO> currentSupervisions = supervisionMapper.selectList(
                new LambdaQueryWrapper<YzRiverChannelSupervisionDO>()
                        .eq(YzRiverChannelSupervisionDO::getRiverChannelId, reqVO.getRiverChannelId()));
        // 允许 riverSectionId 为空，表示监督单位直接关联河道；Collectors.groupingBy 不支持 null key，这里手动分组
        Map<Long, List<YzRiverChannelSupervisionDO>> currentSupervisionBySection = new HashMap<>();
        for (YzRiverChannelSupervisionDO item : currentSupervisions) {
            if (item == null) {
                continue;
            }
            Long sectionId = item.getRiverSectionId();
            currentSupervisionBySection.computeIfAbsent(sectionId, k -> new ArrayList<>()).add(item);
        }

        Map<Long, RiverHeadSectionItemReqVO> reqSectionMap = new HashMap<>();
        if (!CollUtil.isEmpty(reqVO.getSections())) {
            if (noSectionDivided) {
                // 未划分河段时将所有河长聚合到河道层级（riverSectionId 为空）
                List<RiverHeadItemReqVO> mergedHeads = new ArrayList<>();
                List<RiverHeadSupervisionItemReqVO> mergedSupervisions = new ArrayList<>();
                String mergedSectionName = null;
                for (RiverHeadSectionItemReqVO section : reqVO.getSections()) {
                    if (section == null) {
                        continue;
                    }
                    mergedSectionName = StrUtil.blankToDefault(section.getSectionName(), mergedSectionName);
                    mergedHeads.addAll(CollUtil.defaultIfEmpty(section.getHeads(), List.of()));
                    mergedSupervisions.addAll(CollUtil.defaultIfEmpty(section.getSupervisions(), List.of()));
                }
                RiverHeadSectionItemReqVO merged = new RiverHeadSectionItemReqVO();
                merged.setSectionId(null);
                merged.setSectionName(StrUtil.blankToDefault(mergedSectionName, channel.getRiverName()));
                merged.setHeads(mergedHeads);
                merged.setSupervisions(mergedSupervisions);
                reqSectionMap.put(null, merged);
            } else {
                for (RiverHeadSectionItemReqVO section : reqVO.getSections()) {
                    if (section == null) {
                        continue;
                    }
                    if (section.getSectionId() == null) {
                        throw ServiceExceptionUtil.invalidParamException("已划分河段时，监督单位必须绑定到具体河段");
                    }
                    if (!sectionIds.contains(section.getSectionId())) {
                        throw ServiceExceptionUtil.invalidParamException("存在无效河段ID：{}", section.getSectionId());
                    }
                    // 已划分河段时要求 sectionId 必填且有效
                    reqSectionMap.put(section.getSectionId(), section);
                }
            }
        }

        // 需要处理的河段：当前存在的 + 本次提交的；未划分河段时强制以河道维度处理
        Set<Long> sectionIdUnion = new HashSet<>();
        if (noSectionDivided) {
            sectionIdUnion.add(null);
        } else {
            sectionIdUnion.addAll(currentHeadBySection.keySet());
            sectionIdUnion.addAll(currentSupervisionBySection.keySet());
            sectionIdUnion.addAll(reqSectionMap.keySet());
        }

        LocalDateTime now = LocalDateTime.now();
        List<YzRiverChannelManagementDO> inserted = new ArrayList<>();
        for (Long sectionId : sectionIdUnion) {
            if (!noSectionDivided && sectionId != null && !sectionIds.contains(sectionId)) {
                continue;
            }

            Long bindSectionId = noSectionDivided ? null : sectionId;
            List<YzRiverChannelManagementDO> currentSecHeads = currentHeadBySection.getOrDefault(bindSectionId, List.of());
            RiverHeadSectionItemReqVO reqSection = reqSectionMap.get(bindSectionId);
            List<RiverHeadItemReqVO> reqHeads = reqSection == null
                    ? List.of()
                    : CollUtil.defaultIfEmpty(reqSection.getHeads(), List.of()).stream()
                    .filter(Objects::nonNull)
                    .filter(this::hasAnyHeadField)
                    .toList();
            List<RiverHeadSupervisionItemReqVO> reqSupervisions = reqSection == null
                    ? List.of()
                    : CollUtil.defaultIfEmpty(reqSection.getSupervisions(), List.of());
            List<RiverHeadSupervisionItemReqVO> effectiveReqSupervisions = reqSupervisions.stream()
                    .filter(sup -> sup != null && (StrUtil.isNotBlank(sup.getSupervisionUnit()) || StrUtil.isNotBlank(sup.getSupervisionContact())))
                    .toList();
            String sectionName = reqSection == null
                    ? channel.getRiverName()
                    : StrUtil.blankToDefault(reqSection.getSectionName(), channel.getRiverName());

            List<YzRiverChannelSupervisionDO> currentSecSupervisions =
                    currentSupervisionBySection.getOrDefault(bindSectionId, List.of());

            boolean headChanged = !StrUtil.equals(
                    buildHeadSnapshotFromCurrent(currentSecHeads),
                    buildHeadSnapshotFromReq(reqHeads));
            // 河道页面不再维护河长：未提交河长数据时，不触发河长版本变更
            if (CollUtil.isEmpty(reqHeads)) {
                headChanged = false;
            }
            boolean supervisionChanged = !StrUtil.equals(
                    buildSupervisionSnapshotFromCurrent(currentSecSupervisions),
                    buildSupervisionSnapshotFromReq(reqSupervisions));
            if (!headChanged && !supervisionChanged) {
                continue;
            }

            if (headChanged && !currentSecHeads.isEmpty()) {
                LambdaUpdateWrapper<YzRiverChannelManagementDO> updateWrapper = new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                        .eq(YzRiverChannelManagementDO::getRiverChannelId, reqVO.getRiverChannelId())
                        .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                        .set(YzRiverChannelManagementDO::getEffectiveTo, now)
                        .set(YzRiverChannelManagementDO::getIsCurrent, 0);
                if (bindSectionId == null) {
                    updateWrapper.isNull(YzRiverChannelManagementDO::getRiverSectionId);
                } else {
                    updateWrapper.eq(YzRiverChannelManagementDO::getRiverSectionId, bindSectionId);
                }
                managementMapper.update(null, updateWrapper);
            }

            if (headChanged) {
                Integer nextVersionNo = selectNextVersionNo(reqVO.getRiverChannelId(), bindSectionId);
                Long referenceId = bindSectionId != null ? bindSectionId : reqVO.getRiverChannelId();
                String referenceType = bindSectionId != null ? ReferenceTypeConstants.RIVER_SECTION : ReferenceTypeConstants.RIVER;
                for (int i = 0; i < reqHeads.size(); i++) {
                    RiverHeadItemReqVO head = reqHeads.get(i);
                    if (StrUtil.isBlank(head.getHeadLevel())) {
                        throw ServiceExceptionUtil.invalidParamException("【{}】第{}个河长：河长级别不能为空", sectionName, i + 1);
                    }
                    if (StrUtil.isBlank(head.getHeadName())) {
                        throw ServiceExceptionUtil.invalidParamException("【{}】第{}个河长：河长姓名不能为空", sectionName, i + 1);
                    }
                }
                for (RiverHeadItemReqVO head : reqHeads) {
                    YzRiverChannelManagementDO mgmt = new YzRiverChannelManagementDO();
                    mgmt.setId(SNOWFLAKE.nextId());
                    mgmt.setRiverChannelId(reqVO.getRiverChannelId());
                    mgmt.setRiverSectionId(bindSectionId);
                    mgmt.setReferenceId(referenceId);
                    mgmt.setReferenceType(referenceType);
                    mgmt.setSectionName(sectionName);
                    mgmt.setHeadLevel(StrUtil.trimToNull(head.getHeadLevel()));
                    mgmt.setHeadPosition(StrUtil.trimToNull(head.getHeadPosition()));
                    mgmt.setHeadName(StrUtil.trimToNull(head.getHeadName()));
                    mgmt.setHeadContact(null);
                    mgmt.setAdministrativeRegion(toStringArray(head.getAdministrativeRegion()));
                    mgmt.setVersionNo(nextVersionNo);
                    mgmt.setEffectiveFrom(now);
                    mgmt.setEffectiveTo(null);
                    mgmt.setIsCurrent(1);
                    managementMapper.insert(mgmt);
                    inserted.add(mgmt);
                }
            }

            if (supervisionChanged) {
                LambdaQueryWrapper<YzRiverChannelSupervisionDO> deleteWrapper = new LambdaQueryWrapper<YzRiverChannelSupervisionDO>()
                        .eq(YzRiverChannelSupervisionDO::getRiverChannelId, reqVO.getRiverChannelId());
                if (bindSectionId == null) {
                    deleteWrapper.isNull(YzRiverChannelSupervisionDO::getRiverSectionId);
                } else {
                    deleteWrapper.eq(YzRiverChannelSupervisionDO::getRiverSectionId, bindSectionId);
                }
                supervisionMapper.delete(deleteWrapper);

                List<YzRiverChannelSupervisionDO> insertList = effectiveReqSupervisions.stream()
                        .map(sup -> {
                            YzRiverChannelSupervisionDO supervision = new YzRiverChannelSupervisionDO();
                            supervision.setId(SNOWFLAKE.nextId());
                            supervision.setRiverChannelId(reqVO.getRiverChannelId());
                            supervision.setRiverSectionId(bindSectionId);
                            supervision.setRiverChannelManagementId(null);
                            supervision.setSupervisionUnit(sup.getSupervisionUnit());
                            supervision.setSupervisionContact(sup.getSupervisionContact());
                            return supervision;
                        }).collect(Collectors.toList());
                if (!insertList.isEmpty()) {
                    supervisionMapper.insertBatch(insertList);
                }
            }
        }

        // 按需求临时关闭：新增/编辑河长不再自动同步创建系统用户（AdminUserDO）
        // riverChiefUserAccountService.syncAndBindStrict(inserted);
    }

    private boolean hasAnyHeadField(RiverHeadItemReqVO head) {
        if (head == null) {
            return false;
        }
        return StrUtil.isNotBlank(head.getHeadLevel())
                || StrUtil.isNotBlank(head.getHeadPosition())
                || StrUtil.isNotBlank(head.getHeadName())
                || CollUtil.isNotEmpty(head.getAdministrativeRegion());
    }

    /**
     * 生成提交数据的快照字符串（用于判断是否发生变更）
     */
    private String buildHeadSnapshotFromReq(List<RiverHeadItemReqVO> heads) {
        if (CollUtil.isEmpty(heads)) {
            return "";
        }
        List<String> headKeys = heads.stream()
                .filter(Objects::nonNull)
                .map(head -> String.join("|",
                        StrUtil.blankToDefault(head.getHeadLevel(), ""),
                        StrUtil.blankToDefault(head.getHeadPosition(), ""),
                        StrUtil.blankToDefault(head.getHeadName(), ""),
                        joinRegion(normalizeRegionList(head.getAdministrativeRegion()))))
                .sorted()
                .collect(Collectors.toList());
        return String.join(";", headKeys);
    }

    /**
     * 生成当前数据的快照字符串（用于判断是否发生变更）
     */
    private String buildHeadSnapshotFromCurrent(List<YzRiverChannelManagementDO> heads) {
        if (CollUtil.isEmpty(heads)) {
            return "";
        }
        List<String> headKeys = heads.stream()
                .filter(Objects::nonNull)
                .map(head -> String.join("|",
                        StrUtil.blankToDefault(head.getHeadLevel(), ""),
                        StrUtil.blankToDefault(head.getHeadPosition(), ""),
                        StrUtil.blankToDefault(head.getHeadName(), ""),
                        joinRegion(toStringList(head.getAdministrativeRegion()))))
                .sorted()
                .collect(Collectors.toList());
        return String.join(";", headKeys);
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

    private String[] toStringArray(List<String> values) {
        List<String> cleaned = normalizeRegionList(values);
        return cleaned.isEmpty() ? null : cleaned.toArray(new String[0]);
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

    private String buildSupervisionSnapshotFromReq(List<RiverHeadSupervisionItemReqVO> supervisions) {
        if (CollUtil.isEmpty(supervisions)) {
            return "";
        }
        List<String> supKeys = supervisions.stream()
                .filter(Objects::nonNull)
                .filter(sup -> StrUtil.isNotBlank(sup.getSupervisionUnit()) || StrUtil.isNotBlank(sup.getSupervisionContact()))
                .map(sup -> StrUtil.blankToDefault(sup.getSupervisionUnit(), "") + "#" +
                        StrUtil.blankToDefault(sup.getSupervisionContact(), ""))
                .sorted()
                .collect(Collectors.toList());
        return String.join(",", supKeys);
    }

    private String buildSupervisionSnapshotFromCurrent(List<YzRiverChannelSupervisionDO> supervisions) {
        if (CollUtil.isEmpty(supervisions)) {
            return "";
        }
        List<String> supKeys = supervisions.stream()
                .filter(Objects::nonNull)
                .filter(sup -> StrUtil.isNotBlank(sup.getSupervisionUnit()) || StrUtil.isNotBlank(sup.getSupervisionContact()))
                .map(sup -> StrUtil.blankToDefault(sup.getSupervisionUnit(), "") + "#" +
                        StrUtil.blankToDefault(sup.getSupervisionContact(), ""))
                .sorted()
                .collect(Collectors.toList());
        return String.join(",", supKeys);
    }

    /**
     * 计算河段下一次版本号（按 riverChannelId + riverSectionId 递增）
     */
    private Integer selectNextVersionNo(Long riverChannelId, Long sectionId) {
        LambdaQueryWrapper<YzRiverChannelManagementDO> wrapper = new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getRiverChannelId, riverChannelId)
                .orderByDesc(YzRiverChannelManagementDO::getVersionNo)
                .last("LIMIT 1");
        if (sectionId == null) {
            wrapper.isNull(YzRiverChannelManagementDO::getRiverSectionId);
        } else {
            wrapper.eq(YzRiverChannelManagementDO::getRiverSectionId, sectionId);
        }
        YzRiverChannelManagementDO max = managementMapper.selectOne(wrapper);
        Integer maxNo = (max == null || max.getVersionNo() == null) ? 0 : max.getVersionNo();
        return maxNo + 1;
    }

    /**
     * 构建河道二维码数据中的河段列表
     */
    private List<RiverSectionQrVO> buildSectionQrList(List<YzRiverSectionDO> sections,
                                                      Map<Long, YzWaterFacilityBaseDO> facilityMap,
                                                      Map<Long, List<YzRiverChannelManagementDO>> headBySection,
                                                      Map<Long, List<YzRiverChannelSupervisionDO>> supervisionBySection,
                                                      Map<String, String> headLevelMap,
                                                      String channelResponsibilities) {
        if (CollUtil.isEmpty(sections)) {
            return List.of();
        }
        return sections.stream().map(sec -> {
            RiverSectionQrVO vo = new RiverSectionQrVO();
            vo.setId(sec.getId());
            vo.setFacilityId(sec.getFacilityId());
            vo.setSectionName(sec.getSectionName());
            vo.setStartPoint(sec.getStartPoint());
            vo.setEndPoint(sec.getEndPoint());
            vo.setStartLongitude(sec.getStartLongitude());
            vo.setStartLatitude(sec.getStartLatitude());
            vo.setEndLongitude(sec.getEndLongitude());
            vo.setEndLatitude(sec.getEndLatitude());
            YzWaterFacilityBaseDO facility = facilityMap.get(sec.getFacilityId());
            if (facility != null) {
                vo.setGeomType(facility.getGeomType());
                vo.setSrid(facility.getSrid());
                vo.setGeomWkt(toWktWithSrid(facility.getGeom()));
            }
            vo.setHeads(buildHeadVos(headBySection.get(sec.getId()), supervisionBySection.getOrDefault(sec.getId(), List.of()), headLevelMap, channelResponsibilities));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 构建二维码数据中的河长信息
     */
    private List<RiverHeadQrVO> buildHeadVos(List<YzRiverChannelManagementDO> heads,
                                            List<YzRiverChannelSupervisionDO> supervisions,
                                            Map<String, String> headLevelMap,
                                            String channelResponsibilities) {
        if (CollUtil.isEmpty(heads)) {
            return List.of();
        }
        return heads.stream().map(item -> {
            RiverHeadQrVO vo = new RiverHeadQrVO();
            vo.setId(item.getId());
            vo.setRiverSectionId(item.getRiverSectionId());
            vo.setSectionName(item.getSectionName());
            vo.setHeadLevel(item.getHeadLevel());
            vo.setHeadLevelLabel(resolveLabel(item.getHeadLevel(), headLevelMap));
            vo.setHeadName(item.getHeadName());
            vo.setHeadPosition(item.getHeadPosition());
            vo.setHeadUnit(item.getHeadUnit());
            vo.setHeadContact(item.getHeadContact());
            vo.setResponsibilities(channelResponsibilities);
            vo.setRemarks(item.getRemarks());
            vo.setSupervisions(buildSupervisionVos(supervisions));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 构建二维码数据中的监督信息
     */
    private List<RiverSupervisionQrVO> buildSupervisionVos(List<YzRiverChannelSupervisionDO> supervisions) {
        if (CollUtil.isEmpty(supervisions)) {
            return List.of();
        }
        return supervisions.stream().map(item -> {
            RiverSupervisionQrVO vo = new RiverSupervisionQrVO();
            vo.setId(item.getId());
            vo.setRiverSectionId(item.getRiverSectionId());
            vo.setSupervisionUnit(item.getSupervisionUnit());
            vo.setSupervisionContact(item.getSupervisionContact());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 构建二维码数据中的河道概要
     */
    private RiverChannelQrSummaryVO buildChannelQrSummary(YzRiverChannelDO channel,
                                                         YzWaterFacilityBaseDO facility,
                                                         Map<String, String> basinMap,
                                                         Map<String, String> riverTypeMap,
                                                         Map<String, String> ecologyMap,
                                                         Map<String, String> riverLevelMap) {
        RiverChannelQrSummaryVO summary = new RiverChannelQrSummaryVO();
        summary.setId(channel.getId());
        summary.setRiverName(channel.getRiverName());
        summary.setLengthKm(channel.getLengthKm());
        summary.setStartPoint(channel.getStartPoint());
        summary.setEndPoint(channel.getEndPoint());
        summary.setCatchmentKm2(channel.getCatchmentKm2());
        summary.setBasinType(channel.getBasinType());
        summary.setBasinTypeLabel(resolveLabel(channel.getBasinType(), basinMap));
        summary.setRiverType(channel.getRiverType());
        summary.setRiverTypeLabel(resolveRiverTypeLabel(channel.getRiverType(), riverTypeMap));
        summary.setEcologyType(channel.getEcologyType());
        summary.setEcologyTypeLabel(resolveLabel(channel.getEcologyType(), ecologyMap));
        summary.setRiverLevel(channel.getRiverLevel());
        summary.setRiverLevelLabel(resolveLabel(channel.getRiverLevel(), riverLevelMap));
        summary.setRemarks(channel.getRemarks());
        summary.setFlowAreas(joinArray(channel.getFlowAreas()));
        summary.setHistoricalMaxWaterLevel(channel.getHistoricalMaxWaterLevel());
        summary.setHistoricalMinWaterLevel(channel.getHistoricalMinWaterLevel());
        summary.setFacilityId(channel.getFacilityId());
        if (facility != null) {
            summary.setGeomType(facility.getGeomType());
            summary.setSrid(facility.getSrid());
            summary.setGeomWkt(toWktWithSrid(facility.getGeom()));
        }
        return summary;
    }

    /**
     * 将二维码数据封装为 data: 链接，方便前端扫描直接解析
     */
    private String buildContentUrl(String riverCode, RiverChannelQrRespVO respVO) {
        // 固定二维码不嵌入动态数据，避免河道信息变更导致二维码内容变化

        return buildApiUrlByCode(riverCode);
    }


    /**
     * 压缩并编码快照，尽量减小二维码数据大小
     */
    private String compressSnapshot(RiverChannelQrSnapshotVO snapshot) {
        String json = JsonUtils.toJsonString(snapshot);
        byte[] raw = json.getBytes(StandardCharsets.UTF_8);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(raw);
            gzip.finish();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            // 压缩失败时退回原始 JSON Base64，方便仍可生成
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        }
    }

    /**
     * 构建接口 URL（当数据过大时使用）
     */
    private String buildApiUrl(Long channelId) {
        // 直接指向河道详情接口，扫描后可再拉取完整信息
        return "/gis/river/channel/" + channelId;
    }

    private String buildApiUrlByCode(String riverCode) {
        // 通过河道编码访问公开快照接口，避免依赖内部 ID
        return "/gis/river/channel/by-code/" + riverCode;
    }

    /**
     * 生成二维码图片并输出 Base64
     */
    private String buildQrcode(String content) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BufferedImage image = QrCodeUtil.generate(content, 360, 360);
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_RIVER_CHANNEL_QRCODE_FAIL);
        }
    }

    /**
     * Geometry 转换为带 SRID 的 WKT 字符串
     */
    private String toWktWithSrid(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        String wkt = WKT_WRITER.write(geometry);
        int srid = geometry.getSRID();
        if (srid > 0) {
            return "SRID=" + srid + ";" + wkt;
        }
        return wkt;
    }

    private RiverChannelPageRespVO buildPageResp(YzRiverChannelDO item,
                                                 Map<String, String> levelMap,
                                                 Map<String, String> basinMap,
                                                 Map<String, String> typeMap) {
        RiverChannelPageRespVO respVO = new RiverChannelPageRespVO();
        respVO.setId(item.getId());
        respVO.setRiverCode(item.getRiverCode());
        respVO.setRiverName(item.getRiverName());
        respVO.setRiverLevelLabel(resolveLabel(item.getRiverLevel(), levelMap));
        respVO.setRiverTypeLabel(resolveRiverTypeLabel(item.getRiverType(), typeMap));
        respVO.setBasinTypeLabel(resolveLabel(item.getBasinType(), basinMap));
        respVO.setStartEndLocation(buildStartEnd(item.getStartPoint(), item.getEndPoint()));
        respVO.setLengthKm(item.getLengthKm());
        respVO.setCatchmentKm2(item.getCatchmentKm2());
        respVO.setManagementUnit(item.getManagementUnit());
        respVO.setIsProvincialBackbone(item.getIsProvincialBackbone());
        return respVO;
    }

    private RiverChannelExportExcelVO buildExportExcelVO(YzRiverChannelDO item,
                                                        Map<String, String> levelMap,
                                                        Map<String, String> basinMap,
                                                        Map<String, String> typeMap,
                                                        Map<String, String> ecologyMap,
                                                        Map<String, String> transboundaryMap,
                                                        Map<String, String> floodMap,
                                                        Map<String, String> embankmentMap,
                                                        Map<String, String> waterQualityMap) {
        RiverChannelExportExcelVO vo = new RiverChannelExportExcelVO();
        vo.setRiverCode(item.getRiverCode());
        vo.setRiverName(item.getRiverName());
        vo.setLengthKm(item.getLengthKm());
        vo.setCatchmentKm2(item.getCatchmentKm2());
        vo.setAverageSlope(item.getAverageSlope());
        vo.setBasinTypeLabel(resolveLabel(item.getBasinType(), basinMap));
        vo.setEcologyTypeLabel(resolveLabel(item.getEcologyType(), ecologyMap));
        vo.setTransboundaryTypeLabel(resolveLabel(item.getTransboundaryType(), transboundaryMap));
        vo.setFloodStandardLabel(resolveLabel(item.getFloodStandard(), floodMap));
        vo.setEmbankmentLevelLabel(resolveLabel(item.getEmbankmentLevel(), embankmentMap));
        vo.setEmbankmentLength(item.getEmbankmentLength());
        vo.setCentroidLongitude(item.getCentroidLongitude());
        vo.setCentroidLatitude(item.getCentroidLatitude());
        vo.setRiverEndLongitude(item.getRiverEndLongitude());
        vo.setRiverEndLatitude(item.getRiverEndLatitude());
        vo.setRiverSourceLongitude(item.getRiverSourceLongitude());
        vo.setRiverSourceLatitude(item.getRiverSourceLatitude());
        vo.setFlowAreas(joinArray(item.getFlowAreas()));
        vo.setHistoricalMaxWaterLevel(item.getHistoricalMaxWaterLevel());
        vo.setMaxWaterLevelDate(item.getMaxWaterLevelDate());
        vo.setLowestWaterLevelDate(item.getLowestWaterLevelDate());
        vo.setHistoricalMinWaterLevel(item.getHistoricalMinWaterLevel());
        vo.setAverageAnnualRunoff(item.getAverageAnnualRunoff());
        vo.setSourceMountainRange(item.getSourceMountainRange());
        vo.setRiverTerminus(item.getRiverTerminus());
        vo.setRiverLevelLabel(resolveLabel(item.getRiverLevel(), levelMap));
        vo.setRiverEntrance(item.getRiverEntrance());
        vo.setRiverOrigin(item.getRiverOrigin());
        vo.setRiverTypeLabel(resolveRiverTypeLabelForExport(item.getRiverType(), typeMap));
        vo.setStartPoint(item.getStartPoint());
        vo.setEndPoint(item.getEndPoint());
        //vo.setRiverPhotos(joinArray(item.getRiverPhotos()));
        vo.setWaterQualityStatusLabel(resolveLabel(item.getWaterQualityStatus(), waterQualityMap));
        //vo.setAssociatedFacilities(item.getAssociatedFacilities());
        vo.setRiverSectionCount(item.getRiverSectionCount());
        vo.setTown(joinArray(item.getTown()));
        vo.setManagementUnit(item.getManagementUnit());
        vo.setRemarks(item.getRemarks());
        return vo;
    }

    private String resolveRiverTypeLabelForExport(String[] types, Map<String, String> typeMap) {
        if (types == null || types.length == 0) {
            return null;
        }
        return CollUtil.toList(types).stream()
                .map(type -> resolveLabel(type, typeMap))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining(","));
    }

    private String joinArray(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return String.join(",", values);
    }

    private String buildStartEnd(String origin, String entrance) {
        String start = StrUtil.blankToDefault(origin, "");
        String end = StrUtil.blankToDefault(entrance, "");
        if (StrUtil.isBlank(start) && StrUtil.isBlank(end)) {
            return "";
        }
        return start + "/" + end;
    }

    private String resolveRiverTypeLabel(String[] types, Map<String, String> typeMap) {
        if (types == null || types.length <= 0) {
            return null;
        }
        return CollUtil.toList(types).stream()
                .map(type -> resolveLabel(type, typeMap))
                .collect(Collectors.joining("、"));
    }

    private String resolveLabel(String value, Map<String, String> map) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        return StrUtil.blankToDefault(map.get(value), value);
    }

    private List<RiverChiefOverviewSectionVO> buildSectionChiefOverview(List<YzRiverSectionDO> sections,
                                                                        List<YzRiverChannelManagementDO> sectionChiefs,
                                                                        Map<String, String> headLevelMap) {
        if (CollUtil.isEmpty(sections)) {
            return List.of();
        }
        Map<Long, List<YzRiverChannelManagementDO>> grouped = sectionChiefs.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getRiverSectionId() != null || item.getReferenceId() != null)
                .collect(Collectors.groupingBy(item -> item.getRiverSectionId() != null
                                ? item.getRiverSectionId() : item.getReferenceId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<RiverChiefOverviewSectionVO> result = new ArrayList<>();
        for (YzRiverSectionDO section : sections) {
            if (section == null || section.getId() == null) {
                continue;
            }
            List<RiverChiefOverviewChiefVO> chiefs = buildChiefOverviewItems(
                    grouped.getOrDefault(section.getId(), List.of()), headLevelMap);
            if (chiefs.isEmpty()) {
                continue;
            }
            RiverChiefOverviewSectionVO item = new RiverChiefOverviewSectionVO();
            item.setSectionId(section.getId());
            item.setSectionName(StrUtil.blankToDefault(section.getSectionName(), "-"));
            item.setChiefs(chiefs);
            result.add(item);
        }
        return result;
    }

    private List<RiverChiefOverviewChiefVO> buildChiefOverviewItems(List<YzRiverChannelManagementDO> records,
                                                                    Map<String, String> headLevelMap) {
        if (CollUtil.isEmpty(records)) {
            return List.of();
        }
        return records.stream()
                .filter(Objects::nonNull)
                .map(record -> {
                    RiverChiefOverviewChiefVO item = new RiverChiefOverviewChiefVO();
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
     * 创建河道基础设施记录
     */
    private Long createFacilityBase(RiverChannelSaveReqVO reqVO) {
        Long baseId = SNOWFLAKE.nextId();
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityCode(StrUtil.blankToDefault(StrUtil.trimToNull(reqVO.getRiverCode()), String.valueOf(baseId)));
        base.setFacilityName(reqVO.getRiverName());
        base.setFacilityType(FACILITY_TYPE_RIVER);
        base.setManageUnit(reqVO.getManagementUnit());
        base.setBasinCode(reqVO.getBasinType());
        base.setSourceType("system");
        facilityBaseMapper.insert(base);
        return baseId;
    }

    /**
     * 确保河道基础设施存在并更新基础信息
     */
    private Long ensureChannelFacility(RiverChannelSaveReqVO reqVO, Long facilityId) {
        if (facilityId == null) {
            return createFacilityBase(reqVO);
        }
        YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(facilityId);
        if (base == null) {
            return createFacilityBase(reqVO);
        }
        base.setFacilityName(reqVO.getRiverName());
        base.setFacilityCode(StrUtil.blankToDefault(StrUtil.trimToNull(reqVO.getRiverCode()), base.getFacilityCode()));
        base.setFacilityType(FACILITY_TYPE_RIVER);
        base.setManageUnit(reqVO.getManagementUnit());
        base.setBasinCode(reqVO.getBasinType());
        facilityBaseMapper.updateRiverChannelSyncFieldsById(
                base.getId(),
                base.getFacilityName(),
                base.getFacilityType(),
                base.getFacilityCode(),
                base.getManageUnit(),
                base.getBasinCode()
        );
        return base.getId();
    }

    /**
     * 创建河段基础设施记录
     */
    private Long createSectionFacilityBase(String sectionName) {
        Long baseId = SNOWFLAKE.nextId();
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityCode(String.valueOf(baseId));
        base.setFacilityName(sectionName);
        base.setFacilityType(FACILITY_TYPE_RIVER_SECTION);
        base.setSourceType("system");
        facilityBaseMapper.insert(base);
        return baseId;
    }

    /**
     * 更新或创建河段基础设施
     */
    private Long ensureSectionFacility(String sectionName, Long facilityId) {
        if (facilityId == null) {
            return createSectionFacilityBase(sectionName);
        }
        YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(facilityId);
        if (base == null) {
            return createSectionFacilityBase(sectionName);
        }
        base.setFacilityName(sectionName);
        base.setFacilityType(FACILITY_TYPE_RIVER_SECTION);
        facilityBaseMapper.updateById(base);
        return base.getId();
    }

    /**
     * 创建河段列表
     */
    private void insertSections(Long channelId, List<RiverSectionSaveReqVO> sections) {
        if (CollUtil.isEmpty(sections)) {
            return;
        }
        List<YzRiverSectionDO> entities = sections.stream().map(item -> {
            YzRiverSectionDO section = new YzRiverSectionDO();
            section.setId(SNOWFLAKE.nextId());
            section.setRiverChannelId(channelId);
            section.setSectionName(item.getSectionName());
            section.setFacilityId(createSectionFacilityBase(item.getSectionName()));
            section.setStartPoint(item.getStartPoint());
            section.setEndPoint(item.getEndPoint());
            section.setStartLongitude(item.getStartLongitude());
            section.setStartLatitude(item.getStartLatitude());
            section.setEndLongitude(item.getEndLongitude());
            section.setEndLatitude(item.getEndLatitude());
            section.setRemarks(item.getRemarks());
            return section;
        }).collect(Collectors.toList());
        riverSectionMapper.insertBatch(entities);
    }

    /**
     * 河段增删改（带基础表与关联清理）
     */
    private void upsertSections(Long channelId, List<RiverSectionSaveReqVO> sections) {
        List<YzRiverSectionDO> exists = riverSectionMapper.selectList(
                new LambdaQueryWrapper<YzRiverSectionDO>().eq(YzRiverSectionDO::getRiverChannelId, channelId));
        Map<Long, YzRiverSectionDO> existsMap = exists.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzRiverSectionDO::getId, item -> item));

        if (CollUtil.isEmpty(sections)) {
            if (!exists.isEmpty()) {
                deleteSectionsWithRelations(exists.stream().map(YzRiverSectionDO::getId).collect(Collectors.toList()), existsMap);
            }
            return;
        }

        List<YzRiverSectionDO> toInsert = new ArrayList<>();
        List<YzRiverSectionDO> toUpdate = new ArrayList<>();
        Set<Long> requestIds = new HashSet<>();

        for (RiverSectionSaveReqVO item : sections) {
            Long sectionId = item.getId();
            if (sectionId != null) {
                requestIds.add(sectionId);
            }
            if (sectionId != null && existsMap.containsKey(sectionId)) {
                YzRiverSectionDO section = existsMap.get(sectionId);
                section.setSectionName(item.getSectionName());
                section.setFacilityId(ensureSectionFacility(item.getSectionName(), section.getFacilityId()));
                section.setStartPoint(item.getStartPoint());
                section.setEndPoint(item.getEndPoint());
                section.setStartLongitude(item.getStartLongitude());
                section.setStartLatitude(item.getStartLatitude());
                section.setEndLongitude(item.getEndLongitude());
                section.setEndLatitude(item.getEndLatitude());
                section.setRemarks(item.getRemarks());
                toUpdate.add(section);
            } else {
                YzRiverSectionDO section = new YzRiverSectionDO();
                section.setId(SNOWFLAKE.nextId());
                section.setRiverChannelId(channelId);
                section.setSectionName(item.getSectionName());
                section.setFacilityId(ensureSectionFacility(item.getSectionName(), item.getFacilityId()));
                section.setStartPoint(item.getStartPoint());
                section.setEndPoint(item.getEndPoint());
                section.setStartLongitude(item.getStartLongitude());
                section.setStartLatitude(item.getStartLatitude());
                section.setEndLongitude(item.getEndLongitude());
                section.setEndLatitude(item.getEndLatitude());
                section.setRemarks(item.getRemarks());
                toInsert.add(section);
            }
        }

        // 删除被移除的河段及关联信息
        List<Long> removedIds = existsMap.keySet().stream()
                .filter(id -> !requestIds.contains(id))
                .collect(Collectors.toList());
        if (!removedIds.isEmpty()) {
            deleteSectionsWithRelations(removedIds, existsMap);
        }

        if (!toInsert.isEmpty()) {
            riverSectionMapper.insertBatch(toInsert);
        }
        for (YzRiverSectionDO section : toUpdate) {
            riverSectionMapper.updateEditFieldsById(section);
        }
    }

    /**
     * 删除河段、基础表及河长/监督关联
     */
    private void deleteSectionsWithRelations(List<Long> sectionIds, Map<Long, YzRiverSectionDO> existsMap) {
        if (CollUtil.isEmpty(sectionIds)) {
            return;
        }
        supervisionMapper.delete(new LambdaQueryWrapper<YzRiverChannelSupervisionDO>()
                .in(YzRiverChannelSupervisionDO::getRiverSectionId, sectionIds));
        managementMapper.delete(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .in(YzRiverChannelManagementDO::getRiverSectionId, sectionIds));
        riverSectionMapper.delete(new LambdaQueryWrapper<YzRiverSectionDO>().in(YzRiverSectionDO::getId, sectionIds));
        List<Long> facilityIds = sectionIds.stream()
                .map(existsMap::get)
                .filter(Objects::nonNull)
                .map(YzRiverSectionDO::getFacilityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!facilityIds.isEmpty()) {
            facilityBaseMapper.deleteBatchIds(facilityIds);
        }
    }
}
