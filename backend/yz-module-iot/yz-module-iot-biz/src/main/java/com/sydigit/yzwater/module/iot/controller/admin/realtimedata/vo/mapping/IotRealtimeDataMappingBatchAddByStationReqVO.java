package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 站点设备批量新增点位映射 Request VO")
@Data
public class IotRealtimeDataMappingBatchAddByStationReqVO {

    @Schema(description = "采集源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "采集源编号不能为空")
    private Long sourceId;

    @Schema(description = "站点编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZD001")
    @NotBlank(message = "站点编号不能为空")
    private String stationId;

}
