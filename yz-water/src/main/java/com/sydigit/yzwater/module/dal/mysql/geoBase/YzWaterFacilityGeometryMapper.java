package com.sydigit.yzwater.module.dal.mysql.geoBase;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityGeometryDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 水利对象空间信息 Mapper
 */
@Mapper
public interface YzWaterFacilityGeometryMapper extends BaseMapperX<YzWaterFacilityGeometryDO> {
}
