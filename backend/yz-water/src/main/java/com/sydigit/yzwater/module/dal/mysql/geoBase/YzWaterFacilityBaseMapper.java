package com.sydigit.yzwater.module.dal.mysql.geoBase;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 水利对象基础信息 Mapper
 */
@Mapper
public interface YzWaterFacilityBaseMapper extends BaseMapperX<YzWaterFacilityBaseDO> {

    @Select({
            "<script>",
            "select (",
            "    (select count(1) from yz_river_channel where facility_id = #{facilityId} and COALESCE(deleted, 0) = 0)",
            "  + (select count(1) from yz_river_section where facility_id = #{facilityId} and COALESCE(deleted, 0) = 0)",
            ")",
            "</script>"
    })
    Long countOriginalRiverFacilityRefs(@Param("facilityId") Long facilityId);

    @Select({
            "<script>",
            "select count(1)",
            "from yz_water_reservoir",
            "where facility_id = #{facilityId}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    Long countOriginalReservoirFacilityRefs(@Param("facilityId") Long facilityId);

    @Update({
            "<script>",
            "update yz_water_facility_base",
            "set facility_name = #{facilityName},",
            "    facility_type = #{facilityType},",
            "    facility_code = #{facilityCode},",
            "    admin_region_code = #{adminRegionCode},",
            "    admin_region = #{adminRegion},",
            "    manage_unit = #{manageUnit},",
            "    update_time = now()",
            "where id = #{id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int updateReservoirSyncFieldsById(@Param("id") Long id,
                                      @Param("facilityName") String facilityName,
                                      @Param("facilityType") String facilityType,
                                      @Param("facilityCode") String facilityCode,
                                      @Param("adminRegionCode") String adminRegionCode,
                                      @Param("adminRegion") String adminRegion,
                                      @Param("manageUnit") String manageUnit);

    @Update({
            "<script>",
            "update yz_water_facility_base",
            "set facility_name = #{facilityName},",
            "    facility_type = #{facilityType},",
            "    facility_code = #{facilityCode},",
            "    manage_unit = #{manageUnit},",
            "    basin_code = #{basinCode},",
            "    update_time = now()",
            "where id = #{id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int updateRiverChannelSyncFieldsById(@Param("id") Long id,
                                         @Param("facilityName") String facilityName,
                                         @Param("facilityType") String facilityType,
                                         @Param("facilityCode") String facilityCode,
                                         @Param("manageUnit") String manageUnit,
                                         @Param("basinCode") String basinCode);

    default int updateGeomById(Long id, String geomType, org.locationtech.jts.geom.Geometry geom) {
        YzWaterFacilityBaseDO update = new YzWaterFacilityBaseDO();
        update.setId(id);
        update.setGeomType(geomType);
        update.setGeom(geom);
        return this.updateById(update);
    }

    @Update({
            "<script>",
            "update yz_water_facility_base",
            "set geom = null,",
            "    geom_type = null,",
            "    srid = null,",
            "    update_time = now()",
            "where id = #{id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int clearGeomById(@Param("id") Long id);
}
