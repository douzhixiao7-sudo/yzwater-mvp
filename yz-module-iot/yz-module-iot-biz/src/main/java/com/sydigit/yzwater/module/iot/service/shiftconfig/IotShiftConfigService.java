package com.sydigit.yzwater.module.iot.service.shiftconfig;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigSaveReqVO;

import java.util.List;

/**
 * 班次配置 Service
 */
public interface IotShiftConfigService {

    /**
     * 创建班次
     */
    Long createShiftConfig(IotShiftConfigSaveReqVO createReqVO);

    /**
     * 更新班次
     */
    void updateShiftConfig(IotShiftConfigSaveReqVO updateReqVO);

    /**
     * 删除班次
     */
    void deleteShiftConfig(Long id);

    /**
     * 获取班次详情
     */
    IotShiftConfigRespVO getShiftConfig(Long id);

    /**
     * 分页查询班次
     */
    PageResult<IotShiftConfigRespVO> getShiftConfigPage(IotShiftConfigPageReqVO pageReqVO);

    /**
     * 导出查询班次
     */
    List<IotShiftConfigRespVO> getShiftConfigList(IotShiftConfigPageReqVO reqVO);
}
