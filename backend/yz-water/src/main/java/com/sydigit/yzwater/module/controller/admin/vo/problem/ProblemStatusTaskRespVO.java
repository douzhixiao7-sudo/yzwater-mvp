package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题状态记录返回
 */
@Data
public class ProblemStatusTaskRespVO {

    @Schema(description = "状态记录ID")
    private Long id;

    @Schema(description = "问题状态（0-待受理, 1-已驳回, 2-处理中, 3-待核验, 4-已办结）")
    private Integer status;

    @Schema(description = "问题状态名称")
    private String statusLabel;

    @Schema(description = "指派处理人ID")
    private Long assignedPersonId;

    @Schema(description = "指派处理人姓名")
    private String assignedPersonName;

    @Schema(description = "审核人ID")
    private Long reviewerPersonId;

    @Schema(description = "审核人姓名")
    private String reviewerPersonName;

    @Schema(description = "审核时间")
    private LocalDateTime reviewerPersonTime;

    @Schema(description = "核验人ID")
    private Long verificationId;

    @Schema(description = "核验人姓名")
    private String verificationName;

    @Schema(description = "核验时间")
    private LocalDateTime verificationTime;

    @Schema(description = "问题状态描述")
    private String statusDescription;

    @Schema(description = "问题状态流转时间")
    private LocalDateTime statusDescriptionTime;

    @Schema(description = "上传的图片/视频 URL 列表")
    private List<String> uploadedFiles;

    @Schema(description = "处理图片/视频 URL 列表")
    private List<String> problemHandleImages;

    @Schema(description = "核验图片 URL 列表")
    private List<String> verifyHandleImages;

    @Schema(description = "处理时间")
    private LocalDateTime processingTime;

    @Schema(description = "处理结果描述")
    private String resolutionDescription;

    @Schema(description = "是否无需处理")
    private Boolean noNeedHandle;

    @Schema(description = "处理完成后的核验结果")
    private String verificationResult;

    @Schema(description = "问题完成时间")
    private LocalDateTime completionTime;

    @Schema(description = "计划完成时间")
    private LocalDateTime plannedCompletionTime;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;
}
