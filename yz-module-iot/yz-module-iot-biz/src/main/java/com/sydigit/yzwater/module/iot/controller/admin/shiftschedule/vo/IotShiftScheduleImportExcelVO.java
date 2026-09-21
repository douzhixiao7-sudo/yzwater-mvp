package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 员工排班导入 Excel VO
 */
@Data
public class IotShiftScheduleImportExcelVO {

    @ExcelProperty("值班日期")
    private LocalDate scheduleDate;

    @ExcelProperty("班次ID")
    private Long shiftId;

    @ExcelProperty("班组ID")
    private Long teamId;

    @ExcelProperty("值班人员ID")
    private Long dutyUserId;

    @ExcelProperty("值班日志")
    private String dutyLog;

    @ExcelProperty("备注")
    private String remark;
}
