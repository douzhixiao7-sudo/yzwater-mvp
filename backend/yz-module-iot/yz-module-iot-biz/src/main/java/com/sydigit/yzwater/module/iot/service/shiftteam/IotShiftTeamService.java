package com.sydigit.yzwater.module.iot.service.shiftteam;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamSaveReqVO;

import java.util.List;

/**
 * 班组 Service
 */
public interface IotShiftTeamService {

    /**
     * 创建班组
     */
    Long createShiftTeam(IotShiftTeamSaveReqVO createReqVO);

    /**
     * 更新班组
     */
    void updateShiftTeam(IotShiftTeamSaveReqVO updateReqVO);

    /**
     * 删除班组
     */
    void deleteShiftTeam(Long id);

    /**
     * 获取班组详情
     */
    IotShiftTeamRespVO getShiftTeam(Long id);

    /**
     * 分页查询班组
     */
    PageResult<IotShiftTeamRespVO> getShiftTeamPage(IotShiftTeamPageReqVO pageReqVO);

    /**
     * 导出查询班组
     */
    List<IotShiftTeamRespVO> getShiftTeamList(IotShiftTeamPageReqVO reqVO);
}
