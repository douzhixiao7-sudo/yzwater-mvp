package com.sydigit.yzwater.module.dal.dataobject.gis;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 缓冲区查询结果快照
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "缓冲区查询结果快照")
@TableName(value = "yz_gis_buffer_query_item", autoResultMap = true)
@TenantIgnore
public class YzGisBufferQueryItemDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "缓冲区主表ID")
    @TableField("buffer_id")
    private Long bufferId;

    @Schema(description = "设施基础表ID")
    @TableField("facility_id")
    private Long facilityId;

    @Schema(description = "设施类型(zd_sslb)")
    @TableField("facility_type")
    private String facilityType;

    @Schema(description = "设施名称")
    @TableField("facility_name")
    private String facilityName;

    @Schema(description = "行政区划编码")
    @TableField("admin_region_code")
    private String adminRegionCode;

    @Schema(description = "几何类型")
    @TableField("geom_type")
    private String geomType;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;
}
