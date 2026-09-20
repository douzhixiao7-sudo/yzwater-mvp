package com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调度指令日志 DO
 */
@TableName("yz_dispatch_instruction_log")
@KeySequence("yz_dispatch_instruction_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDispatchInstructionLogDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 调度指令 ID
     */
    private Long instructionId;

    /**
     * 操作类型（1下达 2接收 3反馈 4逾期刷新 5手工修改）
     */
    private Integer actionType;

    /**
     * 操作描述
     */
    private String actionDesc;

    /**
     * 操作人用户 ID
     */
    private Long operatorUserId;

    /**
     * 操作人姓名
     */
    private String operatorUserName;

    /**
     * 操作时间
     */
    private LocalDateTime actionTime;
}

