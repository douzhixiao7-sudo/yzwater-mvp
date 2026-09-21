package com.sydigit.yzwater.module.dal.mysql.geoBase;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirBfDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 水库工程信息 Mapper
 */
@Mapper
public interface YzWaterReservoirBfMapper extends BaseMapperX<YzWaterReservoirBfDO> {

    /**
     * 根据关联设施几何计算中心点并回写水库经纬度。
     *
     * <p>仅处理存在 facility_id 且设施 geom 不为空的数据；无 geom 自动跳过。</p>
     */
    @Update({
            "with source_data as (",
            "  select",
            "    r.id as reservoir_id,",
            "    ST_Centroid(",
            "      case",
            "        when ST_IsValid(b.geom) then b.geom",
            "        else ST_MakeValid(b.geom)",
            "      end",
            "    ) as center_geom",
            "  from yz_water_reservoir_bf r",
            "  join yz_water_facility_base_bf b on b.id = r.facility_id",
            "  where r.facility_id is not null",
            "    and b.geom is not null",
            "    and not ST_IsEmpty(b.geom)",
            "    and COALESCE(r.deleted, 0) = 0",
            "    and COALESCE(b.deleted, 0) = 0",
            "), target as (",
            "  select",
            "    reservoir_id,",
            "    round(cast(ST_X(center_geom) as numeric), 6) as longitude,",
            "    round(cast(ST_Y(center_geom) as numeric), 6) as latitude",
            "  from source_data",
            "  where center_geom is not null",
            "    and not ST_IsEmpty(center_geom)",
            ")",
            "update yz_water_reservoir_bf r",
            "set longitude = t.longitude,",
            "    latitude = t.latitude,",
            "    update_time = now()",
            "from target t",
            "where r.id = t.reservoir_id"
    })
    int syncLongitudeLatitudeFromFacilityGeom();

