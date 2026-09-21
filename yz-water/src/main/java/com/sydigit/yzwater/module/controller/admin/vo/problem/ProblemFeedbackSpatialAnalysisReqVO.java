package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 问题反馈 - 空间分析请求
 */
@Data
public class ProblemFeedbackSpatialAnalysisReqVO {

    @Schema(description = "中心点经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "119.329")
    @NotNull(message = "中心点经度不能为空")
    private BigDecimal longitude;

    @Schema(description = "中心点纬度", requiredMode = Schema.RequiredMode.REQUIRED, example = "32.250")
    @NotNull(message = "中心点纬度不能为空")
    private BigDecimal latitude;

    @Schema(description = "分析半径(米)", requiredMode = Schema.RequiredMode.REQUIRED, example = "500")
    @NotNull(message = "分析半径不能为空")
    @DecimalMin(value = "0.01", message = "分析半径必须大于0")
    private BigDecimal radiusM;
}
