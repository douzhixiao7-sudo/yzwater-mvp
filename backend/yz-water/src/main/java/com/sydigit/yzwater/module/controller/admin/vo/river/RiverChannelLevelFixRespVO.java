package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河道级别字段修复返回
 */
@Data
public class RiverChannelLevelFixRespVO {

    @Schema(description = "将 riverLevel=xcjhd 修复为 7j 的数量")
    private Long xcjhdTo7jCount;

    @Schema(description = "将 riverLevel=xjhl 修复为 6j 的数量")
    private Long xjhlTo6jCount;

    @Schema(description = "本次总修复数量")
    private Long totalCount;
}

