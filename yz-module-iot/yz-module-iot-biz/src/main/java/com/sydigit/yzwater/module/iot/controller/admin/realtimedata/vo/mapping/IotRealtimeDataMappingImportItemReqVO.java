package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - IoT 实时数据点位映射批量导入项 Request VO")
@Data
public class IotRealtimeDataMappingImportItemReqVO {

    @Schema(description = "点位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "PH")
    @NotEmpty(message = "点位名称不能为空")
    private String pointName;

    @Schema(description = "标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "ph")
    @NotEmpty(message = "标识符不能为空")
    private String identifier;

}
