package com.sydigit.yzwater.module.service.flood;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxTaskGeometryMetaBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void shouldBuildGeomMetaWithLabelsAndRecomputedRiskCenters() throws Exception {
        LineString line = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4),
                new Coordinate(119.3, 32.5)
        });

        ObjectNode input = objectMapper.createObjectNode();
        input.putArray("vertexMarkers").add("location").add("arrow").add("location");
        input.putArray("vertexLabels").add("起点A").add("").add("终点B");
        input.putArray("riskSegments").add(false).add(true);
        input.putArray("riskSegmentNames").add("").add(" 坝段渗漏 ");
        input.putArray("riskSegmentDescriptions").add("").add(" 渗漏严重，需加固 ");
        input.putArray("riskSegmentCenters").addNull().addArray().add(0).add(0);

        String metaJson = FxTaskGeometryMetaBuilder.buildGeomMetaJson(line, input, objectMapper);
        JsonNode meta = objectMapper.readTree(metaJson);

        assertEquals("location", meta.get("vertexMarkers").get(0).asText());
        assertEquals("arrow", meta.get("vertexMarkers").get(1).asText());
        assertEquals("location", meta.get("vertexMarkers").get(2).asText());
        assertEquals("起点A", meta.get("vertexLabels").get(0).asText());
        assertEquals("终点B", meta.get("vertexLabels").get(2).asText());
        assertFalse(meta.get("riskSegments").get(0).asBoolean());
        assertTrue(meta.get("riskSegments").get(1).asBoolean());
        assertEquals("坝段渗漏", meta.get("riskSegmentNames").get(1).asText());
        assertEquals("渗漏严重，需加固", meta.get("riskSegmentDescriptions").get(1).asText());
        assertTrue(meta.get("riskSegmentCenters").get(0).isNull());
        assertEquals(119.25, meta.get("riskSegmentCenters").get(1).get(0).asDouble(), 0.0001);
        assertEquals(32.45, meta.get("riskSegmentCenters").get(1).get(1).asDouble(), 0.0001);
    }

    @Test
    void shouldExtractInputMetaFromGeoJsonRoot() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "LineString");
        root.putArray("coordinates");
        root.putArray("vertexLabels").add("起点").add("终点");
        root.putArray("riskSegments").add(true);

        ObjectNode extracted = FxTaskGeometryMetaBuilder.extractInputMeta(root, objectMapper);
        assertTrue(extracted.has("vertexLabels"));
        assertTrue(extracted.has("riskSegments"));
        assertFalse(extracted.has("type"));
    }

    @Test
    void shouldBuildMultiLineGeomMetaWithNestedArrays() throws Exception {
        LineString line1 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4)
        });
        LineString line2 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.3, 32.5),
                new Coordinate(119.4, 32.6),
                new Coordinate(119.5, 32.7)
        });
        MultiLineString multi = geometryFactory.createMultiLineString(new LineString[]{line1, line2});

        ObjectNode input = objectMapper.createObjectNode();
        ArrayNode line1Risk = objectMapper.createArrayNode().add(true);
        ArrayNode line2Risk = objectMapper.createArrayNode().add(false).add(true);
        input.set("riskSegments", objectMapper.createArrayNode().add(line1Risk).add(line2Risk));

        String metaJson = FxTaskGeometryMetaBuilder.buildGeomMetaJson(multi, input, objectMapper);
        JsonNode meta = objectMapper.readTree(metaJson);

        assertTrue(meta.get("riskSegments").get(0).get(0).asBoolean());
        assertTrue(meta.get("riskSegments").get(1).get(1).asBoolean());
        assertEquals(119.15, meta.get("riskSegmentCenters").get(0).get(0).get(0).asDouble(), 0.0001);
        assertEquals(119.45, meta.get("riskSegmentCenters").get(1).get(1).get(0).asDouble(), 0.0001);
    }

    @Test
    void shouldValidateSharedHazardStartPointsForMultiLineString() {
        LineString line1 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4)
        });
        LineString line2 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.4, 32.6)
        });
        MultiLineString multi = geometryFactory.createMultiLineString(new LineString[]{line1, line2});

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> FxTaskGeometryMetaBuilder.validateLineGeometry(multi));
    }

    @Test
    void shouldPersistLinkedWarehouseIdsInGeomMeta() throws Exception {
        LineString line = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4)
        });
        ObjectNode input = objectMapper.createObjectNode();
        input.put("linkedWarehouseId", "1847234567890123456");

        String metaJson = FxTaskGeometryMetaBuilder.buildGeomMetaJson(line, input, objectMapper);
        JsonNode meta = objectMapper.readTree(metaJson);
        assertEquals("1847234567890123456", meta.get("linkedWarehouseId").asText());

        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "LineString");
        FxTaskGeometryMetaBuilder.mergeMetaToGeometryRoot(root, meta);
        assertEquals("1847234567890123456", root.get("linkedWarehouseId").asText());
    }

    @Test
    void shouldRejectMultiLineStringWithDifferentStartPoints() {
        LineString line1 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.1, 32.3),
                new Coordinate(119.2, 32.4)
        });
        LineString line2 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(119.3, 32.5),
                new Coordinate(119.4, 32.6)
        });
        MultiLineString multi = geometryFactory.createMultiLineString(new LineString[]{line1, line2});

        org.junit.jupiter.api.Assertions.assertThrows(
                com.sydigit.yzwater.framework.common.exception.ServiceException.class,
                () -> FxTaskGeometryMetaBuilder.validateLineGeometry(multi));
    }
}
