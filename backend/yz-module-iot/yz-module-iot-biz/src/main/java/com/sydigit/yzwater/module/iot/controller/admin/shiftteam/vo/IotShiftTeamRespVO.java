package com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 班组响应 VO
 */
@Schema(description = "IoT - 班组 Response VO")
@Data
public class IotShiftTeamRespVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "班组编号")
    private String teamNo;

    @Schema(description = "班组名称")
    private String teamName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "班组长用户 ID")
    private Long leaderUserId;

    @Schema(description = "班组长")
    private String leaderUserName;

    @Schema(description = "班组人数")
    private Integer memberCount;

    @Schema(description = "成员用户 ID 列表")
    private List<Long> memberUserIds;

    @Schema(description = "成员列表")
    private List<IotShiftTeamMemberRespVO> members;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
