package com.sydigit.yzwater.module.service.file;

import com.sydigit.yzwater.module.enums.GeometryTypeEnum;
import com.sydigit.yzwater.module.service.dto.GeometryFeatureDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xsd.Parser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.enums.ErrorCodeConstants.YZ_FILE_CONVERT_FAIL;
import static com.sydigit.yzwater.module.enums.ErrorCodeConstants.YZ_SHP_PARSE_ERROR;

/**
 * 空间文件读取器，负责将不同格式转换为统一的 GeometryFeatureDTO 列表
 *
 * @author Lijun
 */
@Component
public class GeometryFeatureFileReader {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    /**
     * 读取 Shapefile
     *
     * @param shpFile    shp 文件路径
     * @param targetSrid 目标 SRID
     * @param sourceFile 原始文件名
     * @param bizType    业务分类
     */
    public List<GeometryFeatureDTO> readShapefile(URL shpFile,
                                                  Integer targetSrid,
                                                  String sourceFile,
                                                  String bizType) throws Exception {
        FileDataStore store = FileDataStoreFinder.getDataStore(shpFile);
        if (store == null) {
            throw exception(YZ_SHP_PARSE_ERROR);
        }
        if (store instanceof ShapefileDataStore shapefileDataStore) {
            shapefileDataStore.setCharset(StandardCharsets.UTF_8);
        }
        CoordinateReferenceSystem sourceCrs = store.getSchema().getCoordinateReferenceSystem();
        int sridToUse = resolveTargetSrid(sourceCrs, targetSrid);
        CoordinateReferenceSystem targetCrs = sridToUse > 0
                ? org.geotools.referencing.CRS.decode("EPSG:" + sridToUse)
                : null;
        MathTransform transform = null;
        if (sourceCrs != null && targetCrs != null && !org.geotools.referencing.CRS.equalsIgnoreMetadata(sourceCrs, targetCrs)) {
            transform = org.geotools.referencing.CRS.findMathTransform(sourceCrs, targetCrs, true);
        }

        List<GeometryFeatureDTO> features = new ArrayList<>();
        try (SimpleFeatureIterator iterator = ((SimpleFeatureCollection) store.getFeatureSource().getFeatures()).features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geometry = extractGeometry(feature, transform, sridToUse);
                for (Geometry part : explodeGeometry(geometry)) {
                    GeometryFeatureDTO dto = new GeometryFeatureDTO();
                    dto.setFeatureName(resolveFeatureName(feature));
                    dto.setBizType(bizType);
                    dto.setProperties(extractProperties(feature));
                    dto.setGeometry(part);
                    dto.setSrid(sridToUse);
                    dto.setGeometryType(GeometryTypeEnum.fromGeometry(part));
                    dto.setSourceFile(sourceFile);
                    features.add(dto);
                }
            }
        } finally {
            store.dispose();
        }
        return features;
    }

    private int resolveTargetSrid(CoordinateReferenceSystem sourceCrs, Integer targetSrid) throws Exception {
        if (targetSrid != null && targetSrid > 0) {
            return targetSrid;
        }
        if (sourceCrs == null) {
            return 4326;
        }
        Integer epsg = org.geotools.referencing.CRS.lookupEpsgCode(sourceCrs, true);
        return epsg != null ? epsg : 4326;
    }

    /**
     * 读取 GeoJSON
     */
    public List<GeometryFeatureDTO> readGeoJson(InputStream inputStream,
                                                String sourceFile,
                                                String bizType) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        try (ByteArrayInputStream first = new ByteArrayInputStream(bytes)) {
            return readGeoJsonStrict(first, sourceFile, bizType);
        } catch (Exception ex) {
            // 属性字段不一致等异常时，使用宽松解析避免报错
            try (ByteArrayInputStream fallback = new ByteArrayInputStream(bytes)) {
                return readGeoJsonLenient(fallback, sourceFile, bizType);
            }
        }
    }

    /**
     * 严格 GeoJSON 解析（GeoTools 默认行为）
     */
    private List<GeometryFeatureDTO> readGeoJsonStrict(InputStream inputStream,
                                                       String sourceFile,
                                                       String bizType) throws IOException {
        FeatureJSON featureJSON = new FeatureJSON();
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureJSON.readFeatureCollection(inputStream);
        int srid = 4326;
        List<GeometryFeatureDTO> list = new ArrayList<>();
        try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                if (geometry == null) {
                    continue;
                }
                geometry.setSRID(srid);
                GeometryFeatureDTO dto = new GeometryFeatureDTO();
                dto.setFeatureName(resolveFeatureName(feature));
                dto.setBizType(bizType);
                dto.setProperties(extractProperties(feature));
                dto.setGeometry(geometry);
                dto.setSrid(srid);
                dto.setGeometryType(GeometryTypeEnum.fromGeometry(geometry));
                dto.setSourceFile(sourceFile);
                list.add(dto);
            }
        }
        return list;
    }

    /**
     * 宽松 GeoJSON 解析，直接下沉属性到 Map，避免 schema 不一致导致的异常
     */
    private List<GeometryFeatureDTO> readGeoJsonLenient(InputStream inputStream,
                                                        String sourceFile,
                                                        String bizType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(inputStream);
        if (root == null || !root.has("features") || !root.get("features").isArray()) {
            return new ArrayList<>();
        }
        int srid = 4326;
        GeometryJSON geometryJSON = new GeometryJSON();
        List<GeometryFeatureDTO> list = new ArrayList<>();
        for (JsonNode featureNode : root.get("features")) {
            JsonNode geometryNode = featureNode.get("geometry");
            if (geometryNode == null || geometryNode.isNull()) {
                continue;
            }
            Geometry geometry;
            try {
                geometry = geometryJSON.read(geometryNode.toString());
            } catch (Exception ex) {
                continue;
            }
            if (geometry == null) {
                continue;
            }
            geometry.setSRID(srid);

            JsonNode propertiesNode = featureNode.get("properties");
            Map<String, Object> props = new LinkedHashMap<>();
            if (propertiesNode != null && propertiesNode.isObject()) {
                propertiesNode.fields().forEachRemaining(entry -> {
                    String key = entry.getKey() == null ? "" : entry.getKey().toLowerCase(Locale.ROOT);
                    props.put(key, objectMapper.convertValue(entry.getValue(), Object.class));
                });
            }

            GeometryFeatureDTO dto = new GeometryFeatureDTO();
            dto.setFeatureName(resolveFeatureName(props, featureNode));
            dto.setBizType(bizType);
            dto.setProperties(props);
            dto.setGeometry(geometry);
            dto.setSrid(srid);
            dto.setGeometryType(GeometryTypeEnum.fromGeometry(geometry));
            dto.setSourceFile(sourceFile);
            list.add(dto);
        }
        return list;
    }

    /**
     * 读取 KML（按 WGS84 解析）
     */
    public List<GeometryFeatureDTO> readKml(InputStream inputStream,
                                            String sourceFile,
                                            String bizType) throws Exception {
        Parser parser = new Parser(new KMLConfiguration());
        Object parsed = parser.parse(inputStream);
        if (!(parsed instanceof SimpleFeature feature)) {
            throw exception(YZ_FILE_CONVERT_FAIL);
        }
        List<GeometryFeatureDTO> result = new ArrayList<>();
        collectKmlFeatures(feature, result, 4326, sourceFile, bizType);
        return result;
    }

    private void collectKmlFeatures(SimpleFeature feature,
                                    List<GeometryFeatureDTO> collector,
                                    int targetSrid,
                                    String sourceFile,
                                    String bizType) {
        if (feature == null) {
            return;
        }
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        if (geometry != null) {
            geometry.setSRID(targetSrid);
            GeometryFeatureDTO dto = new GeometryFeatureDTO();
            dto.setFeatureName(resolveFeatureName(feature));
            dto.setBizType(bizType);
            dto.setProperties(extractProperties(feature));
            dto.setGeometry(geometry);
            dto.setSrid(targetSrid);
            dto.setGeometryType(GeometryTypeEnum.fromGeometry(geometry));
            dto.setSourceFile(sourceFile);
            collector.add(dto);
        }
        for (Property property : feature.getProperties()) {
            Object value = property.getValue();
            if (value instanceof SimpleFeature childFeature) {
                collectKmlFeatures(childFeature, collector, targetSrid, sourceFile, bizType);
            } else if (value instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof SimpleFeature nested) {
                        collectKmlFeatures(nested, collector, targetSrid, sourceFile, bizType);
                    }
                }
            }
        }
    }

    private Geometry extractGeometry(SimpleFeature feature, MathTransform transform, int targetSrid)
            throws TransformException {
        Object defaultGeometry = feature.getDefaultGeometry();
        if (!(defaultGeometry instanceof Geometry geometry)) {
            throw exception(YZ_SHP_PARSE_ERROR);
        }
        Geometry cloned = (Geometry) geometry.copy();
        if (transform != null) {
            cloned = JTS.transform(cloned, transform);
        }
        cloned.setSRID(targetSrid);
        return cloned;
    }

    private List<Geometry> explodeGeometry(Geometry geometry) {
        List<Geometry> result = new ArrayList<>();
        if (geometry == null) {
            return result;
        }
        if (geometry instanceof GeometryCollection collection && !(geometry instanceof Polygon)) {
            for (int i = 0; i < collection.getNumGeometries(); i++) {
                result.addAll(explodeGeometry(collection.getGeometryN(i)));
            }
        } else {
            result.add(geometry);
        }
        return result;
    }

    private Map<String, Object> extractProperties(SimpleFeature feature) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (Property property : feature.getProperties()) {
            if (property == null || property.getValue() == null) {
                continue;
            }
            if (property.getType() instanceof GeometryDescriptor) {
                continue;
            }
            String key = property.getName().toString();
            properties.put(key == null ? "" : key.toLowerCase(Locale.ROOT), property.getValue());
        }
        return properties;
    }

    private String resolveFeatureName(Map<String, Object> properties, JsonNode featureNode) {
        List<String> candidates = List.of("name", "hdmc", "hhmc", "skmc", "mc", "o_name", "reservoirname");
        for (String key : candidates) {
            Object value = properties.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        JsonNode idNode = featureNode.get("id");
        return idNode != null ? idNode.asText() : "";
    }

    private String resolveFeatureName(SimpleFeature feature) {
        List<String> candidates = List.of("name", "NAME", "Name", "feature_name", "FEATURE_NAME");
        for (String candidate : candidates) {
            Property property = feature.getProperty(candidate);
            if (property != null && property.getValue() != null) {
                return property.getValue().toString();
            }
        }
        return feature.getID();
    }
}
