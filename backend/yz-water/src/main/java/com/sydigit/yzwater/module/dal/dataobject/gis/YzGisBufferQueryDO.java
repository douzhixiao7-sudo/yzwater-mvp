package com.sydigit.yzwater.module.dal.dataobject.gis;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.JsonbMapTypeHandler;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.LongArrayTypeHandler;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 缓冲区查询主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "缓冲区查询主表")
@TableName(value = "yz_gis_buffer_query", autoResultMap = true)
@TenantIgnore
public class YzGisBufferQueryDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "中心点(SRID=4490)")
    @TableField(value = "center_point", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Point centerPoint;

    @Schema(description = "缓冲区半径(米)")
    @TableField("radius_m")
    private BigDecimal radiusM;

    @Schema(description = "缓冲区几何面(SRID=4490)")
    @TableField(value = "buffer_geom", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Geometry bufferGeom;

    @Schema(description = "缓冲区面积(平方米)")
    @TableField("buffer_area_m2")
    private BigDecimal bufferAreaM2;

    @Schema(description = "选中的设施类型列表(zd_sslb)")
    @TableField(value = "facility_types", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] facilityTypes;

    @Schema(description = "设施数量")
    @TableField("facility_count")
    private Integer facilityCount;

    @Schema(description = "分类型统计(JSON)")
    @TableField(value = "facility_type_cnt", jdbcType = JdbcType.OTHER, typeHandler = JsonbMapTypeHandler.class)
    private Map<String, Object> facilityTypeCnt;

    @Schema(description = "涉及行政区划ID列表")
    @TableField(value = "admin_area_ids", jdbcType = JdbcType.ARRAY, typeHandler = LongArrayTypeHandler.class)
    private Long[] adminAreaIds;

    @Schema(description = "涉及行政区划名称列表")
    @TableField(value = "admin_area_names", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] adminAreaNames;

    @Schema(description = "统计时间")
    @TableField("stats_time")
    private LocalDateTime statsTime;
}
