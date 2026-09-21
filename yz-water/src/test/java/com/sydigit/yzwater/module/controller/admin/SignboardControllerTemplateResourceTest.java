package com.sydigit.yzwater.module.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignboardControllerTemplateResourceTest {

    @Test
    void shouldResolveImportTemplateFromClasspath() throws Exception {
        SignboardController controller = new SignboardController(null, null);
        Method method = SignboardController.class.getDeclaredMethod("resolveTemplateResource");
        method.setAccessible(true);

        Resource resource = (Resource) method.invoke(controller);

        assertTrue(resource.exists());
        assertEquals("公示牌导入模版.xlsx", resource.getFilename());
        assertTrue(resource.contentLength() > 0);
    }
}
