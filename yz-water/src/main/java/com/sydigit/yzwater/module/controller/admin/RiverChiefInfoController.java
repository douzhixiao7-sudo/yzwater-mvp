package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefLatestImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoTotalChiefRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoTotalChiefSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefInfoUpdateReqVO;
import com.sydigit.yzwater.module.service.river.RiverChiefInfoExcelImportService;
import com.sydigit.yzwater.module.service.river.RiverChiefLatestExcelImportService;
import com.sydigit.yzwater.module.service.river.RiverChiefLatestV2ExcelImportService;
import com.sydigit.yzwater.module.service.river.RiverChiefInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 河长信息（按河长维度）
 */
@Tag(name = "仪征管理后台 - 河长信息")
@RestController
@RequestMapping("/river/chief-info")
@Validated
public class RiverChiefInfoController {

    private final RiverChiefInfoService riverChiefInfoService;
    private final RiverChiefInfoExcelImportService riverChiefInfoExcelImportService;
    private final RiverChiefLatestExcelImportService riverChiefLatestExcelImportService;
    private final RiverChiefLatestV2ExcelImportService riverChiefLatestV2ExcelImportService;

    public RiverChiefInfoController(RiverChiefInfoService riverChiefInfoService,
                                    RiverChiefInfoExcelImportService riverChiefInfoExcelImportService,
                                    RiverChiefLatestExcelImportService riverChiefLatestExcelImportService,
                                    RiverChiefLatestV2ExcelImportService riverChiefLatestV2ExcelImportService) {
        this.riverChiefInfoService = riverChiefInfoService;
        this.riverChiefInfoExcelImportService = riverChiefInfoExcelImportService;
        this.riverChiefLatestExcelImportService = riverChiefLatestExcelImportService;
        this.riverChiefLatestV2ExcelImportService = riverChiefLatestV2ExcelImportService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询河长信息（按河长维度聚合）")
    public CommonResult<PageResult<RiverChiefInfoPageRespVO>> getPage(@Valid RiverChiefInfoPageReqVO reqVO) {
        return success(riverChiefInfoService.getPage(reqVO));
    }

    @GetMapping("/total-chief/list")
    @Operation(summary = "查询总河长列表（固定展示，不分页）")
    public CommonResult<List<RiverChiefInfoTotalChiefRespVO>> getTotalChiefList() {
        return success(riverChiefInfoService.getTotalChiefList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询河长详情（包含关联设施列表）")
    public CommonResult<RiverChiefInfoDetailRespVO> getDetail(@PathVariable("id") Long id) {
        return success(riverChiefInfoService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增河长（支持关联多个河道/河段/水库）")
    public CommonResult<Long> create(@Valid @RequestBody RiverChiefInfoCreateReqVO reqVO) {
        return success(riverChiefInfoService.create(reqVO));
    }

    @PostMapping("/total-chief")
    @Operation(summary = "新增总河长")
    public CommonResult<Long> createTotalChief(@Valid @RequestBody RiverChiefInfoTotalChiefSaveReqVO reqVO) {
        return success(riverChiefInfoService.createTotalChief(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑河长（支持增删关联设施并同步基础信息）")
    public CommonResult<Boolean> update(@Valid @RequestBody RiverChiefInfoUpdateReqVO reqVO) {
        riverChiefInfoService.update(reqVO);
        return success(true);
    }

    @PutMapping("/total-chief")
    @Operation(summary = "编辑总河长")
    public CommonResult<Boolean> updateTotalChief(@Valid @RequestBody RiverChiefInfoTotalChiefSaveReqVO reqVO) {
        riverChiefInfoService.updateTotalChief(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除河长（按河长维度删除其全部当前关联设施）")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        riverChiefInfoService.delete(id);
        return success(true);
    }

    @DeleteMapping("/total-chief/{id}")
    @Operation(summary = "删除总河长")
    public CommonResult<Boolean> deleteTotalChief(@PathVariable("id") Long id) {
        riverChiefInfoService.deleteTotalChief(id);
        return success(true);
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传 Excel 并批量导入河长信息")
    public CommonResult<RiverChiefInfoImportRespVO> importExcel(@RequestPart("file") MultipartFile file) {
        return success(riverChiefInfoExcelImportService.importExcel(file));
    }

    @PostMapping(value = "/import-latest-head-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传最新河长 Excel 并导入河长信息")
    public CommonResult<RiverChiefLatestImportRespVO> importLatestHeadExcel(@RequestPart("file") MultipartFile file) {
        return success(riverChiefLatestExcelImportService.importLatestExcel(file));
    }

    @PostMapping(value = "/import-latest-head-excel-v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传河长2 Excel 并导入最新河长信息")
    public CommonResult<RiverChiefLatestImportRespVO> importLatestHeadExcelV2(@RequestPart("file") MultipartFile file) {
        return success(riverChiefLatestV2ExcelImportService.importLatestExcel(file));
    }
}
