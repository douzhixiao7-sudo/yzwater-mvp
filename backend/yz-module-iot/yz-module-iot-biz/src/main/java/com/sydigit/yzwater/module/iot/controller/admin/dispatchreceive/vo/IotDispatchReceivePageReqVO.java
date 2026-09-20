package com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 调令接受分页 Request VO
 */
@Schema(description = "IoT - 调令接受分页 Request VO")
@Data
public class IotDispatchReceivePageReqVO extends PageParam {

    @Schema(description = "调度编号")
    private String instructionNo;

    @Schema(description = "指令名称")
    private String instructionName;

    @Schema(description = "发令单位")
    private String issueOrgName;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "执行状态（0待接收 1待执行 2已执行 3已逾期）")
    private Integer executionStatus;

    @Schema(description = "下发时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] issueTime;

    @Schema(description = "执行状态（服务端注入）", hidden = true)
    private Integer status;

    @Schema(description = "接收状态（服务端注入）", hidden = true)
    private Integer receiveStatus;

    @Schema(description = "当前访问用户ID（服务端注入）", hidden = true)
    private Long accessUserId;
}
