package com.sydigit.yzwater.module.dal.dataobject.rivers;

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
 * 河段监督单位实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "河段监督单位实体")
@TableName(value = "yz_river_channel_supervision")
@TenantIgnore
public class YzRiverChannelSupervisionDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联河道ID")
    @TableField("river_channel_id")
    private Long riverChannelId;

    @Schema(description = "关联河段ID")
    @TableField("river_section_id")
    private Long riverSectionId;

    @Schema(description = "关联河长ID")
    @TableField("river_channel_management_id")
    private Long riverChannelManagementId;

    @Schema(description = "监督单位")
    @TableField("supervision_unit")
    private String supervisionUnit;

    @Schema(description = "监督电话")
    @TableField("supervision_contact")
    private String supervisionContact;
}
