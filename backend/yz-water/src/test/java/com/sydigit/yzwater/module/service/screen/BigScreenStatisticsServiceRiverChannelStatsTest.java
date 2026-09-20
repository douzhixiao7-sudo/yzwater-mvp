package com.sydigit.yzwater.module.service.screen;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictCountItemVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelBfDO;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelBfMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BigScreenStatisticsServiceRiverChannelStatsTest {

    @Test
    void shouldExcludeVillageLevelRiverChannelsFromStats() {
        AtomicInteger selectMapsCallIndex = new AtomicInteger(0);
        YzRiverChannelBfMapper riverChannelBfMapper = proxy(YzRiverChannelBfMapper.class, (proxy, method, args) -> {
            if (Objects.equals(method.getName(), "selectMaps")) {
                QueryWrapper<?> wrapper = (QueryWrapper<?>) args[0];
                assertExcludeVillageLevel(wrapper);
                int index = selectMapsCallIndex.getAndIncrement();
                if (index == 0) {
                    return List.of(Map.of("level", "6j", "cnt", 2L));
                }
                return List.of(Map.of(
                        "totalcount", 2L,
                        "totallengthkm", new BigDecimal("12.50"),
                        "totalcatchmentkm2", new BigDecimal("20.75")
                ));
            }
            return handleDefaultInvocation(proxy, method, args);
        });
        DictDataCommonApi dictDataApi = proxy(DictDataCommonApi.class, (proxy, method, args) -> {
            if (Objects.equals(method.getName(), "getDictDataList")
                    && args != null
                    && args.length == 1
                    && Objects.equals(args[0], ZdConstants.ZD_HLJB)) {
                return List.of(
                        buildDict("6j", "镇级"),
                        buildDict("7j", "村级")
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });

        BigScreenStatisticsService service = new BigScreenStatisticsService(
                null, null, riverChannelBfMapper, null, null, null, null,
                null, null, null, null, null, null, dictDataApi, null, null
        );

        var resp = service.getRiverChannelStats();
        Map<String, BigScreenDictCountItemVO> levelStats = resp.getLevelStats().stream()
                .collect(Collectors.toMap(BigScreenDictCountItemVO::getValue, Function.identity()));

        assertEquals(2L, resp.getTotalCount());
        assertEquals(new BigDecimal("12.50"), resp.getTotalLengthKm());
        assertEquals(new BigDecimal("20.75"), resp.getTotalCatchmentKm2());
        assertEquals(1, levelStats.size());
        assertEquals(2L, levelStats.get("6j").getCount());
        assertEquals("镇级", levelStats.get("6j").getLabel());
    }

    private void assertExcludeVillageLevel(QueryWrapper<?> wrapper) {
        assertTrue(wrapper.getSqlSegment().contains("river_level <>"),
                "统计查询必须排除村级河道");
        assertTrue(wrapper.getParamNameValuePairs().containsValue("7j"),
                "统计查询必须以 7j 作为排除值");
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
