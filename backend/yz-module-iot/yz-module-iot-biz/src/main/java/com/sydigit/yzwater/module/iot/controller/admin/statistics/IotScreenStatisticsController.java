package com.sydigit.yzwater.module.iot.controller.admin.statistics;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenAreaNodeRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenAreaRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenDeviceOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenDeviceStartStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenGateMetricSnapshotRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenOverviewCountRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPumpStationGateDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPumpStationHostDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPondFilterOptionsRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPondMvtReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPondPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPumpStationRunningStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenReservoirCapacityRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenReservoirSummaryRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStationHostStatusRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStationOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStreamWaterListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStreamWaterRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenVideoStationCountRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenWorkConditionLatestRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenWorkConditionListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenWorkConditionRespVO;
import com.sydigit.yzwater.module.iot.service.statistics.IotScreenStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "大屏统计-综合总览")
@RestController
@RequestMapping("/iot/statistics/screen")
@Validated
public class IotScreenStatisticsController {

    @Resource
    private IotScreenStatisticsService screenStatisticsService;

    @GetMapping("/overview-count")
    @Operation(summary = "获取大屏总览数量")
    @PermitAll
    public CommonResult<IotScreenOverviewCountRespVO> getOverviewCount() {
        return success(screenStatisticsService.getOverviewCount());
    }

    @GetMapping("/reservoir-summary")
    @Operation(summary = "获取水库总库容/总集水面积/总灌溉面积")
    @PermitAll
    public CommonResult<IotScreenReservoirSummaryRespVO> getReservoirSummary() {
        return success(screenStatisticsService.getReservoirSummary());
    }

    @GetMapping("/reservoir-capacity-list")
    @Operation(summary = "获取水库容量列表（名称 + totalCapacity）")
    @PermitAll
    public CommonResult<List<IotScreenReservoirCapacityRespVO>> getReservoirCapacityList() {
        return success(screenStatisticsService.getReservoirCapacityList());
    }

    @GetMapping("/station-options")
    @Operation(summary = "获取站点下拉（字典 iot_zd_sbzd）")
    @PermitAll
    public CommonResult<List<IotScreenStationOptionRespVO>> getStationOptions() {
        return success(screenStatisticsService.getStationOptions());
    }

    @GetMapping("/station-host-status-list")
    @Operation(summary = "查询各站点主机运行/停止数量")
    @PermitAll
    public CommonResult<List<IotScreenStationHostStatusRespVO>> getStationHostStatusList() {
        return success(screenStatisticsService.getStationHostStatusList());
    }

    @GetMapping("/pump-station-host-detail-list")
    @Operation(summary = "按泵站名称查询主机名称、主机状态、有功功率和采集时间")
    @Parameter(name = "pumpStationName", description = "泵站名称（YzPumpStationDO.pumpStationName）", required = true)
    @PermitAll
    public CommonResult<List<IotScreenPumpStationHostDetailRespVO>> getPumpStationHostDetailList(
            @RequestParam("pumpStationName") @NotBlank(message = "泵站名称不能为空") String pumpStationName) {
        return success(screenStatisticsService.getPumpStationHostDetailList(pumpStationName));
    }

    @GetMapping("/device-options")
    @Operation(summary = "获取设备名称下拉（先按站点，再按设备类型过滤；不传 deviceType 默认水位计）")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @Parameter(name = "deviceType", description = "设备类型（可选；不传默认 6=水位计；传 -1 返回该站全部类型）", required = false)
    @PermitAll
    public CommonResult<List<IotScreenDeviceOptionRespVO>> getDeviceOptions(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId,
            @RequestParam(value = "deviceType", required = false) Integer deviceType) {
        return success(screenStatisticsService.getDeviceOptions(stationId, deviceType));
    }

    @GetMapping("/device-start-stat-list")
    @Operation(summary = "查询设备启停统计（所属站点 + 设备类型=3）")
    @Parameter(name = "stationId", description = "所属站点值", required = true)
    @PermitAll
    public CommonResult<List<IotScreenDeviceStartStatRespVO>> getDeviceStartStatList(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId) {
        return success(screenStatisticsService.getDeviceStartStatList(stationId));
    }

