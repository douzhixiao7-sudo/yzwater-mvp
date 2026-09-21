package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工排班导出 Excel VO
 */
@Schema(description = "IoT - 员工排班导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotShiftScheduleExportExcelVO {

    @ExcelProperty("排班编号")
    private String scheduleNo;

    @ExcelProperty("值班日期")
    private LocalDate scheduleDate;

    @ExcelProperty("值班班次")
    private String shiftName;

    @ExcelProperty("班组")
    private String teamName;

    @ExcelProperty("值班人员")
    private String dutyUserName;

    @ExcelProperty("联系方式")
    private String dutyMobile;

    @ExcelProperty("岗位")
    private String dutyPostName;

    @ExcelProperty("值班开始时间")
    private LocalDateTime dutyStartTime;

    @ExcelProperty("值班结束时间")
    private LocalDateTime dutyEndTime;

    @ExcelProperty("值班日志")
    private String dutyLog;

    @ExcelProperty("状态")
    private String statusName;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建人")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
