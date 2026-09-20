package com.sydigit.yzwater.module.iot.controller.admin.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.imports.IotRealtimeDataImportRespVO;
import com.sydigit.yzwater.module.iot.job.realtimedata.IotRealtimeDataPullJob;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotRealtimeDataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 实时数据")
@RestController
@RequestMapping("/iot/realtime-data")
@Validated
public class IotRealtimeDataController {

    @Resource
    private IotRealtimeDataPullJob realtimeDataPullJob;
    @Resource
    private IotRealtimeDataImportService importService;

    @PostMapping("/pull")
    @Operation(summary = "手动触发实时数据拉取")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data:pull')")
    public CommonResult<Boolean> pull() throws Exception {
        realtimeDataPullJob.execute(null);
        return success(true);
    }

    @PostMapping("/import-config")
    @Operation(summary = "导入实时数据配置（从配置文件）")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data:import')")
    public CommonResult<IotRealtimeDataImportRespVO> importConfig(
            @RequestParam(value = "name", required = false) String name) {
        return success(importService.importFromProperties(name));
    }

}
