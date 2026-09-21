package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.flood.WarningBroadcastStationListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.WarningBroadcastStationListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.WarningBroadcastStationSaveReqVO;
import com.sydigit.yzwater.module.service.flood.WarningBroadcastStationService;
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
 * 管理后台 - 预警广播站
 */
@Tag(name = "管理后台 - 预警广播站")
@RestController
@RequestMapping("/warning-broadcast-station")
@Validated
public class WarningBroadcastStationController {

    private final WarningBroadcastStationService warningBroadcastStationService;

    public WarningBroadcastStationController(WarningBroadcastStationService warningBroadcastStationService) {
        this.warningBroadcastStationService = warningBroadcastStationService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询预警广播站")
    public CommonResult<List<WarningBroadcastStationListRespVO>> getList(WarningBroadcastStationListReqVO reqVO) {
        return success(warningBroadcastStationService.getList(reqVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询预警广播站详情")
    public CommonResult<WarningBroadcastStationSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(warningBroadcastStationService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增预警广播站")
    public CommonResult<String> create(@Valid @RequestBody WarningBroadcastStationSaveReqVO reqVO) {
        return success(warningBroadcastStationService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑预警广播站")
    public CommonResult<Boolean> update(@Valid @RequestBody WarningBroadcastStationSaveReqVO reqVO) {
        warningBroadcastStationService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除预警广播站")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        warningBroadcastStationService.delete(id);
        return success(true);
    }
}

