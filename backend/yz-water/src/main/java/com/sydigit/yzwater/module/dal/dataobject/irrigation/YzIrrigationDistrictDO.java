package com.sydigit.yzwater.module.dal.dataobject.irrigation;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;

/**
 * 灌区信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "灌区信息实体")
@TableName(value = "yz_irrigation_district", autoResultMap = true)
@TenantIgnore
public class YzIrrigationDistrictDO extends BaseDO {

    @Schema(description = "主键 ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联基础表 yz_water_facility_base.id")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "灌区编码")
    @TableField("irrigation_district_code")
    private String irrigationDistrictCode;

    @Schema(description = "灌区名称")
    @TableField("irrigation_district_name")
    private String irrigationDistrictName;

    @Schema(description = "灌区图片（text[]）")
    @TableField(value = "irrigation_district_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] irrigationDistrictImages;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;

    @Schema(description = "所在流域(字典：zd_szly)")
    @TableField("basin_code")
    private String basinCode;

    @Schema(description = "行政区划（text[]）")
    @TableField(value = "division_code", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] divisionCode;

    @Schema(description = "设计灌溉面积，单位万亩")
    @TableField("design_irrigation_area")
    private BigDecimal designIrrigationArea;

    @Schema(description = "实际空间面积,单位k㎡")
    @TableField("actual_irrigable_area")
    private BigDecimal actualIrrigableArea;

    @Schema(description = "实际基本农田面积(k㎡)")
    @TableField("basic_farmland_area_km2")
    private BigDecimal basicFarmlandAreaKm2;

    @Schema(description = "是否生态红线(0 否 1 是)")
    @TableField("is_ecological_red_line")
    private Integer isEcologicalRedLine;

    @Schema(description = "是否开发边界(0 否 1 是)")
    @TableField("is_development_boundary")
    private Integer isDevelopmentBoundary;

    @Schema(description = "干渠长度(单位 m)")
    @TableField("main_canal_length_m")
    private BigDecimal mainCanalLengthM;

    @Schema(description = "负责人")
    @TableField("leader_name")
    private String leaderName;

    @Schema(description = "联系电话")
    @TableField("leader_phone")
    private String leaderPhone;

    @Schema(description = "管理单位（text[]，字典：zd_gldw）")
    @TableField(value = "management_unit", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] managementUnit;

    @Schema(description = "灌区类型（字典：zd_gqlx）")
    @TableField("irrigation_district_type")
    private String irrigationDistrictType;
}
