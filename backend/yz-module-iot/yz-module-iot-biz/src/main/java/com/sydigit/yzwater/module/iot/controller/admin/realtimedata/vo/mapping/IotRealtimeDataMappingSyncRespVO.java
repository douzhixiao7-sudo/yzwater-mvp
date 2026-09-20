package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * IoT 实时数据点位映射同步结果
 */
@Data
public class IotRealtimeDataMappingSyncRespVO {

    @Schema(description = "设备编号")
    private Long deviceId;

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "点位名称已更新数量")
    private Integer updatedCount;

    @Schema(description = "物模型缺失导致禁用数量")
    private Integer disabledCount;

    @Schema(description = "标识符异常数量")
    private Integer invalidCount;

    @Schema(description = "无变化数量")
    private Integer unchangedCount;

}
