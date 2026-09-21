package com.sydigit.yzwater.module.service.river;

import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RiverChiefManagementServiceRecordFacilityKeyTest {

    @Test
    void shouldBuildReservoirFacilityKeyFromRecordWithoutLoadingReservoir() throws Exception {
        RiverChiefManagementService service = new RiverChiefManagementService(null, null, null, null, null, null);
        YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
        record.setReferenceType(ReferenceTypeConstants.RESERVOIR);
        record.setReferenceId(2001868668741083136L);
        record.setWaterReservoirId(2001868668741083136L);

        Object key = invokeResolveFacilityKeyFromRecord(service, record);

        assertNotNull(key);
        assertEquals(ReferenceTypeConstants.RESERVOIR, readField(key, "referenceType"));
        assertEquals(2001868668741083136L, readField(key, "referenceId"));
    }

    @Test
    void shouldKeepRiverChannelIdWhenBuildingRiverSectionFacilityKeyFromRecord() throws Exception {
        RiverChiefManagementService service = new RiverChiefManagementService(null, null, null, null, null, null);
        YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
        record.setReferenceType(ReferenceTypeConstants.RIVER_SECTION);
        record.setReferenceId(2001L);
        record.setRiverSectionId(2001L);
        record.setRiverChannelId(1001L);

        Object key = invokeResolveFacilityKeyFromRecord(service, record);

        assertNotNull(key);
        assertEquals(ReferenceTypeConstants.RIVER_SECTION, readField(key, "referenceType"));
        assertEquals(2001L, readField(key, "referenceId"));
        assertEquals(1001L, readField(key, "riverChannelId"));
    }

    @Test
    void shouldBuildReservoirReferenceDetailWithoutLoadingDeletedReservoir() throws Exception {
        RiverChiefManagementService service = new RiverChiefManagementService(null, null, null, null, null, null);
        Object key = newFacilityKey(ReferenceTypeConstants.RESERVOIR, 2001868668741083136L);

        Object detail = invokeLoadReferenceDetail(service, key);

        assertNotNull(detail);
        assertEquals(ReferenceTypeConstants.RESERVOIR, readField(detail, "referenceType"));
        assertEquals(2001868668741083136L, readField(detail, "referenceId"));
        assertEquals(2001868668741083136L, readField(detail, "waterReservoirId"));
    }

    private Object invokeResolveFacilityKeyFromRecord(RiverChiefManagementService service,
                                                      YzRiverChannelManagementDO record) throws Exception {
        Method method = RiverChiefManagementService.class
                .getDeclaredMethod("resolveFacilityKeyFromRecord", YzRiverChannelManagementDO.class);
        method.setAccessible(true);
        return method.invoke(service, record);
    }

    private Object invokeLoadReferenceDetail(RiverChiefManagementService service, Object key) throws Exception {
        Method method = RiverChiefManagementService.class
                .getDeclaredMethod("loadReferenceDetail", key.getClass());
        method.setAccessible(true);
        return method.invoke(service, key);
    }

    private Object newFacilityKey(String referenceType, Long referenceId) throws Exception {
        Class<?> keyClass = Class.forName(
                "com.sydigit.yzwater.module.service.river.RiverChiefManagementService$FacilityKey");
        var constructor = keyClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object key = constructor.newInstance();
        writeField(key, "referenceType", referenceType);
        writeField(key, "referenceId", referenceId);
        return key;
    }

    private Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private void writeField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
