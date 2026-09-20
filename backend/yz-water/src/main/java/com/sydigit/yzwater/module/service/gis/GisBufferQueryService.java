package com.sydigit.yzwater.module.service.gis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryAreaRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryFacilityRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.gis.GisBufferQueryRespVO;
import com.sydigit.yzwater.module.dal.dataobject.gis.YzGisBufferQueryDO;
import com.sydigit.yzwater.module.dal.dataobject.gis.YzGisBufferQueryItemDO;
import com.sydigit.yzwater.module.dal.dataobject.embankment.YzEmbankmentDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.pump.YzPumpStationDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardDO;
import com.sydigit.yzwater.module.dal.mysql.gis.YzGisBufferQueryItemMapper;
import com.sydigit.yzwater.module.dal.mysql.gis.YzGisBufferQueryMapper;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisBufferAreaRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisBufferFacilityRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisBufferGeometryRow;
import com.sydigit.yzwater.module.dal.mysql.embankment.YzEmbankmentMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.irrigation.YzIrrigationDistrictMapper;
import com.sydigit.yzwater.module.dal.mysql.pump.YzPumpStationMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class GisBufferQueryService {

    private static final Long YIZHENG_AREA_ID = 321081L;
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzGisBufferQueryMapper bufferQueryMapper;
    private final YzGisBufferQueryItemMapper bufferQueryItemMapper;
    private final YzEmbankmentMapper embankmentMapper;
    private final YzPumpStationMapper pumpStationMapper;
    private final YzSignboardMapper signboardMapper;
    private final YzIrrigationDistrictMapper irrigationDistrictMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzRiverChannelMapper riverChannelMapper;
    private final YzFloodPreventionMaterialWarehouseMapper floodWarehouseMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4490);

    public GisBufferQueryService(YzGisBufferQueryMapper bufferQueryMapper,
                                 YzGisBufferQueryItemMapper bufferQueryItemMapper,
                                 YzEmbankmentMapper embankmentMapper,
                                 YzPumpStationMapper pumpStationMapper,
                                 YzSignboardMapper signboardMapper,
                                 YzIrrigationDistrictMapper irrigationDistrictMapper,
                                 YzWaterReservoirMapper waterReservoirMapper,
                                 YzRiverChannelMapper riverChannelMapper,
                                 YzFloodPreventionMaterialWarehouseMapper floodWarehouseMapper,
                                 YzWaterFacilityBaseMapper facilityBaseMapper) {
        this.bufferQueryMapper = bufferQueryMapper;
        this.bufferQueryItemMapper = bufferQueryItemMapper;
        this.embankmentMapper = embankmentMapper;
        this.pumpStationMapper = pumpStationMapper;
        this.signboardMapper = signboardMapper;
        this.irrigationDistrictMapper = irrigationDistrictMapper;
        this.waterReservoirMapper = waterReservoirMapper;
        this.riverChannelMapper = riverChannelMapper;
        this.floodWarehouseMapper = floodWarehouseMapper;
        this.facilityBaseMapper = facilityBaseMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public GisBufferQueryRespVO createBufferQuery(GisBufferQueryCreateReqVO reqVO) {
        BigDecimal longitude = reqVO.getCenterLongitude();
        BigDecimal latitude = reqVO.getCenterLatitude();
        BigDecimal radius = reqVO.getRadiusMeters();
        if (longitude == null || latitude == null || radius == null) {
            throw ServiceExceptionUtil.invalidParamException("中心点与半径不能为空");
        }
        if (radius.compareTo(BigDecimal.ZERO) <= 0) {
            throw ServiceExceptionUtil.invalidParamException("缓冲区半径必须大于0");
        }
        if (!Boolean.TRUE.equals(reqVO.getSelectAll()) && CollUtil.isEmpty(reqVO.getFacilityTypes())) {
            throw ServiceExceptionUtil.invalidParamException("请选择至少一个设施类型");
        }

        Boolean inArea = bufferQueryMapper.existsPointInArea(YIZHENG_AREA_ID, longitude, latitude);
        if (!Boolean.TRUE.equals(inArea)) {
            throw ServiceExceptionUtil.invalidParamException("选点必须位于仪征市范围内");
        }

        GisBufferGeometryRow geometryRow = bufferQueryMapper.selectBufferGeometry(longitude, latitude, radius);
        Geometry bufferGeom = geometryRow != null ? geometryRow.getBufferGeom() : null;
        if (bufferGeom == null || bufferGeom.isEmpty()) {
            throw ServiceExceptionUtil.invalidParamException("缓冲区生成失败");
        }

        List<String> facilityTypes = normalizeFacilityTypes(reqVO);
        List<GisBufferFacilityRow> facilities = bufferQueryMapper.selectFacilitiesInBuffer(
                bufferGeom,
                facilityTypes.isEmpty() ? null : facilityTypes
        );
        List<GisBufferAreaRow> areas = bufferQueryMapper.selectAreasInBuffer(bufferGeom);
        List<String> areaCodes = buildAreaCodes(areas);
        if (!areaCodes.isEmpty()) {
            List<GisBufferFacilityRow> areaFacilities = selectFacilitiesByAreaCodes(areaCodes, facilityTypes, bufferGeom);
            facilities = mergeFacilityRows(facilities, areaFacilities);
        }
        Map<String, Integer> typeCount = buildFacilityTypeCount(facilities);

        Long bufferId = SNOWFLAKE.nextId();
        LocalDateTime statsTime = LocalDateTime.now();
        YzGisBufferQueryDO bufferQuery = buildBufferQuery(bufferId, longitude, latitude, radius,
                bufferGeom, geometryRow.getAreaM2(), facilityTypes, facilities, areas, typeCount, statsTime);
        bufferQueryMapper.insert(bufferQuery);

        List<YzGisBufferQueryItemDO> items = buildBufferItems(bufferId, facilities);
        if (CollUtil.isNotEmpty(items)) {
            bufferQueryItemMapper.insertBatch(items);
        }

        GisBufferQueryRespVO respVO = buildBufferQueryResp(bufferQuery, facilities, areas);
        respVO.setBufferGeoJson(toGeoJson(bufferGeom));
        respVO.setCenterLongitude(longitude);
        respVO.setCenterLatitude(latitude);
        return respVO;
    }

    public PageResult<GisBufferQueryPageRespVO> getBufferQueryPage(GisBufferQueryPageReqVO reqVO) {
        PageResult<YzGisBufferQueryDO> pageResult = bufferQueryMapper.selectPage(reqVO,
                new LambdaQueryWrapper<YzGisBufferQueryDO>()
                        .orderByDesc(YzGisBufferQueryDO::getStatsTime)
                        .orderByDesc(YzGisBufferQueryDO::getId));
        List<GisBufferQueryPageRespVO> list = new ArrayList<>();
        for (YzGisBufferQueryDO item : pageResult.getList()) {
            GisBufferQueryPageRespVO respVO = new GisBufferQueryPageRespVO();
            respVO.setId(item.getId());
            respVO.setRadiusMeters(item.getRadiusM());
            respVO.setBufferAreaM2(item.getBufferAreaM2());
            respVO.setFacilityCount(item.getFacilityCount());
            respVO.setStatsTime(item.getStatsTime());
            respVO.setAdminAreaNames(arrayToList(item.getAdminAreaNames()));
            respVO.setFacilityTypes(arrayToList(item.getFacilityTypes()));
            list.add(respVO);
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    public GisBufferQueryRespVO getBufferQueryDetail(Long id) {
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("缓冲区ID不能为空");
        }
        YzGisBufferQueryDO bufferQuery = bufferQueryMapper.selectById(id);
        if (bufferQuery == null) {
            throw ServiceExceptionUtil.invalidParamException("缓冲区不存在或已删除");
        }
        List<YzGisBufferQueryItemDO> items = bufferQueryItemMapper.selectByBufferId(id);
        GisBufferQueryRespVO respVO = buildBufferQueryResp(bufferQuery, items);
        respVO.setBufferGeoJson(toGeoJson(bufferQuery.getBufferGeom()));
        Point center = bufferQuery.getCenterPoint();
        if (center != null) {
            respVO.setCenterLongitude(BigDecimal.valueOf(center.getX()));
            respVO.setCenterLatitude(BigDecimal.valueOf(center.getY()));
        }
        return respVO;
    }

    private YzGisBufferQueryDO buildBufferQuery(Long id,
                                                BigDecimal longitude,
                                                BigDecimal latitude,
                                                BigDecimal radius,
                                                Geometry bufferGeom,
                                                BigDecimal areaM2,
                                                List<String> facilityTypes,
                                                List<GisBufferFacilityRow> facilities,
                                                List<GisBufferAreaRow> areas,
                                                Map<String, Integer> typeCount,
                                                LocalDateTime statsTime) {
        YzGisBufferQueryDO bufferQuery = new YzGisBufferQueryDO();
        bufferQuery.setId(id);
        bufferQuery.setCenterPoint(buildCenterPoint(longitude, latitude));
        bufferQuery.setRadiusM(radius);
        bufferQuery.setBufferGeom(bufferGeom);
        bufferQuery.setBufferAreaM2(areaM2);
        bufferQuery.setFacilityTypes(facilityTypes.isEmpty() ? null : facilityTypes.toArray(new String[0]));
        bufferQuery.setFacilityCount(facilities != null ? facilities.size() : 0);
        bufferQuery.setFacilityTypeCnt(new LinkedHashMap<>(typeCount));
        bufferQuery.setStatsTime(statsTime);
        if (areas != null && !areas.isEmpty()) {
            Long[] ids = new Long[areas.size()];
            String[] names = new String[areas.size()];
            for (int i = 0; i < areas.size(); i++) {
                GisBufferAreaRow row = areas.get(i);
                ids[i] = row != null ? row.getId() : null;
                names[i] = row != null ? row.getName() : null;
            }
            bufferQuery.setAdminAreaIds(ids);
            bufferQuery.setAdminAreaNames(names);
        }
        return bufferQuery;
    }

    private List<YzGisBufferQueryItemDO> buildBufferItems(Long bufferId, List<GisBufferFacilityRow> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return List.of();
        }
        List<YzGisBufferQueryItemDO> items = new ArrayList<>(facilities.size());
        for (GisBufferFacilityRow row : facilities) {
            if (row == null || row.getFacilityId() == null) {
                continue;
            }
            YzGisBufferQueryItemDO item = new YzGisBufferQueryItemDO();
            item.setId(SNOWFLAKE.nextId());
            item.setBufferId(bufferId);
            item.setFacilityId(row.getFacilityId());
            item.setFacilityType(row.getFacilityType());
            item.setFacilityName(row.getFacilityName());
            item.setAdminRegionCode(row.getAdminRegionCode());
            item.setGeomType(row.getGeomType());
            item.setLongitude(row.getLongitude());
            item.setLatitude(row.getLatitude());
            items.add(item);
        }
        return items;
    }

    private GisBufferQueryRespVO buildBufferQueryResp(YzGisBufferQueryDO bufferQuery,
                                                     List<GisBufferFacilityRow> facilities,
                                                     List<GisBufferAreaRow> areas) {
        GisBufferQueryRespVO respVO = baseResp(bufferQuery);
        respVO.setFacilities(convertFacilityRows(facilities));
        respVO.setAdminAreas(convertAreas(areas, bufferQuery));
        respVO.setFacilityTypeCount(convertFacilityTypeCount(bufferQuery.getFacilityTypeCnt()));
        return respVO;
    }

    private GisBufferQueryRespVO buildBufferQueryResp(YzGisBufferQueryDO bufferQuery,
                                                     List<YzGisBufferQueryItemDO> items) {
        GisBufferQueryRespVO respVO = baseResp(bufferQuery);
        respVO.setFacilities(convertFacilityItems(items));
        respVO.setAdminAreas(convertAreas(null, bufferQuery));
        respVO.setFacilityTypeCount(convertFacilityTypeCount(bufferQuery.getFacilityTypeCnt()));
        return respVO;
    }

    private GisBufferQueryRespVO baseResp(YzGisBufferQueryDO bufferQuery) {
        GisBufferQueryRespVO respVO = new GisBufferQueryRespVO();
        respVO.setId(bufferQuery.getId());
        respVO.setRadiusMeters(bufferQuery.getRadiusM());
        respVO.setBufferAreaM2(bufferQuery.getBufferAreaM2());
        respVO.setFacilityTypes(arrayToList(bufferQuery.getFacilityTypes()));
        respVO.setFacilityCount(bufferQuery.getFacilityCount());
        respVO.setStatsTime(bufferQuery.getStatsTime());
        return respVO;
    }

    private List<GisBufferQueryFacilityRespVO> convertFacilityRows(List<GisBufferFacilityRow> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return List.of();
        }
        List<GisBufferQueryFacilityRespVO> list = new ArrayList<>(facilities.size());
        for (GisBufferFacilityRow row : facilities) {
            if (row == null) continue;
            GisBufferQueryFacilityRespVO respVO = new GisBufferQueryFacilityRespVO();
            respVO.setFacilityId(row.getFacilityId());
            respVO.setFacilityType(row.getFacilityType());
            respVO.setFacilityName(row.getFacilityName());
            respVO.setAdminRegionCode(row.getAdminRegionCode());
            respVO.setGeomType(row.getGeomType());
            respVO.setLongitude(row.getLongitude());
            respVO.setLatitude(row.getLatitude());
            list.add(respVO);
        }
        return list;
    }

    private List<GisBufferQueryFacilityRespVO> convertFacilityItems(List<YzGisBufferQueryItemDO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<GisBufferQueryFacilityRespVO> list = new ArrayList<>(items.size());
        for (YzGisBufferQueryItemDO row : items) {
            if (row == null) continue;
            GisBufferQueryFacilityRespVO respVO = new GisBufferQueryFacilityRespVO();
            respVO.setFacilityId(row.getFacilityId());
            respVO.setFacilityType(row.getFacilityType());
            respVO.setFacilityName(row.getFacilityName());
            respVO.setAdminRegionCode(row.getAdminRegionCode());
            respVO.setGeomType(row.getGeomType());
            respVO.setLongitude(row.getLongitude());
            respVO.setLatitude(row.getLatitude());
            list.add(respVO);
        }
        return list;
    }

    private List<GisBufferQueryAreaRespVO> convertAreas(List<GisBufferAreaRow> areas, YzGisBufferQueryDO bufferQuery) {
        if (areas != null && !areas.isEmpty()) {
            List<GisBufferQueryAreaRespVO> list = new ArrayList<>(areas.size());
            for (GisBufferAreaRow row : areas) {
                if (row == null) continue;
                GisBufferQueryAreaRespVO respVO = new GisBufferQueryAreaRespVO();
                respVO.setId(row.getId());
                respVO.setName(row.getName());
                list.add(respVO);
            }
            return list;
        }
        Long[] ids = bufferQuery.getAdminAreaIds();
        String[] names = bufferQuery.getAdminAreaNames();
        if (ids == null && names == null) {
            return List.of();
        }
        int len = Math.max(ids != null ? ids.length : 0, names != null ? names.length : 0);
        List<GisBufferQueryAreaRespVO> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            GisBufferQueryAreaRespVO respVO = new GisBufferQueryAreaRespVO();
            if (ids != null && i < ids.length) {
                respVO.setId(ids[i]);
            }
            if (names != null && i < names.length) {
                respVO.setName(names[i]);
            }
            if (respVO.getId() != null || StrUtil.isNotBlank(respVO.getName())) {
                list.add(respVO);
            }
        }
        return list;
    }

    private Map<String, Integer> buildFacilityTypeCount(List<GisBufferFacilityRow> facilities) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (facilities == null) return result;
        for (GisBufferFacilityRow row : facilities) {
            if (row == null) continue;
            String type = StrUtil.trimToNull(row.getFacilityType());
            if (type == null) continue;
            result.merge(type, 1, Integer::sum);
        }
        return result;
    }

    private Map<String, Integer> convertFacilityTypeCount(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        raw.forEach((key, value) -> {
            if (StrUtil.isBlank(key)) return;
            if (value instanceof Number num) {
                result.put(key, num.intValue());
                return;
            }
            String text = String.valueOf(value);
            if (StrUtil.isNotBlank(text) && StrUtil.isNumeric(text)) {
                result.put(key, Integer.parseInt(text));
            }
        });
        return result;
    }

    private List<String> normalizeFacilityTypes(GisBufferQueryCreateReqVO reqVO) {
        if (Boolean.TRUE.equals(reqVO.getSelectAll())) {
            return List.of();
        }
        List<String> types = reqVO.getFacilityTypes();
        if (types == null || types.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String type : types) {
            String val = StrUtil.trimToNull(type);
            if (val != null) {
                result.add(val);
            }
        }
        return result;
    }

    private Point buildCenterPoint(BigDecimal longitude, BigDecimal latitude) {
        Coordinate coordinate = new Coordinate(longitude.doubleValue(), latitude.doubleValue());
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4490);
        return point;
    }

    private String toGeoJson(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        GeometryJSON geometryJSON = new GeometryJSON();
        StringWriter writer = new StringWriter();
        try {
            geometryJSON.write(geometry, writer);
            return writer.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> buildAreaCodes(List<GisBufferAreaRow> areas) {
        if (areas == null || areas.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        for (GisBufferAreaRow row : areas) {
            if (row == null || row.getId() == null) {
                continue;
            }
            String code = String.valueOf(row.getId());
            if (StrUtil.isNotBlank(code)) {
                codes.add(code);
            }
        }
        return new ArrayList<>(codes);
    }

    private List<GisBufferFacilityRow> selectFacilitiesByAreaCodes(List<String> areaCodes,
                                                                   List<String> facilityTypes,
                                                                   Geometry bufferGeom) {
        if (CollUtil.isEmpty(areaCodes)) {
            return List.of();
        }
        String[] areaCodeArray = areaCodes.toArray(new String[0]);
        List<GisBufferFacilityRow> result = new ArrayList<>();

        // 按缓冲区覆盖的行政区划补齐设施，避免仅依赖空间相交漏数
        if (matchesFacilityType(facilityTypes, "dike", "embankment")) {
            List<YzEmbankmentDO> list = embankmentMapper.selectListByDivisionCodes(areaCodeArray);
            for (YzEmbankmentDO item : list) {
                GisBufferFacilityRow row = buildFacilityRow(item.getFacilityId(), "dike",
                        item.getEmbankmentName(), null, item.getLongitude(), item.getLatitude());
                if (row != null) {
                    result.add(row);
                }
            }
        }

        if (matchesFacilityType(facilityTypes, "pump_station", "pumpstation")) {
            List<YzPumpStationDO> list = pumpStationMapper.selectListByDivisionCodes(areaCodeArray);
            for (YzPumpStationDO item : list) {
                GisBufferFacilityRow row = buildFacilityRow(item.getFacilityId(), "pump_station",
                        item.getPumpStationName(), null, item.getLongitude(), item.getLatitude());
                if (row != null) {
                    result.add(row);
                }
            }
        }

        if (matchesFacilityType(facilityTypes, "signboard", "public_notice", "publicnotice")) {
            List<YzSignboardDO> list = signboardMapper.selectListByAreaCodes(areaCodeArray);
            for (YzSignboardDO item : list) {
                GisBufferFacilityRow row = buildFacilityRow(item.getId(), "signboard",
                        item.getSignboardName(), item.getAdminRegion(), item.getLongitude(), item.getLatitude());
                if (row != null) {
                    row.setGeomType("POINT");
                    result.add(row);
                }
            }
        }

        if (matchesFacilityType(facilityTypes, "irrigation", "irrigation_district", "irrigationdistrict")) {
            List<Map<String, Object>> list = irrigationDistrictMapper.selectIrrigationDistrictListByDivisionCodes(areaCodeArray);
            for (Map<String, Object> item : list) {
                Long facilityId = getLongFromMap(item, "facility_base_id");
                if (facilityId == null) {
                    facilityId = getLongFromMap(item, "irrigation_district_id");
                }
                GisBufferFacilityRow row = buildFacilityRow(facilityId, "irrigation",
                        getStringFromMap(item, "irrigation_district_name"), null, null, null);
                if (row != null) {
                    fillLocationByGeoJson(row, getStringFromMap(item, "geometry_geojson"));
                    result.add(row);
                }
            }
        }

        if (matchesFacilityType(facilityTypes, "reservoir", "water_reservoir", "waterreservoir")) {
            List<Map<String, Object>> list = waterReservoirMapper.selectReservoirListByTowns(areaCodeArray);
            for (Map<String, Object> item : list) {
                Long facilityId = getLongFromMap(item, "facility_base_id");
                if (facilityId == null) {
                    facilityId = getLongFromMap(item, "reservoir_id");
                }
                GisBufferFacilityRow row = buildFacilityRow(facilityId, "reservoir",
                        getStringFromMap(item, "reservoir_name"),
                        null,
                        getBigDecimalFromMap(item, "longitude"),
                        getBigDecimalFromMap(item, "latitude"));
                if (row != null) {
                    if (row.getLongitude() == null || row.getLatitude() == null) {
                        fillLocationByGeoJson(row, getStringFromMap(item, "geometry_geojson"));
                    }
                    result.add(row);
                }
            }
        }

        if (matchesFacilityType(facilityTypes, "river", "river_channel", "riverchannel")) {
            List<YzRiverChannelDO> list = riverChannelMapper.selectRiverListByTowns(areaCodeArray);
            for (YzRiverChannelDO item : list) {
                GisBufferFacilityRow row = buildFacilityRow(item.getFacilityId(), "river",
                        item.getRiverName(), null, item.getCentroidLongitude(), item.getCentroidLatitude());
                if (row != null) {
                    row.setGeomType("LINESTRING");
                    result.add(row);
                }
            }
        }

        if (matchesFacilityType(facilityTypes, "flood_prevention_material")) {
            List<YzFloodPreventionMaterialWarehouseDO> list = floodWarehouseMapper.selectWarehouseListByDivisionCodes(areaCodeArray);
            for (YzFloodPreventionMaterialWarehouseDO item : list) {
                GisBufferFacilityRow row = buildFacilityRow(item.getId(), "flood_prevention_material",
                        item.getWarehouseName(), null, item.getLongitude(), item.getLatitude());
                if (row != null) {
                    row.setGeomType("POINT");
                    result.add(row);
                }
            }
        }

        if (matchesFacilityType(facilityTypes, "customize")) {
            List<YzWaterFacilityBaseDO> list = facilityBaseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                    .select(YzWaterFacilityBaseDO::getId,
                            YzWaterFacilityBaseDO::getFacilityName,
                            YzWaterFacilityBaseDO::getAdminRegionCode,
                            YzWaterFacilityBaseDO::getGeomType,
                            YzWaterFacilityBaseDO::getGeom)
                    .eq(YzWaterFacilityBaseDO::getFacilityType, "customize")
                    .in(YzWaterFacilityBaseDO::getAdminRegionCode, areaCodes)
                    .orderByAsc(YzWaterFacilityBaseDO::getFacilityName));
            for (YzWaterFacilityBaseDO item : list) {
                if (item == null || item.getId() == null) {
                    continue;
                }
                GisBufferFacilityRow row = new GisBufferFacilityRow();
                row.setFacilityId(item.getId());
                row.setFacilityType("customize");
                row.setFacilityName(item.getFacilityName());
                row.setAdminRegionCode(item.getAdminRegionCode());
                row.setGeomType(item.getGeomType());
                fillLocationByGeometry(row, item.getGeom());
                result.add(row);
            }
        }

        fillFacilityBaseInfo(result, bufferGeom);
        return result;
    }

    private List<GisBufferFacilityRow> mergeFacilityRows(List<GisBufferFacilityRow> fromGeom,
                                                         List<GisBufferFacilityRow> fromArea) {
        if (CollUtil.isEmpty(fromGeom) && CollUtil.isEmpty(fromArea)) {
            return List.of();
        }
        LinkedHashMap<String, GisBufferFacilityRow> merged = new LinkedHashMap<>();
        if (fromGeom != null) {
            for (GisBufferFacilityRow row : fromGeom) {
                if (row == null || row.getFacilityId() == null) {
                    continue;
                }
                merged.put(buildFacilityKey(row), row);
            }
        }
        if (fromArea != null) {
            for (GisBufferFacilityRow row : fromArea) {
                if (row == null || row.getFacilityId() == null) {
                    continue;
                }
                String key = buildFacilityKey(row);
                GisBufferFacilityRow exists = merged.get(key);
                if (exists == null) {
                    merged.put(key, row);
                    continue;
                }
                mergeFacilityRow(exists, row);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private void mergeFacilityRow(GisBufferFacilityRow target, GisBufferFacilityRow source) {
        if (target == null || source == null) {
            return;
        }
        if (StrUtil.isBlank(target.getFacilityType())) {
            target.setFacilityType(source.getFacilityType());
        }
        if (StrUtil.isBlank(target.getFacilityName())) {
            target.setFacilityName(source.getFacilityName());
        }
        if (StrUtil.isBlank(target.getAdminRegionCode())) {
            target.setAdminRegionCode(source.getAdminRegionCode());
        }
        if (StrUtil.isBlank(target.getGeomType())) {
            target.setGeomType(source.getGeomType());
        }
        if (target.getLongitude() == null && source.getLongitude() != null) {
            target.setLongitude(source.getLongitude());
        }
        if (target.getLatitude() == null && source.getLatitude() != null) {
            target.setLatitude(source.getLatitude());
        }
    }

    private String buildFacilityKey(GisBufferFacilityRow row) {
        return StrUtil.blankToDefault(row.getFacilityType(), "") + ":" + row.getFacilityId();
    }

    private boolean matchesFacilityType(List<String> facilityTypes, String primaryType, String... aliases) {
        if (facilityTypes == null || facilityTypes.isEmpty()) {
            return true;
        }
        String primary = StrUtil.trimToEmpty(primaryType).toLowerCase();
        for (String type : facilityTypes) {
            if (StrUtil.isBlank(type)) {
                continue;
            }
            String val = type.trim().toLowerCase();
            if (val.equals(primary)) {
                return true;
            }
            if (aliases != null) {
                for (String alias : aliases) {
                    if (StrUtil.isBlank(alias)) {
                        continue;
                    }
                    if (val.equals(alias.trim().toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private GisBufferFacilityRow buildFacilityRow(Long facilityId,
                                                  String facilityType,
                                                  String facilityName,
                                                  String adminRegionCode,
                                                  BigDecimal longitude,
                                                  BigDecimal latitude) {
        if (facilityId == null) {
            return null;
        }
        GisBufferFacilityRow row = new GisBufferFacilityRow();
        row.setFacilityId(facilityId);
        row.setFacilityType(facilityType);
        row.setFacilityName(StrUtil.blankToDefault(facilityName, null));
        row.setAdminRegionCode(StrUtil.blankToDefault(adminRegionCode, null));
        row.setLongitude(longitude);
        row.setLatitude(latitude);
        return row;
    }

    private void fillFacilityBaseInfo(List<GisBufferFacilityRow> rows, Geometry bufferGeom) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (GisBufferFacilityRow row : rows) {
            if (row != null && row.getFacilityId() != null) {
                ids.add(row.getFacilityId());
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        List<YzWaterFacilityBaseDO> list = facilityBaseMapper.selectList(new LambdaQueryWrapper<YzWaterFacilityBaseDO>()
                .select(YzWaterFacilityBaseDO::getId,
                        YzWaterFacilityBaseDO::getFacilityName,
                        YzWaterFacilityBaseDO::getAdminRegionCode,
                        YzWaterFacilityBaseDO::getGeomType,
                        YzWaterFacilityBaseDO::getGeom,
                        YzWaterFacilityBaseDO::getFacilityType)
                .in(YzWaterFacilityBaseDO::getId, ids));
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, YzWaterFacilityBaseDO> baseMap = new LinkedHashMap<>(list.size());
        for (YzWaterFacilityBaseDO base : list) {
            if (base != null && base.getId() != null) {
                baseMap.put(base.getId(), base);
            }
        }
        rows.removeIf(row -> row == null || row.getFacilityId() == null);
        for (GisBufferFacilityRow row : rows) {
            YzWaterFacilityBaseDO base = baseMap.get(row.getFacilityId());
            if (base != null) {
                if (StrUtil.isBlank(row.getFacilityName())) {
                    row.setFacilityName(base.getFacilityName());
                }
                if (StrUtil.isBlank(row.getFacilityType())) {
                    row.setFacilityType(base.getFacilityType());
                }
                if (StrUtil.isBlank(row.getAdminRegionCode())) {
                    row.setAdminRegionCode(base.getAdminRegionCode());
                }
                if (StrUtil.isBlank(row.getGeomType())) {
                    row.setGeomType(base.getGeomType());
                }
            }
            if (bufferGeom == null || bufferGeom.isEmpty()) {
                if (base != null && (row.getLongitude() == null || row.getLatitude() == null)) {
                    fillLocationByGeometry(row, base.getGeom());
                }
                continue;
            }
            Geometry facilityGeom = base != null ? base.getGeom() : null;
            if (!ensureFacilityInBuffer(row, facilityGeom, bufferGeom)) {
                row.setFacilityId(null);
            }
        }
        rows.removeIf(row -> row == null || row.getFacilityId() == null);
    }

    private boolean ensureFacilityInBuffer(GisBufferFacilityRow row, Geometry facilityGeom, Geometry bufferGeom) {
        if (row == null) {
            return false;
        }
        if (bufferGeom == null || bufferGeom.isEmpty()) {
            return true;
        }
        if (facilityGeom != null && !facilityGeom.isEmpty()) {
            try {
                Geometry intersection = facilityGeom.intersection(bufferGeom);
                if (intersection == null || intersection.isEmpty()) {
                    return false;
                }
                Point point = intersection.getInteriorPoint();
                if (point != null && !point.isEmpty()) {
                    row.setLongitude(BigDecimal.valueOf(point.getX()));
                    row.setLatitude(BigDecimal.valueOf(point.getY()));
                }
                return true;
            } catch (Exception ignored) {
                // 几何计算失败时，降级为点位判断
            }
        }
        if (row.getLongitude() == null || row.getLatitude() == null) {
            return false;
        }
        Point point = geometryFactory.createPoint(new Coordinate(row.getLongitude().doubleValue(), row.getLatitude().doubleValue()));
        point.setSRID(4490);
        try {
            return bufferGeom.covers(point);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void fillLocationByGeoJson(GisBufferFacilityRow row, String geoJson) {
        if (row == null || StrUtil.isBlank(geoJson)) {
            return;
        }
        GeometryJSON geometryJSON = new GeometryJSON();
        try {
            Geometry geometry = geometryJSON.read(geoJson);
            fillLocationByGeometry(row, geometry);
        } catch (Exception ignored) {
            // GeoJSON 解析失败时，不影响列表返回
        }
    }

    private void fillLocationByGeometry(GisBufferFacilityRow row, Geometry geometry) {
        if (row == null || geometry == null || geometry.isEmpty()) {
            return;
        }
        try {
            Point centroid = geometry.getCentroid();
            if (centroid != null) {
                row.setLongitude(BigDecimal.valueOf(centroid.getX()));
                row.setLatitude(BigDecimal.valueOf(centroid.getY()));
            }
        } catch (Exception ignored) {
            // 质心计算失败时，不影响列表返回
        }
    }

    private String getStringFromMap(Map<String, Object> map, String key) {
        if (map == null || StrUtil.isBlank(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return StrUtil.blankToDefault(text, null);
    }

    private Long getLongFromMap(Map<String, Object> map, String key) {
        if (map == null || StrUtil.isBlank(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number num) {
            return num.longValue();
        }
        try {
            String text = String.valueOf(value);
            return StrUtil.isNotBlank(text) ? Long.parseLong(text) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        if (map == null || StrUtil.isBlank(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        try {
            String text = String.valueOf(value);
            return StrUtil.isNotBlank(text) ? new BigDecimal(text) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> arrayToList(String[] items) {
        if (items == null || items.length == 0) {
            return List.of();
        }
        List<String> list = new ArrayList<>(items.length);
        for (String item : items) {
            if (StrUtil.isBlank(item)) continue;
            list.add(item);
        }
        return list;
    }
}
