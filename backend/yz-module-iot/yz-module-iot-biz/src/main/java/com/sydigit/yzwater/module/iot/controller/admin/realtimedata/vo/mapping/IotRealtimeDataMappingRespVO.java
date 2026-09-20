package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 实时数据点位映射 Response VO")
@Data
public class IotRealtimeDataMappingRespVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "采集源编号", example = "1024")
    private Long sourceId;

    @Schema(description = "点位名称", example = "modbus:xxx")
    private String pointName;

    @Schema(description = "设备编号", example = "2048")
    private Long deviceId;

    @Schema(description = "设备名称", example = "潘家河设备")
    private String deviceName;

    @Schema(description = "物模型标识符", example = "ph")
    private String identifier;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
