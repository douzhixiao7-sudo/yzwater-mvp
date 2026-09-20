package com.sydigit.yzwater.module.dal.dataobject.pump;

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
import java.time.LocalDate;

/**
 * 泵站信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "泵站信息实体")
@TableName(value = "yz_pump_station", autoResultMap = true)
@TenantIgnore
public class YzPumpStationDO extends BaseDO {

    @Schema(description = "主键 ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联基础表 yz_water_facility_base.id")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "泵站代码")
    @TableField("pump_station_code")
    private String pumpStationCode;

    @Schema(description = "泵站名称")
    @TableField("pump_station_name")
    private String pumpStationName;

    @Schema(description = "区划代码")
    @TableField(value = "division_code", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] divisionCode;

    @Schema(description = "泵站经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "泵站纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "工程等别(字典：zd_gcdb)")
    @TableField("engineering_grade")
    private String engineeringGrade;

    @Schema(description = "闸站规模")
    @TableField("engineering_scale")
    private String engineeringScale;

    @Schema(description = "工程建设情况")
    @TableField("engineering_construction_status")
    private String engineeringConstructionStatus;

    @Schema(description = "工程任务")
    @TableField("engineering_task")
    private String engineeringTask;

    @Schema(description = "自排流量(m³/s)")
    @TableField("self_flow")
    private BigDecimal selfFlow;

    @Schema(description = "抽引流量(m³/s)")
    @TableField("installed_flow")
    private BigDecimal installedFlow;

    @Schema(description = "抽排流量(m³/s)")
    @TableField("pumping_flow")
    private BigDecimal pumpingFlow;

    @Schema(description = "装机流量(m³/s)")
    @TableField("capacity_flow")
    private BigDecimal capacityFlow;

    @Schema(description = "机组数量")
    @TableField("unit_count")
    private Integer unitCount;

    @Schema(description = "常水位(m)")
    @TableField("normal_water_level")
    private BigDecimal normalWaterLevel;

    @Schema(description = "防办预降水位(m)")
    @TableField("pre_drop_water_level")
    private BigDecimal preDropWaterLevel;

    @Schema(description = "最低运行水位(m)")
    @TableField("minimum_operating_water_level")
    private BigDecimal minimumOperatingWaterLevel;

    @Schema(description = "单机组功率(KW)")
    @TableField("single_unit_power")
    private BigDecimal singleUnitPower;

    @Schema(description = "装机功率(MW)")
    @TableField("installed_capacity")
    private BigDecimal installedCapacity;

    @Schema(description = "具体位置")
    @TableField("pump_station_position")
    private String pumpStationPosition;

    @Schema(description = "桩号")
    @TableField("pile_number")
    private String pileNumber;

    @Schema(description = "泵站规模")
    @TableField("pump_station_size")
    private String pumpStationSize;

    @Schema(description = "泵站类型（字典：zd_bzlx）")
    @TableField("pump_station_type")
    private String pumpStationType;

    @Schema(description = "归口管理部门（字典：zd_gldw）")
    @TableField(value = "management_department", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] managementDepartment;

    @Schema(description = "泵站图片")
    @TableField(value = "pump_station_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] pumpStationImages;

    @Schema(description = "泵站概览")
    @TableField("pump_station_overview")
    private String pumpStationOverview;

    @Schema(description = "建设时间")
    @TableField("construction_time")
    private LocalDate constructionTime;

    @Schema(description = "设计洪水控制标准")
    @TableField("flood_control_design_standard")
    private String FloodControlDesignStandard;

}
