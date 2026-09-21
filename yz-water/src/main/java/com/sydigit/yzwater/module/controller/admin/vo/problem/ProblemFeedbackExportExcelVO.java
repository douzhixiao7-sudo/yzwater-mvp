package com.sydigit.yzwater.module.controller.admin.vo.problem;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问题反馈导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 问题反馈导出 Excel VO")
public class ProblemFeedbackExportExcelVO {

    @ExcelProperty("设施编码")
    private String facilityCode;

    @ExcelProperty("设施名称")
    private String facilityName;

    @ExcelProperty("设施类型")
    private String facilityTypeLabel;

    @ExcelProperty("问题类型")
    private String feedbackTypeLabel;

    @ExcelProperty("反馈内容")
    private String feedbackContent;

    @ExcelProperty("反馈人")
    private String feedbackPerson;

/*    @ExcelProperty("图片/视频")
    private String uploadedFiles;*/

    @ExcelProperty("具体位置")
    private String specificLocation;

    @ExcelProperty("反馈日期")
    private LocalDateTime createTime;

    @ExcelProperty("处理进度")
    private String statusLabel;

    @ExcelProperty("处理人")
    private String assignedPersonName;
}
