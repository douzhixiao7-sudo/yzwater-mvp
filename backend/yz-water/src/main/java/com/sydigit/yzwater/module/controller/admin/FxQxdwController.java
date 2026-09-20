package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxQxdwListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxQxdwSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxQxdwService;
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
 * 管理后台 - 市级防汛抢险队伍
 */
@Tag(name = "管理后台 - 市级防汛抢险队伍")
@RestController
@RequestMapping("/fx-qxdw")
@Validated
public class FxQxdwController {

    /** 抢险队伍导出表头（显式指定，不使用注解 VO，避免误带「队伍名称」列） */
    private static final List<List<String>> QXDW_EXPORT_HEAD = List.of(
            List.of("单位"),
            List.of("人数"),
            List.of("联系人"),
            List.of("联系电话"),
            List.of("备注"));

    private final FxQxdwService qxdwService;

    public FxQxdwController(FxQxdwService qxdwService) {
        this.qxdwService = qxdwService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询市级防汛抢险队伍")
    public CommonResult<List<FxQxdwListRespVO>> getList() {
        return success(qxdwService.getList());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出市级防汛抢险队伍 Excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<List<Object>> data = qxdwService.getExportExcelDataRows();
        ExcelUtils.writeWithHeadList(response, "市级防汛抢险队伍计划表.xls", "数据", QXDW_EXPORT_HEAD, data);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询市级防汛抢险队伍详情")
    public CommonResult<FxQxdwSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(qxdwService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增市级防汛抢险队伍")
    public CommonResult<String> create(@Valid @RequestBody FxQxdwSaveReqVO reqVO) {
        return success(qxdwService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑市级防汛抢险队伍")
    public CommonResult<Boolean> update(@Valid @RequestBody FxQxdwSaveReqVO reqVO) {
        qxdwService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除市级防汛抢险队伍")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        qxdwService.delete(id);
        return success(true);
    }
}
