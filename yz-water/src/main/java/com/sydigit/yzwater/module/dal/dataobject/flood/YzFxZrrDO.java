package com.sydigit.yzwater.module.dal.dataobject.flood;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 防汛责任人实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "防汛责任人实体")
@TableName(value = "yz_fx_zrr", autoResultMap = true)
@TenantIgnore
public class YzFxZrrDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "类型:1 市级 2 园区")
    @TableField("type")
    private String type;

    @Schema(description = "区划代码")
    @TableField("division_code")
    private String divisionCode;

    @Schema(description = "行政姓名")
    @TableField("xz_name")
    private String administrativeName;

    @Schema(description = "行政职务")
    @TableField("xz_title")
    private String administrativeTitle;

    @Schema(description = "技术姓名")
    @TableField("js_name")
    private String technicalName;

    @Schema(description = "技术职务")
    @TableField("js_title")
    private String technicalTitle;
}
