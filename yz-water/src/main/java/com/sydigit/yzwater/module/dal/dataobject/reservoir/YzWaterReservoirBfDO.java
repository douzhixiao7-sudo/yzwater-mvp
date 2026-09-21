package com.sydigit.yzwater.module.dal.dataobject.reservoir;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 水库工程信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "水库工程信息实体")
@TableName(value = "yz_water_reservoir_bf", autoResultMap = true)
@TenantIgnore
public class YzWaterReservoirBfDO extends BaseDO {

    @Schema(description = "主键 ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联基础表 yz_water_facility_base.id")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "水库编码")
    @TableField("reservoir_code")
    private String reservoirCode;

    @Schema(description = "水库名称")
    @TableField("reservoir_name")
    private String reservoirName;

    @Schema(description = "规模（字典表：zd_skgm）")
    @TableField("reservoir_scale")
    private String reservoirScale;

    @Schema(description = "所在乡镇")
    @TableField(value = "township", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] township;

    @Schema(description = "所在地点（可到村/组/坐标等）")
    @TableField("location")
    private String location;

    @Schema(description = "管理单位（字典表：zd_gldw）")
    @TableField(value = "management_unit", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] managementUnit;

    @Schema(description = "水库照片")
    @TableField(value = "reservoir_photos", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] reservoirPhotos;


    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "主管部门")
    @TableField("supervising_department")
    private String supervisingDepartment;

    @Schema(description = "水库性质（字典表：zd_skxz）")
    @TableField("reservoir_nature")
    private String reservoirNature;

    @Schema(description = "灌溉面积（亩）")
    @TableField("irrigation_area")
    private BigDecimal irrigationArea;

    @Schema(description = "设计灌溉面积（亩）")
    @TableField("design_irrigation_area")
    private BigDecimal designIrrigationArea;

    @Schema(description = "实际灌溉面积（亩）")
    @TableField("actual_irrigation_area")
    private String actualIrrigationArea;

    @Schema(description = "保护面积（亩）")
    @TableField("protection_area")
    private BigDecimal protectionArea;

    @Schema(description = "下游主要设施")
    @TableField("downstream_facilities")
    private String downstreamFacilities;

    @Schema(description = "供水对象（城镇/农村/工业等）")
    @TableField("water_supply_target")
    private String waterSupplyTarget;

    @Schema(description = "集水面积（平方公里）")
    @TableField("catchment_area")
    private BigDecimal catchmentArea;

    @Schema(description = "高程基准面（黄海/国家85等）")
    @TableField("elevation_datum")
    private String elevationDatum;

    @Schema(description = "设计（复核）抗震烈度")
    @TableField("seismic_intensity")
    private String seismicIntensity;

    @Schema(description = "竣工日期")
    @TableField("completion_date")
    private LocalDate completionDate;

    @Schema(description = "除险加固开工年月")
    @TableField("reinforcement_start_date")
    private LocalDate reinforcementStartDate;

    @Schema(description = "除险加固竣工年月")
    @TableField("reinforcement_end_date")
    private LocalDate reinforcementEndDate;

    @Schema(description = "除险加固日期（兼容历史字段）")
    @TableField("reinforcement_date")
    private LocalDateTime reinforcementDate;

    @Schema(description = "设计洪水标准（文字/等级）")
    @TableField("design_flood_standard")
    private String designFloodStandard;

    @Schema(description = "校核洪水标准（文字/等级）")
    @TableField("verified_flood_standard")
    private String verifiedFloodStandard;

    @Schema(description = "重现期设计（年）")
    @TableField("design_return_period")
    private Integer designReturnPeriod;

    @Schema(description = "重现期校核（年）")
    @TableField("check_return_period")
    private Integer checkReturnPeriod;

    @Schema(description = "总库容（m³）")
    @TableField("total_capacity")
    private BigDecimal totalCapacity;

    @Schema(description = "兴利库容（m³）")
    @TableField("active_capacity")
    private BigDecimal activeCapacity;

    @Schema(description = "调洪库容（m³）")
    @TableField("flood_control_capacity")
    private BigDecimal floodControlCapacity;

    @Schema(description = "死库容（m³）")
    @TableField("dead_capacity")
    private BigDecimal deadCapacity;

    @Schema(description = "校核水位/校核洪水位（m）")
    @TableField("verified_flood_level")
    private BigDecimal verifiedFloodLevel;

    @Schema(description = "设计水位/设计洪水位（m）")
    @TableField("design_flood_level")
    private BigDecimal designFloodLevel;

    @Schema(description = "兴利水位（m）")
    @TableField("normal_operating_level")
    private BigDecimal normalOperatingLevel;

    @Schema(description = "汛限水位（m）")
    @TableField("flood_limit_level")
    private BigDecimal floodLimitLevel;

    @Schema(description = "兴利/汛限水位（原合并字段，兼容）")
    @TableField("operational_level")
    private BigDecimal operationalLevel;

    @Schema(description = "死水位（m）")
    @TableField("dead_level")
    private BigDecimal deadLevel;

    @Schema(description = "坝顶高程（m）")
    @TableField("dam_crest_elevation")
    private String damCrestElevation;

    @Schema(description = "坝顶宽度（m）")
    @TableField("dam_top_width")
    private String damTopWidth;

    @Schema(description = "坝顶高度/坝顶相对高（m）")
    @TableField("dam_top_height")
    private BigDecimal damTopHeight;

    @Schema(description = "最大坝高（m）")
    @TableField("max_dam_height")
    private BigDecimal maxDamHeight;

    @Schema(description = "坝顶长度（m）")
    @TableField("dam_top_length")
    private String damTopLength;

    @Schema(description = "挡浪墙顶高程（m）")
    @TableField("wave_wall_crest_elevation")
    private BigDecimal waveWallCrestElevation;

    @Schema(description = "坝顶路面结构型式")
    @TableField("dam_road_surface_type")
    private String damRoadSurfaceType;

    @Schema(description = "防渗处理结构型式")
    @TableField("seepage_control_type")
    private String seepageControlType;

    @Schema(description = "防渗处理起止桩号")
    @TableField("seepage_pile_range")
    private String seepagePileRange;

    @Schema(description = "防渗处理起止高程")
    @TableField("seepage_elev_range")
    private String seepageElevRange;

    @Schema(description = "迎水坡型式")
    @TableField("upstream_slope_type")
    private String upstreamSlopeType;

    @Schema(description = "迎水坡起止高程")
    @TableField("upstream_slope_elevation")
    private String upstreamSlopeElevation;

    @Schema(description = "迎水坡坡比")
    @TableField("upstream_slope_ratio")
    private String upstreamSlopeRatio;

    @Schema(description = "背水坡坡比")
    @TableField("downstream_slope_ratio")
    private String downstreamSlopeRatio;

    @Schema(description = "护坡结构型式")
    @TableField("slope_protection_type")
    private String slopeProtectionType;

    @Schema(description = "护坡起止高程")
    @TableField("slope_protection_elev_range")
    private String slopeProtectionElevRange;

    @Schema(description = "背水坡护坝地高程（m）")
    @TableField("downstream_slope_elevation")
    private BigDecimal downstreamSlopeElevation;

    @Schema(description = "背水坡护坝地宽（m）")
    @TableField("downstream_slope_width")
    private BigDecimal downstreamSlopeWidth;

    @Schema(description = "溢洪道型式")
    @TableField("spillway_type")
    private String spillwayType;

    @Schema(description = "溢洪道控制方式（有闸/开敞式）")
    @TableField("spillway_control_type")
    private String spillwayControlType;

    @Schema(description = "溢洪道有无交通桥")
    @TableField("spillway_has_bridge")
    private Boolean spillwayHasBridge;

    @Schema(description = "溢洪道堰顶高程（m）")
    @TableField("spillway_crest_elevation")
    private BigDecimal spillwayCrestElevation;

    @Schema(description = "溢洪道底高程（m）")
    @TableField("spillway_bottom_elevation")
    private BigDecimal spillwayBottomElevation;

    @Schema(description = "溢洪道底宽（孔*宽）（m）")
    @TableField("spillway_bottom_width")
    private String spillwayBottomWidth;

    @Schema(description = "溢洪道最大流量（m³/s）")
    @TableField("spillway_max_discharge")
    private BigDecimal spillwayMaxDischarge;

    @Schema(description = "排洪河道名称")
    @TableField("flood_channel_name")
    private String floodChannelName;

    @Schema(description = "排洪河道安全泄量（m³/s）")
    @TableField("flood_channel_safe_discharge")
    private String floodChannelSafeDischarge;

    @Schema(description = "灌溉/放水涵洞结构型式")
    @TableField("culvert_type")
    private String culvertType;

    @Schema(description = "灌溉涵洞断面尺寸（宽*高）（m）")
    @TableField("culvert_section_size")
    private String culvertSectionSize;

    @Schema(description = "灌溉涵洞闸门型式")
    @TableField("culvert_gate_type")
    private String culvertGateType;

    @Schema(description = "灌溉涵洞设计流量（m³/s）")
    @TableField("culvert_design_discharge")
    private BigDecimal culvertDesignDischarge;

    @Schema(description = "涵洞出口底高程（m）")
    @TableField("culvert_exit_elevation")
    private BigDecimal culvertExitElevation;

    @Schema(description = "涵洞直径（m）")
    @TableField("culvert_diameter")
    private BigDecimal culvertDiameter;

    @Schema(description = "涵洞高度（m）")
    @TableField("culvert_height")
    private BigDecimal culvertHeight;

    @Schema(description = "年供水量（万m³）")
    @TableField("annual_water_supply")
    private BigDecimal annualWaterSupply;

    @Schema(description = "宜鱼面积（亩）")
    @TableField("fishery_area")
    private BigDecimal fisheryArea;

    @Schema(description = "是否水源地（饮用水源地）")
    @TableField("is_water_source")
    private Boolean waterSource;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;

    @Schema(description = "跳转链接")
    @TableField("jump_url")
    private String jumpUrl;

    @Schema(description = "河长职责")
    @TableField("responsibilities")
    private String responsibilities;
}
