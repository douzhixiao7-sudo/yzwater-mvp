package com.sydigit.yzwater.module.iot.dal.dataobject.openapi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 幸福河湖视频事件回调表。
 */
@Data
@Schema(description = "幸福河湖视频事件回调表")
@TableName("yz_video_event")
public class XfhhVideoEventMysqlDO implements Serializable {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("event_rev_content")
    private String eventRevContent;
}
