package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 幸福河湖平台-水质报警记录查询请求。
 */
@Data
@Schema(description = "幸福河湖平台-水质报警记录查询请求")
public class XfhhWaterQualityAlertListReqVO {

    @Schema(description = "外部平台数据源编号", example = "fb8cd0fe-addf-4364-82a1-8a0b11a9440c")
    private String id;

    @Valid
    @NotNull(message = "params 不能为空")
    @Schema(description = "查询参数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Params params;

    /**
     * 查询参数。
     */
    @Data
    @Schema(name = "XfhhWaterQualityAlertListParams", description = "幸福河湖平台-水质报警记录查询参数")
    public static class Params {

        @Schema(description = "站点编号", example = "4")
        private Long stationId;

        @NotBlank(message = "bgnAlertTime 不能为空")
        @Schema(description = "开始报警时间", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2026-04-01 00:00:00")
        @JsonAlias("bgnRecordTime")
        private String bgnAlertTime;

        @Schema(description = "结束报警时间", example = "2026-04-02 00:00:00")
        @JsonAlias("endRecordTime")
        private String endAlertTime;
    }
}
