package com.sydigit.yzwater.module.controller.admin.vo.flood;

import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNull;

class FxWzSaveReqVOMaterialTypeOptionalTest {

    @Test
    void materialTypeShouldNotBeRequired() throws Exception {
        Field field = FxWzSaveReqVO.class.getDeclaredField("materialType");
        assertNull(field.getAnnotation(NotBlank.class));
    }
}
