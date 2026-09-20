package com.sydigit.yzwater.module.controller.admin.vo.embankment;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 堤防导入行对象
 *
 * <p>按列索引读取，适配模板《堤防数据260328.xlsx》：</p>
 * <p>第 1 行为表头，第 2 行起为数据；实际有效数据从 B 列开始。</p>
 */
@Data
public class EmbankmentImportExcelVO {

    @ExcelProperty(index = 0)
    private String serialNo;

    @ExcelProperty(index = 1)
    private String embankmentName;

    @ExcelProperty(index = 2)
    private String divisionName;

    @ExcelProperty(index = 3)
    private String riverName;

    @ExcelProperty(index = 4)
    private String riverBankSide;

    @ExcelProperty(index = 5)
    private String embankmentLevel;

    @ExcelProperty(index = 6)
    private String floodStandard;

    @ExcelProperty(index = 7)
    private String lengthM;

    @ExcelProperty(index = 8)
    private String designHighTide;

    @ExcelProperty(index = 9)
    private String crestElevation;

    @ExcelProperty(index = 10)
    private String startPoint;

    @ExcelProperty(index = 11)
    private String endPoint;

    @ExcelProperty(index = 12)
    private String description;
}
