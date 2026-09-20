package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度方案导出 Excel VO
 */
@Schema(description = "IoT - 调度方案导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotDispatchPlanExportExcelVO {

    @ExcelProperty("方案编号")
    private String planNo;

    @ExcelProperty("方案名称")
    private String planName;

    @ExcelProperty("方案类型")
    private String planTypeName;

    @ExcelProperty("编制人")
    private String prepareUserName;

    @ExcelProperty("编制单位")
    private String prepareOrgName;

    @ExcelProperty("编制时间")
    private LocalDateTime prepareTime;

    @ExcelProperty("方案状态")
    private String planStatusName;

    @ExcelProperty("核心建议目标")
    private String coreTarget;

    @ExcelProperty("涉及工程")
    private String projectName;

    @ExcelProperty("操作对象")
    private String objectNames;

    @ExcelProperty("附件数量")
    private Integer attachmentCount;

    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;
}

