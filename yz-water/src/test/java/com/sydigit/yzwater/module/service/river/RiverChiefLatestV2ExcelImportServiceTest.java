package com.sydigit.yzwater.module.service.river;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefLatestImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiverChiefLatestV2ExcelImportServiceTest {

    @Test
    void shouldBindRiverSectionAndExpireCurrentHeads() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("真州镇", area(321081001L, "真州镇"));
        context.dictByType.put(ZdConstants.ZD_HLJB, List.of(dict("省级", "prov_river")));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("省级", "provincial"),
                dict("市级", "city"),
                dict("县级", "county"),
                dict("镇级", "township"),
                dict("村级", "village")
        ));

        YzRiverChannelDO river = new YzRiverChannelDO();
        river.setId(1001L);
        river.setRiverName("长江");
        context.existingRivers.add(river);

        YzRiverSectionDO section = new YzRiverSectionDO();
        section.setId(2001L);
        section.setRiverChannelId(1001L);
        section.setSectionName("青州镇段");
        context.existingSections.add(section);

        YzRiverChannelManagementDO current = new YzRiverChannelManagementDO();
        current.setId(3001L);
        current.setVersionNo(2);
        current.setReferenceType(ReferenceTypeConstants.RIVER_SECTION);
        current.setReferenceId(2001L);
        current.setRiverChannelId(1001L);
        current.setRiverSectionId(2001L);
        context.managementSelectResults.add(List.of(current));

        RiverChiefLatestV2ExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(riverSectionRow()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(5, respVO.getSuccessCount(), "errors=" + respVO.getErrors());
        assertEquals(0, respVO.getFailureCount());
        assertEquals(1, context.managementUpdateCount);
        assertEquals(5, context.insertedManagementRecords.size());
        assertEquals(1, context.updatedRivers.size());
        assertArrayEquals(new String[]{"321081001"}, context.updatedRivers.get(0).getTown());
        assertEquals("prov_river", context.updatedRivers.get(0).getRiverLevel());
        YzRiverChannelManagementDO cityHead = context.insertedManagementRecords.get(1);
        assertEquals(3, cityHead.getVersionNo());
        assertEquals(ReferenceTypeConstants.RIVER_SECTION, cityHead.getReferenceType());
        assertEquals(Long.valueOf(2001L), cityHead.getReferenceId());
        assertEquals(Long.valueOf(2001L), cityHead.getRiverSectionId());
        assertEquals(Long.valueOf(1001L), cityHead.getRiverChannelId());
        assertNull(cityHead.getHeadContact());
    }

    @Test
    void shouldSkipRowWhenSectionNotFoundWithoutFallbackToRiver() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("真州镇", area(321081001L, "真州镇"));
        context.dictByType.put(ZdConstants.ZD_HLJB, List.of(dict("省级", "prov_river")));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township")
        ));

        YzRiverChannelDO river = new YzRiverChannelDO();
        river.setId(1001L);
        river.setRiverName("长江");
        context.existingRivers.add(river);

        RiverChiefLatestV2ExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(riverMissingSectionRow()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(0, respVO.getSuccessCount());
        assertEquals(2, respVO.getFailureCount());
        assertTrue(respVO.getErrors().stream().anyMatch(msg -> msg.contains("河段")));
        assertEquals(0, context.managementUpdateCount);
        assertTrue(context.insertedManagementRecords.isEmpty());
    }

    @Test
    void shouldMatchDuplicateReservoirByTownshipAndExpireCurrentHeads() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("月塘镇", area(321081101L, "月塘镇"));
        context.areaByName.put("枣林湾旅游度假区", area(321081102L, "枣林湾旅游度假区"));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township")
        ));

        YzWaterReservoirDO r1 = new YzWaterReservoirDO();
        r1.setId(4001L);
        r1.setReservoirName("邵冲水库");
        r1.setTownship(new String[]{"321081102"});
        context.existingReservoirs.add(r1);

        YzWaterReservoirDO r2 = new YzWaterReservoirDO();
        r2.setId(4002L);
        r2.setReservoirName("邵冲水库");
        r2.setTownship(new String[]{"321081101"});
        context.existingReservoirs.add(r2);

        YzRiverChannelManagementDO current = new YzRiverChannelManagementDO();
        current.setId(5001L);
        current.setVersionNo(1);
        current.setReferenceType(ReferenceTypeConstants.RESERVOIR);
        current.setReferenceId(4002L);
        current.setWaterReservoirId(4002L);
        context.managementSelectResults.add(List.of(current));

        RiverChiefLatestV2ExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(duplicateReservoirRow()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(2, respVO.getSuccessCount(), "errors=" + respVO.getErrors());
        assertEquals(0, respVO.getFailureCount());
        assertEquals(1, context.managementUpdateCount);
        assertEquals(1, context.updatedReservoirs.size());
        assertArrayEquals(new String[]{"321081101"}, context.updatedReservoirs.get(0).getTownship());
        assertEquals(Long.valueOf(4002L), context.insertedManagementRecords.get(0).getReferenceId());
        assertEquals(ReferenceTypeConstants.RESERVOIR, context.insertedManagementRecords.get(0).getReferenceType());
        assertEquals(2, context.insertedManagementRecords.get(0).getVersionNo());
    }

    @Test
    void shouldFailWhenDuplicateRiverStillAmbiguousAfterTownshipMatch() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("真州镇", area(321081001L, "真州镇"));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(dict("县级", "county")));

        YzRiverChannelDO river1 = new YzRiverChannelDO();
        river1.setId(6001L);
        river1.setRiverName("仪城河");
        river1.setTown(new String[]{"321081001"});
        context.existingRivers.add(river1);

        YzRiverChannelDO river2 = new YzRiverChannelDO();
        river2.setId(6002L);
        river2.setRiverName("仪城河");
        river2.setTown(new String[]{"321081001"});
        context.existingRivers.add(river2);

        RiverChiefLatestV2ExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(ambiguousRiverRow()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(0, respVO.getSuccessCount());
        assertEquals(1, respVO.getFailureCount());
        assertTrue(respVO.getErrors().stream().anyMatch(msg -> msg.contains("重名")));
        assertTrue(context.insertedManagementRecords.isEmpty());
        assertEquals(0, context.managementUpdateCount);
    }

    @Test
    void shouldUseDeletedZeroWhenQueryingRiverAndReservoir() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("月塘镇", area(321081101L, "月塘镇"));
        context.areaByName.put("真州镇", area(321081001L, "真州镇"));
        context.dictByType.put(ZdConstants.ZD_HLJB, List.of(dict("省级", "prov_river")));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(dict("县级", "county")));

        YzRiverChannelDO river = new YzRiverChannelDO();
        river.setId(7001L);
        river.setRiverName("长江");
        context.existingRivers.add(river);

        RiverChiefLatestV2ExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(List.of(queryRiverRow(), singleReservoirRow()));

        service.importLatestExcel(file);

        assertHasDeletedZero(context.lastRiverSelectArg);
        assertHasDeletedZero(context.lastReservoirSelectArg);
    }

    private static DictDataRespDTO dict(String label, String value) {
        DictDataRespDTO dto = new DictDataRespDTO();
        dto.setLabel(label);
        dto.setValue(value);
        return dto;
    }

    private static SystemAreaDO area(Long id, String name) {
        SystemAreaDO area = new SystemAreaDO();
        area.setId(id);
        area.setName(name);
        return area;
    }

    private static MockMultipartFile buildExcelFile(List<String[]> dataRows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("河长2");
            Row headerRow = sheet.createRow(0);
            String[] headers = headers();
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < dataRows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] values = dataRows.get(i);
                for (int j = 0; j < values.length; j++) {
                    if (values[j] != null) {
                        row.createCell(j).setCellValue(values[j]);
                    }
                }
            }
            workbook.write(output);
            return new MockMultipartFile(
                    "file",
                    "河长2.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }

    private static String[] headers() {
        return new String[]{
                "序号", "河道名称", "河段", "所属乡镇", "河道/水库", "河道级别",
                "省级河长", "省级河长职务",
                "市级河长", "市级河长职务",
                "县级河长", "县级河长职务",
                "镇级河长", "镇级河长联系电话", "镇级河长职务",
                "村级河长1", "村级河长1联系电话", "村级河长1职务",
                "村级河长2", "村级河长2联系电话", "村级河长2职务",
                "村级河长3", "村级河长3联系电话", "村级河长3职务",
                "村级河长4", "村级河长4联系电话", "村级河长4职务",
                "村级河长5", "村级河长5联系电话", "村级河长5职务",
                "村级河长6", "村级河长6联系电话", "村级河长6职务",
                "村级河长7", "村级河长7联系电话", "村级河长7职务",
                "村级河长8", "村级河长8联系电话", "村级河长8职务",
                "村级河长9", "村级河长9联系电话", "村级河长9职务",
                "村级河长10", "村级河长10联系电话", "村级河长10职务"
        };
    }

    private static String[] riverSectionRow() {
        String[] row = new String[48];
        row[0] = "1";
        row[1] = "长江";
        row[2] = "青州镇段";
        row[3] = "真州镇";
        row[4] = "河道";
        row[5] = "省级";
        row[6] = "马欣";
        row[7] = "省委常委";
        row[8] = "王进健";
        row[9] = "扬州市委书记";
        row[10] = "杨庆洋";
        row[11] = "仪征市委副书记";
        row[12] = "叶军";
        row[13] = "13951446123";
        row[14] = "真州镇副镇长";
        row[15] = "陈明松";
        row[16] = "13905253098";
        row[17] = "长江村党总支书记";
        return row;
    }

    private static String[] riverMissingSectionRow() {
        String[] row = new String[48];
        row[0] = "1";
        row[1] = "长江";
        row[2] = "不存在河段";
        row[3] = "真州镇";
        row[4] = "河道";
        row[5] = "省级";
        row[10] = "杨庆洋";
        row[11] = "仪征市委副书记";
        row[12] = "叶军";
        row[13] = "13951446123";
        row[14] = "真州镇副镇长";
        return row;
    }

    private static String[] duplicateReservoirRow() {
        String[] row = new String[48];
        row[0] = "1";
        row[1] = "邵冲水库";
        row[3] = "月塘镇";
        row[4] = "水库";
        row[10] = "李晟文";
        row[11] = "度假区党委书记";
        row[12] = "汪天兵";
        row[13] = "18118226808";
        row[14] = "综合部部长";
        return row;
    }

    private static String[] ambiguousRiverRow() {
        String[] row = new String[48];
        row[0] = "1";
        row[1] = "仪城河";
        row[3] = "真州镇";
        row[4] = "河道";
        row[10] = "曹昕";
        row[11] = "仪征市政府副市长";
        return row;
    }

    private static String[] singleReservoirRow() {
        String[] row = new String[48];
        row[0] = "1";
        row[1] = "红旗水库";
        row[3] = "月塘镇";
        row[4] = "水库";
        row[10] = "库长甲";
        row[11] = "县级职务";
        return row;
    }

    private static String[] queryRiverRow() {
        String[] row = new String[48];
        row[0] = "2";
        row[1] = "长江";
        row[3] = "真州镇";
        row[4] = "河道";
        row[5] = "省级";
        row[10] = "杨庆洋";
        row[11] = "仪征市委副书记";
        return row;
    }

    private static void assertHasDeletedZero(Object wrapper) {
        assertTrue(wrapper instanceof AbstractWrapper, "查询参数应为 MyBatis Wrapper");
        @SuppressWarnings("unchecked")
        AbstractWrapper<?, ?, ?> abstractWrapper = (AbstractWrapper<?, ?, ?>) wrapper;
        abstractWrapper.getSqlSegment();
        List<Object> values = new ArrayList<>(abstractWrapper.getParamNameValuePairs().values());
        assertTrue(values.contains(0), "查询 deleted 应传 0，实际参数：" + values);
        assertFalse(values.contains(Boolean.FALSE), "查询 deleted 不应传 false，实际参数：" + values);
    }

    private static final class TestContext {

        private final Map<String, SystemAreaDO> areaByName = new HashMap<>();
        private final Map<String, List<DictDataRespDTO>> dictByType = new HashMap<>();
        private final List<YzRiverChannelDO> existingRivers = new ArrayList<>();
        private final List<YzWaterReservoirDO> existingReservoirs = new ArrayList<>();
        private final List<YzRiverSectionDO> existingSections = new ArrayList<>();
        private final Deque<List<YzRiverChannelManagementDO>> managementSelectResults = new ArrayDeque<>();
        private final List<YzRiverChannelDO> updatedRivers = new ArrayList<>();
        private final List<YzWaterReservoirDO> updatedReservoirs = new ArrayList<>();
        private final List<YzRiverChannelManagementDO> insertedManagementRecords = new ArrayList<>();
        private Object lastRiverSelectArg;
        private Object lastReservoirSelectArg;
        private int managementUpdateCount;

        private RiverChiefLatestV2ExcelImportService buildService() {
            YzRiverChannelMapper riverChannelMapper = proxy(YzRiverChannelMapper.class, this::handleRiverChannelMapper);
            YzWaterReservoirMapper waterReservoirMapper = proxy(YzWaterReservoirMapper.class, this::handleReservoirMapper);
            YzRiverSectionMapper riverSectionMapper = proxy(YzRiverSectionMapper.class, this::handleSectionMapper);
            YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class, this::handleManagementMapper);
            SystemAreaMapper systemAreaMapper = proxy(SystemAreaMapper.class, this::handleAreaMapper);
            DictDataCommonApi dictDataApi = proxy(DictDataCommonApi.class, this::handleDictDataApi);
            return new RiverChiefLatestV2ExcelImportService(
                    riverChannelMapper,
                    waterReservoirMapper,
                    riverSectionMapper,
                    managementMapper,
                    systemAreaMapper,
                    dictDataApi
            );
        }

        private Object handleRiverChannelMapper(Object proxy, Method method, Object[] args) {
            if ("selectList".equals(method.getName())) {
                lastRiverSelectArg = args == null || args.length == 0 ? null : args[0];
                return new ArrayList<>(existingRivers);
            }
            if ("updateById".equals(method.getName()) && args != null && args.length == 1) {
                updatedRivers.add((YzRiverChannelDO) args[0]);
                return 1;
            }
            return defaultValue(proxy, method, args);
        }

        private Object handleReservoirMapper(Object proxy, Method method, Object[] args) {
            if ("selectList".equals(method.getName())) {
                lastReservoirSelectArg = args == null || args.length == 0 ? null : args[0];
                return new ArrayList<>(existingReservoirs);
            }
            if ("updateById".equals(method.getName()) && args != null && args.length == 1) {
                updatedReservoirs.add((YzWaterReservoirDO) args[0]);
                return 1;
            }
            return defaultValue(proxy, method, args);
        }

        private Object handleSectionMapper(Object proxy, Method method, Object[] args) {
            if ("selectList".equals(method.getName())) {
                return new ArrayList<>(existingSections);
            }
            return defaultValue(proxy, method, args);
        }

        private Object handleManagementMapper(Object proxy, Method method, Object[] args) {
            if ("selectList".equals(method.getName())) {
                return managementSelectResults.isEmpty() ? List.of() : managementSelectResults.removeFirst();
            }
            if ("update".equals(method.getName())) {
                managementUpdateCount++;
                return 1;
            }
            if ("insert".equals(method.getName()) && args != null && args.length == 1) {
                insertedManagementRecords.add((YzRiverChannelManagementDO) args[0]);
                return 1;
            }
            return defaultValue(proxy, method, args);
        }

        private Object handleAreaMapper(Object proxy, Method method, Object[] args) {
            if ("selectByNames".equals(method.getName()) && args != null && args.length == 1) {
                List<?> names = (List<?>) args[0];
                List<SystemAreaDO> result = new ArrayList<>();
                for (Object item : names) {
                    SystemAreaDO area = areaByName.get(Objects.toString(item, null));
                    if (area != null) {
                        result.add(area);
                    }
                }
                return result;
            }
            return defaultValue(proxy, method, args);
        }

        private Object handleDictDataApi(Object proxy, Method method, Object[] args) {
            if ("getDictDataList".equals(method.getName()) && args != null && args.length == 1) {
                return dictByType.getOrDefault(Objects.toString(args[0], null), List.of());
            }
            return defaultValue(proxy, method, args);
        }

        @SuppressWarnings("unchecked")
        private <T> T proxy(Class<T> type, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
        }

        private Object defaultValue(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                if ("toString".equals(method.getName())) {
                    return method.getName();
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
    }
}
