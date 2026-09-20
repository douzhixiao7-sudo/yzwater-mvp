package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 巡检标准新增/修改 Request VO")
@Data
public class IotInspectionStandardSaveReqVO {

    @Schema(description = "标准 ID", example = "1024")
    private Long id;

    @Schema(description = "标准名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标准名称不能为空")
    private String standardName;

    @Schema(description = "巡检类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "巡检类型不能为空")
    private String inspectionType;

    @Schema(description = "建议周期（字典 iot_inspection_period.value）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "建议检查周期不能为空")
    private String suggestCycleUnit;

    @Schema(description = "建议周期值，策略A固定为1")
    @Min(value = 1, message = "建议周期值必须大于等于 1")
    private Integer suggestCycleValue;

    @Schema(description = "所属站点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "状态（0 启用，1 停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "适用对象分组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "适用对象不能为空")
    @Valid
    private List<IotInspectionStandardTargetSaveReqVO> targets;

}
