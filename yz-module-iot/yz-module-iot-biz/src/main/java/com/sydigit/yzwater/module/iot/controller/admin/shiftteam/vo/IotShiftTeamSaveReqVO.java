package com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 班组新增/编辑请求 VO
 */
@Schema(description = "IoT - 班组新增/编辑 Request VO")
@Data
public class IotShiftTeamSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）", example = "1024")
    private Long id;

    @Schema(description = "班组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "班组名称不能为空")
    private String teamName;

    @Schema(description = "所属站点", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "班组长用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "班组长不能为空")
    private Long leaderUserId;

    @Schema(description = "成员用户 ID 列表")
    private List<Long> memberUserIds;

    @Schema(description = "备注")
    private String remark;
}
