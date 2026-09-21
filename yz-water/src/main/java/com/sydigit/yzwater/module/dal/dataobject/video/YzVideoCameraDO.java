package com.sydigit.yzwater.module.dal.dataobject.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频摄像头实体
 */
@Data
@Schema(description = "视频摄像头实体")
@TableName("yz_video_cameras")
@TenantIgnore
public class YzVideoCameraDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "监控点编码")
    @TableId(value = "camera_index_code", type = IdType.INPUT)
    private String cameraIndexCode;

    @Schema(description = "监控点名称")
    @TableField("camera_name")
    private String cameraName;

    @Schema(description = "所属监控区域编码")
    @TableField("region_index_code")
    private String regionIndexCode;

    @Schema(description = "通道编号")
    @TableField("channel_no")
    private Integer channelNo;

    @Schema(description = "经度")
    @TableField("lon")
    private String lon;

    @Schema(description = "纬度")
    @TableField("lat")
    private String lat;

    @Schema(description = "在线状态")
    @TableField("online")
    private String online;

    @Schema(description = "监控点类型")
    @TableField("camera_type")
    private String cameraType;

    @Schema(description = "监控点类型说明")
    @TableField("camera_type_name")
    private String cameraTypeName;
}

