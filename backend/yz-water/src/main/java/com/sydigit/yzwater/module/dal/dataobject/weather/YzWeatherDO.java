package com.sydigit.yzwater.module.dal.dataobject.weather;

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

import java.util.Map;

/**
 * 天气缓存实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "天气缓存实体")
@TableName(value = "yz_weather", autoResultMap = true)
@TenantIgnore
public class YzWeatherDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "同步时间")
    @TableField("syn_time")
    private String synTime;

    @Schema(description = "天气内容（天气、雨量、报警等）")
    @TableField(value = "weather_content", jdbcType = JdbcType.OTHER, typeHandler = JsonbMapTypeHandler.class)
    private Map<String, Object> weatherContent;
}
