package com.sydigit.yzwater.module.controller.admin.vo.pump;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 泵站导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 泵站导出 Excel VO")
public class PumpStationExportExcelVO {

    @ExcelProperty("泵站名称")
    private String pumpStationName;

    @ExcelProperty("泵站类型")
    private String pumpStationTypeLabel;

    @ExcelProperty("工程等别")
    private String engineeringGradeLabel;

    @ExcelProperty("装机功率(KW)")
    private BigDecimal installedCapacityKw;

    @ExcelProperty("具体位置")
    private String pumpStationPosition;

    @ExcelProperty("管理单位")
    private String managementDepartmentLabel;
}

