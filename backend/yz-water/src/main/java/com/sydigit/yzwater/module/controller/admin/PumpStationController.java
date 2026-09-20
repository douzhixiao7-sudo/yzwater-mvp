package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pump.PumpStationSaveReqVO;
import com.sydigit.yzwater.module.service.pump.PumpStationImportService;
import com.sydigit.yzwater.module.service.pump.PumpStationService;
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
 * 管理后台 - 泵站管理
 */
@Tag(name = "管理后台 - 泵站管理")
@RestController
@RequestMapping("/pump-station")
@Validated
public class PumpStationController {

    private final PumpStationImportService pumpStationImportService;
    private final PumpStationService pumpStationService;

    public PumpStationController(PumpStationImportService pumpStationImportService,
                                 PumpStationService pumpStationService) {
        this.pumpStationImportService = pumpStationImportService;
        this.pumpStationService = pumpStationService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传泵站 GeoJSON/JSON 并入库（基础表 + 泵站表）")
    @PermitAll
    public CommonResult<PumpStationImportRespVO> importPumpStation(
            @Parameter(description = "GeoJSON/JSON 文件（UTF-8，无 BOM）", required = true)
            @RequestPart("file") MultipartFile file) {
        return success(pumpStationImportService.importPumpStations(file));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询泵站")
    public CommonResult<PageResult<PumpStationPageRespVO>> getPage(@Valid PumpStationPageReqVO reqVO) {
        return success(pumpStationService.getPumpStationPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出泵站 Excel")
    public void exportExcel(@Valid PumpStationPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<PumpStationExportExcelVO> list = pumpStationService.getPumpStationExportList(reqVO);
        ExcelUtils.write(response, "泵站信息.xls", "数据", PumpStationExportExcelVO.class, list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询泵站详情")
    public CommonResult<PumpStationSaveReqVO> getDetail(@PathVariable("id") Long id) {
        return success(pumpStationService.getPumpStationDetail(id));
    }

    @GetMapping("/facility/{facilityId}")
    @Operation(summary = "根据设施ID查询泵站详情")
    public CommonResult<PumpStationSaveReqVO> getDetailByFacility(@PathVariable("facilityId") Long facilityId) {
        return success(pumpStationService.getPumpStationDetailByFacilityId(facilityId));
    }

    @PostMapping
    @Operation(summary = "新增泵站")
    public CommonResult<Long> create(@Valid @RequestBody PumpStationSaveReqVO reqVO) {
        return success(pumpStationService.createPumpStation(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑泵站")
    public CommonResult<Boolean> update(@Valid @RequestBody PumpStationSaveReqVO reqVO) {
        pumpStationService.updatePumpStation(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除泵站")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        pumpStationService.deletePumpStation(id);
        return success(true);
    }
}
