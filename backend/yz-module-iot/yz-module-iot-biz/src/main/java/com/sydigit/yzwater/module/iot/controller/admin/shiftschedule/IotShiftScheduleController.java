package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleCalendarReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleImportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftSchedulePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleShiftOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleTeamOptionRespVO;
import com.sydigit.yzwater.module.iot.service.shiftschedule.IotShiftScheduleService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 员工排班 Controller
 */
@Tag(name = "IoT - 员工排班")
@RestController
@RequestMapping("/iot/shift-schedule")
@Validated
public class IotShiftScheduleController {

    @Resource
    private IotShiftScheduleService shiftScheduleService;

    @PostMapping("/create")
    @Operation(summary = "创建员工排班")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:create')")
    public CommonResult<Long> createShiftSchedule(@Valid @RequestBody IotShiftScheduleSaveReqVO createReqVO) {
        return success(shiftScheduleService.createShiftSchedule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新员工排班")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:update')")
    public CommonResult<Boolean> updateShiftSchedule(@Valid @RequestBody IotShiftScheduleSaveReqVO updateReqVO) {
        shiftScheduleService.updateShiftSchedule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工排班")
    @Parameter(name = "id", description = "员工排班 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:delete')")
    public CommonResult<Boolean> deleteShiftSchedule(@RequestParam("id") Long id) {
        shiftScheduleService.deleteShiftSchedule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取员工排班详情")
    @Parameter(name = "id", description = "员工排班 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:get')")
    public CommonResult<IotShiftScheduleRespVO> getShiftSchedule(@RequestParam("id") Long id) {
        return success(shiftScheduleService.getShiftSchedule(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询员工排班")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:query')")
    public CommonResult<PageResult<IotShiftScheduleRespVO>> getShiftSchedulePage(@Valid IotShiftSchedulePageReqVO pageReqVO) {
        return success(shiftScheduleService.getShiftSchedulePage(pageReqVO));
    }

    @GetMapping("/calendar")
    @Operation(summary = "获取员工排班月历数据")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:query')")
    public CommonResult<List<IotShiftScheduleRespVO>> getShiftScheduleCalendar(@Valid IotShiftScheduleCalendarReqVO reqVO) {
        return success(shiftScheduleService.getShiftScheduleCalendar(reqVO));
    }

    @GetMapping("/shift-options")
    @Operation(summary = "获取班次选项")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:query')")
    public CommonResult<List<IotShiftScheduleShiftOptionRespVO>> getShiftOptions(
            @RequestParam(value = "stationId", required = false) String stationId) {
        return success(shiftScheduleService.getShiftOptions(stationId));
    }

    @GetMapping("/team-options")
    @Operation(summary = "获取班组选项")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:query')")
    public CommonResult<List<IotShiftScheduleTeamOptionRespVO>> getTeamOptions(
            @RequestParam(value = "stationId", required = false) String stationId) {
        return success(shiftScheduleService.getTeamOptions(stationId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出员工排班 Excel")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShiftScheduleExcel(@Valid IotShiftSchedulePageReqVO exportReqVO,
                                         HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotShiftScheduleRespVO> list = shiftScheduleService.getShiftScheduleList(exportReqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "员工排班表.xls", "员工排班", IotShiftScheduleExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotShiftScheduleExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotShiftScheduleExportExcelVO excelVO = new IotShiftScheduleExportExcelVO();
            excelVO.setScheduleNo(item.getScheduleNo());
            excelVO.setScheduleDate(item.getScheduleDate());
            excelVO.setShiftName(item.getShiftName());
            excelVO.setTeamName(item.getTeamName());
            excelVO.setDutyUserName(item.getDutyUserName());
            excelVO.setDutyMobile(item.getDutyMobile());
            excelVO.setDutyPostName(item.getDutyPostName());
            excelVO.setDutyStartTime(item.getDutyStartTime());
            excelVO.setDutyEndTime(item.getDutyEndTime());
            excelVO.setDutyLog(item.getDutyLog());
            excelVO.setStatusName(resolveStatusName(item.getStatus()));
            excelVO.setRemark(item.getRemark());
            excelVO.setCreator(item.getCreator());
            excelVO.setCreateTime(item.getCreateTime());
            return excelVO;
        });
        ExcelUtils.write(response, "员工排班表.xls", "员工排班", IotShiftScheduleExportExcelVO.class, excelList);
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入员工排班 Excel")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:import')")
    public CommonResult<IotShiftScheduleImportRespVO> importShiftSchedule(@RequestParam("file") MultipartFile file)
            throws Exception {
        List<IotShiftScheduleImportExcelVO> list = ExcelUtils.read(file, IotShiftScheduleImportExcelVO.class);
        return success(shiftScheduleService.importShiftSchedule(list));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载员工排班导入模板")
    @PreAuthorize("@ss.hasPermission('iot:shift-schedule:import-template')")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        List<IotShiftScheduleImportExcelVO> list = Arrays.asList(createDemoRow());
        ExcelUtils.write(response, "员工排班导入模板.xls", "员工排班", IotShiftScheduleImportExcelVO.class, list);
    }

    private String resolveStatusName(Integer status) {
        if (status == null) {
            return "待值班";
        }
        return switch (status) {
            case 1 -> "值班中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "待值班";
        };
    }

    private IotShiftScheduleImportExcelVO createDemoRow() {
        IotShiftScheduleImportExcelVO row = new IotShiftScheduleImportExcelVO();
        row.setScheduleDate(LocalDate.now());
        row.setShiftId(1L);
        row.setTeamId(1L);
        row.setDutyUserId(1L);
        row.setDutyLog("示例：已完成值班巡检与运行参数记录");
        row.setRemark("示例数据");
        return row;
    }
}
