package com.sydigit.yzwater.module.iot.dal.dataobject.shiftschedule;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工排班 DO
 */
@TableName("yz_shift_schedule")
@KeySequence("yz_shift_schedule_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotShiftScheduleDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 排班编号
     */
    private String scheduleNo;

    /**
     * 值班日期
     */
    private LocalDate scheduleDate;

    /**
     * 所属站点
     */
    private String stationId;

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
     * 值班人员 ID
     */
    private Long dutyUserId;

    /**
     * 值班人员姓名快照
     */
    private String dutyUserName;

    /**
     * 联系方式快照
     */
    private String dutyMobile;

    /**
     * 岗位快照
     */
    private String dutyPostName;

    /**
     * 值班开始时间
     */
    private LocalDateTime dutyStartTime;

    /**
     * 值班结束时间
     */
    private LocalDateTime dutyEndTime;

    /**
     * 值班日志
     */
    private String dutyLog;

    /**
     * 状态（0 待值班 1 值班中 2 已完成 3 已取消）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
