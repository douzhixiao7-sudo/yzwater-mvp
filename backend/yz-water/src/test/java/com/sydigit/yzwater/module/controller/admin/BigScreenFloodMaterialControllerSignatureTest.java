package com.sydigit.yzwater.module.controller.admin;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigScreenFloodMaterialControllerSignatureTest {

    @Test
    void getMaterialListByTypeShouldNotRequireRequestParam() throws Exception {
        Method method = BigScreenFloodMaterialController.class.getDeclaredMethod("getMaterialListByType");
        assertEquals(0, method.getParameterCount());
    }
}
