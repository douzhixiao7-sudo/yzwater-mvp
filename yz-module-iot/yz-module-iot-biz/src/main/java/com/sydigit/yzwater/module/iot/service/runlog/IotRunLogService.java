package com.sydigit.yzwater.module.iot.service.runlog;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogDefaultRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogDispatchOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogSaveReqVO;

import java.util.List;

/**
 * 运行日志 Service
 */
public interface IotRunLogService {

    /**
     * 创建运行日志
     */
    Long createRunLog(IotRunLogSaveReqVO createReqVO);

    /**
     * 更新运行日志
     */
    void updateRunLog(IotRunLogSaveReqVO updateReqVO);

    /**
     * 删除运行日志
     */
    void deleteRunLog(Long id);

    /**
     * 获取运行日志详情
     */
    IotRunLogRespVO getRunLog(Long id);

    /**
     * 分页查询运行日志
     */
    PageResult<IotRunLogRespVO> getRunLogPage(IotRunLogPageReqVO pageReqVO);

    /**
     * 导出查询运行日志
     */
    List<IotRunLogRespVO> getRunLogList(IotRunLogPageReqVO reqVO);

    /**
     * 获取运行日志默认信息
     */
    IotRunLogDefaultRespVO getRunLogDefaults();

    /**
     * 获取关联调令选项
     */
    List<IotRunLogDispatchOptionRespVO> getDispatchOptions(String keyword, String stationId);
}
