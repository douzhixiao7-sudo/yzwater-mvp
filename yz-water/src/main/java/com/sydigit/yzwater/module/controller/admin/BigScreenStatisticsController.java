package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictCountItemVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictLabelValueRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFacilityGeomReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFacilityGeomRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelSearchRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelStatsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverHeadStatsRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelWithSectionsListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenReservoirAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenPumpStationAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenEmbankmentAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFxRiskHazardLayerRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialWarehouseAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenIrrigationDistrictAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenWaterPondAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardAreaOverviewRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondPageReqVO;
import com.sydigit.yzwater.module.service.pond.WaterPondService;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardLocationRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardProblemListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardReferenceDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.problem.ProblemFeedbackPageRespVO;
import com.sydigit.yzwater.module.service.screen.BigScreenStatisticsService;
import com.sydigit.yzwater.module.service.problem.ProblemFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 大屏统计
 */
@Tag(name = "仪征管理后台 - 大屏统计")
@RestController
@RequestMapping("/screen/statistics")
@Validated
public class BigScreenStatisticsController {

    private final BigScreenStatisticsService statisticsService;
    private final ProblemFeedbackService problemFeedbackService;
    private final WaterPondService waterPondService;

    public BigScreenStatisticsController(BigScreenStatisticsService statisticsService,
                                         ProblemFeedbackService problemFeedbackService,
                                         WaterPondService waterPondService) {
        this.statisticsService = statisticsService;
        this.problemFeedbackService = problemFeedbackService;
        this.waterPondService = waterPondService;
    }

    @GetMapping("/river-head")
    @Operation(summary = "大屏统计 - 河长总数")
    @PermitAll
    public CommonResult<BigScreenRiverHeadStatsRespVO> getRiverHeadStats() {
        return success(statisticsService.getRiverHeadStats());
    }

    @GetMapping("/river-channel")
    @Operation(summary = "大屏统计 - 河道总数/长度/流域面积（yz_river_channel_bf，不含村级）")
    @PermitAll
    public CommonResult<BigScreenRiverChannelStatsRespVO> getRiverChannelStats() {
        return success(statisticsService.getRiverChannelStats());
    }

    @GetMapping("/facility-types")
    @Operation(summary = "大屏统计 - 设施类别字典（zd_sslb）")
    @PermitAll
    public CommonResult<List<BigScreenDictLabelValueRespVO>> getFacilityTypes() {
        return success(statisticsService.getFacilityTypeDictList());
    }

    @PostMapping("/facility-geom")
    @Operation(summary = "大屏统计 - 按设施类别查询设施几何（WKT）与名称")
    @PermitAll
    public CommonResult<List<BigScreenFacilityGeomRespVO>> getFacilityGeom(@Valid @RequestBody BigScreenFacilityGeomReqVO reqVO) {
        return success(statisticsService.getFacilityGeomList(reqVO));
    }

    @GetMapping("/river-channel-search")
    @Operation(summary = "大屏统计 - 河道名称模糊查询（默认全部，按创建时间倒序）")
    @PermitAll
    public CommonResult<List<BigScreenRiverChannelSearchRespVO>> searchRiverChannel(@RequestParam(value = "riverName", required = false) String riverName) {
        return success(statisticsService.searchRiverChannels(riverName));
    }

    @GetMapping("/signboard-locations")
    @Operation(summary = "大屏统计 - 公示牌点位（可按河道级别筛选）")
    @PermitAll
    public CommonResult<BigScreenSignboardLocationRespVO> getSignboardLocations(@RequestParam(value = "riverLevel", required = false) String riverLevel) {
        return success(statisticsService.getSignboardLocations(riverLevel));
    }

    @GetMapping("/signboard-count-by-river-level")
    @Operation(summary = "大屏统计 - 按河道级别统计公示牌数量")
    @PermitAll
    public CommonResult<List<BigScreenDictCountItemVO>> getSignboardCountByRiverLevel() {
        return success(statisticsService.getSignboardCountByRiverLevel());
    }

    @GetMapping("/river-channel-count-by-river-level")
    @Operation(summary = "大屏统计 - 按河道级别统计河道数量")
    @PermitAll
    public CommonResult<List<BigScreenDictCountItemVO>> getRiverChannelCountByRiverLevel() {
        return success(statisticsService.getRiverChannelCountByRiverLevel());
    }

    @GetMapping("/signboard-reference-detail")
    @Operation(summary = "大屏统计 - 公示牌关联设施详情（名称/几何/ID）")
    @PermitAll
    public CommonResult<BigScreenSignboardReferenceDetailRespVO> getSignboardReferenceDetail(
            @RequestParam("referenceType") String referenceType,
            @RequestParam("referenceId") Long referenceId) {
        return success(statisticsService.getSignboardReferenceDetail(referenceType, referenceId));
    }

    @GetMapping("/river-channel-list")
    @Operation(summary = "大屏统计 - 河道列表（不分页，按河道/河段名称模糊或生态类型筛选）")
    @PermitAll
    public CommonResult<BigScreenRiverChannelWithSectionsListRespVO> getRiverChannelList(@Valid BigScreenRiverChannelListReqVO reqVO) {
        return success(statisticsService.getRiverChannelsWithSections(reqVO));
    }

    @GetMapping("/signboard-problems")
    @Operation(summary = "大屏统计 - 公示牌扫码问题（未办结）")
    @PermitAll
    public CommonResult<BigScreenSignboardProblemListRespVO> getSignboardProblems(@RequestParam("signboardId") Long signboardId,
                                                                                 @RequestParam(value = "createTime", required = false) String createTime) {
        return success(statisticsService.getSignboardProblems(signboardId, createTime));
    }

