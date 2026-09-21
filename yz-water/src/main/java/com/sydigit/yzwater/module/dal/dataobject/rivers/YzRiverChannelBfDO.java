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
import java.time.LocalDateTime;

/**
 * 河道基础信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "河道基础信息实体")
@TableName(value = "yz_river_channel_bf", autoResultMap = true)
@TenantIgnore
public class YzRiverChannelBfDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联基础表 sy_water_facility_base.id")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "河道编码")
    @TableField("river_code")
    private String riverCode;

    @Schema(description = "河道名称")
    @TableField("river_name")
    private String riverName;

    @Schema(description = "河道长度(km)")
    @TableField("length_km")
    private BigDecimal lengthKm;

    @Schema(description = "流域面积(平方公里)")
    @TableField("catchment_km2")
    private BigDecimal catchmentKm2;

    @Schema(description = "河道平均比降")
    @TableField("average_slope")
    private BigDecimal averageSlope;

    @Schema(description = "所在流域(字典: zd_szly)")
    @TableField("basin_type")
    private String basinType;

    @Schema(description = "生态类型(字典: zd_stlx)")
    @TableField("ecology_type")
    private String ecologyType;

    @Schema(description = "跨界类别(字典: zd_kjlb)")
    @TableField("transboundary_type")
    private String transboundaryType;

    @Schema(description = "防洪标准(字典: zd_fhbz)")
    @TableField("flood_standard")
    private String floodStandard;

    @Schema(description = "堤防等级(字典: zd_dfdj)")
    @TableField("embankment_level")
    private String embankmentLevel;

    @Schema(description = "堤防长度(km)")
    @TableField("embankment_length")
    private BigDecimal embankmentLength;

    @Schema(description = "中心点经度")
    @TableField("centroid_longitude")
    private BigDecimal centroidLongitude;

    @Schema(description = "中心点纬度")
    @TableField("centroid_latitude")
    private BigDecimal centroidLatitude;

    @Schema(description = "河口经度")
    @TableField("river_end_longitude")
    private BigDecimal riverEndLongitude;

    @Schema(description = "河口纬度")
    @TableField("river_end_latitude")
    private BigDecimal riverEndLatitude;

    @Schema(description = "河源经度")
    @TableField("river_source_longitude")
    private BigDecimal riverSourceLongitude;

    @Schema(description = "河源纬度")
    @TableField("river_source_latitude")
    private BigDecimal riverSourceLatitude;

    @Schema(description = "流经地区")
    @TableField(value = "flow_areas", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] flowAreas;

    @Schema(description = "历史最高水位(m)")
    @TableField("historical_max_water_level")
    private BigDecimal historicalMaxWaterLevel;

    @Schema(description = "最高水位时间")
    @TableField("max_water_level_date")
    private LocalDateTime maxWaterLevelDate;

    @Schema(description = "最低水位时间")
    @TableField("lowest_water_level_date")
    private LocalDateTime lowestWaterLevelDate;

    @Schema(description = "历史最低水位(m)")
    @TableField("historical_min_water_level")
    private BigDecimal historicalMinWaterLevel;

    @Schema(description = "年均径流量")
    @TableField("average_annual_runoff")
    private BigDecimal averageAnnualRunoff;

    @Schema(description = "发源山系")
    @TableField("source_mountain_range")
    private String sourceMountainRange;

    @Schema(description = "河流归宿")
    @TableField("river_terminus")
    private String riverTerminus;

    @Schema(description = "河流级别(字典: zd_hljb)")
    @TableField("river_level")
    private String riverLevel;

    @Schema(description = "是否省级骨干河道(0-否 1-是)")
    @TableField("is_provincial_backbone")
    private Integer isProvincialBackbone;

    @Schema(description = "河口位置")
    @TableField("river_entrance")
    private String riverEntrance;

    @Schema(description = "河源位置")
    @TableField("river_origin")
    private String riverOrigin;

    @Schema(description = "河道类型(多选字典: zd_hdlx)")
    @TableField(value = "river_type", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] riverType;

    @Schema(description = "起点")
    @TableField("start_point")
    private String startPoint;

    @Schema(description = "终点")
    @TableField("end_point")
    private String endPoint;

    @Schema(description = "河道照片")
    @TableField(value = "river_photos", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] riverPhotos;

    @Schema(description = "河道水质情况(字典: zd_hdszqk)")
    @TableField("water_quality_status")
    private String waterQualityStatus;

    @Schema(description = "关联设施")
    @TableField("associated_facilities")
    private String associatedFacilities;

    @Schema(description = "河段划分数量")
    @TableField("river_section_count")
    private Integer riverSectionCount;

    @Schema(description = "所属乡镇")
    @TableField(value = "town", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] town;

    @Schema(description = "管理单位")
    @TableField("management_unit")
    private String managementUnit;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;

    @Schema(description = "河长职责")
    @TableField("responsibilities")
    private String responsibilities;
}
