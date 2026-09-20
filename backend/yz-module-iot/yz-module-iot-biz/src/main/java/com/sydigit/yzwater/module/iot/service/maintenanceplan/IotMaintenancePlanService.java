package com.sydigit.yzwater.module.iot.service.maintenanceplan;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanCreateReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanSubmitReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan.IotMaintenancePlanDO;
import jakarta.validation.Valid;

/**
 * 养护计划 Service 接口
 */
public interface IotMaintenancePlanService {

    /**
     * 获取养护计划分页
     *
     * @param pageReqVO 分页查询
     * @return 分页数据
     */
    PageResult<IotMaintenancePlanDO> getMaintenancePlanPage(IotMaintenancePlanPageReqVO pageReqVO);

    /**
     * 获取养护计划详情
     *
     * @param id 主键 ID
     * @return 养护计划
     */
    IotMaintenancePlanDO getMaintenancePlan(Long id);

    /**
     * 创建养护计划（手动新增）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaintenancePlan(@Valid IotMaintenancePlanCreateReqVO createReqVO);

    /**
     * 提交养护信息
     *
     * @param submitReqVO 提交信息
     */
    void submitMaintenancePlan(@Valid IotMaintenancePlanSubmitReqVO submitReqVO);

    /**
     * 同步设备自动养护计划
     *
     * @param deviceId 设备 ID
     */
    void syncAutoMaintenancePlanByDevice(Long deviceId);
}
