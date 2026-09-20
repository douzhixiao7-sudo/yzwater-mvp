package com.sydigit.yzwater.module.dal.mysql.rivers;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionBfDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 河段表 Mapper
 */
@Mapper
public interface YzRiverSectionBfMapper extends BaseMapperX<YzRiverSectionBfDO> {

    @Update({
            "<script>",
            "update yz_river_section_bf",
            "set facility_id = #{entity.facilityId},",
            "    section_name = #{entity.sectionName},",
            "    start_point = #{entity.startPoint},",
            "    end_point = #{entity.endPoint},",
            "    start_longitude = #{entity.startLongitude},",
            "    start_latitude = #{entity.startLatitude},",
            "    end_longitude = #{entity.endLongitude},",
            "    end_latitude = #{entity.endLatitude},",
            "    remarks = #{entity.remarks},",
            "    update_time = now()",
            "where id = #{entity.id}",
            "  and COALESCE(deleted, 0) = 0",
            "</script>"
    })
    int updateEditFieldsById(@Param("entity") YzRiverSectionBfDO entity);
}
