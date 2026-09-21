package com.sydigit.yzwater.module.dal.mysql.weather;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 天气站点基础 Mapper
 */
@Mapper
public interface YzWeatherStationMapper extends BaseMapperX<YzWeatherStationDO> {

    @Select("select station_code from yz_weather_station order by station_code")
    List<String> selectStationCodeList();

}

