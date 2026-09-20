package com.sydigit.yzwater.module.controller.admin.vo.flood;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛抗旱组织部成员导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "管理后台 - 防汛抗旱组织部成员导出 Excel VO")
public class FxZzbcyExportExcelVO {

    @ExcelProperty("岗位")
    private String positionLabel;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("职务")
    private String title;
}
