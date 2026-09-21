package com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "IoT - 故障维修备件消耗项 VO")
@Data
public class IotFaultRepairSpareUsageVO {

    @Schema(description = "备件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "备件不能为空")
    private Long spareId;

    @Schema(description = "消耗数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "消耗数量不能为空")
    @Min(value = 1, message = "消耗数量必须大于 0")
    private Integer qty;
}
