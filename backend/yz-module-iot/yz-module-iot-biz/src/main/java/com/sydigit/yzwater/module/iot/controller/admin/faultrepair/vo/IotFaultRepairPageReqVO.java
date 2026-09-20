package com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 故障维修工单分页 Request VO")
@Data
public class IotFaultRepairPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "闸门电机")
    private String deviceName;

    @Schema(description = "设备类型", example = "泵站设备")
    private String deviceType;

    @Schema(description = "上报人", example = "张三")
    private String reporterName;

    @Schema(description = "维修人", example = "李四")
    private String repairName;

    @Schema(description = "处理人用户ID", example = "10086")
    private Long repairUserId;

    @Schema(description = "处理状态", example = "pending")
    private String status;

    @Schema(description = "故障时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] faultTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
