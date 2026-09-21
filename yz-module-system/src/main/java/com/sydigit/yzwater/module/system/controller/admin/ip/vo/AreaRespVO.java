package com.sydigit.yzwater.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 行政区划详情 Response VO")
@Data
public class AreaRespVO {

    @Schema(description = "行政区划编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "321081")
    private Long id;

    @Schema(description = "父级行政区划编码", example = "1")
    private Long parentId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "仪征市")
    private String name;

    @Schema(description = "类型（对应 Area.type）", example = "0")
    private Integer type;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "空间信息（WKT，SRID=4490）", example = "POINT(119.2 32.3)")
    private String gemo;

    @Schema(description = "空间信息（GeoJSON，SRID=4490）", example = "{\"type\":\"Point\",\"coordinates\":[119.2,32.3]}")
    private String gemoGeoJson;
}
