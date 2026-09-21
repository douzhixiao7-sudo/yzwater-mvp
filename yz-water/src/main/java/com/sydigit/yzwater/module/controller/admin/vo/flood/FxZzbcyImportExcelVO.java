package com.sydigit.yzwater.module.controller.admin.vo.flood;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛抗旱组织部成员导入 Excel 行
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "管理后台 - 防汛抗旱组织部成员导入 Excel 行")
public class FxZzbcyImportExcelVO {

    @ExcelProperty("姓名")
    @Schema(description = "姓名")
    private String name;

    @ExcelProperty("职务")
    @Schema(description = "职务")
    private String title;

    @ExcelProperty("担任防指职务")
    @Schema(description = "担任防指职务（字典中文标签）")
    private String positionLabel;
}
