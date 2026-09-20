package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 站点设备批量新增点位映射 Response VO")
@Data
public class IotRealtimeDataMappingBatchAddByStationRespVO {

    @Schema(description = "站点设备数量", example = "5")
    private Integer deviceCount;

    @Schema(description = "本次新增点位数量", example = "60")
    private Integer importedCount;

    @Schema(description = "本次删除旧点位数量", example = "30")
    private Integer deletedCount;

    @Schema(description = "未新增点位的设备数量", example = "1")
    private Integer skippedDeviceCount;

}
