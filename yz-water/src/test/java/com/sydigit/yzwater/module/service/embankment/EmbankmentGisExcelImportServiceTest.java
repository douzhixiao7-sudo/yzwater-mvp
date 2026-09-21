package com.sydigit.yzwater.module.service.embankment;

import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentGisImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.mysql.embankment.YzEmbankmentMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbankmentGisExcelImportServiceTest {

    @Mock
    private EmbankmentService embankmentService;
    @Mock
    private YzEmbankmentMapper embankmentMapper;
    @Mock
    private YzWaterFacilityBaseMapper facilityBaseMapper;

    private EmbankmentGisExcelImportService service;

    @BeforeEach
    void setUp() {
        service = new EmbankmentGisExcelImportService(embankmentService, embankmentMapper, facilityBaseMapper);
    }

    @Test
    void shouldImportExcelAndUpdateGeometryAndCenterPoint() throws IOException {
        MockMultipartFile file = buildExcelFile(List.of(
                new String[]{"胥浦河东堤", "408675.2", "3571178", "408875.2", "3571378"},
                new String[]{"胥浦河东堤", "408875.2", "3571378", "409275.2", "3571778"}
        ));

        YzEmbankmentDO embankment = new YzEmbankmentDO();
        embankment.setId(1001L);
        embankment.setFacilityId(2002L);
        embankment.setEmbankmentCode("DF001");
        embankment.setEmbankmentName("胥浦河东堤");
        embankment.setDivisionCode(new String[]{"321081"});

        when(embankmentMapper.selectList(any())).thenReturn(List.of(embankment));
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(2002L);
        when(facilityBaseMapper.selectById(2002L)).thenReturn(base);
        when(embankmentService.ensureFacilityBaseForExisting(embankment)).thenReturn(2002L);

        EmbankmentGisImportRespVO respVO = service.importExcel(file);

        assertEquals(2, respVO.getRowCount());
        assertEquals(1, respVO.getNameGroupCount());
        assertEquals(1, respVO.getSuccessCount());
        assertEquals(0, respVO.getSkipCount());
        assertEquals(1, respVO.getUpdateCount());
        assertEquals(0, respVO.getCreateFacilityCount());

        ArgumentCaptor<YzWaterFacilityBaseDO> baseCaptor = ArgumentCaptor.forClass(YzWaterFacilityBaseDO.class);
        verify(facilityBaseMapper, times(1)).clearGeomById(2002L);
        verify(facilityBaseMapper, times(1)).updateById(baseCaptor.capture());
        YzWaterFacilityBaseDO baseUpdate = baseCaptor.getValue();
        assertEquals(2002L, baseUpdate.getId());
        assertNotNull(baseUpdate.getGeom());
        assertEquals(4490, baseUpdate.getSrid());
        assertTrue(baseUpdate.getGeomType().contains("LineString"));

        ArgumentCaptor<YzEmbankmentDO> embankmentCaptor = ArgumentCaptor.forClass(YzEmbankmentDO.class);
        verify(embankmentMapper, times(1)).updateById(embankmentCaptor.capture());
        YzEmbankmentDO embankmentUpdate = embankmentCaptor.getValue();
        assertEquals(1001L, embankmentUpdate.getId());
        assertNotNull(embankmentUpdate.getLongitude());
        assertNotNull(embankmentUpdate.getLatitude());
        assertTrue(embankmentUpdate.getLongitude().compareTo(new BigDecimal("119")) > 0);
        assertTrue(embankmentUpdate.getLongitude().compareTo(new BigDecimal("120")) < 0);
        assertTrue(embankmentUpdate.getLatitude().compareTo(new BigDecimal("32")) > 0);
        assertTrue(embankmentUpdate.getLatitude().compareTo(new BigDecimal("33")) < 0);
    }

    @Test
    void shouldUpdateAllDuplicateEmbankmentsWhenNamesExistInDatabase() throws IOException {
        MockMultipartFile file = buildExcelFile(List.<String[]>of(
                new String[]{"胥浦河西堤", "408675.2", "3571178", "408875.2", "3571378"}
        ));

        YzEmbankmentDO first = new YzEmbankmentDO();
        first.setId(1L);
        first.setFacilityId(11L);
        first.setEmbankmentCode("DF-A");
        first.setEmbankmentName("胥浦河西堤");
        YzEmbankmentDO second = new YzEmbankmentDO();
        second.setId(2L);
        second.setFacilityId(null);
        second.setEmbankmentCode("DF-B");
        second.setEmbankmentName("胥浦河西堤");

        when(embankmentMapper.selectList(any())).thenReturn(List.of(first, second));
        YzWaterFacilityBaseDO firstBase = new YzWaterFacilityBaseDO();
        firstBase.setId(11L);
        when(facilityBaseMapper.selectById(11L)).thenReturn(firstBase);
        when(embankmentService.ensureFacilityBaseForExisting(first)).thenReturn(11L);
        when(embankmentService.ensureFacilityBaseForExisting(second)).thenReturn(22L);

        EmbankmentGisImportRespVO respVO = service.importExcel(file);

        assertEquals(1, respVO.getRowCount());
        assertEquals(1, respVO.getNameGroupCount());
        assertEquals(1, respVO.getSuccessCount());
        assertEquals(0, respVO.getSkipCount());
        assertEquals(2, respVO.getUpdateCount());
        assertEquals(1, respVO.getCreateFacilityCount());
        assertTrue(respVO.getItems() == null || respVO.getItems().isEmpty());

        verify(embankmentService, times(1)).ensureFacilityBaseForExisting(first);
        verify(embankmentService, times(1)).ensureFacilityBaseForExisting(second);
        verify(facilityBaseMapper, times(1)).clearGeomById(11L);
        verify(facilityBaseMapper, times(1)).clearGeomById(22L);
        verify(facilityBaseMapper, times(2)).updateById(any(YzWaterFacilityBaseDO.class));
        verify(embankmentMapper, times(2)).updateById(any(YzEmbankmentDO.class));
    }

    private static MockMultipartFile buildExcelFile(List<String[]> dataRows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"堤防名称", "起点坐标X", "起点坐标Y", "讫点坐标X", "讫点坐标Y"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < dataRows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                String[] values = dataRows.get(i);
                for (int j = 0; j < values.length; j++) {
                    row.createCell(j).setCellValue(values[j]);
                }
            }
            workbook.write(output);
            return new MockMultipartFile(
                    "file",
                    "堤防gis.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }
}
