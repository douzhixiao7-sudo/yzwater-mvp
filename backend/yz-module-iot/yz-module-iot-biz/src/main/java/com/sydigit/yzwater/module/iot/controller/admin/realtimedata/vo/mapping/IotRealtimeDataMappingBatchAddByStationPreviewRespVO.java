package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 站点设备批量新增点位映射预览 Response VO")
@Data
public class IotRealtimeDataMappingBatchAddByStationPreviewRespVO {

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "水位计")
    private String deviceName;

    @Schema(description = "该设备当前点位数量", example = "8")
    private Integer existingCount;

    @Schema(description = "本次可新增点位数量", example = "12")
    private Integer importCount;

    @Schema(description = "可新增点位列表")
    private List<IotRealtimeDataMappingImportItemRespVO> items;

}
