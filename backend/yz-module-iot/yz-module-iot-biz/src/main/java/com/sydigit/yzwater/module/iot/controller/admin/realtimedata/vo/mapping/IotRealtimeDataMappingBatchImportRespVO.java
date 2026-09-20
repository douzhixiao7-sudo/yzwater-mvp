package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 实时数据点位映射批量导入 Response VO")
@Data
public class IotRealtimeDataMappingBatchImportRespVO {

    @Schema(description = "待导入总数", example = "10")
    private Integer totalCount;

    @Schema(description = "成功导入数", example = "8")
    private Integer importedCount;

    @Schema(description = "跳过数量", example = "2")
    private Integer skippedCount;

}
