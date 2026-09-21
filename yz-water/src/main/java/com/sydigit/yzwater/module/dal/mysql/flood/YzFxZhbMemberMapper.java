package com.sydigit.yzwater.module.dal.mysql.flood;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberExportReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZhbMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 防汛指挥部成员 Mapper
 */
@Mapper
public interface YzFxZhbMemberMapper extends BaseMapperX<YzFxZhbMemberDO> {

    default LambdaQueryWrapper<YzFxZhbMemberDO> buildQueryWrapper(FxZhbMemberPageReqVO reqVO) {
        return new LambdaQueryWrapper<YzFxZhbMemberDO>()
                .eq(StrUtil.isNotBlank(reqVO.getCommandDepartmentId()),
                        YzFxZhbMemberDO::getCommandDepartmentId, reqVO.getCommandDepartmentId())
                .like(StrUtil.isNotBlank(reqVO.getName()), YzFxZhbMemberDO::getName, reqVO.getName())
                .like(StrUtil.isNotBlank(reqVO.getTitle()), YzFxZhbMemberDO::getTitle, reqVO.getTitle())
                .orderByAsc(YzFxZhbMemberDO::getSort)
                .orderByDesc(YzFxZhbMemberDO::getCreateTime);
    }

    default LambdaQueryWrapper<YzFxZhbMemberDO> buildQueryWrapper(FxZhbMemberExportReqVO reqVO) {
        return new LambdaQueryWrapper<YzFxZhbMemberDO>()
                .eq(StrUtil.isNotBlank(reqVO.getCommandDepartmentId()),
                        YzFxZhbMemberDO::getCommandDepartmentId, reqVO.getCommandDepartmentId())
                .like(StrUtil.isNotBlank(reqVO.getName()), YzFxZhbMemberDO::getName, reqVO.getName())
                .like(StrUtil.isNotBlank(reqVO.getTitle()), YzFxZhbMemberDO::getTitle, reqVO.getTitle())
                .orderByAsc(YzFxZhbMemberDO::getSort)
                .orderByDesc(YzFxZhbMemberDO::getCreateTime);
    }

    default List<YzFxZhbMemberDO> selectByCommandDepartmentId(String commandDepartmentId) {
        if (StrUtil.isBlank(commandDepartmentId)) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<YzFxZhbMemberDO>()
                .eq(YzFxZhbMemberDO::getCommandDepartmentId, commandDepartmentId)
                .orderByAsc(YzFxZhbMemberDO::getSort)
                .orderByDesc(YzFxZhbMemberDO::getCreateTime));
    }

    default int deleteByCommandDepartmentId(String commandDepartmentId) {
        if (StrUtil.isBlank(commandDepartmentId)) {
            return 0;
        }
        return delete(new LambdaQueryWrapper<YzFxZhbMemberDO>()
                .eq(YzFxZhbMemberDO::getCommandDepartmentId, commandDepartmentId));
    }
}
