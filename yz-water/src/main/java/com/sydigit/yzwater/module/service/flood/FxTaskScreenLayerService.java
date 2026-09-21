package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardLayerRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardMaterialRouteItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardRouteMaterialItemRespVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxTaskDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 风险隐患点大屏 GIS 图层（只读）
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxTaskScreenLayerService {

    private static final String DICT_FX_LEVEL = "zd_fxdj";
    private static final String DEFAULT_HAZARD_POINT_NAME = "隐患点";
    private static final int COORD_SCALE = 8;
    private static final double MIN_LONGITUDE = -180D;
    private static final double MAX_LONGITUDE = 180D;
    private static final double MIN_LATITUDE = -90D;
    private static final double MAX_LATITUDE = 90D;
    /** 大屏单次返回任务上限，防止异常数据拖垮内存 */
    private static final int MAX_SCREEN_TASKS = 2_000;

    private final YzFxTaskMapper taskMapper;
    private final FxTaskService fxTaskService;
    private final FxWzService fxWzService;
    private final DictDataCommonApi dictDataApi;
    private final ObjectMapper objectMapper;

    /**
     * 风险隐患点图层：每条任务聚合业务字段、隐患点坐标与完整物资调运路线。
     */
    public BigScreenFxRiskHazardLayerRespVO getRiskHazardLayer() {
        List<YzFxTaskDO> tasks = taskMapper.selectListWithGeomOrderBySort();
        if (tasks.size() > MAX_SCREEN_TASKS) {
            tasks = tasks.subList(0, MAX_SCREEN_TASKS);
        }

        Map<String, String> levelLabelMap = loadLevelLabelMap();
        List<BigScreenFxRiskHazardItemRespVO> items = new ArrayList<>();
        for (YzFxTaskDO task : tasks) {
            BigScreenFxRiskHazardItemRespVO item = buildItem(task, levelLabelMap);
            if (item != null) {
                items.add(item);
            }
        }

        BigScreenFxRiskHazardLayerRespVO resp = new BigScreenFxRiskHazardLayerRespVO();
        resp.setLayerCode(BigScreenFxRiskHazardLayerRespVO.LAYER_CODE);
        resp.setLayerName(BigScreenFxRiskHazardLayerRespVO.LAYER_NAME);
        resp.setItemCount(items.size());
        resp.setItems(items);
        return resp;
    }

    private BigScreenFxRiskHazardItemRespVO buildItem(YzFxTaskDO task, Map<String, String> levelLabelMap) {
        if (task == null || task.getGeom() == null || task.getGeom().isEmpty()) {
            return null;
        }
        String taskId = StrUtil.trimToNull(task.getId());
        if (taskId == null) {
            return null;
        }
        String geometryGeoJson = fxTaskService.toGeometryGeoJson(task.getGeom(), task.getGeomMeta());
        if (StrUtil.isBlank(geometryGeoJson)) {
            return null;
        }

        BigScreenFxRiskHazardItemRespVO item = new BigScreenFxRiskHazardItemRespVO();
        fillTaskFields(item, task, levelLabelMap);
        item.setGeometryGeoJson(geometryGeoJson);

        try {
            JsonNode root = objectMapper.readTree(geometryGeoJson);
            HazardPointSnapshot hazardPoint = resolveHazardPoint(root);
            if (hazardPoint != null) {
                item.setHazardLongitude(toBigDecimal(hazardPoint.longitude()));
                item.setHazardLatitude(toBigDecimal(hazardPoint.latitude()));
                item.setHazardPointName(hazardPoint.name());
            }
            item.setMaterialRoutes(buildMaterialRoutes(root));
        } catch (Exception ignored) {
            item.setMaterialRoutes(List.of());
        }
        return item;
    }

    private void fillTaskFields(BigScreenFxRiskHazardItemRespVO target,
                                YzFxTaskDO task,
                                Map<String, String> levelLabelMap) {
        target.setTaskId(task.getId());
        target.setCode(StrUtil.trimToNull(task.getCode()));
        target.setName(StrUtil.trimToNull(task.getName()));
        target.setRiverChannelId(task.getRiverChannelId() == null ? null : String.valueOf(task.getRiverChannelId()));
        target.setRiverChannelName(StrUtil.trimToNull(task.getRiverChannelName()));
        target.setAddr(StrUtil.trimToNull(task.getAddr()));
        target.setLevel(StrUtil.trimToNull(task.getLevel()));
        target.setLevelLabel(resolveLevelLabel(task.getLevel(), levelLabelMap));
        target.setContent(StrUtil.trimToNull(task.getContent()));
        target.setCounterMeasures(StrUtil.trimToNull(task.getCounterMeasures()));
    }

    private static HazardPointSnapshot resolveHazardPoint(JsonNode root) {
        if (root == null || !root.hasNonNull("type")) {
            return null;
        }
        return switch (root.get("type").asText()) {
            case "Point" -> resolvePointHazard(root);
            case "LineString" -> resolveLineHazard(root, 0);
            case "MultiLineString" -> resolveLineHazard(root, 0);
            default -> null;
        };
    }

    private static HazardPointSnapshot resolvePointHazard(JsonNode root) {
        double[] coordinate = parseCoordinate(root.get("coordinates"));
        if (coordinate == null) {
            return null;
        }
        return new HazardPointSnapshot(coordinate[0], coordinate[1], null);
    }

    private static HazardPointSnapshot resolveLineHazard(JsonNode root, int lineIndex) {
        JsonNode coordinates = pickLineCoordinates(root, lineIndex);
        if (coordinates == null || !coordinates.isArray() || coordinates.isEmpty()) {
            return null;
        }
        double[] start = parseCoordinate(coordinates.get(0));
        if (start == null) {
            return null;
        }
        JsonNode vertexLabels = pickLineArrayNode(root.get("vertexLabels"), lineIndex);
        return new HazardPointSnapshot(start[0], start[1], resolveStartLabel(vertexLabels));
    }

    private List<BigScreenFxRiskHazardMaterialRouteItemRespVO> buildMaterialRoutes(JsonNode root) {
        if (root == null || !root.hasNonNull("type")) {
            return List.of();
        }
        return switch (root.get("type").asText()) {
            case "LineString" -> buildLineMaterialRoutes(root, root.get("coordinates"), root.get("vertexLabels"), 0);
            case "MultiLineString" -> buildMultiLineMaterialRoutes(root);
            default -> List.of();
        };
    }

    private List<BigScreenFxRiskHazardMaterialRouteItemRespVO> buildMultiLineMaterialRoutes(JsonNode root) {
        JsonNode coordinates = root.get("coordinates");
        if (coordinates == null || !coordinates.isArray() || coordinates.isEmpty()) {
            return List.of();
        }
        List<BigScreenFxRiskHazardMaterialRouteItemRespVO> routes = new ArrayList<>();
        for (int lineIndex = 0; lineIndex < coordinates.size(); lineIndex += 1) {
            routes.addAll(buildLineMaterialRoutes(
                    root,
                    coordinates.get(lineIndex),
                    pickLineArrayNode(root.get("vertexLabels"), lineIndex),
                    lineIndex));
        }
        return routes;
    }

    private List<BigScreenFxRiskHazardMaterialRouteItemRespVO> buildLineMaterialRoutes(JsonNode root,
                                                                                        JsonNode lineCoordinates,
                                                                                        JsonNode vertexLabels,
                                                                                        int routeIndex) {
        BigScreenFxRiskHazardMaterialRouteItemRespVO route = buildSingleMaterialRoute(
                root,
                lineCoordinates,
                vertexLabels,
                routeIndex);
        return route == null ? List.of() : List.of(route);
    }

    private BigScreenFxRiskHazardMaterialRouteItemRespVO buildSingleMaterialRoute(JsonNode root,
                                                                                  JsonNode lineCoordinates,
                                                                                  JsonNode vertexLabels,
                                                                                  int routeIndex) {
        if (lineCoordinates == null || !lineCoordinates.isArray() || lineCoordinates.size() < 2) {
            return null;
        }
        List<List<BigDecimal>> coordinates = new ArrayList<>();
        for (JsonNode node : lineCoordinates) {
            double[] coordinate = parseCoordinate(node);
            if (coordinate == null) {
                return null;
            }
            coordinates.add(List.of(toBigDecimal(coordinate[0]), toBigDecimal(coordinate[1])));
        }
        double[] end = parseCoordinate(lineCoordinates.get(lineCoordinates.size() - 1));
        if (end == null) {
            return null;
        }

        String linkedMaterialId = resolveLinkedMaterialId(root, routeIndex);
        String endLabel = resolveEndLabel(vertexLabels);
        List<YzFxWzDO> materials = resolveRouteMaterials(linkedMaterialId, end[0], end[1], endLabel);
        YzFxWzDO anchorMaterial = pickAnchorMaterial(linkedMaterialId, materials);

        BigScreenFxRiskHazardMaterialRouteItemRespVO route = new BigScreenFxRiskHazardMaterialRouteItemRespVO();
        route.setRouteIndex(routeIndex);
        route.setMaterialId(anchorMaterial == null ? linkedMaterialId : anchorMaterial.getId());
        route.setWarehouseName(resolveWarehouseDisplayName(anchorMaterial, materials, endLabel));
        route.setWarehouseAddress(resolveWarehouseAddress(anchorMaterial, materials));
        route.setStorageUnit(resolveStorageUnit(anchorMaterial, materials));
        route.setEndLongitude(resolveEndLongitude(anchorMaterial, end[0]));
        route.setEndLatitude(resolveEndLatitude(anchorMaterial, end[1]));
        route.setMaterials(buildRouteMaterialItems(materials));
        route.setCoordinates(coordinates);
        return route;
    }

    private List<YzFxWzDO> resolveRouteMaterials(String linkedMaterialId, double endLng, double endLat, String endLabel) {
        if (StrUtil.isNotBlank(linkedMaterialId)) {
            List<YzFxWzDO> linkedMaterials = fxWzService.listMaterialsAtSameLocation(linkedMaterialId);
            if (!linkedMaterials.isEmpty()) {
                return linkedMaterials;
            }
        }
        return fxWzService.listMaterialsAtEndpoint(
                toBigDecimal(endLng),
                toBigDecimal(endLat),
                endLabel);
    }

    private YzFxWzDO pickAnchorMaterial(String linkedMaterialId, List<YzFxWzDO> materials) {
        if (StrUtil.isNotBlank(linkedMaterialId)) {
            YzFxWzDO linkedMaterial = fxWzService.findMaterialById(linkedMaterialId);
            if (linkedMaterial != null) {
                return linkedMaterial;
            }
        }
        return materials.isEmpty() ? null : materials.get(0);
    }

    private List<BigScreenFxRiskHazardRouteMaterialItemRespVO> buildRouteMaterialItems(List<YzFxWzDO> materials) {
        if (materials == null || materials.isEmpty()) {
            return List.of();
        }
        List<BigScreenFxRiskHazardRouteMaterialItemRespVO> items = new ArrayList<>();
        for (YzFxWzDO material : materials) {
            if (material == null || StrUtil.isBlank(material.getId())) {
                continue;
            }
            BigScreenFxRiskHazardRouteMaterialItemRespVO item = new BigScreenFxRiskHazardRouteMaterialItemRespVO();
            item.setMaterialId(material.getId());
            item.setMaterialName(StrUtil.trimToNull(material.getMaterialName()));
            item.setQuantity(material.getQuantity());
            item.setUnit(StrUtil.trimToNull(material.getUnit()));
            item.setStorageUnit(StrUtil.trimToNull(material.getStorageUnit()));
            item.setWarehouseAddress(StrUtil.trimToNull(material.getWarehouseAddress()));
            item.setLongitude(material.getLongitude());
            item.setLatitude(material.getLatitude());
            items.add(item);
        }
        return items;
    }

    private static String resolveWarehouseDisplayName(YzFxWzDO anchorMaterial,
                                                      List<YzFxWzDO> materials,
                                                      String fallbackLabel) {
        String address = resolveWarehouseAddress(anchorMaterial, materials);
        if (address != null) {
            return address;
        }
        String storageUnit = resolveStorageUnit(anchorMaterial, materials);
        if (storageUnit != null) {
            return storageUnit;
        }
        return StrUtil.trimToNull(fallbackLabel);
    }

    private static String resolveWarehouseAddress(YzFxWzDO anchorMaterial, List<YzFxWzDO> materials) {
        if (anchorMaterial != null) {
            String address = StrUtil.trimToNull(anchorMaterial.getWarehouseAddress());
            if (address != null) {
                return address;
            }
        }
        for (YzFxWzDO material : materials) {
            String address = StrUtil.trimToNull(material.getWarehouseAddress());
            if (address != null) {
                return address;
            }
        }
        return null;
    }

    private static String resolveStorageUnit(YzFxWzDO anchorMaterial, List<YzFxWzDO> materials) {
        if (anchorMaterial != null) {
            String storageUnit = StrUtil.trimToNull(anchorMaterial.getStorageUnit());
            if (storageUnit != null) {
                return storageUnit;
            }
        }
        for (YzFxWzDO material : materials) {
            String storageUnit = StrUtil.trimToNull(material.getStorageUnit());
            if (storageUnit != null) {
                return storageUnit;
            }
        }
        return null;
    }

    private static BigDecimal resolveEndLongitude(YzFxWzDO anchorMaterial, double fallbackLng) {
        if (anchorMaterial != null && anchorMaterial.getLongitude() != null) {
            return anchorMaterial.getLongitude();
        }
        return toBigDecimal(fallbackLng);
    }

    private static BigDecimal resolveEndLatitude(YzFxWzDO anchorMaterial, double fallbackLat) {
        if (anchorMaterial != null && anchorMaterial.getLatitude() != null) {
            return anchorMaterial.getLatitude();
        }
        return toBigDecimal(fallbackLat);
    }

    private static String resolveLinkedMaterialId(JsonNode root, int routeIndex) {
        JsonNode linkedWarehouseIds = root.get("linkedWarehouseIds");
        if (linkedWarehouseIds != null && linkedWarehouseIds.isArray() && routeIndex < linkedWarehouseIds.size()) {
            String linkedId = textOrNull(linkedWarehouseIds.get(routeIndex));
            if (linkedId != null) {
                return linkedId;
            }
        }
        if (routeIndex == 0) {
            return textOrNull(root.get("linkedWarehouseId"));
        }
        return null;
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return StrUtil.trimToNull(node.asText());
    }

    private static String resolveStartLabel(JsonNode vertexLabels) {
        if (vertexLabels == null || !vertexLabels.isArray() || vertexLabels.isEmpty() || vertexLabels.get(0).isNull()) {
            return DEFAULT_HAZARD_POINT_NAME;
        }
        String label = StrUtil.trimToNull(vertexLabels.get(0).asText());
        return label != null ? label : DEFAULT_HAZARD_POINT_NAME;
    }

    private static String resolveEndLabel(JsonNode vertexLabels) {
        if (vertexLabels == null || !vertexLabels.isArray() || vertexLabels.isEmpty()) {
            return null;
        }
        JsonNode endNode = vertexLabels.get(vertexLabels.size() - 1);
        if (endNode == null || endNode.isNull()) {
            return null;
        }
        return StrUtil.trimToNull(endNode.asText());
    }

    private static JsonNode pickLineCoordinates(JsonNode root, int lineIndex) {
        JsonNode coordinates = root.get("coordinates");
        if (coordinates == null || !coordinates.isArray() || coordinates.isEmpty()) {
            return null;
        }
        if ("MultiLineString".equals(root.path("type").asText())) {
            return lineIndex < coordinates.size() ? coordinates.get(lineIndex) : null;
        }
        return lineIndex == 0 ? coordinates : null;
    }

    private static JsonNode pickLineArrayNode(JsonNode root, int lineIndex) {
        if (root == null || !root.isArray() || root.isEmpty()) {
            return null;
        }
        if (root.get(0).isArray()) {
            return lineIndex < root.size() ? root.get(lineIndex) : null;
        }
        return lineIndex == 0 ? root : null;
    }

    private static String resolveLevelLabel(String level, Map<String, String> levelLabelMap) {
        String key = StrUtil.trimToNull(level);
        if (key == null) {
            return null;
        }
        return levelLabelMap.getOrDefault(key, key);
    }

    private static double[] parseCoordinate(JsonNode node) {
        if (node == null || node.isNull() || !node.isArray() || node.size() < 2) {
            return null;
        }
        double lng = node.get(0).asDouble(Double.NaN);
        double lat = node.get(1).asDouble(Double.NaN);
        if (!Double.isFinite(lng) || !Double.isFinite(lat)) {
            return null;
        }
        if (lng < MIN_LONGITUDE || lng > MAX_LONGITUDE || lat < MIN_LATITUDE || lat > MAX_LATITUDE) {
            return null;
        }
        return new double[]{lng, lat};
    }

    private static BigDecimal toBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(COORD_SCALE, RoundingMode.HALF_UP);
    }

    private Map<String, String> loadLevelLabelMap() {
        List<DictDataRespDTO> dictList = dictDataApi.getDictDataList(DICT_FX_LEVEL);
        if (dictList == null || dictList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (DictDataRespDTO item : dictList) {
            if (item == null) {
                continue;
            }
            String value = StrUtil.trimToNull(item.getValue());
            if (value == null) {
                continue;
            }
            String label = StrUtil.trimToNull(item.getLabel());
            map.putIfAbsent(value, label != null ? label : value);
        }
        return map;
    }

    private record HazardPointSnapshot(double longitude, double latitude, String name) {
    }
}
