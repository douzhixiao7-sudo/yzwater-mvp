package com.sydigit.yzwater.module.controller.admin.vo.river;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNull;

class RiverChannelSaveReqVOOptionalFieldsTest {

    @Test
    void riverLevelEcologyTypeAndBasinTypeShouldNotBeRequired() throws Exception {
        assertOptional("riverLevel");
        assertOptional("ecologyType");
        assertOptional("basinType");
    }

    private void assertOptional(String fieldName) throws Exception {
        Field field = RiverChannelSaveReqVO.class.getDeclaredField(fieldName);
        assertNull(field.getAnnotation(NotBlank.class));
        assertNull(field.getAnnotation(NotNull.class));
        assertNull(field.getAnnotation(NotEmpty.class));
    }
}
