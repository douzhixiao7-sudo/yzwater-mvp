package com.sydigit.yzwater.module.service.river;

import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadItemReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadSectionItemReqVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelSupervisionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
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

class RiverChannelServiceSaveManagementBehaviorTest {

    @Test
    void shouldSaveRiverChiefWithoutPhoneAndUseCurrentTime() {
        AtomicReference<YzRiverChannelManagementDO> insertedRecord = new AtomicReference<>();
        YzRiverChannelMapper riverChannelMapper = proxy(YzRiverChannelMapper.class, (proxy, method, args) -> {
            if ("selectById".equals(method.getName()) && args != null && args.length == 1 && Objects.equals(args[0], 1001L)) {
                YzRiverChannelDO channel = new YzRiverChannelDO();
                channel.setId(1001L);
                channel.setRiverName("仪扬河");
                return channel;
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

        RiverChannelService service = new RiverChannelService(
                riverChannelMapper,
                proxy(YzRiverSectionMapper.class, (proxy, method, args) -> {
                    if ("selectList".equals(method.getName())) {
                        return List.of();
                    }
                    return handleDefaultInvocation(proxy, method, args);
                }),
                managementMapper,
                proxy(YzRiverChannelSupervisionMapper.class, (proxy, method, args) -> {
                    if ("selectList".equals(method.getName())) {
                        return List.of();
                    }
                    return handleDefaultInvocation(proxy, method, args);
                }),
                proxy(YzWaterFacilityBaseMapper.class, this::handleDefaultInvocation),
                null,
                null
        );

        RiverHeadItemReqVO head = new RiverHeadItemReqVO();
        head.setHeadLevel("city");
        head.setHeadName("李河长");
        head.setHeadPosition("河长");
        RiverHeadSectionItemReqVO section = new RiverHeadSectionItemReqVO();
        section.setSectionName("仪扬河");
        section.setHeads(List.of(head));
        RiverHeadBatchSaveReqVO reqVO = new RiverHeadBatchSaveReqVO();
        reqVO.setRiverChannelId(1001L);
        reqVO.setSections(List.of(section));

        LocalDateTime before = LocalDateTime.now();
        service.saveRiverManagement(reqVO);
        LocalDateTime after = LocalDateTime.now();

        YzRiverChannelManagementDO record = insertedRecord.get();
        assertNotNull(record);
        assertEquals("李河长", record.getHeadName());
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
