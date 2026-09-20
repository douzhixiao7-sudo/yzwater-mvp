package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 调度管理分页 Request VO
 */
@Schema(description = "IoT - 调度管理分页 Request VO")
@Data
public class IotDispatchManagePageReqVO extends PageParam {

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "调度编号")
    private String instructionNo;

    @Schema(description = "发令单位")
    private String issueOrgName;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "执行状态")
    private Integer status;

    @Schema(description = "计划完成时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] plannedFinishTime;

    @Schema(description = "接收人用户ID（服务端注入）", hidden = true)
    private Long receiverUserId;
}
