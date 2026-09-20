package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题反馈分页返回
 */
@Data
public class ProblemFeedbackPageRespVO {

    @Schema(description = "问题反馈ID")
    private Long id;

    @Schema(description = "河道编码（已弃用，请使用 facilityCode）")
    @Deprecated
    private String riverCode;

    @Schema(description = "反馈类型（字典值）")
    private String feedbackType;

    @Schema(description = "反馈类型名称")
    private String feedbackTypeLabel;

    @Schema(description = "反馈内容")
    private String feedbackContent;

    @Schema(description = "反馈人")
    private String feedbackPerson;

    @Schema(description = "反馈图片/视频 URL 列表")
    private List<String> uploadedFiles;


    @Schema(description = "河道名称（已弃用，请使用 facilityName）")
    @Deprecated
    private String riverName;

    @Schema(description = "所在河道名称（已弃用，请使用 referenceName）")
    @Deprecated
    private String riverChannelName;

    @Schema(description = "所在河段名称（已弃用，请使用 referenceName）")
    @Deprecated
    private String riverSectionName;

    @Schema(description = "公示牌经度")
    private BigDecimal longitude;

    @Schema(description = "公示牌纬度")
    private BigDecimal latitude;

   /* @Schema(description = "公示牌具体位置")
    private String specificLocation;*/

    @Schema(description = "问题经度")
    private BigDecimal problemLongitude;

    @Schema(description = "问题纬度")
    private BigDecimal problemLatitude;

    @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "关联对象ID")
    private Long referenceId;

    @Schema(description = "关联对象名称")
    private String referenceName;

    @Schema(description = "设施编码（河道/河段/水库编码）")
    private String facilityCode;

    @Schema(description = "设施名称（河道/河段/水库名称）")
    private String facilityName;

    @Schema(description = "问题具体位置")
    private String issueSpecificLocation;

    @Schema(description = "问题状态（0-待受理, 1-已驳回, 2-处理中, 3-待核验, 4-已办结）")
    private Integer status;

    @Schema(description = "问题状态名称")
    private String statusLabel;

    @Schema(description = "处理人ID")
    private Long assignedPersonId;

    @Schema(description = "处理人姓名")
    private String assignedPersonName;

    @Schema(description = "计划完成时间")
    private LocalDateTime plannedCompletionTime;

    @Schema(description = "是否催办（0-未催办，1-已催办）")
    private Integer expedited;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否实名反馈（0-匿名，1-实名）")
    private Boolean realName;

    @Schema(description = "姓名（实名反馈时为必填）")
    private String name;
}
