package com.sydigit.yzwater.module.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbankmentControllerTemplateResourceTest {

    @Test
    void shouldResolveImportTemplateFromClasspath() throws Exception {
        EmbankmentController controller = new EmbankmentController(null, null, null);
        Method method = EmbankmentController.class.getDeclaredMethod("resolveTemplateResource");
        method.setAccessible(true);

        Resource resource = (Resource) method.invoke(controller);

        assertTrue(resource.exists());
        assertEquals("堤防导入模版.xlsx", resource.getFilename());
        assertTrue(resource.contentLength() > 0);
    }
}
