package com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "IoT - 养护计划分页 Request VO")
@Data
public class IotMaintenancePlanPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "闸门电机")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private String deviceType;

    @Schema(description = "所属闸站字典值", example = "1001")
    private String stationId;

    @Schema(description = "养护状态", example = "pending")
    private String status;

    @Schema(description = "养护类型", example = "periodic")
    private String maintainType;

    @Schema(description = "计划养护日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] planDate;
}
