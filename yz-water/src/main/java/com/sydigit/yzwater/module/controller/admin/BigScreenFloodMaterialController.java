package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictLabelValueRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialRescuePlanRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialTypeItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialWarehouseItemRespVO;
import com.sydigit.yzwater.module.service.screen.BigScreenFloodMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 大屏统计 - 防汛物资
 */
@Tag(name = "大屏统计-防汛物资")
@RestController
@RequestMapping("/screen/flood-material")
@Validated
public class BigScreenFloodMaterialController {

    private final BigScreenFloodMaterialService floodMaterialService;

    public BigScreenFloodMaterialController(BigScreenFloodMaterialService floodMaterialService) {
        this.floodMaterialService = floodMaterialService;
    }

    @GetMapping("/warehouse-options")
    @Operation(summary = "大屏统计 - 防汛物资储备单位下拉")
    @PermitAll
    public CommonResult<List<BigScreenDictLabelValueRespVO>> getWarehouseOptions() {
        return success(floodMaterialService.getWarehouseOptions());
    }

    @GetMapping("/by-warehouse")
    @Operation(summary = "大屏统计 - 按储备单位或防汛物资 ID 查询物资（不传 warehouseId 默认全部）")
    @PermitAll
    public CommonResult<List<BigScreenFloodMaterialWarehouseItemRespVO>> getMaterialListByWarehouse(
            @RequestParam(value = "warehouseId", required = false) String warehouseId) {
        return success(floodMaterialService.getMaterialListByWarehouse(warehouseId));
    }

    @GetMapping("/by-material-type")
    @Operation(summary = "大屏统计 - 查询全部防汛物资（返回格式保持按物资类型接口兼容）")
    @PermitAll
    public CommonResult<List<BigScreenFloodMaterialTypeItemRespVO>> getMaterialListByType() {
        return success(floodMaterialService.getMaterialListByType());
    }

    @GetMapping("/rescue-plan")
    @Operation(summary = "大屏统计 - 抢险队伍计划（单位、人数）")
    @PermitAll
    public CommonResult<List<BigScreenFloodMaterialRescuePlanRespVO>> getRescuePlanList() {
        return success(floodMaterialService.getRescuePlanList());
    }
}
