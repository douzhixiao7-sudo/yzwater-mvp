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

/**
 * 市级防汛抢险队伍实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "市级防汛抢险队伍实体")
@TableName(value = "yz_fx_qxdw", autoResultMap = true)
@TenantIgnore
public class YzFxQxdwDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "单位")
    @TableField("dw")
    private String unitName;

    @Schema(description = "队伍名称")
    @TableField("mc")
    private String teamName;

    @Schema(description = "人数")
    @TableField("rs")
    private Integer planCount;

    @Schema(description = "联系人")
    @TableField("lxr")
    private String contactName;

    @Schema(description = "联系电话")
    @TableField("lxdh")
    private String contactPhone;

    @Schema(description = "备注")
    @TableField("bz")
    private String remark;
}
