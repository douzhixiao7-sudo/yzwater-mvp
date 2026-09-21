package com.sydigit.yzwater.module.controller.admin.vo.embankment;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 堤防导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 堤防导出 Excel VO")
public class EmbankmentExportExcelVO {

    @ExcelProperty("堤防代码")
    private String embankmentCode;

    @ExcelProperty("堤防名称")
    private String embankmentName;

    @ExcelProperty("堤防级别")
    private String embankmentLevelLabel;

    @ExcelProperty("堤防形式")
    private String embankmentFormLabel;

    @ExcelProperty("堤防长度")
    private BigDecimal lengthM;

    @ExcelProperty("堤防类型")
    private String embankmentTypeLabel;

    @ExcelProperty("归口管理部门")
    private String managementDepartment;

    @ExcelProperty("高程系统")
    private String elevationSystem;

    @ExcelProperty("工程任务")
    private String projectTask;

    @ExcelProperty("工程建设情况")
    private String constructionStatus;
}

