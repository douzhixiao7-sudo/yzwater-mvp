package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题反馈 - 可选用户简要信息
 */
@Data
public class ProblemFeedbackUserSimpleRespVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "角色名称（聚合）")
    private String roleNames;

    @Schema(description = "河长级别")
    private String headLevel;

    @Schema(description = "河长级别名称")
    private String headLevelLabel;
}
