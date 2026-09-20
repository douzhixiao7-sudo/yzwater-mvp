package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 防汛物资单位列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 防汛物资单位列表 Response VO")
public class FxWzDwListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "单位名称")
    private String unitName;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "是否代储(0-否 1-是)")
    private Integer isDelegateStorage;
}
