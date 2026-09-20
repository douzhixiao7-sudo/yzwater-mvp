package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirBaseSyncRespVO;
import com.sydigit.yzwater.module.service.geoBase.ReservoirBaseSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - yz_base_reservoir 同步
 */
@Tag(name = "仪征管理后台 - 水库基础数据同步")
@RestController
@RequestMapping("/reservoir/base")
@Validated
public class ReservoirBaseController {

    private final ReservoirBaseSyncService reservoirBaseSyncService;

    public ReservoirBaseController(ReservoirBaseSyncService reservoirBaseSyncService) {
        this.reservoirBaseSyncService = reservoirBaseSyncService;
    }

    @PostMapping("/sync")
    @Operation(summary = "同步yz_base_reservoir到水利基础与水库业务表")
    public CommonResult<ReservoirBaseSyncRespVO> syncBaseReservoir() {
        return success(reservoirBaseSyncService.syncFromBase());
    }
}
