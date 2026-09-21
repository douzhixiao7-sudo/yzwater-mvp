package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 实时数据点位映射批量导入 Request VO")
@Data
public class IotRealtimeDataMappingBatchImportReqVO {

    @Schema(description = "采集源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "采集源编号不能为空")
    private Long sourceId;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "待导入点位列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "待导入点位不能为空")
    @Valid
    private List<IotRealtimeDataMappingImportItemReqVO> items;

}
