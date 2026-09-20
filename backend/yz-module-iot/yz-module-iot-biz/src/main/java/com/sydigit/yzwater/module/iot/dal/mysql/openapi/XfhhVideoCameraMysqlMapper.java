package com.sydigit.yzwater.module.iot.dal.mysql.openapi;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoCameraDO;
import com.sydigit.yzwater.module.iot.framework.xfhh.core.annotation.XfhhMysqlDS;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 幸福河湖视频点位 Mapper。
 */
@Mapper
@XfhhMysqlDS
public interface XfhhVideoCameraMysqlMapper extends BaseMapperX<YzVideoCameraDO> {

    @Delete("delete from yz_video_cameras")
    void deleteAll();

    @Update("update yz_video_cameras set online = #{online} where camera_index_code = #{cameraIndexCode}")
    void updateOnlineByIndexCode(@Param("cameraIndexCode") String cameraIndexCode,
                                 @Param("online") String online);

    @Update("update yz_video_cameras set online = '0'")
    void resetAllOffline();
}
