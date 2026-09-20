package com.sydigit.yzwater.module.service.problem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.security.core.LoginUser;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.util.GuestRoleUtils;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackAuditReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackDashboardRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackNotifyRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackProcessReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackStatusSummaryVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackVerifyReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemStatusTaskRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackUserSimpleRespVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackMyPageReqVO;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.dal.dataobject.problem.YzProblemFeedbackDO;
import com.sydigit.yzwater.module.dal.dataobject.problem.YzProblemStatusTaskDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseBfDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirBfDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.mysql.problem.YzProblemFeedbackMapper;
import com.sydigit.yzwater.module.dal.mysql.problem.YzProblemStatusTaskMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirBfMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseBfMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.system.SystemUserSimpleMapper;
import com.sydigit.yzwater.module.dal.mysql.system.dto.SystemUserRoleRow;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.api.sms.SmsSendApi;
import com.sydigit.yzwater.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import com.sydigit.yzwater.module.service.shortlink.ShortLinkService;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 问题反馈服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class ProblemFeedbackService {

    private static final int STATUS_WAIT_AUDIT = 0;//待受理
    private static final int STATUS_REJECTED = 1;//已驳回
    private static final int STATUS_PROCESSING = 2;//处理中
    private static final int STATUS_PENDING_VERIFY = 3;//待核验
    private static final int STATUS_FINISHED = 4;//已办结
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private static final String PERIOD_MONTH = "month";
    private static final String PERIOD_QUARTER = "quarter";
    private static final String PERIOD_YEAR = "year";

    /**
     * 游客角色编码：纯游客不允许查看管理端问题反馈数据
     */
    private static final String ROLE_GUEST = GuestRoleUtils.ROLE_GUEST;

    /**
     * 管理员角色编码：拥有任意一个管理员角色即可查看全部问题反馈
     */
    private static final List<String> ADMIN_ROLE_CODES = List.of(
            "super_admin",
            "tenant_admin",
            "crm_admin",
            "sladmin",
            "yzadmin",
            // 河长制（角色标识 river_head）：问题反馈分页/列表/导出按管理员口径查看全部
            "river_head"
    );

    private static final String SMS_TEMPLATE_PUBLIC_ACCEPTED = "SMS_499290705";//问题受理通知
    private static final String SMS_TEMPLATE_PUBLIC_REJECTED = "SMS_499170662";//管理员驳回通知
    private static final String SMS_TEMPLATE_PUBLIC_FINISHED = "SMS_499215706";//办结通知
    private static final String SMS_TEMPLATE_HANDLER_ASSIGN = "SMS_499260689";//问题指派通知
    private static final String SMS_TEMPLATE_HANDLER_OVERDUE = "SMS_499160720";//催办通知


    //飞鸽短信平台
    private static final String FEIGE_TEMPLATE_PUBLIC_ACCEPTED = "15549";//问题受理通知
    private static final String FEIGE_TEMPLATE_PUBLIC_REJECTED = "16198";//管理员驳回通知
    private static final String FEIGE_TEMPLATE_PUBLIC_FINISHED = "15550";//办结通知
    private static final String FEIGE_TEMPLATE_HANDLER_ASSIGN = "15553";//问题指派通知
    private static final String FEIGE_TEMPLATE_HANDLER_OVERDUE = "15548";//催办通知

    private static final DateTimeFormatter SMS_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String SHORT_LINK_SCENE_PUBLIC_DETAIL = "PROBLEM_PUBLIC_DETAIL";
    private static final String SHORT_LINK_SCENE_HANDLER_TASK = "PROBLEM_HANDLER_TASK";
    private static final String SHORT_LINK_BIZ_TYPE_PROBLEM_FEEDBACK = "PROBLEM_FEEDBACK";
    private static final int SHORT_LINK_PUBLIC_EXPIRE_DAYS = 30;
    private static final int SHORT_LINK_HANDLER_EXPIRE_DAYS = 7;

    private static final Pattern RESOLUTION_DESCRIPTION_PATTERN =
            Pattern.compile("^处理结果[:：](.*?)\\s*[;；]\\s*处理描述[:：](.*)$");

    private final YzProblemFeedbackMapper feedbackMapper;
    private final YzProblemStatusTaskMapper statusTaskMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverChannelManagementMapper riverChannelManagementMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    /** 手机端问题反馈：关联设施校验与展示兜底（仅 BF 备份表） */
    private final YzRiverChannelBfMapper riverChannelBfMapper;
    private final YzRiverSectionBfMapper riverSectionBfMapper;
    private final YzWaterReservoirBfMapper waterReservoirBfMapper;
    private final YzWaterFacilityBaseBfMapper facilityBaseBfMapper;
    private final DictDataCommonApi dictDataApi;
    private final SystemUserSimpleMapper systemUserSimpleMapper;
    private final SystemAreaMapper systemAreaMapper;
    private final SmsSendApi smsSendApi;
    private final ShortLinkService shortLinkService;

    /**
     * 公示牌关联对象解析结果。
     *
     * @param referenceType 关联类型（river/river_section/reservoir）
     * @param referenceId   关联ID（对应河道/河段/水库主键）
     * @param facilityCode  设施编码
     * @param referenceName 关联对象名称（同时作为设施名称）
     */
    private record ReferenceInfo(String referenceType, Long referenceId, String facilityCode, String referenceName) {
    }

    /**
     * 创建问题反馈并生成初始状态任务
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createFeedback(ProblemFeedbackCreateReqVO reqVO) {
        String referenceType = normalizeReferenceType(reqVO.getReferenceType());
        Long referenceId = reqVO.getReferenceId();
        if (referenceType == null || referenceId == null) {
            throw ServiceExceptionUtil.invalidParamException("关联对象不能为空");
        }
        ReferenceInfo referenceInfo = resolveReferenceInfo(referenceType, referenceId);
        if (referenceInfo == null) {
            throw ServiceExceptionUtil.invalidParamException("关联对象不存在");
        }

        YzProblemFeedbackDO feedback = BeanUtils.toBean(reqVO, YzProblemFeedbackDO.class);
        feedback.setId(SNOWFLAKE.nextId());
        feedback.setStatus(STATUS_WAIT_AUDIT);
        feedback.setReferenceType(referenceType);
        feedback.setReferenceId(referenceId);
        String[] divisionCodes = resolveDivisionCodes(referenceType, referenceId);
        if (divisionCodes != null && divisionCodes.length > 0) {
            feedback.setDivisionCode(divisionCodes);
        }
        List<String> files = reqVO.getUploadedFiles();
        feedback.setUploadedFiles(CollUtil.isEmpty(files) ? null : files.toArray(new String[0]));
        feedbackMapper.insert(feedback);

        YzProblemStatusTaskDO statusTask = new YzProblemStatusTaskDO();
        statusTask.setId(SNOWFLAKE.nextId());
        statusTask.setProblemFeedbackId(feedback.getId());
        statusTask.setStatus(STATUS_WAIT_AUDIT);
        statusTask.setExpedited(0);
        statusTaskMapper.insert(statusTask);
        return feedback.getId();
    }

    /**
     * 手机端 App 创建问题反馈：关联河道/河段/水库仅接受备份表（_bf）中存在的数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createFeedbackForMobileApp(ProblemFeedbackCreateReqVO reqVO) {
        String referenceType = normalizeReferenceType(reqVO.getReferenceType());
        Long referenceId = reqVO.getReferenceId();
        if (referenceType == null || referenceId == null) {
            throw ServiceExceptionUtil.invalidParamException("关联对象不能为空");
        }
        ReferenceInfo referenceInfo = resolveReferenceInfoFromBackupTables(referenceType, referenceId);
        if (referenceInfo == null) {
            throw ServiceExceptionUtil.invalidParamException("关联对象不存在");
        }

        YzProblemFeedbackDO feedback = BeanUtils.toBean(reqVO, YzProblemFeedbackDO.class);
        feedback.setId(SNOWFLAKE.nextId());
        feedback.setStatus(STATUS_WAIT_AUDIT);
        feedback.setReferenceType(referenceType);
        feedback.setReferenceId(referenceId);
        String[] divisionCodes = resolveDivisionCodesFromBackupTables(referenceType, referenceId);
        if (divisionCodes != null && divisionCodes.length > 0) {
            feedback.setDivisionCode(divisionCodes);
        }
        List<String> files = reqVO.getUploadedFiles();
        feedback.setUploadedFiles(CollUtil.isEmpty(files) ? null : files.toArray(new String[0]));
        feedbackMapper.insert(feedback);

        YzProblemStatusTaskDO statusTask = new YzProblemStatusTaskDO();
        statusTask.setId(SNOWFLAKE.nextId());
        statusTask.setProblemFeedbackId(feedback.getId());
        statusTask.setStatus(STATUS_WAIT_AUDIT);
        statusTask.setExpedited(0);
        statusTaskMapper.insert(statusTask);
        return feedback.getId();
    }

    /**
     * 分页查询问题反馈（管理员）
     */
    public PageResult<ProblemFeedbackPageRespVO> getFeedbackPage(ProblemFeedbackPageReqVO reqVO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (isGuest(loginUser)) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = feedbackMapper.buildQueryWrapper(reqVO);

        if (!applyReferenceFilter(wrapper, reqVO)) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        if (!isAdmin(loginUser)) {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            if (userId == null) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
            // 普通用户仅查看指派给自己的任务，使用子查询避免大列表 in(...) 带来的 SQL 长度与性能问题
            wrapper.inSql(YzProblemFeedbackDO::getId,
                    "select distinct problem_feedback from yz_problem_status_task where assigned_person_id = " + userId);
        }
        wrapper.orderByDesc(YzProblemFeedbackDO::getUpdateTime);
        PageResult<YzProblemFeedbackDO> page = feedbackMapper.selectPage(reqVO, wrapper);
        Map<String, String> feedbackTypeLabelMap = loadDictLabel(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabel(ZdConstants.ZD_WTJD);
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(page.getList());
        Map<Long, YzProblemStatusTaskDO> latestTaskMap = loadLatestTask(page.getList());
        Map<Long, String> nicknameMap = loadNicknameMap(latestTaskMap);
        Long currentUserId = loginUser == null ? null : loginUser.getId();
        List<ProblemFeedbackPageRespVO> list = page.getList().stream()
                .map(item -> {
                    YzProblemStatusTaskDO latestTask = latestTaskMap.get(item.getId());
                    ProblemFeedbackPageRespVO vo = buildResp(item,
                        referenceInfoMap,
                        latestTask,
                        nicknameMap,
                        feedbackTypeLabelMap,
                        progressLabelMap);
                    // 动态状态文案：当前用户作为处理人查看“处理中(2)”时显示为“待处理”（仅影响展示，不改变数据库状态）
                    if (Objects.equals(item.getStatus(), STATUS_PROCESSING)
                            && currentUserId != null
                            && latestTask != null
                            && Objects.equals(latestTask.getAssignedPersonId(), currentUserId)) {
                        vo.setStatusLabel("待处理");
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 列表查询问题反馈（不分页）
     */
    public List<ProblemFeedbackPageRespVO> getFeedbackList(ProblemFeedbackListReqVO reqVO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (isGuest(loginUser)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = feedbackMapper.buildQueryWrapper(reqVO);

        if (!applyReferenceFilter(wrapper, reqVO)) {
            return Collections.emptyList();
        }

        if (!isAdmin(loginUser)) {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            if (userId == null) {
                return Collections.emptyList();
            }
            wrapper.inSql(YzProblemFeedbackDO::getId,
                    "select distinct problem_feedback from yz_problem_status_task where assigned_person_id = " + userId);
        }

        List<YzProblemFeedbackDO> list = feedbackMapper.selectList(wrapper);
        Map<String, String> feedbackTypeLabelMap = loadDictLabel(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabel(ZdConstants.ZD_WTJD);
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(list);
        Map<Long, YzProblemStatusTaskDO> latestTaskMap = loadLatestTask(list);
        Map<Long, String> nicknameMap = loadNicknameMap(latestTaskMap);
        Long currentUserId = loginUser == null ? null : loginUser.getId();
        return list.stream()
                .map(item -> {
                    YzProblemStatusTaskDO latestTask = latestTaskMap.get(item.getId());
                    ProblemFeedbackPageRespVO vo = buildResp(item,
                            referenceInfoMap,
                            latestTask,
                            nicknameMap,
                            feedbackTypeLabelMap,
                            progressLabelMap);
                    if (Objects.equals(item.getStatus(), STATUS_PROCESSING)
                            && currentUserId != null
                            && latestTask != null
                            && Objects.equals(latestTask.getAssignedPersonId(), currentUserId)) {
                        vo.setStatusLabel("待处理");
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 大屏分页查询问题反馈（不校验登录与角色，统一按“管理员口径”查询全部数据）。
     *
     * <p>说明：仅用于大屏统计展示，不改变数据库状态。</p>
     */
    public PageResult<ProblemFeedbackPageRespVO> getFeedbackPageForScreen(ProblemFeedbackPageReqVO reqVO) {
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = feedbackMapper.buildQueryWrapper(reqVO);

        if (!applyReferenceFilter(wrapper, reqVO)) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        PageResult<YzProblemFeedbackDO> page = feedbackMapper.selectPage(reqVO, wrapper);
        Map<String, String> feedbackTypeLabelMap = loadDictLabel(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabel(ZdConstants.ZD_WTJD);
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(page.getList());
        Map<Long, YzProblemStatusTaskDO> latestTaskMap = loadLatestTask(page.getList());
        Map<Long, String> nicknameMap = loadNicknameMap(latestTaskMap);

        List<ProblemFeedbackPageRespVO> list = page.getList().stream()
                .map(item -> buildResp(item,
                        referenceInfoMap,
                        latestTaskMap.get(item.getId()),
                        nicknameMap,
                        feedbackTypeLabelMap,
                        progressLabelMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 大屏列表查询问题反馈（不分页、不校验登录与角色，统一按“管理员口径”查询全部数据）
     *
     * <p>说明：仅用于大屏统计展示，不改变数据库状态。</p>
     */
    public List<ProblemFeedbackPageRespVO> getFeedbackListForScreen(ProblemFeedbackListReqVO reqVO) {
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = feedbackMapper.buildQueryWrapper(reqVO);

        if (!applyReferenceFilter(wrapper, reqVO)) {
            return Collections.emptyList();
        }

        List<YzProblemFeedbackDO> list = feedbackMapper.selectList(wrapper);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<String, String> feedbackTypeLabelMap = loadDictLabel(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabel(ZdConstants.ZD_WTJD);
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(list);
        Map<Long, YzProblemStatusTaskDO> latestTaskMap = loadLatestTask(list);
        Map<Long, String> nicknameMap = loadNicknameMap(latestTaskMap);

        return list.stream()
                .map(item -> buildResp(item,
                        referenceInfoMap,
                        latestTaskMap.get(item.getId()),
                        nicknameMap,
                        feedbackTypeLabelMap,
                        progressLabelMap))
                .collect(Collectors.toList());
    }

    /**
     * 导出问题反馈（管理员导出全部，普通用户仅导出指派给自己的任务）
     */
    public List<ProblemFeedbackExportExcelVO> getFeedbackExportList(ProblemFeedbackPageReqVO reqVO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (isGuest(loginUser)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = feedbackMapper.buildQueryWrapper(reqVO);

        if (!applyReferenceFilter(wrapper, reqVO)) {
            return Collections.emptyList();
        }

        if (!isAdmin(loginUser)) {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            if (userId == null) {
                return Collections.emptyList();
            }
            wrapper.inSql(YzProblemFeedbackDO::getId,
                    "select distinct problem_feedback from yz_problem_status_task where assigned_person_id = " + userId);
        }

        List<YzProblemFeedbackDO> feedbackList = feedbackMapper.selectList(wrapper);
        if (CollUtil.isEmpty(feedbackList)) {
            return Collections.emptyList();
        }
        Map<String, String> feedbackTypeLabelMap = loadDictLabel(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabel(ZdConstants.ZD_WTJD);
        Map<String, String> facilityTypeLabelMap = loadDictLabel(ZdConstants.ZD_SSLB);
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(feedbackList);
        Map<Long, YzProblemStatusTaskDO> latestTaskMap = loadLatestTask(feedbackList);
        Map<Long, String> nicknameMap = loadNicknameMap(latestTaskMap);
        Long currentUserId = loginUser == null ? null : loginUser.getId();

        return feedbackList.stream()
                .map(item -> {
                    YzProblemStatusTaskDO latestTask = latestTaskMap.get(item.getId());
                    ProblemFeedbackPageRespVO vo = buildResp(item,
                            referenceInfoMap,
                            latestTask,
                            nicknameMap,
                            feedbackTypeLabelMap,
                            progressLabelMap);
                    // 动态状态文案：当前用户作为处理人导出“处理中(2)”时显示为“待处理”（保持与列表一致）
                    if (Objects.equals(item.getStatus(), STATUS_PROCESSING)
                            && currentUserId != null
                            && latestTask != null
                            && Objects.equals(latestTask.getAssignedPersonId(), currentUserId)) {
                        vo.setStatusLabel("待处理");
                    }
                    ProblemFeedbackExportExcelVO excelVO = new ProblemFeedbackExportExcelVO();
                    excelVO.setFacilityCode(vo.getFacilityCode());
                    excelVO.setFacilityName(vo.getFacilityName());
                    excelVO.setFacilityTypeLabel(StrUtil.blankToDefault(facilityTypeLabelMap.get(vo.getReferenceType()), vo.getReferenceType()));
                    excelVO.setFeedbackTypeLabel(vo.getFeedbackTypeLabel());
                    excelVO.setFeedbackContent(vo.getFeedbackContent());
                    excelVO.setFeedbackPerson(vo.getFeedbackPerson());
                    //List<String> uploadedFiles = vo.getUploadedFiles();
                    //excelVO.setUploadedFiles(CollUtil.isEmpty(uploadedFiles) ? null : String.join(";", uploadedFiles));
                    excelVO.setSpecificLocation(item.getIssueSpecificLocation());
                    excelVO.setCreateTime(vo.getCreateTime());
                    excelVO.setStatusLabel(vo.getStatusLabel());
                    excelVO.setAssignedPersonName(vo.getAssignedPersonName());
                    return excelVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 按关联设施条件过滤问题反馈。
     *
     * @return true 可继续执行查询；false 无匹配结果，直接返回空列表
     */
    private boolean applyReferenceFilter(LambdaQueryWrapper<YzProblemFeedbackDO> wrapper, ProblemFeedbackPageReqVO reqVO) {
        if (reqVO == null) {
            return true;
        }
        return applyReferenceFilter(wrapper, reqVO.getFacilityType(), reqVO.getFacilityName(), reqVO.getRiverName(), reqVO.getRiverCode());
    }

    private boolean applyReferenceFilter(LambdaQueryWrapper<YzProblemFeedbackDO> wrapper, ProblemFeedbackListReqVO reqVO) {
        if (reqVO == null) {
            return true;
        }
        return applyReferenceFilter(wrapper, reqVO.getFacilityType(), reqVO.getFacilityName(), reqVO.getRiverName(), reqVO.getRiverCode());
    }

    private boolean applyReferenceFilter(LambdaQueryWrapper<YzProblemFeedbackDO> wrapper,
                                         String facilityTypeRaw,
                                         String facilityNameRaw,
                                         String riverNameRaw,
                                         String riverCodeRaw) {
        List<ReferenceCondition> conditions = resolveReferenceConditions(facilityTypeRaw, facilityNameRaw, riverNameRaw, riverCodeRaw);
        if (conditions == null) {
            return true;
        }
        if (conditions.isEmpty()) {
            return false;
        }
        wrapper.and(q -> {
            boolean first = true;
            for (ReferenceCondition condition : conditions) {
                if (condition == null || condition.referenceType() == null) {
                    continue;
                }
                if (!first) {
                    q.or();
                }
                List<Long> ids = condition.referenceIds();
                if (ids == null) {
                    q.eq(YzProblemFeedbackDO::getReferenceType, condition.referenceType());
                } else if (!ids.isEmpty()) {
                    q.eq(YzProblemFeedbackDO::getReferenceType, condition.referenceType())
                            .in(YzProblemFeedbackDO::getReferenceId, ids);
                }
                first = false;
            }
        });
        return true;
    }

    private List<ReferenceCondition> resolveReferenceConditions(String facilityTypeRaw,
                                                                 String facilityNameRaw,
                                                                 String riverNameRaw,
                                                                 String riverCodeRaw) {
        String facilityType = normalizeReferenceType(facilityTypeRaw);
        String facilityName = trimToNull(facilityNameRaw);
        if (facilityName == null) {
            facilityName = trimToNull(riverNameRaw);
        }
        String riverCode = trimToNull(riverCodeRaw);
        if (facilityType == null && facilityName == null && riverCode == null) {
            return null;
        }
        List<ReferenceCondition> result = new ArrayList<>();
        if (facilityType != null) {
            ReferenceCondition condition = buildReferenceConditionByType(facilityType, facilityName, riverCode);
            return condition == null ? Collections.emptyList() : List.of(condition);
        }
        ReferenceCondition river = buildReferenceConditionByType(ReferenceTypeConstants.RIVER, facilityName, riverCode);
        if (river != null) {
            result.add(river);
        }
        ReferenceCondition section = buildReferenceConditionByType(ReferenceTypeConstants.RIVER_SECTION, facilityName, null);
        if (section != null) {
            result.add(section);
        }
        ReferenceCondition reservoir = buildReferenceConditionByType(ReferenceTypeConstants.RESERVOIR, facilityName, null);
        if (reservoir != null) {
            result.add(reservoir);
        }
        return result;
    }

    private ReferenceCondition buildReferenceConditionByType(String referenceType, String facilityName, String riverCode) {
        String type = normalizeReferenceType(referenceType);
        if (type == null) {
            return null;
        }
        boolean hasKeyword = facilityName != null || riverCode != null;
        if (!hasKeyword) {
            return new ReferenceCondition(type, null);
        }
        List<Long> ids = null;
        if (ReferenceTypeConstants.RIVER.equals(type)) {
            ids = resolveRiverChannelIds(facilityName, riverCode);
        } else if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            ids = resolveRiverSectionIds(facilityName, null);
        } else if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            ids = resolveReservoirIds(facilityName, null);
        }
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return new ReferenceCondition(type, ids);
    }

    private record ReferenceCondition(String referenceType, List<Long> referenceIds) {
    }

    private String normalizeReferenceType(String referenceType) {
        if (StrUtil.isBlank(referenceType)) {
            return null;
        }
        String type = StrUtil.trim(referenceType).toLowerCase();
        if ("river_section".equals(type) || "riversection".equals(type) || "river-section".equals(type)) {
            return ReferenceTypeConstants.RIVER_SECTION;
        }
        if ("river".equals(type) || "river_channel".equals(type) || "channel".equals(type)) {
            return ReferenceTypeConstants.RIVER;
        }
        if ("reservoir".equals(type) || "water_reservoir".equals(type) || "waterreservoir".equals(type)) {
            return ReferenceTypeConstants.RESERVOIR;
        }
        return type;
    }

    private List<Long> resolveRiverChannelIds(String facilityName, String riverCode) {
        if (facilityName == null && riverCode == null) {
            return null;
        }
        List<YzRiverChannelDO> list = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId)
                .like(facilityName != null, YzRiverChannelDO::getRiverName, facilityName)
                .like(riverCode != null, YzRiverChannelDO::getRiverCode, riverCode));
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(YzRiverChannelDO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> resolveRiverSectionIds(String facilityName, String riverName) {
        if (facilityName == null && riverName == null) {
            return null;
        }
        Set<Long> ids = new HashSet<>();
        if (facilityName != null) {
            List<YzRiverSectionDO> sections = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                    .select(YzRiverSectionDO::getId)
                    .like(YzRiverSectionDO::getSectionName, facilityName));
            if (sections != null) {
                for (YzRiverSectionDO section : sections) {
                    if (section != null && section.getId() != null) {
                        ids.add(section.getId());
                    }
                }
            }
        }
        if (riverName != null) {
            List<Long> channelIds = resolveRiverChannelIds(riverName, null);
            if (CollUtil.isNotEmpty(channelIds)) {
                List<YzRiverSectionDO> sections = riverSectionMapper.selectList(new LambdaQueryWrapper<YzRiverSectionDO>()
                        .select(YzRiverSectionDO::getId)
                        .in(YzRiverSectionDO::getRiverChannelId, channelIds));
                if (sections != null) {
                    for (YzRiverSectionDO section : sections) {
                        if (section != null && section.getId() != null) {
                            ids.add(section.getId());
                        }
                    }
                }
            }
        }
        return ids.isEmpty() ? Collections.emptyList() : new ArrayList<>(ids);
    }

    private List<Long> resolveReservoirIds(String facilityName, String reservoirCode) {
        if (facilityName == null && reservoirCode == null) {
            return null;
        }
        List<YzWaterReservoirDO> list = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                .select(YzWaterReservoirDO::getId)
                .like(facilityName != null, YzWaterReservoirDO::getReservoirName, facilityName)
                .like(reservoirCode != null, YzWaterReservoirDO::getReservoirCode, reservoirCode));
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(YzWaterReservoirDO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private String trimToNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return StrUtil.trim(value);
    }

    /**
     * 手机端分页查询我提出的问题
     */
    public PageResult<ProblemFeedbackPageRespVO> getMyRiverProblemPage(AppProblemFeedbackMyPageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .eq(YzProblemFeedbackDO::getCreator, String.valueOf(userId))
                .orderByDesc(YzProblemFeedbackDO::getCreateTime);

        Integer statusFilter = reqVO.getStatusFilter();
        if (statusFilter != null) {
            if (statusFilter >= STATUS_WAIT_AUDIT && statusFilter <= STATUS_FINISHED) {
                wrapper.eq(YzProblemFeedbackDO::getStatus, statusFilter);
            }
        }

        LocalDate[] createTime = parseCreateDateRange(reqVO.getCreateTime());
        if (createTime != null && createTime.length == 2 && (createTime[0] != null || createTime[1] != null)) {
            // 入参 createTime 精确到“天”，这里统一转成当天起止范围（包含结束日）进行过滤
            LocalDate startDate = createTime[0];
            LocalDate endDate = createTime[1];
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("开始时间不能晚于结束时间");
                }
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
                wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDateTime)
                        .lt(YzProblemFeedbackDO::getCreateTime, endExclusive);
            } else if (startDate != null) {
                wrapper.ge(YzProblemFeedbackDO::getCreateTime, startDate.atStartOfDay());
            } else if (endDate != null) {
                wrapper.lt(YzProblemFeedbackDO::getCreateTime, endDate.plusDays(1).atStartOfDay());
            }
        }

        PageResult<YzProblemFeedbackDO> page = feedbackMapper.selectPage(reqVO, wrapper);
        Map<String, String> feedbackTypeLabelMap = loadDictLabel(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabel(ZdConstants.ZD_WTJD);
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(page.getList());
        Map<Long, YzProblemStatusTaskDO> latestTaskMap = loadLatestTask(page.getList());
        Map<Long, String> nicknameMap = loadNicknameMap(latestTaskMap);
        List<ProblemFeedbackPageRespVO> list = page.getList().stream()
                .map(item -> buildResp(item,
                        referenceInfoMap,
                        latestTaskMap.get(item.getId()),
                        nicknameMap,
                        feedbackTypeLabelMap,
                        progressLabelMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 解析按天范围参数，兼容以下传参方式：
     * <ul>
     *     <li>createTime=2025-12-02&createTime=2025-12-03</li>
     *     <li>createTime[0]=2025-12-02&createTime[1]=2025-12-03</li>
     *     <li>createTime[]=2025-12-02&createTime[]=2025-12-03</li>
     *     <li>createTime=[2025-12-02,2025-12-03]</li>
     * </ul>
     *
     * @param rawCreateTime 原始入参
     * @return 固定长度为 2 的日期数组（开始、结束），元素允许为空
     */
    private LocalDate[] parseCreateDateRange(String[] rawCreateTime) {
        if (rawCreateTime == null || rawCreateTime.length == 0) {
            return null;
        }

        List<String> candidates = new ArrayList<>();
        for (String item : rawCreateTime) {
            if (StrUtil.isBlank(item)) {
                continue;
            }
            String value = StrUtil.trim(item);
            // 兼容：createTime=[2025-12-02,2025-12-03] 或 createTime=["2025-12-02","2025-12-03"]
            if (value.startsWith("[") && value.endsWith("]")) {
                value = value.substring(1, value.length() - 1);
            }
            value = value.replace("\"", "");
            if (value.contains(",")) {
                String[] parts = value.split(",");
                for (String part : parts) {
                    if (StrUtil.isNotBlank(part)) {
                        candidates.add(StrUtil.trim(part));
                    }
                }
            } else {
                candidates.add(value);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        LocalDate start = null;
        LocalDate end = null;
        if (candidates.size() >= 1) {
            start = parseDateOrNull(candidates.get(0));
        }
        if (candidates.size() >= 2) {
            end = parseDateOrNull(candidates.get(1));
        }
        return new LocalDate[]{start, end};
    }

    private LocalDate parseDateOrNull(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String value = StrUtil.trim(text);
        // 仅取日期部分，避免前端误传 2025-12-02 00:00:00 导致解析失败
        if (value.length() > 10) {
            value = value.substring(0, 10);
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("创建时间格式不正确，要求 yyyy-MM-dd，例如 2025-12-02", ex);
        }
    }

    /**
     * 受理并指派
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditAndAssign(ProblemFeedbackAuditReqVO reqVO) {
        ensureAdmin();
        Long reviewerId = SecurityFrameworkUtils.getLoginUserId();
        if (reviewerId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
        }
        YzProblemFeedbackDO feedback = feedbackMapper.selectById(reqVO.getId());
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        LocalDateTime now = LocalDateTime.now();
        // 受理指派阶段不再上传确认图片
        int status = STATUS_PROCESSING;
        if (reqVO.getAssignedPersonId() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_ASSIGNEE_REQUIRED);
        }
        ensureAssigneeNotPureGuest(reqVO.getAssignedPersonId());
        if (reqVO.getPlannedCompletionTime() == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PLANNED_COMPLETION_TIME_REQUIRED);
        }
        if (!reqVO.getPlannedCompletionTime().isAfter(now)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PLANNED_COMPLETION_TIME_INVALID);
        }
        YzProblemFeedbackDO feedbackUpdate = new YzProblemFeedbackDO();
        feedbackUpdate.setId(feedback.getId());
        feedbackUpdate.setStatus(status);
        feedbackMapper.updateById(feedbackUpdate);

        YzProblemStatusTaskDO currentTask = getLatestTask(feedback.getId());
        if (currentTask == null) {
            currentTask = new YzProblemStatusTaskDO();
            currentTask.setId(SNOWFLAKE.nextId());
            currentTask.setProblemFeedbackId(feedback.getId());
            currentTask.setStatus(STATUS_WAIT_AUDIT);
            currentTask.setExpedited(0);
            statusTaskMapper.insert(currentTask);
        }

        // 一条问题反馈只维护一条状态任务记录：审核/处理/核验均更新同一条记录
        YzProblemStatusTaskDO update = new YzProblemStatusTaskDO();
        update.setId(currentTask.getId());
        update.setProblemFeedbackId(feedback.getId());
        update.setStatus(status);
        update.setExpedited(0);
        update.setReviewerPersonId(reviewerId);
        update.setReviewerPersonTime(now);
        update.setAssignedPersonId(reqVO.getAssignedPersonId());
        update.setPlannedCompletionTime(reqVO.getPlannedCompletionTime());
        update.setStatusDescription(reqVO.getStatusDescription());
        update.setStatusDescriptionTime(now);
        statusTaskMapper.updateById(update);
        // 兜底：确保审核人信息一定落库（避免部分环境下更新字段丢失）
        YzProblemStatusTaskDO reviewerUpdate = new YzProblemStatusTaskDO();
        reviewerUpdate.setId(currentTask.getId());
        reviewerUpdate.setReviewerPersonId(reviewerId);
        reviewerUpdate.setReviewerPersonTime(now);
        statusTaskMapper.updateById(reviewerUpdate);

        // 受理指派阶段提前创建短链并写入手机号，便于 H5 端通过短链实现免登录跳转
        createAuditAssignShortLinks(feedback, reqVO, status);
    }

    /**
     * 受理指派阶段创建短链（携带手机号），用于短信短链免登录。
     * <p>
     * 说明：该逻辑不依赖短信是否发送，目的是先把 short_link 记录准备好。
     */
    private void createAuditAssignShortLinks(YzProblemFeedbackDO feedback, ProblemFeedbackAuditReqVO reqVO, int status) {
        if (feedback == null || feedback.getId() == null) {
            return;
        }
        String publicMobile = StrUtil.trimToNull(feedback.getPhoneNumber());
        if (status == STATUS_PROCESSING) {
            if (StrUtil.isNotBlank(publicMobile)) {
                createSmsShortLink(SHORT_LINK_SCENE_PUBLIC_DETAIL, feedback.getId(), SHORT_LINK_PUBLIC_EXPIRE_DAYS, "assign", "public", publicMobile);
            }
            Long assignedPersonId = reqVO == null ? null : reqVO.getAssignedPersonId();
            if (assignedPersonId == null) {
                return;
            }
            String handlerMobile = systemUserSimpleMapper.selectMobileByUserId(assignedPersonId);
            if (StrUtil.isNotBlank(handlerMobile)) {
                createSmsShortLink(SHORT_LINK_SCENE_HANDLER_TASK, feedback.getId(), SHORT_LINK_HANDLER_EXPIRE_DAYS, "assign", "handler", handlerMobile);
            }
            return;
        }
        if (status == STATUS_REJECTED) {
            if (StrUtil.isNotBlank(publicMobile)) {
                createSmsShortLink(SHORT_LINK_SCENE_PUBLIC_DETAIL, feedback.getId(), SHORT_LINK_PUBLIC_EXPIRE_DAYS, "reject", "public", publicMobile);
            }
        }
    }

    /**
     * 处理人处理问题
     */
    @Transactional(rollbackFor = Exception.class)
    public void processFeedback(ProblemFeedbackProcessReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
        }
        YzProblemFeedbackDO feedback = feedbackMapper.selectById(reqVO.getId());
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        YzProblemStatusTaskDO latestTask = getLatestTask(feedback.getId());
        if (latestTask == null || latestTask.getAssignedPersonId() == null
                || !Objects.equals(latestTask.getAssignedPersonId(), userId)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_NOT_ASSIGNED);
        }
        if (!Objects.equals(feedback.getStatus(), STATUS_PROCESSING)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        YzProblemFeedbackDO feedbackUpdate = new YzProblemFeedbackDO();
        feedbackUpdate.setId(feedback.getId());
        feedbackUpdate.setStatus(STATUS_PENDING_VERIFY);
        feedbackMapper.updateById(feedbackUpdate);

        // 更新同一条任务记录：写入处理信息与处理附件
        YzProblemStatusTaskDO update = new YzProblemStatusTaskDO();
        update.setId(latestTask.getId());
        update.setStatus(STATUS_PENDING_VERIFY);
        update.setAssignedPersonId(userId);
        update.setProcessingTime(now);
        String handleResult = StrUtil.trimToEmpty(reqVO.getHandleResult());
        String resolutionDescription = StrUtil.trimToEmpty(reqVO.getResolutionDescription());
        String combinedDescription = resolutionDescription;
        if (StrUtil.isNotBlank(handleResult)) {
            combinedDescription = StrUtil.format("处理结果：{}；处理描述：{}", handleResult, resolutionDescription);
        }
        update.setResolutionDescription(combinedDescription);

        List<String> files = reqVO.getUploadedFiles();
        update.setProblemHandleImages(CollUtil.isEmpty(files) ? null : files.toArray(new String[0]));
        statusTaskMapper.updateById(update);
    }

    /**
     * 管理员核验并办结/驳回
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyFeedback(ProblemFeedbackVerifyReqVO reqVO) {
        ensureAdmin();
        Long verificationId = SecurityFrameworkUtils.getLoginUserId();
        if (verificationId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
        }
        YzProblemFeedbackDO feedback = feedbackMapper.selectById(reqVO.getId());
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        if (!Objects.equals(feedback.getStatus(), STATUS_PENDING_VERIFY)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        int status = Boolean.TRUE.equals(reqVO.getSolved()) ? STATUS_FINISHED : STATUS_REJECTED;
        YzProblemFeedbackDO feedbackUpdate = new YzProblemFeedbackDO();
        feedbackUpdate.setId(feedback.getId());
        feedbackUpdate.setStatus(status);
        feedbackMapper.updateById(feedbackUpdate);

        YzProblemStatusTaskDO latestTask = getLatestTask(feedback.getId());
        if (latestTask == null) {
            latestTask = new YzProblemStatusTaskDO();
            latestTask.setId(SNOWFLAKE.nextId());
            latestTask.setProblemFeedbackId(feedback.getId());
            latestTask.setStatus(STATUS_WAIT_AUDIT);
            latestTask.setExpedited(0);
            statusTaskMapper.insert(latestTask);
        }

        // 更新同一条任务记录：写入核验信息与核验附件
        YzProblemStatusTaskDO update = new YzProblemStatusTaskDO();
        update.setId(latestTask.getId());
        update.setStatus(status);
        update.setVerificationId(verificationId);
        update.setVerificationResult(reqVO.getVerificationResult());
        List<String> files = reqVO.getUploadedFiles();
        update.setVerifyHandleImages(CollUtil.isEmpty(files) ? null : files.toArray(new String[0]));
        update.setCompletionTime(now);
        statusTaskMapper.updateById(update);
    }

    /**
     * 管理员指派后发送短信（给反馈人 + 处理人）。
     * <p>
     * 说明：短信发送为“尽力而为”，不参与业务状态的事务回滚。
     */
    public ProblemFeedbackNotifyRespVO notifyAssignSms(Long feedbackId) {
        ensureAdmin();
        ProblemFeedbackNotifyRespVO resp = new ProblemFeedbackNotifyRespVO();
        resp.setPublicSent(false);
        resp.setHandlerSent(false);

        YzProblemFeedbackDO feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        if (!Objects.equals(feedback.getStatus(), STATUS_PROCESSING)) {
            resp.setPublicReason("当前状态非“处理中”，不发送指派短信");
            resp.setHandlerReason("当前状态非“处理中”，不发送指派短信");
            return resp;
        }

        // 公众用户短信策略：仅允许登录验证码，业务通知场景一律禁用
        resp.setPublicReason("公众用户短信仅用于登录验证码，受理通知已禁用");

        // 处理人短信：新任务派遣（SMS_499260689）
        YzProblemStatusTaskDO latestTask = getLatestTask(feedbackId);
        Long assignedPersonId = latestTask == null ? null : latestTask.getAssignedPersonId();
        LocalDateTime plannedCompletionTime = latestTask == null ? null : latestTask.getPlannedCompletionTime();
        if (assignedPersonId == null) {
            resp.setHandlerReason("未指派处理人，跳过发送");
            return resp;
        }


        String handlerMobile = systemUserSimpleMapper.selectMobileByUserId(assignedPersonId);
        String handlerCode = createSmsShortLink(SHORT_LINK_SCENE_HANDLER_TASK, feedbackId, SHORT_LINK_HANDLER_EXPIRE_DAYS, "assign", "handler", handlerMobile);
        SmsSendSingleToUserReqDTO reqDTO = new SmsSendSingleToUserReqDTO();
        reqDTO.setUserId(assignedPersonId);
        reqDTO.setTemplateCode(FEIGE_TEMPLATE_HANDLER_ASSIGN);
        Map<String, Object> params = new HashMap<>();
        params.put("time", SMS_TIME_FORMATTER.format(plannedCompletionTime));
        params.put("code", handlerCode);
        reqDTO.setTemplateParams(params);
        try {
            Long logId = smsSendApi.sendSingleSmsToAdmin(reqDTO);
            resp.setHandlerSent(true);
            resp.setHandlerLogId(logId);
            if (latestTask != null && latestTask.getId() != null) {
                YzProblemStatusTaskDO update = new YzProblemStatusTaskDO();
                update.setId(latestTask.getId());
                statusTaskMapper.updateById(update);
            }
        } catch (Exception ex) {
            resp.setHandlerReason("发送失败，请检查短信模板与渠道配置");
        }
        return resp;
    }

    /**
     * 管理员办结后发送短信（给反馈人）。
     * <p>
     * 说明：短信发送为“尽力而为”，不参与业务状态的事务回滚。
     */
    public ProblemFeedbackNotifyRespVO notifyFinishSms(Long feedbackId) {
        ensureAdmin();
        ProblemFeedbackNotifyRespVO resp = new ProblemFeedbackNotifyRespVO();
        resp.setPublicSent(false);
        resp.setHandlerSent(false);

        YzProblemFeedbackDO feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        if (!Objects.equals(feedback.getStatus(), STATUS_FINISHED)) {
            resp.setPublicReason("当前状态非“已办结”，不发送办结短信");
            return resp;
        }


        // 公众用户短信策略：仅允许登录验证码，业务通知场景一律禁用
        resp.setPublicReason("公众用户短信仅用于登录验证码，办结通知已禁用");
        return resp;
    }

    /**
     * 管理员驳回后发送短信（给反馈人）。
     * <p>
     * 说明：短信发送为“尽力而为”，不参与业务状态的事务回滚。
     */
    public ProblemFeedbackNotifyRespVO notifyRejectSms(Long feedbackId) {
        ensureAdmin();
        ProblemFeedbackNotifyRespVO resp = new ProblemFeedbackNotifyRespVO();
        resp.setPublicSent(false);
        resp.setHandlerSent(false);
        resp.setHandlerReason("驳回短信仅发送给反馈人");

        YzProblemFeedbackDO feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        if (!Objects.equals(feedback.getStatus(), STATUS_REJECTED)) {
            resp.setPublicReason("当前状态非“已驳回”，不发送驳回短信");
            return resp;
        }

        // 公众用户短信策略：仅允许登录验证码，业务通知场景一律禁用
        resp.setPublicReason("公众用户短信仅用于登录验证码，驳回通知已禁用");
        return resp;
    }

    /**
     * 管理员催办超时问题发送短信（给处理人）。
     * <p>
     * 说明：短信发送为“尽力而为”，不参与业务状态的事务回滚。
     */
    public ProblemFeedbackNotifyRespVO notifyOverdueSms(Long feedbackId) {
        ensureAdmin();
        ProblemFeedbackNotifyRespVO resp = new ProblemFeedbackNotifyRespVO();
        resp.setPublicSent(false);
        resp.setHandlerSent(false);
        resp.setPublicReason("催办短信仅发送给处理人");

        YzProblemFeedbackDO feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        if (!Objects.equals(feedback.getStatus(), STATUS_PROCESSING)) {
            resp.setHandlerReason("当前状态非“处理中”，不发送催办短信");
            return resp;
        }

        YzProblemStatusTaskDO latestTask = getLatestTask(feedbackId);
        Long assignedPersonId = latestTask == null ? null : latestTask.getAssignedPersonId();
        LocalDateTime plannedCompletionTime = latestTask == null ? null : latestTask.getPlannedCompletionTime();
        if (assignedPersonId == null) {
            resp.setHandlerReason("未指派处理人，跳过发送");
            return resp;
        }
        if (plannedCompletionTime == null) {
            resp.setHandlerReason("计划完成时间为空，无法判断是否超时");
            return resp;
        }
        if (plannedCompletionTime.isAfter(LocalDateTime.now())) {
            resp.setHandlerReason("未超时，跳过发送");
            return resp;
        }

        String handlerMobile = systemUserSimpleMapper.selectMobileByUserId(assignedPersonId);
        String code = createSmsShortLink(SHORT_LINK_SCENE_HANDLER_TASK, feedbackId, SHORT_LINK_HANDLER_EXPIRE_DAYS, "overdue", "handler", handlerMobile);
        SmsSendSingleToUserReqDTO reqDTO = new SmsSendSingleToUserReqDTO();
        reqDTO.setUserId(assignedPersonId);
        reqDTO.setTemplateCode(FEIGE_TEMPLATE_HANDLER_OVERDUE);
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        reqDTO.setTemplateParams(params);
        try {
            Long logId = smsSendApi.sendSingleSmsToAdmin(reqDTO);
            resp.setHandlerSent(true);
            resp.setHandlerLogId(logId);
        } catch (Exception ex) {
            resp.setHandlerReason("发送失败，请检查短信模板与渠道配置");
        }
        return resp;
    }

    /**
     * 统计问题状态数量
     */
    public ProblemFeedbackStatusSummaryVO getStatusSummary() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (isGuest(loginUser)) {
            return emptySummary();
        }
        boolean admin = isAdmin(loginUser);
        List<Long> assignedFeedbackIds = null;
        if (!admin) {
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            if (userId == null) {
                return emptySummary();
            }
            assignedFeedbackIds = statusTaskMapper.selectList(
                            new LambdaQueryWrapper<YzProblemStatusTaskDO>()
                                    .eq(YzProblemStatusTaskDO::getAssignedPersonId, userId))
                    .stream()
                    .map(YzProblemStatusTaskDO::getProblemFeedbackId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (CollUtil.isEmpty(assignedFeedbackIds)) {
                return emptySummary();
            }
        }
        ProblemFeedbackStatusSummaryVO vo = new ProblemFeedbackStatusSummaryVO();
        vo.setTotalCount(countTotal(admin, assignedFeedbackIds));
        vo.setProcessingCount(countWithStatus(admin, assignedFeedbackIds, STATUS_PROCESSING));
        vo.setPendingVerifyCount(countWithStatus(admin, assignedFeedbackIds, STATUS_PENDING_VERIFY));
        vo.setFinishedCount(countWithStatus(admin, assignedFeedbackIds, STATUS_FINISHED));
        return vo;
    }

    /**
     * 问题反馈看板统计（本月/本季度/本年度）。
     * <p>
     * 说明：仅用于列表页展示，不改变数据库状态；已驳回的问题仅参与状态统计（statusStat），参与度仅计入反馈总次数，不参与类型占比/TOP5。
     */
    public ProblemFeedbackDashboardRespVO getDashboard(String period) {
        TimeRange range = resolvePeriodRange(period);
        List<YzProblemFeedbackDO> feedbackList = feedbackMapper.selectList(new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .select(YzProblemFeedbackDO::getId,
                        YzProblemFeedbackDO::getFeedbackType,
                        YzProblemFeedbackDO::getStatus,
                        YzProblemFeedbackDO::getRealName,
                        YzProblemFeedbackDO::getName,
                        YzProblemFeedbackDO::getPhoneNumber,
                        YzProblemFeedbackDO::getReferenceId,
                        YzProblemFeedbackDO::getReferenceType,
                        YzProblemFeedbackDO::getPublicNoticeId,
                        YzProblemFeedbackDO::getCreateTime)
                .ge(YzProblemFeedbackDO::getCreateTime, range.startTime())
                .lt(YzProblemFeedbackDO::getCreateTime, range.endExclusiveTime())
                .ne(YzProblemFeedbackDO::getStatus, STATUS_REJECTED));

        Long rejectedCount = feedbackMapper.selectCount(new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .ge(YzProblemFeedbackDO::getCreateTime, range.startTime())
                .lt(YzProblemFeedbackDO::getCreateTime, range.endExclusiveTime())
                .eq(YzProblemFeedbackDO::getStatus, STATUS_REJECTED));

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        Long todayFeedbackCount = feedbackMapper.selectCount(new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .ge(YzProblemFeedbackDO::getCreateTime, todayStart)
                .lt(YzProblemFeedbackDO::getCreateTime, tomorrowStart));

        ProblemFeedbackDashboardRespVO respVO = new ProblemFeedbackDashboardRespVO();
        long rejected = rejectedCount == null ? 0L : rejectedCount;
        ProblemFeedbackDashboardRespVO.StatusStat statusStat = buildStatusStat(feedbackList, rejected);
        respVO.setStatusStat(statusStat);
        respVO.setTypeStatList(buildTypeStatList(feedbackList));
        respVO.setTop5List(buildTop5List(feedbackList));
        ProblemFeedbackDashboardRespVO.ParticipationStat participation = buildParticipation(feedbackList);
        if (participation != null) {
            // 已驳回也计入参与度的反馈总次数
            long totalFeedbackCount = (participation.getTotalFeedbackCount() == null ? 0L : participation.getTotalFeedbackCount()) + rejected;
            participation.setTotalFeedbackCount(totalFeedbackCount);
            participation.setAnonymousRate(calcPercent(
                    participation.getAnonymousPersonCount() == null ? 0L : participation.getAnonymousPersonCount(),
                    totalFeedbackCount));
        }
        respVO.setParticipation(participation);
        respVO.setTodayFeedbackCount(todayFeedbackCount == null ? 0L : todayFeedbackCount);

        long periodTotalCount = (feedbackList == null ? 0L : feedbackList.size()) + rejected;
        long finishedCount = statusStat.getFinishedCount() == null ? 0L : statusStat.getFinishedCount();
        long solvedCount = finishedCount + rejected;
        respVO.setSolveRate(calcPercent(solvedCount, periodTotalCount));
        return respVO;
    }

    private ProblemFeedbackDashboardRespVO.StatusStat buildStatusStat(List<YzProblemFeedbackDO> feedbackList, long rejectedCount) {
        ProblemFeedbackDashboardRespVO.StatusStat vo = new ProblemFeedbackDashboardRespVO.StatusStat();
        long pending = 0;
        long processing = 0;
        long pendingVerify = 0;
        long finished = 0;
        if (CollUtil.isNotEmpty(feedbackList)) {
            for (YzProblemFeedbackDO item : feedbackList) {
                if (item == null || item.getStatus() == null) {
                    continue;
                }
                Integer status = item.getStatus();
                if (status == STATUS_WAIT_AUDIT) {
                    pending++;
                } else if (status == STATUS_PROCESSING) {
                    processing++;
                } else if (status == STATUS_PENDING_VERIFY) {
                    pendingVerify++;
                } else if (status == STATUS_FINISHED) {
                    finished++;
                }
            }
        }
        vo.setPendingCount(pending);
        vo.setRejectedCount(rejectedCount);
        vo.setProcessingCount(processing);
        vo.setPendingVerifyCount(pendingVerify);
        vo.setFinishedCount(finished);
        return vo;
    }

    private List<ProblemFeedbackDashboardRespVO.TypeStat> buildTypeStatList(List<YzProblemFeedbackDO> feedbackList) {
        if (CollUtil.isEmpty(feedbackList)) {
            return Collections.emptyList();
        }
        List<DictDataRespDTO> dictList = dictDataApi.getDictDataList(ZdConstants.ZD_FKLX);
        if (CollUtil.isEmpty(dictList)) {
            return Collections.emptyList();
        }
        Map<String, String> labelMap = dictList.stream()
                .filter(item -> ObjUtil.isNotEmpty(item.getValue()) && CommonStatusEnum.isEnable(item.getStatus()))
                .collect(Collectors.toMap(DictDataRespDTO::getValue, DictDataRespDTO::getLabel, (a, b) -> a));
        if (labelMap.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Long> countMap = new HashMap<>();
        for (YzProblemFeedbackDO item : feedbackList) {
            if (item == null) {
                continue;
            }
            String type = trimToNull(item.getFeedbackType());
            if (type == null) {
                continue;
            }
            // 仅统计字典表中状态为启用的数据
            if (!labelMap.containsKey(type)) {
                continue;
            }
            countMap.merge(type, 1L, Long::sum);
        }
        if (countMap.isEmpty()) {
            return Collections.emptyList();
        }
        return countMap.entrySet().stream()
                .map(e -> {
                    ProblemFeedbackDashboardRespVO.TypeStat vo = new ProblemFeedbackDashboardRespVO.TypeStat();
                    vo.setFeedbackType(e.getKey());
                    vo.setFeedbackTypeLabel(StrUtil.blankToDefault(labelMap.get(e.getKey()), e.getKey()));
                    vo.setCount(e.getValue());
                    return vo;
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    private List<ProblemFeedbackDashboardRespVO.TopStat> buildTop5List(List<YzProblemFeedbackDO> feedbackList) {
        if (CollUtil.isEmpty(feedbackList)) {
            return Collections.emptyList();
        }
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(feedbackList);
        if (referenceInfoMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Long> countMap = new HashMap<>();
        Map<String, String> nameMap = new HashMap<>();
        Map<String, LocalDateTime> createTimeMap = loadReferenceCreateTimeMap(referenceInfoMap);

        for (YzProblemFeedbackDO item : feedbackList) {
            if (item == null) {
                continue;
            }
            ReferenceInfo ref = referenceInfoMap.get(item.getId());
            String referenceType = ref != null ? ref.referenceType() : normalizeReferenceType(item.getReferenceType());
            Long referenceId = ref != null ? ref.referenceId() : item.getReferenceId();
            if (referenceId == null || StrUtil.isBlank(referenceType)) {
                continue;
            }
            String key = referenceType + ":" + referenceId;
            countMap.merge(key, 1L, Long::sum);
            String referenceName = ref != null ? ref.referenceName() : null;
            if (StrUtil.isNotBlank(referenceName)) {
                nameMap.putIfAbsent(key, referenceName);
            }
        }
        if (countMap.isEmpty()) {
            return Collections.emptyList();
        }

        return countMap.entrySet().stream()
                .map(e -> {
                    String key = e.getKey();
                    int idx = key.indexOf(':');
                    String type = idx > 0 ? key.substring(0, idx) : key;
                    Long refId = idx > 0 ? parseLongOrNull(key.substring(idx + 1)) : null;
                    ProblemFeedbackDashboardRespVO.TopStat vo = new ProblemFeedbackDashboardRespVO.TopStat();
                    vo.setReferenceType(type);
                    vo.setReferenceId(refId);
                    vo.setReferenceName(nameMap.getOrDefault(key, ""));
                    vo.setCount(e.getValue());
                    return vo;
                })
                .sorted((a, b) -> {
                    int cmp = Long.compare(b.getCount(), a.getCount());
                    if (cmp != 0) return cmp;
                    LocalDateTime at = createTimeMap.get(a.getReferenceType() + ":" + a.getReferenceId());
                    LocalDateTime bt = createTimeMap.get(b.getReferenceType() + ":" + b.getReferenceId());
                    LocalDateTime av = at == null ? LocalDateTime.MIN : at;
                    LocalDateTime bv = bt == null ? LocalDateTime.MIN : bt;
                    return bv.compareTo(av);
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    private Map<String, LocalDateTime> loadReferenceCreateTimeMap(Map<Long, ReferenceInfo> referenceInfoMap) {
        if (referenceInfoMap == null || referenceInfoMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> channelIds = new HashSet<>();
        Set<Long> sectionIds = new HashSet<>();
        Set<Long> reservoirIds = new HashSet<>();
        for (ReferenceInfo ref : referenceInfoMap.values()) {
            if (ref == null || ref.referenceId() == null || StrUtil.isBlank(ref.referenceType())) {
                continue;
            }
            if (StrUtil.equals(ref.referenceType(), ReferenceTypeConstants.RIVER)) {
                channelIds.add(ref.referenceId());
            } else if (StrUtil.equals(ref.referenceType(), ReferenceTypeConstants.RIVER_SECTION)) {
                sectionIds.add(ref.referenceId());
            } else if (StrUtil.equals(ref.referenceType(), ReferenceTypeConstants.RESERVOIR)) {
                reservoirIds.add(ref.referenceId());
            }
        }

        Map<String, LocalDateTime> result = new HashMap<>();
        if (!channelIds.isEmpty()) {
            for (YzRiverChannelDO item : riverChannelMapper.selectBatchIds(channelIds)) {
                if (item != null && item.getId() != null) {
                    result.put(ReferenceTypeConstants.RIVER + ":" + item.getId(), item.getCreateTime());
                }
            }
        }
        if (!sectionIds.isEmpty()) {
            for (YzRiverSectionDO item : riverSectionMapper.selectBatchIds(sectionIds)) {
                if (item != null && item.getId() != null) {
                    result.put(ReferenceTypeConstants.RIVER_SECTION + ":" + item.getId(), item.getCreateTime());
                }
            }
        }
        if (!reservoirIds.isEmpty()) {
            for (YzWaterReservoirDO item : waterReservoirMapper.selectBatchIds(reservoirIds)) {
                if (item != null && item.getId() != null) {
                    result.put(ReferenceTypeConstants.RESERVOIR + ":" + item.getId(), item.getCreateTime());
                }
            }
        }
        return result;
    }

    private Long parseLongOrNull(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return Long.parseLong(StrUtil.trim(text));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ProblemFeedbackDashboardRespVO.ParticipationStat buildParticipation(List<YzProblemFeedbackDO> feedbackList) {
        ProblemFeedbackDashboardRespVO.ParticipationStat vo = new ProblemFeedbackDashboardRespVO.ParticipationStat();
        if (CollUtil.isEmpty(feedbackList)) {
            vo.setTotalFeedbackCount(0L);
            vo.setAnonymousPersonCount(0L);
            vo.setRealPersonCount(0L);
            vo.setFeedbackPersonCount(0L);
            vo.setRealNameRate(0);
            vo.setAnonymousRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return vo;
        }

        long totalFeedbackCount = feedbackList.size();
        long anonymous = 0;
        Set<String> realPersonKeys = new HashSet<>();
        for (YzProblemFeedbackDO item : feedbackList) {
            if (item == null) {
                continue;
            }
            if (Boolean.TRUE.equals(item.getRealName())) {
                String name = StrUtil.blankToDefault(item.getName(), "");
                String phone = StrUtil.blankToDefault(item.getPhoneNumber(), "");
                realPersonKeys.add(StrUtil.trim(name) + "|" + StrUtil.trim(phone));
            } else {
                anonymous++;
            }
        }
        long real = realPersonKeys.size();
        long personCount = anonymous + real;
        int realNameRate = personCount == 0 ? 0 : (int) Math.round(real * 100.0 / personCount);

        vo.setTotalFeedbackCount(totalFeedbackCount);
        vo.setAnonymousPersonCount(anonymous);
        vo.setRealPersonCount(real);
        vo.setFeedbackPersonCount(personCount);
        vo.setRealNameRate(realNameRate);
        vo.setAnonymousRate(calcPercent(anonymous, totalFeedbackCount));
        return vo;
    }

    private ProblemFeedbackDashboardRespVO emptyDashboard() {
        ProblemFeedbackDashboardRespVO respVO = new ProblemFeedbackDashboardRespVO();
        respVO.setTodayFeedbackCount(0L);
        respVO.setSolveRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        respVO.setStatusStat(buildStatusStat(Collections.emptyList(), 0L));
        respVO.setTypeStatList(Collections.emptyList());
        respVO.setTop5List(Collections.emptyList());
        respVO.setParticipation(buildParticipation(Collections.emptyList()));
        return respVO;
    }

    private BigDecimal calcPercent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private record TimeRange(LocalDateTime startTime, LocalDateTime endExclusiveTime) {
    }

    private TimeRange resolvePeriodRange(String period) {
        String value = StrUtil.blankToDefault(period, PERIOD_MONTH);
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate endExclusive;
        if (StrUtil.equalsIgnoreCase(value, PERIOD_YEAR)) {
            start = LocalDate.of(today.getYear(), 1, 1);
            endExclusive = start.plusYears(1);
        } else if (StrUtil.equalsIgnoreCase(value, PERIOD_QUARTER)) {
            int month = today.getMonthValue();
            int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
            start = LocalDate.of(today.getYear(), quarterStartMonth, 1);
            endExclusive = start.plusMonths(3);
        } else {
            start = today.withDayOfMonth(1);
            endExclusive = start.plusMonths(1);
        }
        return new TimeRange(start.atStartOfDay(), endExclusive.atStartOfDay());
    }

    /**
     * 查询问题详情
     */
    public ProblemFeedbackDetailRespVO getDetail(Long id) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (isGuest(loginUser)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
        }
        YzProblemFeedbackDO feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        if (!canAccessFeedback(loginUser, feedback.getId())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
        }
        ProblemFeedbackDetailRespVO vo = buildDetailResp(feedback);
        adjustPendingStatusLabelForAssignee(loginUser, vo);
        return vo;
    }

    /**
     * 大屏查询问题详情（不校验登录与角色）。
     */
    public ProblemFeedbackDetailRespVO getDetailForScreen(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("问题反馈ID不能为空");
        }
        YzProblemFeedbackDO feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        return buildDetailResp(feedback);
    }

    /**
     * 手机端查询我提出的问题详情
     */
    public ProblemFeedbackDetailRespVO getMyDetail(Long id) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
        }

        YzProblemFeedbackDO feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }

        // 手机端“我的问题”仅允许查看自己创建的记录；管理员放行用于排查
        if (!isAdmin(loginUser)) {
            String creator = feedback.getCreator();
            if (creator == null || !creator.equals(String.valueOf(userId))) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
            }
        }
        ProblemFeedbackDetailRespVO vo = buildDetailResp(feedback);
        adjustPendingStatusLabelForAssignee(loginUser, vo);
        return vo;
    }

    /**
     * 查询可指派的用户列表（排除纯游客，按用户更新时间倒序）
     */
    public List<ProblemFeedbackUserSimpleRespVO> getNormalUsers(Long feedbackId) {
        // 旧逻辑（按河长关联与问题点距离优先排序）已废弃，不再用于指派人下拉。
        List<ProblemFeedbackUserSimpleRespVO> users = systemUserSimpleMapper.selectNormalUsersExcludeAdminAndGuest();
        if (CollUtil.isEmpty(users)) {
            return Collections.emptyList();
        }
        List<Long> userIds = users.stream()
                .filter(Objects::nonNull)
                .map(ProblemFeedbackUserSimpleRespVO::getId)
                .filter(Objects::nonNull)
                .toList();
        if (CollUtil.isEmpty(userIds)) {
            return users;
        }
        Map<Long, String> roleNameMap = loadRoleNameMap(userIds);
        for (ProblemFeedbackUserSimpleRespVO user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }
            user.setRoleNames(roleNameMap.get(user.getId()));
        }
        return users;
    }

    private Map<Long, String> loadRoleNameMap(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<SystemUserRoleRow> rows = systemUserSimpleMapper.selectUserRoleRowsByUserIds(userIds);
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyMap();
        }
        Map<Long, List<SystemUserRoleRow>> grouped = rows.stream()
                .filter(row -> row != null && row.getUserId() != null)
                .collect(Collectors.groupingBy(SystemUserRoleRow::getUserId));
        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Long, List<SystemUserRoleRow>> entry : grouped.entrySet()) {
            List<SystemUserRoleRow> roleRows = entry.getValue();
            if (CollUtil.isEmpty(roleRows)) {
                continue;
            }
            List<String> names = roleRows.stream()
                    .filter(row -> StrUtil.isNotBlank(row.getRoleName()))
                    .sorted(Comparator
                            .comparing((SystemUserRoleRow row) -> row.getRoleSort() == null ? Integer.MAX_VALUE : row.getRoleSort())
                            .thenComparing(row -> row.getRoleId() == null ? Long.MAX_VALUE : row.getRoleId()))
                    .map(row -> StrUtil.trimToEmpty(row.getRoleName()))
                    .distinct()
                    .toList();
            if (names.isEmpty()) {
                continue;
            }
            result.put(entry.getKey(), String.join("、", names));
        }
        return result;
    }

    private Point buildProblemPoint(Long feedbackId) {
        if (feedbackId == null) {
            return null;
        }
        YzProblemFeedbackDO feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        BigDecimal longitude = feedback.getLongitude();
        BigDecimal latitude = feedback.getLatitude();
        if (longitude == null || latitude == null) {
            return null;
        }
        double lng = longitude.doubleValue();
        double lat = latitude.doubleValue();
        if (!Double.isFinite(lng) || !Double.isFinite(lat)) {
            return null;
        }
        if (Math.abs(lng) > 180 || Math.abs(lat) > 90) {
            return null;
        }
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4490);
        return point;
    }

    private DistanceContext buildDistanceContext(List<YzRiverChannelManagementDO> records) {
        Map<Long, Geometry> areaGeomMap = loadAreaGeomMap(records);
        Map<String, Geometry> referenceGeomMap = loadReferenceGeomMap(records);
        return new DistanceContext(areaGeomMap, referenceGeomMap);
    }

    private Map<Long, Geometry> loadAreaGeomMap(List<YzRiverChannelManagementDO> records) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> areaIds = new HashSet<>();
        for (YzRiverChannelManagementDO record : records) {
            areaIds.addAll(parseAreaIds(record == null ? null : record.getAdministrativeRegion()));
        }
        if (areaIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectByIdsWithGemo(new ArrayList<>(areaIds));
        if (CollUtil.isEmpty(areas)) {
            return Collections.emptyMap();
        }
        Map<Long, Geometry> result = new HashMap<>();
        WKTReader reader = new WKTReader();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null || StrUtil.isBlank(area.getGemo())) {
                continue;
            }
            try {
                Geometry geometry = reader.read(area.getGemo());
                geometry.setSRID(4490);
                result.put(area.getId(), geometry);
            } catch (Exception ex) {
                // 忽略非法几何数据
            }
        }
        return result;
    }

    private Map<String, Geometry> loadReferenceGeomMap(List<YzRiverChannelManagementDO> records) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> riverIds = new HashSet<>();
        Set<Long> sectionIds = new HashSet<>();
        Set<Long> reservoirIds = new HashSet<>();
        for (YzRiverChannelManagementDO record : records) {
            if (record == null) {
                continue;
            }
            if (CollUtil.isNotEmpty(parseAreaIds(record.getAdministrativeRegion()))) {
                continue;
            }
            ResolvedReference resolved = resolveReference(record);
            if (resolved.referenceId == null || StrUtil.isBlank(resolved.referenceType)) {
                continue;
            }
            if (ReferenceTypeConstants.RIVER.equals(resolved.referenceType)) {
                riverIds.add(resolved.referenceId);
            } else if (ReferenceTypeConstants.RIVER_SECTION.equals(resolved.referenceType)) {
                sectionIds.add(resolved.referenceId);
            } else if (ReferenceTypeConstants.RESERVOIR.equals(resolved.referenceType)) {
                reservoirIds.add(resolved.referenceId);
            }
        }

        Map<Long, Long> riverFacilityMap = new HashMap<>();
        if (!riverIds.isEmpty()) {
            for (YzRiverChannelDO river : riverChannelMapper.selectBatchIds(riverIds)) {
                if (river != null && river.getId() != null && river.getFacilityId() != null) {
                    riverFacilityMap.put(river.getId(), river.getFacilityId());
                }
            }
        }
        Map<Long, Long> sectionFacilityMap = new HashMap<>();
        if (!sectionIds.isEmpty()) {
            for (YzRiverSectionDO section : riverSectionMapper.selectBatchIds(sectionIds)) {
                if (section != null && section.getId() != null && section.getFacilityId() != null) {
                    sectionFacilityMap.put(section.getId(), section.getFacilityId());
                }
            }
        }
        Map<Long, Long> reservoirFacilityMap = new HashMap<>();
        if (!reservoirIds.isEmpty()) {
            for (YzWaterReservoirDO reservoir : waterReservoirMapper.selectBatchIds(reservoirIds)) {
                if (reservoir != null && reservoir.getId() != null && reservoir.getFacilityId() != null) {
                    reservoirFacilityMap.put(reservoir.getId(), reservoir.getFacilityId());
                }
            }
        }

        Set<Long> facilityIds = new HashSet<>();
        facilityIds.addAll(riverFacilityMap.values());
        facilityIds.addAll(sectionFacilityMap.values());
        facilityIds.addAll(reservoirFacilityMap.values());
        if (facilityIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Geometry> facilityGeomMap = new HashMap<>();
        List<YzWaterFacilityBaseDO> bases = facilityBaseMapper.selectBatchIds(facilityIds);
        for (YzWaterFacilityBaseDO base : bases) {
            if (base == null || base.getId() == null || base.getGeom() == null) {
                continue;
            }
            facilityGeomMap.put(base.getId(), base.getGeom());
        }

        Map<String, Geometry> result = new HashMap<>();
        riverFacilityMap.forEach((refId, facilityId) -> {
            Geometry geometry = facilityGeomMap.get(facilityId);
            if (geometry != null) {
                result.put(ResolvedReference.key(ReferenceTypeConstants.RIVER, refId), geometry);
            }
        });
        sectionFacilityMap.forEach((refId, facilityId) -> {
            Geometry geometry = facilityGeomMap.get(facilityId);
            if (geometry != null) {
                result.put(ResolvedReference.key(ReferenceTypeConstants.RIVER_SECTION, refId), geometry);
            }
        });
        reservoirFacilityMap.forEach((refId, facilityId) -> {
            Geometry geometry = facilityGeomMap.get(facilityId);
            if (geometry != null) {
                result.put(ResolvedReference.key(ReferenceTypeConstants.RESERVOIR, refId), geometry);
            }
        });
        return result;
    }

    private DistanceResult resolveNearestRecord(Point point,
                                                List<YzRiverChannelManagementDO> records,
                                                DistanceContext context) {
        if (point == null || CollUtil.isEmpty(records)) {
            return null;
        }
        double min = Double.MAX_VALUE;
        YzRiverChannelManagementDO nearest = null;
        for (YzRiverChannelManagementDO record : records) {
            Double distance = resolveRecordDistance(point, record, context);
            if (distance == null) {
                continue;
            }
            if (distance < min) {
                min = distance;
                nearest = record;
            }
        }
        if (nearest == null) {
            return null;
        }
        return new DistanceResult(nearest, min);
    }

    private Double resolveRecordDistance(Point point,
                                         YzRiverChannelManagementDO record,
                                         DistanceContext context) {
        if (point == null || record == null || context == null) {
            return null;
        }
        List<Long> areaIds = parseAreaIds(record.getAdministrativeRegion());
        if (!areaIds.isEmpty()) {
            Double min = null;
            for (Long areaId : areaIds) {
                Geometry geometry = context.areaGeomMap.get(areaId);
                Double distance = calcDistance(point, geometry);
                if (distance == null) {
                    continue;
                }
                if (min == null || distance < min) {
                    min = distance;
                }
            }
            return min;
        }
        ResolvedReference resolved = resolveReference(record);
        if (resolved.referenceId == null || StrUtil.isBlank(resolved.referenceType)) {
            return null;
        }
        Geometry geometry = context.referenceGeomMap.get(ResolvedReference.key(resolved.referenceType, resolved.referenceId));
        return calcDistance(point, geometry);
    }

    private Double calcDistance(Point point, Geometry geometry) {
        if (point == null || geometry == null || geometry.isEmpty()) {
            return null;
        }
        return point.distance(geometry);
    }

    private List<Long> parseAreaIds(String[] administrativeRegion) {
        if (administrativeRegion == null || administrativeRegion.length == 0) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>();
        for (String item : administrativeRegion) {
            Long id = parseLongOrNull(item);
            if (id != null) {
                result.add(id);
            }
        }
        return result;
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

    private record DistanceContext(Map<Long, Geometry> areaGeomMap, Map<String, Geometry> referenceGeomMap) {
    }

    private record DistanceResult(YzRiverChannelManagementDO record, double distance) {
    }

    private record ResolvedReference(String referenceType, Long referenceId) {
        private static String key(String referenceType, Long referenceId) {
            return String.format("%s:%s",
                    StrUtil.blankToDefault(referenceType, ""),
                    referenceId == null ? "" : referenceId);
        }
    }

    private record ResolutionParseResult(String description, Boolean noNeedHandle) {
    }

    private ResolutionParseResult parseResolutionDescription(String resolutionDescription) {
        if (StrUtil.isBlank(resolutionDescription)) {
            return new ResolutionParseResult(resolutionDescription, Boolean.FALSE);
        }
        String text = StrUtil.trim(resolutionDescription);
        Matcher matcher = RESOLUTION_DESCRIPTION_PATTERN.matcher(text);
        if (!matcher.find()) {
            return new ResolutionParseResult(resolutionDescription, Boolean.FALSE);
        }
        String handleResult = StrUtil.trimToEmpty(matcher.group(1));
        String description = StrUtil.trimToEmpty(matcher.group(2));
        boolean noNeedHandle = "无需处理".equals(handleResult);
        return new ResolutionParseResult(description, noNeedHandle);
    }

    private ProblemFeedbackDetailRespVO buildDetailResp(YzProblemFeedbackDO feedback) {
        Map<String, String> feedbackTypeLabelMap = loadDictLabel(ZdConstants.ZD_FKLX);
        Map<String, String> progressLabelMap = loadDictLabel(ZdConstants.ZD_WTJD);
        ProblemFeedbackDetailRespVO vo = BeanUtils.toBean(feedback, ProblemFeedbackDetailRespVO.class);
        vo.setUploadedFiles(feedback.getUploadedFiles() == null ? Collections.emptyList() : List.of(feedback.getUploadedFiles()));
        vo.setFeedbackTypeLabel(feedbackTypeLabelMap.get(feedback.getFeedbackType()));
        vo.setStatusLabel(resolveProgressLabel(feedback.getStatus(), progressLabelMap));
        vo.setFeedbackPerson(resolveFeedbackPerson(feedback));
        vo.setProblemLatitude(feedback.getLatitude());
        vo.setProblemLongitude(feedback.getLongitude());
        vo.setIssueSpecificLocation(feedback.getIssueSpecificLocation());
        Map<Long, ReferenceInfo> referenceInfoMap = loadReferenceInfoMap(List.of(feedback));
        ReferenceInfo ref = referenceInfoMap.get(feedback.getId());
        String referenceType = ref != null ? ref.referenceType() : normalizeReferenceType(feedback.getReferenceType());
        Long referenceId = ref != null ? ref.referenceId() : feedback.getReferenceId();
        String referenceName = ref != null ? ref.referenceName() : null;
        String facilityCode = ref != null ? ref.facilityCode() : null;
        vo.setReferenceType(referenceType);
        vo.setReferenceId(referenceId);
        vo.setReferenceName(referenceName);
        vo.setFacilityCode(facilityCode);
        vo.setFacilityName(referenceName);
        // 兼容旧字段：逐步用 facilityCode/facilityName 替代 riverCode/riverName
        vo.setRiverCode(facilityCode);
        vo.setRiverName(referenceName);
        vo.setLongitude(null);
        vo.setLatitude(null);
        vo.setSpecificLocation(null);
        // 一条反馈只对应一条状态任务记录，详情仅返回最新一条（历史多条数据不再返回）
        YzProblemStatusTaskDO task = getLatestTask(feedback.getId());
        List<YzProblemStatusTaskDO> taskList = task == null ? Collections.emptyList() : List.of(task);
        Map<Long, String> nicknameMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(taskList)) {
            Set<Long> userIds = new HashSet<>();
            for (YzProblemStatusTaskDO item : taskList) {
                if (item == null) {
                    continue;
                }
                if (item.getAssignedPersonId() != null) {
                    userIds.add(item.getAssignedPersonId());
                }
                if (item.getReviewerPersonId() != null) {
                    userIds.add(item.getReviewerPersonId());
                }
                if (item.getVerificationId() != null) {
                    userIds.add(item.getVerificationId());
                }
            }
            if (CollUtil.isNotEmpty(userIds)) {
                nicknameMap = systemUserSimpleMapper.selectUsersByIds(userIds).stream()
                        .filter(it -> it != null && it.getId() != null)
                        .collect(Collectors.toMap(ProblemFeedbackUserSimpleRespVO::getId, ProblemFeedbackUserSimpleRespVO::getNickname, (a, b) -> a));
            }
        }
        Map<Long, String> finalNicknameMap = nicknameMap;
        List<ProblemStatusTaskRespVO> taskVOList = taskList.stream().map(t -> {
            ProblemStatusTaskRespVO item = BeanUtils.toBean(t, ProblemStatusTaskRespVO.class);
            ResolutionParseResult resolutionResult = parseResolutionDescription(item.getResolutionDescription());
            item.setResolutionDescription(resolutionResult.description());
            item.setNoNeedHandle(resolutionResult.noNeedHandle());
            item.setUploadedFiles(t.getUploadedFiles() == null ? Collections.emptyList() : List.of(t.getUploadedFiles()));
            item.setProblemHandleImages(t.getProblemHandleImages() == null ? Collections.emptyList() : List.of(t.getProblemHandleImages()));
            item.setVerifyHandleImages(t.getVerifyHandleImages() == null ? Collections.emptyList() : List.of(t.getVerifyHandleImages()));
            item.setStatusLabel(resolveProgressLabel(t.getStatus(), progressLabelMap));
            if (t.getAssignedPersonId() != null) {
                item.setAssignedPersonName(finalNicknameMap.get(t.getAssignedPersonId()));
            }
            if (t.getReviewerPersonId() != null) {
                item.setReviewerPersonName(finalNicknameMap.get(t.getReviewerPersonId()));
            }
            if (t.getVerificationId() != null) {
                item.setVerificationName(finalNicknameMap.get(t.getVerificationId()));
            }
            // completionTime 即为核验办结时间，这里同步填充给核验时间字段，方便前端展示
            item.setVerificationTime(t.getCompletionTime());
            return item;
        }).collect(Collectors.toList());
        vo.setStatusTasks(taskVOList);
        return vo;
    }

    /**
     * 动态状态文案：当“当前登录用户”为处理人，且问题状态为“处理中(2)”时，对外展示为“待处理”。
     * <p>
     * 说明：仅影响返回 VO 的展示文案，不改变数据库状态。
     */
    private void adjustPendingStatusLabelForAssignee(LoginUser loginUser, ProblemFeedbackDetailRespVO vo) {
        if (loginUser == null || loginUser.getId() == null || vo == null) {
            return;
        }
        if (!Objects.equals(vo.getStatus(), STATUS_PROCESSING)) {
            return;
        }
        List<ProblemStatusTaskRespVO> tasks = vo.getStatusTasks();
        if (CollUtil.isEmpty(tasks)) {
            return;
        }
        ProblemStatusTaskRespVO task = tasks.get(0);
        if (task == null || task.getAssignedPersonId() == null) {
            return;
        }
        if (!Objects.equals(task.getAssignedPersonId(), loginUser.getId())) {
            return;
        }
        vo.setStatusLabel("待处理");
        // 同步任务节点文案，避免前端同时展示两套状态文案
        if (Objects.equals(task.getStatus(), STATUS_PROCESSING)) {
            task.setStatusLabel("待处理");
        }
    }

    private ProblemFeedbackPageRespVO buildResp(YzProblemFeedbackDO item,
                                               Map<Long, ReferenceInfo> referenceInfoMap,
                                               YzProblemStatusTaskDO latestTask,
                                               Map<Long, String> nicknameMap,
                                               Map<String, String> feedbackTypeLabelMap,
                                               Map<String, String> progressLabelMap) {
        ProblemFeedbackPageRespVO vo = BeanUtils.toBean(item, ProblemFeedbackPageRespVO.class);
        vo.setUploadedFiles(item.getUploadedFiles() == null ? Collections.emptyList() : List.of(item.getUploadedFiles()));
        vo.setFeedbackTypeLabel(feedbackTypeLabelMap.get(item.getFeedbackType()));
        vo.setStatusLabel(resolveProgressLabel(item.getStatus(), progressLabelMap));
        vo.setFeedbackPerson(resolveFeedbackPerson(item));
        vo.setProblemLatitude(item.getLatitude());
        vo.setProblemLongitude(item.getLongitude());
        vo.setIssueSpecificLocation(item.getIssueSpecificLocation());
        if (latestTask != null && latestTask.getAssignedPersonId() != null) {
            vo.setAssignedPersonId(latestTask.getAssignedPersonId());
            vo.setAssignedPersonName(nicknameMap.get(latestTask.getAssignedPersonId()));
            vo.setPlannedCompletionTime(latestTask.getPlannedCompletionTime());
        }
        if (latestTask != null) {
            vo.setExpedited(latestTask.getExpedited());
        }
        ReferenceInfo ref = referenceInfoMap == null ? null : referenceInfoMap.get(item.getId());
        String referenceType = ref != null ? ref.referenceType() : normalizeReferenceType(item.getReferenceType());
        Long referenceId = ref != null ? ref.referenceId() : item.getReferenceId();
        String referenceName = ref != null ? ref.referenceName() : null;
        String facilityCode = ref != null ? ref.facilityCode() : null;
        vo.setReferenceType(referenceType);
        vo.setReferenceId(referenceId);
        vo.setReferenceName(referenceName);
        vo.setFacilityCode(facilityCode);
        vo.setFacilityName(referenceName);
        // 兼容旧字段：逐步用 facilityCode/facilityName 替代 riverCode/riverName
        vo.setRiverCode(facilityCode);
        vo.setRiverName(referenceName);
        vo.setLongitude(null);
        vo.setLatitude(null);
        return vo;
    }

    /**
     * 加载问题反馈关联对象信息映射：key 为反馈ID，value 为关联对象信息（类型、名称、设施编码）。
     */
    private Map<Long, ReferenceInfo> loadReferenceInfoMap(List<YzProblemFeedbackDO> feedbackList) {
        if (CollUtil.isEmpty(feedbackList)) {
            return Collections.emptyMap();
        }
        Map<Long, ResolvedReference> resolvedMap = new HashMap<>();
        Set<Long> channelIds = new HashSet<>();
        Set<Long> sectionIds = new HashSet<>();
        Set<Long> reservoirIds = new HashSet<>();
        for (YzProblemFeedbackDO feedback : feedbackList) {
            if (feedback == null || feedback.getId() == null) {
                continue;
            }
            String referenceType = normalizeReferenceType(feedback.getReferenceType());
            Long referenceId = feedback.getReferenceId();
            if (referenceId == null || StrUtil.isBlank(referenceType)) {
                continue;
            }
            resolvedMap.put(feedback.getId(), new ResolvedReference(referenceType, referenceId));
            if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
                channelIds.add(referenceId);
            } else if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
                sectionIds.add(referenceId);
            } else if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
                reservoirIds.add(referenceId);
            }
        }
        if (resolvedMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, YzRiverChannelDO> channelMap = new HashMap<>();
        if (!channelIds.isEmpty()) {
            List<YzRiverChannelDO> list = riverChannelMapper.selectBatchIds(channelIds);
            for (YzRiverChannelDO item : list) {
                if (item != null && item.getId() != null) {
                    channelMap.put(item.getId(), item);
                }
            }
        }
        Map<Long, YzRiverChannelBfDO> channelBfMap = new HashMap<>();
        if (!channelIds.isEmpty()) {
            Set<Long> missingChannels = new HashSet<>(channelIds);
            missingChannels.removeAll(channelMap.keySet());
            if (!missingChannels.isEmpty()) {
                for (YzRiverChannelBfDO item : riverChannelBfMapper.selectBatchIds(missingChannels)) {
                    if (item != null && item.getId() != null) {
                        channelBfMap.put(item.getId(), item);
                    }
                }
            }
        }

        Map<Long, YzRiverSectionDO> sectionMap = new HashMap<>();
        Set<Long> sectionFacilityIds = new HashSet<>();
        if (!sectionIds.isEmpty()) {
            List<YzRiverSectionDO> list = riverSectionMapper.selectBatchIds(sectionIds);
            for (YzRiverSectionDO item : list) {
                if (item != null && item.getId() != null) {
                    sectionMap.put(item.getId(), item);
                    if (item.getFacilityId() != null) {
                        sectionFacilityIds.add(item.getFacilityId());
                    }
                }
            }
        }
        Map<Long, YzRiverSectionBfDO> sectionBfMap = new HashMap<>();
        Set<Long> sectionBfFacilityIds = new HashSet<>();
        if (!sectionIds.isEmpty()) {
            Set<Long> missingSections = new HashSet<>(sectionIds);
            missingSections.removeAll(sectionMap.keySet());
            if (!missingSections.isEmpty()) {
                for (YzRiverSectionBfDO item : riverSectionBfMapper.selectBatchIds(missingSections)) {
                    if (item != null && item.getId() != null) {
                        sectionBfMap.put(item.getId(), item);
                        if (item.getFacilityId() != null) {
                            sectionBfFacilityIds.add(item.getFacilityId());
                        }
                    }
                }
            }
        }

        Map<Long, String> sectionFacilityCodeMap = new HashMap<>();
        if (!sectionFacilityIds.isEmpty()) {
            List<YzWaterFacilityBaseDO> list = facilityBaseMapper.selectBatchIds(sectionFacilityIds);
            for (YzWaterFacilityBaseDO base : list) {
                if (base != null && base.getId() != null) {
                    sectionFacilityCodeMap.put(base.getId(), base.getFacilityCode());
                }
            }
        }
        Map<Long, String> sectionBfFacilityCodeMap = new HashMap<>();
        if (!sectionBfFacilityIds.isEmpty()) {
            for (YzWaterFacilityBaseBfDO base : facilityBaseBfMapper.selectBatchIds(sectionBfFacilityIds)) {
                if (base != null && base.getId() != null) {
                    sectionBfFacilityCodeMap.put(base.getId(), base.getFacilityCode());
                }
            }
        }

        Map<Long, YzWaterReservoirDO> reservoirMap = new HashMap<>();
        if (!reservoirIds.isEmpty()) {
            List<YzWaterReservoirDO> list = waterReservoirMapper.selectBatchIds(reservoirIds);
            for (YzWaterReservoirDO item : list) {
                if (item != null && item.getId() != null) {
                    reservoirMap.put(item.getId(), item);
                }
            }
        }
        Map<Long, YzWaterReservoirBfDO> reservoirBfMap = new HashMap<>();
        if (!reservoirIds.isEmpty()) {
            Set<Long> missingReservoirs = new HashSet<>(reservoirIds);
            missingReservoirs.removeAll(reservoirMap.keySet());
            if (!missingReservoirs.isEmpty()) {
                for (YzWaterReservoirBfDO item : waterReservoirBfMapper.selectBatchIds(missingReservoirs)) {
                    if (item != null && item.getId() != null) {
                        reservoirBfMap.put(item.getId(), item);
                    }
                }
            }
        }

        Map<Long, ReferenceInfo> result = new HashMap<>();
        for (Map.Entry<Long, ResolvedReference> entry : resolvedMap.entrySet()) {
            Long feedbackId = entry.getKey();
            ResolvedReference ref = entry.getValue();
            if (feedbackId == null || ref == null) {
                continue;
            }
            String referenceType = ref.referenceType;
            Long referenceId = ref.referenceId;
            String referenceName = null;
            String facilityCode = null;

            if (ReferenceTypeConstants.RIVER.equals(referenceType)) {
                YzRiverChannelDO channel = channelMap.get(referenceId);
                if (channel != null) {
                    referenceName = channel.getRiverName();
                    facilityCode = channel.getRiverCode();
                } else {
                    YzRiverChannelBfDO bf = channelBfMap.get(referenceId);
                    if (bf != null) {
                        referenceName = bf.getRiverName();
                        facilityCode = bf.getRiverCode();
                    }
                }
            } else if (ReferenceTypeConstants.RIVER_SECTION.equals(referenceType)) {
                YzRiverSectionDO section = sectionMap.get(referenceId);
                if (section != null) {
                    referenceName = section.getSectionName();
                    Long facilityId = section.getFacilityId();
                    if (facilityId != null) {
                        facilityCode = sectionFacilityCodeMap.get(facilityId);
                    }
                } else {
                    YzRiverSectionBfDO sbf = sectionBfMap.get(referenceId);
                    if (sbf != null) {
                        referenceName = sbf.getSectionName();
                        Long facilityId = sbf.getFacilityId();
                        if (facilityId != null) {
                            facilityCode = sectionBfFacilityCodeMap.get(facilityId);
                        }
                    }
                }
            } else if (ReferenceTypeConstants.RESERVOIR.equals(referenceType)) {
                YzWaterReservoirDO reservoir = reservoirMap.get(referenceId);
                if (reservoir != null) {
                    referenceName = reservoir.getReservoirName();
                    facilityCode = reservoir.getReservoirCode();
                } else {
                    YzWaterReservoirBfDO rbf = reservoirBfMap.get(referenceId);
                    if (rbf != null) {
                        referenceName = rbf.getReservoirName();
                        facilityCode = rbf.getReservoirCode();
                    }
                }
            }

            if (StrUtil.isNotBlank(referenceName) || StrUtil.isNotBlank(facilityCode)) {
                result.put(feedbackId, new ReferenceInfo(referenceType, referenceId, facilityCode, referenceName));
            }
        }
        return result;
    }

    private ReferenceInfo resolveReferenceInfo(String referenceType, Long referenceId) {
        String type = normalizeReferenceType(referenceType);
        if (type == null || referenceId == null) {
            return null;
        }
        if (ReferenceTypeConstants.RIVER.equals(type)) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(referenceId);
            if (channel == null) {
                return null;
            }
            return new ReferenceInfo(type, channel.getId(), channel.getRiverCode(), channel.getRiverName());
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            YzRiverSectionDO section = riverSectionMapper.selectById(referenceId);
            if (section == null) {
                return null;
            }
            String facilityCode = null;
            if (section.getFacilityId() != null) {
                YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(section.getFacilityId());
                if (base != null) {
                    facilityCode = base.getFacilityCode();
                }
            }
            return new ReferenceInfo(type, section.getId(), facilityCode, section.getSectionName());
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(referenceId);
            if (reservoir == null) {
                return null;
            }
            return new ReferenceInfo(type, reservoir.getId(), reservoir.getReservoirCode(), reservoir.getReservoirName());
        }
        return null;
    }

    /**
     * 手机端：仅从 BF 备份表解析关联对象（禁止回落到正式表，避免误入生产设施主数据）。
     */
    private ReferenceInfo resolveReferenceInfoFromBackupTables(String referenceType, Long referenceId) {
        String type = normalizeReferenceType(referenceType);
        if (type == null || referenceId == null) {
            return null;
        }
        if (ReferenceTypeConstants.RIVER.equals(type)) {
            YzRiverChannelBfDO channel = riverChannelBfMapper.selectById(referenceId);
            if (channel == null) {
                return null;
            }
            return new ReferenceInfo(type, channel.getId(), channel.getRiverCode(), channel.getRiverName());
        }
        if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            YzRiverSectionBfDO section = riverSectionBfMapper.selectById(referenceId);
            if (section == null) {
                return null;
            }
            String facilityCode = null;
            if (section.getFacilityId() != null) {
                YzWaterFacilityBaseBfDO base = facilityBaseBfMapper.selectById(section.getFacilityId());
                if (base != null) {
                    facilityCode = base.getFacilityCode();
                }
            }
            return new ReferenceInfo(type, section.getId(), facilityCode, section.getSectionName());
        }
        if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            YzWaterReservoirBfDO reservoir = waterReservoirBfMapper.selectById(referenceId);
            if (reservoir == null) {
                return null;
            }
            return new ReferenceInfo(type, reservoir.getId(), reservoir.getReservoirCode(), reservoir.getReservoirName());
        }
        return null;
    }

    /**
     * 手机端创建：行政区划依据 BF 备份表关联的基础信息。
     */
    private String[] resolveDivisionCodesFromBackupTables(String referenceType, Long referenceId) {
        String type = normalizeReferenceType(referenceType);
        if (type == null || referenceId == null) {
            return null;
        }
        Long facilityId = null;
        if (ReferenceTypeConstants.RIVER.equals(type)) {
            YzRiverChannelBfDO channel = riverChannelBfMapper.selectById(referenceId);
            if (channel == null) {
                return null;
            }
            facilityId = channel.getFacilityId();
        } else if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            YzRiverSectionBfDO section = riverSectionBfMapper.selectById(referenceId);
            if (section == null) {
                return null;
            }
            facilityId = section.getFacilityId();
        } else if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            YzWaterReservoirBfDO reservoir = waterReservoirBfMapper.selectById(referenceId);
            if (reservoir == null) {
                return null;
            }
            String[] townshipCodes = normalizeDivisionCodes(reservoir.getTownship());
            if (townshipCodes != null && townshipCodes.length > 0) {
                return townshipCodes;
            }
            facilityId = reservoir.getFacilityId();
        }
        if (facilityId == null) {
            return null;
        }
        YzWaterFacilityBaseBfDO base = facilityBaseBfMapper.selectById(facilityId);
        if (base == null || StrUtil.isBlank(base.getAdminRegionCode())) {
            return null;
        }
        return new String[]{StrUtil.trim(base.getAdminRegionCode())};
    }

    private String[] resolveDivisionCodes(String referenceType, Long referenceId) {
        String type = normalizeReferenceType(referenceType);
        if (type == null || referenceId == null) {
            return null;
        }
        Long facilityId = null;
        if (ReferenceTypeConstants.RIVER.equals(type)) {
            YzRiverChannelDO channel = riverChannelMapper.selectById(referenceId);
            if (channel == null) {
                return null;
            }
            facilityId = channel.getFacilityId();
        } else if (ReferenceTypeConstants.RIVER_SECTION.equals(type)) {
            YzRiverSectionDO section = riverSectionMapper.selectById(referenceId);
            if (section == null) {
                return null;
            }
            facilityId = section.getFacilityId();
        } else if (ReferenceTypeConstants.RESERVOIR.equals(type)) {
            YzWaterReservoirDO reservoir = waterReservoirMapper.selectById(referenceId);
            if (reservoir == null) {
                return null;
            }
            String[] townshipCodes = normalizeDivisionCodes(reservoir.getTownship());
            if (townshipCodes != null && townshipCodes.length > 0) {
                return townshipCodes;
            }
            facilityId = reservoir.getFacilityId();
        }
        if (facilityId == null) {
            return null;
        }
        YzWaterFacilityBaseDO base = facilityBaseMapper.selectById(facilityId);
        if (base == null || StrUtil.isBlank(base.getAdminRegionCode())) {
            return null;
        }
        return new String[]{StrUtil.trim(base.getAdminRegionCode())};
    }

    private String[] normalizeDivisionCodes(String[] codes) {
        if (codes == null || codes.length == 0) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String code : codes) {
            if (StrUtil.isBlank(code)) {
                continue;
            }
            String trimmed = StrUtil.trim(code);
            if (!result.contains(trimmed)) {
                result.add(trimmed);
            }
        }
        return result.isEmpty() ? null : result.toArray(new String[0]);
    }

    private String resolveFeedbackPerson(YzProblemFeedbackDO item) {
        if (item == null) {
            return null;
        }
        if (Boolean.TRUE.equals(item.getRealName()) && StrUtil.isNotBlank(item.getName())) {
            return item.getName();
        }
        return "匿名";
    }

    private String resolveProgressLabel(Integer status, Map<String, String> progressLabelMap) {
        if (status == null) {
            return null;
        }
        String statusValue = String.valueOf(status);
        String label = progressLabelMap.get(statusValue);
        if (StrUtil.isNotBlank(label)) {
            return label;
        }
        return statusLabel(status);
    }

    private Map<Long, YzProblemStatusTaskDO> loadLatestTask(List<YzProblemFeedbackDO> feedbackList) {
        if (CollUtil.isEmpty(feedbackList)) {
            return Collections.emptyMap();
        }
        List<Long> ids = feedbackList.stream()
                .map(YzProblemFeedbackDO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<YzProblemStatusTaskDO> tasks = statusTaskMapper.selectList(new LambdaQueryWrapper<YzProblemStatusTaskDO>()
                .in(YzProblemStatusTaskDO::getProblemFeedbackId, ids)
                .orderByDesc(YzProblemStatusTaskDO::getCreateTime));
        if (CollUtil.isEmpty(tasks)) {
            return Collections.emptyMap();
        }
        Map<Long, YzProblemStatusTaskDO> map = new HashMap<>();
        for (YzProblemStatusTaskDO task : tasks) {
            if (task == null || task.getProblemFeedbackId() == null) {
                continue;
            }
            if (task.getAssignedPersonId() == null) {
                continue;
            }
            map.putIfAbsent(task.getProblemFeedbackId(), task);
        }
        return map;
    }

    private Map<Long, String> loadNicknameMap(Map<Long, YzProblemStatusTaskDO> latestTaskMap) {
        if (latestTaskMap == null || latestTaskMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = latestTaskMap.values().stream()
                .map(YzProblemStatusTaskDO::getAssignedPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<ProblemFeedbackUserSimpleRespVO> users = systemUserSimpleMapper.selectUsersByIds(userIds);
        if (CollUtil.isEmpty(users)) {
            return Collections.emptyMap();
        }
        return users.stream()
                .filter(it -> it != null && it.getId() != null)
                .collect(Collectors.toMap(ProblemFeedbackUserSimpleRespVO::getId, ProblemFeedbackUserSimpleRespVO::getNickname, (a, b) -> a));
    }

    private YzProblemStatusTaskDO getLatestTask(Long feedbackId) {
        return statusTaskMapper.selectOne(new LambdaQueryWrapper<YzProblemStatusTaskDO>()
                .eq(YzProblemStatusTaskDO::getProblemFeedbackId, feedbackId)
                .orderByDesc(YzProblemStatusTaskDO::getUpdateTime)
                .orderByDesc(YzProblemStatusTaskDO::getCreateTime)
                .last("LIMIT 1"));
    }

    private boolean isAdmin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            return false;
        }
        Set<String> roleCodes = getRoleCodeSet(loginUser);
        for (String roleCode : ADMIN_ROLE_CODES) {
            if (roleCodes.contains(roleCode)) {
                return true;
            }
        }
        if (roleCodes.contains(ROLE_GUEST)) {
            return false;
        }
        return false;
    }

    private boolean isGuest(LoginUser loginUser) {
        if (loginUser == null || loginUser.getId() == null) {
            return false;
        }
        Set<String> roleCodes = getRoleCodeSet(loginUser);
        for (String roleCode : ADMIN_ROLE_CODES) {
            if (roleCodes.contains(roleCode)) {
                return false;
            }
        }
        return GuestRoleUtils.isPureGuest(roleCodes);
    }

    private void ensureAssigneeNotPureGuest(Long assignedPersonId) {
        if (assignedPersonId == null) {
            return;
        }
        if (Boolean.TRUE.equals(systemUserSimpleMapper.selectIsPureGuestByUserId(assignedPersonId))) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_ASSIGNEE_PURE_GUEST_NOT_ALLOWED);
        }
    }

    private Set<String> getRoleCodeSet(LoginUser loginUser) {
        @SuppressWarnings("unchecked")
        List<String> cached = loginUser.getContext("roleCodes", List.class);
        if (cached != null) {
            return cached.stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
        }
        List<String> roleCodes = systemUserSimpleMapper.selectRoleCodesByUserId(loginUser.getId());
        loginUser.setContext("roleCodes", roleCodes == null ? Collections.emptyList() : roleCodes);
        if (CollUtil.isEmpty(roleCodes)) {
            return Collections.emptySet();
        }
        return roleCodes.stream().filter(StrUtil::isNotBlank).collect(Collectors.toSet());
    }

    private void ensureAdmin() {
        if (!isAdmin(SecurityFrameworkUtils.getLoginUser())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_PERMISSION_DENIED);
        }
    }

    /**
     * 生成短信短链并落库，返回短信模板需要的 code。
     * <p>
     * 说明：推荐在“组装短信参数”之前创建短链记录，避免短信里出现无法解析的 code。
     */
    private String createSmsShortLink(String scene, Long feedbackId, int expireDays, String action, String receiver, String mobilephone) {
        LocalDateTime expiresTime = expireDays <= 0 ? null : LocalDateTime.now().plusDays(expireDays);
        Map<String, Object> ext = new HashMap<>();
        ext.put("from", "sms");
        ext.put("action", StrUtil.blankToDefault(action, ""));
        ext.put("receiver", StrUtil.blankToDefault(receiver, ""));
        ext.put("feedbackId", feedbackId == null ? "" : String.valueOf(feedbackId));
        return shortLinkService.create(scene, SHORT_LINK_BIZ_TYPE_PROBLEM_FEEDBACK, feedbackId, mobilephone, expiresTime, ext);
    }

    private Map<String, String> loadDictLabel(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(item -> ObjUtil.isNotEmpty(item.getValue()))
                .collect(Collectors.toMap(DictDataRespDTO::getValue, DictDataRespDTO::getLabel, (a, b) -> a));
    }

    private String statusLabel(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case STATUS_WAIT_AUDIT -> "待受理";
            case STATUS_REJECTED -> "已驳回";
            case STATUS_PROCESSING -> "处理中";
            case STATUS_PENDING_VERIFY -> "待核验";
            case STATUS_FINISHED -> "已办结";
            default -> "未知";
        };
    }

    private ProblemFeedbackStatusSummaryVO emptySummary() {
        ProblemFeedbackStatusSummaryVO vo = new ProblemFeedbackStatusSummaryVO();
        vo.setTotalCount(0L);
        vo.setProcessingCount(0L);
        vo.setPendingVerifyCount(0L);
        vo.setFinishedCount(0L);
        return vo;
    }

    private Long countWithStatus(boolean admin, List<Long> assignedIds, int status) {
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .eq(YzProblemFeedbackDO::getStatus, status);
        if (!admin) {
            wrapper.in(YzProblemFeedbackDO::getId, assignedIds);
        }
        return feedbackMapper.selectCount(wrapper);
    }

    private Long countTotal(boolean admin, List<Long> assignedIds) {
        if (admin) {
            return feedbackMapper.selectCount(new LambdaQueryWrapper<>());
        }
        LambdaQueryWrapper<YzProblemFeedbackDO> wrapper = new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .in(YzProblemFeedbackDO::getId, assignedIds);
        return feedbackMapper.selectCount(wrapper);
    }

    private boolean canAccessFeedback(LoginUser loginUser, Long feedbackId) {
        if (isGuest(loginUser)) {
            return false;
        }
        if (isAdmin(loginUser)) {
            return true;
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return false;
        }
        List<YzProblemStatusTaskDO> tasks = statusTaskMapper.selectList(new LambdaQueryWrapper<YzProblemStatusTaskDO>()
                .eq(YzProblemStatusTaskDO::getProblemFeedbackId, feedbackId)
                .eq(YzProblemStatusTaskDO::getAssignedPersonId, userId));
        return CollUtil.isNotEmpty(tasks);
    }

    /**
     * 删除问题反馈（仅删除问题反馈与状态任务记录）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedback(Long id) {
        ensureAdmin();
        YzProblemFeedbackDO feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_PROBLEM_FEEDBACK_NOT_EXISTS);
        }
        statusTaskMapper.delete(new LambdaQueryWrapper<YzProblemStatusTaskDO>()
                .eq(YzProblemStatusTaskDO::getProblemFeedbackId, id));
        feedbackMapper.deleteById(id);
    }
}
