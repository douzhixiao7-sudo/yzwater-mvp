package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度管理导出 Excel VO
 */
@Schema(description = "IoT - 调度管理导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotDispatchManageExportExcelVO {

    @ExcelProperty("调度编号")
    private String instructionNo;

    @ExcelProperty("发令单位")
    private String issueOrgName;

    @ExcelProperty("发令人")
    private String issueUserName;

    @ExcelProperty("调度内容")
    private String instructionContent;

    @ExcelProperty("调度方案")
    private String planNames;

    @ExcelProperty("计划完成时间")
    private LocalDateTime plannedFinishTime;

    @ExcelProperty("接收单位")
    private String receiverDeptName;

    @ExcelProperty("接收人")
    private String receiverUserName;

    @ExcelProperty("执行人")
    private String executorUserName;

    @ExcelProperty("执行状态")
    private String statusName;

    @ExcelProperty("运行日志数")
    private Integer runLogCount;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
