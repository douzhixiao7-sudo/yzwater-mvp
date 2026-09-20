package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史河长信息导出 Excel VO（按设施聚合，仅导出当前河长）
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 历史河长信息导出 Excel VO")
public class RiverChiefHistoryExportExcelVO {

    @ExcelProperty("关联设施")
    private String referenceTypeLabel;

    @ExcelProperty("关联设施名称")
    private String referenceName;

    @ExcelProperty("当前河长姓名")
    private String currentHeadNames;

    @ExcelProperty("河长级别")
    private String headLevelLabel;

    @ExcelProperty("生效时间")
    private LocalDateTime effectiveFrom;

    @ExcelProperty("行政区划")
    private String administrativeRegion;
}

