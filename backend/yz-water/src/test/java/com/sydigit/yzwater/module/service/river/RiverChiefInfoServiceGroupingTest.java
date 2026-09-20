package com.sydigit.yzwater.module.service.river;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoTotalChiefRespVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.dto.RiverChiefInfoGroupRow;
import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiverChiefInfoServiceGroupingTest {

    @Test
    void shouldKeepFacilitySummarySeparatedWhenHeadNameSameButLevelDifferent() {
        RiverChiefInfoGroupRow cityLevelGroup = new RiverChiefInfoGroupRow();
        cityLevelGroup.setId(1L);
        cityLevelGroup.setHeadName("张三");
        cityLevelGroup.setHeadLevel("city");
        cityLevelGroup.setEffectiveFrom(LocalDateTime.of(2026, 4, 22, 10, 0));

        RiverChiefInfoGroupRow districtLevelGroup = new RiverChiefInfoGroupRow();
        districtLevelGroup.setId(2L);
        districtLevelGroup.setHeadName("张三");
        districtLevelGroup.setHeadLevel("district");
        districtLevelGroup.setEffectiveFrom(LocalDateTime.of(2026, 4, 22, 11, 0));

        YzRiverChannelManagementDO cityLevelRecord = buildRiverRecord(1L, "张三", "city", 101L,
                LocalDateTime.of(2026, 4, 22, 10, 0));
        YzRiverChannelManagementDO districtLevelRecord = buildRiverRecord(2L, "张三", "district", 202L,
                LocalDateTime.of(2026, 4, 22, 11, 0));

        RiverChiefInfoService service = buildService(
                List.of(cityLevelGroup, districtLevelGroup),
                List.of(cityLevelRecord, districtLevelRecord),
                List.of(),
                Map.of(101L, "运河一线", 202L, "运河二线"));

        RiverChiefInfoPageReqVO reqVO = new RiverChiefInfoPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<RiverChiefInfoPageRespVO> result = service.getPage(reqVO);

        assertEquals(2, result.getList().size());
        assertEquals("运河一线", result.getList().get(0).getFacilitySummary());
        assertEquals("运河二线", result.getList().get(1).getFacilitySummary());
    }

    @Test
    void shouldOnlyLoadFacilitiesWithinSameHeadNameAndHeadLevelGroup() {
        YzRiverChannelManagementDO cityLevelRecord = buildRiverRecord(1L, "张三", "city", 101L,
                LocalDateTime.of(2026, 4, 22, 10, 0));
        YzRiverChannelManagementDO districtLevelRecord = buildRiverRecord(2L, "张三", "district", 202L,
                LocalDateTime.of(2026, 4, 22, 11, 0));

        RiverChiefInfoService service = buildService(
                List.of(),
                List.of(cityLevelRecord, districtLevelRecord),
                List.of(),
                Map.of(101L, "运河一线", 202L, "运河二线"));

        RiverChiefInfoDetailRespVO detail = service.getDetail(1L);

        assertEquals(1, detail.getFacilities().size());
        assertEquals("运河一线", detail.getFacilities().get(0).getReferenceName());
    }

    @Test
    void shouldMergeFacilitySummariesWhenSameNameSamePositionAndEffectiveFrom() {
        LocalDateTime eff = LocalDateTime.of(2026, 5, 1, 10, 0);
        RiverChiefInfoGroupRow group = new RiverChiefInfoGroupRow();
        group.setId(100L);
        group.setHeadName("陈俊");
        group.setHeadLevel("city");
        group.setHeadPosition("副市长");
        group.setEffectiveFrom(eff);

        YzRiverChannelManagementDO r1 = buildRiverRecord(100L, "陈俊", "city", "副市长", 501L, eff);
        YzRiverChannelManagementDO r2 = buildRiverRecord(101L, "陈俊", "city", "副市长", 502L, eff);

        RiverChiefInfoService service = buildService(
                List.of(group),
                List.of(r1, r2),
                List.of(),
                Map.of(501L, "河道甲", 502L, "河道乙"));

        RiverChiefInfoPageReqVO reqVO = new RiverChiefInfoPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<RiverChiefInfoPageRespVO> result = service.getPage(reqVO);

        assertEquals(1, result.getList().size());
        assertEquals("河道甲、河道乙", result.getList().get(0).getFacilitySummary());
        assertEquals(2, result.getList().get(0).getMemberIds().size());
    }

    @Test
    void shouldMergeWhenSameNameLevelPositionDespiteDifferentEffectiveFrom() {
        LocalDateTime firstBatch = LocalDateTime.of(2026, 4, 22, 9, 0);
        LocalDateTime secondBatch = LocalDateTime.of(2026, 4, 22, 10, 0);

        RiverChiefInfoGroupRow mergedGroup = new RiverChiefInfoGroupRow();
        mergedGroup.setId(12L);
        mergedGroup.setHeadName("李四");
        mergedGroup.setHeadLevel("town");
        mergedGroup.setEffectiveFrom(secondBatch);

        YzRiverChannelManagementDO firstRecord = buildRiverRecord(11L, "李四", "town", 301L, firstBatch);
        YzRiverChannelManagementDO secondRecord = buildRiverRecord(12L, "李四", "town", 302L, secondBatch);

        RiverChiefInfoService service = buildService(
                List.of(mergedGroup),
                List.of(firstRecord, secondRecord),
                List.of(),
                Map.of(301L, "老北河", 302L, "老南河"));

        RiverChiefInfoPageReqVO reqVO = new RiverChiefInfoPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<RiverChiefInfoPageRespVO> result = service.getPage(reqVO);

        assertEquals(1, result.getList().size());
        assertEquals("老北河、老南河", result.getList().get(0).getFacilitySummary());

        RiverChiefInfoDetailRespVO detail = service.getDetail(11L);
        assertEquals(2, detail.getFacilities().size());
    }

    @Test
    void shouldReturnCurrentTotalChiefList() {
        YzRiverChannelManagementDO totalChiefA = buildTotalChiefRecord(21L, "孙卫中", "city", "仪征市副市长",
                LocalDateTime.of(2026, 4, 1, 0, 0));
        YzRiverChannelManagementDO totalChiefB = buildTotalChiefRecord(22L, "刘国庆", "district", "仪征市水利局总工程师",
                LocalDateTime.of(2026, 4, 2, 0, 0));
        YzRiverChannelManagementDO legacyLikeRecord = new YzRiverChannelManagementDO();
        legacyLikeRecord.setId(23L);
        legacyLikeRecord.setDeleted(false);
        legacyLikeRecord.setIsCurrent(1);
        legacyLikeRecord.setHeadName("历史总河长");
        legacyLikeRecord.setHeadLevel("town");
        legacyLikeRecord.setHeadPosition("旧口径记录");
        legacyLikeRecord.setEffectiveFrom(LocalDateTime.of(2026, 4, 3, 0, 0));

        RiverChiefInfoService service = buildService(
                List.of(),
                List.of(),
                List.of(totalChiefA, totalChiefB, legacyLikeRecord),
                Map.of());

        List<RiverChiefInfoTotalChiefRespVO> result = service.getTotalChiefList();

        assertEquals(2, result.size());
        assertEquals("刘国庆", result.get(0).getHeadName());
        assertEquals("district", result.get(0).getHeadLevel());
        assertEquals("仪征市水利局总工程师", result.get(0).getHeadPosition());
        assertEquals("孙卫中", result.get(1).getHeadName());
        assertEquals("city", result.get(1).getHeadLevel());
    }

    private RiverChiefInfoService buildService(List<RiverChiefInfoGroupRow> pageGroups,
                                               List<YzRiverChannelManagementDO> currentRecords,
                                               List<YzRiverChannelManagementDO> totalChiefRecords,
                                               Map<Long, String> riverNames) {
        YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class,
                (proxy, method, args) -> handleManagementMapper(method, args, pageGroups, currentRecords, totalChiefRecords));
        YzRiverChannelMapper riverChannelMapper = proxy(YzRiverChannelMapper.class,
                (proxy, method, args) -> handleRiverChannelMapper(method, args, riverNames));
        YzRiverSectionMapper riverSectionMapper = proxy(YzRiverSectionMapper.class,
                this::handleDefaultInvocation);
        YzWaterReservoirMapper waterReservoirMapper = proxy(YzWaterReservoirMapper.class,
                this::handleDefaultInvocation);
        return new RiverChiefInfoService(managementMapper, riverChannelMapper, riverSectionMapper,
                waterReservoirMapper, null);
    }

    private Object handleManagementMapper(Method method, Object[] args, List<RiverChiefInfoGroupRow> pageGroups,
                                          List<YzRiverChannelManagementDO> currentRecords,
                                          List<YzRiverChannelManagementDO> totalChiefRecords) {
        String methodName = method.getName();
        if ("countCurrentChiefGroup".equals(methodName)) {
            return (long) pageGroups.size();
        }
        if ("selectCurrentChiefGroupPage".equals(methodName)) {
            return pageGroups;
        }
        if ("selectCurrentTotalChiefs".equals(methodName)) {
            return totalChiefRecords;
        }
        if ("selectCurrentByHeadNames".equals(methodName)) {
            return currentRecords;
        }
        if ("selectById".equals(methodName) && args != null && args.length == 1) {
            Long id = (Long) args[0];
            return currentRecords.stream()
                    .filter(item -> Objects.equals(item.getId(), id))
                    .findFirst()
                    .orElse(null);
        }
        if ("selectCurrentByChiefDimension".equals(methodName)) {
            String targetHeadName = args != null && args.length > 0 ? (String) args[0] : null;
            String targetHeadLevel = args != null && args.length > 1 ? (String) args[1] : null;
            String targetHeadPosition = args != null && args.length > 2 ? (String) args[2] : null;
            return currentRecords.stream()
                    .filter(item -> Objects.equals(item.getHeadName(), targetHeadName))
                    .filter(item -> Objects.equals(StrUtil.trimToEmpty(item.getHeadLevel()),
                            StrUtil.trimToEmpty(targetHeadLevel)))
                    .filter(item -> Objects.equals(normalizePositionKey(item.getHeadPosition()),
                            normalizePositionKey(targetHeadPosition)))
                    .toList();
        }
        return handleDefaultInvocation(null, method, args);
    }

    private static String normalizePositionKey(String headPosition) {
        if (headPosition == null) {
            return "";
        }
        return headPosition.replace('\u3000', ' ')
                .replaceAll("\\s+", "");
    }

    private Object handleRiverChannelMapper(Method method, Object[] args, Map<Long, String> riverNames) {
        if ("selectById".equals(method.getName()) && args != null && args.length == 1) {
            Long id = (Long) args[0];
            String riverName = riverNames.get(id);
            if (riverName == null) {
                return null;
            }
            YzRiverChannelDO channel = new YzRiverChannelDO();
            channel.setId(id);
            channel.setRiverName(riverName);
            return channel;
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

    private YzRiverChannelManagementDO buildRiverRecord(Long id, String headName, String headLevel, Long riverId,
                                                        LocalDateTime effectiveFrom) {
        return buildRiverRecord(id, headName, headLevel, null, riverId, effectiveFrom);
    }

    private YzRiverChannelManagementDO buildRiverRecord(Long id, String headName, String headLevel,
                                                        String headPosition, Long riverId, LocalDateTime effectiveFrom) {
        YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
        record.setId(id);
        record.setDeleted(false);
        record.setIsCurrent(1);
        record.setHeadName(headName);
        record.setHeadLevel(headLevel);
        record.setHeadPosition(headPosition);
        record.setReferenceType(ReferenceTypeConstants.RIVER);
        record.setReferenceId(riverId);
        record.setRiverChannelId(riverId);
        record.setEffectiveFrom(effectiveFrom);
        return record;
    }

    private YzRiverChannelManagementDO buildTotalChiefRecord(Long id, String headName, String headLevel,
                                                             String headPosition, LocalDateTime effectiveFrom) {
        YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
        record.setId(id);
        record.setDeleted(false);
        record.setIsCurrent(1);
        record.setHeadName(headName);
        record.setHeadLevel(headLevel);
        record.setHeadPosition(headPosition);
        record.setReferenceType(ReferenceTypeConstants.TOTAL_CHIEF);
        record.setEffectiveFrom(effectiveFrom);
        return record;
    }
}
