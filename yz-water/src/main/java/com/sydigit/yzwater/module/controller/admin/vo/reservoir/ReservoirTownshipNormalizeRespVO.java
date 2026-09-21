package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 水库乡镇字段规范化结果
 */
@Data
public class ReservoirTownshipNormalizeRespVO {

    @Schema(description = "本次扫描水库数量")
    private Long totalCount = 0L;

    @Schema(description = "已更新数量")
    private Long updatedCount = 0L;

    @Schema(description = "未更新数量")
    private Long skippedCount = 0L;

    @Schema(description = "未匹配到区划编码的乡镇名称数量")
    private Long unmatchedCount = 0L;

    @Schema(description = "未匹配到区划编码的乡镇名称列表")
    private List<String> unmatchedNames = new ArrayList<>();

    @Schema(description = "未匹配到管理单位字典值的名称数量")
    private Long unmatchedManagementUnitCount = 0L;

    @Schema(description = "未匹配到管理单位字典值的名称列表")
    private List<String> unmatchedManagementUnitNames = new ArrayList<>();
}
