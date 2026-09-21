package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河长管理导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 河长管理导出 Excel VO")
public class RiverChiefManagementExportExcelVO {

    @ExcelProperty("河长姓名")
    private String headName;

    @ExcelProperty("河长级别")
    private String headLevelLabel;

    @ExcelProperty("关联设施")
    private String referenceTypeLabel;

    @ExcelProperty("关联设施名称")
    private String referenceName;

    @ExcelProperty("河长职务")
    private String headPosition;

    @ExcelProperty("河长工作单位")
    private String headUnit;

    @ExcelProperty("河长联系电话")
    private String headContact;

    @ExcelProperty("河长职责")
    private String responsibilities;
}

