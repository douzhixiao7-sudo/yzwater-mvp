package com.sydigit.yzwater.module.controller.admin.vo.flood;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 防汛物资仓库分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 防汛物资仓库分页查询 Request VO")
public class FloodPreventionMaterialWarehousePageReqVO extends PageParam {

    @Schema(description = "仓库名称（模糊查询）")
    private String warehouseName;

    @Schema(description = "归属单位（模糊查询）")
    private String belongUnit;

    @Schema(description = "行政划分（单个节点筛选，数组中包含该值即可命中）")
    private String divisionCode;
}

