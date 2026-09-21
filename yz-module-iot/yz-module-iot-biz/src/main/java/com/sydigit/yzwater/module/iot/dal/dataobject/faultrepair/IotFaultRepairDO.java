package com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.sydigit.yzwater.module.iot.dal.mybatis.typehandler.StringArrayTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 故障维修工单 DO
 */
@TableName(value = "yz_equipment_fault_repair", autoResultMap = true)
@KeySequence("yz_equipment_fault_repair_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotFaultRepairDO extends TenantBaseDO {

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
     * 设备名称（快照）
     */
    private String deviceName;
    /**
     * 工单编号
     */
    private String orderNo;
    /**
     * 设备类型（快照）
     */
    private String deviceType;
    /**
     * 故障类型
     */
    private String faultType;
    /**
     * 故障时间
     */
    private LocalDateTime faultTime;
    /**
     * 故障现象
     */
    private String faultSymptom;
    /**
     * 上报人用户 ID
     */
    private Long reporterUserId;
    /**
     * 上报人姓名
     */
    private String reporterName;
    /**
     * 维修人用户 ID
     */
    private Long repairUserId;
    /**
     * 维修人姓名
     */
    private String repairName;
    /**
     * 处理状态
     */
    private String status;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    /**
     * 计划完成时间
     */
    private LocalDateTime planFinishTime;
    /**
     * 故障图片（多张）
     */
    @TableField(value = "fault_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] faultImages;
    /**
     * 备件消耗列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<IotFaultRepairSpareUsageDO> spareUsages;
    /**
     * 备注
     */
    private String remark;
}
