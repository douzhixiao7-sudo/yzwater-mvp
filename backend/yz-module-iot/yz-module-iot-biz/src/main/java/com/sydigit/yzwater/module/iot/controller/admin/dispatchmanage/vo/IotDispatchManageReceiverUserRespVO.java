package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 接收人选项 Response VO
 */
@Schema(description = "IoT - 调度管理接收人选项 Response VO")
@Data
public class IotDispatchManageReceiverUserRespVO {

    @Schema(description = "用户 ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "部门 ID")
    private Long deptId;
}

