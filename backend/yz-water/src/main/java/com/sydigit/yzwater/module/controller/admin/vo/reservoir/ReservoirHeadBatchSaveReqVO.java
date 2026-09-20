package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 水库河长批量保存请求
 */
@Schema(description = "仪征管理后台 - 水库河长批量保存请求")
@Data
public class ReservoirHeadBatchSaveReqVO {

    @Schema(description = "水库ID")
    @NotNull(message = "水库ID不能为空")
    private Long waterReservoirId;

    @Schema(description = "关联对象ID（默认等于 waterReservoirId）")
    private Long referenceId;

    @Schema(description = "关联对象类型（reservoir：水库）")
    private String referenceType;

    @Schema(description = "河长列表")
    private List<ReservoirHeadItemSaveReqVO> heads;
}
