package com.sydigit.yzwater.module.controller.admin.vo.video;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 云台控制请求
 */
@Data
@Schema(description = "管理后台 - 视频云台控制请求")
public class VideoPtzControlReqVO {

    @Schema(description = "监控点编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "cameraIndexCode 不能为空")
    private String cameraIndexCode;

    @Schema(description = "动作，0开始 1停止", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "action 不能为空")
    private Integer action;

    @Schema(description = "云台命令，如 LEFT、RIGHT、UP、DOWN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "command 不能为空")
    private String command;

    @Schema(description = "速度，建议 1-100", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "speed 不能为空")
    private Integer speed;

    @Schema(description = "预置点，默认 0", example = "0")
    private Integer presetIndex;
}

