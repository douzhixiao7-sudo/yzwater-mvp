package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelManagementResponsibilitiesSyncReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverManagementSectionDetailVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionWithChannelSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityGeometryUpdateReqVO;
import com.sydigit.yzwater.module.service.river.RiverChannelBfExcelImportService;
import com.sydigit.yzwater.module.service.river.RiverChannelBfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 大屏-河道管理
 */
@Tag(name = "仪征管理后台 - 大屏-河道管理")
@RestController
@RequestMapping("/river/channel-bf")
@Validated
public class RiverChannelBfController {

    private final RiverChannelBfService riverChannelService;
    private final DictDataCommonApi dictDataApi;
    private final RiverChannelBfExcelImportService riverChannelExcelImportService;

    public RiverChannelBfController(RiverChannelBfService riverChannelService,
                                    DictDataCommonApi dictDataApi,
                                    RiverChannelBfExcelImportService riverChannelExcelImportService) {
        this.riverChannelService = riverChannelService;
        this.dictDataApi = dictDataApi;
        this.riverChannelExcelImportService = riverChannelExcelImportService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询河道")
    public CommonResult<PageResult<RiverChannelPageRespVO>> getPage(@Valid RiverChannelPageReqVO reqVO) {
        return success(riverChannelService.getRiverChannelPage(reqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "查询全部河道（简要信息）")
    public CommonResult<List<RiverChannelSimpleRespVO>> getSimpleList() {
        return success(riverChannelService.getRiverChannelSimpleList());
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载河道导入模板")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "河道导入模板.xls", "数据", RiverChannelImportExcelVO.class, Collections.emptyList());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出河道 Excel")
    public void exportExcel(@Valid RiverChannelPageReqVO reqVO,
                            HttpServletResponse response) throws IOException {
        List<RiverChannelExportExcelVO> list = riverChannelService.getRiverChannelExportList(reqVO);
        ExcelUtils.write(response, "河道信息.xls", "数据", RiverChannelExportExcelVO.class, list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询河道详情")
    public CommonResult<RiverChannelDetailRespVO> getDetail(@PathVariable("id") Long id) {
        return success(riverChannelService.getRiverChannelDetail(id));
    }

    @GetMapping("/facility/{facilityId}")
    @Operation(summary = "按设施ID查询河道详情")
    public CommonResult<RiverChannelDetailRespVO> getDetailByFacility(@PathVariable("facilityId") Long facilityId) {
        return success(riverChannelService.getRiverChannelDetailByFacilityId(facilityId));
    }

    @GetMapping("/section/facility/{facilityId}")
    @Operation(summary = "按设施ID查询河段详情")
    public CommonResult<RiverSectionDetailRespVO> getSectionDetailByFacility(@PathVariable("facilityId") Long facilityId) {
        return success(riverChannelService.getRiverSectionDetailByFacilityId(facilityId));
    }

    @PostMapping
    @Operation(summary = "新增河道")
    public CommonResult<Long> create(@Valid @RequestBody RiverChannelSaveReqVO reqVO) {
        return success(riverChannelService.createRiverChannel(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑河道")
    public CommonResult<Boolean> update(@Valid @RequestBody RiverChannelSaveReqVO reqVO) {
        riverChannelService.updateRiverChannel(reqVO);
        return success(true);
    }

    @PutMapping("/{id}/geometry")
    @Operation(summary = "更新大屏河道 GIS 位置")
    public CommonResult<Long> updateGeometry(@PathVariable("id") Long id,
                                             @RequestBody WaterFacilityGeometryUpdateReqVO reqVO) {
        return success(riverChannelService.updateRiverChannelGeometry(id, reqVO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除河道")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        riverChannelService.deleteRiverChannel(id);
        return success(true);
    }

    @GetMapping("/{id}/sections")
    @Operation(summary = "查询河段列表（简要）")
    public CommonResult<List<RiverSectionSimpleRespVO>> getSections(@PathVariable("id") Long id) {
        return success(riverChannelService.getSectionsByChannelId(id));
    }

    @GetMapping("/section-simple-list")
    @Operation(summary = "查询全部河段（简要，包含所属河道名称）")
    public CommonResult<List<RiverSectionWithChannelSimpleRespVO>> getSectionSimpleList() {
        return success(riverChannelService.getSectionWithChannelSimpleList());
    }

    @GetMapping("/{id}/sections/detail")
    @Operation(summary = "查询河段详情列表")
    public CommonResult<List<RiverSectionDetailRespVO>> getSectionDetails(@PathVariable("id") Long id) {
        return success(riverChannelService.getSectionDetailsByChannelId(id));
    }

    @GetMapping("/{id}/management")
    @Operation(summary = "查询河长与监督详情")
    public CommonResult<List<RiverManagementSectionDetailVO>> getManagement(@PathVariable("id") Long id) {
        return success(riverChannelService.getRiverManagement(id));
    }

    @GetMapping("/{id}/chief-overview")
    @Operation(summary = "查询河道当前有效河长概览")
    public CommonResult<RiverChiefOverviewRespVO> getChiefOverview(@PathVariable("id") Long id) {
        return success(riverChannelService.getRiverChiefOverview(id));
    }

    @PostMapping("/management/save")
    @Operation(summary = "保存河长及监督信息（覆盖式）")
    public CommonResult<Boolean> saveManagement(@Valid @RequestBody RiverHeadBatchSaveReqVO reqVO) {
        riverChannelService.saveRiverManagement(reqVO);
        return success(true);
    }

    @PutMapping("/{id}/management/responsibilities")
    @Operation(summary = "同步河长职责到河长表")
    public CommonResult<Boolean> syncManagementResponsibilities(@PathVariable("id") Long id,
                                                                @Valid @RequestBody RiverChannelManagementResponsibilitiesSyncReqVO reqVO) {
        riverChannelService.syncRiverManagementResponsibilities(id, reqVO.getResponsibilities());
        return success(true);
    }

    @GetMapping("/dict")
    @Operation(summary = "查询字典数据")
    public CommonResult<List<DictDataRespDTO>> getDict(@RequestParam("dictType") String dictType) {
        return success(dictDataApi.getDictDataList(dictType));
    }

    @GetMapping("/check-code")
    @Operation(summary = "校验河道编码是否已存在")
    public CommonResult<Boolean> checkRiverCode(@RequestParam("riverCode") String riverCode,
                                                @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return success(riverChannelService.isRiverCodeExists(riverCode, excludeId));
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传 Excel 并导入河道信息")
    public CommonResult<RiverChannelImportRespVO> importRiverChannelExcel(@RequestPart("file") MultipartFile file) {
        return success(riverChannelExcelImportService.importExcel(file));
    }
}