    @GetMapping("/pump-station-running-stat")
    @Operation(summary = "按泵站名称查询闸站全开统计（总数、开机台数）")
    @Parameter(name = "pumpStationName", description = "泵站名称", required = true)
    @PermitAll
    public CommonResult<IotScreenPumpStationRunningStatRespVO> getPumpStationRunningStat(
            @RequestParam("pumpStationName") @NotBlank(message = "泵站名称不能为空") String pumpStationName) {
        return success(screenStatisticsService.getPumpStationRunningStat(pumpStationName));
    }

    @GetMapping("/pump-station-gate-detail-list")
    @Operation(summary = "按泵站名称查询闸门名称、全开/全关/上升/下降状态、有功功率、采集时间")
    @Parameter(name = "pumpStationName", description = "泵站名称（YzPumpStationDO.pumpStationName）", required = true)
    @PermitAll
    public CommonResult<List<IotScreenPumpStationGateDetailRespVO>> getPumpStationGateDetailList(
            @RequestParam("pumpStationName") @NotBlank(message = "泵站名称不能为空") String pumpStationName) {
        return success(screenStatisticsService.getPumpStationGateDetailList(pumpStationName));
    }

    @GetMapping("/pump-station-gate-open-stat")
    @Operation(summary = "按泵站名称查询闸站全开数统计（总数、开机台数）")
    @Parameter(name = "pumpStationName", description = "泵站名称（YzPumpStationDO.pumpStationName）", required = true)
    @PermitAll
    public CommonResult<IotScreenPumpStationRunningStatRespVO> getPumpStationGateOpenStat(
            @RequestParam("pumpStationName") @NotBlank(message = "泵站名称不能为空") String pumpStationName) {
        return success(screenStatisticsService.getPumpStationGateOpenStat(pumpStationName));
    }

    @GetMapping("/work-condition-list")
    @Operation(summary = "查询工情统计（所属站点 + 设备名称 + 时间范围）")
    @PermitAll
    public CommonResult<List<IotScreenWorkConditionRespVO>> getWorkConditionStatList(
            @Valid IotScreenWorkConditionListReqVO reqVO) {
        return success(screenStatisticsService.getWorkConditionStatList(reqVO));
    }

    @GetMapping("/work-condition-latest")
    @Operation(summary = "查询工况统计最新一条（所属站点 + 设备ID）")
    @Parameter(name = "stationId", description = "所属站点ID", required = true)
    @Parameter(name = "deviceId", description = "设备ID（IotDeviceDO.id）", required = true)
    @PermitAll
    public CommonResult<IotScreenWorkConditionLatestRespVO> getLatestWorkConditionStat(
            @RequestParam("stationId") @NotBlank(message = "所属站点不能为空") String stationId,
            @RequestParam("deviceId") @NotNull(message = "设备ID不能为空") Long deviceId) {
        return success(screenStatisticsService.getLatestWorkConditionStat(stationId, deviceId));
    }

    @GetMapping("/gate-metric-snapshot")
    @Operation(summary = "根据设备ID查询闸门电参快照（deviceType=7）")
    @Parameter(name = "deviceId", description = "设备ID（IotDeviceDO.id）", required = true)
    @PermitAll
    public CommonResult<IotScreenGateMetricSnapshotRespVO> getGateMetricSnapshot(
            @RequestParam("deviceId") @NotNull(message = "设备ID不能为空") Long deviceId) {
        return success(screenStatisticsService.getGateMetricSnapshot(deviceId));
    }

    @GetMapping("/stream-water-list")
    @Operation(summary = "查询 streamWater 水位列表（按时间倒序）")
    @PermitAll
    public CommonResult<List<IotScreenStreamWaterRespVO>> getStreamWaterList(
            @Valid IotScreenStreamWaterListReqVO reqVO) {
        return success(screenStatisticsService.getStreamWaterList(reqVO));
    }

