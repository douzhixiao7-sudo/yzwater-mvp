package com.sydigit.yzwater.module.dal.dataobject.flood;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 预警广播站实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "预警广播站实体")
@TableName(value = "yz_warning_broadcast_station", autoResultMap = true)
@TenantIgnore
public class YzWarningBroadcastStationDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "行政区划ID（system_area.id）")
    @TableField("admin_division")
    private String adminDivision;

    @Schema(description = "代码")
    @TableField("code")
    private String code;

    @Schema(description = "数量")
    @TableField("quantity")
    private Integer quantity;
}
