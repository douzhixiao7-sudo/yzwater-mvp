package com.sydigit.yzwater.module.iot.dal.dataobject.shiftconfig;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * 班次配置 DO
 */
@TableName("yz_shift_config")
@KeySequence("yz_shift_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotShiftConfigDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 班次编号
     */
    private String shiftNo;

    /**
     * 班次名称
     */
    private String shiftName;

    /**
     * 所属站点
     */
    private String stationId;

    /**
     * 起始时间
     */
    private LocalTime startTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 跨天标识（0 否 1 是）
     */
    private Integer crossDay;

    /**
     * 状态（0 启用 1 停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