    @GetMapping("/area-tree")
    @Operation(summary = "大屏行政区划树（仅结构，不含面；与一张图 /system/area/tree 一致）")
    @Parameter(name = "rootId", description = "根区划 id，默认 321081（仪征市）")
    @PermitAll
    public CommonResult<List<IotScreenAreaNodeRespVO>> getScreenAreaTree(
            @RequestParam(value = "rootId", required = false) Long rootId) {
        return success(screenStatisticsService.getScreenAreaTree(rootId));
    }

    @GetMapping("/area-get")
    @Operation(summary = "大屏行政区划详情（含 gemoGeoJson 面；与一张图 /system/area/get 一致，点击区划后按需查询）")
    @Parameter(name = "id", description = "区划 id", required = true, example = "321081104")
    @PermitAll
    public CommonResult<IotScreenAreaRespVO> getScreenArea(@RequestParam("id") Long id) {
        return success(screenStatisticsService.getScreenArea(id));
    }

    @GetMapping("/facility-by-slss")
    @Operation(summary = "按 zd_slss 查询。坑塘(14)分页列表支持台账同款多维过滤；传 id 返回详情")
    @Parameter(name = "value", description = "设施类型值。单值如 14", required = true)
    @Parameter(name = "id", description = "坑塘主键；传则返回详情")
    @Parameter(name = "pageNo", description = "页码（列表必传）")
    @Parameter(name = "pageSize", description = "每页条数（列表必传）")
    @Parameter(name = "areaId", description = "行政区划 id（含子级）")
    @Parameter(name = "villageCode", description = "行政区划代码（areaId 为空时生效）")
    @Parameter(name = "resourceName", description = "资源名称（模糊）；也可用 name/keyword")
    @Parameter(name = "resourceCode", description = "资源编号（模糊）")
    @Parameter(name = "locationDesc", description = "坐落位置（模糊）")
    @Parameter(name = "ownerUnit", description = "权属单位（模糊）")
    @Parameter(name = "ownershipType", description = "土地权属（精确）")
    @Parameter(name = "resourceType", description = "资源类型（精确）")
    @Parameter(name = "usageStatus", description = "使用状态（精确）")
    @Parameter(name = "resourceNature", description = "资源性质（精确）")
    @Parameter(name = "occupationStatus", description = "占用情况（精确）")
    @Parameter(name = "remark", description = "备注（模糊）")
    @Parameter(name = "surveyor", description = "调查员（模糊）")
    @Parameter(name = "landType", description = "国土地类（模糊）")
    @Parameter(name = "eastTo", description = "东至（模糊）")
    @Parameter(name = "southTo", description = "南至（模糊）")
    @Parameter(name = "westTo", description = "西至（模糊）")
    @Parameter(name = "northTo", description = "北至（模糊）")
    @PermitAll
    public CommonResult<?> getFacilityBySlss(@RequestParam("value") List<String> values,
                                             @RequestParam(value = "id", required = false) String id,
                                             IotScreenPondPageReqVO reqVO) {
        if (isPondOnlySlssQuery(values)) {
            // 按 id 查详情：直接返回丰富对象，便于页面渲染弹窗
            Long pondId = parsePondId(id);
            if (pondId != null) {
                Map<String, Object> detail = screenStatisticsService.getPondDetail(pondId,
                        reqVO == null ? null : reqVO.getIncludeGeometry());
                return success(detail == null ? Collections.emptyMap() : detail);
            }
            IotScreenPondPageReqVO pageReq = reqVO == null ? new IotScreenPondPageReqVO() : reqVO;
            return success(screenStatisticsService.getPondPage(pageReq));
        }
        Long areaId = reqVO == null ? null : reqVO.getAreaId();
        Map<String, List<Map<String, Object>>> grouped = screenStatisticsService.getFacilityBySlss(values, areaId);
        if (grouped == null || grouped.isEmpty()) {
            return success(Collections.emptyList());
        }
        // 单类型：直接返回数组，避免 {"坑塘":[...]} / {"14":[...]} 这层包装
        if (grouped.size() == 1) {
            return success(grouped.values().iterator().next());
        }
        return success(grouped);
    }

