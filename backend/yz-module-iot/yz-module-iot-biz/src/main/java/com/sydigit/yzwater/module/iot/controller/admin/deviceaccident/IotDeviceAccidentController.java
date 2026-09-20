package com.sydigit.yzwater.module.iot.controller.admin.deviceaccident;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.accident.IotDeviceAccidentDO;
import com.sydigit.yzwater.module.iot.service.deviceaccident.IotDeviceAccidentService;
import com.sydigit.yzwater.module.system.dal.dataobject.user.AdminUserDO;
import com.sydigit.yzwater.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 设备事故")
@RestController
@RequestMapping("/iot/device-accident")
@Validated
public class IotDeviceAccidentController {

    @Resource
    private IotDeviceAccidentService deviceAccidentService;
    @Resource
    private AdminUserService adminUserService;

    @PostMapping("/create")
    @Operation(summary = "创建设备事故")
    @PreAuthorize("@ss.hasPermission('iot:device-accident:create')")
    public CommonResult<Long> createDeviceAccident(@Valid @RequestBody IotDeviceAccidentSaveReqVO createReqVO) {
        return success(deviceAccidentService.createDeviceAccident(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备事故")
    @PreAuthorize("@ss.hasPermission('iot:device-accident:update')")
    public CommonResult<Boolean> updateDeviceAccident(@Valid @RequestBody IotDeviceAccidentSaveReqVO updateReqVO) {
        deviceAccidentService.updateDeviceAccident(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备事故")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device-accident:delete')")
    public CommonResult<Boolean> deleteDeviceAccident(@RequestParam("id") Long id) {
        deviceAccidentService.deleteDeviceAccident(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备事故")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-accident:query')")
    public CommonResult<IotDeviceAccidentRespVO> getDeviceAccident(@RequestParam("id") Long id) {
        IotDeviceAccidentDO accident = deviceAccidentService.getDeviceAccident(id);
        IotDeviceAccidentRespVO respVO = convertResp(accident);
        fillCreatorNames(Collections.singletonList(respVO));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "设备事故分页")
    @PreAuthorize("@ss.hasPermission('iot:device-accident:query')")
    public CommonResult<PageResult<IotDeviceAccidentRespVO>> getDeviceAccidentPage(
            @Valid IotDeviceAccidentPageReqVO pageReqVO) {
        PageResult<IotDeviceAccidentDO> pageResult = deviceAccidentService.getDeviceAccidentPage(pageReqVO);
        List<IotDeviceAccidentRespVO> list = CollectionUtils.convertList(pageResult.getList(), this::convertResp);
        fillCreatorNames(list);
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备事故 Excel")
    @PreAuthorize("@ss.hasPermission('iot:device-accident:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceAccidentExcel(@Valid IotDeviceAccidentPageReqVO exportReqVO,
                                          HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDeviceAccidentDO> list = deviceAccidentService.getDeviceAccidentList(exportReqVO);
        List<IotDeviceAccidentExportExcelVO> excelList = CollectionUtils.convertList(list, this::convertExport);
        ExcelUtils.write(response, "设备事故.xls", "设备事故", IotDeviceAccidentExportExcelVO.class, excelList);
    }

    private IotDeviceAccidentRespVO convertResp(IotDeviceAccidentDO accident) {
        if (accident == null) {
            return null;
        }
        IotDeviceAccidentRespVO respVO = BeanUtils.toBean(accident, IotDeviceAccidentRespVO.class);
        if (accident.getAttachments() != null) {
            respVO.setAttachments(Arrays.asList(accident.getAttachments()));
        } else {
            respVO.setAttachments(Collections.emptyList());
        }
        return respVO;
    }

    private IotDeviceAccidentExportExcelVO convertExport(IotDeviceAccidentDO accident) {
        IotDeviceAccidentExportExcelVO respVO = BeanUtils.toBean(accident, IotDeviceAccidentExportExcelVO.class);
        if (accident.getAttachments() != null && accident.getAttachments().length > 0) {
            respVO.setAttachments(String.join(",", accident.getAttachments()));
        }
        return respVO;
    }

    private void fillCreatorNames(List<IotDeviceAccidentRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> creatorIds = list.stream()
                .map(IotDeviceAccidentRespVO::getCreator)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .map(text -> {
                    try {
                        return Long.parseLong(text);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (creatorIds.isEmpty()) {
            return;
        }
        Map<Long, AdminUserDO> userMap = adminUserService.getUserMap(creatorIds);
        list.forEach(item -> {
            if (item == null) {
                return;
            }
            try {
                Long userId = item.getCreator() != null ? Long.parseLong(item.getCreator()) : null;
                AdminUserDO user = userId != null ? userMap.get(userId) : null;
                if (user != null) {
                    item.setCreatorName(user.getNickname());
                }
            } catch (NumberFormatException ignored) {
                // ignore non-numeric creator
            }
        });
    }
}
