package com.sydigit.yzwater.module.iot.controller.admin.devicedoc;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.doc.IotDeviceDocDO;
import com.sydigit.yzwater.module.iot.service.devicedoc.IotDeviceDocService;
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
import java.util.List;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 设备技术资料库")
@RestController
@RequestMapping("/iot/device-doc")
@Validated
public class IotDeviceDocController {

    @Resource
    private IotDeviceDocService deviceDocService;

    @PostMapping("/create")
    @Operation(summary = "创建技术资料")
    @PreAuthorize("@ss.hasPermission('iot:device-doc:create')")
    public CommonResult<Long> createDeviceDoc(@Valid @RequestBody IotDeviceDocSaveReqVO createReqVO) {
        return success(deviceDocService.createDeviceDoc(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新技术资料")
    @PreAuthorize("@ss.hasPermission('iot:device-doc:update')")
    public CommonResult<Boolean> updateDeviceDoc(@Valid @RequestBody IotDeviceDocSaveReqVO updateReqVO) {
        deviceDocService.updateDeviceDoc(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除技术资料")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device-doc:delete')")
    public CommonResult<Boolean> deleteDeviceDoc(@RequestParam("id") Long id) {
        deviceDocService.deleteDeviceDoc(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得技术资料详情")
    @Parameter(name = "id", description = "主键", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-doc:query')")
    public CommonResult<IotDeviceDocRespVO> getDeviceDoc(@RequestParam("id") Long id) {
        IotDeviceDocDO doc = deviceDocService.getDeviceDoc(id);
        return success(BeanUtils.toBean(doc, IotDeviceDocRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得技术资料分页")
    @PreAuthorize("@ss.hasPermission('iot:device-doc:query')")
    public CommonResult<PageResult<IotDeviceDocRespVO>> getDeviceDocPage(@Valid IotDeviceDocPageReqVO pageReqVO) {
        PageResult<IotDeviceDocDO> pageResult = deviceDocService.getDeviceDocPage(pageReqVO);
        List<IotDeviceDocRespVO> list = convertList(pageResult.getList(),
                item -> BeanUtils.toBean(item, IotDeviceDocRespVO.class));
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出技术资料 Excel")
    @PreAuthorize("@ss.hasPermission('iot:device-doc:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceDocExcel(@Valid IotDeviceDocPageReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<IotDeviceDocDO> pageResult = deviceDocService.getDeviceDocPage(exportReqVO);
        List<IotDeviceDocExportExcelVO> list = convertList(pageResult.getList(),
                item -> BeanUtils.toBean(item, IotDeviceDocExportExcelVO.class));
        ExcelUtils.write(response, "技术资料库.xls", "技术资料", IotDeviceDocExportExcelVO.class, list);
    }
}
