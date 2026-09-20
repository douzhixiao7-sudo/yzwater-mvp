package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 字典项（仅返回 label/value）
 */
@Data
public class BigScreenDictLabelValueRespVO {

    @Schema(description = "字典项文本")
    private String label;

    @Schema(description = "字典项值")
    private String value;
}

