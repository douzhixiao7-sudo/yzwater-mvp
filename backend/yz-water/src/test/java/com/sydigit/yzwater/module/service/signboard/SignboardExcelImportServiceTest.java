package com.sydigit.yzwater.module.service.signboard;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignboardExcelImportServiceTest {

    @Test
    void shouldResolveSignboardLevelValueByExactAndFuzzyLabel() throws Exception {
        SignboardExcelImportService service = new SignboardExcelImportService(null, null, null, null, null, null);
        Method method = SignboardExcelImportService.class.getDeclaredMethod(
                "resolveSignboardLevelValue", String.class, Map.class);
        method.setAccessible(true);

        Map<String, String> levelMap = new HashMap<>();
        levelMap.put("省级", "province");
        levelMap.put("市级", "city");

        assertEquals("province", method.invoke(service, "省级", levelMap));
        assertEquals("city", method.invoke(service, "市级河长", levelMap));
        assertNull(method.invoke(service, "", levelMap));
    }

    @Test
    void shouldThrowWhenSignboardLevelLabelCannotBeMatched() throws Exception {
        SignboardExcelImportService service = new SignboardExcelImportService(null, null, null, null, null, null);
        Method method = SignboardExcelImportService.class.getDeclaredMethod(
                "resolveSignboardLevelValue", String.class, Map.class);
        method.setAccessible(true);

        Map<String, String> levelMap = Map.of("省级", "province");

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> method.invoke(service, "未知级别", levelMap));
        assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        assertEquals("公示牌等级【未知级别】未匹配到字典 zd_hljb.label", ex.getCause().getMessage());
    }
}
