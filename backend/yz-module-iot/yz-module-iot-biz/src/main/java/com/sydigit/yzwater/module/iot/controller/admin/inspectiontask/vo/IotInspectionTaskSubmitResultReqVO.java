package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 巡检任务提交结果 Request VO")
@Data
public class IotInspectionTaskSubmitResultReqVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "任务ID不能为空")
    private Long id;

    @Schema(description = "异常点数", example = "2")
    @Min(value = 0, message = "异常点数不能小于0")
    private Integer abnormalCount;

    @Schema(description = "提交备注", example = "巡检已完成，发现2处不合格项")
    private String remark;

    @Schema(description = "检查项目提交明细")
    @Valid
    private List<IotInspectionTaskSubmitResultItemVO> items;

    @Schema(description = "是否自动生成故障记录", example = "true")
    private Boolean autoCreateFaultRecords;

}
