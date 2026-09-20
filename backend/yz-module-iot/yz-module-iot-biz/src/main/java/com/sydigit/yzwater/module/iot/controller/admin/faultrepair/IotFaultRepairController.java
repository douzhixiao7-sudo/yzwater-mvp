package com.sydigit.yzwater.module.iot.controller.admin.faultrepair;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairAuditAssignReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairResultReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairSpareUsageVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair.IotFaultRepairDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair.IotFaultRepairSpareUsageDO;
import com.sydigit.yzwater.module.iot.service.faultrepair.IotFaultRepairService;
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

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "IoT - 故障维修")
@RestController
@RequestMapping("/iot/fault-repair")
@Validated
public class IotFaultRepairController {

    @Resource
    private IotFaultRepairService faultRepairService;

    @PostMapping("/create")
    @Operation(summary = "创建故障维修工单")
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:create')")
    public CommonResult<Long> createFaultRepair(@Valid @RequestBody IotFaultRepairSaveReqVO createReqVO) {
        return success(faultRepairService.createFaultRepair(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新故障维修工单")
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:update')")
    public CommonResult<Boolean> updateFaultRepair(@Valid @RequestBody IotFaultRepairSaveReqVO updateReqVO) {
        faultRepairService.updateFaultRepair(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除故障维修工单")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:delete')")
    public CommonResult<Boolean> deleteFaultRepair(@RequestParam("id") Long id) {
        faultRepairService.deleteFaultRepair(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取故障维修工单")
    @Parameter(name = "id", description = "主键", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:query')")
    public CommonResult<IotFaultRepairRespVO> getFaultRepair(@RequestParam("id") Long id) {
        IotFaultRepairDO faultRepair = faultRepairService.getFaultRepair(id);
        return success(convertResp(faultRepair));
    }

    @GetMapping("/page")
    @Operation(summary = "故障维修工单分页")
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:query')")
    public CommonResult<PageResult<IotFaultRepairRespVO>> getFaultRepairPage(@Valid IotFaultRepairPageReqVO pageReqVO) {
        PageResult<IotFaultRepairDO> pageResult = faultRepairService.getFaultRepairPage(pageReqVO);
        List<IotFaultRepairRespVO> list = CollectionUtils.convertList(pageResult.getList(), this::convertResp);
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出故障维修工单 Excel")
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFaultRepairExcel(@Valid IotFaultRepairPageReqVO exportReqVO, HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotFaultRepairDO> list = faultRepairService.getFaultRepairList(exportReqVO);
        List<IotFaultRepairExportExcelVO> excelList = CollectionUtils.convertList(list, this::convertExport);
        ExcelUtils.write(response, "故障维修工单.xls", "故障维修工单", IotFaultRepairExportExcelVO.class, excelList);
    }

    @PostMapping("/audit-assign")
    @Operation(summary = "审核派工")
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:audit')")
    public CommonResult<Boolean> auditAssign(@Valid @RequestBody IotFaultRepairAuditAssignReqVO reqVO) {
        faultRepairService.auditAssign(reqVO);
        return success(true);
    }

    @PostMapping("/submit-result")
    @Operation(summary = "反馈维修结果")
    @PreAuthorize("@ss.hasPermission('iot:fault-repair:feedback')")
    public CommonResult<Boolean> submitResult(@Valid @RequestBody IotFaultRepairResultReqVO reqVO) {
        faultRepairService.submitResult(reqVO);
        return success(true);
    }

    private IotFaultRepairRespVO convertResp(IotFaultRepairDO faultRepair) {
        if (faultRepair == null) {
            return null;
        }
        IotFaultRepairRespVO respVO = BeanUtils.toBean(faultRepair, IotFaultRepairRespVO.class);
        if (faultRepair.getFaultImages() != null) {
            respVO.setFaultImages(Arrays.asList(faultRepair.getFaultImages()));
        } else {
            respVO.setFaultImages(Collections.emptyList());
        }
        respVO.setSpareUsages(convertSpareUsages(faultRepair.getSpareUsages()));
        return respVO;
    }

    private IotFaultRepairExportExcelVO convertExport(IotFaultRepairDO faultRepair) {
        IotFaultRepairExportExcelVO respVO = BeanUtils.toBean(faultRepair, IotFaultRepairExportExcelVO.class);
        if (faultRepair.getFaultImages() != null && faultRepair.getFaultImages().length > 0) {
            respVO.setFaultImages(String.join(",", faultRepair.getFaultImages()));
        }
        return respVO;
    }

    private List<IotFaultRepairSpareUsageVO> convertSpareUsages(List<IotFaultRepairSpareUsageDO> spareUsages) {
        if (spareUsages == null || spareUsages.isEmpty()) {
            return Collections.emptyList();
        }
        return CollectionUtils.convertList(spareUsages, item -> BeanUtils.toBean(item, IotFaultRepairSpareUsageVO.class));
    }
}
