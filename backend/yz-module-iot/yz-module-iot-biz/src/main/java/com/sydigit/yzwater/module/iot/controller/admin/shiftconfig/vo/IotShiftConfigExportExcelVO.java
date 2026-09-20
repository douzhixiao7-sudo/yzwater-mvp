package com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班次配置导出 VO
 */
@Schema(description = "IoT - 班次配置导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotShiftConfigExportExcelVO {

    @ExcelProperty("班次编号")
    private String shiftNo;

    @ExcelProperty("班次名称")
    private String shiftName;

    @ExcelProperty("起始时间")
    private String startTime;

    @ExcelProperty("结束时间")
    private String endTime;

    @ExcelProperty("跨天标识")
    private String crossDay;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建人")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
