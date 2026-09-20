package com.sydigit.yzwater.module.service.flood;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardLayerRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardMaterialRouteItemRespVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxTaskDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FxTaskScreenLayerServiceTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private YzFxTaskMapper taskMapper;
    @Mock
    private FxTaskService fxTaskService;
    @Mock
    private FxWzService fxWzService;
    @Mock
    private DictDataCommonApi dictDataApi;

    private FxTaskScreenLayerService screenLayerService;

    @BeforeEach
    void setUp() {
        screenLayerService = new FxTaskScreenLayerService(taskMapper, fxTaskService, fxWzService, dictDataApi, objectMapper);
    }

    @Test
    void shouldBuildLayerItemWithHazardPointAndMaterialRoute() throws Exception {
        LineString line = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4),
                new Coordinate(119.3, 32.5)
        });
        String geomMeta = """
                {"vertexMarkers":["location","arrow","location"],"vertexLabels":["东坝隐患点","","仓库A"],
                "riskSegments":[false,false],"riskSegmentNames":["",""],"riskSegmentCenters":[null,null],
                "linkedWarehouseId":"mat-001"}
                """;
        String geometryGeoJson = fxTaskServiceRealisticGeoJson(line, geomMeta);

        YzFxWzDO material = buildMaterial("mat-001", "编织袋", "城东仓库", "城东物资点", 119.3, 32.5);
        when(fxWzService.listMaterialsAtSameLocation("mat-001")).thenReturn(List.of(material));
        when(fxWzService.findMaterialById("mat-001")).thenReturn(material);

        YzFxTaskDO task = buildTask("task-001", line, geomMeta);
        task.setRiverChannelId(10001L);
        task.setRiverChannelName("仪征河");

        when(taskMapper.selectListWithGeomOrderBySort()).thenReturn(List.of(task));
        when(fxTaskService.toGeometryGeoJson(eq(line), eq(geomMeta))).thenReturn(geometryGeoJson);
        when(dictDataApi.getDictDataList("zd_fxdj")).thenReturn(List.of());

        BigScreenFxRiskHazardLayerRespVO layer = screenLayerService.getRiskHazardLayer();

        assertEquals(BigScreenFxRiskHazardLayerRespVO.LAYER_CODE, layer.getLayerCode());
        assertEquals(BigScreenFxRiskHazardLayerRespVO.LAYER_NAME, layer.getLayerName());
        assertEquals(1, layer.getItemCount());

        BigScreenFxRiskHazardItemRespVO item = layer.getItems().get(0);
        assertEquals("task-001", item.getTaskId());
        assertEquals("测试险工", item.getName());
        assertEquals("仪征河", item.getRiverChannelName());
        assertEquals("10001", item.getRiverChannelId());
        assertEquals("仪征测试位置", item.getAddr());
        assertEquals("险情", item.getContent());
        assertEquals("措施", item.getCounterMeasures());
        assertEquals("东坝隐患点", item.getHazardPointName());
        assertEquals(0, item.getHazardLongitude().compareTo(java.math.BigDecimal.valueOf(119.1)));

        assertEquals(1, item.getMaterialRoutes().size());
        BigScreenFxRiskHazardMaterialRouteItemRespVO route = item.getMaterialRoutes().get(0);
        assertEquals(0, route.getRouteIndex());
        assertEquals("mat-001", route.getMaterialId());
        assertEquals("城东物资点", route.getWarehouseName());
        assertEquals("城东物资点", route.getWarehouseAddress());
        assertEquals("城东仓库", route.getStorageUnit());
        assertEquals(1, route.getMaterials().size());
        assertEquals("编织袋", route.getMaterials().get(0).getMaterialName());
        assertEquals(3, route.getCoordinates().size());
        assertEquals(0, route.getEndLongitude().compareTo(java.math.BigDecimal.valueOf(119.3)));

        assertNotNull(item.getGeometryGeoJson());
        JsonNode root = objectMapper.readTree(item.getGeometryGeoJson());
        assertTrue(root.get("coordinates").isArray());
    }

    @Test
    void shouldBuildPointItemWhenGeometryIsPoint() {
        Point point = geometryFactory.createPoint(new Coordinate(119.18, 32.28));
        YzFxTaskDO task = buildTask("task-point", point, null);
        task.setName("点状隐患");
        String geoJson = "{\"type\":\"Point\",\"coordinates\":[119.18,32.28]}";

        when(taskMapper.selectListWithGeomOrderBySort()).thenReturn(List.of(task));
        when(fxTaskService.toGeometryGeoJson(eq(point), any())).thenReturn(geoJson);
        when(dictDataApi.getDictDataList("zd_fxdj")).thenReturn(List.of());

        BigScreenFxRiskHazardLayerRespVO layer = screenLayerService.getRiskHazardLayer();

        assertEquals(1, layer.getItemCount());
        BigScreenFxRiskHazardItemRespVO item = layer.getItems().get(0);
        assertEquals("task-point", item.getTaskId());
        assertEquals(0, item.getHazardLongitude().compareTo(java.math.BigDecimal.valueOf(119.18)));
        assertTrue(item.getMaterialRoutes().isEmpty());
    }

    @Test
    void shouldBuildMultiLineMaterialRoutesFromSharedHazardPoint() {
        LineString line1 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4)
        });
        LineString line2 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.4, 32.6),
                new Coordinate(119.5, 32.7)
        });
        MultiLineString multiLine = geometryFactory.createMultiLineString(new LineString[]{line1, line2});
        String geoJson = """
                {"type":"MultiLineString","coordinates":[[[119.1,32.3],[119.2,32.4]],[[119.1,32.3],[119.4,32.6],[119.5,32.7]]],
                "vertexLabels":[["东坝隐患点","仓库A"],["东坝隐患点","拐点","仓库B"]],
                "linkedWarehouseIds":["mat-a","mat-b"]}
                """;

        YzFxWzDO materialA = buildMaterial("mat-a", "编织袋", "仓库A单位", "仓库A地址", 119.2, 32.4);
        YzFxWzDO materialB = buildMaterial("mat-b", "木桩", "仓库B单位", "仓库B地址", 119.5, 32.7);
        when(fxWzService.listMaterialsAtSameLocation("mat-a")).thenReturn(List.of(materialA));
        when(fxWzService.listMaterialsAtSameLocation("mat-b")).thenReturn(List.of(materialB));
        when(fxWzService.findMaterialById("mat-a")).thenReturn(materialA);
        when(fxWzService.findMaterialById("mat-b")).thenReturn(materialB);

        YzFxTaskDO task = buildTask("task-multi", multiLine, null);
        when(taskMapper.selectListWithGeomOrderBySort()).thenReturn(List.of(task));
        when(fxTaskService.toGeometryGeoJson(eq(multiLine), any())).thenReturn(geoJson);
        when(dictDataApi.getDictDataList("zd_fxdj")).thenReturn(List.of());

        BigScreenFxRiskHazardLayerRespVO layer = screenLayerService.getRiskHazardLayer();

        assertEquals(1, layer.getItemCount());
        BigScreenFxRiskHazardItemRespVO item = layer.getItems().get(0);
        assertEquals("东坝隐患点", item.getHazardPointName());
        assertEquals(2, item.getMaterialRoutes().size());
        assertEquals("仓库A地址", item.getMaterialRoutes().get(0).getWarehouseName());
        assertEquals("仓库B地址", item.getMaterialRoutes().get(1).getWarehouseName());
        assertEquals(2, item.getMaterialRoutes().get(0).getCoordinates().size());
        assertEquals(3, item.getMaterialRoutes().get(1).getCoordinates().size());
    }

    @Test
    void shouldFallbackToEndpointMaterialsWhenLinkedIdMissing() {
        LineString line = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4)
        });
        String geoJson = """
                {"type":"LineString","coordinates":[[119.1,32.3],[119.2,32.4]],
                "vertexLabels":["东坝隐患点","城东物资点"]}
                """;
        YzFxWzDO material = buildMaterial("mat-end", "沙袋", "城东仓库", "城东物资点", 119.2, 32.4);
        when(fxWzService.listMaterialsAtEndpoint(any(BigDecimal.class), any(BigDecimal.class), eq("城东物资点")))
                .thenReturn(List.of(material));

        YzFxTaskDO task = buildTask("task-fallback", line, null);
        when(taskMapper.selectListWithGeomOrderBySort()).thenReturn(List.of(task));
        when(fxTaskService.toGeometryGeoJson(eq(line), any())).thenReturn(geoJson);
        when(dictDataApi.getDictDataList("zd_fxdj")).thenReturn(List.of());

        BigScreenFxRiskHazardLayerRespVO layer = screenLayerService.getRiskHazardLayer();

        assertEquals(1, layer.getItems().get(0).getMaterialRoutes().size());
        assertEquals("沙袋", layer.getItems().get(0).getMaterialRoutes().get(0).getMaterials().get(0).getMaterialName());
    }

    private YzFxTaskDO buildTask(String id, org.locationtech.jts.geom.Geometry geometry, String geomMeta) {
        YzFxTaskDO task = new YzFxTaskDO();
        task.setId(id);
        task.setCode("FXYH-TEST01");
        task.setName("测试险工");
        task.setAddr("仪征测试位置");
        task.setLevel("1");
        task.setContent("险情");
        task.setCounterMeasures("措施");
        task.setGeom(geometry);
        task.setGeomMeta(geomMeta);
        return task;
    }

    private YzFxWzDO buildMaterial(String id,
                                   String materialName,
                                   String storageUnit,
                                   String warehouseAddress,
                                   double lng,
                                   double lat) {
        YzFxWzDO item = new YzFxWzDO();
        item.setId(id);
        item.setMaterialName(materialName);
        item.setStorageUnit(storageUnit);
        item.setWarehouseAddress(warehouseAddress);
        item.setQuantity(BigDecimal.ONE);
        item.setLongitude(BigDecimal.valueOf(lng));
        item.setLatitude(BigDecimal.valueOf(lat));
        return item;
    }

    private String fxTaskServiceRealisticGeoJson(LineString line, String geomMeta) throws Exception {
        FxTaskService service = new FxTaskService(null, null, objectMapper);
        return service.toGeometryGeoJson(line, geomMeta);
    }
}
