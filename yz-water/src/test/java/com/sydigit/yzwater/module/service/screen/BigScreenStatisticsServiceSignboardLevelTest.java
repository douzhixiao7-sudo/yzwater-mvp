package com.sydigit.yzwater.module.service.screen;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictCountItemVO;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardBfMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigScreenStatisticsServiceSignboardLevelTest {

    @Test
    void shouldCountBySignboardLevelAndExposeDictLabel() {
        YzSignboardBfMapper signboardBfMapper = proxy(YzSignboardBfMapper.class, (proxy, method, args) -> {
            if (Objects.equals(method.getName(), "selectRiverLevelCountByReference")
                    || Objects.equals(method.getName(), "selectSignboardLevelCount")) {
                return List.of(
                        Map.of("signboard_level", "province", "cnt", 2L),
                        Map.of("signboard_level", "city", "cnt", 1L)
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });
        DictDataCommonApi dictDataApi = proxy(DictDataCommonApi.class, (proxy, method, args) -> {
            if (Objects.equals(method.getName(), "getDictDataList")
                    && args != null
                    && args.length == 1
                    && Objects.equals(args[0], ZdConstants.ZD_HLJB)) {
                return List.of(
                        buildDict("province", "省级"),
                        buildDict("city", "市级"),
                        buildDict("county", "县级")
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });

        BigScreenStatisticsService service = new BigScreenStatisticsService(
                null, null, null, null, signboardBfMapper, null, null,
                null, null, null, null, null, null, dictDataApi, null, null
        );

        Map<String, BigScreenDictCountItemVO> result = service.getSignboardCountByRiverLevel().stream()
                .collect(Collectors.toMap(BigScreenDictCountItemVO::getValue, Function.identity()));

        assertEquals(2L, result.get("province").getCount());
        assertEquals("省级", result.get("province").getLabel());
        assertEquals(1L, result.get("city").getCount());
        assertEquals("市级", result.get("city").getLabel());
        assertEquals(0L, result.get("county").getCount());
        assertEquals("县级", result.get("county").getLabel());
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
