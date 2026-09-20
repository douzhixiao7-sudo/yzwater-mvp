package com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 养护计划 DO
 */
@TableName(value = "yz_equipment_maintenance", autoResultMap = true)
@KeySequence("yz_equipment_maintenance_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotMaintenancePlanDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;
    /**
     * 设备 ID
     */
    private Long deviceId;
    /**
     * 所属闸站字典值
     */
    private String stationId;
    /**
     * 计划养护日期
     */
    private LocalDate planDate;
    /**
     * 养护类型
     */
    private String maintainType;
    /**
     * 养护项目
     */
    private String maintainItems;
    /**
     * 备件消耗列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<IotMaintenancePlanSpareUsageDO> spareUsages;
    /**
     * 养护人用户 ID
     */
    private Long maintainerUserId;
    /**
     * 养护人姓名
     */
    private String maintainerName;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    /**
     * 养护状态
     */
    private String status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 数据来源（auto/manual）
     */
    private String sourceType;
}
