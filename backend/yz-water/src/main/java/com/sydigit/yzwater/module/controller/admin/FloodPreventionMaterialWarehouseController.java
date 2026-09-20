package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehouseExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehouseMapPointRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehousePageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehousePageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FloodPreventionMaterialWarehouseSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FloodPreventionMaterialWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 防汛物资仓库
 */
@Tag(name = "管理后台 - 防汛物资仓库")
@RestController
@RequestMapping("/flood-prevention-material")
@Validated
public class FloodPreventionMaterialWarehouseController {

    private final FloodPreventionMaterialWarehouseService warehouseService;

    public FloodPreventionMaterialWarehouseController(FloodPreventionMaterialWarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询防汛物资仓库")
    public CommonResult<PageResult<FloodPreventionMaterialWarehousePageRespVO>> getPage(@Valid FloodPreventionMaterialWarehousePageReqVO reqVO) {
        return success(warehouseService.getPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出防汛物资仓库 Excel")
    public void exportExcel(@Valid FloodPreventionMaterialWarehousePageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<FloodPreventionMaterialWarehouseExportExcelVO> list = warehouseService.getExportList(reqVO);
        ExcelUtils.write(response, "防汛物资仓库.xls", "数据", FloodPreventionMaterialWarehouseExportExcelVO.class, list);
    }

    @GetMapping("/warehouse-map-points")
    @Operation(summary = "查询防汛物资仓库地图点位（含坐标）")
    public CommonResult<List<FloodPreventionMaterialWarehouseMapPointRespVO>> getMapPoints() {
        return success(warehouseService.getMapPointList());
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "查询防汛物资仓库详情")
    public CommonResult<FloodPreventionMaterialWarehouseSaveReqVO> getDetail(@PathVariable("id") Long id) {
        return success(warehouseService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增防汛物资仓库")
    public CommonResult<Long> create(@Valid @RequestBody FloodPreventionMaterialWarehouseSaveReqVO reqVO) {
        return success(warehouseService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑防汛物资仓库")
    public CommonResult<Boolean> update(@Valid @RequestBody FloodPreventionMaterialWarehouseSaveReqVO reqVO) {
        warehouseService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "删除防汛物资仓库")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        warehouseService.delete(id);
        return success(true);
    }
}

