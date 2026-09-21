package com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

/**
 * 班次配置新增/编辑请求 VO
 */
@Schema(description = "IoT - 班次配置新增/编辑 Request VO")
@Data
public class IotShiftConfigSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）", example = "1024")
    private Long id;

    @Schema(description = "班次名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "早班")
    @NotBlank(message = "班次名称不能为空")
    private String shiftName;

    @Schema(description = "所属站点", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "起始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "08:00")
    @NotNull(message = "起始时间不能为空")
    @DateTimeFormat(pattern = "HH:mm")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "16:00")
    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(pattern = "HH:mm")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Schema(description = "跨天标识（true 跨天，false 不跨天）", example = "false")
    private Boolean crossDay;

    @Schema(description = "备注")
    private String remark;
}
