package com.sydigit.yzwater.module.dal.dataobject.geoBase;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.JsonbMapTypeHandler;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Geometry;

import java.util.Map;

/**
 * 水利对象基础信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "水利对象基础信息实体")
@TableName(value = "yz_water_facility_base", autoResultMap = true)
@TenantIgnore
public class YzWaterFacilityBaseDO extends BaseDO {

    @Schema(description = "主键 ID")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField("id")
    private Long id;

    @Schema(description = "设施唯一编码")
    @TableField("facility_code")
    private String facilityCode;

    @Schema(description = "设施名称")
    @TableField("facility_name")
    private String facilityName;

    @Schema(description = "设施类别（字典表：zd_sslb）")
    @TableField("facility_type")
    private String facilityType;

    @Schema(description = "行政区划名称")
    @TableField("admin_region")
    private String adminRegion;

    @Schema(description = "行政区划编码")
    @TableField("admin_region_code")
    private String adminRegionCode;

    @Schema(description = "管理单位")
    @TableField("manage_unit")
    private String manageUnit;

    @Schema(description = "扩展属性（JSON 格式）")
    @TableField(value = "attributes", jdbcType = JdbcType.OTHER, typeHandler = JsonbMapTypeHandler.class)
    private Map<String, Object> attributes;

    @Schema(description = "流域编码")
    @TableField("basin_code")
    private String basinCode;

    @Schema(description = "运行状态")
    @TableField("status")
    private String status;

    @Schema(description = "安全等级")
    @TableField("safety_level")
    private String safetyLevel;

    @Schema(description = "设计标准")
    @TableField("design_standard")
    private String designStandard;

    @Schema(description = "几何类型（POINT、LINESTRING、POLYGON 等）")
    @TableField("geom_type")
    private String geomType;

    @Schema(description = "坐标系编号")
    @TableField("srid")
    private Integer srid;

    @Schema(description = "空间几何，支持点/线/面，SRID=4490")
    @TableField(value = "geom", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Geometry geom;

    @Schema(description = "数据来源（system: 系统手工新增；import: 导入；third: 外部数据）")
    @TableField("source_type")
    private String sourceType;
}
