package com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.maintenanceplan.IotMaintenancePlanTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 养护计划提交 Request VO")
@Data
public class IotMaintenancePlanSubmitReqVO {

    @Schema(description = "主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "养护类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "periodic")
    @NotBlank(message = "养护类型不能为空")
    @InEnum(value = IotMaintenancePlanTypeEnum.class, message = "养护类型必须是 {value}")
    private String maintainType;

    @Schema(description = "养护项目")
    private String maintainItems;

    @Schema(description = "备件消耗列表")
    @Valid
    private List<IotMaintenancePlanSpareUsageVO> spareUsages;

    @Schema(description = "养护人姓名")
    private String maintainerName;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "备注不能为空")
    private String remark;
}
