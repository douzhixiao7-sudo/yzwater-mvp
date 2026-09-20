package com.sydigit.yzwater.module.dal.dataobject.flood;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Geometry;

/**
 * 防汛物资-单位实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "防汛物资-单位实体")
@TableName(value = "yz_fx_wz_dw", autoResultMap = true)
@TenantIgnore
public class YzFxWzDwDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "单位名称")
    @TableField("dwmc")
    private String unitName;

    @Schema(description = "位置")
    @TableField("pos")
    private String pos;

    @Schema(description = "地址")
    @TableField("dz")
    private String address;

    @Schema(description = "是否代储(0-否 1-是)")
    @TableField("is_delegate_storage")
    private Integer isDelegateStorage;

    @Schema(description = "位置(4490)")
    @TableField(value = "geom", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class)
    private Geometry geom;
}
