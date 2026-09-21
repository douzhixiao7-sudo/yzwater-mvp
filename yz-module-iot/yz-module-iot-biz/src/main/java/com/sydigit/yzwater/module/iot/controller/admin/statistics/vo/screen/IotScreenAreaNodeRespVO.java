package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "大屏 - 行政区划树节点（仅结构，不含面；与 /system/area/tree 一致）")
@Data
public class IotScreenAreaNodeRespVO {

    @Schema(description = "区划 id（字符串，避免前端精度问题；用作 MVT areaId / village_code）", example = "321081104")
    private String id;

    @Schema(description = "区划名称", example = "真州镇")
    private String name;

    @Schema(description = "级别（与 system_area.type 一致，常见：市/镇/村）", example = "4")
    private Integer type;

    @Schema(description = "子节点")
    private List<IotScreenAreaNodeRespVO> children;

}
