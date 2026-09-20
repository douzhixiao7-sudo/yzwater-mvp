package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 幸福河湖平台-水位监测数据查询请求
 */
@Data
@Schema(description = "幸福河湖平台-水位监测数据查询请求")
public class XfhhWaterLevelDataListReqVO {

    @Schema(description = "外部平台数据源编号", example = "fb8cd0fe-addf-4364-82a1-8a0b11a9440c")
    private String id;

    @Valid
    @NotNull(message = "params 不能为空")
    @Schema(description = "查询参数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Params params;

    /**
     * 查询参数
     */
    @Data
    @Schema(name = "XfhhWaterLevelDataListParams", description = "幸福河湖平台-水位监测数据查询参数")
    public static class Params {

        @NotNull(message = "gaugeId 不能为空")
        @Schema(description = "水位计编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
        private Long gaugeId;

        @NotBlank(message = "bgnRecordTime 不能为空")
        @Schema(description = "开始监测时间", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2026-04-01 00:00:00")
        @JsonAlias("bgnAlertTime")
        private String bgnRecordTime;

        @NotBlank(message = "endRecordTime 不能为空")
        @Schema(description = "结束监测时间", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "2026-04-01 23:59:59")
        @JsonAlias("endAlertTime")
        private String endRecordTime;
    }
}
