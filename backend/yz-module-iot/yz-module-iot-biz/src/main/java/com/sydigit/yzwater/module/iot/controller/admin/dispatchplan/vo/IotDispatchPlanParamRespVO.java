package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 调度方案对象参数 Response VO
 */
@Schema(description = "IoT - 调度方案对象参数 Response VO")
@Data
public class IotDispatchPlanParamRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "参数排序", example = "1")
    private Integer paramSort;

    @Schema(description = "参数名称", example = "开度")
    private String paramName;

    @Schema(description = "参数值", example = "2.5")
    private String paramValue;

    @Schema(description = "参数单位", example = "m")
    private String paramUnit;

    @Schema(description = "参数备注")
    private String remark;
}

