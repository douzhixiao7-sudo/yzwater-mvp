package com.sydigit.yzwater.module.dal.dataobject.weather;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 天气站点实体
 */
@Data
@Schema(description = "天气站点实体")
@TableName("yz_weather_station")
@TenantIgnore
public class YzWeatherStationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "站点编码")
    @TableId(value = "station_code", type = IdType.INPUT)
    private String stationCode;

    @Schema(description = "站点名称")
    @TableField("station_name")
    private String stationName;

    @Schema(description = "纬度")
    @TableField("lat")
    private String lat;

    @Schema(description = "经度")
    @TableField("lon")
    private String lon;

    @Schema(description = "站点所属分组ID")
    @TableField("station_ss_id")
    private String stationSsId;
}
