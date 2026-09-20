package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 调度方案选项 Response VO
 */
@Schema(description = "IoT - 调度方案选项 Response VO")
@Data
public class IotDispatchManagePlanOptionRespVO {

    @Schema(description = "调度方案 ID")
    private Long id;

    @Schema(description = "调度方案编号")
    private String planNo;

    @Schema(description = "调度方案名称")
    private String planName;

    @Schema(description = "调度方案类型")
    private String planType;
}
