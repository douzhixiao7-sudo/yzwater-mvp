package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河长信息批量导入 Excel 行
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 河长信息批量导入 Excel 行")
public class RiverChiefInfoImportExcelVO {

    @ExcelProperty(index = 0)
    @Schema(description = "序号")
    private String index;

    @ExcelProperty(index = 1)
    @Schema(description = "河长姓名")
    private String headName;

    @ExcelProperty(index = 2)
    @Schema(description = "行政区划")
    private String administrativeRegionName;

    @ExcelProperty(index = 3)
    @Schema(description = "河长级别")
    private String headLevelLabel;

    @ExcelProperty(index = 4)
    @Schema(description = "关联河道/水库名称")
    private String referenceName;

    @ExcelProperty(index = 5)
    @Schema(description = "河段名称")
    private String sectionName;

    @ExcelProperty(index = 6)
    @Schema(description = "河长职务")
    private String headPosition;

    @ExcelProperty(index = 7)
    @Schema(description = "联系电话")
    private String headContact;
}

