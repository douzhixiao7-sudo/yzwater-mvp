package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.river.MainRiverImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelGeoJsonImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelHdmcGeoJsonImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelNameGeoJsonUpdateRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelONameGeoJsonImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelLevelFixRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelQrRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelQrSnapshotVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelAreaExcelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelYzwaterExcelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadExcelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelManagementResponsibilitiesSyncReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverManagementSectionDetailVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverSectionWithChannelSimpleRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSimpleRespVO;
import com.sydigit.yzwater.module.service.river.MainRiverImportService;
import com.sydigit.yzwater.module.service.river.RiverChannelGeoJsonImportService;
import com.sydigit.yzwater.module.service.river.RiverChannelHdmcGeoJsonImportService;
import com.sydigit.yzwater.module.service.river.RiverChannelNameGeoJsonUpdateService;
import com.sydigit.yzwater.module.service.river.RiverChannelONameGeoJsonImportService;
import com.sydigit.yzwater.module.service.river.RiverChannelService;
import com.sydigit.yzwater.module.service.river.RiverChannelExcelImportService;
import com.sydigit.yzwater.module.service.river.RiverChannelAreaExcelImportService;
import com.sydigit.yzwater.module.service.river.RiverChannelYzwaterExcelImportService;
import com.sydigit.yzwater.module.service.river.RiverHeadExcelImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
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

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 河道管理
 */
@Tag(name = "仪征管理后台 - 河道管理")
@RestController
@RequestMapping("/river/channel")
@Validated
public class RiverChannelController {

    private final RiverChannelService riverChannelService;
    private final DictDataCommonApi dictDataApi;
    private final MainRiverImportService mainRiverImportService;
    private final RiverChannelGeoJsonImportService riverChannelGeoJsonImportService;
    private final RiverChannelHdmcGeoJsonImportService riverChannelHdmcGeoJsonImportService;
    private final RiverChannelNameGeoJsonUpdateService riverChannelNameGeoJsonUpdateService;
    private final RiverChannelONameGeoJsonImportService riverChannelONameGeoJsonImportService;
    private final RiverHeadExcelImportService riverHeadExcelImportService;
    private final RiverChannelAreaExcelImportService riverChannelAreaExcelImportService;
    private final RiverChannelExcelImportService riverChannelExcelImportService;
    private final RiverChannelYzwaterExcelImportService riverChannelYzwaterExcelImportService;

