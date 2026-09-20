package com.sydigit.yzwater.module.iot.dal.mysql.shiftteam;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 班组成员 Mapper
 */
@Mapper
public interface IotShiftTeamMemberMapper extends BaseMapperX<IotShiftTeamMemberDO> {

    /**
     * 查询单个班组成员列表
     */
    default List<IotShiftTeamMemberDO> selectListByTeamId(Long teamId) {
        if (teamId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotShiftTeamMemberDO>()
                .eq(IotShiftTeamMemberDO::getTeamId, teamId)
                .orderByAsc(IotShiftTeamMemberDO::getMemberSort)
                .orderByAsc(IotShiftTeamMemberDO::getId));
    }

    /**
     * 查询多个班组成员列表
     */
    default List<IotShiftTeamMemberDO> selectListByTeamIds(Collection<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotShiftTeamMemberDO>()
                .in(IotShiftTeamMemberDO::getTeamId, teamIds)
                .orderByAsc(IotShiftTeamMemberDO::getTeamId)
                .orderByAsc(IotShiftTeamMemberDO::getMemberSort)
                .orderByAsc(IotShiftTeamMemberDO::getId));
    }

    /**
     * 根据班组 ID 删除成员
     */
    default void deleteByTeamId(Long teamId) {
        if (teamId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotShiftTeamMemberDO>().eq(IotShiftTeamMemberDO::getTeamId, teamId));
    }
}
