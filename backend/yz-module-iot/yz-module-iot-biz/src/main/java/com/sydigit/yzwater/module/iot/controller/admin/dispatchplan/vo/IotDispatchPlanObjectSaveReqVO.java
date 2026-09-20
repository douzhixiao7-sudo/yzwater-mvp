package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.dispatchplan.IotDispatchPlanObjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 调度方案操作对象新增/修改 Request VO
 */
@Schema(description = "IoT - 调度方案操作对象新增/修改 Request VO")
@Data
public class IotDispatchPlanObjectSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "对象类型（1设备 2自定义）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "对象类型不能为空")
    @InEnum(value = IotDispatchPlanObjectTypeEnum.class, message = "对象类型必须是 {value}")
    private Integer objectType;

    @Schema(description = "操作对象名称（自定义对象必填）")
    @Size(max = 128, message = "操作对象名称长度不能超过128个字符")
    private String objectName;

    @Schema(description = "设备 ID（设备对象必填）", example = "1001")
    private Long deviceId;

    @Schema(description = "安装位置 ID（可选）", example = "2001")
    private Long locationId;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Schema(description = "参数列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "控制参数不能为空")
    @Valid
    private List<IotDispatchPlanParamSaveReqVO> params;
}

