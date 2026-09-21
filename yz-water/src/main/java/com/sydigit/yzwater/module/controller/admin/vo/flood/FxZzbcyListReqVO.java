package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 防汛抗旱组织部成员列表 Request VO
 */
@Data
@Schema(description = "管理后台 - 防汛抗旱组织部成员列表 Request VO")
public class FxZzbcyListReqVO {

    @Schema(description = "姓名（模糊）")
    private String name;

    @Schema(description = "职务（模糊）")
    private String title;
}
