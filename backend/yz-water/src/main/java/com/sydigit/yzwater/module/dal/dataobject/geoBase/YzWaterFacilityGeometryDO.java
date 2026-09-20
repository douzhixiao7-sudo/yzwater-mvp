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
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.Map;

/**
 * 水利对象空间信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "水利对象空间信息实体-已废弃，不使用")
@TableName(value = "yz_water_facility_geometry", autoResultMap = true)
@TenantIgnore
@Deprecated
public class YzWaterFacilityGeometryDO extends BaseDO {

    @Schema(description = "主键 ID")
    @TableId(value = "id", type = IdType.INPUT)
    @TableField("id")
    private Long id;

    @Schema(description = "关联基础表 ID")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "同步基础表编码")
    @TableField("facility_code")
    private String facilityCode;

    @Schema(description = "行政区划名称")
    @TableField("admin_region")
    private String adminRegion;

    @Schema(description = "行政区划编码")
    @TableField("admin_region_code")
    private String adminRegionCode;

    @Schema(description = "空间几何，支持点/线/面，SRID=4490")
    @TableField(value = "geom", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Geometry geom;

    @Schema(description = "几何用途（主线、边界、中心点、缓冲区等）")
    @TableField("purpose")
    private String purpose;

    @Schema(description = "中心点")
    @TableField(value = "centroid", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Point centroid;

    @Schema(description = "外包络（多边形）")
    @TableField(value = "envelope", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Polygon envelope;

    @Schema(description = "扩展属性（JSON 格式）")
    @TableField(value = "attributes", jdbcType = JdbcType.OTHER, typeHandler = JsonbMapTypeHandler.class)
    private Map<String, Object> attributes;

    @Schema(description = "几何类型（POINT、LINESTRING、POLYGON 等）")
    @TableField("geom_type")
    private String geomType;

    @Schema(description = "坐标系编号")
    @TableField("srid")
    private Integer srid;

    @Schema(description = "数据来源（system: 系统手工新增；import: 导入；third: 外部数据）")
    @TableField("source_type")
    private String sourceType;
}
