package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareIoAuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "IoT - 备件出入库审批 Request VO")
@Data
public class IotSpareIoAuditReqVO {

    @Schema(description = "主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "approved")
    @NotBlank(message = "审批状态不能为空")
    @InEnum(value = IotSpareIoAuditStatusEnum.class, message = "审批状态必须是 {value}")
    private String auditStatus;

    @Schema(description = "审批备注")
    private String auditRemark;

}
