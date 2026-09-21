package com.sydigit.yzwater.module.service.river;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefOverviewRespVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiverChannelServiceChiefOverviewTest {

    @Test
    void shouldGroupCurrentChiefsByRiverAndSection() {
        YzRiverChannelMapper riverChannelMapper = proxy(YzRiverChannelMapper.class, (proxy, method, args) -> {
            if ("selectById".equals(method.getName()) && args != null && args.length == 1) {
                Long id = (Long) args[0];
                if (!Objects.equals(id, 10L)) {
                    return null;
                }
                YzRiverChannelDO channel = new YzRiverChannelDO();
                channel.setId(10L);
                channel.setRiverName("仪扬河");
                return channel;
            }
            return handleDefaultInvocation(proxy, method, args);
        });
        YzRiverSectionMapper riverSectionMapper = proxy(YzRiverSectionMapper.class, (proxy, method, args) -> {
            if ("selectList".equals(method.getName())) {
                return List.of(
                        buildSection(101L, 10L, "东闸段"),
                        buildSection(102L, 10L, "西闸段")
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });
        YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class, (proxy, method, args) -> {
            if ("selectCurrentByResolvedReference".equals(method.getName())) {
                return List.of(
                        buildChief(1L, ReferenceTypeConstants.RIVER, 10L, null, "张河长", "province", "总河长", "13800000001")
                );
            }
            if ("selectCurrentByResolvedReferenceIds".equals(method.getName())) {
                return List.of(
                        buildChief(2L, ReferenceTypeConstants.RIVER_SECTION, 101L, 101L, "李河长", "city", "段长", "13800000002"),
                        buildChief(3L, ReferenceTypeConstants.RIVER_SECTION, 102L, 102L, "王河长", "county", "副段长", "13800000003")
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
                        buildDict("city", "市级河长"),
                        buildDict("county", "县级河长")
                );
            }
            return handleDefaultInvocation(proxy, method, args);
        });

        RiverChannelService service = new RiverChannelService(
                riverChannelMapper,
                riverSectionMapper,
                managementMapper,
                proxy(YzRiverChannelSupervisionMapper.class, this::handleDefaultInvocation),
                null,
                dictDataApi,
                null
        );

        RiverChiefOverviewRespVO overview = service.getRiverChiefOverview(10L);

        assertEquals("仪扬河", overview.getRiverName());
        assertEquals(1, overview.getRiverChiefs().size());
        assertEquals("张河长", overview.getRiverChiefs().get(0).getHeadName());
        assertEquals("省级河长", overview.getRiverChiefs().get(0).getHeadLevelLabel());
        assertEquals(2, overview.getSectionChiefGroups().size());
        Map<String, List<String>> sectionChiefMap = overview.getSectionChiefGroups().stream()
                .collect(Collectors.toMap(item -> item.getSectionName(),
                        item -> item.getChiefs().stream().map(chief -> chief.getHeadName()).toList()));
        assertEquals(List.of("李河长"), sectionChiefMap.get("东闸段"));
        assertEquals(List.of("王河长"), sectionChiefMap.get("西闸段"));
        assertEquals(3, overview.getTotalCount());
    }

    private YzRiverSectionDO buildSection(Long id, Long riverChannelId, String sectionName) {
        YzRiverSectionDO section = new YzRiverSectionDO();
        section.setId(id);
        section.setRiverChannelId(riverChannelId);
        section.setSectionName(sectionName);
        return section;
    }

    private YzRiverChannelManagementDO buildChief(Long id, String referenceType, Long referenceId, Long sectionId,
                                                  String headName, String headLevel, String headPosition,
                                                  String headContact) {
        YzRiverChannelManagementDO chief = new YzRiverChannelManagementDO();
        chief.setId(id);
        chief.setReferenceType(referenceType);
        chief.setReferenceId(referenceId);
        chief.setRiverSectionId(sectionId);
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
