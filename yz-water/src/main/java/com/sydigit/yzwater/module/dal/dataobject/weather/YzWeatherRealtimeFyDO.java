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
 * FY 实时天气图层实体
 */
@Data
@Schema(description = "FY实时天气图层实体")
@TableName("yz_weather_realtime_fy")
@TenantIgnore
public class YzWeatherRealtimeFyDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "图层类型")
    @TableField("type")
    private String type;

    @Schema(description = "图层图片地址")
    @TableField("weather_image")
    private String weatherImage;

    @Schema(description = "天气时间")
    @TableField("weather_time")
    private String weatherTime;

    @Schema(description = "文件名")
    @TableField("file_name")
    private String fileName;
}
