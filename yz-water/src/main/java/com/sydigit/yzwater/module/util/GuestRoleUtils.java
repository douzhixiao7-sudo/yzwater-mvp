package com.sydigit.yzwater.module.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Collection;

/**
 * 游客角色判定工具。
 * <p>
 * 纯游客：拥有游客角色，且除游客外没有其他业务角色。
 * 同时拥有游客与内部业务角色的用户，仍按内部人员处理。
 */
public final class GuestRoleUtils {

    public static final String ROLE_GUEST = "GUEST";

    private GuestRoleUtils() {
    }

    public static boolean isGuestRoleCode(String roleCode) {
        return StrUtil.isNotBlank(roleCode) && ROLE_GUEST.equalsIgnoreCase(StrUtil.trim(roleCode));
    }

    /**
     * 是否为纯游客（仅有游客角色，或除游客外无其他业务角色）。
     */
    public static boolean isPureGuest(Collection<String> roleCodes) {
        if (CollUtil.isEmpty(roleCodes)) {
            return false;
        }
        boolean hasGuest = false;
        boolean hasNonGuest = false;
        for (String roleCode : roleCodes) {
            if (StrUtil.isBlank(roleCode)) {
                continue;
            }
            if (isGuestRoleCode(roleCode)) {
                hasGuest = true;
            } else {
                hasNonGuest = true;
            }
        }
        return hasGuest && !hasNonGuest;
    }
}
