package com.sydigit.yzwater.module.iot.controller.admin.spare;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSpareExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSparePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSpareRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSpareSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSpareSimpleRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import com.sydigit.yzwater.module.iot.service.spare.IotSpareService;
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
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "IoT - 备件台账")
@RestController
@RequestMapping("/iot/spare")
@Validated
public class IotSpareController {

    @Resource
    private IotSpareService spareService;

    @PostMapping("/create")
    @Operation(summary = "创建备件台账")
    @PreAuthorize("@ss.hasPermission('iot:spare:create')")
    public CommonResult<Long> createSpare(@Valid @RequestBody IotSpareSaveReqVO createReqVO) {
        return success(spareService.createSpare(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新备件台账")
    @PreAuthorize("@ss.hasPermission('iot:spare:update')")
    public CommonResult<Boolean> updateSpare(@Valid @RequestBody IotSpareSaveReqVO updateReqVO) {
        spareService.updateSpare(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除备件台账")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:spare:delete')")
    public CommonResult<Boolean> deleteSpare(@RequestParam("id") Long id) {
        spareService.deleteSpare(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取备件台账")
    @Parameter(name = "id", description = "主键", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:spare:query')")
    public CommonResult<IotSpareRespVO> getSpare(@RequestParam("id") Long id) {
        IotSpareDO spare = spareService.getSpare(id);
        return success(convertResp(spare));
    }

    @GetMapping("/page")
    @Operation(summary = "备件台账分页")
    @PreAuthorize("@ss.hasPermission('iot:spare:query')")
    public CommonResult<PageResult<IotSpareRespVO>> getSparePage(@Valid IotSparePageReqVO pageReqVO) {
        PageResult<IotSpareDO> pageResult = spareService.getSparePage(pageReqVO);
        List<IotSpareRespVO> list = convertList(pageResult.getList(), this::convertResp);
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取备件台账精简列表")
    public CommonResult<List<IotSpareSimpleRespVO>> getSpareSimpleList() {
        List<IotSpareDO> list = spareService.getSpareList();
        return success(convertList(list, spare -> {
            IotSpareSimpleRespVO respVO = new IotSpareSimpleRespVO();
            respVO.setId(spare.getId());
            respVO.setSpareName(spare.getSpareName());
            respVO.setSpareSpec(spare.getSpareSpec());
            respVO.setSpareModel(spare.getSpareModel());
            return respVO;
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出备件台账 Excel")
    @PreAuthorize("@ss.hasPermission('iot:spare:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSpareExcel(@Valid IotSparePageReqVO exportReqVO, HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<IotSpareDO> pageResult = spareService.getSparePage(exportReqVO);
        List<IotSpareExportExcelVO> list = convertList(pageResult.getList(), this::convertExport);
        ExcelUtils.write(response, "备件台账.xls", "备件台账", IotSpareExportExcelVO.class, list);
    }

    private IotSpareRespVO convertResp(IotSpareDO spare) {
        if (spare == null) {
            return null;
        }
        IotSpareRespVO respVO = BeanUtils.toBean(spare, IotSpareRespVO.class);
        if (spare.getSpareImages() != null) {
            respVO.setSpareImages(Arrays.asList(spare.getSpareImages()));
        } else {
            respVO.setSpareImages(Collections.emptyList());
        }
        respVO.setWarning(isWarning(spare));
        return respVO;
    }

    private IotSpareExportExcelVO convertExport(IotSpareDO spare) {
        IotSpareExportExcelVO respVO = BeanUtils.toBean(spare, IotSpareExportExcelVO.class);
        if (spare.getSpareImages() != null && spare.getSpareImages().length > 0) {
            respVO.setSpareImages(String.join(",", spare.getSpareImages()));
        }
        return respVO;
    }

    private boolean isWarning(IotSpareDO spare) {
        Integer stock = spare.getStockQty();
        Integer minStock = spare.getMinStock();
        if (stock == null || minStock == null) {
            return false;
        }
        return stock <= minStock;
    }

}
