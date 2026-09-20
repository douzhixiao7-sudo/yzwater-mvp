package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 最新河长信息导入结果
 */
@Data
public class RiverChiefLatestImportRespVO {

    @Schema(description = "解析到的河长信息条数")
    private Integer totalCount = 0;

    @Schema(description = "成功导入的河长记录条数")
    private Integer successCount = 0;

    @Schema(description = "失败的河长记录条数")
    private Integer failureCount = 0;

    @Schema(description = "跳过的说明")
    private List<String> skipMessages = new ArrayList<>();

    @Schema(description = "失败原因明细")
    private List<String> errors = new ArrayList<>();

    public void addTotal(int count) {
        this.totalCount = this.totalCount + Math.max(count, 0);
    }

    public void addSuccess(int count) {
        this.successCount = this.successCount + Math.max(count, 0);
    }

    public void addFailure(int count, String message) {
        this.failureCount = this.failureCount + Math.max(count, 0);
        if (message != null && !message.isBlank()) {
            this.errors.add(message);
        }
    }

    public void addSkip(String message) {
        if (message != null && !message.isBlank()) {
            this.skipMessages.add(message);
        }
    }
}
