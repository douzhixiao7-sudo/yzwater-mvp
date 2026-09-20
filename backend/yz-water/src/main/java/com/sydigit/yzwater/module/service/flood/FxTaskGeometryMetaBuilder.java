package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

/**
 * 风险隐患点折线 GeoJSON 扩展元数据（geom_meta）构建与合并。
 */
final class FxTaskGeometryMetaBuilder {

    private static final int MAX_VERTEX_LABEL_LENGTH = 32;
    private static final int MAX_RISK_SEGMENT_NAME_LENGTH = 64;
    private static final int MAX_RISK_SEGMENT_DESCRIPTION_LENGTH = 512;
    private static final int MAX_LINE_POINT_COUNT = 500;
    private static final int MAX_LINE_COUNT = 20;
    private static final double SHARED_START_COORD_TOLERANCE = 1e-6;

    private FxTaskGeometryMetaBuilder() {
    }

    static ObjectNode extractInputMeta(JsonNode root, ObjectMapper objectMapper) {
        ObjectNode inputMeta = objectMapper.createObjectNode();
        if (root == null || !root.isObject()) {
            return inputMeta;
        }
        copyIfPresent(root, inputMeta, "vertexMarkers");
        copyIfPresent(root, inputMeta, "vertexLabels");
        copyIfPresent(root, inputMeta, "riskSegments");
        copyIfPresent(root, inputMeta, "riskSegmentNames");
        copyIfPresent(root, inputMeta, "riskSegmentDescriptions");
        copyIfPresent(root, inputMeta, "riskSegmentCenters");
        copyIfPresent(root, inputMeta, "linkedWarehouseId");
        copyIfPresent(root, inputMeta, "linkedWarehouseIds");
        copyIfPresent(root, inputMeta, "routeColor");
        copyIfPresent(root, inputMeta, "routeColors");
        return inputMeta;
    }

