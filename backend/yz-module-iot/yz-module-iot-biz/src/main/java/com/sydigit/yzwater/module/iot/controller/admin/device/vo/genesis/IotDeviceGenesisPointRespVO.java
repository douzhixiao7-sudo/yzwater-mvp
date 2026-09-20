package com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备 GENESIS64 点位配置 Response VO")
@Data
public class IotDeviceGenesisPointRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "物模型属性编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long thingModelId;

    @Schema(description = "属性标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "switch01")
    private String identifier;

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "开关量01")
    private String name;

    @Schema(description = "GENESIS64 点位名称", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "modbus:3号闸.开入量06")
    private String pointName;

    @Schema(description = "排序号", example = "10")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
