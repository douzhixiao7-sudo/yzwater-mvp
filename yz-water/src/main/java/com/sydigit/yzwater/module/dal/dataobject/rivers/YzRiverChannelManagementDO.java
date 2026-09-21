package com.sydigit.yzwater.module.dal.dataobject.rivers;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 河长信息实体（关联河道/河段）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "河长信息实体（关联河道/河段）")
@TableName(value = "yz_river_channel_management", autoResultMap = true)
@TenantIgnore
public class YzRiverChannelManagementDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联河道ID")
    @TableField("river_channel_id")
    private Long riverChannelId;

    @Schema(description = "关联河段ID")
    @TableField("river_section_id")
    private Long riverSectionId;

    @Schema(description = "关联水库ID")
    @TableField("water_reservoir_id")
    private Long waterReservoirId;

    @Schema(description = "关联对象ID")
    @TableField("reference_id")
    private Long referenceId;

    @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）")
    @TableField("reference_type")
    private String referenceType;

    @Schema(description = "行政区划（text[]，可为空）")
    @TableField(value = "administrative_region", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] administrativeRegion;

    @Schema(description = "河段名称")
    @TableField("section_name")
    private String sectionName;

    @Schema(description = "河长级别(字典: zd_hzjb)")
    @TableField("head_level")
    private String headLevel;

    @Schema(description = "河长姓名")
    @TableField("head_name")
    private String headName;

    @Schema(description = "河长职务")
    @TableField("head_position")
    private String headPosition;

    @Schema(description = "河长工作单位")
    @TableField("head_unit")
    private String headUnit;

    @Schema(description = "河长联系电话")
    @TableField("head_contact")
    private String headContact;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;

    @Schema(description = "版本号，从1开始递增")
    @TableField("version_no")
    private Integer versionNo;

    @Schema(description = "记录生效时间")
    @TableField("effective_from")
    private LocalDateTime effectiveFrom;

    @Schema(description = "记录失效时间(当前版本为空)")
    @TableField("effective_to")
    private LocalDateTime effectiveTo;

    @Schema(description = "是否当前版本(1当前,0历史)")
    @TableField("is_current")
    private Integer isCurrent;

    @Schema(description = "对应system_users.id")
    @TableField("user_id")
    private Long userId;
}
