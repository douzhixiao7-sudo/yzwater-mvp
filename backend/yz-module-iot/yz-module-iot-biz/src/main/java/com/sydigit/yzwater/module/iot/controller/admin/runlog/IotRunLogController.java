package com.sydigit.yzwater.module.iot.controller.admin.runlog;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogDefaultRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogDispatchOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogSaveReqVO;
import com.sydigit.yzwater.module.iot.service.runlog.IotRunLogService;
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
 * 运行日志 Controller
 */
@Tag(name = "IoT - 运行日志")
@RestController
@RequestMapping("/iot/run-log")
@Validated
public class IotRunLogController {

    @Resource
    private IotRunLogService runLogService;

    @PostMapping("/create")
    @Operation(summary = "创建运行日志")
    @PreAuthorize("@ss.hasPermission('iot:run-log:create')")
    public CommonResult<Long> createRunLog(@Valid @RequestBody IotRunLogSaveReqVO createReqVO) {
        return success(runLogService.createRunLog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新运行日志")
    @PreAuthorize("@ss.hasPermission('iot:run-log:update')")
    public CommonResult<Boolean> updateRunLog(@Valid @RequestBody IotRunLogSaveReqVO updateReqVO) {
        runLogService.updateRunLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除运行日志")
    @Parameter(name = "id", description = "记录 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:run-log:delete')")
    public CommonResult<Boolean> deleteRunLog(@RequestParam("id") Long id) {
        runLogService.deleteRunLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取运行日志详情")
    @Parameter(name = "id", description = "记录 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasAnyPermissions('iot:run-log:get', 'iot:run-log:query', 'iot:dispatch-receive:query')")
    public CommonResult<IotRunLogRespVO> getRunLog(@RequestParam("id") Long id) {
        return success(runLogService.getRunLog(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询运行日志")
    @PreAuthorize("@ss.hasPermission('iot:run-log:query')")
    public CommonResult<PageResult<IotRunLogRespVO>> getRunLogPage(@Valid IotRunLogPageReqVO pageReqVO) {
        return success(runLogService.getRunLogPage(pageReqVO));
    }

    @GetMapping("/defaults")
    @Operation(summary = "获取运行日志默认信息")
    @PreAuthorize("@ss.hasPermission('iot:run-log:query')")
    public CommonResult<IotRunLogDefaultRespVO> getRunLogDefaults() {
        return success(runLogService.getRunLogDefaults());
    }

    @GetMapping("/dispatch-options")
    @Operation(summary = "获取可关联调令选项")
    @PreAuthorize("@ss.hasPermission('iot:run-log:query')")
    public CommonResult<List<IotRunLogDispatchOptionRespVO>> getDispatchOptions(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "stationId", required = false) String stationId) {
        return success(runLogService.getDispatchOptions(keyword, stationId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出运行日志 Excel")
    @PreAuthorize("@ss.hasPermission('iot:run-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRunLogExcel(@Valid IotRunLogPageReqVO exportReqVO,
                                  HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotRunLogRespVO> list = runLogService.getRunLogList(exportReqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "运行日志.xls", "运行日志", IotRunLogExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotRunLogExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotRunLogExportExcelVO excelVO = new IotRunLogExportExcelVO();
            excelVO.setLogNo(item.getLogNo());
            excelVO.setTaskName(item.getTaskName());
            excelVO.setDutyTeamName(item.getDutyTeamName());
            excelVO.setRecorderUserName(item.getRecorderUserName());
            excelVO.setRecordTime(item.getRecordTime());
            excelVO.setRunStartTime(item.getRunStartTime());
            excelVO.setRunEndTime(item.getRunEndTime());
            excelVO.setCheckPeriod(item.getCheckPeriod());
            excelVO.setDeviceName(item.getDeviceName());
            excelVO.setDispatchInstructionNo(item.getDispatchInstructionNo());
            excelVO.setRemark(item.getRemark());
            return excelVO;
        });
        ExcelUtils.write(response, "运行日志.xls", "运行日志", IotRunLogExportExcelVO.class, excelList);
    }
}
