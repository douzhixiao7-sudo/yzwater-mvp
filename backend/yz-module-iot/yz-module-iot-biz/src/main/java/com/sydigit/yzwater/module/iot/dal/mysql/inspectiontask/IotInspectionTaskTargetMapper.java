package com.sydigit.yzwater.module.iot.dal.mysql.inspectiontask;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskTargetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface IotInspectionTaskTargetMapper extends BaseMapperX<IotInspectionTaskTargetDO> {

    /**
     * 根据任务 ID 查询对象明细。
     */
    default List<IotInspectionTaskTargetDO> selectListByTaskId(Long taskId) {
        if (taskId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionTaskTargetDO>()
                .eq(IotInspectionTaskTargetDO::getTaskId, taskId)
                .orderByAsc(IotInspectionTaskTargetDO::getTargetSort)
                .orderByAsc(IotInspectionTaskTargetDO::getId));
    }

    /**
     * 根据任务 ID 集合查询对象明细。
     */
    default List<IotInspectionTaskTargetDO> selectListByTaskIds(Collection<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionTaskTargetDO>()
                .in(IotInspectionTaskTargetDO::getTaskId, taskIds)
                .orderByAsc(IotInspectionTaskTargetDO::getTargetSort)
                .orderByAsc(IotInspectionTaskTargetDO::getId));
    }

    /**
     * 根据任务 ID 删除对象明细。
     */
    default void deleteByTaskId(Long taskId) {
        if (taskId == null) {
            return;
        }
        deleteByTaskIdLogicSafe(taskId);
    }

    /**
     * 逻辑删除任务对象明细：
     * 先把 task_id 改为 -id，再置 deleted=1，避免唯一索引冲突。
     */
    @Update("UPDATE yz_equipment_inspection_task_target " +
            "SET task_id = -id, deleted = 1, update_time = CURRENT_TIMESTAMP " +
            "WHERE task_id = #{taskId} AND deleted = 0")
    void deleteByTaskIdLogicSafe(@Param("taskId") Long taskId);

}
