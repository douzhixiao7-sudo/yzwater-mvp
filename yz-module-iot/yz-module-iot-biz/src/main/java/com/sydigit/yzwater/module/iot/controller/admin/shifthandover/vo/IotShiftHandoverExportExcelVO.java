package com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交接班导出 Excel VO
 */
@Schema(description = "IoT - 交接班导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotShiftHandoverExportExcelVO {

    @ExcelProperty("交接班编号")
    private String handoverNo;

    @ExcelProperty("交接班时间")
    private LocalDateTime handoverTime;

    @ExcelProperty("值班班次")
    private String shiftName;

    @ExcelProperty("值班班组")
    private String teamName;

    @ExcelProperty("交班人")
    private String handoverUserName;

    @ExcelProperty("接班人")
    private String takeoverUserName;

    @ExcelProperty("值班日志")
    private String dutyLog;

    @ExcelProperty("待关注事项")
    private String pendingItems;

    @ExcelProperty("关联调度编号")
    private String dispatchInstructionNo;

    @ExcelProperty("关联指令名称")
    private String dispatchInstructionName;

    @ExcelProperty("状态")
    private String statusName;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建人")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}

