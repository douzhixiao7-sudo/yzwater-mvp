package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * 员工排班分页请求
 */
@Schema(description = "IoT - 员工排班分页 Request VO")
@Data
public class IotShiftSchedulePageReqVO extends PageParam {

    @Schema(description = "关键字（排班编号/值班人员/联系方式/班组/班次）")
    private String keyword;

    @Schema(description = "班组 ID")
    private Long teamId;

    @Schema(description = "班次 ID")
    private Long shiftId;

    @Schema(description = "值班人员 ID")
    private Long dutyUserId;

    @Schema(description = "值班人员")
    private String dutyUserName;

    @Schema(description = "状态（0待值班 1值班中 2已完成 3已取消）")
    private Integer status;

    @Schema(description = "值班日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] scheduleDateRange;
}
