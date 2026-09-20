package com.sydigit.yzwater.module.dal.dataobject.pond;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;

/**
 * 坑塘业务表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "坑塘业务实体")
@TableName(value = "yz_water_pond", autoResultMap = true)
@TenantIgnore
public class YzWaterPondDO extends BaseDO {

    @Schema(description = "主键 ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联基础表 yz_water_facility_base.id")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "资源编号 CHBH")
    @TableField("resource_code")
    private String resourceCode;

    @Schema(description = "资源名称 ZYMC")
    @TableField("resource_name")
    private String resourceName;

    @Schema(description = "坐落位置 ZLWZ")
    @TableField("location_desc")
    private String locationDesc;

    @Schema(description = "行政村名称 XZQMC")
    @TableField("village_name")
    private String villageName;

    @Schema(description = "行政区划代码 XZQDM，对应 system_area.id")
    @TableField("village_code")
    private String villageCode;

    @Schema(description = "权属单位 QSDWMC")
    @TableField("owner_unit")
    private String ownerUnit;

    @Schema(description = "权属人（预留）")
    @TableField("owner_person")
    private String ownerPerson;

    @Schema(description = "土地权属 QSXZ")
    @TableField("ownership_type")
    private String ownershipType;

    @Schema(description = "国土地类")
    @TableField("land_type")
    private String landType;

    @Schema(description = "面积平方米 Shape_Area")
    @TableField("area_sqm")
    private BigDecimal areaSqm;

    @Schema(description = "面积亩 SCMJ")
    @TableField("area_mu")
    private BigDecimal areaMu;

    @Schema(description = "占农经权面积")
    @TableField("occupy_farm_area")
    private BigDecimal occupyFarmArea;

    @Schema(description = "东至 SZD")
    @TableField("east_to")
    private String eastTo;

    @Schema(description = "南至 SZN")
    @TableField("south_to")
    private String southTo;

    @Schema(description = "西至 SZX")
    @TableField("west_to")
    private String westTo;

    @Schema(description = "北至 SZB")
    @TableField("north_to")
    private String northTo;

    @Schema(description = "使用状态 SYZT")
    @TableField("usage_status")
    private String usageStatus;

    @Schema(description = "资源性质 ZYXZ")
    @TableField("resource_nature")
    private String resourceNature;

    @Schema(description = "占用情况 ZYQK")
    @TableField("occupation_status")
    private String occupationStatus;

    @Schema(description = "调查员 DCYXM")
    @TableField("surveyor")
    private String surveyor;

    @Schema(description = "联系方式 LXFS")
    @TableField("surveyor_phone")
    private String surveyorPhone;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "资源类型（如坑塘水面）")
    @TableField("resource_type")
    private String resourceType;

    @Schema(description = "中心点经度 WGS84")
    @TableField("center_lon")
    private BigDecimal centerLon;

    @Schema(description = "中心点纬度 WGS84")
    @TableField("center_lat")
    private BigDecimal centerLat;

    @Schema(description = "几何 MultiPolygon，SRID=4326")
    @TableField(value = "geom", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Geometry geom;
}
