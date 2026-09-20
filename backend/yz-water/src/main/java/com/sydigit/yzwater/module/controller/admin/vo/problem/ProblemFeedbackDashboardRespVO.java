package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 问题反馈看板统计返回
 */
@Data
public class ProblemFeedbackDashboardRespVO {

    @Schema(description = "今日反馈问题个数")
    private Long todayFeedbackCount;

    @Schema(description = "问题解决率（%），保留两位小数，四舍五入；已解决包含已驳回")
    private BigDecimal solveRate;

    @Schema(description = "问题状态统计")
    private StatusStat statusStat;

    @Schema(description = "问题类型占比（已驳回不参与）")
    private List<TypeStat> typeStatList;

    @Schema(description = "问题 TOP5（已驳回不参与）")
    private List<TopStat> top5List;

    @Schema(description = "公众参与度（已驳回仅计入反馈总次数）")
    private ParticipationStat participation;

    @Data
    public static class StatusStat {
        @Schema(description = "待受理数量（状态=0）")
        private Long pendingCount;
        @Schema(description = "已驳回数量（状态=1）")
        private Long rejectedCount;
        @Schema(description = "处理中数量（状态=2）")
        private Long processingCount;
        @Schema(description = "待核验数量（状态=3）")
        private Long pendingVerifyCount;
        @Schema(description = "已办结数量（状态=4）")
        private Long finishedCount;
    }

    @Data
    public static class TypeStat {
        @Schema(description = "问题类型（字典值：zd_fklx）")
        private String feedbackType;
        @Schema(description = "问题类型名称（字典 label）")
        private String feedbackTypeLabel;
        @Schema(description = "出现次数")
        private Long count;
    }

    @Data
    public static class TopStat {
        @Schema(description = "设施类型（river/river_section/reservoir）")
        private String referenceType;
        @Schema(description = "设施ID")
        private Long referenceId;
        @Schema(description = "设施名称")
        private String referenceName;
        @Schema(description = "问题次数")
        private Long count;
    }

    @Data
    public static class ParticipationStat {
        @Schema(description = "反馈总次数（问题条数，含已驳回）")
        private Long totalFeedbackCount;
        @Schema(description = "反馈人数（实名人数去重 + 匿名人数）")
        private Long feedbackPersonCount;
        @Schema(description = "实名人数（姓名+手机号去重）")
        private Long realPersonCount;
        @Schema(description = "匿名人数（按条计数）")
        private Long anonymousPersonCount;
        @Schema(description = "实名率（0-100）")
        private Integer realNameRate;

        @Schema(description = "匿名率（%），保留两位小数，四舍五入")
        private BigDecimal anonymousRate;
    }
}
