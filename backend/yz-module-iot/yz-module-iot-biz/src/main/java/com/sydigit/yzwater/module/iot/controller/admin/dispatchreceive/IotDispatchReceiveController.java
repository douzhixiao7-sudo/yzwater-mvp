package com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceivePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveRunLogOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveSubmitReqVO;
import com.sydigit.yzwater.module.iot.service.dispatchmanage.IotDispatchManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
 * 调令接受 Controller
 */
@Tag(name = "IoT - 调令接受")
@RestController
@RequestMapping("/iot/dispatch-receive")
@Validated
public class IotDispatchReceiveController {

    @Resource
    private IotDispatchManageService dispatchManageService;

    @GetMapping("/page")
    @Operation(summary = "分页查询调令接受任务")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-receive:query')")
    public CommonResult<PageResult<IotDispatchReceiveRespVO>> getReceivePage(@Valid IotDispatchReceivePageReqVO reqVO) {
        return success(dispatchManageService.getReceivePage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取调令接受详情")
    @Parameter(name = "id", description = "调令 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-receive:query')")
    public CommonResult<IotDispatchReceiveRespVO> getReceive(@RequestParam("id") Long id) {
        return success(dispatchManageService.getReceive(id));
    }

    @PostMapping("/accept")
    @Operation(summary = "接收调令")
    @Parameter(name = "id", description = "调令 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-receive:accept')")
    public CommonResult<Boolean> acceptReceive(@RequestParam("id") Long id) {
        dispatchManageService.acceptReceive(id);
        return success(true);
    }

    @PostMapping("/submit-result")
    @Operation(summary = "提交调令执行结果")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-receive:submit')")
    public CommonResult<Boolean> submitReceiveResult(@Valid @RequestBody IotDispatchReceiveSubmitReqVO reqVO) {
        dispatchManageService.submitReceiveResult(reqVO);
        return success(true);
    }

    @GetMapping("/run-log-options")
    @Operation(summary = "获取运行日志选项")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-receive:query')")
    public CommonResult<List<IotDispatchReceiveRunLogOptionRespVO>> getRunLogOptions(
            @RequestParam(value = "stationId", required = false) String stationId) {
        return success(dispatchManageService.getReceiveRunLogOptions(stationId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出调令接受任务 Excel")
    @PreAuthorize("@ss.hasPermission('iot:dispatch-receive:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReceiveExcel(@Valid IotDispatchReceivePageReqVO reqVO,
                                   HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDispatchReceiveRespVO> list = dispatchManageService.getReceiveList(reqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "调令接受.xls", "调令接受", IotDispatchReceiveExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotDispatchReceiveExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotDispatchReceiveExportExcelVO excelVO = new IotDispatchReceiveExportExcelVO();
            excelVO.setInstructionNo(item.getInstructionNo());
            excelVO.setInstructionName(item.getInstructionName());
            excelVO.setIssueOrgName(item.getIssueOrgName());
            excelVO.setInstructionContent(item.getInstructionContent());
            excelVO.setIssueTime(item.getIssueTime());
            excelVO.setReceiverUserName(item.getReceiverUserName());
            excelVO.setExecutorUserName(item.getExecutorUserName());
            excelVO.setExecutionStatusName(item.getExecutionStatusName());
            excelVO.setFinishTime(item.getFinishTime());
            excelVO.setSubmitUserName(item.getSubmitUserName());
            return excelVO;
        });
        ExcelUtils.write(response, "调令接受.xls", "调令接受", IotDispatchReceiveExportExcelVO.class, excelList);
    }
}
