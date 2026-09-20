package com.sydigit.yzwater.module.controller.admin.vo.flood;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛指挥部导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "管理后台 - 防汛指挥部导出 Excel VO")
public class FxZhbExportExcelVO {

    @ExcelProperty("指挥部名称")
    private String name;

    @ExcelProperty("地址")
    private String addr;

    @ExcelProperty("电话")
    private String tel;

    @ExcelProperty("传真")
    private String fax;

    @ExcelProperty("区划代码")
    private String areaCode;

    @ExcelProperty("邮政编码")
    private String zipCode;
}
