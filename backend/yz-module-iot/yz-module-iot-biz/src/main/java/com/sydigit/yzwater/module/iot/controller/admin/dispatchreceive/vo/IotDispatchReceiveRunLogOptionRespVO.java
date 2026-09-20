package com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 调令接受运行日志选项 Response VO
 */
@Schema(description = "IoT - 调令接受运行日志选项 Response VO")
@Data
public class IotDispatchReceiveRunLogOptionRespVO {

    @Schema(description = "运行日志ID")
    private Long id;

    @Schema(description = "展示标题")
    private String title;
}
