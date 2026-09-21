package com.sydigit.yzwater.module.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuestRoleUtilsTest {

    @Test
    void shouldTreatGuestOnlyUserAsPureGuest() {
        assertTrue(GuestRoleUtils.isPureGuest(List.of("GUEST")));
        assertTrue(GuestRoleUtils.isPureGuest(List.of("guest")));
    }

    @Test
    void shouldNotTreatGuestPlusBusinessRoleAsPureGuest() {
        assertFalse(GuestRoleUtils.isPureGuest(List.of("GUEST", "river_head")));
        assertFalse(GuestRoleUtils.isPureGuest(List.of("guest", "common")));
    }

    @Test
    void shouldNotTreatBusinessOnlyUserAsPureGuest() {
        assertFalse(GuestRoleUtils.isPureGuest(List.of("river_head")));
        assertFalse(GuestRoleUtils.isPureGuest(List.of()));
    }
}
