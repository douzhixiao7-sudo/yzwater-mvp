package com.sydigit.yzwater.module.iot.dal.mysql.shiftteam;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 排班关联校验 Mapper
 */
@Mapper
public interface IotShiftScheduleRefMapper {

    /**
     * 校验排班表是否存在
     */
    @Select("SELECT to_regclass('yz_shift_schedule') IS NOT NULL")
    Boolean existsShiftScheduleTable();

    /**
     * 统计班组被排班引用数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM yz_shift_schedule
            WHERE team_id = #{teamId}
              AND deleted = 0
            """)
    Long selectCountByTeamId(@Param("teamId") Long teamId);
}
