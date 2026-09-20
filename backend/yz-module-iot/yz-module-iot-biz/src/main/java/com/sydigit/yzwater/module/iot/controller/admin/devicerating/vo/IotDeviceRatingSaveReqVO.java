package com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.devicerating.IotDeviceRatingResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "IoT - 设备评级新增/修改 Request VO")
@Data
public class IotDeviceRatingSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备不能为空")
    private Long deviceId;

    @Schema(description = "评级时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-05")
    @NotNull(message = "评级时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate ratingTime;

    @Schema(description = "评级人用户 ID", example = "1")
    private Long ratingUserId;

    @Schema(description = "评级人", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "评级人不能为空")
    private String ratingUserName;

    @Schema(description = "评级依据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评级依据不能为空")
    private String ratingBasis;

    @Schema(description = "评级结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "excellent")
    @NotBlank(message = "评级结果不能为空")
    @InEnum(value = IotDeviceRatingResultEnum.class, message = "评级结果必须是 {value}")
    private String ratingResult;

    @Schema(description = "整改建议（不合格必填）")
    private String rectifyAdvice;

    @Schema(description = "整改期限（不合格必填）", example = "2026-02-28")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate rectifyDeadline;

    @Schema(description = "附件")
    private List<String> attachments;
}