    @GetMapping("/river-area-overview")
    @Operation(summary = "大屏统计 - 河道总览（不分页，统计全量河道）")
    @PermitAll
    public CommonResult<BigScreenRiverAreaOverviewRespVO> getRiverAreaOverview() {
        return success(statisticsService.getRiverAreaOverview());
    }

    @GetMapping("/river-area-overview-by-area")
    @Operation(summary = "大屏统计 - 河道总览（按行政区划筛选，包含子级）")
    @PermitAll
    public CommonResult<BigScreenRiverAreaOverviewRespVO> getRiverAreaOverviewByArea(@RequestParam("areaId") Long areaId) {
        return success(statisticsService.getRiverAreaOverviewByArea(areaId));
    }

    @GetMapping("/reservoir-area-overview-by-area")
    @Operation(summary = "大屏统计 - 水库总览（按行政区划筛选，包含子级）")
    @PermitAll
    public CommonResult<BigScreenReservoirAreaOverviewRespVO> getReservoirAreaOverviewByArea(@RequestParam("areaId") Long areaId) {
        return success(statisticsService.getReservoirAreaOverviewByArea(areaId));
    }

    @GetMapping("/pump-station-area-overview-by-area")
    @Operation(summary = "大屏统计 - 泵站总览（按行政区划筛选，包含子级）")
    @PermitAll
    public CommonResult<BigScreenPumpStationAreaOverviewRespVO> getPumpStationAreaOverviewByArea(@RequestParam("areaId") Long areaId) {
        return success(statisticsService.getPumpStationAreaOverviewByArea(areaId));
    }

    @GetMapping("/embankment-area-overview-by-area")
    @Operation(summary = "大屏统计 - 提防总览（按行政区划筛选，包含子级）")
    @PermitAll
    public CommonResult<BigScreenEmbankmentAreaOverviewRespVO> getEmbankmentAreaOverviewByArea(@RequestParam("areaId") Long areaId) {
        return success(statisticsService.getEmbankmentAreaOverviewByArea(areaId));
    }

    @GetMapping("/flood-material-warehouse-area-overview-by-area")
    @Operation(summary = "大屏统计 - 防汛物资仓库总览（按行政区划筛选，包含子级）")
    @PermitAll
    public CommonResult<BigScreenFloodMaterialWarehouseAreaOverviewRespVO> getFloodMaterialWarehouseAreaOverviewByArea(
            @RequestParam("areaId") Long areaId) {
        return success(statisticsService.getFloodMaterialWarehouseAreaOverviewByArea(areaId));
    }

    @GetMapping("/irrigation-area-overview-by-area")
    @Operation(summary = "大屏统计 - 灌区总览（按行政区划筛选，包含子级）")
    @PermitAll
    public CommonResult<BigScreenIrrigationDistrictAreaOverviewRespVO> getIrrigationAreaOverviewByArea(@RequestParam("areaId") Long areaId) {
        return success(statisticsService.getIrrigationDistrictAreaOverviewByArea(areaId));
    }

    @GetMapping("/pond-area-overview-by-area")
    @Operation(summary = "大屏统计 - 坑塘总览（按行政区划筛选，包含子级；支持坑塘业务筛选条件；返回面几何 GeoJSON）")
    @PermitAll
    public CommonResult<BigScreenWaterPondAreaOverviewRespVO> getPondAreaOverviewByArea(
            @RequestParam(value = "areaId", required = false) Long areaId,
            WaterPondPageReqVO reqVO) {
        return success(waterPondService.getMapAreaOverviewByArea(areaId, reqVO));
    }

    @GetMapping("/signboard-area-overview-by-area")
    @Operation(summary = "大屏统计 - 公示牌总览（按行政区划筛选，包含子级）")
    @PermitAll
    public CommonResult<BigScreenSignboardAreaOverviewRespVO> getSignboardAreaOverviewByArea(@RequestParam("areaId") Long areaId) {
        return success(statisticsService.getSignboardAreaOverviewByArea(areaId));
    }

    @GetMapping("/problem/feedback/page")
    @Operation(summary = "大屏统计 - 分页查询问题反馈（不校验登录与角色）")
    @PermitAll
    public CommonResult<PageResult<ProblemFeedbackPageRespVO>> getProblemFeedbackPage(@Valid ProblemFeedbackPageReqVO reqVO) {
        return success(problemFeedbackService.getFeedbackPageForScreen(reqVO));
    }

    @GetMapping("/problem/feedback/list")
    @Operation(summary = "大屏统计 - 列表查询问题反馈（不分页、不校验登录与角色）")
    @PermitAll
    public CommonResult<List<ProblemFeedbackPageRespVO>> getProblemFeedbackList(ProblemFeedbackListReqVO reqVO) {
        return success(problemFeedbackService.getFeedbackListForScreen(reqVO));
    }

    @GetMapping("/problem/feedback/{id}")
    @Operation(summary = "大屏统计 - 查询问题反馈详情（不校验登录与角色）")
    @PermitAll
    public CommonResult<ProblemFeedbackDetailRespVO> getProblemFeedbackDetail(@PathVariable("id") Long id) {
        return success(problemFeedbackService.getDetailForScreen(id));
    }

    @GetMapping("/fx-risk-hazard-layer")
    @Operation(summary = "大屏统计 - 风险隐患点 GIS 图层（业务信息 + 隐患点坐标 + 物资调运路线）")
    @PermitAll
    public CommonResult<BigScreenFxRiskHazardLayerRespVO> getFxRiskHazardLayer() {
        return success(statisticsService.getFxRiskHazardLayer());
    }
}
