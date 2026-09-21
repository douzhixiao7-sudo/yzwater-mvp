package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberExportReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbMemberSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxZhbMemberService;
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
 * 管理后台 - 防汛指挥部成员
 */
@Tag(name = "管理后台 - 防汛指挥部成员")
@RestController
@RequestMapping("/fx-zhb-member")
@Validated
public class FxZhbMemberController {

    private final FxZhbMemberService memberService;

    public FxZhbMemberController(FxZhbMemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询防汛指挥部成员")
    public CommonResult<PageResult<FxZhbMemberPageRespVO>> getPage(@Valid FxZhbMemberPageReqVO reqVO) {
        return success(memberService.getPage(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出防汛指挥部成员 Excel")
    public void exportExcel(@Valid FxZhbMemberExportReqVO reqVO, HttpServletResponse response) throws IOException {
        List<FxZhbMemberExportExcelVO> list = memberService.getExportList(reqVO);
        ExcelUtils.write(response, "防汛指挥部成员.xls", "数据", FxZhbMemberExportExcelVO.class, list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询防汛指挥部成员详情")
    public CommonResult<FxZhbMemberSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(memberService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增防汛指挥部成员")
    public CommonResult<String> create(@Valid @RequestBody FxZhbMemberSaveReqVO reqVO) {
        return success(memberService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑防汛指挥部成员")
    public CommonResult<Boolean> update(@Valid @RequestBody FxZhbMemberSaveReqVO reqVO) {
        memberService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除防汛指挥部成员")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        memberService.delete(id);
        return success(true);
    }
}
