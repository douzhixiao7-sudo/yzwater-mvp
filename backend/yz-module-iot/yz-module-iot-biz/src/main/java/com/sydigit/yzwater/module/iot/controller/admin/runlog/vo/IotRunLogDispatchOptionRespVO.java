package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运行日志关联调令选项响应
 */
@Schema(description = "IoT - 运行日志关联调令选项 Response VO")
@Data
public class IotRunLogDispatchOptionRespVO {

    @Schema(description = "调令 ID")
    private Long id;

    @Schema(description = "调令编号")
    private String instructionNo;

    @Schema(description = "调令名称")
    private String instructionName;

    @Schema(description = "展示标题")
    private String title;
}
