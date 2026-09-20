package com.sydigit.yzwater.module.iot.controller.admin.statistics;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateSummaryRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringPressValueRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringFlowValueRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringRuntimeStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStationOptionRespVO;
import com.sydigit.yzwater.module.iot.service.statistics.IotScreenStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "大屏统计-工程监测")
@RestController
@RequestMapping("/iot/statistics/screen/engineering-monitor")
@Validated
public class IotScreenEngineeringMonitoringController {

    @Resource
    private IotScreenStatisticsService screenStatisticsService;

    @GetMapping("/station-options")
    @Operation(summary = "获取站点下拉（字典 iot_zd_sbzd，按 label 展示）")
    @PermitAll
    public CommonResult<List<IotScreenStationOptionRespVO>> getStationOptions() {
        return success(screenStatisticsService.getStationOptions());
    }

    @GetMapping("/device-status-summary")
    @Operation(summary = "查询设备总数/关闭总数/运行总数")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @PermitAll
    public CommonResult<IotScreenEngineeringDeviceStatusRespVO> getDeviceStatusSummary(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId) {
        return success(screenStatisticsService.getEngineeringDeviceStatusSummary(stationId));
    }

    @GetMapping("/device-status-list")
    @Operation(summary = "查询设备状态列表（设备类型仅支持7或3）")
    @PermitAll
    public CommonResult<List<IotScreenEngineeringDeviceStatusListRespVO>> getDeviceStatusList(
            @Valid IotScreenEngineeringDeviceStatusListReqVO reqVO) {
        return success(screenStatisticsService.getEngineeringDeviceStatusList(reqVO));
    }

    @GetMapping("/device-runtime-summary")
    @Operation(summary = "查询开机次数/累计运行时间/本次运行时间")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @PermitAll
    public CommonResult<List<IotScreenEngineeringRuntimeStatRespVO>> getDeviceRuntimeSummary(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId) {
        return success(screenStatisticsService.getEngineeringRuntimeStatSummary(stationId));
    }

    @GetMapping("/gate-status-summary")
    @Operation(summary = "查询闸门总数/全关总数/全开总数")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @PermitAll
    public CommonResult<IotScreenEngineeringGateSummaryRespVO> getGateStatusSummary(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId) {
        return success(screenStatisticsService.getEngineeringGateStatusSummary(stationId));
    }

    @GetMapping("/gate-status-list")
    @Operation(summary = "查询闸门状态明细列表")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @PermitAll
    public CommonResult<List<IotScreenEngineeringGateDetailRespVO>> getGateStatusList(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId) {
        return success(screenStatisticsService.getEngineeringGateStatusList(stationId));
    }

    @GetMapping("/device-press-value-list")
    @Operation(summary = "根据站点查询设备名称及压力值（deviceType=9，物模型 pressValue）")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @PermitAll
    public CommonResult<List<IotScreenEngineeringPressValueRespVO>> getDevicePressValueList(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId) {
        return success(screenStatisticsService.getEngineeringPressValueList(stationId));
    }

    @GetMapping("/device-flow-value-list")
    @Operation(summary = "根据站点查询流量计实时值（物模型 flowMeterWaterSpeedValue）")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @PermitAll
    public CommonResult<List<IotScreenEngineeringFlowValueRespVO>> getDeviceFlowValueList(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId) {
        return success(screenStatisticsService.getEngineeringFlowValueList(stationId));
    }
}
