package com.sydigit.yzwater.module.controller.admin.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 公示牌 Excel 导入结果
 */
@Data
public class SignboardExcelImportRespVO {

    @Schema(description = "总行数（不含表头）")
    private Integer totalCount = 0;

    @Schema(description = "成功行数")
    private Integer successCount = 0;

    @Schema(description = "跳过行数（例如河道不存在）")
    private Integer skipCount = 0;

    @Schema(description = "失败行数（例如字典未匹配、坐标格式错误）")
    private Integer failureCount = 0;

    @Schema(description = "错误/跳过原因列表")
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

