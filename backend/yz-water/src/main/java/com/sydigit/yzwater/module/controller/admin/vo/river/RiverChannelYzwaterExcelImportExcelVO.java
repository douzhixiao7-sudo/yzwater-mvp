package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * yzwater.xlsx 导入行模型（按列索引读取）
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "河道信息 Excel 导入行")
public class RiverChannelYzwaterExcelImportExcelVO {

    @ExcelProperty(index = 0)
    private String riverName;

    @ExcelProperty(index = 1)
    private String riverLevel;

    @ExcelProperty(index = 2)
    private String town;

    @ExcelProperty(index = 3)
    private String ecologyType;

    @ExcelProperty(index = 4)
    private String flowAreas;

    @ExcelProperty(index = 5)
    private String ignoreCol6;

    @ExcelProperty(index = 6)
    private String ignoreCol7;

    @ExcelProperty(index = 7)
    private String catchmentKm2;

    @ExcelProperty(index = 8)
    private String lengthKm;

    @ExcelProperty(index = 9)
    private String managementUnit;
}
