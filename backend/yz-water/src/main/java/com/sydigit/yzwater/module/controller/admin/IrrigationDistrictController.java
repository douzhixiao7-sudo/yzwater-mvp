package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.irrigation.IrrigationDistrictSaveReqVO;
import com.sydigit.yzwater.module.service.irrigation.IrrigationDistrictImportService;
import com.sydigit.yzwater.module.service.irrigation.IrrigationDistrictService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 灌区
 */
@Tag(name = "管理后台 - 灌区")
@RestController
@RequestMapping("/irrigation-district")
@Validated
public class IrrigationDistrictController {

    private final IrrigationDistrictService irrigationDistrictService;
    private final IrrigationDistrictImportService irrigationDistrictImportService;

    public IrrigationDistrictController(IrrigationDistrictService irrigationDistrictService,
                                        IrrigationDistrictImportService irrigationDistrictImportService) {
        this.irrigationDistrictService = irrigationDistrictService;
        this.irrigationDistrictImportService = irrigationDistrictImportService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传灌区 JSON/GeoJSON 并导入（设施基础表 + 灌区表）")
    @PermitAll
    public CommonResult<IrrigationDistrictImportRespVO> importIrrigationDistricts(
            @Parameter(description = "JSON/GeoJSON 文件（UTF-8，无 BOM）", required = true)
            @RequestPart("file") MultipartFile file) {
        return success(irrigationDistrictImportService.importIrrigationDistricts(file));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询灌区")
    public CommonResult<PageResult<IrrigationDistrictPageRespVO>> getPage(@Valid IrrigationDistrictPageReqVO reqVO) {
        return success(irrigationDistrictService.getPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出灌区 Excel")
    public void exportExcel(@Valid IrrigationDistrictPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<IrrigationDistrictExportExcelVO> list = irrigationDistrictService.getExportList(reqVO);
        ExcelUtils.write(response, "灌区.xls", "数据", IrrigationDistrictExportExcelVO.class, list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询灌区详情")
    public CommonResult<IrrigationDistrictSaveReqVO> getDetail(@PathVariable("id") Long id) {
        return success(irrigationDistrictService.getDetail(id));
    }

    @GetMapping("/facility/{facilityId}")
    @Operation(summary = "按设施ID查询灌区详情")
    public CommonResult<IrrigationDistrictSaveReqVO> getDetailByFacility(@PathVariable("facilityId") Long facilityId) {
        return success(irrigationDistrictService.getDetailByFacilityId(facilityId));
    }

    @PostMapping
    @Operation(summary = "新增灌区")
    public CommonResult<Long> create(@Valid @RequestBody IrrigationDistrictSaveReqVO reqVO) {
        return success(irrigationDistrictService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑灌区")
    public CommonResult<Boolean> update(@Valid @RequestBody IrrigationDistrictSaveReqVO reqVO) {
        irrigationDistrictService.update(reqVO);
        return success(true);
    }

    @PutMapping(value = "/geometry/by-name", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "按灌区名称上传 geometry.json 更新 GIS")
    public CommonResult<Boolean> updateGeometryByName(
            @Parameter(description = "灌区名称", required = true)
            @RequestPart("irrigationDistrictName") String irrigationDistrictName,
            @Parameter(description = "geometry.json 文件（仅 geometry 对象）", required = true)
            @RequestPart("file") MultipartFile file) {
        irrigationDistrictService.updateGeometryByDistrictName(irrigationDistrictName, file);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除灌区")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        irrigationDistrictService.delete(id);
        return success(true);
    }
}
