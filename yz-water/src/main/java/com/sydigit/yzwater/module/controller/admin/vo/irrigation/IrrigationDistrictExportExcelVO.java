package com.sydigit.yzwater.module.controller.admin.vo.irrigation;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 灌区导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "管理后台 - 灌区导出 Excel VO")
public class IrrigationDistrictExportExcelVO {

    @ExcelProperty("灌区编码")
    private String irrigationDistrictCode;

    @ExcelProperty("灌区名称")
    private String irrigationDistrictName;

    @ExcelProperty("所在流域")
    private String basinCode;

    @ExcelProperty("行政区划")
    private String divisionCode;

    @ExcelProperty("灌区图片")
    private String irrigationDistrictImages;

    @ExcelProperty("设计灌溉面积（万亩）")
    private BigDecimal designIrrigationArea;

    @ExcelProperty("实际可灌面积")
    private BigDecimal actualIrrigableArea;

    @ExcelProperty("实际基本农田面积(k㎡)")
    private BigDecimal basicFarmlandAreaKm2;

    @ExcelProperty("是否生态红线")
    private String isEcologicalRedLine;

    @ExcelProperty("是否开发边界")
    private String isDevelopmentBoundary;

    @ExcelProperty("干渠长度(单位 m)")
    private BigDecimal mainCanalLengthM;

    @ExcelProperty("负责人")
    private String leaderName;

    @ExcelProperty("联系电话")
    private String leaderPhone;

    @ExcelProperty("管理单位")
    private String managementUnit;

    @ExcelProperty("灌区类型")
    private String irrigationDistrictType;

    @ExcelProperty("备注")
    private String remarks;
}
