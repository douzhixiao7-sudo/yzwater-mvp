package com.sydigit.yzwater.module.controller.admin.vo.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 天气查询请求
 */
@Data
@Schema(description = "管理后台 - 天气查询请求")
public class WeatherBeginTimeReqVO {

    @Schema(description = "查询日期，格式 yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-23")
    @NotBlank(message = "beginTime 不能为空")
    private String beginTime;
}
