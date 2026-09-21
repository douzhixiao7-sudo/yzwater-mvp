package com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 故障维修派工 Request VO")
@Data
public class IotFaultRepairAuditAssignReqVO {

    @Schema(description = "主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "维修人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "维修人不能为空")
    private String repairName;

    @Schema(description = "维修人用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "维修人不能为空")
    private Long repairUserId;

    @Schema(description = "计划完成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划完成时间不能为空")
    private LocalDateTime planFinishTime;

    @Schema(description = "备注")
    private String remark;
}
