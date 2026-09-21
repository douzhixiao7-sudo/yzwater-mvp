package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrCityExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrParkExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxZrrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 防汛责任人
 */
@Tag(name = "管理后台 - 防汛责任人")
@RestController
@RequestMapping("/fx-zrr")
@Validated
public class FxZrrController {

    private final FxZrrService zrrService;

    public FxZrrController(FxZrrService zrrService) {
        this.zrrService = zrrService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询防汛责任人（按类型）")
    public CommonResult<List<FxZrrListRespVO>> getList(@RequestParam("type") @NotBlank String type) {
        return success(zrrService.getList(type));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询防汛责任人详情")
    public CommonResult<FxZrrSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(zrrService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增防汛责任人")
    public CommonResult<String> create(@Valid @RequestBody FxZrrSaveReqVO reqVO) {
        return success(zrrService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑防汛责任人")
    public CommonResult<Boolean> update(@Valid @RequestBody FxZrrSaveReqVO reqVO) {
        zrrService.update(reqVO);
        return success(true);
    }

    @PostMapping("/batch-save")
    @Operation(summary = "批量保存防汛责任人")
    public CommonResult<Boolean> batchSave(@Valid @RequestBody FxZrrBatchSaveReqVO reqVO) {
        zrrService.batchSave(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除防汛责任人")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        zrrService.delete(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出防汛责任人 Excel")
    public void exportExcel(@RequestParam("type") @NotBlank String type, HttpServletResponse response) throws IOException {
        List<FxZrrListRespVO> list = zrrService.getList(type);
        if ("1".equals(type)) {
            List<FxZrrCityExportExcelVO> excelList = list.stream().map(this::buildCityExport).toList();
            ExcelUtils.write(response, "市级防汛抗旱责任人名单.xls", "数据", FxZrrCityExportExcelVO.class, excelList);
            return;
        }
        Map<String, String> divisionNameMap = zrrService.loadDivisionNameMap(list);
        List<FxZrrParkExportExcelVO> excelList = list.stream()
                .map(item -> buildParkExport(item, divisionNameMap))
                .toList();
        ExcelUtils.write(response, "园镇防汛抗旱责任人名单.xls", "数据", FxZrrParkExportExcelVO.class, excelList);
    }

    private FxZrrCityExportExcelVO buildCityExport(FxZrrListRespVO item) {
        FxZrrCityExportExcelVO vo = new FxZrrCityExportExcelVO();
        vo.setAdministrativeName(item.getAdministrativeName());
        vo.setAdministrativeTitle(item.getAdministrativeTitle());
        vo.setTechnicalName(item.getTechnicalName());
        vo.setTechnicalTitle(item.getTechnicalTitle());
        return vo;
    }

    private FxZrrParkExportExcelVO buildParkExport(FxZrrListRespVO item, java.util.Map<String, String> divisionNameMap) {
        FxZrrParkExportExcelVO vo = new FxZrrParkExportExcelVO();
        String code = item.getDivisionCode();
        String name = divisionNameMap == null ? code : divisionNameMap.getOrDefault(code, code);
        vo.setDivisionName(name);
        vo.setAdministrativeName(item.getAdministrativeName());
        vo.setAdministrativeTitle(item.getAdministrativeTitle());
        vo.setTechnicalName(item.getTechnicalName());
        vo.setTechnicalTitle(item.getTechnicalTitle());
        return vo;
    }
}
