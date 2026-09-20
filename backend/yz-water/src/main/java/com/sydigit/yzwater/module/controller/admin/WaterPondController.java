package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondFilterOptionsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondStatsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityGeometryUpdateReqVO;
import com.sydigit.yzwater.module.service.pond.WaterPondImportService;
import com.sydigit.yzwater.module.service.pond.WaterPondService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 坑塘
 */
@Tag(name = "管理后台 - 坑塘")
@RestController
@RequestMapping("/water-pond")
@Validated
public class WaterPondController {

    private final WaterPondService waterPondService;
    private final WaterPondImportService waterPondImportService;

    public WaterPondController(WaterPondService waterPondService,
                               WaterPondImportService waterPondImportService) {
        this.waterPondService = waterPondService;
        this.waterPondImportService = waterPondImportService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传坑塘 GeoJSON 并导入（按 CHBH upsert：已存在则覆盖属性与几何，不存在则新增）")
    @PermitAll
    public CommonResult<WaterPondImportRespVO> importGeoJson(
            @Parameter(description = "GeoJSON/JSON 文件（UTF-8）", required = true)
            @RequestPart("file") MultipartFile file) {
        return success(waterPondImportService.importGeoJson(file));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询坑塘")
    public CommonResult<PageResult<WaterPondPageRespVO>> getPage(@Valid WaterPondPageReqVO reqVO) {
        return success(waterPondService.getPage(reqVO));
    }

    @GetMapping("/filter-options")
    @Operation(summary = "查询坑塘筛选项（库内去重）")
    public CommonResult<WaterPondFilterOptionsRespVO> getFilterOptions() {
        return success(waterPondService.getFilterOptions());
    }

    @GetMapping("/stats")
    @Operation(summary = "坑塘统计总览（卡片 + 图表，与列表共用筛选条件）")
    public CommonResult<WaterPondStatsRespVO> getStats(WaterPondPageReqVO reqVO) {
        return success(waterPondService.getStats(reqVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询坑塘详情（含几何 GeoJSON）")
    public CommonResult<WaterPondSaveReqVO> getDetail(@PathVariable("id") Long id) {
        return success(waterPondService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增坑塘（设施基础表 + 坑塘表）")
    public CommonResult<Long> create(@Valid @RequestBody WaterPondSaveReqVO reqVO) {
        return success(waterPondService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑坑塘属性")
    public CommonResult<Boolean> update(@Valid @RequestBody WaterPondSaveReqVO reqVO) {
        waterPondService.update(reqVO);
        return success(true);
    }

    @PutMapping("/{id}/geometry")
    @Operation(summary = "更新坑塘面几何（坑塘表 4326 + 基础表 4490）")
    public CommonResult<Boolean> updateGeometry(@PathVariable("id") Long id,
                                                @RequestBody WaterFacilityGeometryUpdateReqVO reqVO) {
        waterPondService.updateGeometry(id, reqVO == null ? null : reqVO.getGeometryGeoJson());
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除坑塘（逻辑删除，同步基础表）")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        waterPondService.delete(id);
        return success(true);
    }
}
