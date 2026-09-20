package com.sydigit.yzwater.module.controller.app.vo.problem;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 手机端 - 我提出的问题分页查询请求
 */
@Schema(description = "手机端 - 我提出的问题分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppProblemFeedbackMyPageReqVO extends PageParam {

    @Schema(description = "创建时间区间（开始、结束，格式：yyyy-MM-dd；兼容：createTime=[2025-12-02,2025-12-03]）")
    private String[] createTime;

    @Schema(description = "状态筛选（0-待受理，1-已驳回，2-处理中，3-待核验，4-已办结；不传则查询全部）")
    private Integer statusFilter;
}
