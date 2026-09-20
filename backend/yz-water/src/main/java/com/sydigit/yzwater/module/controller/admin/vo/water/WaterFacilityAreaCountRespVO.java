package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 水利设施 - 按行政区划统计数量
 */
@Data
public class WaterFacilityAreaCountRespVO {

    @Schema(description = "行政区划编码（对应 /system/area/tree 的 id）", example = "321082")
    private Long areaId;

    @Schema(description = "设施数量", example = "12")
    private Long count;
}

