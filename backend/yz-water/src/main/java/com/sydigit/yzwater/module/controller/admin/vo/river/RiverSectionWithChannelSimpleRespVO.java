package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 河段简要信息（包含所属河道名称）
 */
@Schema(description = "仪征管理后台 - 河段简要信息（包含所属河道名称）")
@Data
public class RiverSectionWithChannelSimpleRespVO {

    @Schema(description = "河段ID")
    private Long id;

    @Schema(description = "所属河道ID")
    private Long riverChannelId;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河段名称")
    private String sectionName;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
