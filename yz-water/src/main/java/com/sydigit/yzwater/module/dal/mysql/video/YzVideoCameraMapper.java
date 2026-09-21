package com.sydigit.yzwater.module.dal.mysql.video;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoCameraDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频摄像头 Mapper
 */
@Mapper
public interface YzVideoCameraMapper extends BaseMapperX<YzVideoCameraDO> {
}

