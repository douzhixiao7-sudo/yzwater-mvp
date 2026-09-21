package com.sydigit.yzwater.module.iot.dal.dataobject.shifthandover;

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
 * 交接班记录 DO
 */
@TableName("yz_shift_handover")
@KeySequence("yz_shift_handover_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotShiftHandoverDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 交接班编号
     */
    private String handoverNo;

    /**
     * 排班 ID（交班对应排班）
     */
    private Long scheduleId;

    /**
     * 交接班时间
     */
    private LocalDateTime handoverTime;

    /**
     * 班次 ID
     */
    private Long shiftId;

    /**
     * 班次名称快照
     */
    private String shiftName;

    /**
     * 班组 ID
     */
    private Long teamId;

    /**
     * 班组名称快照
     */
    private String teamName;

    /**
     * 交班人用户 ID
     */
    private Long handoverUserId;

    /**
     * 交班人姓名快照
     */
    private String handoverUserName;

    /**
     * 接班人用户 ID
     */
    private Long takeoverUserId;

    /**
     * 接班人姓名快照
     */
    private String takeoverUserName;

    /**
     * 值班日志
     */
    private String dutyLog;

    /**
     * 待关注事项
     */
    private String pendingItems;

    /**
     * 关联调度指令ID
     */
    private Long dispatchInstructionId;

    /**
     * 关联调度编号快照
     */
    private String dispatchInstructionNo;

    /**
     * 关联指令名称快照
     */
    private String dispatchInstructionName;

    /**
     * 缺陷/两票关联标识（已停用，不再维护）
     */
    private String defectTicketFlag;

    /**
     * 状态（0待交接 1已交接）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

