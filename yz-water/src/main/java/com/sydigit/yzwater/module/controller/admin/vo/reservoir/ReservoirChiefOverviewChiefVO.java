package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 水库页 - 河长概览项
 */
@Data
@Schema(description = "仪征管理后台 - 水库页河长概览项")
public class ReservoirChiefOverviewChiefVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长级别")
    private String headLevel;

    @Schema(description = "河长级别中文")
    private String headLevelLabel;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "联系电话")
    private String headContact;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveFrom;
}
