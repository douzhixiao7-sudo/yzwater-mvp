package com.sydigit.yzwater.module.dal.dataobject.flood;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * 防汛物资实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "防汛物资实体")
@TableName(value = "yz_fx_wz", autoResultMap = true)
@TenantIgnore
public class YzFxWzDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "品名")
    @TableField("pm")
    private String materialName;

    @Schema(description = "数量")
    @TableField("sl")
    private BigDecimal quantity;

    @Schema(description = "单位")
    @TableField("dw")
    private String unit;

    @Schema(description = "物资类型")
    @TableField("wzlx")
    private String materialType;

    @Schema(description = "储备单位")
    @TableField("cbdw")
    private String storageUnit;

    @Schema(description = "是否代储(0-否 1-是)")
    @TableField("is_delegate_storage")
    private Integer isDelegateStorage;

    @Schema(description = "备注")
    @TableField("bz")
    private String remark;

    @Schema(description = "仓库地址")
    @TableField("warehouse_address")
    private String warehouseAddress;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "储备单位ID")
    @TableField("dw_id")
    private String unitId;

    @Schema(description = "联系人")
    @TableField(value = "contact_person", updateStrategy = FieldStrategy.ALWAYS)
    private String contactPerson;

    @Schema(description = "联系方式")
    @TableField(value = "contact_info", updateStrategy = FieldStrategy.ALWAYS)
    private String contactInfo;
}
