package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 调度方案对象参数新增/修改 Request VO
 */
@Schema(description = "IoT - 调度方案对象参数新增/修改 Request VO")
@Data
public class IotDispatchPlanParamSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "开度")
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称长度不能超过100个字符")
    private String paramName;

    @Schema(description = "参数值", requiredMode = Schema.RequiredMode.REQUIRED, example = "2.5")
    @NotBlank(message = "参数值不能为空")
    @Size(max = 200, message = "参数值长度不能超过200个字符")
    private String paramValue;

    @Schema(description = "参数单位", example = "m")
    @Size(max = 32, message = "参数单位长度不能超过32个字符")
    private String paramUnit;

    @Schema(description = "参数备注")
    @Size(max = 200, message = "参数备注长度不能超过200个字符")
    private String remark;
}

