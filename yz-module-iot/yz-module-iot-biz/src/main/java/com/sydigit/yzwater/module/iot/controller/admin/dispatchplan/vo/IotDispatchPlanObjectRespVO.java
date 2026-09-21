package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 调度方案操作对象 Response VO
 */
@Schema(description = "IoT - 调度方案操作对象 Response VO")
@Data
public class IotDispatchPlanObjectRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "对象排序", example = "1")
    private Integer objectSort;

    @Schema(description = "对象类型（1设备 2自定义）", example = "1")
    private Integer objectType;

    @Schema(description = "对象名称")
    private String objectName;

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "安装位置 ID")
    private Long locationId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "参数列表")
    private List<IotDispatchPlanParamRespVO> params;
}

