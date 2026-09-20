package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 水库分页查询请求
 */
@Schema(description = "仪征管理后台 - 水库分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReservoirPageReqVO extends PageParam {

    @Schema(description = "水库编码，支持模糊匹配")
    private String reservoirCode;

    @Schema(description = "水库名称，支持模糊匹配")
    private String reservoirName;

    @Schema(description = "规模（字典：zd_skgm）")
    private String reservoirScale;

    @Schema(description = "管理单位（字典：zd_gldw）")
    private String managementUnit;
}

