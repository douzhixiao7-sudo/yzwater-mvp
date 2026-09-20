package com.sydigit.yzwater.module.service.river;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefLatestImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
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

class RiverChiefLatestExcelImportServiceTest {

    @Test
    void shouldImportVillageHeadsWithoutFacilityBindingWhenFacilityLevelIsVillage() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("真州镇", area(321081001L, "真州镇"));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township"),
                dict("村级", "village")
        ));

        RiverChiefLatestExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(villageOnlyRow()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(1, respVO.getSuccessCount(),
                "errors=" + respVO.getErrors() + ", skips=" + respVO.getSkipMessages());
        assertEquals(0, respVO.getFailureCount());
        assertEquals(1, context.insertedManagementRecords.size());

        YzRiverChannelManagementDO inserted = context.insertedManagementRecords.get(0);
        assertNull(inserted.getReferenceType());
        assertNull(inserted.getReferenceId());
        assertEquals("三八村", inserted.getSectionName());
        assertEquals("village", inserted.getHeadLevel());
        assertEquals("村干部甲", inserted.getHeadName());
        assertArrayEquals(new String[]{"321081001"}, inserted.getAdministrativeRegion());
    }

    @Test
    void shouldCreateMissingRiverAndImportAllHeads() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("枣林湾度假区", area(321081101L, "枣林湾度假区"));
        context.dictByType.put(ZdConstants.ZD_HLJB, List.of(dict("县级", "county_level")));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township"),
                dict("村级", "village")
        ));
        context.managementSelectResults.add(List.of());
        context.managementSelectResults.add(List.of());

        RiverChiefLatestExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(riverRowForCreate()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(2, respVO.getSuccessCount(),
                "errors=" + respVO.getErrors() + ", skips=" + respVO.getSkipMessages());
        assertEquals(0, respVO.getFailureCount());
        assertEquals(1, context.insertedFacilityBases.size());
        assertEquals(1, context.insertedRivers.size());
        assertEquals(2, context.insertedManagementRecords.size());

        YzRiverChannelDO river = context.insertedRivers.get(0);
        assertEquals("程营冲心河", river.getRiverName());
        assertEquals("county_level", river.getRiverLevel());
        assertArrayEquals(new String[]{"321081101"}, river.getTown());
    }

    @Test
    void shouldQueryRiverByDeletedZeroInsteadOfBooleanFalse() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("枣林湾度假区", area(321081101L, "枣林湾度假区"));
        context.dictByType.put(ZdConstants.ZD_HLJB, List.of(dict("县级", "county_level")));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township"),
                dict("村级", "village")
        ));
        context.managementSelectResults.add(List.of());

        RiverChiefLatestExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(riverRowForCreate()));

        service.importLatestExcel(file);

        assertHasDeletedZero(context.lastRiverSelectArg);
    }

    @Test
    void shouldQueryReservoirByDeletedZeroInsteadOfBooleanFalse() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("月塘镇", area(321081102L, "月塘镇"));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township"),
                dict("村级", "village")
        ));
        context.managementSelectResults.add(List.of());

        RiverChiefLatestExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(reservoirRowForAppend()));

        service.importLatestExcel(file);

        assertHasDeletedZero(context.lastReservoirSelectArg);
    }

    @Test
    void shouldKeepCurrentHeadsAndAppendNextVersionForExistingRiver() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("枣林湾度假区", area(321081101L, "枣林湾度假区"));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township"),
                dict("村级", "village")
        ));

        YzRiverChannelDO existingRiver = new YzRiverChannelDO();
        existingRiver.setId(9001L);
        existingRiver.setRiverName("程营冲心河");
        context.existingRivers.add(existingRiver);

        YzRiverChannelManagementDO current = new YzRiverChannelManagementDO();
        current.setId(1L);
        current.setVersionNo(1);
        current.setReferenceType(ReferenceTypeConstants.RIVER);
        current.setReferenceId(9001L);
        current.setRiverChannelId(9001L);
        current.setHeadLevel("county");
        current.setHeadName("旧县长");
        context.managementSelectResults.add(List.of(current));

        RiverChiefLatestExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(riverRowForOverwrite()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(2, respVO.getSuccessCount(),
                "errors=" + respVO.getErrors() + ", skips=" + respVO.getSkipMessages());
        assertEquals(0, respVO.getFailureCount());
        assertEquals(0, context.managementUpdateCount);
        assertEquals(2, context.insertedManagementRecords.size());
        assertEquals(2, context.insertedManagementRecords.get(0).getVersionNo());
        assertEquals(2, context.insertedManagementRecords.get(1).getVersionNo());
        assertEquals("新县长", context.insertedManagementRecords.get(0).getHeadName());
        assertEquals("旧县长", current.getHeadName());
    }

    @Test
    void shouldKeepCurrentHeadsAndAppendNextVersionForExistingReservoir() throws Exception {
        TestContext context = new TestContext();
        context.areaByName.put("月塘镇", area(321081102L, "月塘镇"));
        context.dictByType.put(ZdConstants.ZD_HZJB, List.of(
                dict("县级", "county"),
                dict("镇级", "township"),
                dict("村级", "village")
        ));

        YzWaterReservoirDO existingReservoir = new YzWaterReservoirDO();
        existingReservoir.setId(9101L);
        existingReservoir.setReservoirName("红旗水库");
        existingReservoir.setTownship(new String[]{"321081102"});
        context.existingReservoirs.add(existingReservoir);

        YzRiverChannelManagementDO current = new YzRiverChannelManagementDO();
        current.setId(2L);
        current.setVersionNo(3);
        current.setReferenceType(ReferenceTypeConstants.RESERVOIR);
        current.setReferenceId(9101L);
        current.setWaterReservoirId(9101L);
        current.setHeadLevel("county");
        current.setHeadName("旧库长");
        context.managementSelectResults.add(List.of(current));

        RiverChiefLatestExcelImportService service = context.buildService();
        MockMultipartFile file = buildExcelFile(java.util.Collections.singletonList(reservoirRowForAppend()));

        RiverChiefLatestImportRespVO respVO = service.importLatestExcel(file);

        assertEquals(2, respVO.getSuccessCount(),
                "errors=" + respVO.getErrors() + ", skips=" + respVO.getSkipMessages());
        assertEquals(0, respVO.getFailureCount());
        assertEquals(0, context.managementUpdateCount);
        assertEquals(2, context.insertedManagementRecords.size());
        assertEquals(4, context.insertedManagementRecords.get(0).getVersionNo());
        assertEquals(4, context.insertedManagementRecords.get(1).getVersionNo());
        assertEquals(ReferenceTypeConstants.RESERVOIR, context.insertedManagementRecords.get(0).getReferenceType());
        assertEquals(Long.valueOf(9101L), context.insertedManagementRecords.get(0).getReferenceId());
        assertEquals("新库长", context.insertedManagementRecords.get(0).getHeadName());
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
            Sheet sheet = workbook.createSheet("河长");
            sheet.createRow(0).createCell(0).setCellValue("说明1");
            sheet.createRow(1).createCell(0).setCellValue("说明2");
            sheet.createRow(2).createCell(0).setCellValue("说明3");
            Row headerRow = sheet.createRow(3);
            String[] headers = headers();
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < dataRows.size(); i++) {
                Row row = sheet.createRow(i + 4);
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
                    "河长.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }

    private static String[] headers() {
        return new String[]{
                "序号", "河道名称", "所属乡镇", "河道/水库", "河道级别",
                "县级河长", "县级河长职务",
                "镇级河长", "镇级河长联系电话", "镇级河长职务",
                "村级河长1", "村级河长1联系电话", "村级河长1职务",
                "村级河长2", "村级河长2联系电话", "村级河长2职务",
                "村级河长3", "村级河长3联系电话", "村级河长3职务",
                "村级河长4", "村级河长4联系电话", "村级河长4职务",
                "村级河长5", "村级河长5联系电话", "村级河长5职务",
                "村级河长6", "村级河长6联系电话", "村级河长6职务",
                "村级河长7", "村级河长7联系电话", "村级河长7职务"
        };
    }

    private static String[] villageOnlyRow() {
        String[] row = new String[31];
        row[0] = "1";
        row[1] = "三八村";
        row[2] = "真州镇";
        row[3] = "河道";
        row[4] = "村级";
        row[10] = "村干部甲";
        row[11] = "13800000000";
        row[12] = "村主任";
        return row;
    }

    private static String[] riverRowForCreate() {
        String[] row = new String[31];
        row[0] = "1";
        row[1] = "程营冲心河";
        row[2] = "枣林湾度假区";
        row[3] = "河道";
        row[4] = "县级";
        row[5] = "县长甲";
        row[6] = "县级职务";
        row[7] = "镇长乙";
        row[8] = "13900000000";
        row[9] = "镇级职务";
        return row;
    }

    private static String[] riverRowForOverwrite() {
        String[] row = new String[31];
        row[0] = "1";
        row[1] = "程营冲心河";
        row[2] = "枣林湾度假区";
        row[3] = "河道";
        row[4] = "县级";
        row[5] = "新县长";
        row[6] = "新县级职务";
        row[7] = "新镇长";
        row[8] = "13700000000";
        row[9] = "新镇级职务";
        return row;
    }

    private static String[] reservoirRowForAppend() {
        String[] row = new String[31];
        row[0] = "1";
        row[1] = "红旗水库";
        row[2] = "月塘镇";
        row[3] = "水库";
        row[4] = "县级";
        row[5] = "新库长";
        row[6] = "新库长职务";
        row[7] = "新镇库长";
        row[8] = "13600000000";
        row[9] = "新镇库长职务";
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
        private final Deque<List<YzRiverChannelManagementDO>> managementSelectResults = new ArrayDeque<>();
        private final List<YzRiverChannelDO> insertedRivers = new ArrayList<>();
        private final List<YzWaterReservoirDO> insertedReservoirs = new ArrayList<>();
        private final List<YzWaterFacilityBaseDO> insertedFacilityBases = new ArrayList<>();
        private final List<YzRiverChannelManagementDO> insertedManagementRecords = new ArrayList<>();
        private Object lastRiverSelectArg;
        private Object lastReservoirSelectArg;
        private int managementUpdateCount;

        private RiverChiefLatestExcelImportService buildService() {
            YzRiverChannelMapper riverChannelMapper = proxy(YzRiverChannelMapper.class, this::handleRiverChannelMapper);
            YzWaterReservoirMapper waterReservoirMapper = proxy(YzWaterReservoirMapper.class, this::handleReservoirMapper);
            YzWaterFacilityBaseMapper facilityBaseMapper = proxy(YzWaterFacilityBaseMapper.class, this::handleFacilityBaseMapper);
            YzRiverChannelManagementMapper managementMapper = proxy(YzRiverChannelManagementMapper.class, this::handleManagementMapper);
            SystemAreaMapper systemAreaMapper = proxy(SystemAreaMapper.class, this::handleAreaMapper);
            DictDataCommonApi dictDataApi = proxy(DictDataCommonApi.class, this::handleDictDataApi);
            return new RiverChiefLatestExcelImportService(
                    riverChannelMapper,
                    waterReservoirMapper,
                    facilityBaseMapper,
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
            if ("insert".equals(method.getName()) && args != null && args.length == 1) {
                insertedRivers.add((YzRiverChannelDO) args[0]);
                return 1;
            }
            return defaultValue(proxy, method, args);
        }

        private Object handleReservoirMapper(Object proxy, Method method, Object[] args) {
            if ("selectList".equals(method.getName())) {
                lastReservoirSelectArg = args == null || args.length == 0 ? null : args[0];
                return new ArrayList<>(existingReservoirs);
            }
            if ("insert".equals(method.getName()) && args != null && args.length == 1) {
                insertedReservoirs.add((YzWaterReservoirDO) args[0]);
                return 1;
            }
            return defaultValue(proxy, method, args);
        }

        private Object handleFacilityBaseMapper(Object proxy, Method method, Object[] args) {
            if ("insert".equals(method.getName()) && args != null && args.length == 1) {
                insertedFacilityBases.add((YzWaterFacilityBaseDO) args[0]);
                return 1;
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
