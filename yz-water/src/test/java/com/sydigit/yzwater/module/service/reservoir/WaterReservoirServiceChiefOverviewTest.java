package com.sydigit.yzwater.module.service.reservoir;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirChiefOverviewRespVO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaterReservoirServiceChiefOverviewTest {

    @Test
    void shouldReturnCurrentReservoirChiefsWithLevelLabels() {
        YzWaterReservoirMapper reservoirMapper = proxy(YzWaterReservoirMapper.class, (proxy, method, args) -> {
            if ("selectById".equals(method.getName()) && args != null && args.length == 1) {
                Long id = (Long) args[0];
                if (!Objects.equals(id, 20L)) {
                    return null;
                }
                YzWaterReservoirDO reservoir = new YzWaterReservoirDO();
                reservoir.setId(20L);
                reservoir.setReservoirName("三里港水库");
                return reservoir;
            }
            return handleDefaultInvocation(proxy, method, args);
        });
        YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class, (proxy, method, args) -> {
            if ("selectCurrentByResolvedReference".equals(method.getName())) {
                return List.of(
                        buildChief(1L, "张水库河长", "province", "库长", "13800000001"),
                        buildChief(2L, "李水库河长", "county", "副库长", "13800000002")
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });
        DictDataCommonApi dictDataApi = proxy(DictDataCommonApi.class, (proxy, method, args) -> {
            if ("getDictDataList".equals(method.getName())
                    && args != null
                    && args.length == 1
                    && Objects.equals(args[0], ZdConstants.ZD_HZJB)) {
                return List.of(
                        buildDict("province", "省级河长"),
                        buildDict("county", "县级河长")
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });

        WaterReservoirService service = new WaterReservoirService(
                reservoirMapper,
                proxy(YzWaterFacilityBaseMapper.class, this::handleDefaultInvocation),
                managementMapper,
                dictDataApi,
                null,
                proxy(SystemAreaMapper.class, this::handleDefaultInvocation)
        );

        ReservoirChiefOverviewRespVO overview = service.getReservoirChiefOverview(20L);

        assertEquals("三里港水库", overview.getReservoirName());
        assertEquals(2, overview.getReservoirChiefs().size());
        assertEquals("张水库河长", overview.getReservoirChiefs().get(0).getHeadName());
        assertEquals("省级河长", overview.getReservoirChiefs().get(0).getHeadLevelLabel());
        assertEquals("县级河长", overview.getReservoirChiefs().get(1).getHeadLevelLabel());
        assertEquals(2, overview.getTotalCount());
    }

    private YzRiverChannelManagementDO buildChief(Long id, String headName, String headLevel, String headPosition,
                                                  String headContact) {
        YzRiverChannelManagementDO chief = new YzRiverChannelManagementDO();
        chief.setId(id);
        chief.setReferenceType(ReferenceTypeConstants.RESERVOIR);
        chief.setReferenceId(20L);
        chief.setWaterReservoirId(20L);
        chief.setHeadName(headName);
        chief.setHeadLevel(headLevel);
        chief.setHeadPosition(headPosition);
        chief.setHeadContact(headContact);
        chief.setEffectiveFrom(LocalDateTime.of(2026, 4, 13, 9, 0));
        chief.setIsCurrent(1);
        return chief;
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
