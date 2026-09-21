package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行日志导出 VO
 */
@Schema(description = "IoT - 运行日志导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotRunLogExportExcelVO {

    @ExcelProperty("记录编号")
    private String logNo;

    @ExcelProperty("任务名称")
    private String taskName;

    @ExcelProperty("值班班组")
    private String dutyTeamName;

    @ExcelProperty("记录人")
    private String recorderUserName;

    @ExcelProperty("记录时间")
    private LocalDateTime recordTime;

    @ExcelProperty("运行开始时间")
    private LocalDateTime runStartTime;

    @ExcelProperty("运行结束时间")
    private LocalDateTime runEndTime;

    @ExcelProperty("检查时段")
    private String checkPeriod;

    @ExcelProperty("设备名称")
    private String deviceName;

    @ExcelProperty("关联调令编号")
    private String dispatchInstructionNo;

    @ExcelProperty("备注")
    private String remark;
}
