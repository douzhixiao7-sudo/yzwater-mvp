package com.sydigit.yzwater.module.iot.controller.admin.spare;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoAuditReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareIoDO;
import com.sydigit.yzwater.module.iot.service.spare.IotSpareIoService;
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
import java.util.Map;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertMap;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "IoT - 备件出入库")
@RestController
@RequestMapping("/iot/spare-io")
@Validated
public class IotSpareIoController {

    @Resource
    private IotSpareIoService spareIoService;
    @Resource
    private IotSpareService spareService;

    @PostMapping("/create")
    @Operation(summary = "创建出入库记录")
    @PreAuthorize("@ss.hasPermission('iot:spare-io:create')")
    public CommonResult<Long> createSpareIo(@Valid @RequestBody IotSpareIoSaveReqVO createReqVO) {
        return success(spareIoService.createSpareIo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出入库记录")
    @PreAuthorize("@ss.hasPermission('iot:spare-io:update')")
    public CommonResult<Boolean> updateSpareIo(@Valid @RequestBody IotSpareIoSaveReqVO updateReqVO) {
        spareIoService.updateSpareIo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出入库记录")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:spare-io:delete')")
    public CommonResult<Boolean> deleteSpareIo(@RequestParam("id") Long id) {
        spareIoService.deleteSpareIo(id);
        return success(true);
    }

    @PutMapping("/audit")
    @Operation(summary = "审批出入库记录")
    @PreAuthorize("@ss.hasPermission('iot:spare-io:audit')")
    public CommonResult<Boolean> auditSpareIo(@Valid @RequestBody IotSpareIoAuditReqVO auditReqVO) {
        spareIoService.auditSpareIo(auditReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取出入库记录")
    @Parameter(name = "id", description = "主键", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:spare-io:query')")
    public CommonResult<IotSpareIoRespVO> getSpareIo(@RequestParam("id") Long id) {
        IotSpareIoDO spareIo = spareIoService.getSpareIo(id);
        return success(convertResp(spareIo));
    }

    @GetMapping("/page")
    @Operation(summary = "出入库记录分页")
    @PreAuthorize("@ss.hasPermission('iot:spare-io:query')")
    public CommonResult<PageResult<IotSpareIoRespVO>> getSpareIoPage(@Valid IotSpareIoPageReqVO pageReqVO) {
        PageResult<IotSpareIoDO> pageResult = spareIoService.getSpareIoPage(pageReqVO);
        Map<Long, IotSpareDO> spareMap = getSpareMap(pageResult.getList());
        List<IotSpareIoRespVO> list = convertList(pageResult.getList(), io -> convertResp(io, spareMap));
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出入库记录 Excel")
    @PreAuthorize("@ss.hasPermission('iot:spare-io:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSpareIoExcel(@Valid IotSpareIoPageReqVO exportReqVO, HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<IotSpareIoDO> pageResult = spareIoService.getSpareIoPage(exportReqVO);
        Map<Long, IotSpareDO> spareMap = getSpareMap(pageResult.getList());
        List<IotSpareIoExportExcelVO> list = convertList(pageResult.getList(), io -> convertExport(io, spareMap));
        ExcelUtils.write(response, "备件出入库记录.xls", "出入库记录", IotSpareIoExportExcelVO.class, list);
    }

    private Map<Long, IotSpareDO> getSpareMap(List<IotSpareIoDO> list) {
        return convertMap(spareService.getSpareListByIds(convertSet(list, IotSpareIoDO::getSpareId)), IotSpareDO::getId);
    }

    private IotSpareIoRespVO convertResp(IotSpareIoDO spareIo) {
        if (spareIo == null) {
            return null;
        }
        IotSpareIoRespVO respVO = BeanUtils.toBean(spareIo, IotSpareIoRespVO.class);
        IotSpareDO spare = spareService.getSpareBasic(spareIo.getSpareId());
        if (spare != null) {
            respVO.setSpareName(spare.getSpareName());
            respVO.setSpareSpec(spare.getSpareSpec());
            respVO.setSpareModel(spare.getSpareModel());
            respVO.setStorageLocation(spare.getStorageLocation());
            respVO.setMinStock(spare.getMinStock());
            if (spare.getSpareImages() != null) {
                respVO.setSpareImages(Arrays.asList(spare.getSpareImages()));
            } else {
                respVO.setSpareImages(Collections.emptyList());
            }
        }
        return respVO;
    }

    private IotSpareIoRespVO convertResp(IotSpareIoDO spareIo, Map<Long, IotSpareDO> spareMap) {
        IotSpareIoRespVO respVO = BeanUtils.toBean(spareIo, IotSpareIoRespVO.class);
        IotSpareDO spare = spareMap.get(spareIo.getSpareId());
        if (spare != null) {
            respVO.setSpareName(spare.getSpareName());
            respVO.setSpareSpec(spare.getSpareSpec());
            respVO.setSpareModel(spare.getSpareModel());
            respVO.setStorageLocation(spare.getStorageLocation());
            respVO.setMinStock(spare.getMinStock());
            if (spare.getSpareImages() != null) {
                respVO.setSpareImages(Arrays.asList(spare.getSpareImages()));
            } else {
                respVO.setSpareImages(Collections.emptyList());
            }
        }
        return respVO;
    }

    private IotSpareIoExportExcelVO convertExport(IotSpareIoDO spareIo, Map<Long, IotSpareDO> spareMap) {
        IotSpareIoExportExcelVO respVO = BeanUtils.toBean(spareIo, IotSpareIoExportExcelVO.class);
        IotSpareDO spare = spareMap.get(spareIo.getSpareId());
        if (spare != null) {
            respVO.setSpareName(spare.getSpareName());
            respVO.setSpareSpec(spare.getSpareSpec());
            respVO.setSpareModel(spare.getSpareModel());
        }
        return respVO;
    }

}
