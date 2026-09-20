package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "大屏 - 行政区划详情（与 /system/area/get 一致，id 为字符串）")
@Data
public class IotScreenAreaRespVO {

    @Schema(description = "区划 id（字符串）", example = "321081104")
    private String id;

    @Schema(description = "父级 id（字符串）", example = "321081")
    private String parentId;

    @Schema(description = "区划名称", example = "真州镇")
    private String name;

    @Schema(description = "级别", example = "4")
    private Integer type;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "空间信息 WKT（SRID=4490）")
    private String gemo;

    @Schema(description = "空间信息 GeoJSON 字符串（SRID=4490），前端 JSON.parse 后 geoJSON 渲染")
    private String gemoGeoJson;

}
