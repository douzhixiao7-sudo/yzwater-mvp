package com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.faultrepair.IotFaultRepairStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 故障维修结果反馈 Request VO")
@Data
public class IotFaultRepairResultReqVO {

    @Schema(description = "主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "完成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "完成时间不能为空")
    private LocalDateTime finishTime;

    @Schema(description = "备件消耗列表")
    @Valid
    private List<IotFaultRepairSpareUsageVO> spareUsages;

    @Schema(description = "处理状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "completed")
    @NotBlank(message = "处理状态不能为空")
    @InEnum(value = IotFaultRepairStatusEnum.class, message = "处理状态必须是 {value}")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "维修人姓名")
    private String repairName;
}
