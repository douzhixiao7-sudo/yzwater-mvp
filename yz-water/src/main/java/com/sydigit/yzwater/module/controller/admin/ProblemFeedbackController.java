package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackAuditReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackDashboardRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackNotifyReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackNotifyRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackProcessReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackSpatialAnalysisRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackStatusSummaryVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackUserSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackVerifyReqVO;
import com.sydigit.yzwater.module.service.problem.ProblemFeedbackService;
import com.sydigit.yzwater.module.service.problem.ProblemFeedbackSpatialAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 问题反馈
 */
@Tag(name = "仪征管理后台 - 问题反馈")
@RestController
@RequestMapping("/problem/feedback")
@Validated
public class ProblemFeedbackController {

    private final ProblemFeedbackService problemFeedbackService;
    private final ProblemFeedbackSpatialAnalysisService spatialAnalysisService;

    public ProblemFeedbackController(ProblemFeedbackService problemFeedbackService,
                                     ProblemFeedbackSpatialAnalysisService spatialAnalysisService) {
        this.problemFeedbackService = problemFeedbackService;
        this.spatialAnalysisService = spatialAnalysisService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询问题反馈（管理员查看全部，普通用户仅看指派自己的任务）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ProblemFeedbackPageRespVO>> getPage(@Valid ProblemFeedbackPageReqVO reqVO) {
        return success(problemFeedbackService.getFeedbackPage(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询问题反馈（不分页）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<ProblemFeedbackPageRespVO>> getList(ProblemFeedbackListReqVO reqVO) {
        return success(problemFeedbackService.getFeedbackList(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出问题反馈 Excel")
    @PreAuthorize("isAuthenticated()")
    public void exportExcel(@Valid ProblemFeedbackPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<ProblemFeedbackExportExcelVO> list = problemFeedbackService.getFeedbackExportList(reqVO);
        ExcelUtils.write(response, "问题反馈.xls", "数据", ProblemFeedbackExportExcelVO.class, list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询问题反馈详情（管理员查看全部，普通用户仅看指派给自己的）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackDetailRespVO> getDetail(@Valid @PathVariable("id") Long id) {
        return success(problemFeedbackService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除问题反馈（仅删除问题反馈与状态任务记录）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> delete(@Valid @PathVariable("id") Long id) {
        problemFeedbackService.deleteFeedback(id);
        return success(true);
    }

    @PostMapping("/audit-assign")
    @Operation(summary = "管理员受理并指派问题")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> auditAndAssign(@Valid @RequestBody ProblemFeedbackAuditReqVO reqVO) {
        problemFeedbackService.auditAndAssign(reqVO);
        return success(true);
    }

    @PostMapping("/process")
    @Operation(summary = "处理人处理问题并提交待核验")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> process(@Valid @RequestBody ProblemFeedbackProcessReqVO reqVO) {
        problemFeedbackService.processFeedback(reqVO);
        return success(true);
    }

    @PostMapping("/verify")
    @Operation(summary = "管理员核验问题并办结/驳回")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> verify(@Valid @RequestBody ProblemFeedbackVerifyReqVO reqVO) {
        problemFeedbackService.verifyFeedback(reqVO);
        return success(true);
    }

    @PostMapping("/notify-assign")
    @Operation(summary = "管理员指派后发送短信（仅处理人）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackNotifyRespVO> notifyAssign(@Valid @RequestBody ProblemFeedbackNotifyReqVO reqVO) {
        return success(problemFeedbackService.notifyAssignSms(reqVO.getId()));
    }

    @PostMapping("/notify-finish")
    @Operation(summary = "管理员办结后短信通知（公众用户短信已禁用）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackNotifyRespVO> notifyFinish(@Valid @RequestBody ProblemFeedbackNotifyReqVO reqVO) {
        return success(problemFeedbackService.notifyFinishSms(reqVO.getId()));
    }

    @PostMapping("/notify-reject")
    @Operation(summary = "管理员驳回后短信通知（公众用户短信已禁用）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackNotifyRespVO> notifyReject(@Valid @RequestBody ProblemFeedbackNotifyReqVO reqVO) {
        return success(problemFeedbackService.notifyRejectSms(reqVO.getId()));
    }

    @PostMapping("/notify-overdue")
    @Operation(summary = "管理员催办超时问题发送短信（给处理人）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackNotifyRespVO> notifyOverdue(@Valid @RequestBody ProblemFeedbackNotifyReqVO reqVO) {
        return success(problemFeedbackService.notifyOverdueSms(reqVO.getId()));
    }

    @GetMapping("/status-summary")
    @Operation(summary = "查询问题状态数量汇总（管理员全部，普通用户仅看指派给自己的）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackStatusSummaryVO> getStatusSummary() {
        return success(problemFeedbackService.getStatusSummary());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "问题反馈看板统计（本月/本季度/本年度）")
    @PermitAll
    public CommonResult<ProblemFeedbackDashboardRespVO> getDashboard(@RequestParam(value = "period", required = false) String period) {
        return success(problemFeedbackService.getDashboard(period));
    }

    @GetMapping("/users")
    @Operation(summary = "查询可指派的用户列表（排除纯游客）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<ProblemFeedbackUserSimpleRespVO>> getUserList(
            @RequestParam(value = "feedbackId", required = false) Long feedbackId) {
        return success(problemFeedbackService.getNormalUsers(feedbackId));
    }

    @PostMapping("/spatial-analysis")
    @Operation(summary = "问题反馈空间分析")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackSpatialAnalysisRespVO> spatialAnalysis(
            @Valid @RequestBody ProblemFeedbackSpatialAnalysisReqVO reqVO) {
        return success(spatialAnalysisService.analyze(reqVO));
    }
}
