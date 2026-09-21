package com.sydigit.yzwater.module.service.signboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardPageRespVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardDO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SignboardPageRespMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldExposeSignboardLevelAndLabelOnPageResp() throws Exception {
        SignboardService service = new SignboardService(null, null, null, null, null);
        YzSignboardDO item = new YzSignboardDO();
        invokeSetterIfPresent(item, "setSignboardLevel", "6j");

        SignboardPageRespVO resp = invokeBuildPageResp(service, item, Map.of("6j", "6级"));
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(resp));

        assertNotNull(json.get("signboardLevel"), "分页响应应包含 signboardLevel 字段");
        assertEquals("6j", json.get("signboardLevel").asText());
        assertNotNull(json.get("signboardLevelLabel"), "分页响应应包含 signboardLevelLabel 字段");
        assertEquals("6级", json.get("signboardLevelLabel").asText());
    }

    private SignboardPageRespVO invokeBuildPageResp(SignboardService service,
                                                    YzSignboardDO item,
                                                    Map<String, String> riverLevelMap) throws Exception {
        Method method = SignboardService.class.getDeclaredMethod("buildPageResp",
                YzSignboardDO.class, Map.class, Map.class, Map.class, Map.class, Map.class);
        method.setAccessible(true);
        return (SignboardPageRespVO) method.invoke(service, item,
                riverLevelMap, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    private void invokeSetterIfPresent(Object target, String methodName, Object value) throws Exception {
        try {
            Method method = target.getClass().getMethod(methodName, value.getClass());
            method.invoke(target, value);
        } catch (NoSuchMethodException ignored) {
            // 目标字段尚未实现时保持空值，让测试先失败
        }
    }
}
