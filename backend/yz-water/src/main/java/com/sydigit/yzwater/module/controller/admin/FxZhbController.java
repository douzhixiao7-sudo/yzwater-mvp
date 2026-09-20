package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxZhbService;
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
 * 管理后台 - 防汛指挥部
 */
@Tag(name = "管理后台 - 防汛指挥部")
@RestController
@RequestMapping("/fx-zhb")
@Validated
public class FxZhbController {

    private final FxZhbService fxZhbService;

    public FxZhbController(FxZhbService fxZhbService) {
        this.fxZhbService = fxZhbService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询防汛指挥部")
    public CommonResult<PageResult<FxZhbPageRespVO>> getPage(@Valid FxZhbPageReqVO reqVO) {
        return success(fxZhbService.getPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出防汛指挥部 Excel")
    public void exportExcel(@Valid FxZhbPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<FxZhbExportExcelVO> list = fxZhbService.getExportList(reqVO);
        ExcelUtils.write(response, "防汛指挥部.xls", "数据", FxZhbExportExcelVO.class, list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询防汛指挥部详情")
    public CommonResult<FxZhbDetailRespVO> getDetail(@PathVariable("id") String id) {
        return success(fxZhbService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增防汛指挥部")
    public CommonResult<String> create(@Valid @RequestBody FxZhbSaveReqVO reqVO) {
        return success(fxZhbService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑防汛指挥部")
    public CommonResult<Boolean> update(@Valid @RequestBody FxZhbSaveReqVO reqVO) {
        fxZhbService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除防汛指挥部")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        fxZhbService.delete(id);
        return success(true);
    }
}
