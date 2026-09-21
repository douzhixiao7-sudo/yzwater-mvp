package com.sydigit.yzwater.module.controller.admin.vo.flood;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛指挥部成员导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "管理后台 - 防汛指挥部成员导出 Excel VO")
public class FxZhbMemberExportExcelVO {

    @ExcelProperty("指挥部名称")
    private String commandDepartmentName;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("职务")
    private String title;

    @ExcelProperty("电话")
    private String tel;

    @ExcelProperty("排序号")
    private Integer sort;
}
