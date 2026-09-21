package com.sydigit.yzwater.module.iot.controller.admin.device.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 设备属性标签批量保存 Request VO")
@Data
public class IotDevicePropertyTagSaveReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "标签列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "标签列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "属性标识符不能为空")
        private String identifier;

        @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "标签名称不能为空")
        private String tagName;

    }

}
