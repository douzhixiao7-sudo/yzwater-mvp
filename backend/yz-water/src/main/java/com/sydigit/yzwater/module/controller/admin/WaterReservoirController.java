package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirImportExcelTemplateVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.ReservoirImportRespVO;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirHeadBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirChiefOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirHeadItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirManagementResponsibilitiesSyncReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirTownshipNormalizeRespVO;
import com.sydigit.yzwater.module.service.geoBase.ReservoirImportService;
import com.sydigit.yzwater.module.service.reservoir.ReservoirExcelImportService;
import com.sydigit.yzwater.module.service.reservoir.WaterReservoirService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 水库管理
 */
@Tag(name = "仪征管理后台 - 水库管理")
@RestController
@RequestMapping("/reservoir")
@Validated
public class WaterReservoirController {

    private final ReservoirImportService reservoirImportService;
    private final ReservoirExcelImportService reservoirExcelImportService;
    private final WaterReservoirService waterReservoirService;

    public WaterReservoirController(ReservoirImportService reservoirImportService,
                                    ReservoirExcelImportService reservoirExcelImportService,
                                    WaterReservoirService waterReservoirService) {
        this.reservoirImportService = reservoirImportService;
        this.reservoirExcelImportService = reservoirExcelImportService;
        this.waterReservoirService = waterReservoirService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询水库")
    public CommonResult<PageResult<ReservoirPageRespVO>> getPage(@Valid ReservoirPageReqVO reqVO) {
        return success(waterReservoirService.getReservoirPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出水库 Excel")
    public void exportExcel(@Valid ReservoirPageReqVO reqVO,
                            HttpServletResponse response) throws IOException {
        List<ReservoirExportExcelVO> list = waterReservoirService.getReservoirExportList(reqVO);
        ExcelUtils.write(response, "水库信息.xls", "数据", ReservoirExportExcelVO.class, list);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "查询全部水库（简要信息）")
    public CommonResult<List<ReservoirSimpleRespVO>> getSimpleList() {
        return success(waterReservoirService.getReservoirSimpleList());
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载水库导入模板")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "水库导入模板.xls", "数据", ReservoirImportExcelTemplateVO.class, Collections.emptyList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询水库详情")
    @PermitAll
    public CommonResult<ReservoirSaveReqVO> getDetail(@PathVariable("id") Long id) {
        return success(waterReservoirService.getReservoirDetail(id));
    }

    @GetMapping("/facility/{facilityId}")
    @Operation(summary = "按设施ID查询水库详情")
    public CommonResult<ReservoirSaveReqVO> getDetailByFacility(@PathVariable("facilityId") Long facilityId) {
        return success(waterReservoirService.getReservoirDetailByFacilityId(facilityId));
    }

    @PostMapping
    @Operation(summary = "新增水库")
    public CommonResult<Long> create(@Valid @RequestBody ReservoirSaveReqVO reqVO) {
        // 新增水库时，同时写入水利对象基础表与水库表，保持数据一致性
        return success(waterReservoirService.createReservoir(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑水库")
    public CommonResult<Boolean> update(@Valid @RequestBody ReservoirSaveReqVO reqVO) {
        waterReservoirService.updateReservoir(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除水库")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        waterReservoirService.deleteReservoir(id);
        return success(true);
    }

    @GetMapping("/{id}/management")
    @Operation(summary = "查询水库河长信息")
    @PermitAll
    public CommonResult<List<ReservoirHeadItemRespVO>> getManagement(@PathVariable("id") Long id) {
        return success(waterReservoirService.getReservoirManagement(id));
    }

    @GetMapping("/{id}/chief-overview")
    @Operation(summary = "查看水库页当前有效河长概览")
    @PermitAll
    public CommonResult<ReservoirChiefOverviewRespVO> getChiefOverview(@PathVariable("id") Long id) {
        return success(waterReservoirService.getReservoirChiefOverview(id));
    }

    @PostMapping("/management/save")
    @Operation(summary = "保存水库河长信息（覆盖式）")
    public CommonResult<Boolean> saveManagement(@Valid @RequestBody ReservoirHeadBatchSaveReqVO reqVO) {
        throw ServiceExceptionUtil.invalidParamException("河长信息已统一至【河长管理】页面维护，请勿在水库页面保存");
    }

    @PutMapping("/{id}/management/responsibilities")
    @Operation(summary = "同步河长职责到河长表")
    public CommonResult<Boolean> syncManagementResponsibilities(@PathVariable("id") Long id,
                                                                @Valid @RequestBody ReservoirManagementResponsibilitiesSyncReqVO reqVO) {
        waterReservoirService.syncReservoirManagementResponsibilities(id, reqVO.getResponsibilities());
        return success(true);
    }

    @GetMapping("/check-code")
    @Operation(summary = "校验水库编码是否已存在")
    public CommonResult<Boolean> checkReservoirCode(@RequestParam("reservoirCode") String reservoirCode,
                                                    @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return success(waterReservoirService.isReservoirCodeExists(reservoirCode, excludeId));
    }

    @PostMapping("/import")
    @Operation(summary = "上传“仪征水库导入.xlsx”并导入水库数据")
    @PermitAll
    public CommonResult<ReservoirImportRespVO> importReservoir(@RequestPart("file") MultipartFile file) {
        return success(reservoirImportService.importExcel(file));
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传 Excel 并导入水库数据")
    public CommonResult<ReservoirImportRespVO> importReservoirExcel(@RequestPart("file") MultipartFile file) {
        return success(reservoirExcelImportService.importExcel(file));
    }

    @PostMapping("/sync-center-point")
    @Operation(summary = "根据设施 GIS 计算并回写水库中心点经纬度（免登录）")
    @PermitAll
    public CommonResult<Integer> syncCenterPoint() {
        return success(waterReservoirService.syncLongitudeLatitudeByFacilityGeom());
    }

    @PostMapping("/township/normalize")
    @Operation(summary = "规范化水库乡镇字段（中文名转区划编码）")
    public CommonResult<ReservoirTownshipNormalizeRespVO> normalizeTownship() {
        return success(waterReservoirService.normalizeReservoirTownship());
    }
}
