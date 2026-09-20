package com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班组导出 VO
 */
@Schema(description = "IoT - 班组导出 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotShiftTeamExportExcelVO {

    @ExcelProperty("班组编号")
    private String teamNo;

    @ExcelProperty("班组名称")
    private String teamName;

    @ExcelProperty("班组长")
    private String leaderUserName;

    @ExcelProperty("班组人数")
    private Integer memberCount;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建人")
    private String creator;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
