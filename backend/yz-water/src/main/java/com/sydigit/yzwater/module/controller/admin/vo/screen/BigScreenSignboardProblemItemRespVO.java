package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 大屏统计 - 公示牌扫码问题返回项
 */
@Data
public class BigScreenSignboardProblemItemRespVO {

    @Schema(description = "问题主键ID")
    private Long id;

    @Schema(description = "问题类型（字典 zd_fklx 标签）")
    private String feedbackTypeLabel;

    @Schema(description = "设施名称")
    private String facilityName;

    @Schema(description = "反馈时间")
    private LocalDateTime feedbackTime;

    @Schema(description = "问题状态（字典 zd_wtjd 标签）")
    private String statusLabel;
}

