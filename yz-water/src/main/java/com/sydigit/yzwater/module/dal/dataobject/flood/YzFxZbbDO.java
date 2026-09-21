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
 * 值班表头实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "值班表头实体")
@TableName(value = "yz_fx_zbb", autoResultMap = true)
@TenantIgnore
public class YzFxZbbDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "开始日期")
    @TableField("ksrq")
    private String startDate;

    @Schema(description = "结束日期")
    @TableField("jsrq")
    private String endDate;

    @Schema(description = "值班说明")
    @TableField("description")
    private String description;
}
