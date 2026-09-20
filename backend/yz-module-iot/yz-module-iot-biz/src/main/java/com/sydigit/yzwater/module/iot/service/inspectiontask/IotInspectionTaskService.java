package com.sydigit.yzwater.module.iot.service.inspectiontask;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSubmitResultReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskTargetDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 巡检任务 Service 接口
 */
public interface IotInspectionTaskService {

    /**
     * 创建巡检任务（人工创建）。
     */
    Long createTask(@Valid IotInspectionTaskSaveReqVO createReqVO);

    /**
     * 更新巡检任务（仅人工任务可编辑）。
     */
    void updateTask(@Valid IotInspectionTaskSaveReqVO updateReqVO);

    /**
     * 根据巡检计划生成任务（按计划默认时间窗）。
     */
    Long createTaskFromPlan(Long planId);

    /**
     * 根据巡检计划与指定时间窗生成任务。
     */
    Long createTaskFromPlanByWindow(Long planId, LocalDateTime planStartTime, LocalDateTime planEndTime);

    /**
     * 巡检计划是否允许编辑。
     */
    boolean isPlanEditable(Long planId);

    /**
     * 删除巡检计划前，级联删除满足条件的自动任务。
     * 若存在已处理任务则抛出异常，阻止删除巡检计划。
     */
    void cascadeDeletePlanAutoTasks(Long planId);

    /**
     * 提交巡检结果并结束流程任务。
     */
    void submitTaskResult(@Valid IotInspectionTaskSubmitResultReqVO reqVO);

    /**
     * 删除巡检任务。
     */
    void deleteTask(Long id);

    /**
     * 获取巡检任务详情。
     */
    IotInspectionTaskDO getTask(Long id);

    /**
     * 校验巡检任务是否存在。
     */
    IotInspectionTaskDO validateTaskExists(Long id);

    /**
     * 分页查询巡检任务。
     */
    PageResult<IotInspectionTaskDO> getTaskPage(IotInspectionTaskPageReqVO pageReqVO);

    /**
     * 根据任务 ID 获取巡检对象列表。
     */
    List<IotInspectionTaskTargetDO> getTaskTargetList(Long taskId);

    /**
     * 根据任务 ID 集合批量获取巡检对象列表。
     */
    List<IotInspectionTaskTargetDO> getTaskTargetList(Collection<Long> taskIds);

}
