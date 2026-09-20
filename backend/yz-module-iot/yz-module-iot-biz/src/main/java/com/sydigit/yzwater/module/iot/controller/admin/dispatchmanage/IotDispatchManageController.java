package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManagePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManagePlanOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageReceiverUserRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageSubmitResultReqVO;
import com.sydigit.yzwater.module.iot.service.dispatchmanage.IotDispatchManageService;
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
 * 调度管理 Controller
 */
@Tag(name = "IoT - 调度管理")
@RestController
@RequestMapping("/iot/dispatch-manage")
@Validated
public class IotDispatchManageController {

    @Resource
    private IotDispatchManageService dispatchManageService;

    @PostMapping("/create")
    @Operation(summary = "创建调度指令")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:create')")
    public CommonResult<Long> createInstruction(@Valid @RequestBody IotDispatchManageSaveReqVO createReqVO) {
        return success(dispatchManageService.createInstruction(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新调度指令")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:update')")
    public CommonResult<Boolean> updateInstruction(@Valid @RequestBody IotDispatchManageSaveReqVO updateReqVO) {
        dispatchManageService.updateInstruction(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除调度指令")
    @Parameter(name = "id", description = "调度指令 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:delete')")
    public CommonResult<Boolean> deleteInstruction(@RequestParam("id") Long id) {
        dispatchManageService.deleteInstruction(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取调度指令详情")
    @Parameter(name = "id", description = "调度指令 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:query')")
    public CommonResult<IotDispatchManageRespVO> getInstruction(@RequestParam("id") Long id) {
        return success(dispatchManageService.getInstruction(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询调度指令")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:query')")
    public CommonResult<PageResult<IotDispatchManageRespVO>> getInstructionPage(@Valid IotDispatchManagePageReqVO pageReqVO) {
        return success(dispatchManageService.getInstructionPage(pageReqVO));
    }

    @PostMapping("/submit-result")
    @Operation(summary = "提交调度反馈")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:submit')")
    public CommonResult<Boolean> submitResult(@Valid @RequestBody IotDispatchManageSubmitResultReqVO reqVO) {
        dispatchManageService.submitResult(reqVO);
        return success(true);
    }

    @GetMapping("/plan-options")
    @Operation(summary = "获取调度方案选项")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:query')")
    public CommonResult<List<IotDispatchManagePlanOptionRespVO>> getPlanOptions(
            @RequestParam(value = "planStatus", required = false) Integer planStatus,
            @RequestParam(value = "stationId", required = false) String stationId) {
        return success(dispatchManageService.getPlanOptions(planStatus, stationId));
    }

    @GetMapping("/receiver-user-list")
    @Operation(summary = "根据接收单位获取接收人列表")
    @Parameter(name = "deptId", description = "部门 ID", required = true, example = "100")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:query')")
    public CommonResult<List<IotDispatchManageReceiverUserRespVO>> getReceiverUserList(@RequestParam("deptId") Long deptId) {
        return success(dispatchManageService.getReceiverUserList(deptId));
    }

    @GetMapping("/executor-user-list")
    @Operation(summary = "获取执行人列表（全系统启用用户，排除游客角色）")
    @Parameter(name = "deptId", description = "部门 ID（兼容参数，可不传）", required = false, example = "100")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:query')")
    public CommonResult<List<IotDispatchManageReceiverUserRespVO>> getExecutorUserList(
            @RequestParam(value = "deptId", required = false) Long deptId) {
        return success(dispatchManageService.getExecutorUserList(deptId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出调度管理 Excel")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-manage:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInstructionExcel(@Valid IotDispatchManagePageReqVO exportReqVO,
                                       HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDispatchManageRespVO> list = dispatchManageService.getInstructionList(exportReqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "调度管理.xls", "调度管理", IotDispatchManageExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotDispatchManageExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotDispatchManageExportExcelVO excelVO = new IotDispatchManageExportExcelVO();
            excelVO.setInstructionNo(item.getInstructionNo());
            excelVO.setIssueOrgName(item.getIssueOrgName());
            excelVO.setIssueUserName(item.getIssueUserName());
            excelVO.setInstructionContent(item.getInstructionContent());
            excelVO.setPlanNames(item.getPlanNames() == null ? "" : String.join("；", item.getPlanNames()));
            excelVO.setPlannedFinishTime(item.getPlannedFinishTime());
            excelVO.setReceiverDeptName(item.getReceiverDeptName());
            excelVO.setReceiverUserName(item.getReceiverUserName());
            excelVO.setExecutorUserName(item.getExecutorUserName());
            excelVO.setStatusName(item.getStatusName());
            excelVO.setRunLogCount(item.getRunLogCount());
            excelVO.setCreateTime(item.getCreateTime());
            return excelVO;
        });
        ExcelUtils.write(response, "调度管理.xls", "调度管理", IotDispatchManageExportExcelVO.class, excelList);
    }
}
