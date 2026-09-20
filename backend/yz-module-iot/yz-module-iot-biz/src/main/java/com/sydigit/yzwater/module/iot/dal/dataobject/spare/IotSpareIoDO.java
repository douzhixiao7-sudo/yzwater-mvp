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
 * 备件出入库记录 DO
 */
@TableName("yz_equipment_spare_io")
@KeySequence("yz_equipment_spare_io_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotSpareIoDO extends TenantBaseDO {

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
     * 出入库类型（IN/OUT）
     */
    private String ioType;
    /**
     * 出入库时间
     */
    private LocalDateTime ioTime;
    /**
     * 出入库数量
     */
    private Integer ioQty;
    /**
     * 出库用途类型
     */
    private String usageType;
    /**
     * 用途关联 ID
     */
    private Long usageId;
    /**
     * 操作人用户 ID
     */
    private Long operatorUserId;
    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 审批状态
     */
    private String auditStatus;
    /**
     * 审批人用户 ID
     */
    private Long auditUserId;
    /**
     * 审批人姓名
     */
    private String auditUserName;
    /**
     * 审批时间
     */
    private LocalDateTime auditTime;
    /**
     * 审批备注
     */
    private String auditRemark;
    /**
     * 备注
     */
    private String remark;

}
