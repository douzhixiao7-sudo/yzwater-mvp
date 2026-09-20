package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxTaskSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxTaskDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxTaskMapper;
import org.geotools.geojson.geom.GeometryJSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.locationtech.jts.geom.MultiLineString;

@ExtendWith(MockitoExtension.class)
class FxTaskServiceMultiLineGeometryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private YzFxTaskMapper taskMapper;

    @InjectMocks
    private FxTaskService fxTaskService;

    @BeforeEach
    void setUp() {
        fxTaskService = new FxTaskService(taskMapper, null, objectMapper);
    }

    @Test
    void shouldParseMultiLineStringGeoJson() throws Exception {
        String geoJson = """
                {"type":"MultiLineString","coordinates":[[[119.1,32.3],[119.2,32.4]],[[119.3,32.5],[119.4,32.6],[119.5,32.7]]],
                "vertexMarkers":[["location","location"],["location","arrow","location"]],
                "riskSegments":[[true],[false,true]],"riskSegmentNames":[["段1"],["","段2"]]}
                """;
        Geometry geometry = new GeometryJSON().read("""
                {"type":"MultiLineString","coordinates":[[[119.1,32.3],[119.2,32.4]],[[119.3,32.5],[119.4,32.6],[119.5,32.7]]]}
                """);
        assertInstanceOf(MultiLineString.class, geometry);
        assertEquals("MultiLineString", geometry.getGeometryType());
    }

    @Test
    void shouldPersistMultiLineStringTaskGeometry() {
        when(taskMapper.insert(any(YzFxTaskDO.class))).thenAnswer(invocation -> {
            YzFxTaskDO insert = invocation.getArgument(0);
            insert.setId(IdUtil.fastSimpleUUID());
            return 1;
        });

        FxTaskSaveReqVO req = new FxTaskSaveReqVO();
        req.setName("测试险工");
        req.setAddr("仪征测试");
        req.setContent("险情");
        req.setGeometryGeoJson("""
                {"type":"MultiLineString","coordinates":[[[119.1,32.3],[119.2,32.4]],[[119.1,32.3],[119.4,32.6]]],
                "riskSegments":[[true],[false]],"riskSegmentNames":[["坝段"],[""]]}
                """);

        fxTaskService.create(req);

        ArgumentCaptor<YzFxTaskDO> captor = ArgumentCaptor.forClass(YzFxTaskDO.class);
        verify(taskMapper).insert(captor.capture());
        YzFxTaskDO saved = captor.getValue();
        assertNotNull(saved.getGeom());
        assertInstanceOf(MultiLineString.class, saved.getGeom());
        assertNotNull(saved.getGeomMeta());
    }
}
