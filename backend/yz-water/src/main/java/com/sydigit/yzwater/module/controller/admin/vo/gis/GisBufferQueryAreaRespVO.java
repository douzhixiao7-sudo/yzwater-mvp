package com.sydigit.yzwater.module.controller.admin.vo.gis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "仪征管理后台 - 缓冲区涉及行政区")
@Data
public class GisBufferQueryAreaRespVO {

    @Schema(description = "行政区ID")
    private Long id;

    @Schema(description = "行政区名称")
    private String name;
}
