package com.sydigit.yzwater.module.iot.service.faultrepair;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairAuditAssignReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairResultReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair.IotFaultRepairDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 故障维修工单 Service 接口
 */
public interface IotFaultRepairService {

    /**
     * 创建故障维修工单
     */
    Long createFaultRepair(@Valid IotFaultRepairSaveReqVO createReqVO);

    /**
     * 更新故障维修工单
     */
    void updateFaultRepair(@Valid IotFaultRepairSaveReqVO updateReqVO);

    /**
     * 删除故障维修工单
     */
    void deleteFaultRepair(Long id);

    /**
     * 获取故障维修工单
     */
    IotFaultRepairDO getFaultRepair(Long id);

    /**
     * 故障维修工单分页
     */
    PageResult<IotFaultRepairDO> getFaultRepairPage(IotFaultRepairPageReqVO pageReqVO);

    /**
     * 故障维修工单导出列表
     */
    List<IotFaultRepairDO> getFaultRepairList(IotFaultRepairPageReqVO exportReqVO);

    /**
     * 审核派工
     */
    void auditAssign(@Valid IotFaultRepairAuditAssignReqVO reqVO);

    /**
     * 反馈维修结果
     */
    void submitResult(@Valid IotFaultRepairResultReqVO reqVO);
}
