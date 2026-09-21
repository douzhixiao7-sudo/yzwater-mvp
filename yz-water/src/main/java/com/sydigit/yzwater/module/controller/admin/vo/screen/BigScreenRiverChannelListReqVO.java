package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 河道列表查询入参（不分页）
 */
@Data
public class BigScreenRiverChannelListReqVO {

    @Schema(description = "河道名称（模糊查询）")
    private String riverName;

    @Schema(description = "河段名称（模糊查询，会回溯对应河道）")
    private String sectionName;

    @Schema(description = "生态类型（字典 zd_stlx 的 value）")
    private String ecologyType;
}
