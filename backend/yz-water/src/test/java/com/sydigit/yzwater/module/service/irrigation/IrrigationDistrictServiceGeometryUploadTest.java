package com.sydigit.yzwater.module.service.irrigation;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.irrigation.YzIrrigationDistrictDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.irrigation.YzIrrigationDistrictMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IrrigationDistrictServiceGeometryUploadTest {

    @Mock
    private YzIrrigationDistrictMapper irrigationDistrictMapper;
    @Mock
    private YzWaterFacilityBaseMapper facilityBaseMapper;
    @Mock
    private DictDataCommonApi dictDataApi;

    private IrrigationDistrictService service;

    @BeforeEach
    void setUp() {
        service = new IrrigationDistrictService(irrigationDistrictMapper, facilityBaseMapper, dictDataApi);
    }

    @Test
    void shouldGenerateDistrictCodeWhenCreate() {
        IrrigationDistrictSaveReqVO reqVO = new IrrigationDistrictSaveReqVO();
        reqVO.setIrrigationDistrictName("龙河灌区");

        service.create(reqVO);

        ArgumentCaptor<YzIrrigationDistrictDO> captor = ArgumentCaptor.forClass(YzIrrigationDistrictDO.class);
        verify(irrigationDistrictMapper, times(1)).insert(captor.capture());
        YzIrrigationDistrictDO inserted = captor.getValue();
        assertNotNull(inserted.getIrrigationDistrictCode());
        assertNotEquals("", inserted.getIrrigationDistrictCode().trim());
        assertEquals("龙河灌区", inserted.getIrrigationDistrictName());

        ArgumentCaptor<YzWaterFacilityBaseDO> baseCaptor = ArgumentCaptor.forClass(YzWaterFacilityBaseDO.class);
        verify(facilityBaseMapper, times(1)).insert(baseCaptor.capture());
        assertEquals(inserted.getIrrigationDistrictCode(), baseCaptor.getValue().getFacilityCode());
    }

    @Test
    void shouldReplaceExistingGeometryByDistrictName() {
        YzIrrigationDistrictDO district = new YzIrrigationDistrictDO();
        district.setId(1001L);
        district.setFacilityId(2002L);
        district.setIrrigationDistrictCode("GQ001");
        district.setIrrigationDistrictName("朱桥灌区");

        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(2002L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "geometry.json",
                "application/json",
                "{\"type\":\"Polygon\",\"coordinates\":[[[119.1,32.1],[119.2,32.1],[119.2,32.2],[119.1,32.1]]]}"
                        .getBytes(StandardCharsets.UTF_8));

        when(irrigationDistrictMapper.selectList(any())).thenReturn(List.of(district));
        when(facilityBaseMapper.selectById(2002L)).thenReturn(base);

        service.updateGeometryByDistrictName("朱桥灌区", file);

        verify(facilityBaseMapper, times(1)).clearGeomById(2002L);
        verify(irrigationDistrictMapper, times(1)).updateById(any(YzIrrigationDistrictDO.class));

        ArgumentCaptor<YzWaterFacilityBaseDO> captor = ArgumentCaptor.forClass(YzWaterFacilityBaseDO.class);
        verify(facilityBaseMapper, atLeastOnce()).updateById(captor.capture());
        YzWaterFacilityBaseDO geometryUpdate = captor.getAllValues().stream()
                .filter(item -> item.getGeom() != null)
                .findFirst()
                .orElse(null);

        assertNotNull(geometryUpdate);
        assertEquals(2002L, geometryUpdate.getId());
        assertEquals("Polygon", geometryUpdate.getGeomType());
        assertEquals(4490, geometryUpdate.getSrid());
    }

    @Test
    void shouldRejectDuplicateDistrictNames() {
        YzIrrigationDistrictDO first = new YzIrrigationDistrictDO();
        first.setId(1L);
        first.setIrrigationDistrictName("月塘灌区");
        YzIrrigationDistrictDO second = new YzIrrigationDistrictDO();
        second.setId(2L);
        second.setIrrigationDistrictName("月塘灌区");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "geometry.json",
                "application/json",
                "{\"type\":\"Polygon\",\"coordinates\":[[[119.1,32.1],[119.2,32.1],[119.2,32.2],[119.1,32.1]]]}"
                        .getBytes(StandardCharsets.UTF_8));
        when(irrigationDistrictMapper.selectList(any())).thenReturn(List.of(first, second));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateGeometryByDistrictName("月塘灌区", file));
        assertEquals("存在多个名称为【月塘灌区】的灌区，请先处理重名数据", ex.getMessage());
    }
}
