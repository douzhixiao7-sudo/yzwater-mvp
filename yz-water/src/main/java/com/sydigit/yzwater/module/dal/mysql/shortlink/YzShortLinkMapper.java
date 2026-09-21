package com.sydigit.yzwater.module.dal.mysql.shortlink;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.shortlink.YzShortLinkDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短链记录 Mapper
 */
@Mapper
public interface YzShortLinkMapper extends BaseMapperX<YzShortLinkDO> {
}

