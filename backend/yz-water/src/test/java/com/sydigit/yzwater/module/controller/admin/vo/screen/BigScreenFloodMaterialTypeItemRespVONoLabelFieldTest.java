package com.sydigit.yzwater.module.controller.admin.vo.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BigScreenFloodMaterialTypeItemRespVONoLabelFieldTest {

    @Test
    void shouldNotExposeMaterialTypeLabelField() {
        assertThrows(NoSuchFieldException.class,
                () -> BigScreenFloodMaterialTypeItemRespVO.class.getDeclaredField("materialTypeLabel"));
    }
}
