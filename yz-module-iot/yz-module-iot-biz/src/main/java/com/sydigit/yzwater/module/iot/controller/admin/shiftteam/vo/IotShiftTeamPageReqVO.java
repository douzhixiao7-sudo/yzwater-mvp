package com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 班组分页请求 VO
 */
@Schema(description = "IoT - 班组分页 Request VO")
@Data
public class IotShiftTeamPageReqVO extends PageParam {

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "班组名称")
    private String teamName;

    @Schema(description = "班组长")
    private String leaderUserName;
}
