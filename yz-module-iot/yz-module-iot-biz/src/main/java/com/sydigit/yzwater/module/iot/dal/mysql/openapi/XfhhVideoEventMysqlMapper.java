package com.sydigit.yzwater.module.iot.dal.mysql.openapi;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.openapi.XfhhVideoEventMysqlDO;
import com.sydigit.yzwater.module.iot.framework.xfhh.core.annotation.XfhhMysqlDS;
import org.apache.ibatis.annotations.Mapper;

/**
 * 幸福河湖视频事件 Mapper。
 */
@Mapper
@XfhhMysqlDS
public interface XfhhVideoEventMysqlMapper extends BaseMapperX<XfhhVideoEventMysqlDO> {
}
