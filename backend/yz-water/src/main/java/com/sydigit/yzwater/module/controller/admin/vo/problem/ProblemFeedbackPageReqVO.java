package com.sydigit.yzwater.module.controller.admin.vo.problem;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 问题反馈分页查询请求
 */
@Schema(description = "仪征管理后台 - 问题反馈分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemFeedbackPageReqVO extends PageParam {

    @Schema(description = "河道编码（已弃用，前端请使用 facilityName/facilityType 查询）")
    @Deprecated
    private String riverCode;

    @Schema(description = "河道名称（已弃用，前端请使用 facilityName/facilityType 查询）")
    @Deprecated
    private String riverName;

    @Schema(description = "设施名称（模糊查询：河道/河段/水库名称）")
    private String facilityName;

    @Schema(description = "设施类型（字典：zd_sslb，对应公示牌 referenceType）")
    private String facilityType;

    @Schema(description = "问题类型（字典值：zd_fklx）")
    private String feedbackType;

    @Schema(description = "问题状态（0-待受理, 1-已驳回, 2-处理中, 3-待核验, 4-已办结）")
    private Integer status;

    @Schema(description = "创建时间区间（开始、结束）")
    private String[] createTime;
}
