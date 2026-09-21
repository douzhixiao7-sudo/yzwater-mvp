package com.sydigit.yzwater.module.dal.dataobject.embankment;

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
 * 堤防信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "堤防信息实体")
@TableName(value = "yz_embankment", autoResultMap = true)
@TenantIgnore
public class YzEmbankmentDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联基础表 yz_water_facility_base.id")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "关联河道ID")
    @TableField("river_channel_id")
    private Long riverChannelId;

    @Schema(description = "关联河段ID")
    @TableField("river_section_id")
    private Long riverSectionId;

    @Schema(description = "堤防代码")
    @TableField("embankment_code")
    private String embankmentCode;

    @Schema(description = "堤防名称")
    @TableField("embankment_name")
    private String embankmentName;

    @Schema(description = "区划代码")
    @TableField(value = "division_code", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] divisionCode;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "河流岸别(字典: zd_hlab)")
    @TableField("river_bank_side")
    private String riverBankSide;

    @Schema(description = "堤防跨界情况")
    @TableField("cross_boundary_status")
    private String crossBoundaryStatus;

    @Schema(description = "堤防类型(字典: zd_dflx)")
    @TableField("embankment_type")
    private String embankmentType;

    @Schema(description = "堤防形式(字典: zd_dfxs)")
    @TableField("embankment_form")
    private String embankmentForm;

    @Schema(description = "堤防级别(字典: zd_dfjb)")
    @TableField("embankment_level")
    private String embankmentLevel;

    @Schema(description = "防洪标准")
    @TableField("flood_standard")
    private String floodStandard;

    @Schema(description = "设计重现期(年)")
    @TableField("design_return_period")
    private Integer designReturnPeriod;

    @Schema(description = "堤防长度(m)")
    @TableField("length_m")
    private BigDecimal lengthM;

    @Schema(description = "标准长度(m)")
    @TableField("standard_length_m")
    private BigDecimal standardLengthM;

    @Schema(description = "高程系统")
    @TableField("elevation_system")
    private String elevationSystem;

    @Schema(description = "设计高潮位(m)")
    @TableField("design_high_tide")
    private String designHighTide;

    @Schema(description = "堤防最大高度(m)")
    @TableField("max_height")
    private BigDecimal maxHeight;

    @Schema(description = "堤防最小高度(m)")
    @TableField("min_height")
    private BigDecimal minHeight;

    @Schema(description = "堤防最大宽度(m)")
    @TableField("max_width")
    private BigDecimal maxWidth;

    @Schema(description = "堤防最小宽度(m)")
    @TableField("min_width")
    private BigDecimal minWidth;

    @Schema(description = "堤顶高程(m)")
    @TableField("crest_elevation")
    private BigDecimal crestElevation;

    @Schema(description = "起点")
    @TableField("start_point")
    private String startPoint;

    @Schema(description = "终点")
    @TableField("end_point")
    private String endPoint;

    @Schema(description = "堤顶起点高程(m)")
    @TableField("crest_start_elevation")
    private BigDecimal crestStartElevation;

    @Schema(description = "堤顶终点高程(m)")
    @TableField("crest_end_elevation")
    private BigDecimal crestEndElevation;

    @Schema(description = "工程任务")
    @TableField("project_task")
    private String projectTask;

    @Schema(description = "终点所在位置")
    @TableField("end_location")
    private String endLocation;

    @Schema(description = "工程建设情况")
    @TableField("construction_status")
    private String constructionStatus;

    @Schema(description = "归口管理部门")
    @TableField("management_department")
    private String managementDepartment;

    @Schema(description = "堤防图片(text[]，可多张)")
    @TableField(value = "embankment_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] embankmentImages;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;
}

