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
 * 组织部成员实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "组织部成员实体")
@TableName(value = "yz_fx_zzbcy", autoResultMap = true)
@TenantIgnore
public class YzFxZzbcyDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "岗位（字典表：zd_zzbgw）")
    @TableField("gw")
    private String position;

    @Schema(description = "姓名")
    @TableField("name")
    private String name;

    @Schema(description = "职务")
    @TableField("title")
    private String title;

    @Schema(description = "电话")
    @TableField("dh")
    private String phone;
}
