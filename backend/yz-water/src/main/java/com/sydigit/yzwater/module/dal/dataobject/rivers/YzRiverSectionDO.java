package com.sydigit.yzwater.module.dal.dataobject.rivers;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;

/**
 * 河段信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "河段信息实体")
@TableName(value = "yz_river_section", autoResultMap = true)
@TenantIgnore
public class YzRiverSectionDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联基础表 sy_water_facility_base.id")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "关联河道ID")
    @TableField("river_channel_id")
    private Long riverChannelId;

    @Schema(description = "河段名称")
    @TableField("section_name")
    private String sectionName;

    @Schema(description = "起点")
    @TableField("start_point")
    private String startPoint;

    @Schema(description = "终点")
    @TableField("end_point")
    private String endPoint;

    @Schema(description = "起点经度")
    @TableField("start_longitude")
    private BigDecimal startLongitude;

    @Schema(description = "起点纬度")
    @TableField("start_latitude")
    private BigDecimal startLatitude;

    @Schema(description = "终点经度")
    @TableField("end_longitude")
    private BigDecimal endLongitude;

    @Schema(description = "终点纬度")
    @TableField("end_latitude")
    private BigDecimal endLatitude;

    @Schema(description = "河段照片")
    @TableField(value = "section_photos", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] sectionPhotos;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;
}
