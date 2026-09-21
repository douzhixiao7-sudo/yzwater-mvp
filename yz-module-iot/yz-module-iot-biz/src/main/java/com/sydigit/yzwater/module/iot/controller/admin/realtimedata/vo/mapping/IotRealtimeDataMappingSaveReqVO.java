package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 实时数据点位映射新增/修改 Request VO")
@Data
public class IotRealtimeDataMappingSaveReqVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "采集源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "采集源编号不能为空")
    private Long sourceId;

    @Schema(description = "点位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "modbus:xxx")
    @NotEmpty(message = "点位名称不能为空")
    private String pointName;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "物模型标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "ph")
    @NotEmpty(message = "物模型标识符不能为空")
    private String identifier;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "排序", example = "0")
    private Integer sort;

}