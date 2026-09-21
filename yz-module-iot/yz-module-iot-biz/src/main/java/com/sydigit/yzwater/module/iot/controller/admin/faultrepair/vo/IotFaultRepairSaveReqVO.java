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

@Schema(description = "IoT - 故障维修工单新增/修改 Request VO")
@Data
public class IotFaultRepairSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备不能为空")
    private Long deviceId;

    @Schema(description = "设备名称", example = "闸门电机")
    private String deviceName;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "泵站设备")
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @Schema(description = "故障类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "故障类型不能为空")
    private String faultType;

    @Schema(description = "故障时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "故障时间不能为空")
    private LocalDateTime faultTime;

    @Schema(description = "故障现象", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "故障现象不能为空")
    private String faultSymptom;

    @Schema(description = "上报人姓名")
    private String reporterName;

    @Schema(description = "上报人用户ID")
    private Long reporterUserId;

    @Schema(description = "维修人姓名")
    private String repairName;

    @Schema(description = "处理状态", example = "pending")
    @InEnum(value = IotFaultRepairStatusEnum.class, message = "处理状态必须是 {value}")
    private String status;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "故障图片")
    private List<String> faultImages;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "备件消耗列表")
    @Valid
    private List<IotFaultRepairSpareUsageVO> spareUsages;
}
