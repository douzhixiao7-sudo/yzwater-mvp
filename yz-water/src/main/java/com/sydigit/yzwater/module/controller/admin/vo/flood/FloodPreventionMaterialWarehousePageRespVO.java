package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 防汛物资仓库分页返回
 */
@Data
@Schema(description = "管理后台 - 防汛物资仓库分页返回 Response VO")
public class FloodPreventionMaterialWarehousePageRespVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "具体位置")
    private String specificLocation;

    @Schema(description = "归属单位")
    private String belongUnit;

    @Schema(description = "负责人姓名")
    private String leaderName;

    @Schema(description = "负责人电话")
    private String leaderPhone;

    @Schema(description = "行政划分（text[]）")
    private List<String> divisionCode;
}

