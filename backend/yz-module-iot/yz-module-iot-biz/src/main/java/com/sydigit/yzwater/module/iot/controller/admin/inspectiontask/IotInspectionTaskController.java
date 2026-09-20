package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSubmitResultItemVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSubmitResultReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskTargetRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskTargetDO;
import com.sydigit.yzwater.module.iot.service.inspectiontask.IotInspectionTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "IoT - 巡检任务")
@RestController
@RequestMapping("/iot/inspection-task")
@Validated
public class IotInspectionTaskController {

    @Resource
    private IotInspectionTaskService taskService;

    /**
     * 创建巡检任务（人工创建）。
     */
    @PostMapping("/create")
    @Operation(summary = "创建巡检任务")
    @PreAuthorize("@ss.hasPermission('iot:inspection-task:create')")
    public CommonResult<Long> createTask(@Valid @RequestBody IotInspectionTaskSaveReqVO createReqVO) {
        return success(taskService.createTask(createReqVO));
    }

    /**
     * 更新巡检任务（仅人工创建任务可编辑）。
     */
    @PutMapping("/update")
    @Operation(summary = "更新巡检任务")
    @PreAuthorize("@ss.hasPermission('iot:inspection-task:update')")
    public CommonResult<Boolean> updateTask(@Valid @RequestBody IotInspectionTaskSaveReqVO updateReqVO) {
        taskService.updateTask(updateReqVO);
        return success(true);
    }

    /**
     * 提交巡检结果（执行人提交后，流程结束）。
     */
    @PostMapping("/submit-result")
    @Operation(summary = "提交巡检结果")
    @PreAuthorize("@ss.hasPermission('iot:inspection-task:submit')")
    public CommonResult<Boolean> submitTaskResult(@Valid @RequestBody IotInspectionTaskSubmitResultReqVO reqVO) {
        taskService.submitTaskResult(reqVO);
        return success(true);
    }

    /**
     * 删除巡检任务。
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除巡检任务")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-task:delete')")
    public CommonResult<Boolean> deleteTask(@RequestParam("id") Long id) {
        taskService.deleteTask(id);
        return success(true);
    }

    /**
     * 获取巡检任务详情。
     */
    @GetMapping("/get")
    @Operation(summary = "获取巡检任务详情")
    @Parameter(name = "id", description = "任务 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-task:query')")
    public CommonResult<IotInspectionTaskRespVO> getTask(@RequestParam("id") Long id) {
        IotInspectionTaskDO task = taskService.getTask(id);
        if (task == null) {
            return success(null);
        }
        List<IotInspectionTaskTargetDO> targets = taskService.getTaskTargetList(id);
        return success(convertResp(task, targets, true));
    }

    /**
     * 分页查询巡检任务。
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询巡检任务")
    @PreAuthorize("@ss.hasPermission('iot:inspection-task:query')")
    public CommonResult<PageResult<IotInspectionTaskRespVO>> getTaskPage(@Valid IotInspectionTaskPageReqVO pageReqVO) {
        PageResult<IotInspectionTaskDO> pageResult = taskService.getTaskPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return success(new PageResult<>(Collections.emptyList(), pageResult.getTotal()));
        }
        List<Long> taskIds = CollectionUtils.convertList(pageResult.getList(), IotInspectionTaskDO::getId);
        List<IotInspectionTaskTargetDO> targets = taskService.getTaskTargetList(taskIds);
        Map<Long, List<IotInspectionTaskTargetDO>> targetMap = targets.stream()
                .collect(Collectors.groupingBy(IotInspectionTaskTargetDO::getTaskId, LinkedHashMap::new, Collectors.toList()));
        List<IotInspectionTaskRespVO> list = new ArrayList<>();
        for (IotInspectionTaskDO task : pageResult.getList()) {
            list.add(convertResp(task, targetMap.getOrDefault(task.getId(), Collections.emptyList()), false));
        }
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    /**
     * 组装响应对象。
     */
    private IotInspectionTaskRespVO convertResp(IotInspectionTaskDO task,
                                                List<IotInspectionTaskTargetDO> targets,
                                                boolean includeSubmitItems) {
        IotInspectionTaskRespVO respVO = BeanUtils.toBean(task, IotInspectionTaskRespVO.class);
        respVO.setItems(includeSubmitItems ? parseSubmitResultItems(task.getResultItemsJson()) : Collections.emptyList());
        List<IotInspectionTaskTargetRespVO> targetRespList = new ArrayList<>(targets.size());
        List<Long> targetIds = new ArrayList<>(targets.size());
        List<String> targetNames = new ArrayList<>(targets.size());
        for (IotInspectionTaskTargetDO target : targets) {
            Long targetId = target.getDeviceId() != null ? target.getDeviceId() : target.getLocationId();
            IotInspectionTaskTargetRespVO targetResp = BeanUtils.toBean(target, IotInspectionTaskTargetRespVO.class);
            targetResp.setTargetId(targetId);
            targetRespList.add(targetResp);
            if (targetId != null) {
                targetIds.add(targetId);
            }
            targetNames.add(target.getTargetName());
        }
        respVO.setTargets(targetRespList);
        respVO.setTargetIds(targetIds);
        respVO.setTargetNames(targetNames);
        respVO.setTargetCount(targets.size());
        return respVO;
    }

    /**
     * 解析巡检任务提交结果明细 JSON，兼容历史空值。
     */
    private List<IotInspectionTaskSubmitResultItemVO> parseSubmitResultItems(String resultItemsJson) {
        if (resultItemsJson == null || resultItemsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<IotInspectionTaskSubmitResultItemVO> items =
                    JsonUtils.parseArray(resultItemsJson, IotInspectionTaskSubmitResultItemVO.class);
            return items == null ? Collections.emptyList() : items;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

}
