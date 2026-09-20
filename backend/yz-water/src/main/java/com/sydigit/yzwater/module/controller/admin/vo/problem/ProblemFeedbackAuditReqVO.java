package com.sydigit.yzwater.module.controller.admin.vo.problem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理员受理并指派请求
 */
@Data
public class ProblemFeedbackAuditReqVO {

    @Schema(description = "问题反馈ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问题反馈ID不能为空")
    private Long id;

    @Schema(description = "指派处理人ID")
    private Long assignedPersonId;

    @Schema(description = "计划完成时间（问题存在且指派人不为空时必填）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime plannedCompletionTime;

    @Schema(description = "问题状态描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "状态描述不能为空")
    private String statusDescription;

}
