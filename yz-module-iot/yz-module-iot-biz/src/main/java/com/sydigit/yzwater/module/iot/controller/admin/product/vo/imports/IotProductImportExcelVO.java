package com.sydigit.yzwater.module.iot.controller.admin.product.vo.imports;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * IoT 产品 Excel 导入行 VO
 */
@Data
@ExcelIgnoreUnannotated
public class IotProductImportExcelVO {

    @ExcelProperty(index = 0)
    @Schema(description = "产品名称")
    private String productName;

    @ExcelProperty(index = 1)
    @Schema(description = "设备名称")
    private String deviceName;

    @ExcelProperty(index = 2)
    @Schema(description = "标签名称")
    private String tagName;

    @ExcelProperty(index = 3)
    @Schema(description = "功能名称")
    private String thingModelName;

    @ExcelProperty(index = 4)
    @Schema(description = "标识符")
    private String thingModelIdentifier;

    @ExcelProperty(index = 5)
    @Schema(description = "数据类型")
    private String dataType;

    @ExcelProperty(index = 6)
    @Schema(description = "单位名称")
    private String unitName;

}