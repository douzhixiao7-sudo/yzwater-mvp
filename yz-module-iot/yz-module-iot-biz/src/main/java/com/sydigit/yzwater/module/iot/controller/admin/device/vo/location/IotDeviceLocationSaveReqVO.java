package com.sydigit.yzwater.module.iot.controller.admin.device.vo.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备位置新增/修改 Request VO")
@Data
public class IotDeviceLocationSaveReqVO {

    @Schema(description = "位置编号", example = "1024")
    private Long id;

    @Schema(description = "父级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "父级位置不能为空")
    private Long parentId;

    @Schema(description = "位置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号泵站")
    @NotBlank(message = "位置名称不能为空")
    private String name;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
