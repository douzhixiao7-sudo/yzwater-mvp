package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzMaterialMapPointRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxWzWarehouseOptionRespVO;
import com.sydigit.yzwater.module.service.flood.FxWzService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 物资管理
 */
@Tag(name = "管理后台 - 物资管理")
@RestController
@RequestMapping("/fx-wz")
@Validated
public class FxWzController {

    private final FxWzService wzService;

    public FxWzController(FxWzService wzService) {
        this.wzService = wzService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询物资")
    public CommonResult<List<FxWzListRespVO>> getList(@Validated @ModelAttribute FxWzListReqVO reqVO) {
        return success(wzService.getList(reqVO));
    }

    @GetMapping("/warehouse-options")
    @Operation(summary = "查询储备单位下拉选项")
    public CommonResult<List<FxWzWarehouseOptionRespVO>> getWarehouseOptions() {
        return success(wzService.getWarehouseOptions());
    }

    @GetMapping("/material-map-points")
    @Operation(summary = "查询防汛物资地图点位（含仓库地址坐标，供风险隐患点路线关联）")
    public CommonResult<List<FxWzMaterialMapPointRespVO>> getMaterialMapPoints() {
        return success(wzService.getMaterialMapPointList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询物资详情")
    public CommonResult<FxWzSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(wzService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增物资")
    public CommonResult<String> create(@Valid @RequestBody FxWzSaveReqVO reqVO) {
        return success(wzService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑物资")
    public CommonResult<Boolean> update(@Valid @RequestBody FxWzSaveReqVO reqVO) {
        wzService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除物资")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        wzService.delete(id);
        return success(true);
    }
}
