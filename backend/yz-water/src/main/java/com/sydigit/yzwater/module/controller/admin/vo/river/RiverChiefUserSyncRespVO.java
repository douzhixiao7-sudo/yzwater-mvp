package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 河长账号同步结果
 */
@Data
public class RiverChiefUserSyncRespVO {

    @Schema(description = "当前有效河长记录数（effectiveTo为空且isCurrent=1）")
    private Integer totalChiefCount = 0;

    @Schema(description = "需要同步的账号数（按河长联系电话去重）")
    private Integer totalAccountCount = 0;

    @Schema(description = "创建用户数")
    private Integer createdCount = 0;

    @Schema(description = "更新用户数")
    private Integer updatedCount = 0;

    @Schema(description = "跳过数量")
    private Integer skipCount = 0;

    @Schema(description = "失败数量")
    private Integer failureCount = 0;

    @Schema(description = "提示/错误信息列表")
    private List<String> messages = new ArrayList<>();

    public void addCreated(String message) {
        this.messages.add(message);
        this.createdCount = this.createdCount + 1;
    }

    public void addUpdated(String message) {
        this.messages.add(message);
        this.updatedCount = this.updatedCount + 1;
    }

    public void addSkip(String message) {
        this.messages.add(message);
        this.skipCount = this.skipCount + 1;
    }

    public void addFailure(String message) {
        this.messages.add(message);
        this.failureCount = this.failureCount + 1;
    }
}

