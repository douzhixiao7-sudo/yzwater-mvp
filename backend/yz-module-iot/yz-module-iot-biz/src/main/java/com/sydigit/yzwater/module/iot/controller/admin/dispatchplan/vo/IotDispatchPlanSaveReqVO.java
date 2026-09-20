package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.dispatchplan.IotDispatchPlanStatusEnum;
import com.sydigit.yzwater.module.iot.enums.dispatchplan.IotDispatchPlanTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 调度方案新增/修改 Request VO
 */
@Schema(description = "IoT - 调度方案新增/修改 Request VO")
@Data
public class IotDispatchPlanSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "方案名称不能为空")
    @Size(max = 60, message = "方案名称长度不能超过60个字符")
    private String planName;

    @Schema(description = "方案类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "routine")
    @NotBlank(message = "方案类型不能为空")
    @InEnum(value = IotDispatchPlanTypeEnum.class, message = "方案类型必须是 {value}")
    private String planType;

    @Schema(description = "所属站点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属站点不能为空")
    @Size(max = 64, message = "所属站点长度不能超过64个字符")
    private String stationId;

    @Schema(description = "编制人用户 ID", example = "1")
    private Long prepareUserId;

    @Schema(description = "编制人姓名")
    @Size(max = 64, message = "编制人姓名长度不能超过64个字符")
    private String prepareUserName;

    @Schema(description = "编制单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "编制单位不能为空")
    @Size(max = 128, message = "编制单位长度不能超过128个字符")
    private String prepareOrgName;

    @Schema(description = "编制时间", example = "2026-02-27 08:30:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime prepareTime;

    @Schema(description = "方案状态（0草稿 1已完成 2已归档 3已作废）", example = "0")
    @InEnum(value = IotDispatchPlanStatusEnum.class, message = "方案状态必须是 {value}")
    private Integer planStatus;

    @Schema(description = "核心建议目标", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "核心建议目标不能为空")
    @Size(max = 200, message = "核心建议目标长度不能超过200个字符")
    private String coreTarget;

    @Schema(description = "涉及工程", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "涉及工程不能为空")
    @Size(max = 200, message = "涉及工程长度不能超过200个字符")
    private String projectName;

    @Schema(description = "预期效果分析")
    private String expectedEffect;

    @Schema(description = "编制说明")
    private String prepareDesc;

    @Schema(description = "附件 URL 列表")
    private List<String> attachments;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Schema(description = "操作对象列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "操作对象不能为空")
    @Valid
    private List<IotDispatchPlanObjectSaveReqVO> objects;
}
