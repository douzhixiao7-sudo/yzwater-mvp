package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.weather.WeatherBeginTimeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.weather.WeatherCollectReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.weather.WeatherFyCollectReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.weather.WeatherStationCollectReqVO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherRealtimeFyDO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationSsDO;
import com.sydigit.yzwater.module.service.weather.YzWeatherRealtimeFyService;
import com.sydigit.yzwater.module.service.weather.YzWeatherService;
import com.sydigit.yzwater.module.service.weather.YzWeatherStationSpiderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 天气采集
 */
@Tag(name = "管理后台 - 天气采集")
@RestController
@RequestMapping("/weather")
@Validated
@RequiredArgsConstructor
public class YzWeatherController {

    private final YzWeatherService weatherService;
    private final YzWeatherStationSpiderService weatherStationSpiderService;
    private final YzWeatherRealtimeFyService weatherRealtimeFyService;

    @PostMapping("/getWeatherByBeginTime")
    @Operation(summary = "按 beginTime 获取天气；无缓存时自动采集并入库")
    @PermitAll
    public CommonResult<Map<String, Object>> getWeatherByBeginTime(@Valid @RequestBody WeatherBeginTimeReqVO reqVO) {
        return success(weatherService.getWeatherByBeginTime(reqVO.getBeginTime()));
    }

    @PostMapping("/collect")
    @Operation(summary = "手动触发天气采集并落库；synTime 为空默认当天")
    @PermitAll
    public CommonResult<Map<String, Object>> collect(@RequestBody(required = false) WeatherCollectReqVO reqVO) {
        String synTime = reqVO == null ? null : reqVO.getSynTime();
        return success(weatherService.collectWeatherBySynTime(synTime));
    }

    @PostMapping("/station/collect")
    @Operation(summary = "手动触发天气站点实时采集并写入 yz_weather_station_ss")
    @PermitAll
    public CommonResult<Map<String, Object>> collectStationRealtime(
            @RequestBody(required = false) WeatherStationCollectReqVO reqVO) {
        List<String> stationCodes = reqVO == null ? null : reqVO.getStationCodes();
        return success(weatherStationSpiderService.collectStationRealtime(stationCodes));
    }

    @GetMapping("/station/latest")
    @Operation(summary = "按站点编码查询最新实时天气")
    @PermitAll
    public CommonResult<YzWeatherStationSsDO> getLatestStationRealtime(@RequestParam("stationCode") String stationCode) {
        return success(weatherStationSpiderService.getLatestByStationCode(stationCode));
    }

    @PostMapping("/fy/collect")
    @Operation(summary = "手动触发 FY 实时图层采集并写入 yz_weather_realtime_fy")
    @PermitAll
    public CommonResult<Map<String, Object>> collectFyRealtime(@RequestBody(required = false) WeatherFyCollectReqVO reqVO) {
        List<String> types = reqVO == null ? null : reqVO.getTypes();
        return success(weatherRealtimeFyService.collectRealtimeFy(types));
    }

    @GetMapping("/fy/latest")
    @Operation(summary = "查询最新 FY 图层列表（可按类型过滤）")
    @PermitAll
    public CommonResult<List<YzWeatherRealtimeFyDO>> getLatestFyList(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return success(weatherRealtimeFyService.getLatestList(type, limit));
    }
}
