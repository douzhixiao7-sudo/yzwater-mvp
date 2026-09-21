package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 幸福河湖平台-按区域查询视频点位请求。
 */
@Data
@Schema(description = "幸福河湖平台-按区域查询视频点位请求")
public class XfhhVideoCameraByRegionListReqVO {

    @Schema(description = "外部平台数据源编号", example = "fb8cd0fe-addf-4364-82a1-8a0b11a9440c")
    private String id;

    @Valid
    @NotNull(message = "params 不能为空")
    @Schema(description = "查询参数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Params params;

    /**
     * 查询参数。
     */
    @Data
    @Schema(name = "XfhhVideoCameraByRegionListParams", description = "幸福河湖平台-按区域查询视频点位参数")
    public static class Params {

        @Schema(description = "区域编码，不传时兼容旧脚本默认区域", example = "e26d0d02a5654932abab42bdf216a066")
        private String regionIndexCode;
    }
}
