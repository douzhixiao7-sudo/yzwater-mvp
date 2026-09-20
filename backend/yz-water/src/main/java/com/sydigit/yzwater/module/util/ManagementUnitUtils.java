package com.sydigit.yzwater.module.util;

import cn.hutool.core.util.StrUtil;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 管理单位字段解析工具。
 *
 * <p>managementUnit 数据库存储为 PostgreSQL 的 text[]，因此需要将来源字符串按常见分隔符拆分为数组。</p>
 */
public final class ManagementUnitUtils {

    private ManagementUnitUtils() {
    }

    /**
     * 解析“管理单位”字符串为数组。
     *
     * <p>兼容常见分隔符：英文逗号、中文逗号、顿号、分号、竖线、斜杠。</p>
     */
    public static String[] parseManagementUnit(String input) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        String normalized = input.trim();
        // 兼容部分数据以 JSON/数组字面量保存的情况，例如：["a","b"] 或 {a,b}
        if ((normalized.startsWith("[") && normalized.endsWith("]"))
                || (normalized.startsWith("{") && normalized.endsWith("}"))) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        normalized = normalized
                .replace('，', ',')
                .replace('、', ',')
                .replace('；', ',')
                .replace(';', ',')
                .replace('|', ',')
                .replace('/', ',');

        String[] parts = normalized.split(",");
        Set<String> result = new LinkedHashSet<>();
        for (String part : parts) {
            String item = normalizeManagementUnitItem(part);
            if (item != null) {
                result.add(item);
            }
        }
        return result.isEmpty() ? null : result.toArray(new String[0]);
    }

    private static String normalizeManagementUnitItem(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String item = text.trim();
        // 去掉常见包裹字符，避免导入时把引号也存进去
        item = StrUtil.removePrefix(item, "\"");
        item = StrUtil.removeSuffix(item, "\"");
        item = StrUtil.removePrefix(item, "'");
        item = StrUtil.removeSuffix(item, "'");

        // 仅由占位符组成的内容视为无效（例如：/、—、_）
        if (item.matches("^[/＿_—\\-]+$")) {
            return null;
        }
        return StrUtil.isBlank(item) ? null : item;
    }
}

