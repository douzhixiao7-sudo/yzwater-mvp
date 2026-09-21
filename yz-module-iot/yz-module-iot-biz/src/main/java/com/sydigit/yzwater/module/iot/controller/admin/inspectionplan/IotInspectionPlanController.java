package com.sydigit.yzwater.module.iot.controller.admin.inspectionplan;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanLineOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanStandardOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanTargetOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanTargetRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanTargetDO;
import com.sydigit.yzwater.module.iot.service.inspectionplan.IotInspectionPlanService;
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

@Tag(name = "IoT - 巡检计划")
@RestController
@RequestMapping("/iot/inspection-plan")
@Validated
public class IotInspectionPlanController {

    @Resource
    private IotInspectionPlanService planService;

    /**
     * 创建巡检计划。
     */
    @PostMapping("/create")
    @Operation(summary = "创建巡检计划")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody IotInspectionPlanSaveReqVO createReqVO) {
        return success(planService.createPlan(createReqVO));
    }

    /**
     * 更新巡检计划。
     */
    @PutMapping("/update")
    @Operation(summary = "更新巡检计划")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:update')")
    public CommonResult<Boolean> updatePlan(@Valid @RequestBody IotInspectionPlanSaveReqVO updateReqVO) {
        planService.updatePlan(updateReqVO);
        return success(true);
    }

    /**
     * 删除巡检计划。
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除巡检计划")
    @Parameter(name = "id", description = "计划 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:delete')")
    public CommonResult<Boolean> deletePlan(@RequestParam("id") Long id) {
        planService.deletePlan(id);
        return success(true);
    }

    /**
     * 获取巡检计划详情。
     */
    @GetMapping("/get")
    @Operation(summary = "获取巡检计划详情")
    @Parameter(name = "id", description = "计划 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:query')")
    public CommonResult<IotInspectionPlanRespVO> getPlan(@RequestParam("id") Long id) {
        IotInspectionPlanDO plan = planService.getPlan(id);
        if (plan == null) {
            return success(null);
        }
        List<IotInspectionPlanTargetDO> targets = planService.getPlanTargetList(id);
        Map<Long, String> standardNameMap = plan.getStandardId() == null
                ? Collections.emptyMap()
                : planService.getStandardNameMap(Collections.singletonList(plan.getStandardId()));
        Map<Long, String> lineNameMap = plan.getLineId() == null
                ? Collections.emptyMap()
                : planService.getLineNameMap(Collections.singletonList(plan.getLineId()));
        return success(convertResp(plan, targets, standardNameMap, lineNameMap));
    }

    /**
     * 分页查询巡检计划。
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询巡检计划")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:query')")
    public CommonResult<PageResult<IotInspectionPlanRespVO>> getPlanPage(@Valid IotInspectionPlanPageReqVO pageReqVO) {
        PageResult<IotInspectionPlanDO> pageResult = planService.getPlanPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return success(new PageResult<>(Collections.emptyList(), pageResult.getTotal()));
        }

        List<Long> planIds = CollectionUtils.convertList(pageResult.getList(), IotInspectionPlanDO::getId);
        List<IotInspectionPlanTargetDO> targets = planService.getPlanTargetList(planIds);
        Map<Long, List<IotInspectionPlanTargetDO>> targetMap = targets.stream()
                .collect(Collectors.groupingBy(IotInspectionPlanTargetDO::getPlanId, LinkedHashMap::new, Collectors.toList()));

        List<Long> standardIds = pageResult.getList().stream()
                .map(IotInspectionPlanDO::getStandardId)
                .filter(item -> item != null && item > 0)
                .distinct()
                .toList();
        Map<Long, String> standardNameMap = planService.getStandardNameMap(standardIds);
        List<Long> lineIds = pageResult.getList().stream()
                .map(IotInspectionPlanDO::getLineId)
                .filter(item -> item != null && item > 0)
                .distinct()
                .toList();
        Map<Long, String> lineNameMap = planService.getLineNameMap(lineIds);

        List<IotInspectionPlanRespVO> list = new ArrayList<>();
        for (IotInspectionPlanDO plan : pageResult.getList()) {
            list.add(convertResp(plan, targetMap.getOrDefault(plan.getId(), Collections.emptyList()), standardNameMap, lineNameMap));
        }
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    /**
     * 查询巡检对象下拉。
     */
    @GetMapping("/target-options")
    @Operation(summary = "查询巡检计划对象下拉")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:query')")
    public CommonResult<List<IotInspectionPlanTargetOptionRespVO>> getTargetOptions(
            @RequestParam("objectType") Integer objectType,
            @RequestParam(value = "stationId", required = false) String stationId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return success(planService.listTargetOptions(objectType, stationId, keyword, limit));
    }

    /**
     * 查询巡检标准下拉。
     */
    @GetMapping("/standard-options")
    @Operation(summary = "查询巡检计划标准下拉")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:query')")
    public CommonResult<List<IotInspectionPlanStandardOptionRespVO>> getStandardOptions(
            @RequestParam(value = "stationId", required = false) String stationId,
            @RequestParam(value = "inspectionType", required = false) String inspectionType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return success(planService.listStandardOptions(stationId, inspectionType, keyword, limit));
    }

    /**
     * 查询巡检线路下拉。
     */
    @GetMapping("/line-options")
    @Operation(summary = "查询巡检计划线路下拉")
    @PreAuthorize("@ss.hasPermission('iot:inspection-plan:query')")
    public CommonResult<List<IotInspectionPlanLineOptionRespVO>> getLineOptions(
            @RequestParam(value = "stationId", required = false) String stationId,
            @RequestParam(value = "inspectionType", required = false) String inspectionType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return success(planService.listLineOptions(stationId, inspectionType, keyword, limit));
    }

    /**
     * 组装响应对象。
     */
    private IotInspectionPlanRespVO convertResp(IotInspectionPlanDO plan,
                                                List<IotInspectionPlanTargetDO> targets,
                                                Map<Long, String> standardNameMap,
                                                Map<Long, String> lineNameMap) {
        IotInspectionPlanRespVO respVO = BeanUtils.toBean(plan, IotInspectionPlanRespVO.class);
        respVO.setPlanStartDate(plan.getPlanStartTime());
        respVO.setPlanEndDate(plan.getPlanEndTime());
        respVO.setStandardName(plan.getStandardId() == null ? "" : standardNameMap.getOrDefault(plan.getStandardId(), ""));
        respVO.setLineName(plan.getLineId() == null ? "" : lineNameMap.getOrDefault(plan.getLineId(), ""));
        List<IotInspectionPlanTargetRespVO> targetRespList = new ArrayList<>(targets.size());
        List<Long> targetIds = new ArrayList<>(targets.size());
        List<String> targetNames = new ArrayList<>(targets.size());
        for (IotInspectionPlanTargetDO target : targets) {
            Long targetId = target.getDeviceId() != null ? target.getDeviceId() : target.getLocationId();
            IotInspectionPlanTargetRespVO targetResp = BeanUtils.toBean(target, IotInspectionPlanTargetRespVO.class);
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
        if (respVO.getTargetCount() == null) {
            respVO.setTargetCount(targets.size());
        }
        return respVO;
    }

}
