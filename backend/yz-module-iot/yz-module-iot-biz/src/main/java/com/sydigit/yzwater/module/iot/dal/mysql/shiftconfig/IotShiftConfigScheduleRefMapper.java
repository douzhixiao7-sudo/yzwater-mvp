package com.sydigit.yzwater.module.iot.dal.mysql.shiftconfig;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 排班引用校验 Mapper
 */
@Mapper
public interface IotShiftConfigScheduleRefMapper {

    /**
     * 校验排班表是否存在
     */
    @Select("SELECT to_regclass('yz_shift_schedule') IS NOT NULL")
    Boolean existsShiftScheduleTable();

    /**
     * 统计班次被排班引用数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM yz_shift_schedule
            WHERE shift_id = #{shiftId}
              AND deleted = 0
            """)
    Long selectCountByShiftId(@Param("shiftId") Long shiftId);
}