    public RiverChannelController(RiverChannelService riverChannelService,
                                  DictDataCommonApi dictDataApi,
                                  MainRiverImportService mainRiverImportService,
                                  RiverChannelGeoJsonImportService riverChannelGeoJsonImportService,
                                  RiverChannelHdmcGeoJsonImportService riverChannelHdmcGeoJsonImportService,
                                  RiverChannelNameGeoJsonUpdateService riverChannelNameGeoJsonUpdateService,
                                  RiverChannelONameGeoJsonImportService riverChannelONameGeoJsonImportService,
                                  RiverHeadExcelImportService riverHeadExcelImportService,
                                  RiverChannelAreaExcelImportService riverChannelAreaExcelImportService,
                                  RiverChannelExcelImportService riverChannelExcelImportService,
                                  RiverChannelYzwaterExcelImportService riverChannelYzwaterExcelImportService) {
        this.riverChannelService = riverChannelService;
        this.dictDataApi = dictDataApi;
        this.mainRiverImportService = mainRiverImportService;
        this.riverChannelGeoJsonImportService = riverChannelGeoJsonImportService;
        this.riverChannelHdmcGeoJsonImportService = riverChannelHdmcGeoJsonImportService;
        this.riverChannelNameGeoJsonUpdateService = riverChannelNameGeoJsonUpdateService;
        this.riverChannelONameGeoJsonImportService = riverChannelONameGeoJsonImportService;
        this.riverHeadExcelImportService = riverHeadExcelImportService;
        this.riverChannelAreaExcelImportService = riverChannelAreaExcelImportService;
        this.riverChannelExcelImportService = riverChannelExcelImportService;
        this.riverChannelYzwaterExcelImportService = riverChannelYzwaterExcelImportService;
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

 /*   @GetMapping("/{id}/qrcode")
    @Operation(summary = "生成河道信息二维码")
    @PermitAll
    public CommonResult<RiverChannelQrRespVO> generateQrcode(@PathVariable("id") Long id) {
        return success(riverChannelService.generateRiverChannelQrcode(id));
    }*/

   /* @GetMapping("/by-code/{riverCode}")
    @Operation(summary = "根据河道编码获取二维码快照数据")
    @PermitAll
    public CommonResult<RiverChannelQrSnapshotVO> getQrSnapshotByCode(@PathVariable("riverCode") String riverCode) {
        return success(riverChannelService.getRiverChannelQrSnapshotByCode(riverCode));
    }

    @GetMapping("/qrcode")
    @Operation(summary = "生成河道固定二维码（基于河道编码）")
    @PermitAll
    public CommonResult<RiverChannelQrRespVO> generateQrcodeByCode(@RequestParam("riverCode") String riverCode) {
        return success(riverChannelService.generateRiverChannelQrcodeByCode(riverCode));
    }*/

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
    @Operation(summary = "查询全部河段（简要，包含所属河道名称；仅返回河段数不为 0 的河道下河段）")
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
    @Operation(summary = "查询河道当前有效河长概览（只读）")
    public CommonResult<RiverChiefOverviewRespVO> getChiefOverview(@PathVariable("id") Long id) {
        return success(riverChannelService.getRiverChiefOverview(id));
    }

    @PostMapping("/management/save")
    @Operation(summary = "保存河长及监督信息（覆盖式）")
    public CommonResult<Boolean> saveManagement(@Valid @RequestBody RiverHeadBatchSaveReqVO reqVO) {
        if (containsHeadData(reqVO)) {
            throw ServiceExceptionUtil.invalidParamException("河长信息已统一至【河长管理】页面维护，请勿在河道页面保存");
        }
        riverChannelService.saveRiverManagement(reqVO);
        return success(true);
    }

    private boolean containsHeadData(RiverHeadBatchSaveReqVO reqVO) {
        if (reqVO == null || reqVO.getSections() == null || reqVO.getSections().isEmpty()) {
            return false;
        }
        return reqVO.getSections().stream()
                .filter(Objects::nonNull)
                .anyMatch(section -> section.getHeads() != null && !section.getHeads().isEmpty());
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

    @PostMapping("/import-main")
    @Operation(summary = "上传省级河道 GeoJSON 并入库（河道+河段+基础表）")
    public CommonResult<MainRiverImportRespVO> importMainRiver(@RequestPart("file") MultipartFile file) {
        return success(mainRiverImportService.importMainRiverGeoJson(file));
    }

    @PostMapping("/import-geojson")
    @Operation(summary = "上传河道 GeoJSON 并入库（基础表+河道+河长，按 doc/xuqiu.md 字段映射）")
    public CommonResult<RiverChannelGeoJsonImportRespVO> importRiverGeoJson(@RequestPart("file") MultipartFile file) {
        return success(riverChannelGeoJsonImportService.importRiverChannelGeoJson(file));
    }

    @PostMapping("/import-geojson-oname")
    @Operation(summary = "上传河道 GeoJSON 并入库（基础表+河道，按 properties.O_Name；存在同名河道则更新几何）")
    public CommonResult<RiverChannelONameGeoJsonImportRespVO> importRiverGeoJsonByOName(@RequestPart("file") MultipartFile file) {
        return success(riverChannelONameGeoJsonImportService.importRiverGeoJsonByOName(file));
    }

    @PostMapping(value = "/public-import-geojson-hdmc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "免登录上传 .geojson，按 hdmc 更新河道 geom，不存在则新增并映射字段")
    @PermitAll
    public CommonResult<RiverChannelHdmcGeoJsonImportRespVO> importRiverGeoJsonByHdmc(@RequestPart("file") MultipartFile file) {
        return success(riverChannelHdmcGeoJsonImportService.importRiverGeoJsonByHdmc(file));
    }

    @PostMapping(value = "/public-update-geojson-name", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "免登录上传 .geojson，按 properties.name 更新河道 geom（同名要素先聚合）")
    @PermitAll
    public CommonResult<RiverChannelNameGeoJsonUpdateRespVO> importRiverGeoJsonByName(@RequestPart("file") MultipartFile file) {
        return success(riverChannelNameGeoJsonUpdateService.importRiverGeoJsonByName(file));
    }

    @PostMapping("/import-head-excel")
    @Operation(summary = "上传“河长信息.xls”并导入河长信息（仅读取 sheet1）")
    public CommonResult<RiverHeadExcelImportRespVO> importRiverHeadExcel(@RequestPart("file") MultipartFile file) {
        return success(riverHeadExcelImportService.importExcel(file));
    }

    @PostMapping("/import-area-excel")
    @Operation(summary = "上传“vvv1.xlsx”并导入河道流经地区/乡镇（仅读取 sheet1）")
    public CommonResult<RiverChannelAreaExcelImportRespVO> importRiverAreaExcel(@RequestPart("file") MultipartFile file) {
        return success(riverChannelAreaExcelImportService.importExcel(file));
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传 Excel 并导入河道信息（默认不划分河段）")
    public CommonResult<RiverChannelImportRespVO> importRiverChannelExcel(@RequestPart("file") MultipartFile file) {
        return success(riverChannelExcelImportService.importExcel(file));
    }

    @PostMapping("/import-yzwater-excel")
    @Operation(summary = "上传 yzwater.xlsx 并按规则更新河道信息（仅更新，不新增）")
    public CommonResult<RiverChannelYzwaterExcelImportRespVO> importYzwaterExcel(@RequestPart("file") MultipartFile file) {
        return success(riverChannelYzwaterExcelImportService.importExcel(file));
    }

    @PostMapping("/fix-river-level")
    @Operation(summary = "修复河道级别字段取值（xcjhd→7j，xjhl→6j）")
    public CommonResult<RiverChannelLevelFixRespVO> fixRiverLevel() {
        return success(riverChannelService.fixRiverLevelValues());
    }
}
