package com.sydigit.yzwater.module.controller.app;


import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackCreateReqVO;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageRespVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackBufferQueryReqVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackBufferQueryRespVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackMyPageReqVO;
import com.sydigit.yzwater.module.service.problem.ProblemFeedbackBufferQueryService;
import com.sydigit.yzwater.module.service.problem.ProblemFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 手机端仪征管理后台 - 问题反馈
 */
@Tag(name = "仪征手机端 - 问题反馈")
@RestController
@RequestMapping("/problem/feedback")
@Validated
public class AppProblemFeedbackController {


    private final ProblemFeedbackService problemFeedbackService;
    private final ProblemFeedbackBufferQueryService bufferQueryService;

    public AppProblemFeedbackController(ProblemFeedbackService problemFeedbackService,
                                        ProblemFeedbackBufferQueryService bufferQueryService) {
        this.problemFeedbackService = problemFeedbackService;
        this.bufferQueryService = bufferQueryService;
    }

    @PostMapping
    @Operation(summary = "新增问题反馈（关联河道/河段/水库须为 BF 备份表数据），默认待受理")
    public CommonResult<Long> create(@Valid @RequestBody ProblemFeedbackCreateReqVO reqVO) {
        return success(problemFeedbackService.createFeedbackForMobileApp(reqVO));
    }

    @GetMapping("/my-page")
    @Operation(summary = "分页查询我提出的河道问题（支持按时间与状态筛选）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ProblemFeedbackPageRespVO>> getMyPage(@Valid AppProblemFeedbackMyPageReqVO reqVO,
                                                                         @RequestParam(value = "createTime[]", required = false) String[] createTime) {
        // 兼容部分前端将数组序列化为 createTime[]=2025-12-09&createTime[]=2025-12-23 的写法
        if ((reqVO.getCreateTime() == null || reqVO.getCreateTime().length == 0) && createTime != null && createTime.length > 0) {
            reqVO.setCreateTime(createTime);
        }
        return success(problemFeedbackService.getMyRiverProblemPage(reqVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询我提出的河道问题详情")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ProblemFeedbackDetailRespVO> getMyDetail(@Valid @PathVariable("id") Long id) {
        return success(problemFeedbackService.getMyDetail(id));
    }

    @PostMapping("/buffer-query")
    @Operation(summary = "手机端-问题反馈缓冲区查询（河道/河段/水库）")
    @PermitAll
    public CommonResult<AppProblemFeedbackBufferQueryRespVO> bufferQuery(
            @Valid @RequestBody AppProblemFeedbackBufferQueryReqVO reqVO) {
        return success(bufferQueryService.query(reqVO));
    }




}
