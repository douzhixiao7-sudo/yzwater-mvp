package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河长账号同步请求
 */
@Data
public class RiverChiefUserSyncReqVO {

    @Schema(description = "归属部门ID；不传则默认取当前登录用户的部门")
    private Long deptId;
}

