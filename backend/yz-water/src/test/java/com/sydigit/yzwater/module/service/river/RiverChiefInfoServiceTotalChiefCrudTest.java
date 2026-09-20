package com.sydigit.yzwater.module.service.river;

import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoTotalChiefSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RiverChiefInfoServiceTotalChiefCrudTest {

    @Test
    void shouldNotBindDeletedBooleanWhenCreateTotalChief() {
        AtomicReference<YzRiverChannelManagementDO> insertedRecord = new AtomicReference<>();
        RiverChiefInfoService service = buildService(insertedRecord, new AtomicReference<>(), buildCurrentTotalChiefRecord(1L));

        RiverChiefInfoTotalChiefSaveReqVO reqVO = new RiverChiefInfoTotalChiefSaveReqVO();
        reqVO.setHeadName("总河长甲");
        reqVO.setHeadLevel("city");
        reqVO.setHeadPosition("市委书记");

        service.createTotalChief(reqVO);

        YzRiverChannelManagementDO record = insertedRecord.get();
        assertNotNull(record);
        assertEquals("总河长甲", record.getHeadName());
        assertEquals("city", record.getHeadLevel());
        assertEquals("市委书记", record.getHeadPosition());
        assertEquals(ReferenceTypeConstants.TOTAL_CHIEF, record.getReferenceType());
        assertNull(record.getDeleted());
        assertNotNull(record.getEffectiveFrom());
    }

    @Test
    void shouldClearDeletedBeforeUpdateTotalChief() {
        AtomicReference<YzRiverChannelManagementDO> updatedRecord = new AtomicReference<>();
        YzRiverChannelManagementDO currentRecord = buildCurrentTotalChiefRecord(2L);
        currentRecord.setDeleted(false);
        RiverChiefInfoService service = buildService(new AtomicReference<>(), updatedRecord, currentRecord);

        RiverChiefInfoTotalChiefSaveReqVO reqVO = new RiverChiefInfoTotalChiefSaveReqVO();
        reqVO.setId(2L);
        reqVO.setHeadName("总河长乙");
        reqVO.setHeadLevel("district");
        reqVO.setHeadPosition("市长");

        service.updateTotalChief(reqVO);

        YzRiverChannelManagementDO record = updatedRecord.get();
        assertNotNull(record);
        assertEquals(2L, record.getId());
        assertEquals("总河长乙", record.getHeadName());
        assertEquals("district", record.getHeadLevel());
        assertEquals("市长", record.getHeadPosition());
        assertEquals(ReferenceTypeConstants.TOTAL_CHIEF, record.getReferenceType());
        assertNull(record.getDeleted());
        assertNotNull(record.getEffectiveFrom());
    }

    private RiverChiefInfoService buildService(AtomicReference<YzRiverChannelManagementDO> insertedRecord,
                                               AtomicReference<YzRiverChannelManagementDO> updatedRecord,
                                               YzRiverChannelManagementDO currentTotalChiefRecord) {
        YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class,
                (proxy, method, args) -> handleManagementMapper(method, args, insertedRecord, updatedRecord, currentTotalChiefRecord));
        YzRiverChannelMapper riverChannelMapper = proxy(YzRiverChannelMapper.class, this::handleDefaultInvocation);
        YzRiverSectionMapper riverSectionMapper = proxy(YzRiverSectionMapper.class, this::handleDefaultInvocation);
        YzWaterReservoirMapper waterReservoirMapper = proxy(YzWaterReservoirMapper.class, this::handleDefaultInvocation);
        return new RiverChiefInfoService(managementMapper, riverChannelMapper, riverSectionMapper, waterReservoirMapper, null);
    }

    private Object handleManagementMapper(Method method, Object[] args,
                                          AtomicReference<YzRiverChannelManagementDO> insertedRecord,
                                          AtomicReference<YzRiverChannelManagementDO> updatedRecord,
                                          YzRiverChannelManagementDO currentTotalChiefRecord) {
        String methodName = method.getName();
        if ("insert".equals(methodName) && args != null && args.length == 1) {
            insertedRecord.set((YzRiverChannelManagementDO) args[0]);
            return 1;
        }
        if ("updateById".equals(methodName) && args != null && args.length == 1) {
            updatedRecord.set((YzRiverChannelManagementDO) args[0]);
            return 1;
        }
        if ("selectById".equals(methodName) && args != null && args.length == 1) {
            Long id = (Long) args[0];
            if (currentTotalChiefRecord != null && Objects.equals(currentTotalChiefRecord.getId(), id)) {
                return currentTotalChiefRecord;
            }
            return null;
        }
        return handleDefaultInvocation(null, method, args);
    }

    private Object handleDefaultInvocation(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            if ("toString".equals(method.getName())) {
                return method.getDeclaringClass().getSimpleName();
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
        }
        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private YzRiverChannelManagementDO buildCurrentTotalChiefRecord(Long id) {
        YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
        record.setId(id);
        record.setHeadName("旧总河长");
        record.setHeadPosition("旧职务");
        record.setReferenceType(ReferenceTypeConstants.TOTAL_CHIEF);
        record.setIsCurrent(1);
        record.setDeleted(false);
        record.setEffectiveFrom(LocalDateTime.of(2026, 4, 16, 0, 0));
        return record;
    }
}
