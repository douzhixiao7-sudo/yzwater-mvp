package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 扬州仪征中小型水库基本资料表（0326）导入行对象
 *
 * <p>按列索引读取，避免因表头文案细微变化导致解析失败。</p>
 */
@Data
public class ReservoirYzBasicInfoImportExcelVO {

    @ExcelProperty(index = 0)
    private String serialNo;

    @ExcelProperty(index = 1)
    private String reservoirName;

    @ExcelProperty(index = 2)
    private String reservoirScale;

    @ExcelProperty(index = 3)
    private String township;

    @ExcelProperty(index = 4)
    private String managementUnit;

    @ExcelProperty(index = 5)
    private String reservoirNature;

    @ExcelProperty(index = 6)
    private String catchmentArea;

    @ExcelProperty(index = 7)
    private String totalCapacity;

    @ExcelProperty(index = 8)
    private String activeCapacity;

    @ExcelProperty(index = 9)
    private String verifiedFloodLevel;

    @ExcelProperty(index = 10)
    private String designFloodLevel;

    @ExcelProperty(index = 11)
    private String normalOperatingLevel;

    @ExcelProperty(index = 12)
    private String floodLimitLevel;

    @ExcelProperty(index = 13)
    private String damCrestElevation;

    @ExcelProperty(index = 14)
    private String damTopWidth;

    @ExcelProperty(index = 15)
    private String maxDamHeight;

    @ExcelProperty(index = 16)
    private String damTopLength;
}
