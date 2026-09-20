package com.sydigit.yzwater.module.service.river;

import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementSaveReqVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class RiverChiefManagementServiceCreateBehaviorTest {

    @Test
    void shouldIgnoreRequestPhoneWhenBuildHeadItemFromReq() throws Exception {
        RiverChiefManagementService service = new RiverChiefManagementService(null, null, null, null, null, null);
        RiverChiefManagementSaveReqVO reqVO = new RiverChiefManagementSaveReqVO();
        reqVO.setHeadName("张河长");
        reqVO.setHeadLevel("city");
        reqVO.setHeadPosition("河长");
        reqVO.setHeadUnit("水利局");
        reqVO.setHeadContact("13800000001");

        Method method = RiverChiefManagementService.class
                .getDeclaredMethod("buildHeadItemFromReq", RiverChiefManagementSaveReqVO.class);
        method.setAccessible(true);
        Object headItem = method.invoke(service, reqVO);

        Field field = headItem.getClass().getDeclaredField("headContact");
        field.setAccessible(true);
        assertNull(field.get(headItem));
    }

    @Test
    void shouldUseCurrentTimeWhenResolveEffectiveFrom() throws Exception {
        RiverChiefManagementService service = new RiverChiefManagementService(null, null, null, null, null, null);
        Method method = RiverChiefManagementService.class
                .getDeclaredMethod("resolveEffectiveFrom", LocalDate.class);
        method.setAccessible(true);

        LocalDateTime before = LocalDateTime.now();
        LocalDateTime actual = (LocalDateTime) method.invoke(service, LocalDate.of(2024, 1, 1));
        LocalDateTime after = LocalDateTime.now();

        assertFalse(actual.isBefore(before));
        assertFalse(actual.isAfter(after));
    }
}
