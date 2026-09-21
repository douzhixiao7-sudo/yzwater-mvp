package com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.sydigit.yzwater.module.iot.dal.mybatis.typehandler.StringArrayTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 调度方案主表 DO
 */
@TableName(value = "yz_dispatch_plan", autoResultMap = true)
@KeySequence("yz_dispatch_plan_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDispatchPlanDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 调度方案编号
     */
    private String planNo;

    /**
     * 方案名称
     */
    private String planName;

    /**
     * 方案类型
     */
    private String planType;

    /**
     * 所属站点
     */
    private String stationId;

    /**
     * 编制人用户 ID
     */
    private Long prepareUserId;

    /**
     * 编制人姓名
     */
    private String prepareUserName;

    /**
     * 编制单位
     */
    private String prepareOrgName;

    /**
     * 编制时间
     */
    private LocalDateTime prepareTime;

    /**
     * 方案状态（0草稿 1已完成 2已归档 3已作废）
     */
    private Integer planStatus;

    /**
     * 核心建议目标
     */
    private String coreTarget;

    /**
     * 涉及工程
     */
    private String projectName;

    /**
     * 预期效果分析
     */
    private String expectedEffect;

    /**
     * 编制说明
     */
    private String prepareDesc;

    /**
     * 附件 URL 数组
     */
    @TableField(value = "attachments", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] attachments;

    /**
     * 操作对象数量
     */
    private Integer objectCount;

    /**
     * 备注
     */
    private String remark;
}
