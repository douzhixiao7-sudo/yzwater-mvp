package com.sydigit.yzwater.module.controller.admin.vo.flood;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 防汛物资仓库导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "管理后台 - 防汛物资仓库导出 Excel VO")
public class FloodPreventionMaterialWarehouseExportExcelVO {

    @ExcelProperty("仓库名称")
    private String warehouseName;

    @ExcelProperty("具体位置")
    private String specificLocation;

    @ExcelProperty("经度")
    private BigDecimal longitude;

    @ExcelProperty("纬度")
    private BigDecimal latitude;

    @ExcelProperty("归属单位")
    private String belongUnit;

    @ExcelProperty("负责人姓名")
    private String leaderName;

    @ExcelProperty("负责人电话")
    private String leaderPhone;

    @ExcelProperty("物资种类")
    private String materialType;

    @ExcelProperty("行政划分")
    private String divisionCode;

    @ExcelProperty("仓库图片")
    private String warehouseImages;

    @ExcelProperty("备注")
    private String remarks;
}

