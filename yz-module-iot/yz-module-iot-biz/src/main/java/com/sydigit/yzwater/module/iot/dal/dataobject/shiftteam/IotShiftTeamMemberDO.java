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
 * 班组成员 DO
 */
@TableName("yz_shift_team_member")
@KeySequence("yz_shift_team_member_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotShiftTeamMemberDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 班组 ID
     */
    private Long teamId;

    /**
     * 成员用户 ID
     */
    private Long userId;

    /**
     * 成员姓名快照
     */
    private String userName;

    /**
     * 联系方式
     */
    private String mobile;

    /**
     * 岗位名称快照
     */
    private String postName;

    /**
     * 是否班组长（0 否 1 是）
     */
    private Integer isLeader;

    /**
     * 成员排序
     */
    private Integer memberSort;

    /**
     * 状态（0 启用 1 停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
