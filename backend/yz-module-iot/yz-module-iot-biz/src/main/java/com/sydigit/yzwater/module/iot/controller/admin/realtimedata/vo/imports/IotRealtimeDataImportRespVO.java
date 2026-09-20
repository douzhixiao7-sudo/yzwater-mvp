package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 实时数据配置导入 Response VO")
@Data
public class IotRealtimeDataImportRespVO {

    @Schema(description = "采集源编号", example = "1024")
    private Long sourceId;

    @Schema(description = "采集源名称", example = "潘家河")
    private String sourceName;

    @Schema(description = "导入点位数量", example = "10")
    private Integer mappingCount;

}