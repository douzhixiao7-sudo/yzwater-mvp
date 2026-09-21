package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 班组下拉选项
 */
@Schema(description = "IoT - 员工排班班组选项 Response VO")
@Data
public class IotShiftScheduleTeamOptionRespVO {

    @Schema(description = "班组 ID", example = "1")
    private Long id;

    @Schema(description = "班组编号")
    private String teamNo;

    @Schema(description = "班组名称")
    private String teamName;
}
