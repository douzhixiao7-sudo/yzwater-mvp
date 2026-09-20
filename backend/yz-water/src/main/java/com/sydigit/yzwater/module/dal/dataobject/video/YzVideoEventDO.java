package com.sydigit.yzwater.module.dal.dataobject.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.JsonbMapTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 视频事件回调实体
 */
@Data
@Schema(description = "视频事件回调实体")
@TableName(value = "yz_video_event", autoResultMap = true)
@TenantIgnore
public class YzVideoEventDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "接收时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "回调原始 JSON")
    @TableField(value = "event_rev_content", jdbcType = JdbcType.OTHER, typeHandler = JsonbMapTypeHandler.class)
    private Map<String, Object> eventRevContent;
}

