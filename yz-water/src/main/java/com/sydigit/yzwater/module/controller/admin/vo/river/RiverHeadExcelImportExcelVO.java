package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河长信息 Excel 导入行（仅读取 sheet1）
 *
 * <p>Excel 第一行表头为：序号，河道名称，河道级别，河长级别，河长姓名，河长职务，联系方式</p>
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 河长信息 Excel 导入行")
public class RiverHeadExcelImportExcelVO {

    @ExcelProperty("序号")
    private String index;

    @ExcelProperty("河道名称")
    private String riverName;

    @ExcelProperty("河道级别")
    private String riverLevel;

    @ExcelProperty("河长级别")
    private String headLevel;

    @ExcelProperty("河长姓名")
    private String headName;

    @ExcelProperty("河长职务")
    private String headPosition;

    @ExcelProperty("联系方式")
    private String headContact;
}

