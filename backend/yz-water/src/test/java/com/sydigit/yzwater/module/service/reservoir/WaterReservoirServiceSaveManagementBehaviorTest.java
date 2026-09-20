package com.sydigit.yzwater.module.service.reservoir;

import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirHeadBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirHeadItemSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WaterReservoirServiceSaveManagementBehaviorTest {

    @Test
    void shouldSaveReservoirChiefWithoutPhoneAndUseCurrentTime() {
        AtomicReference<YzRiverChannelManagementDO> insertedRecord = new AtomicReference<>();
        YzWaterReservoirMapper reservoirMapper = proxy(YzWaterReservoirMapper.class, (proxy, method, args) -> {
            if ("selectById".equals(method.getName()) && args != null && args.length == 1 && Objects.equals(args[0], 2001L)) {
                YzWaterReservoirDO reservoir = new YzWaterReservoirDO();
                reservoir.setId(2001L);
                reservoir.setReservoirName("三里港水库");
                return reservoir;
            }
            return handleDefaultInvocation(proxy, method, args);
        });
        YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class, (proxy, method, args) -> {
            if ("selectList".equals(method.getName())) {
                return List.of();
            }
            if ("insert".equals(method.getName()) && args != null && args.length == 1) {
                insertedRecord.set((YzRiverChannelManagementDO) args[0]);
                return 1;
            }
            return handleDefaultInvocation(proxy, method, args);
        });

        WaterReservoirService service = new WaterReservoirService(
                reservoirMapper,
                proxy(YzWaterFacilityBaseMapper.class, this::handleDefaultInvocation),
                managementMapper,
                null,
                null,
                proxy(SystemAreaMapper.class, this::handleDefaultInvocation)
        );

        ReservoirHeadItemSaveReqVO head = new ReservoirHeadItemSaveReqVO();
        head.setHeadLevel("county");
        head.setHeadName("王库长");
        head.setHeadPosition("库长");
        head.setHeadUnit("管理处");
        ReservoirHeadBatchSaveReqVO reqVO = new ReservoirHeadBatchSaveReqVO();
        reqVO.setWaterReservoirId(2001L);
        reqVO.setReferenceId(2001L);
        reqVO.setReferenceType("reservoir");
        reqVO.setHeads(List.of(head));

        LocalDateTime before = LocalDateTime.now();
        service.saveReservoirManagement(reqVO);
        LocalDateTime after = LocalDateTime.now();

        YzRiverChannelManagementDO record = insertedRecord.get();
        assertNotNull(record);
        assertEquals("王库长", record.getHeadName());
        assertNull(record.getHeadContact());
        assertNotNull(record.getEffectiveFrom());
        assertFalse(record.getEffectiveFrom().isBefore(before));
        assertFalse(record.getEffectiveFrom().isAfter(after));
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
}
