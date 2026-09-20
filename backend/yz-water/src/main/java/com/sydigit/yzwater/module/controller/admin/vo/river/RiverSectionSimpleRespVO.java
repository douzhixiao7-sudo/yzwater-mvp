package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河段简单返回
 */
@Schema(description = "仪征管理后台 - 河段简单返回")
@Data
public class RiverSectionSimpleRespVO {

    @Schema(description = "河段ID")
    private Long id;

    @Schema(description = "河段名称")
    private String sectionName;
}
