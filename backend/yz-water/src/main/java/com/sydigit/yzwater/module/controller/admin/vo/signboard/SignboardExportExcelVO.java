package com.sydigit.yzwater.module.controller.admin.vo.signboard;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公示牌导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 公示牌导出 Excel VO")
public class SignboardExportExcelVO {

    @ExcelProperty("公示牌代码")
    private String signboardCode;

    @ExcelProperty("公示牌名称")
    private String signboardName;

    @ExcelProperty("关联河道/河段")
    private String riverName;

    @ExcelProperty("具体位置")
    private String specificLocation;

    @ExcelProperty("行政区划")
    private String adminRegion;

    @ExcelProperty("责任单位")
    private String maintenanceUnit;

    @ExcelProperty("责任人")
    private String responsiblePerson;

    @ExcelProperty("管理单位")
    private String managementUnit;

    @ExcelProperty("权属单位")
    private String ownershipUnit;
}
