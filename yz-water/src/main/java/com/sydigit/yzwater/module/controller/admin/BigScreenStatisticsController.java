package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFacilityGeomReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFacilityGeomRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenRiverChannelWithSectionsListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardLocationRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenSignboardReferenceDetailRespVO;
import com.sydigit.yzwater.module.service.screen.BigScreenStatisticsService;
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

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 移动端河长地图用到的统计接口。大屏其余统计不交付。
 */
@Tag(name = "仪征管理后台 - 地图统计")
@RestController
@RequestMapping("/screen/statistics")
@Validated
public class BigScreenStatisticsController {

    private final BigScreenStatisticsService statisticsService;

    public BigScreenStatisticsController(BigScreenStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PostMapping("/facility-geom")
    @Operation(summary = "按设施类别查询设施几何（WKT）与名称")
    @PermitAll
    public CommonResult<List<BigScreenFacilityGeomRespVO>> getFacilityGeom(@Valid @RequestBody BigScreenFacilityGeomReqVO reqVO) {
        return success(statisticsService.getFacilityGeomList(reqVO));
    }

    @GetMapping("/signboard-locations")
    @Operation(summary = "公示牌点位（可按河道级别筛选）")
    @PermitAll
    public CommonResult<BigScreenSignboardLocationRespVO> getSignboardLocations(@RequestParam(value = "riverLevel", required = false) String riverLevel) {
        return success(statisticsService.getSignboardLocations(riverLevel));
    }

    @GetMapping("/signboard-reference-detail")
    @Operation(summary = "公示牌关联设施详情（名称/几何/ID）")
    @PermitAll
    public CommonResult<BigScreenSignboardReferenceDetailRespVO> getSignboardReferenceDetail(
            @RequestParam("referenceType") String referenceType,
            @RequestParam("referenceId") Long referenceId) {
        return success(statisticsService.getSignboardReferenceDetail(referenceType, referenceId));
    }

    @GetMapping("/river-channel-list")
    @Operation(summary = "河道列表（不分页，按河道/河段名称模糊或生态类型筛选）")
    @PermitAll
    public CommonResult<BigScreenRiverChannelWithSectionsListRespVO> getRiverChannelList(@Valid BigScreenRiverChannelListReqVO reqVO) {
        return success(statisticsService.getRiverChannelsWithSections(reqVO));
    }
}
