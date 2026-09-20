package com.sydigit.yzwater.module.dal.dataobject.weather;

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
import java.util.Map;

/**
 * 天气站点实时数据实体
 */
@Data
@Schema(description = "天气站点实时数据实体")
@TableName(value = "yz_weather_station_ss", autoResultMap = true)
@TenantIgnore
public class YzWeatherStationSsDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "站点编码")
    @TableField("station_code")
    private String stationCode;

    @Schema(description = "采集时间")
    @TableField("create_time")
    private String createTime;

    @Schema(description = "天气实时数据JSON")
    @TableField(value = "weather", jdbcType = JdbcType.OTHER, typeHandler = JsonbMapTypeHandler.class)
    private Map<String, Object> weather;
}
