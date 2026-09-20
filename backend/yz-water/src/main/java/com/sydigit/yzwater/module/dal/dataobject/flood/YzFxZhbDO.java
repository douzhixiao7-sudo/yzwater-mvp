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
 * 防汛指挥部实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "防汛指挥部实体")
@TableName(value = "yz_fx_zhb", autoResultMap = true)
@TenantIgnore
public class YzFxZhbDO extends BaseDO {

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

    @Schema(description = "地址")
    @TableField("addr")
    private String addr;

    @Schema(description = "电话")
    @TableField("tel")
    private String tel;

    @Schema(description = "传真")
    @TableField("fax")
    private String fax;

    @Schema(description = "区划代码")
    @TableField("area_code")
    private String areaCode;

    @Schema(description = "邮政编码")
    @TableField("zip_code")
    private String zipCode;
}
