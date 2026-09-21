package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtFyImageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtRainfallRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtStationWeatherRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtWeatherIndicatorRespVO;
import com.sydigit.yzwater.module.service.screen.BigScreenFloodDroughtStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 大屏统计-防汛抗旱
 */
@Tag(name = "大屏统计-防汛抗旱")
@RestController
@RequestMapping("/screen/flood-drought")
@Validated
@RequiredArgsConstructor
public class BigScreenFloodDroughtStatisticsController {

    private final BigScreenFloodDroughtStatisticsService floodDroughtStatisticsService;

    @GetMapping("/station-names")
    @Operation(summary = "接口1：获取所有天气站点名称")
    @PermitAll
    public CommonResult<List<String>> getStationNames() {
        return success(floodDroughtStatisticsService.getStationNames());
    }

    @GetMapping("/station-weather")
    @Operation(summary = "接口2：按站点名称查询对应实时天气")
    @Parameter(name = "stationName", description = "站点名称", required = true)
    @PermitAll
    public CommonResult<BigScreenFloodDroughtStationWeatherRespVO> getStationWeather(
            @RequestParam("stationName") @NotBlank(message = "stationName 不能为空") String stationName) {
        return success(floodDroughtStatisticsService.getStationWeatherByStationName(stationName));
    }

    @GetMapping("/rainfall-forecast")
    @Operation(summary = "接口3：获取未来1小时、12小时、24小时降雨量")
    @PermitAll
    public CommonResult<BigScreenFloodDroughtRainfallRespVO> getRainfallForecast() {
        return success(floodDroughtStatisticsService.getRainfallForecast());
    }

    @GetMapping("/fy-images")
    @Operation(summary = "接口4：按天气时间返回当天FY图层数据（weatherImage为完整URL）")
    @Parameter(name = "day", description = "日期，格式 yyyy-MM-dd，不传默认当天", required = false)
    @PermitAll
    public CommonResult<List<BigScreenFloodDroughtFyImageRespVO>> getTodayFyImages(
            @RequestParam(value = "day", required = false) String day) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        List<BigScreenFloodDroughtFyImageRespVO> images = floodDroughtStatisticsService.getTodayFyImages(day, baseUrl);
        String publicPrefix = baseUrl + "/admin-api/screen/flood-drought/fy-image/";
        images.forEach(image -> image.setWeatherImage(publicPrefix + image.getId()));
        return success(images);
    }

    @GetMapping("/fy-image/{id}")
    @Operation(summary = "FY图层图片内容访问（免登录）")
    @PermitAll
    public void getFyImage(@PathVariable("id") String id, HttpServletResponse response) {
        floodDroughtStatisticsService.writeFyImage(id, response);
    }

    @GetMapping("/weather-indicators")
    @Operation(summary = "接口5：获取温度、湿度、风力、PM2.5、空气质量")
    @PermitAll
    public CommonResult<BigScreenFloodDroughtWeatherIndicatorRespVO> getWeatherIndicators() {
        return success(floodDroughtStatisticsService.getWeatherIndicators());
    }

    @GetMapping("/weather-description")
    @Operation(summary = "接口6：获取当天天气描述")
    @PermitAll
    public CommonResult<String> getTodayWeatherDescription() {
        return success(floodDroughtStatisticsService.getTodayWeatherDescription());
    }
}
