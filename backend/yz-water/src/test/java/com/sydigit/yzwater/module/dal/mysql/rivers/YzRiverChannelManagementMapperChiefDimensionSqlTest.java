package com.sydigit.yzwater.module.dal.mysql.rivers;

import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class YzRiverChannelManagementMapperChiefDimensionSqlTest {

    @Test
    void shouldGroupChiefPageAndCountByHeadNameHeadLevelAndHeadPosition() throws Exception {
        String countSql = readSelectSql(YzRiverChannelManagementMapper.class
                .getMethod("countCurrentChiefGroup", String.class, List.class, String.class, String.class, String.class));
        String pageSql = readSelectSql(YzRiverChannelManagementMapper.class
                .getMethod("selectCurrentChiefGroupPage", String.class, List.class, String.class, String.class,
                        String.class, int.class, int.class));

        assertTrue(extractLastGroupByClause(countSql).contains("m.head_position"),
                "河长信息总数统计应按“姓名 + 级别 + 职务”分组");
        assertTrue(extractLastGroupByClause(pageSql).contains("m.head_position"),
                "河长信息分页查询应按“姓名 + 级别 + 职务”分组");
        assertTrue(extractLastGroupByClause(countSql).contains("m.head_level"),
                "河长信息总数统计应包含河长级别");
        assertTrue(extractLastGroupByClause(pageSql).contains("m.head_level"),
                "河长信息分页查询应包含河长级别");
        String countGb = extractLastGroupByClause(countSql);
        assertTrue(!countGb.contains("m.effective_from"),
                "河长信息合并展示不应按 effective_from 拆分列表行");
    }

    @Test
    void shouldRequireHeadLevelAndHeadPositionWhenSelectingCurrentChiefDimension() throws Exception {
        Method method = YzRiverChannelManagementMapper.class
                .getMethod("selectCurrentByChiefDimension", String.class, String.class, String.class);
        String sql = readSelectSql(method);

        assertTrue(normalizeSql(sql).contains("#{headlevel"),
                "按河长维度回查当前记录时必须同时使用河长级别");
        assertTrue(normalizeSql(sql).contains("#{headposition"),
                "按河长维度回查当前记录时必须同时使用河长职务");
    }

    @Test
    void shouldUseReferenceTypeMarkerToExcludeAndSelectTotalChief() throws Exception {
        String countSql = readSelectSql(YzRiverChannelManagementMapper.class
                .getMethod("countCurrentChiefGroup", String.class, List.class, String.class, String.class, String.class));
        String totalChiefSql = readSelectSql(YzRiverChannelManagementMapper.class
                .getMethod("selectCurrentTotalChiefs"));

        String normalizedCountSql = normalizeSql(countSql);
        assertTrue(normalizedCountSql.contains("total_chief") && normalizedCountSql.contains("not ("),
                "普通河长分页统计应通过 reference_type 显式排除总河长");
        String normalizedTotalChiefSql = normalizeSql(totalChiefSql);
        assertTrue(normalizedTotalChiefSql.contains("reference_type, '') = 'total_chief'"),
                "总河长查询应通过 reference_type 显式识别总河长");
        assertTrue(!normalizedTotalChiefSql.contains(" or "),
                "总河长查询不应再兼容历史空字段记录");
    }

    private String readSelectSql(Method method) {
        Select select = method.getAnnotation(Select.class);
        return String.join(" ", select.value());
    }

    private String extractLastGroupByClause(String sql) {
        String normalized = normalizeSql(sql);
        int index = normalized.lastIndexOf("group by");
        return index >= 0 ? normalized.substring(index) : normalized;
    }

    private String normalizeSql(String sql) {
        return sql.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
