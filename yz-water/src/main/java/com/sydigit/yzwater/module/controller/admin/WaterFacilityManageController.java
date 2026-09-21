package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityAreaCountRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityCustomizeCreateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityGeomMigrateRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityMapItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityPageItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityGeometryUpdateReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.WaterFacilityUpdateReqVO;
import com.sydigit.yzwater.module.service.geoBase.WaterFacilityQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 水利设施查询与管理
 */
@Tag(name = "仪征管理后台 - 水利设施管理")
@RestController
@RequestMapping("/water/facility")
@Validated
public class WaterFacilityManageController {

    private final WaterFacilityQueryService waterFacilityQueryService;
    private final DictDataCommonApi dictDataApi;

    public WaterFacilityManageController(WaterFacilityQueryService waterFacilityQueryService,
                                         DictDataCommonApi dictDataApi) {
        this.waterFacilityQueryService = waterFacilityQueryService;
        this.dictDataApi = dictDataApi;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询水利设施")
    public CommonResult<PageResult<WaterFacilityPageItemRespVO>> getFacilityPage(@Valid WaterFacilityPageReqVO reqVO) {
        return success(waterFacilityQueryService.getFacilityPage(reqVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询水利设施详情（含空间信息）")
    public CommonResult<WaterFacilityDetailRespVO> getFacilityDetail(@PathVariable("id") Long id) {
        return success(waterFacilityQueryService.getFacilityDetail(id));
    }

    @PutMapping
    @Operation(summary = "修改水利设施")
    public CommonResult<Boolean> updateFacility(@Valid @RequestBody WaterFacilityUpdateReqVO reqVO) {
        waterFacilityQueryService.updateFacility(reqVO);
        return success(true);
    }

    @PutMapping("/{id}/geometry")
    @Operation(summary = "更新水利设施几何（仅基础表 geom）")
    public CommonResult<Boolean> updateFacilityGeometry(@PathVariable("id") Long id,
                                                        @Valid @RequestBody WaterFacilityGeometryUpdateReqVO reqVO) {
        waterFacilityQueryService.updateFacilityGeometry(id, reqVO);
        return success(true);
    }

    @PutMapping("/migrate-geometry")
    @Operation(summary = "迁移空间表几何数据到基础表")
    public CommonResult<WaterFacilityGeomMigrateRespVO> migrateGeometry() {
        return success(waterFacilityQueryService.migrateGeometryToBase());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除水利设施（含空间信息）")
    public CommonResult<Boolean> deleteFacility(@PathVariable("id") Long id) {
        waterFacilityQueryService.deleteFacility(id);
        return success(true);
    }

    @GetMapping("/area-count")
    @Operation(summary = "按行政区划统计设施数量（用于首页地图）")
    public CommonResult<List<WaterFacilityAreaCountRespVO>> getFacilityAreaCount(@RequestParam("facilityType") String facilityType) {
        return success(waterFacilityQueryService.getFacilityAreaCount(facilityType));
    }

    @GetMapping("/list-by-area")
    @Operation(summary = "按行政区划查询设施列表（用于首页弹窗）")
    public CommonResult<List<WaterFacilityMapItemRespVO>> getFacilityListByArea(@RequestParam("facilityType") String facilityType,
                                                                               @RequestParam("areaId") Long areaId) {
        return success(waterFacilityQueryService.getFacilityListByArea(facilityType, areaId));
    }

    @GetMapping("/list-by-name")
    @Operation(summary = "按名称模糊查询设施列表（用于首页搜索）")
    public CommonResult<List<WaterFacilityMapItemRespVO>> getFacilityListByName(@RequestParam("facilityType") String facilityType,
                                                                               @RequestParam("name") String name,
                                                                               @RequestParam(value = "limit", required = false) Integer limit) {
        return success(waterFacilityQueryService.getFacilityListByName(facilityType, name, limit));
    }

    @GetMapping("/dict")
    @Operation(summary = "获取字典数据（facility_type/gis_stlx）")
    public CommonResult<List<DictDataRespDTO>> getDict(@RequestParam("dictType") String dictType) {
        return success(dictDataApi.getDictDataList(dictType));
    }

    @PostMapping("/customize")
    @Operation(summary = "新增自定义图层（写入设施基础表 yz_water_facility_base）")
    public CommonResult<Long> createCustomizeFacility(@Valid @RequestBody WaterFacilityCustomizeCreateReqVO reqVO) {
        return success(waterFacilityQueryService.createCustomizeFacility(reqVO));
    }
}