    private boolean isPondOnlySlssQuery(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        String pondValue = null;
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String normalized = value.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            if (pondValue != null) {
                return false;
            }
            pondValue = normalized;
        }
        return "14".equals(pondValue);
    }

    /** 坑塘 id 按字符串接收再解析，避免大整数经 Double 丢失精度 */
    private Long parsePondId(String id) {
        if (id == null) {
            return null;
        }
        String text = id.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text).longValueExact();
        } catch (Exception ex) {
            throw invalidParamException("坑塘 id 格式不正确: {0}", id);
        }
    }

    @GetMapping(value = "/pond-tiles/{z}/{x}/{y}", produces = "application/vnd.mapbox-vector-tile")
    @Operation(summary = "坑塘矢量瓦片（MVT）。按 z/x/y 现查现切，图层名 ponds；支持区划/名称/编号/位置/权属等过滤")
    @Parameter(name = "z", description = "缩放级别 0~22", required = true)
    @Parameter(name = "x", description = "瓦片列号", required = true)
    @Parameter(name = "y", description = "瓦片行号", required = true)
    @Parameter(name = "areaId", description = "行政区划 id（含子级）")
    @Parameter(name = "resourceName", description = "坑塘名称/资源名称（模糊）")
    @Parameter(name = "resourceCode", description = "资源编号（模糊）")
    @Parameter(name = "locationDesc", description = "坐落位置（模糊）")
    @Parameter(name = "ownerUnit", description = "权属单位名（模糊）")
    @Parameter(name = "ownershipType", description = "土地权属（精确）")
    @Parameter(name = "resourceType", description = "资源类型（精确）")
    @Parameter(name = "usageStatus", description = "使用状态（精确）")
    @Parameter(name = "resourceNature", description = "资源性质（精确）")
    @Parameter(name = "occupationStatus", description = "占用情况（精确）")
    @Parameter(name = "landType", description = "国土地类（模糊）")
    @Parameter(name = "villageName", description = "行政区名（模糊）")
    @Parameter(name = "remark", description = "备注（模糊）")
    @Parameter(name = "surveyor", description = "调查员（模糊）")
    @Parameter(name = "villageCode", description = "行政区划代码（areaId 为空时生效）")
    @Parameter(name = "eastTo", description = "东至（模糊）")
    @Parameter(name = "southTo", description = "南至（模糊）")
    @Parameter(name = "westTo", description = "西至（模糊）")
    @Parameter(name = "northTo", description = "北至（模糊）")
    @Parameter(name = "centerLon", description = "缓冲区中心经度（与 centerLat、bufferRadiusM 同时传）")
    @Parameter(name = "centerLat", description = "缓冲区中心纬度")
    @Parameter(name = "bufferRadiusM", description = "缓冲区半径（米）")
    @PermitAll
    public ResponseEntity<byte[]> getPondMvtTile(@PathVariable("z") int z,
                                                 @PathVariable("x") int x,
                                                 @PathVariable("y") int y,
                                                 IotScreenPondMvtReqVO reqVO) {
        byte[] tile = screenStatisticsService.getPondMvtTile(z, x, y, reqVO);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile")
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(tile == null ? new byte[0] : tile);
    }

    @GetMapping("/pond-filter-options")
    @Operation(summary = "坑塘筛选下拉选项（土地权属/资源类型/使用状态/资源性质/占用情况），对应台账筛选面板")
    @PermitAll
    public CommonResult<IotScreenPondFilterOptionsRespVO> getPondFilterOptions() {
        return success(screenStatisticsService.getPondFilterOptions());
    }

    @GetMapping("/video-station-count")
    @Operation(summary = "查询各站点监控设备数量（无需登录）")
    @PermitAll
    public CommonResult<List<IotScreenVideoStationCountRespVO>> getVideoStationCount() {
        return success(screenStatisticsService.getVideoCameraStationCountList());
    }

}
