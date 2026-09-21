package com.sydigit.yzwater.module.service.screen;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigScreenStatisticsServiceRiverHeadTest {

    @Test
    void shouldReturnTotalChiefCountAlongsideRiverHeadStats() {
        AtomicInteger selectMapsCallIndex = new AtomicInteger(0);
        YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class,
                (proxy, method, args) -> {
                    if (Objects.equals(method.getName(), "selectMaps")) {
                        int index = selectMapsCallIndex.getAndIncrement();
                        if (index == 0) {
                            return List.of(
                                    Map.of("level", "province", "cnt", 2L),
                                    Map.of("level", "city", "cnt", 1L)
                            );
                        }
                        return List.of(Map.of("cnt", 5L));
                    }
                    if (Objects.equals(method.getName(), "selectCount")) {
                        return 2L;
                    }
                    return handleDefaultInvocation(proxy, method, args);
                });
        DictDataCommonApi dictDataApi = proxy(DictDataCommonApi.class, (proxy, method, args) -> {
            if (Objects.equals(method.getName(), "getDictDataList")
                    && args != null
                    && args.length == 1
                    && Objects.equals(args[0], ZdConstants.ZD_HZJB)) {
                return List.of(
                        buildDict("province", "省级"),
                        buildDict("city", "市级")
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });

        BigScreenStatisticsService service = new BigScreenStatisticsService(
                managementMapper, null, null, null, null, null, null,
                null, null, null, null, null, null, dictDataApi, null, null
        );

        var resp = service.getRiverHeadStats();

        assertEquals(5L, resp.getTotalCount());
        assertEquals(2L, resp.getTotalChiefCount());
    }

    private DictDataRespDTO buildDict(String value, String label) {
        DictDataRespDTO dict = new DictDataRespDTO();
        dict.setValue(value);
        dict.setLabel(label);
        return dict;
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
