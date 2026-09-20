package com.sydigit.yzwater.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 行政区划 GeoJSON 导入结果 Response VO")
@Data
public class AreaGeoJsonImportRespVO {

    @Schema(description = "解析到的要素数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer total;

    @Schema(description = "新增数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer created;

    @Schema(description = "更新数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer updated;

    @Schema(description = "跳过数量（数据不完整等）", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer skipped;

    @Schema(description = "错误信息列表（最多返回前 50 条）")
    private List<String> errors;
}

