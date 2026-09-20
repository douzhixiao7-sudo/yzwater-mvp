package com.sydigit.yzwater.module.dal.dataobject.flood;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;

/**
 * 防汛物资仓库属性实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "防汛物资仓库属性实体")
@TableName(value = "yz_flood_prevention_material", autoResultMap = true)
@TenantIgnore
public class YzFloodPreventionMaterialWarehouseDO extends BaseDO {

    @Schema(description = "主键 ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "仓库名称")
    @TableField("warehouse_name")
    private String warehouseName;

    @Schema(description = "具体位置")
    @TableField("specific_location")
    private String specificLocation;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "归属单位")
    @TableField("belong_unit")
    private String belongUnit;

    @Schema(description = "负责人姓名")
    @TableField("leader_name")
    private String leaderName;

    @Schema(description = "负责人电话")
    @TableField("leader_phone")
    private String leaderPhone;

    @Schema(description = "物资种类")
    @TableField("material_type")
    private String materialType;

    @Schema(description = "是否代储(0-否 1-是)")
    @TableField("is_delegate_storage")
    private Integer isDelegateStorage;

    @Schema(description = "仓库图片（text[]）")
    @TableField(value = "warehouse_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] warehouseImages;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;

    @Schema(description = "行政划分（text[]）")
    @TableField(value = "division_code", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] divisionCode;
}
