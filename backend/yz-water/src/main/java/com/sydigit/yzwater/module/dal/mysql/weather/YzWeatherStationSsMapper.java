package com.sydigit.yzwater.module.dal.mysql.weather;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationSsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 天气站点实时数据 Mapper
 */
@Mapper
public interface YzWeatherStationSsMapper extends BaseMapperX<YzWeatherStationSsDO> {

    /**
     * 按站点编码查询最新一条实时天气。
     * 使用 MyBatis-Plus 查询以复用 DO 的 autoResultMap/typeHandler，避免 JSON 字段映射丢失。
     */
    default YzWeatherStationSsDO selectLatestByStationCode(String stationCode) {
        return selectOne(new LambdaQueryWrapper<YzWeatherStationSsDO>()
                .eq(YzWeatherStationSsDO::getStationCode, stationCode)
                .orderByDesc(YzWeatherStationSsDO::getCreateTime)
                .orderByDesc(YzWeatherStationSsDO::getId)
                .last("limit 1"));
    }

}
