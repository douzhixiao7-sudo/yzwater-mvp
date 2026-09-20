package com.sydigit.yzwater.module.controller.admin.vo.embankment;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

/**
 * 堤防 GIS Excel 导入行对象
 */
@Data
@ExcelIgnoreUnannotated
public class EmbankmentGisImportExcelVO {

    @ExcelProperty(index = 0)
    private String embankmentName;

    @ExcelProperty(index = 1)
    private String startX;

    @ExcelProperty(index = 2)
    private String startY;

    @ExcelProperty(index = 3)
    private String endX;

    @ExcelProperty(index = 4)
    private String endY;
}
