package com.sydigit.yzwater.module.controller.admin.vo.signboard;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公示牌 Excel 导入行。
 *
 * <p>模板《公示牌副本0328.xlsx》从第 4 行开始读取：</p>
 * <p>A=序号，B=河湖名称，C=水库/河道，D=跳过，E=公示牌等级，F=所在乡镇，G=位置描述，H=经纬度，I=管护单位，J=编号。</p>
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 公示牌 Excel 导入行")
public class SignboardExcelImportExcelVO {

    @ExcelProperty(index = 0)
    private String sequenceNo;

    @ExcelProperty(index = 1)
    private String signboardName;

    @ExcelProperty(index = 2)
    private String relationType;

    @ExcelProperty(index = 3)
    private String ignoreColD;

    @ExcelProperty(index = 4)
    private String signboardLevelLabel;

    @ExcelProperty(index = 5)
    private String adminRegionName;

    @ExcelProperty(index = 6)
    private String specificLocation;

    @ExcelProperty(index = 7)
    private String longitudeLatitudeText;

    @ExcelProperty(index = 8)
    private String maintenanceUnit;

    @ExcelProperty(index = 9)
    private String signboardCode;
}
