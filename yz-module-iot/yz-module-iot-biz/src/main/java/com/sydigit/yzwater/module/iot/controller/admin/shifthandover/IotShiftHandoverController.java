package com.sydigit.yzwater.module.iot.controller.admin.shifthandover;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverDefaultRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverReminderRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverSaveReqVO;
import com.sydigit.yzwater.module.iot.service.shifthandover.IotShiftHandoverService;
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
 * 交接班 Controller
 */
@Tag(name = "IoT - 交接班管理")
@RestController
@RequestMapping("/iot/shift-handover")
@Validated
public class IotShiftHandoverController {

    @Resource
    private IotShiftHandoverService shiftHandoverService;

    @PostMapping("/create")
    @Operation(summary = "创建交接班记录")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:create')")
    public CommonResult<Long> createShiftHandover(@Valid @RequestBody IotShiftHandoverSaveReqVO createReqVO) {
        return success(shiftHandoverService.createShiftHandover(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新交接班记录")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:update')")
    public CommonResult<Boolean> updateShiftHandover(@Valid @RequestBody IotShiftHandoverSaveReqVO updateReqVO) {
        shiftHandoverService.updateShiftHandover(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除交接班记录")
    @Parameter(name = "id", description = "交接班 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:delete')")
    public CommonResult<Boolean> deleteShiftHandover(@RequestParam("id") Long id) {
        shiftHandoverService.deleteShiftHandover(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取交接班详情")
    @Parameter(name = "id", description = "交接班 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:get')")
    public CommonResult<IotShiftHandoverRespVO> getShiftHandover(@RequestParam("id") Long id) {
        return success(shiftHandoverService.getShiftHandover(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询交接班记录")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:query')")
    public CommonResult<PageResult<IotShiftHandoverRespVO>> getShiftHandoverPage(@Valid IotShiftHandoverPageReqVO pageReqVO) {
        return success(shiftHandoverService.getShiftHandoverPage(pageReqVO));
    }

    @GetMapping("/defaults")
    @Operation(summary = "获取交接班默认信息")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:query')")
    public CommonResult<IotShiftHandoverDefaultRespVO> getShiftHandoverDefault() {
        return success(shiftHandoverService.getShiftHandoverDefault());
    }

    @GetMapping("/reminder")
    @Operation(summary = "获取交接班提醒")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:query')")
    public CommonResult<IotShiftHandoverReminderRespVO> getShiftHandoverReminder() {
        return success(shiftHandoverService.getShiftHandoverReminder());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出交接班记录 Excel")
    @PreAuthorize("@ss.hasPermission('iot:shift-handover:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShiftHandoverExcel(@Valid IotShiftHandoverPageReqVO exportReqVO,
                                         HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotShiftHandoverRespVO> list = shiftHandoverService.getShiftHandoverList(exportReqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "交接班记录.xls", "交接班记录", IotShiftHandoverExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotShiftHandoverExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotShiftHandoverExportExcelVO excelVO = new IotShiftHandoverExportExcelVO();
            excelVO.setHandoverNo(item.getHandoverNo());
            excelVO.setHandoverTime(item.getHandoverTime());
            excelVO.setShiftName(item.getShiftName());
            excelVO.setTeamName(item.getTeamName());
            excelVO.setHandoverUserName(item.getHandoverUserName());
            excelVO.setTakeoverUserName(item.getTakeoverUserName());
            excelVO.setDutyLog(item.getDutyLog());
            excelVO.setPendingItems(item.getPendingItems());
            excelVO.setDispatchInstructionNo(item.getDispatchInstructionNo());
            excelVO.setDispatchInstructionName(item.getDispatchInstructionName());
            excelVO.setStatusName(resolveStatusName(item.getStatus()));
            excelVO.setRemark(item.getRemark());
            excelVO.setCreator(item.getCreator());
            excelVO.setCreateTime(item.getCreateTime());
            return excelVO;
        });
        ExcelUtils.write(response, "交接班记录.xls", "交接班记录", IotShiftHandoverExportExcelVO.class, excelList);
    }

    private String resolveStatusName(Integer status) {
        if (status == null) {
            return "待交接";
        }
        return status == 1 ? "已交接" : "待交接";
    }
}

