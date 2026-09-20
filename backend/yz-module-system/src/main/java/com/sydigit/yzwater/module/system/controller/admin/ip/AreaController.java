package com.sydigit.yzwater.module.system.controller.admin.ip;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.ip.core.Area;
import com.sydigit.yzwater.framework.ip.core.utils.AreaUtils;
import com.sydigit.yzwater.framework.ip.core.utils.IPUtils;
import com.sydigit.yzwater.module.system.controller.admin.ip.vo.AreaGeoJsonImportRespVO;
import com.sydigit.yzwater.module.system.controller.admin.ip.vo.AreaRespVO;
import com.sydigit.yzwater.module.system.controller.admin.ip.vo.AreaSaveReqVO;
import com.sydigit.yzwater.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.service.area.SystemAreaService;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaGeoJsonImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 地区")
@RestController
@RequestMapping("/system/area")
@Validated
public class AreaController {

    /**
     * 管理后台 /system/area/tree 历史行为：返回 321081（仪征市）下级节点
     */
    private static final Long ADMIN_DEFAULT_ROOT_ID = 320000L;

    @Resource
    private SystemAreaService systemAreaService;

    @GetMapping("/tree")
    @Operation(summary = "获得地区树")
    public CommonResult<List<AreaNodeRespVO>> getAreaTree() {
        return success(BeanUtils.toBean(systemAreaService.getAreaTreeChildren(ADMIN_DEFAULT_ROOT_ID), AreaNodeRespVO.class));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新行政区划（从内置数据刷入 system_area 表）")
    public CommonResult<Integer> refresh() {
        return success(systemAreaService.refreshFromBuiltin());
    }

    @PostMapping("/import-geojson")
    @Operation(summary = "上传行政区划 GeoJSON 并写入 gemo（按名称匹配）")
    public CommonResult<AreaGeoJsonImportRespVO> importGeoJson(@RequestPart("file") MultipartFile file) throws IOException {
        SystemAreaGeoJsonImportResult result = systemAreaService.importGeoJson(file == null ? null : file.getBytes());
        AreaGeoJsonImportRespVO respVO = new AreaGeoJsonImportRespVO();
        respVO.setTotal(result.getTotal());
        respVO.setCreated(result.getCreated());
        respVO.setUpdated(result.getUpdated());
        respVO.setSkipped(result.getSkipped());
        respVO.setErrors(result.getErrors());
        return success(respVO);
    }

    @PostMapping("/import-geojson-by-adcode")
    @Operation(summary = "上传行政区划 GeoJSON 并写入 gemo（按 properties.adcode 匹配）")
    public CommonResult<AreaGeoJsonImportRespVO> importGeoJsonByAdcode(@RequestPart("file") MultipartFile file) throws IOException {
        SystemAreaGeoJsonImportResult result = systemAreaService.importGeoJsonByAdcode(file == null ? null : file.getBytes());
        AreaGeoJsonImportRespVO respVO = new AreaGeoJsonImportRespVO();
        respVO.setTotal(result.getTotal());
        respVO.setCreated(result.getCreated());
        respVO.setUpdated(result.getUpdated());
        respVO.setSkipped(result.getSkipped());
        respVO.setErrors(result.getErrors());
        return success(respVO);
    }

    @PostMapping("/import-geojson-children-by-fromentiid")
    @Operation(summary = "上传行政区划子节点 GeoJSON 并写入 system_area（默认 EPSG:4550 转 EPSG:4490）")
    public CommonResult<AreaGeoJsonImportRespVO> importGeoJsonChildrenByFromEntiId(@RequestPart("file") MultipartFile file) throws IOException {
        SystemAreaGeoJsonImportResult result = systemAreaService.importGeoJsonChildrenByFromEntiId(file == null ? null : file.getBytes());
        AreaGeoJsonImportRespVO respVO = new AreaGeoJsonImportRespVO();
        respVO.setTotal(result.getTotal());
        respVO.setCreated(result.getCreated());
        respVO.setUpdated(result.getUpdated());
        respVO.setSkipped(result.getSkipped());
        respVO.setErrors(result.getErrors());
        return success(respVO);
    }

    @PostMapping("/create")
    @Operation(summary = "新增行政区划")
    public CommonResult<Long> create(@Valid @RequestBody AreaSaveReqVO reqVO) {
        SystemAreaDO area = BeanUtils.toBean(reqVO, SystemAreaDO.class);
        return success(systemAreaService.create(area));
    }

    @PutMapping("/update")
    @Operation(summary = "修改行政区划")
    public CommonResult<Boolean> update(@Valid @RequestBody AreaSaveReqVO reqVO) {
        SystemAreaDO area = BeanUtils.toBean(reqVO, SystemAreaDO.class);
        systemAreaService.update(area);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除行政区划")
    @Parameter(name = "id", description = "行政区划编码", required = true, example = "321081")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        systemAreaService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "查看行政区划详情")
    @Parameter(name = "id", description = "行政区划编码", required = true, example = "321081")
    public CommonResult<AreaRespVO> get(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(systemAreaService.get(id), AreaRespVO.class));
    }

    @GetMapping("/get-by-ip")
    @Operation(summary = "获得 IP 对应的地区名")
    @Parameter(name = "ip", description = "IP", required = true)
    public CommonResult<String> getAreaByIp(@RequestParam("ip") String ip) {
        // 获得城市
        Area area = IPUtils.getArea(ip);
        if (area == null) {
            return success("未知");
        }
        // 格式化返回
        return success(AreaUtils.format(area.getId()));
    }

}
