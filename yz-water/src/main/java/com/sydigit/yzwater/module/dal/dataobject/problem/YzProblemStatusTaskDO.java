package com.sydigit.yzwater.module.dal.dataobject.problem;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 问题状态表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问题状态表实体")
@TableName(value = "yz_problem_status_task", autoResultMap = true)
@TenantIgnore
public class YzProblemStatusTaskDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联的反馈ID")
    @TableField("problem_feedback")
    private Long problemFeedbackId;

    @Schema(description = "指派的处理人ID（系统人员库中的员工）")
    @TableField("assigned_person_id")
    private Long assignedPersonId;

    @Schema(description = "是否催办（0-未催办，1-已催办）")
    @TableField("expedited")
    private Integer expedited;


    @Schema(description = "审核人id")
    @TableField("reviewer_person_id")
    private Long reviewerPersonId;

    @Schema(description = "审核时间")
    @TableField("reviewer_person_time")
    private LocalDateTime reviewerPersonTime;

    @Schema(description = "核验人id")
    @TableField("verification_id")
    private Long verificationId;


    @Schema(description = "问题状态（0-待受理, 1-已驳回, 2-处理中, 3-待核验, 4-已办结）")
    @TableField("status")
    private Integer status;

    @Schema(description = "管理员审核问题图片/视频（存储为文件路径的数组）")
    @TableField(value = "problem_review_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] problemReviewImages;

    @Schema(description = "问题状态流转说明")
    @TableField("status_description")
    private String statusDescription;

    @Schema(description = "问题状态流转时间")
    @TableField("status_description_time")
    private LocalDateTime statusDescriptionTime;

    @Schema(description = "上传的图片/视频（存储为文件路径的数组）")
    @TableField(value = "uploaded_files", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] uploadedFiles;


    @Schema(description = "问题处理图片/视频（存储为文件路径的数组）")
    @TableField(value = "problem_handle_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] problemHandleImages;

    @Schema(description = "核验处理的图片/视频（存储为文件路径的数组）")
    @TableField(value = "verify_handle_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] verifyHandleImages;

    @Schema(description = "处理时间（记录问题开始处理的时间）")
    @TableField("processing_time")
    private LocalDateTime processingTime;

    @Schema(description = "处理结果描述（解决方案或修复说明）")
    @TableField("resolution_description")
    private String resolutionDescription;

    @Schema(description = "处理完成后的核验结果")
    @TableField("verification_result")
    private String verificationResult;

    @Schema(description = "问题完成时间")
    @TableField("completion_time")
    private LocalDateTime completionTime;

    @Schema(description = "计划完成时间")
    @TableField("planned_completion_time")
    private LocalDateTime plannedCompletionTime;

}
