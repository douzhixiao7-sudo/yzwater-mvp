package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxYaglListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxYaglSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxYaglService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 预案管理
 */
@Tag(name = "管理后台 - 预案管理")
@RestController
@RequestMapping("/fx-yagl")
@Validated
public class FxYaglController {

    private final FxYaglService yaglService;

    public FxYaglController(FxYaglService yaglService) {
        this.yaglService = yaglService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询预案")
    public CommonResult<List<FxYaglListRespVO>> getList() {
        return success(yaglService.getList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询预案详情")
    public CommonResult<FxYaglSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(yaglService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增预案")
    public CommonResult<String> create(@Valid @RequestBody FxYaglSaveReqVO reqVO) {
        return success(yaglService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑预案")
    public CommonResult<Boolean> update(@Valid @RequestBody FxYaglSaveReqVO reqVO) {
        yaglService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预案")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        yaglService.delete(id);
        return success(true);
    }
}
