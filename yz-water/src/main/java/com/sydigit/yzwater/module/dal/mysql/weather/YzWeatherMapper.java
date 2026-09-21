package com.sydigit.yzwater.module.dal.mysql.weather;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 天气缓存 Mapper
 */
@Mapper
public interface YzWeatherMapper extends BaseMapperX<YzWeatherDO> {
}
