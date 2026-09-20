package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanSaveReqVO;
import com.sydigit.yzwater.module.iot.convert.dispatchplan.IotDispatchPlanConvert;
import com.sydigit.yzwater.module.iot.service.dispatchplan.IotDispatchPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 调度方案 Controller
 */
@Tag(name = "IoT - 调度方案")
@RestController
@RequestMapping("/iot/dispatch-plan")
@Validated
public class IotDispatchPlanController {

    @Resource
    private IotDispatchPlanService dispatchPlanService;

    @PostMapping("/create")
    @Operation(summary = "创建调度方案")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody IotDispatchPlanSaveReqVO createReqVO) {
        return success(dispatchPlanService.createPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新调度方案")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-plan:update')")
    public CommonResult<Boolean> updatePlan(@Valid @RequestBody IotDispatchPlanSaveReqVO updateReqVO) {
        dispatchPlanService.updatePlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除调度方案")
    @Parameter(name = "id", description = "调度方案 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-plan:delete')")
    public CommonResult<Boolean> deletePlan(@RequestParam("id") Long id) {
        dispatchPlanService.deletePlan(id);
        return success(true);
    }

    @PutMapping("/archive")
    @Operation(summary = "归档调度方案")
    @Parameter(name = "id", description = "调度方案 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-plan:archive')")
    public CommonResult<Boolean> archivePlan(@RequestParam("id") Long id) {
        dispatchPlanService.archivePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取调度方案详情")
    @Parameter(name = "id", description = "调度方案 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-plan:query')")
    public CommonResult<IotDispatchPlanRespVO> getPlan(@RequestParam("id") Long id) {
        return success(dispatchPlanService.getPlan(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询调度方案")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-plan:query')")
    public CommonResult<PageResult<IotDispatchPlanRespVO>> getPlanPage(@Valid IotDispatchPlanPageReqVO pageReqVO) {
        return success(dispatchPlanService.getPlanPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出调度方案 Excel")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPlanExcel(@Valid IotDispatchPlanPageReqVO exportReqVO,
                                HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDispatchPlanRespVO> list = dispatchPlanService.getPlanList(exportReqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "调度方案.xls", "调度方案", IotDispatchPlanExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotDispatchPlanExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotDispatchPlanExportExcelVO excelVO = IotDispatchPlanConvert.INSTANCE.convertExcel(item);
            excelVO.setObjectNames(item.getObjectNames() == null ? "" : String.join("；", item.getObjectNames()));
            excelVO.setAttachmentCount(item.getAttachments() == null ? 0 : item.getAttachments().size());
            return excelVO;
        });
        ExcelUtils.write(response, "调度方案.xls", "调度方案", IotDispatchPlanExportExcelVO.class, excelList);
    }
}

