package com.sydigit.yzwater.module.service.screen;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialTypeItemRespVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxQxdwMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxWzMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import com.sydigit.yzwater.module.service.flood.FxWzService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigScreenFloodMaterialServiceNoFilterTest {

    @Test
    void shouldReturnAllMaterialWithoutTypeFilter() throws Exception {
        YzFxWzMapper wzMapper = proxy(YzFxWzMapper.class, (method, args) -> {
            if ("selectListOrderBySort".equals(method.getName())) {
                return List.of(buildMaterial("编织袋", "type-a", "ge", "1001"), buildMaterial("木桩", "type-b", "gen", "1002"));
            }
            if ("selectListByMaterialType".equals(method.getName())) {
                return List.of(buildMaterial("编织袋", "type-a", "ge", "1001"));
            }
            throw new UnsupportedOperationException(method.getName());
        });
        DictDataCommonApi dictDataApi = proxy(DictDataCommonApi.class, (method, args) -> {
            if (!"getDictDataList".equals(method.getName())) {
                throw new UnsupportedOperationException(method.getName());
            }
            String dictType = String.valueOf(args[0]);
            if (ZdConstants.ZD_WZDW.equals(dictType)) {
                return List.of(buildDict("个", "ge"), buildDict("根", "gen"));
            }
            return List.of();
        });

        BigScreenFloodMaterialService service = new BigScreenFloodMaterialService(
                wzMapper,
                proxy(FxWzService.class, (method, args) -> {
                    if ("findMaterialById".equals(method.getName())) {
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }),
                proxy(YzFloodPreventionMaterialWarehouseMapper.class, (method, args) -> List.of(
                        buildWarehouse(1001L, "市级防汛仓库", "张三", "13800000000"),
                        buildWarehouse(1002L, "镇级防汛仓库", "李四", "13900000000")
                )),
                proxy(YzFxQxdwMapper.class, (method, args) -> List.of()),
                dictDataApi
        );

        Method method = BigScreenFloodMaterialService.class.getDeclaredMethod("getMaterialListByType");
        @SuppressWarnings("unchecked")
        List<BigScreenFloodMaterialTypeItemRespVO> result =
                (List<BigScreenFloodMaterialTypeItemRespVO>) method.invoke(service);

        assertEquals(2, result.size());
        assertEquals("编织袋", result.get(0).getMaterialName());
        assertEquals("个", result.get(0).getUnitLabel());
        assertEquals("市级防汛仓库", result.get(0).getStorageUnit());
        assertEquals("张三", result.get(0).getLeaderName());
        assertEquals("13800000000", result.get(0).getLeaderPhone());
        assertEquals("木桩", result.get(1).getMaterialName());
        assertEquals("根", result.get(1).getUnitLabel());
    }

    private YzFxWzDO buildMaterial(String name, String materialType, String unit, String warehouseId) {
        YzFxWzDO item = new YzFxWzDO();
        item.setMaterialName(name);
        item.setMaterialType(materialType);
        item.setUnit(unit);
        item.setUnitId(warehouseId);
        item.setQuantity(BigDecimal.ONE);
        item.setIsDelegateStorage(1);
        return item;
    }

    private YzFloodPreventionMaterialWarehouseDO buildWarehouse(Long id, String name, String leaderName, String leaderPhone) {
        YzFloodPreventionMaterialWarehouseDO item = new YzFloodPreventionMaterialWarehouseDO();
        item.setId(id);
        item.setWarehouseName(name);
        item.setLeaderName(leaderName);
        item.setLeaderPhone(leaderPhone);
        return item;
    }

    private DictDataRespDTO buildDict(String label, String value) {
        DictDataRespDTO dto = new DictDataRespDTO();
        dto.setLabel(label);
        dto.setValue(value);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type},
                (proxy, method, args) -> invocation.invoke(method, args));
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(Method method, Object[] args) throws Throwable;
    }
}
