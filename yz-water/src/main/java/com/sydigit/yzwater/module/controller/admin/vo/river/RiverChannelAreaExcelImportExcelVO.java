package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河道流经地区/乡镇 Excel 导入行（仅读取 sheet1，数据从第 0 行开始）
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 河道流经地区/乡镇 Excel 导入行")
public class RiverChannelAreaExcelImportExcelVO {

    @ExcelProperty(index = 0)
    private String riverName;

    @ExcelProperty(index = 1)
    private String areaName;
}
