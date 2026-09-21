package com.sydigit.yzwater.module.iot.dal.mysql.statistics;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 天气站点查询 Mapper
 */
@Mapper
public interface IotWeatherStationMapper {

    @Select({
            "select",
            "  station_name,",
            "  lat,",
            "  lon,",
            "  station_ss_id",
            "from yz_weather_station"
    })
    List<Map<String, Object>> selectSimpleList();
}
