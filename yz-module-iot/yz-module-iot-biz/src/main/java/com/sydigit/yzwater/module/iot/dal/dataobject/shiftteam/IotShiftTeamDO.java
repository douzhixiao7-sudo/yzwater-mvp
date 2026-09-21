package com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 班组 DO
 */
@TableName("yz_shift_team")
@KeySequence("yz_shift_team_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotShiftTeamDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 班组编号
     */
    private String teamNo;

    /**
     * 班组名称
     */
    private String teamName;

    /**
     * 所属站点
     */
    private String stationId;

    /**
     * 班组长用户 ID
     */
    private Long leaderUserId;

    /**
     * 班组长姓名快照
     */
    private String leaderUserName;

    /**
     * 班组人数
     */
    private Integer memberCount;

    /**
     * 状态（0 启用 1 停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
