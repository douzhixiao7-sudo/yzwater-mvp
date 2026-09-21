package com.sydigit.yzwater.module.dal.mysql.geoBase;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseBfDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 大屏-BF 水利对象基础信息 Mapper
 */
@Mapper
public interface YzWaterFacilityBaseBfMapper extends BaseMapperX<YzWaterFacilityBaseBfDO> {

    @Update({
            "<script>",
            "update yz_water_facility_base_bf",
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
            "update yz_water_facility_base_bf",
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

    @Update({
            "<script>",
            "update yz_water_facility_base_bf",
            "set geom = null,",
            "    geom_type = null,",
            "    srid = null,",
            "    update_time = now()",
            "where id = #{id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int clearGeomById(@Param("id") Long id);

    @Update({
            "<script>",
            "delete from yz_water_facility_base_bf",
            "where id = #{id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int deletePhysicalById(@Param("id") Long id);
}
