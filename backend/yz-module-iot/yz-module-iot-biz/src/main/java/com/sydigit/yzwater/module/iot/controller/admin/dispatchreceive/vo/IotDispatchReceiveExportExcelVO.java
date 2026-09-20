package com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调令接受导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
public class IotDispatchReceiveExportExcelVO {

    @ExcelProperty("调度编号")
    private String instructionNo;

    @ExcelProperty("指令名称")
    private String instructionName;

    @ExcelProperty("发令单位")
    private String issueOrgName;

    @ExcelProperty("调度内容")
    private String instructionContent;

    @ExcelProperty("下发时间")
    private LocalDateTime issueTime;

    @ExcelProperty("接收人")
    private String receiverUserName;

    @ExcelProperty("执行人")
    private String executorUserName;

    @ExcelProperty("执行状态")
    private String executionStatusName;

    @ExcelProperty("完成时间")
    private LocalDateTime finishTime;

    @ExcelProperty("提交人")
    private String submitUserName;
}
