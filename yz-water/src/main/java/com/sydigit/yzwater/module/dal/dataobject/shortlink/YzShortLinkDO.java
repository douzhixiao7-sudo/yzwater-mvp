package com.sydigit.yzwater.module.dal.dataobject.shortlink;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.JsonbMapTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 短链记录（code -> 业务场景/业务ID -> H5 跳转）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "短链记录")
@TableName(value = "yz_short_link", autoResultMap = true)
@TenantIgnore
public class YzShortLinkDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "短链码", example = "aZ9K2pQ1")
    @TableField("code")
    private String code;

    @Schema(description = "跳转场景", example = "PROBLEM_PUBLIC_DETAIL")
    @TableField("scene")
    private String scene;

    @Schema(description = "业务类型", example = "PROBLEM_FEEDBACK")
    @TableField("biz_type")
    private String bizType;

    @Schema(description = "业务主键ID", example = "10001")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "手机号（用于短链免登录）")
    @TableField("mobilephone")
    private String mobilephone;

    @Schema(description = "状态（0-禁用，1-启用）", example = "1")
    @TableField("status")
    private Integer status;

    @Schema(description = "过期时间（为空表示不过期）")
    @TableField("expires_time")
    private LocalDateTime expiresTime;

    @Schema(description = "点击次数")
    @TableField("click_count")
    private Long clickCount;

    @Schema(description = "最后一次点击时间")
    @TableField("last_click_time")
    private LocalDateTime lastClickTime;

    @Schema(description = "最后一次点击IP")
    @TableField("last_click_ip")
    private String lastClickIp;

    @Schema(description = "最后一次点击UA")
    @TableField("last_click_ua")
    private String lastClickUa;

    @Schema(description = "扩展字段（jsonb）")
    @TableField(value = "ext", jdbcType = JdbcType.OTHER, typeHandler = JsonbMapTypeHandler.class)
    private Map<String, Object> ext;

}
