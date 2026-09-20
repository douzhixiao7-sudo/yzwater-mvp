package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * yz_base_reservoir 同步结果
 */
@Data
public class ReservoirBaseSyncRespVO {

    @Schema(description = "处理总数")
    private Integer total = 0;

    @Schema(description = "因名称包含“大坝”而跳过的数量")
    private Integer nameFilteredCount = 0;

    @Schema(description = "因缺少编码或名称而跳过的数量")
    private Integer dataSkippedCount = 0;

    @Schema(description = "更新几何数量")
    private Integer updatedCount = 0;

    @Schema(description = "新增数量")
    private Integer insertedCount = 0;

    @Schema(description = "因缺少几何而跳过的数量")
    private Integer geometrySkipped = 0;

    public void addUpdated() {
        this.updatedCount++;
    }

    public void addInserted() {
        this.insertedCount++;
    }

    public void addGeometrySkipped() {
        this.geometrySkipped++;
    }

    public void addNameFiltered() {
        this.nameFilteredCount++;
    }

    public void addDataSkipped() {
        this.dataSkippedCount++;
    }
}
