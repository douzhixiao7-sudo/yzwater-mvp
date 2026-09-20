package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 水库导入结果
 */
@Data
public class ReservoirImportRespVO {

    @Schema(description = "总行数")
    private Integer totalCount = 0;

    @Schema(description = "成功行数")
    private Integer successCount = 0;

    @Schema(description = "失败行数")
    private Integer failureCount = 0;

    @Schema(description = "失败原因列表")
    private List<String> errors = new ArrayList<>();

    public void addError(String message) {
        this.errors.add(message);
        this.failureCount = this.errors.size();
    }
}
