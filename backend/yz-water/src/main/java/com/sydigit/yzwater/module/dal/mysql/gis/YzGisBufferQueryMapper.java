package com.sydigit.yzwater.module.dal.mysql.gis;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.gis.YzGisBufferQueryDO;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisBufferAreaRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisBufferFacilityRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisBufferGeometryRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisFacilityBaseRangeRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisFacilityTypeCountRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisReservoirRangeRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisRiverChannelRangeRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisRiverSectionRangeRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface YzGisBufferQueryMapper extends BaseMapperX<YzGisBufferQueryDO> {

    @Select("""
            SELECT CASE WHEN COUNT(1) > 0 THEN TRUE ELSE FALSE END
            FROM system_area
            WHERE id = #{areaId}
              AND deleted = 0
              AND gemo IS NOT NULL
              AND ST_Contains(gemo, ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490))
            """)
    Boolean existsPointInArea(@Param("areaId") Long areaId,
                              @Param("longitude") BigDecimal longitude,
                              @Param("latitude") BigDecimal latitude);

    @Select("""
            SELECT
              ST_Buffer(ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography, #{radius})::geometry AS buffer_geom,
              ST_Area(ST_Buffer(ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography, #{radius})::geography) AS area_m2
            """)
    @Results(id = "gisBufferGeometryRow", value = {
            @Result(column = "buffer_geom", property = "bufferGeom", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class),
            @Result(column = "area_m2", property = "areaM2")
    })
    GisBufferGeometryRow selectBufferGeometry(@Param("longitude") BigDecimal longitude,
                                              @Param("latitude") BigDecimal latitude,
                                              @Param("radius") BigDecimal radius);

    @Select({
            "<script>",
            "WITH valid_base AS (",
            "  SELECT",
            "    id,",
            "    facility_type,",
            "    facility_name,",
            "    admin_region_code,",
            "    geom_type,",
            "    CASE",
            "      WHEN ST_IsValid(geom) THEN geom",
            "      ELSE ST_MakeValid(geom)",
            "    END AS valid_geom",
            "  FROM yz_water_facility_base",
            "  WHERE COALESCE(deleted, 0) = 0",
            "    AND geom IS NOT NULL",
            "    <if test='facilityTypes != null and facilityTypes.size() > 0'>",
            "      AND facility_type IN",
            "      <foreach collection='facilityTypes' item='type' open='(' separator=',' close=')'>",
            "        #{type}",
            "      </foreach>",
            "    </if>",
            ")",
            "SELECT",
            "  id AS facility_id,",
            "  facility_type,",
            "  facility_name,",
            "  admin_region_code,",
            "  geom_type,",
            "  ST_X(display_point) AS longitude,",
            "  ST_Y(display_point) AS latitude",
            "FROM (",
            "  SELECT",
            "    id,",
            "    facility_type,",
            "    facility_name,",
            "    admin_region_code,",
            "    geom_type,",
            "    ST_PointOnSurface(",
            "      ST_Intersection(valid_geom, #{bufferGeom, jdbcType=OTHER, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler})",
            "    ) AS display_point",
            "  FROM valid_base",
            "  WHERE valid_geom IS NOT NULL",
            "    AND ST_Intersects(valid_geom, #{bufferGeom, jdbcType=OTHER, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler})",
            ") t",
            "ORDER BY facility_name ASC, id ASC",
            "</script>"
    })
    @Results(id = "gisBufferFacilityRow", value = {
            @Result(column = "facility_id", property = "facilityId"),
            @Result(column = "facility_type", property = "facilityType"),
            @Result(column = "facility_name", property = "facilityName"),
            @Result(column = "admin_region_code", property = "adminRegionCode"),
            @Result(column = "geom_type", property = "geomType"),
            @Result(column = "longitude", property = "longitude"),
            @Result(column = "latitude", property = "latitude")
    })
    List<GisBufferFacilityRow> selectFacilitiesInBuffer(@Param("bufferGeom") Geometry bufferGeom,
                                                        @Param("facilityTypes") List<String> facilityTypes);

    @Select("""
            SELECT id, name
            FROM system_area
            WHERE deleted = 0
              AND gemo IS NOT NULL
              AND ST_Intersects(gemo, #{bufferGeom, jdbcType=OTHER, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler})
            ORDER BY sort ASC, id ASC
            """)
    List<GisBufferAreaRow> selectAreasInBuffer(@Param("bufferGeom") Geometry bufferGeom);

    @Select({
            "<script>",
            "WITH valid_base AS (",
            "  SELECT",
            "    id,",
            "    facility_type,",
            "    admin_region_code,",
            "    CASE",
            "      WHEN ST_IsValid(geom) THEN geom",
            "      ELSE ST_MakeValid(geom)",
            "    END AS valid_geom",
            "  FROM yz_water_facility_base",
            "  WHERE COALESCE(deleted, 0) = 0",
            "    AND geom IS NOT NULL",
            "    <if test='facilityTypes != null and facilityTypes.size() > 0'>",
            "      AND facility_type IN",
            "      <foreach collection='facilityTypes' item='type' open='(' separator=',' close=')'>",
            "        #{type}",
            "      </foreach>",
            "    </if>",
            ")",
            "SELECT facility_type AS facilityType, count(1) AS count",
            "FROM valid_base",
            "WHERE valid_geom IS NOT NULL",
            "  AND ST_DWithin(",
            "    valid_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "GROUP BY facility_type",
            "</script>"
    })
    List<GisFacilityTypeCountRow> selectFacilityTypeCountInRange(@Param("longitude") BigDecimal longitude,
                                                                  @Param("latitude") BigDecimal latitude,
                                                                  @Param("radius") BigDecimal radius,
                                                                  @Param("facilityTypes") List<String> facilityTypes);

    @Select({
            "<script>",
            "WITH valid_base AS (",
            "  SELECT",
            "    id,",
            "    facility_type,",
            "    admin_region_code,",
            "    CASE",
            "      WHEN ST_IsValid(geom) THEN geom",
            "      ELSE ST_MakeValid(geom)",
            "    END AS valid_geom",
            "  FROM yz_water_facility_base",
            "  WHERE COALESCE(deleted, 0) = 0",
            "    AND geom IS NOT NULL",
            "    <if test='facilityTypes != null and facilityTypes.size() > 0'>",
            "      AND facility_type IN",
            "      <foreach collection='facilityTypes' item='type' open='(' separator=',' close=')'>",
            "        #{type}",
            "      </foreach>",
            "    </if>",
            ")",
            "SELECT id AS facility_id, facility_type, admin_region_code",
            "FROM valid_base",
            "WHERE valid_geom IS NOT NULL",
            "  AND ST_DWithin(",
            "    valid_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "</script>"
    })
    @Results(id = "gisFacilityBaseRangeRow", value = {
            @Result(column = "facility_id", property = "facilityId"),
            @Result(column = "facility_type", property = "facilityType"),
            @Result(column = "admin_region_code", property = "adminRegionCode")
    })
    List<GisFacilityBaseRangeRow> selectFacilityBaseInRange(@Param("longitude") BigDecimal longitude,
                                                            @Param("latitude") BigDecimal latitude,
                                                            @Param("radius") BigDecimal radius,
                                                            @Param("facilityTypes") List<String> facilityTypes);

    @Select({
            "<script>",
            "WITH base AS (",
            "  SELECT",
            "    r.id,",
            "    r.river_name,",
            "    r.river_section_count,",
            "    r.centroid_longitude,",
            "    r.centroid_latitude,",
            "    CASE",
            "      WHEN b.geom IS NULL THEN NULL",
            "      WHEN ST_IsValid(b.geom) THEN b.geom",
            "      ELSE ST_MakeValid(b.geom)",
            "    END AS valid_geom",
            "  FROM yz_river_channel r",
            "  LEFT JOIN yz_water_facility_base b ON b.id = r.facility_id",
            "  WHERE COALESCE(r.deleted, 0) = 0",
            "    AND (b.id IS NULL OR COALESCE(b.deleted, 0) = 0)",
            ")",
            "SELECT",
            "  id,",
            "  river_name,",
            "  river_section_count,",
            "  upper(replace(ST_GeometryType(display_geom), 'ST_', '')) AS geom_type,",
            "  ST_AsEWKT(display_geom) AS geom_wkt,",
            "  ST_Distance(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography",
            "  ) AS distance_m",
            "FROM (",
            "  SELECT",
            "    id,",
            "    river_name,",
            "    river_section_count,",
            "    COALESCE(",
            "      valid_geom,",
            "      CASE",
            "        WHEN centroid_longitude IS NOT NULL AND centroid_latitude IS NOT NULL",
            "        THEN ST_SetSRID(ST_MakePoint(centroid_longitude, centroid_latitude), 4490)",
            "      END",
            "    ) AS display_geom",
            "  FROM base",
            ") t",
            "WHERE display_geom IS NOT NULL",
            "  AND ST_DWithin(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "ORDER BY distance_m ASC, river_name ASC, id ASC",
            "</script>"
    })
    @Results(id = "gisRiverChannelRangeRow", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "river_name", property = "riverName"),
            @Result(column = "river_section_count", property = "riverSectionCount"),
            @Result(column = "geom_type", property = "geomType"),
            @Result(column = "geom_wkt", property = "geomWkt"),
            @Result(column = "distance_m", property = "distanceM")
    })
    List<GisRiverChannelRangeRow> selectRiverChannelInRange(@Param("longitude") BigDecimal longitude,
                                                            @Param("latitude") BigDecimal latitude,
                                                            @Param("radius") BigDecimal radius);

    @Select({
            "<script>",
            "WITH base AS (",
            "  SELECT",
            "    r.id,",
            "    r.reservoir_name,",
            "    r.longitude,",
            "    r.latitude,",
            "    CASE",
            "      WHEN b.geom IS NULL THEN NULL",
            "      WHEN ST_IsValid(b.geom) THEN b.geom",
            "      ELSE ST_MakeValid(b.geom)",
            "    END AS valid_geom",
            "  FROM yz_water_reservoir r",
            "  LEFT JOIN yz_water_facility_base b ON b.id = r.facility_id",
            "  WHERE COALESCE(r.deleted, 0) = 0",
            "    AND (b.id IS NULL OR COALESCE(b.deleted, 0) = 0)",
            ")",
            "SELECT",
            "  id,",
            "  reservoir_name,",
            "  upper(replace(ST_GeometryType(display_geom), 'ST_', '')) AS geom_type,",
            "  ST_AsEWKT(display_geom) AS geom_wkt,",
            "  ST_Distance(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography",
            "  ) AS distance_m",
            "FROM (",
            "  SELECT",
            "    id,",
            "    reservoir_name,",
            "    COALESCE(",
            "      valid_geom,",
            "      CASE",
            "        WHEN longitude IS NOT NULL AND latitude IS NOT NULL",
            "        THEN ST_SetSRID(ST_MakePoint(longitude, latitude), 4490)",
            "      END",
            "    ) AS display_geom",
            "  FROM base",
            ") t",
            "WHERE display_geom IS NOT NULL",
            "  AND ST_DWithin(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "ORDER BY distance_m ASC, reservoir_name ASC, id ASC",
            "</script>"
    })
    @Results(id = "gisReservoirRangeRow", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "reservoir_name", property = "reservoirName"),
            @Result(column = "geom_type", property = "geomType"),
            @Result(column = "geom_wkt", property = "geomWkt"),
            @Result(column = "distance_m", property = "distanceM")
    })
    List<GisReservoirRangeRow> selectReservoirInRange(@Param("longitude") BigDecimal longitude,
                                                      @Param("latitude") BigDecimal latitude,
                                                      @Param("radius") BigDecimal radius);

    @Select({
            "<script>",
            "SELECT",
            "  id,",
            "  river_channel_id,",
            "  section_name,",
            "  river_name,",
            "  river_section_count,",
            "  'LINESTRING' AS geom_type,",
            "  ST_AsEWKT(line_geom) AS geom_wkt,",
            "  ST_Distance(",
            "    line_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography",
            "  ) AS distance_m",
            "FROM (",
            "  SELECT",
            "    s.id,",
            "    s.river_channel_id,",
            "    s.section_name,",
            "    c.river_name,",
            "    c.river_section_count,",
            "    ST_MakeLine(",
            "      ST_SetSRID(ST_MakePoint(s.start_longitude, s.start_latitude), 4490),",
            "      ST_SetSRID(ST_MakePoint(s.end_longitude, s.end_latitude), 4490)",
            "    ) AS line_geom",
            "  FROM yz_river_section s",
            "  LEFT JOIN yz_river_channel c ON c.id = s.river_channel_id",
            "  WHERE COALESCE(s.deleted, 0) = 0",
            "    AND (c.id IS NULL OR COALESCE(c.deleted, 0) = 0)",
            "    AND s.start_longitude IS NOT NULL",
            "    AND s.start_latitude IS NOT NULL",
            "    AND s.end_longitude IS NOT NULL",
            "    AND s.end_latitude IS NOT NULL",
            ") t",
            "WHERE ST_DWithin(",
            "  line_geom::geography,",
            "  ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "  #{radius}",
            ")",
            "ORDER BY distance_m ASC, section_name ASC, id ASC",
            "</script>"
    })
    @Results(id = "gisRiverSectionRangeRow", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "river_channel_id", property = "riverChannelId"),
            @Result(column = "section_name", property = "sectionName"),
            @Result(column = "river_name", property = "riverName"),
            @Result(column = "river_section_count", property = "riverSectionCount"),
            @Result(column = "geom_type", property = "geomType"),
            @Result(column = "geom_wkt", property = "geomWkt"),
            @Result(column = "distance_m", property = "distanceM")
    })
    List<GisRiverSectionRangeRow> selectRiverSectionInRange(@Param("longitude") BigDecimal longitude,
                                                            @Param("latitude") BigDecimal latitude,
                                                            @Param("radius") BigDecimal radius);

    // --- App-only：缓冲区查询数据源为 BF 备份表（与后台 GIS 口径隔离）---
    // 排除镇级(6j/xjhl)、村级(7j/xcjhd)河道；水库仅保留省/市/县级公示牌关联项

    @Select({
            "<script>",
            "WITH base AS (",
            "  SELECT",
            "    r.id,",
            "    r.river_name,",
            "    r.river_section_count,",
            "    r.centroid_longitude,",
            "    r.centroid_latitude,",
            "    CASE",
            "      WHEN b.geom IS NULL THEN NULL",
            "      WHEN ST_IsValid(b.geom) THEN b.geom",
            "      ELSE ST_MakeValid(b.geom)",
            "    END AS valid_geom",
            "  FROM yz_river_channel_bf r",
            "  LEFT JOIN yz_water_facility_base_bf b ON b.id = r.facility_id",
            "  WHERE COALESCE(r.deleted, 0) = 0",
            "    AND (b.id IS NULL OR COALESCE(b.deleted, 0) = 0)",
            "    AND COALESCE(LOWER(TRIM(r.river_level)), '') NOT IN ('6j', '7j', 'xjhl', 'xcjhd')",
            ")",
            "SELECT",
            "  id,",
            "  river_name,",
            "  river_section_count,",
            "  upper(replace(ST_GeometryType(display_geom), 'ST_', '')) AS geom_type,",
            "  ST_AsEWKT(display_geom) AS geom_wkt,",
            "  ST_Distance(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography",
            "  ) AS distance_m",
            "FROM (",
            "  SELECT",
            "    id,",
            "    river_name,",
            "    river_section_count,",
            "    COALESCE(",
            "      valid_geom,",
            "      CASE",
            "        WHEN centroid_longitude IS NOT NULL AND centroid_latitude IS NOT NULL",
            "        THEN ST_SetSRID(ST_MakePoint(centroid_longitude, centroid_latitude), 4490)",
            "      END",
            "    ) AS display_geom",
            "  FROM base",
            ") t",
            "WHERE display_geom IS NOT NULL",
            "  AND ST_DWithin(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "ORDER BY distance_m ASC, river_name ASC, id ASC",
            "</script>"
    })
    @Results(id = "gisRiverChannelBfRangeRowApp", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "river_name", property = "riverName"),
            @Result(column = "river_section_count", property = "riverSectionCount"),
            @Result(column = "geom_type", property = "geomType"),
            @Result(column = "geom_wkt", property = "geomWkt"),
            @Result(column = "distance_m", property = "distanceM")
    })
    List<GisRiverChannelRangeRow> selectRiverChannelBfInRange(@Param("longitude") BigDecimal longitude,
                                                              @Param("latitude") BigDecimal latitude,
                                                              @Param("radius") BigDecimal radius);

    @Select({
            "<script>",
            "SELECT",
            "  id,",
            "  river_channel_id,",
            "  section_name,",
            "  river_name,",
            "  river_section_count,",
            "  'LINESTRING' AS geom_type,",
            "  ST_AsEWKT(line_geom) AS geom_wkt,",
            "  ST_Distance(",
            "    line_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography",
            "  ) AS distance_m",
            "FROM (",
            "  SELECT",
            "    s.id,",
            "    s.river_channel_id,",
            "    s.section_name,",
            "    c.river_name,",
            "    c.river_section_count,",
            "    ST_MakeLine(",
            "      ST_SetSRID(ST_MakePoint(s.start_longitude, s.start_latitude), 4490),",
            "      ST_SetSRID(ST_MakePoint(s.end_longitude, s.end_latitude), 4490)",
            "    ) AS line_geom",
            "  FROM yz_river_section_bf s",
            "  LEFT JOIN yz_river_channel_bf c ON c.id = s.river_channel_id",
            "  WHERE COALESCE(s.deleted, 0) = 0",
            "    AND (c.id IS NULL OR COALESCE(c.deleted, 0) = 0)",
            "    AND (c.id IS NULL OR COALESCE(LOWER(TRIM(c.river_level)), '') NOT IN ('6j', '7j', 'xjhl', 'xcjhd'))",
            "    AND s.start_longitude IS NOT NULL",
            "    AND s.start_latitude IS NOT NULL",
            "    AND s.end_longitude IS NOT NULL",
            "    AND s.end_latitude IS NOT NULL",
            ") t",
            "WHERE ST_DWithin(",
            "  line_geom::geography,",
            "  ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "  #{radius}",
            ")",
            "ORDER BY distance_m ASC, section_name ASC, id ASC",
            "</script>"
    })
    @Results(id = "gisRiverSectionBfRangeRowApp", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "river_channel_id", property = "riverChannelId"),
            @Result(column = "section_name", property = "sectionName"),
            @Result(column = "river_name", property = "riverName"),
            @Result(column = "river_section_count", property = "riverSectionCount"),
            @Result(column = "geom_type", property = "geomType"),
            @Result(column = "geom_wkt", property = "geomWkt"),
            @Result(column = "distance_m", property = "distanceM")
    })
    List<GisRiverSectionRangeRow> selectRiverSectionBfInRange(@Param("longitude") BigDecimal longitude,
                                                              @Param("latitude") BigDecimal latitude,
                                                              @Param("radius") BigDecimal radius);

    @Select({
            "<script>",
            "WITH base AS (",
            "  SELECT",
            "    r.id,",
            "    r.reservoir_name,",
            "    r.longitude,",
            "    r.latitude,",
            "    CASE",
            "      WHEN b.geom IS NULL THEN NULL",
            "      WHEN ST_IsValid(b.geom) THEN b.geom",
            "      ELSE ST_MakeValid(b.geom)",
            "    END AS valid_geom",
            "  FROM yz_water_reservoir_bf r",
            "  LEFT JOIN yz_water_facility_base_bf b ON b.id = r.facility_id",
            "  WHERE COALESCE(r.deleted, 0) = 0",
            "    AND (b.id IS NULL OR COALESCE(b.deleted, 0) = 0)",
            "    AND EXISTS (",
            "      SELECT 1",
            "      FROM yz_signboard_bf sb",
            "      WHERE COALESCE(sb.deleted, 0) = 0",
            "        AND (",
            "          sb.water_reservoir_id = r.id",
            "          OR (",
            "            sb.reference_id = r.id",
            "            AND LOWER(TRIM(COALESCE(sb.reference_type, ''))) IN (",
            "              'reservoir', 'reservoir_bf', 'water_reservoir', 'waterreservoir'",
            "            )",
            "          )",
            "        )",
            "        AND COALESCE(LOWER(TRIM(sb.signboard_level)), '') NOT IN ('6j', '7j', 'xjhl', 'xcjhd')",
            "    )",
            ")",
            "SELECT",
            "  id,",
            "  reservoir_name,",
            "  upper(replace(ST_GeometryType(display_geom), 'ST_', '')) AS geom_type,",
            "  ST_AsEWKT(display_geom) AS geom_wkt,",
            "  ST_Distance(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography",
            "  ) AS distance_m",
            "FROM (",
            "  SELECT",
            "    id,",
            "    reservoir_name,",
            "    COALESCE(",
            "      valid_geom,",
            "      CASE",
            "        WHEN longitude IS NOT NULL AND latitude IS NOT NULL",
            "        THEN ST_SetSRID(ST_MakePoint(longitude, latitude), 4490)",
            "      END",
            "    ) AS display_geom",
            "  FROM base",
            ") t",
            "WHERE display_geom IS NOT NULL",
            "  AND ST_DWithin(",
            "    display_geom::geography,",
            "    ST_SetSRID(ST_MakePoint(#{longitude}, #{latitude}), 4490)::geography,",
            "    #{radius}",
            "  )",
            "ORDER BY distance_m ASC, reservoir_name ASC, id ASC",
            "</script>"
    })
    @Results(id = "gisReservoirBfRangeRowApp", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "reservoir_name", property = "reservoirName"),
            @Result(column = "geom_type", property = "geomType"),
            @Result(column = "geom_wkt", property = "geomWkt"),
            @Result(column = "distance_m", property = "distanceM")
    })
    List<GisReservoirRangeRow> selectReservoirBfInRange(@Param("longitude") BigDecimal longitude,
                                                        @Param("latitude") BigDecimal latitude,
                                                        @Param("radius") BigDecimal radius);
}
