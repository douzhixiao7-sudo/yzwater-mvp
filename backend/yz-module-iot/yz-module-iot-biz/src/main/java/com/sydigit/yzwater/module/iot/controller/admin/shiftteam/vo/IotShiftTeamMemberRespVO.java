package com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 班组成员响应 VO
 */
@Schema(description = "IoT - 班组成员 Response VO")
@Data
public class IotShiftTeamMemberRespVO {

    @Schema(description = "成员用户 ID")
    private Long userId;

    @Schema(description = "成员姓名")
    private String userName;

    @Schema(description = "联系方式")
    private String mobile;

    @Schema(description = "是否班组长")
    private Boolean leader;
}
