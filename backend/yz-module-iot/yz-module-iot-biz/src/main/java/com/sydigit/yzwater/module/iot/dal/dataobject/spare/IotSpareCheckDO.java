package com.sydigit.yzwater.module.iot.dal.dataobject.spare;

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
 * 备件盘点记录 DO
 */
@TableName("yz_equipment_spare_check")
@KeySequence("yz_equipment_spare_check_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotSpareCheckDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;
    /**
     * 备件 ID
     */
    private Long spareId;
    /**
     * 盘点时间
     */
    private LocalDateTime checkTime;
    /**
     * 系统库存
     */
    private Integer systemQty;
    /**
     * 实盘数量
     */
    private Integer actualQty;
    /**
     * 差异数量
     */
    private Integer diffQty;
    /**
     * 盘点结果
     */
    private String resultStatus;
    /**
     * 盘点人用户 ID
     */
    private Long checkerUserId;
    /**
     * 盘点人姓名
     */
    private String checkerUserName;
    /**
     * 是否已反馈
     */
    private Boolean applied;
    /**
     * 反馈人用户 ID
     */
    private Long applyUserId;
    /**
     * 反馈人姓名
     */
    private String applyUserName;
    /**
     * 反馈时间
     */
    private LocalDateTime applyTime;
    /**
     * 备注
     */
    private String remark;

}
