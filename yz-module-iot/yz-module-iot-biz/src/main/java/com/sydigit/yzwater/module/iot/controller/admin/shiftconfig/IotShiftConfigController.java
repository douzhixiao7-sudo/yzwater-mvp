package com.sydigit.yzwater.module.iot.controller.admin.shiftconfig;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigSaveReqVO;
import com.sydigit.yzwater.module.iot.service.shiftconfig.IotShiftConfigService;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 班次配置 Controller
 */
@Tag(name = "IoT - 班次配置")
@RestController
@RequestMapping("/iot/shift-config")
@Validated
public class IotShiftConfigController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private IotShiftConfigService shiftConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建班次")
    @PreAuthorize("@ss.hasPermission('iot:shift-config:create')")
    public CommonResult<Long> createShiftConfig(@Valid @RequestBody IotShiftConfigSaveReqVO createReqVO) {
        return success(shiftConfigService.createShiftConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新班次")
    @PreAuthorize("@ss.hasPermission('iot:shift-config:update')")
    public CommonResult<Boolean> updateShiftConfig(@Valid @RequestBody IotShiftConfigSaveReqVO updateReqVO) {
        shiftConfigService.updateShiftConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除班次")
    @Parameter(name = "id", description = "班次 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-config:delete')")
    public CommonResult<Boolean> deleteShiftConfig(@RequestParam("id") Long id) {
        shiftConfigService.deleteShiftConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取班次详情")
    @Parameter(name = "id", description = "班次 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-config:get')")
    public CommonResult<IotShiftConfigRespVO> getShiftConfig(@RequestParam("id") Long id) {
        return success(shiftConfigService.getShiftConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询班次")
    @PreAuthorize("@ss.hasPermission('iot:shift-config:query')")
    public CommonResult<PageResult<IotShiftConfigRespVO>> getShiftConfigPage(@Valid IotShiftConfigPageReqVO pageReqVO) {
        return success(shiftConfigService.getShiftConfigPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出班次 Excel")
    @PreAuthorize("@ss.hasPermission('iot:shift-config:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShiftConfigExcel(@Valid IotShiftConfigPageReqVO exportReqVO,
                                       HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotShiftConfigRespVO> list = shiftConfigService.getShiftConfigList(exportReqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "班次管理.xls", "班次管理", IotShiftConfigExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotShiftConfigExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotShiftConfigExportExcelVO excelVO = new IotShiftConfigExportExcelVO();
            excelVO.setShiftNo(item.getShiftNo());
            excelVO.setShiftName(item.getShiftName());
            excelVO.setStartTime(formatTime(item.getStartTime()));
            excelVO.setEndTime(formatTime(item.getEndTime()));
            excelVO.setCrossDay(Boolean.TRUE.equals(item.getCrossDay()) ? "是" : "否");
            excelVO.setRemark(item.getRemark());
            excelVO.setCreator(item.getCreator());
            excelVO.setCreateTime(item.getCreateTime());
            return excelVO;
        });
        ExcelUtils.write(response, "班次管理.xls", "班次管理", IotShiftConfigExportExcelVO.class, excelList);
    }

    private String formatTime(LocalTime time) {
        if (Objects.isNull(time)) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }
}
