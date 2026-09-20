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
 * 值班表体实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "值班表体实体")
@TableName(value = "yz_fx_zbb_list", autoResultMap = true)
@TenantIgnore
public class YzFxZbbListDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "值班表ID")
    @TableField("zbb_id")
    private String zbbId;

    @Schema(description = "每周天")
    @TableField("week_day")
    private Integer weekDay;

    @Schema(description = "值班长姓名")
    @TableField("zbz_name")
    private String dutyChiefName;

    @Schema(description = "值班长电话")
    @TableField("zbz_mobile")
    private String dutyChiefMobile;

    @Schema(description = "白班人员姓名")
    @TableField("kz_name")
    private String sectionChiefName;

    @Schema(description = "白班人员电话")
    @TableField("kz_mobile")
    private String sectionChiefMobile;

    @Schema(description = "夜班人员姓名")
    @TableField("zby_name")
    private String dutyStaffName;

    @Schema(description = "夜班人员电话")
    @TableField("zby_mobile")
    private String dutyStaffMobile;
}
