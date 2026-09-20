package com.sydigit.yzwater.module.iot.dal.mysql.shiftconfig;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftconfig.IotShiftConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalTime;
import java.util.List;

/**
 * 班次配置 Mapper
 */
@Mapper
public interface IotShiftConfigMapper extends BaseMapperX<IotShiftConfigDO> {

    /**
     * 分页查询班次配置
     */
    default PageResult<IotShiftConfigDO> selectPage(IotShiftConfigPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftConfigDO::getUpdateTime)
                .orderByDesc(IotShiftConfigDO::getId));
    }

    /**
     * 导出查询班次配置
     */
    default List<IotShiftConfigDO> selectListByReqVO(IotShiftConfigPageReqVO reqVO) {
        return selectList(buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftConfigDO::getUpdateTime)
                .orderByDesc(IotShiftConfigDO::getId));
    }

    /**
     * 根据班次名称查询
     */
    default IotShiftConfigDO selectByShiftName(String shiftName) {
        return selectOne(IotShiftConfigDO::getShiftName, shiftName);
    }

    /**
     * 查询班次编号最大流水号
     */
    @Select("""
            SELECT MAX(CAST(SUBSTRING(TRIM(shift_no) FROM (#{prefixLength} + 1)) AS INT))
            FROM yz_shift_config
            WHERE shift_no IS NOT NULL
              AND LEFT(TRIM(shift_no), #{prefixLength}) = #{prefix}
              AND SUBSTRING(TRIM(shift_no) FROM (#{prefixLength} + 1)) ~ '^[0-9]+$'
            """)
    Integer selectMaxShiftNoSeq(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapperX<IotShiftConfigDO> buildQueryWrapper(IotShiftConfigPageReqVO reqVO) {
        LambdaQueryWrapperX<IotShiftConfigDO> queryWrapper = new LambdaQueryWrapperX<IotShiftConfigDO>()
                .eqIfPresent(IotShiftConfigDO::getStationId, reqVO.getStationId())
                .likeIfPresent(IotShiftConfigDO::getShiftName, reqVO.getShiftName());

        LocalTime[] timeRange = reqVO.getTimeRange();
        if (timeRange != null && timeRange.length == 2 && timeRange[0] != null && timeRange[1] != null) {
            queryWrapper.ge(IotShiftConfigDO::getStartTime, timeRange[0])
                    .le(IotShiftConfigDO::getEndTime, timeRange[1]);
        }
        return queryWrapper;
    }
}
