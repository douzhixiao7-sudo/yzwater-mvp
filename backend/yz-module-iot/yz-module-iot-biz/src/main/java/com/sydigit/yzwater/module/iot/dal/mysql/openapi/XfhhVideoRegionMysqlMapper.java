package com.sydigit.yzwater.module.iot.dal.mysql.openapi;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoRegionDO;
import com.sydigit.yzwater.module.iot.framework.xfhh.core.annotation.XfhhMysqlDS;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 幸福河湖视频区域 Mapper。
 */
@Mapper
@XfhhMysqlDS
public interface XfhhVideoRegionMysqlMapper extends BaseMapperX<YzVideoRegionDO> {

    @Delete("delete from yz_video_regions")
    void deleteAll();
}
