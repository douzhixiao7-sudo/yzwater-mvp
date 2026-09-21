package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 字典分组计数项
 */
@Data
@Schema(description = "大屏统计 - 字典分组计数项")
public class BigScreenDictCountItemVO {

    @Schema(description = "字典值")
    private String value;

    @Schema(description = "字典标签")
    private String label;

    @Schema(description = "数量")
    private Long count;
}

