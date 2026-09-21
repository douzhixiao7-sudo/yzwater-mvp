package com.sydigit.yzwater.module.dal.mysql.rivers;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 河道基础信息 Mapper
 */
@Mapper
public interface YzRiverChannelMapper extends BaseMapperX<YzRiverChannelDO> {

    @Update({
            "<script>",
            "update yz_river_channel",
            "set facility_id = #{entity.facilityId},",
            "    river_code = #{entity.riverCode},",
            "    river_name = #{entity.riverName},",
            "    length_km = #{entity.lengthKm},",
            "    catchment_km2 = #{entity.catchmentKm2},",
            "    average_slope = #{entity.averageSlope},",
            "    basin_type = #{entity.basinType},",
            "    ecology_type = #{entity.ecologyType},",
            "    transboundary_type = #{entity.transboundaryType},",
            "    flood_standard = #{entity.floodStandard},",
            "    embankment_level = #{entity.embankmentLevel},",
            "    embankment_length = #{entity.embankmentLength},",
            "    centroid_longitude = #{entity.centroidLongitude},",
            "    centroid_latitude = #{entity.centroidLatitude},",
            "    river_end_longitude = #{entity.riverEndLongitude},",
            "    river_end_latitude = #{entity.riverEndLatitude},",
            "    river_source_longitude = #{entity.riverSourceLongitude},",
            "    river_source_latitude = #{entity.riverSourceLatitude},",
            "    flow_areas = #{entity.flowAreas, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    historical_max_water_level = #{entity.historicalMaxWaterLevel},",
            "    max_water_level_date = #{entity.maxWaterLevelDate},",
            "    lowest_water_level_date = #{entity.lowestWaterLevelDate},",
            "    historical_min_water_level = #{entity.historicalMinWaterLevel},",
            "    average_annual_runoff = #{entity.averageAnnualRunoff},",
            "    source_mountain_range = #{entity.sourceMountainRange},",
            "    river_terminus = #{entity.riverTerminus},",
            "    river_level = #{entity.riverLevel},",
            "    is_provincial_backbone = #{entity.isProvincialBackbone},",
            "    river_entrance = #{entity.riverEntrance},",
            "    river_origin = #{entity.riverOrigin},",
            "    start_point = #{entity.startPoint},",
            "    end_point = #{entity.endPoint},",
            "    river_type = #{entity.riverType, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    river_photos = #{entity.riverPhotos, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    water_quality_status = #{entity.waterQualityStatus},",
            "    associated_facilities = #{entity.associatedFacilities},",
            "    river_section_count = #{entity.riverSectionCount},",
            "    town = #{entity.town, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    management_unit = #{entity.managementUnit},",
            "    responsibilities = #{entity.responsibilities},",
            "    remarks = #{entity.remarks},",
            "    update_time = now()",
            "where id = #{entity.id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int updateEditFieldsById(@Param("entity") YzRiverChannelDO entity);

    default LambdaQueryWrapper<YzRiverChannelDO> buildQueryWrapper(RiverChannelPageReqVO reqVO) {
        return new LambdaQueryWrapper<YzRiverChannelDO>()
                .like(reqVO.getRiverCode() != null && !reqVO.getRiverCode().isEmpty(),
                        YzRiverChannelDO::getRiverCode, reqVO.getRiverCode())
                .like(reqVO.getRiverName() != null && !reqVO.getRiverName().isEmpty(),
                        YzRiverChannelDO::getRiverName, reqVO.getRiverName())
                .in(reqVO.getRiverLevel() != null && !reqVO.getRiverLevel().isEmpty(),
                        YzRiverChannelDO::getRiverLevel, reqVO.getRiverLevel())
                .eq(reqVO.getEcologyType() != null && !reqVO.getEcologyType().isEmpty(),
                        YzRiverChannelDO::getEcologyType, reqVO.getEcologyType())
                .eq(reqVO.getIsProvincialBackbone() != null,
                        YzRiverChannelDO::getIsProvincialBackbone, reqVO.getIsProvincialBackbone())
                .orderByDesc(YzRiverChannelDO::getUpdateTime)
                .orderByDesc(YzRiverChannelDO::getId);
    }

    /**
     * 按乡镇（行政区划编码）分组统计河道数量。
     *
     * <p>河道表 town 字段为数组，存储 /system/area/tree 的 id（字符串形式）。</p>
     */
    @Select({
            "select t.area_code as area_code, count(1) as cnt",
            "from (",
            "  select unnest(town) as area_code",
            "  from yz_river_channel",
            "  where facility_id is not null",
            "    and town is not null",
            "    and COALESCE(deleted, 0) = 0",
            ") t",
            "group by t.area_code"
    })
    List<Map<String, Object>> selectTownCountGroup();

    /**
     * 查询某乡镇（行政区划编码）下的河道列表（用于首页地图/弹窗）。
     *
     * <p>说明：areaCode 为 /system/area/tree 的 id（字符串）。</p>
     */
    @Select({
            "select id, facility_id, river_code, river_name, centroid_longitude, centroid_latitude",
            "from yz_river_channel",
            "where town is not null",
            "  and facility_id is not null",
            "  and #{areaCode} = any(town)",
            "  and COALESCE(deleted, 0) = 0",
            "order by river_name asc, create_time desc"
    })
    List<YzRiverChannelDO> selectListByTown(@Param("areaCode") String areaCode);

    /**
     * 统计全量河道总览数据。
     */
    @Select({
            "select",
            "  count(1) as total_count,",
            "  coalesce(sum(catchment_km2), 0) as total_catchment_km2,",
            "  coalesce(sum(length_km), 0) as total_length_km",
            "from yz_river_channel",
            "where COALESCE(deleted, 0) = 0"
    })
    Map<String, Object> selectAllSummary();

    /**
     * 按河道级别分组统计数量。
     */
    @Select({
            "select river_level as river_level, count(1) as cnt",
            "from yz_river_channel",
            "where COALESCE(deleted, 0) = 0",
            "group by river_level"
    })
    List<Map<String, Object>> selectAllRiverLevelCount();

    /**
     * 查询全量河道简表（不分页，仅用于首页展示）。
     */
    @Select({
            "select id, facility_id, river_name, river_level, length_km, centroid_longitude, centroid_latitude",
            "from yz_river_channel",
            "where COALESCE(deleted, 0) = 0",
            "order by river_name asc, create_time desc"
    })
    List<YzRiverChannelDO> selectAllRiverList();

    /**
     * 按指定行政区划编码集合统计河道总览数据（包含子级匹配）。
     *
     * <p>说明：town 为 text[]，入参同样按 text[] 传入，使用数组重叠运算符（&&）匹配任意交集。</p>
     */
    @Select({
            "select",
            "  count(1) as total_count,",
            "  coalesce(sum(catchment_km2), 0) as total_catchment_km2,",
            "  coalesce(sum(length_km), 0) as total_length_km",
            "from yz_river_channel",
            "where town is not null",
            "  and facility_id is not null",
            "  and town && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(deleted, 0) = 0"
    })
    Map<String, Object> selectSummaryByTowns(@Param("areaCodes") String[] areaCodes);

    /**
     * 按指定行政区划编码集合分组统计河道级别数量。
     */
    @Select({
            "select river_level as river_level, count(1) as cnt",
            "from yz_river_channel",
            "where town is not null",
            "  and facility_id is not null",
            "  and town && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(deleted, 0) = 0",
            "group by river_level"
    })
    List<Map<String, Object>> selectRiverLevelCountByTowns(@Param("areaCodes") String[] areaCodes);

    /**
     * 查询指定行政区划编码集合下的河道简表（不分页，仅用于首页展示）。
     */
    @Select({
            "select id, facility_id, river_name, river_level, length_km, centroid_longitude, centroid_latitude",
            "from yz_river_channel",
            "where town is not null",
            "  and facility_id is not null",
            "  and town && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(deleted, 0) = 0",
            "order by river_name asc, create_time desc"
    })
    List<YzRiverChannelDO> selectRiverListByTowns(@Param("areaCodes") String[] areaCodes);
}
