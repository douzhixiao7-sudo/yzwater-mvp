package com.sydigit.yzwater.module.iot.controller.admin.device.vo.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 设备位置树节点 Response VO")
@Data
public class IotDeviceLocationNodeRespVO {

    @Schema(description = "位置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "位置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号泵站")
    private String name;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    /**
     * 子节点
     */
    private List<IotDeviceLocationNodeRespVO> children;

}
