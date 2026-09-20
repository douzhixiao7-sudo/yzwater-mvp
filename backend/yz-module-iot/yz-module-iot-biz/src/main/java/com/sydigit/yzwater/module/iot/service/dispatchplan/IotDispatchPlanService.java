package com.sydigit.yzwater.module.iot.service.dispatchplan;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 调度方案 Service 接口
 */
public interface IotDispatchPlanService {

    /**
     * 创建调度方案
     */
    Long createPlan(@Valid IotDispatchPlanSaveReqVO createReqVO);

    /**
     * 更新调度方案
     */
    void updatePlan(@Valid IotDispatchPlanSaveReqVO updateReqVO);

    /**
     * 删除调度方案
     */
    void deletePlan(Long id);

    /**
     * 归档调度方案
     */
    void archivePlan(Long id);

    /**
     * 查询调度方案详情
     */
    IotDispatchPlanRespVO getPlan(Long id);

    /**
     * 分页查询调度方案
     */
    PageResult<IotDispatchPlanRespVO> getPlanPage(IotDispatchPlanPageReqVO pageReqVO);

    /**
     * 列表查询调度方案（导出）
     */
    List<IotDispatchPlanRespVO> getPlanList(IotDispatchPlanPageReqVO reqVO);
}

