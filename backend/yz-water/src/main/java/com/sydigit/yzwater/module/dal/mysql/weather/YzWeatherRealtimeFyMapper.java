package com.sydigit.yzwater.module.dal.mysql.weather;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherRealtimeFyDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * FY 实时天气图层 Mapper
 */
@Mapper
public interface YzWeatherRealtimeFyMapper extends BaseMapperX<YzWeatherRealtimeFyDO> {
}

