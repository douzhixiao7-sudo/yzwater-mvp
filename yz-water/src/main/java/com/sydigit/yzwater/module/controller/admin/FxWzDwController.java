package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzDwGeomMigrateRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzDwListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzDwSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxWzDwService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 防汛物资单位
 */
@Tag(name = "管理后台 - 防汛物资单位")
@RestController
@RequestMapping("/fx-wz-dw")
@Validated
public class FxWzDwController {

    private final FxWzDwService wzDwService;

    public FxWzDwController(FxWzDwService wzDwService) {
        this.wzDwService = wzDwService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询防汛物资单位")
    public CommonResult<java.util.List<FxWzDwListRespVO>> getList() {
        return success(wzDwService.getList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询防汛物资单位详情")
    public CommonResult<FxWzDwSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(wzDwService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增防汛物资单位")
    public CommonResult<String> create(@Valid @RequestBody FxWzDwSaveReqVO reqVO) {
        return success(wzDwService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑防汛物资单位")
    public CommonResult<Boolean> update(@Valid @RequestBody FxWzDwSaveReqVO reqVO) {
        wzDwService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除防汛物资单位")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        wzDwService.delete(id);
        return success(true);
    }

    @PutMapping("/migrate-geometry")
    @Operation(summary = "将 pos 迁移到 geom（SRID=4490）")
    public CommonResult<FxWzDwGeomMigrateRespVO> migrateGeometry() {
        return success(wzDwService.migratePosToGeom());
    }
}