    @Update({
            "<script>",
            "update yz_water_reservoir_bf",
            "set facility_id = #{facilityId},",
            "    reservoir_code = #{req.reservoirCode},",
            "    reservoir_name = #{req.reservoirName},",
            "    reservoir_scale = #{req.reservoirScale},",
            "    township = #{req.township, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    location = #{req.location},",
            "    management_unit = #{managementUnit, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    reservoir_photos = #{reservoirPhotos, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    longitude = #{req.longitude},",
            "    latitude = #{req.latitude},",
            "    supervising_department = #{req.supervisingDepartment},",
            "    reservoir_nature = #{req.reservoirNature},",
            "    irrigation_area = #{req.irrigationArea},",
            "    design_irrigation_area = #{req.designIrrigationArea},",
            "    actual_irrigation_area = #{req.actualIrrigationArea},",
            "    protection_area = #{req.protectionArea},",
            "    downstream_facilities = #{req.downstreamFacilities},",
            "    water_supply_target = #{req.waterSupplyTarget},",
            "    catchment_area = #{req.catchmentArea},",
            "    elevation_datum = #{req.elevationDatum},",
            "    seismic_intensity = #{req.seismicIntensity},",
            "    completion_date = #{req.completionDate},",
            "    reinforcement_start_date = #{req.reinforcementStartDate},",
            "    reinforcement_end_date = #{req.reinforcementEndDate},",
            "    reinforcement_date = #{req.reinforcementDate},",
            "    design_flood_standard = #{req.designFloodStandard},",
            "    verified_flood_standard = #{req.verifiedFloodStandard},",
            "    design_return_period = #{req.designReturnPeriod},",
            "    check_return_period = #{req.checkReturnPeriod},",
            "    total_capacity = #{req.totalCapacity},",
            "    active_capacity = #{req.activeCapacity},",
            "    flood_control_capacity = #{req.floodControlCapacity},",
            "    dead_capacity = #{req.deadCapacity},",
            "    verified_flood_level = #{req.verifiedFloodLevel},",
            "    design_flood_level = #{req.designFloodLevel},",
            "    normal_operating_level = #{req.normalOperatingLevel},",
            "    flood_limit_level = #{req.floodLimitLevel},",
            "    dead_level = #{req.deadLevel},",
            "    dam_crest_elevation = #{req.damCrestElevation},",
            "    dam_top_width = #{req.damTopWidth},",
            "    dam_top_height = #{req.damTopHeight},",
            "    max_dam_height = #{req.maxDamHeight},",
            "    dam_top_length = #{req.damTopLength},",
            "    wave_wall_crest_elevation = #{req.waveWallCrestElevation},",
            "    dam_road_surface_type = #{req.damRoadSurfaceType},",
            "    seepage_control_type = #{req.seepageControlType},",
            "    seepage_pile_range = #{req.seepagePileRange},",
            "    seepage_elev_range = #{req.seepageElevRange},",
            "    upstream_slope_type = #{req.upstreamSlopeType},",
            "    upstream_slope_elevation = #{req.upstreamSlopeElevation},",
            "    upstream_slope_ratio = #{req.upstreamSlopeRatio},",
            "    downstream_slope_ratio = #{req.downstreamSlopeRatio},",
            "    slope_protection_type = #{req.slopeProtectionType},",
            "    slope_protection_elev_range = #{req.slopeProtectionElevRange},",
            "    downstream_slope_elevation = #{req.downstreamSlopeElevation},",
            "    downstream_slope_width = #{req.downstreamSlopeWidth},",
            "    spillway_type = #{req.spillwayType},",
            "    spillway_control_type = #{req.spillwayControlType},",
            "    spillway_has_bridge = #{req.spillwayHasBridge},",
            "    spillway_crest_elevation = #{req.spillwayCrestElevation},",
            "    spillway_bottom_elevation = #{req.spillwayBottomElevation},",
            "    spillway_bottom_width = #{req.spillwayBottomWidth},",
            "    spillway_max_discharge = #{req.spillwayMaxDischarge},",
            "    flood_channel_name = #{req.floodChannelName},",
            "    flood_channel_safe_discharge = #{req.floodChannelSafeDischarge},",
            "    culvert_type = #{req.culvertType},",
            "    culvert_section_size = #{req.culvertSectionSize},",
            "    culvert_gate_type = #{req.culvertGateType},",
            "    culvert_design_discharge = #{req.culvertDesignDischarge},",
            "    culvert_exit_elevation = #{req.culvertExitElevation},",
            "    culvert_diameter = #{req.culvertDiameter},",
            "    culvert_height = #{req.culvertHeight},",
            "    annual_water_supply = #{req.annualWaterSupply},",
            "    fishery_area = #{req.fisheryArea},",
            "    is_water_source = #{req.waterSource},",
            "    responsibilities = #{req.responsibilities},",
            "    remarks = #{req.remarks},",
            "    jump_url = #{req.jumpUrl},",
            "    update_time = now()",
            "where id = #{id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int updateEditFieldsById(@Param("id") Long id,
                             @Param("facilityId") Long facilityId,
                             @Param("req") ReservoirSaveReqVO reqVO,
                             @Param("managementUnit") String[] managementUnit,
                             @Param("reservoirPhotos") String[] reservoirPhotos);

    /**
     * 导入专用更新：
     * 1) 模板内字段按参数赋值（参数为 null 会写入 NULL）；
     * 2) 模板未提供的业务字段统一置空；
     * 3) 不修改 id、facility_id、reservoir_name、reservoir_code 等标识字段。
     */
    @Update({
            "<script>",
            "update yz_water_reservoir_bf",
            "set reservoir_scale = #{reservoirScale},",
            "    township = #{township, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    management_unit = #{managementUnit, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler},",
            "    reservoir_nature = #{reservoirNature},",
            "    catchment_area = #{catchmentArea},",
            "    total_capacity = #{totalCapacity},",
            "    active_capacity = #{activeCapacity},",
            "    verified_flood_level = #{verifiedFloodLevel},",
            "    design_flood_level = #{designFloodLevel},",
            "    normal_operating_level = #{normalOperatingLevel},",
            "    flood_limit_level = #{floodLimitLevel},",
            "    operational_level = #{operationalLevel},",
            "    dam_crest_elevation = #{damCrestElevation},",
            "    dam_top_width = #{damTopWidth},",
            "    max_dam_height = #{maxDamHeight},",
            "    dam_top_length = #{damTopLength},",
            "    location = NULL,",
            "    reservoir_photos = NULL,",
            "    longitude = NULL,",
            "    latitude = NULL,",
            "    supervising_department = NULL,",
            "    irrigation_area = NULL,",
            "    design_irrigation_area = NULL,",
            "    actual_irrigation_area = NULL,",
            "    protection_area = NULL,",
            "    downstream_facilities = NULL,",
            "    water_supply_target = NULL,",
            "    elevation_datum = NULL,",
            "    seismic_intensity = NULL,",
            "    completion_date = NULL,",
            "    reinforcement_start_date = NULL,",
            "    reinforcement_end_date = NULL,",
            "    reinforcement_date = NULL,",
            "    design_flood_standard = NULL,",
            "    verified_flood_standard = NULL,",
            "    design_return_period = NULL,",
            "    check_return_period = NULL,",
            "    flood_control_capacity = NULL,",
            "    dead_capacity = NULL,",
            "    dead_level = NULL,",
            "    dam_top_height = NULL,",
            "    wave_wall_crest_elevation = NULL,",
            "    dam_road_surface_type = NULL,",
            "    seepage_control_type = NULL,",
            "    seepage_pile_range = NULL,",
            "    seepage_elev_range = NULL,",
            "    upstream_slope_type = NULL,",
            "    upstream_slope_elevation = NULL,",
            "    upstream_slope_ratio = NULL,",
            "    downstream_slope_ratio = NULL,",
            "    slope_protection_type = NULL,",
            "    slope_protection_elev_range = NULL,",
            "    downstream_slope_elevation = NULL,",
            "    downstream_slope_width = NULL,",
            "    spillway_type = NULL,",
            "    spillway_control_type = NULL,",
            "    spillway_has_bridge = NULL,",
            "    spillway_crest_elevation = NULL,",
            "    spillway_bottom_elevation = NULL,",
            "    spillway_bottom_width = NULL,",
            "    spillway_max_discharge = NULL,",
            "    flood_channel_name = NULL,",
            "    flood_channel_safe_discharge = NULL,",
            "    culvert_type = NULL,",
            "    culvert_section_size = NULL,",
            "    culvert_gate_type = NULL,",
            "    culvert_design_discharge = NULL,",
            "    culvert_exit_elevation = NULL,",
            "    culvert_diameter = NULL,",
            "    culvert_height = NULL,",
            "    annual_water_supply = NULL,",
            "    fishery_area = NULL,",
            "    is_water_source = NULL,",
            "    remarks = NULL,",
            "    responsibilities = NULL,",
            "    update_time = now()",
            "where id = #{id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int updateImportFieldsById(@Param("id") Long id,
                               @Param("reservoirScale") String reservoirScale,
                               @Param("township") String[] township,
                               @Param("managementUnit") String[] managementUnit,
                               @Param("reservoirNature") String reservoirNature,
                               @Param("catchmentArea") BigDecimal catchmentArea,
                               @Param("totalCapacity") BigDecimal totalCapacity,
                               @Param("activeCapacity") BigDecimal activeCapacity,
                               @Param("verifiedFloodLevel") BigDecimal verifiedFloodLevel,
                               @Param("designFloodLevel") BigDecimal designFloodLevel,
                               @Param("normalOperatingLevel") BigDecimal normalOperatingLevel,
                               @Param("floodLimitLevel") BigDecimal floodLimitLevel,
                               @Param("operationalLevel") BigDecimal operationalLevel,
                               @Param("damCrestElevation") String damCrestElevation,
                               @Param("damTopWidth") String damTopWidth,
                               @Param("maxDamHeight") BigDecimal maxDamHeight,
                               @Param("damTopLength") String damTopLength);

    default LambdaQueryWrapper<YzWaterReservoirBfDO> buildQueryWrapper(ReservoirPageReqVO reqVO) {
        LambdaQueryWrapper<YzWaterReservoirBfDO> wrapper = new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .like(StrUtil.isNotBlank(reqVO.getReservoirCode()),
                        YzWaterReservoirBfDO::getReservoirCode, reqVO.getReservoirCode())
                .like(StrUtil.isNotBlank(reqVO.getReservoirName()),
                        YzWaterReservoirBfDO::getReservoirName, reqVO.getReservoirName())
                .eq(StrUtil.isNotBlank(reqVO.getReservoirScale()),
                        YzWaterReservoirBfDO::getReservoirScale, reqVO.getReservoirScale())
                .orderByDesc(YzWaterReservoirBfDO::getCreateTime);

        if (StrUtil.isNotBlank(reqVO.getManagementUnit())) {
            // 管理单位为数组字段，这里将数组拼接成字符串后做匹配，便于按单个单位筛选
            wrapper.apply("array_to_string(management_unit, ',') like {0}", "%" + reqVO.getManagementUnit() + "%");
        }
        return wrapper;
    }

    /**
     * 按指定行政区划编码集合统计水库总览数据（包含子级匹配）。
     *
     * <p>说明：township 为 text[]，入参同样按 text[] 传入，使用数组重叠运算符（&&）匹配任意交集。</p>
     */
    @Select({
            "select",
            "  count(1) as total_count,",
            "  coalesce(sum(r.total_capacity), 0) as total_capacity,",
            "  coalesce(sum(r.active_capacity), 0) as total_active_capacity",
            "from yz_water_reservoir_bf r",
            "left join yz_water_facility_base_bf b on b.id = r.facility_id",
            "where r.facility_id is not null",
            "  and r.township is not null",
            "  and r.township && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(r.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)"
    })
    Map<String, Object> selectSummaryByTowns(@Param("areaCodes") String[] areaCodes);

    /**
     * 统计全量水库的总库容、总集水面积、总灌溉面积。
     */
    @Select({
            "select",
            "  coalesce(sum(total_capacity), 0) as total_capacity,",
            "  coalesce(sum(catchment_area), 0) as catchment_area,",
            "  coalesce(sum(irrigation_area), 0) as irrigation_area",
            "from yz_water_reservoir_bf",
            "where COALESCE(deleted, 0) = 0"
    })
    Map<String, Object> selectAllCapacityAreaSummary();

    /**
     * 按指定行政区划编码集合分组统计水库规模数量。
     */
    @Select({
            "select reservoir_scale as reservoir_scale, count(1) as cnt",
            "from yz_water_reservoir_bf r",
            "left join yz_water_facility_base_bf b on b.id = r.facility_id",
            "where r.facility_id is not null",
            "  and r.township is not null",
            "  and r.township && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(r.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            "group by reservoir_scale"
    })
    List<Map<String, Object>> selectReservoirScaleCountByTowns(@Param("areaCodes") String[] areaCodes);

    /**
     * 查询指定行政区划编码集合下的水库简表（不分页，用于首页展示）。
     *
     * <p>包含 GeoJSON 几何（从设施基础表读取），便于在地图上展示并绑定点击事件。</p>
     */
    @Select({
            "select",
            "  r.id as reservoir_id,",
            "  r.facility_id as facility_base_id,",
            "  r.reservoir_name as reservoir_name,",
            "  r.reservoir_scale as reservoir_scale,",
            "  r.total_capacity as total_capacity,",
            "  r.dam_top_length as dam_top_length,",
            "  r.longitude as longitude,",
            "  r.latitude as latitude,",
            "  st_asgeojson(b.geom) as geometry_geojson",
            "from yz_water_reservoir_bf r",
            "left join yz_water_facility_base_bf b on b.id = r.facility_id",
            "where r.facility_id is not null",
            "  and r.township is not null",
            "  and r.township && #{areaCodes, jdbcType=ARRAY, typeHandler=com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler}",
            "  and COALESCE(r.deleted, 0) = 0",
            "  and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            "order by r.reservoir_name asc, r.create_time desc"
    })
    List<Map<String, Object>> selectReservoirListByTowns(@Param("areaCodes") String[] areaCodes);

    /**
     * 按所在乡镇（township）分组统计水库数量。
     *
     * <p>township 为 text[]，存储 /system/area/tree 的 id（字符串形式）。</p>
     */
    @Select({
            "select t.area_code as area_code, count(1) as cnt",
            "from (",
            "  select unnest(township) as area_code",
            "  from yz_water_reservoir_bf r",
            "  left join yz_water_facility_base_bf b on b.id = r.facility_id",
            "  where r.facility_id is not null",
            "    and r.township is not null",
            "    and COALESCE(r.deleted, 0) = 0",
            "    and (b.id is null or COALESCE(b.deleted, 0) = 0)",
            ") t",
            "where t.area_code is not null and btrim(t.area_code) <> ''",
            "group by t.area_code"
    })
    List<Map<String, Object>> selectTownshipCountGroup();
}
