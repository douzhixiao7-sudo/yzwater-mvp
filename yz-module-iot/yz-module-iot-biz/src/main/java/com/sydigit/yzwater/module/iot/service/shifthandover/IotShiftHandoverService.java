package com.sydigit.yzwater.module.iot.service.shifthandover;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverDefaultRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverReminderRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverSaveReqVO;

import java.util.List;

/**
 * 交接班 Service
 */
public interface IotShiftHandoverService {

    /**
     * 创建交接班
     */
    Long createShiftHandover(IotShiftHandoverSaveReqVO createReqVO);

    /**
     * 更新交接班
     */
    void updateShiftHandover(IotShiftHandoverSaveReqVO updateReqVO);

    /**
     * 删除交接班
     */
    void deleteShiftHandover(Long id);

    /**
     * 获取交接班详情
     */
    IotShiftHandoverRespVO getShiftHandover(Long id);

    /**
     * 分页查询交接班
     */
    PageResult<IotShiftHandoverRespVO> getShiftHandoverPage(IotShiftHandoverPageReqVO pageReqVO);

    /**
     * 导出查询交接班
     */
    List<IotShiftHandoverRespVO> getShiftHandoverList(IotShiftHandoverPageReqVO reqVO);

    /**
     * 获取默认交接班信息
     */
    IotShiftHandoverDefaultRespVO getShiftHandoverDefault();

    /**
     * 获取交接班提醒
     */
    IotShiftHandoverReminderRespVO getShiftHandoverReminder();
}

