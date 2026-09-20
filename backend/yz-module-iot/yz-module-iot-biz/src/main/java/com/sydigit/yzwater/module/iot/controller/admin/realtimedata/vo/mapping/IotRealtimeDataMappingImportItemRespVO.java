package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 实时数据点位映射批量导入项 Response VO")
@Data
public class IotRealtimeDataMappingImportItemRespVO {

    @Schema(description = "点位名称", example = "PH")
    private String pointName;

    @Schema(description = "标识符", example = "ph")
    private String identifier;

}
