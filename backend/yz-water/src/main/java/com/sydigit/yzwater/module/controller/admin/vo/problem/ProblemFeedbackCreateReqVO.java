package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 问题反馈创建请求
 */
@Data
public class ProblemFeedbackCreateReqVO {

    @Schema(description = "反馈类型（字典值，参考字典表 zd_fklx）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "反馈类型不能为空")
    private String feedbackType;

    @Schema(description = "反馈内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈内容不能为空")
    private String feedbackContent;

    @Schema(description = "上传的图片/视频 URL 列表")
    private List<String> uploadedFiles;

    @Schema(description = "是否实名反馈（0-匿名，1-实名）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实名标记不能为空")
    private Boolean realName;

    @Schema(description = "姓名（实名反馈时为必填）")
    private String name;

    @Schema(description = "手机号码（实名反馈时为必填）")
    private String phoneNumber;

    @Schema(description = "关联对象ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "关联对象ID不能为空")
    private Long referenceId;

    @Schema(description = "关联对象类型（river/river_section/reservoir）", requiredMode = Schema.RequiredMode.REQUIRED, example = "river")
    @NotBlank(message = "关联对象类型不能为空")
    private String referenceType;

    @Schema(description = "问题经度")
    private BigDecimal longitude;

    @Schema(description = "问题纬度")
    private BigDecimal latitude;

    @Schema(description = "问题具体位置")
    private String issueSpecificLocation;
}
