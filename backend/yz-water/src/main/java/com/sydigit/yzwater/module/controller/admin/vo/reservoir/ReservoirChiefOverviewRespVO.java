package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 水库页 - 河长概览响应
 */
@Data
@Schema(description = "仪征管理后台 - 水库页河长概览响应")
public class ReservoirChiefOverviewRespVO {

    @Schema(description = "水库ID")
    private Long reservoirId;

    @Schema(description = "水库名称")
    private String reservoirName;

    @Schema(description = "当前有效河长列表")
    private List<ReservoirChiefOverviewChiefVO> reservoirChiefs;

    @Schema(description = "当前有效河长总数")
    private Integer totalCount;
}
