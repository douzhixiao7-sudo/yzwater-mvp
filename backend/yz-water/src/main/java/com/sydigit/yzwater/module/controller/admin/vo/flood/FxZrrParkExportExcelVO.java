package com.sydigit.yzwater.module.controller.admin.vo.flood;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 园镇防汛责任人导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "管理后台 - 园镇防汛责任人导出 Excel VO")
public class FxZrrParkExportExcelVO {

    @ExcelProperty("区划名称")
    private String divisionName;

    @ExcelProperty("行政责任人姓名")
    private String administrativeName;

    @ExcelProperty("行政责任人职务")
    private String administrativeTitle;

    @ExcelProperty("技术责任人姓名")
    private String technicalName;

    @ExcelProperty("技术责任人职务")
    private String technicalTitle;
}
