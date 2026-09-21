package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题反馈详情返回
 */
@Data
public class ProblemFeedbackDetailRespVO {

    @Schema(description = "问题反馈ID")
    private Long id;

    @Schema(description = "反馈类型（字典值）")
    private String feedbackType;

    @Schema(description = "反馈类型名称")
    private String feedbackTypeLabel;

    @Schema(description = "河道编码（已弃用，请使用 facilityCode）")
    @Deprecated
    private String riverCode;

    @Schema(description = "河道名称（已弃用，请使用 facilityName）")
    @Deprecated
    private String riverName;

    @Schema(description = "河段名称（已弃用，请使用 facilityName）")
    @Deprecated
    private String riverSectionName;

    @Schema(description = "反馈内容")
    private String feedbackContent;

    @Schema(description = "问题经度")
    private BigDecimal problemLongitude;

    @Schema(description = "问题纬度")
    private BigDecimal problemLatitude;

    @Schema(description = "问题具体位置")
    private String issueSpecificLocation;

    @Schema(description = "反馈人（匿名/姓名）")
    private String feedbackPerson;

    @Schema(description = "反馈图片/视频 URL 列表")
    private List<String> uploadedFiles;


    @Schema(description = "所在河道名称（已弃用，请使用 referenceName）")
    @Deprecated
    private String riverChannelName;

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

    @Schema(description = "公示牌经度")
    private BigDecimal longitude;

    @Schema(description = "公示牌纬度")
    private BigDecimal latitude;

    @Schema(description = "公示牌具体位置")
    private String specificLocation;

    @Schema(description = "问题状态（0-待受理, 1-已驳回, 2-处理中, 3-待核验, 4-已办结）")
    private Integer status;

    @Schema(description = "问题状态名称")
    private String statusLabel;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "状态流转记录")
    private List<ProblemStatusTaskRespVO> statusTasks;
}
