package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 河道流经地区/乡镇 Excel 导入结果
 */
@Data
public class RiverChannelAreaExcelImportRespVO {

    @Schema(description = "总行数")
    private Integer totalCount = 0;

    @Schema(description = "更新成功的河道数量")
    private Integer successCount = 0;

    @Schema(description = "跳过行数（例如河道不存在）")
    private Integer skipCount = 0;

    @Schema(description = "失败行数（例如区划未匹配）")
    private Integer failureCount = 0;

    @Schema(description = "失败原因列表")
    private List<String> errors = new ArrayList<>();

    public void addSkip(String message) {
        this.errors.add(message);
        this.skipCount = this.skipCount + 1;
    }

    public void addFailure(String message) {
        this.errors.add(message);
        this.failureCount = this.failureCount + 1;
    }
}
