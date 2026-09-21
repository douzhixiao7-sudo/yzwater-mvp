package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 河道简要信息返回
 */
@Schema(description = "仪征管理后台 - 河道简要信息返回")
@Data
public class RiverChannelSimpleRespVO {

    @Schema(description = "河道ID")
    private Long id;

    @Schema(description = "河道编码")
    private String riverCode;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
