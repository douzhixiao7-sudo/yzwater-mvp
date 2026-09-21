package com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.deviceaccident.IotDeviceAccidentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 设备事故新增/修改 Request VO")
@Data
public class IotDeviceAccidentSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备不能为空")
    private Long deviceId;

    @Schema(description = "事故发生时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-05 08:30:00")
    @NotNull(message = "事故发生时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime accidentTime;

    @Schema(description = "事故地点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "事故地点不能为空")
    private String accidentLocation;

    @Schema(description = "事故类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "device_fault")
    @NotBlank(message = "事故类型不能为空")
    @InEnum(value = IotDeviceAccidentTypeEnum.class, message = "事故类型必须是 {value}")
    private String accidentType;

    @Schema(description = "事故描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "事故描述不能为空")
    private String accidentDesc;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "损失评估", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "损失评估不能为空")
    private String lossAssessment;

    @Schema(description = "责任人用户 ID", example = "1")
    private Long responsibleUserId;

    @Schema(description = "责任人", example = "张三")
    private String responsibleName;

    @Schema(description = "附件")
    private List<String> attachments;
}