package com.sydigit.yzwater.module.iot.dal.mysql.shiftteam;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 班组 Mapper
 */
@Mapper
public interface IotShiftTeamMapper extends BaseMapperX<IotShiftTeamDO> {

    /**
     * 分页查询班组
     */
    default PageResult<IotShiftTeamDO> selectPage(IotShiftTeamPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftTeamDO::getUpdateTime)
                .orderByDesc(IotShiftTeamDO::getId));
    }

    /**
     * 导出查询班组
     */
    default List<IotShiftTeamDO> selectListByReqVO(IotShiftTeamPageReqVO reqVO) {
        return selectList(buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftTeamDO::getUpdateTime)
                .orderByDesc(IotShiftTeamDO::getId));
    }

    /**
     * 根据班组名称查询
     */
    default IotShiftTeamDO selectByTeamName(String teamName) {
        return selectOne(IotShiftTeamDO::getTeamName, teamName);
    }

    /**
     * 查询班组编号最大流水号
     */
    @Select("""
            SELECT MAX(CAST(SUBSTRING(TRIM(team_no) FROM (#{prefixLength} + 1)) AS INT))
            FROM yz_shift_team
            WHERE team_no IS NOT NULL
              AND LEFT(TRIM(team_no), #{prefixLength}) = #{prefix}
              AND SUBSTRING(TRIM(team_no) FROM (#{prefixLength} + 1)) ~ '^[0-9]+$'
            """)
    Integer selectMaxTeamNoSeq(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapperX<IotShiftTeamDO> buildQueryWrapper(IotShiftTeamPageReqVO reqVO) {
        return new LambdaQueryWrapperX<IotShiftTeamDO>()
                .eqIfPresent(IotShiftTeamDO::getStationId, reqVO.getStationId())
                .likeIfPresent(IotShiftTeamDO::getTeamName, reqVO.getTeamName())
                .likeIfPresent(IotShiftTeamDO::getLeaderUserName, reqVO.getLeaderUserName());
    }
}
