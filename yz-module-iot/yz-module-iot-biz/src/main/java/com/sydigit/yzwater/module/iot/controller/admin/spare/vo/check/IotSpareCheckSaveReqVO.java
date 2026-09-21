package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 备件盘点新增 Request VO")
@Data
public class IotSpareCheckSaveReqVO {

    @Schema(description = "备件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "备件不能为空")
    private Long spareId;

    @Schema(description = "盘点时间")
    private LocalDateTime checkTime;

    @Schema(description = "实盘数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "实盘数量不能为空")
    @Min(value = 0, message = "实盘数量必须大于等于 0")
    private Integer actualQty;

    @Schema(description = "是否立即反馈", example = "true")
    private Boolean applyResult;

    @Schema(description = "备注")
    private String remark;

}
