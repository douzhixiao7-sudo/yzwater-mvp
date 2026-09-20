package com.sydigit.yzwater.module.dal.mysql.system.dto;

import lombok.Data;

/**
 * 系统用户角色明细行
 */
@Data
public class SystemUserRoleRow {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色排序
     */
    private Integer roleSort;

    /**
     * 角色 ID
     */
    private Long roleId;
}
