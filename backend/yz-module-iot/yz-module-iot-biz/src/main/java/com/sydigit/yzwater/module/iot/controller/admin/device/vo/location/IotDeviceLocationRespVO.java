package com.sydigit.yzwater.module.iot.controller.admin.device.vo.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备位置 Response VO")
@Data
public class IotDeviceLocationRespVO {

    @Schema(description = "位置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "父级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

    @Schema(description = "位置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号泵站")
    private String name;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