    static void validateLineGeometry(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }
        if (geometry instanceof MultiLineString multiLineString) {
            int lineCount = multiLineString.getNumGeometries();
            if (lineCount < 1) {
                throw ServiceExceptionUtil.invalidParamException("至少需要 1 条线段");
            }
            if (lineCount > MAX_LINE_COUNT) {
                throw ServiceExceptionUtil.invalidParamException("线段数量超出限制");
            }
            for (int i = 0; i < lineCount; i += 1) {
                validateSingleLineString((LineString) multiLineString.getGeometryN(i));
            }
            validateSharedHazardStartPoints(multiLineString);
            return;
        }
        if (geometry instanceof LineString lineString) {
            validateSingleLineString(lineString);
        }
    }

    private static void validateSingleLineString(LineString lineString) {
        int pointCount = lineString.getNumPoints();
        if (pointCount < 2) {
            throw ServiceExceptionUtil.invalidParamException("线段至少需要 2 个节点");
        }
        if (pointCount > MAX_LINE_POINT_COUNT) {
            throw ServiceExceptionUtil.invalidParamException("线段节点数量超出限制");
        }
    }

    private static void validateSharedHazardStartPoints(MultiLineString multiLineString) {
        int lineCount = multiLineString.getNumGeometries();
        if (lineCount <= 1) {
            return;
        }
        Coordinate sharedStart = ((LineString) multiLineString.getGeometryN(0)).getCoordinateN(0);
        for (int i = 1; i < lineCount; i += 1) {
            Coordinate start = ((LineString) multiLineString.getGeometryN(i)).getCoordinateN(0);
            if (!sameCoordinate(sharedStart, start)) {
                throw ServiceExceptionUtil.invalidParamException("多条物资路线须从同一隐患点出发");
            }
        }
    }

    private static boolean sameCoordinate(Coordinate left, Coordinate right) {
        return Math.abs(left.x - right.x) <= SHARED_START_COORD_TOLERANCE
                && Math.abs(left.y - right.y) <= SHARED_START_COORD_TOLERANCE;
    }

    static String buildGeomMetaJson(Geometry geometry, JsonNode inputMeta, ObjectMapper objectMapper) {
        if (geometry == null) {
            return null;
        }
        ObjectNode inputObject = normalizeInputMeta(inputMeta, objectMapper);
        if (geometry instanceof MultiLineString multiLineString) {
            return buildMultiLineGeomMetaJson(multiLineString, inputObject, objectMapper);
        }
        if (!"LineString".equalsIgnoreCase(geometry.getGeometryType())) {
            return inputObject.isEmpty() ? null : inputObject.toString();
        }
        LineString lineString = (LineString) geometry;
        int pointCount = lineString.getNumPoints();
        if (pointCount < 2) {
            return null;
        }
        ObjectNode meta = buildSingleLineMetaObject(lineString, inputObject, objectMapper, 0, false);
        copyWarehouseMetaFromInput(inputObject, meta);
        return meta.toString();
    }

    private static String buildMultiLineGeomMetaJson(
            MultiLineString multiLineString,
            ObjectNode inputObject,
            ObjectMapper objectMapper) {
        int lineCount = multiLineString.getNumGeometries();
        ObjectNode meta = objectMapper.createObjectNode();
        ArrayNode vertexMarkers = objectMapper.createArrayNode();
        ArrayNode vertexLabels = objectMapper.createArrayNode();
        ArrayNode riskSegments = objectMapper.createArrayNode();
        ArrayNode riskSegmentNames = objectMapper.createArrayNode();
        ArrayNode riskSegmentDescriptions = objectMapper.createArrayNode();
        ArrayNode riskSegmentCenters = objectMapper.createArrayNode();
        for (int i = 0; i < lineCount; i += 1) {
            LineString lineString = (LineString) multiLineString.getGeometryN(i);
            ObjectNode lineMeta = buildSingleLineMetaObject(lineString, inputObject, objectMapper, i, true);
            vertexMarkers.add(lineMeta.get("vertexMarkers"));
            vertexLabels.add(lineMeta.get("vertexLabels"));
            riskSegments.add(lineMeta.get("riskSegments"));
            riskSegmentNames.add(lineMeta.get("riskSegmentNames"));
            riskSegmentDescriptions.add(lineMeta.get("riskSegmentDescriptions"));
            riskSegmentCenters.add(lineMeta.get("riskSegmentCenters"));
        }
        meta.set("vertexMarkers", vertexMarkers);
        meta.set("vertexLabels", vertexLabels);
        meta.set("riskSegments", riskSegments);
        meta.set("riskSegmentNames", riskSegmentNames);
        meta.set("riskSegmentDescriptions", riskSegmentDescriptions);
        meta.set("riskSegmentCenters", riskSegmentCenters);
        copyWarehouseMetaFromInput(inputObject, meta);
        return meta.toString();
    }

    private static void copyWarehouseMetaFromInput(ObjectNode inputObject, ObjectNode meta) {
        copyIfPresent(inputObject, meta, "linkedWarehouseId");
        copyIfPresent(inputObject, meta, "linkedWarehouseIds");
        copyIfPresent(inputObject, meta, "routeColor");
        copyIfPresent(inputObject, meta, "routeColors");
    }

    private static ObjectNode buildSingleLineMetaObject(
            LineString lineString,
            ObjectNode inputObject,
            ObjectMapper objectMapper,
            int lineIndex,
            boolean multiLineMode) {
        int pointCount = lineString.getNumPoints();
        int segmentCount = pointCount - 1;
        Coordinate[] coordinates = lineString.getCoordinates();
        ObjectNode lineInput = extractLineInputObject(inputObject, lineIndex, multiLineMode, objectMapper);

        ObjectNode meta = objectMapper.createObjectNode();
        ArrayNode riskSegments = buildRiskSegments(lineInput, objectMapper, segmentCount);
        meta.set("vertexMarkers", buildVertexMarkers(lineInput, objectMapper, pointCount));
        meta.set("vertexLabels", buildVertexLabels(lineInput, objectMapper, pointCount));
        meta.set("riskSegments", riskSegments);
        meta.set("riskSegmentNames", buildRiskSegmentNames(lineInput, objectMapper, segmentCount, riskSegments));
        meta.set("riskSegmentDescriptions", buildRiskSegmentDescriptions(lineInput, objectMapper, segmentCount, riskSegments));
        meta.set("riskSegmentCenters", buildRiskSegmentCenters(objectMapper, segmentCount, riskSegments, coordinates));
        return meta;
    }

    static void mergeMetaToGeometryRoot(ObjectNode root, JsonNode metaNode) {
        if (metaNode == null || !metaNode.isObject()) {
            if (metaNode != null && metaNode.isArray()) {
                root.set("vertexMarkers", metaNode);
            }
            return;
        }
        copyIfPresent(metaNode, root, "vertexMarkers");
        copyIfPresent(metaNode, root, "vertexLabels");
        copyIfPresent(metaNode, root, "riskSegments");
        copyIfPresent(metaNode, root, "riskSegmentNames");
        copyIfPresent(metaNode, root, "riskSegmentDescriptions");
        copyIfPresent(metaNode, root, "riskSegmentCenters");
        copyIfPresent(metaNode, root, "linkedWarehouseId");
        copyIfPresent(metaNode, root, "linkedWarehouseIds");
        copyIfPresent(metaNode, root, "routeColor");
        copyIfPresent(metaNode, root, "routeColors");
    }

    private static ObjectNode normalizeInputMeta(JsonNode inputMeta, ObjectMapper objectMapper) {
        ObjectNode inputObject = objectMapper.createObjectNode();
        if (inputMeta == null) {
            return inputObject;
        }
        if (inputMeta.isObject()) {
            copyIfPresent(inputMeta, inputObject, "vertexMarkers");
            copyIfPresent(inputMeta, inputObject, "vertexLabels");
            copyIfPresent(inputMeta, inputObject, "riskSegments");
            copyIfPresent(inputMeta, inputObject, "riskSegmentNames");
            copyIfPresent(inputMeta, inputObject, "riskSegmentDescriptions");
            copyIfPresent(inputMeta, inputObject, "riskSegmentCenters");
            copyIfPresent(inputMeta, inputObject, "linkedWarehouseId");
            copyIfPresent(inputMeta, inputObject, "linkedWarehouseIds");
            copyIfPresent(inputMeta, inputObject, "routeColor");
            copyIfPresent(inputMeta, inputObject, "routeColors");
            return inputObject;
        }
        if (inputMeta.isArray()) {
            inputObject.set("vertexMarkers", inputMeta);
        }
        return inputObject;
    }

    private static ObjectNode extractLineInputObject(
            ObjectNode inputObject,
            int lineIndex,
            boolean multiLineMode,
            ObjectMapper objectMapper) {
        if (!multiLineMode) {
            return inputObject;
        }
        ObjectNode lineInput = objectMapper.createObjectNode();
        copyLineField(inputObject, lineInput, "vertexMarkers", lineIndex);
        copyLineField(inputObject, lineInput, "vertexLabels", lineIndex);
        copyLineField(inputObject, lineInput, "riskSegments", lineIndex);
        copyLineField(inputObject, lineInput, "riskSegmentNames", lineIndex);
        copyLineField(inputObject, lineInput, "riskSegmentDescriptions", lineIndex);
        copyLineField(inputObject, lineInput, "riskSegmentCenters", lineIndex);
        return lineInput;
    }

    private static void copyLineField(ObjectNode source, ObjectNode target, String field, int lineIndex) {
        if (!source.has(field)) {
            return;
        }
        JsonNode root = source.get(field);
        if (root == null || !root.isArray() || root.isEmpty()) {
            return;
        }
        if (root.get(0).isArray()) {
            if (lineIndex < root.size()) {
                target.set(field, root.get(lineIndex));
            }
            return;
        }
        if (lineIndex == 0) {
            target.set(field, root);
        }
    }

    private static ArrayNode buildVertexMarkers(ObjectNode inputObject, ObjectMapper objectMapper, int pointCount) {
        ArrayNode markers = objectMapper.createArrayNode();
        JsonNode inputMarkers = inputObject.get("vertexMarkers");
        for (int i = 0; i < pointCount; i += 1) {
            String marker = resolveMarkerType(inputMarkers, i, pointCount);
            markers.add(marker);
        }
        return markers;
    }

    private static String resolveMarkerType(JsonNode inputMarkers, int index, int pointCount) {
        if (inputMarkers != null && inputMarkers.isArray() && index < inputMarkers.size()) {
            String raw = StrUtil.trimToEmpty(inputMarkers.get(index).asText(""));
            if ("location".equals(raw) || "arrow".equals(raw) || "default".equals(raw)) {
                return raw;
            }
        }
        if (pointCount > 1 && (index == 0 || index == pointCount - 1)) {
            return "location";
        }
        return "arrow";
    }

    private static ArrayNode buildVertexLabels(ObjectNode inputObject, ObjectMapper objectMapper, int pointCount) {
        ArrayNode labels = objectMapper.createArrayNode();
        JsonNode inputLabels = inputObject.get("vertexLabels");
        for (int i = 0; i < pointCount; i += 1) {
            String label = "";
            if (inputLabels != null && inputLabels.isArray() && i < inputLabels.size()) {
                label = limitText(inputLabels.get(i).asText(""), MAX_VERTEX_LABEL_LENGTH);
            }
            labels.add(label);
        }
        return labels;
    }

    private static ArrayNode buildRiskSegments(ObjectNode inputObject, ObjectMapper objectMapper, int segmentCount) {
        ArrayNode riskSegments = objectMapper.createArrayNode();
        JsonNode inputRisk = inputObject.get("riskSegments");
        for (int i = 0; i < segmentCount; i += 1) {
            boolean risk = inputRisk != null && inputRisk.isArray() && i < inputRisk.size() && inputRisk.get(i).asBoolean(false);
            riskSegments.add(risk);
        }
        return riskSegments;
    }

    private static ArrayNode buildRiskSegmentNames(
            ObjectNode inputObject,
            ObjectMapper objectMapper,
            int segmentCount,
            ArrayNode riskSegments) {
        ArrayNode names = objectMapper.createArrayNode();
        JsonNode inputNames = inputObject.get("riskSegmentNames");
        for (int i = 0; i < segmentCount; i += 1) {
            boolean risk = riskSegments.get(i).asBoolean(false);
            String name = "";
            if (risk && inputNames != null && inputNames.isArray() && i < inputNames.size()) {
                name = limitText(inputNames.get(i).asText(""), MAX_RISK_SEGMENT_NAME_LENGTH);
            }
            names.add(name);
        }
        return names;
    }

    private static ArrayNode buildRiskSegmentDescriptions(
            ObjectNode inputObject,
            ObjectMapper objectMapper,
            int segmentCount,
            ArrayNode riskSegments) {
        ArrayNode descriptions = objectMapper.createArrayNode();
        JsonNode inputDescriptions = inputObject.get("riskSegmentDescriptions");
        for (int i = 0; i < segmentCount; i += 1) {
            boolean risk = riskSegments.get(i).asBoolean(false);
            String description = "";
            if (risk && inputDescriptions != null && inputDescriptions.isArray() && i < inputDescriptions.size()) {
                description = limitText(inputDescriptions.get(i).asText(""), MAX_RISK_SEGMENT_DESCRIPTION_LENGTH);
            }
            descriptions.add(description);
        }
        return descriptions;
    }

    private static ArrayNode buildRiskSegmentCenters(
            ObjectMapper objectMapper,
            int segmentCount,
            ArrayNode riskSegments,
            Coordinate[] coordinates) {
        ArrayNode centers = objectMapper.createArrayNode();
        for (int i = 0; i < segmentCount; i += 1) {
            if (!riskSegments.get(i).asBoolean(false)) {
                centers.addNull();
                continue;
            }
            Coordinate a = coordinates[i];
            Coordinate b = coordinates[i + 1];
            ArrayNode center = objectMapper.createArrayNode();
            center.add((a.x + b.x) / 2.0);
            center.add((a.y + b.y) / 2.0);
            centers.add(center);
        }
        return centers;
    }

    private static void copyIfPresent(JsonNode source, ObjectNode target, String field) {
        if (source.has(field)) {
            target.set(field, source.get(field));
        }
    }

    private static String limitText(String text, int maxLength) {
        return StrUtil.sub(StrUtil.trimToEmpty(text), 0, maxLength);
    }
}
