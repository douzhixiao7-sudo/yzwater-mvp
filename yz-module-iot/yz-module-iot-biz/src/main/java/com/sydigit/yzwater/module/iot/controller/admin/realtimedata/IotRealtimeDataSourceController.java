package com.sydigit.yzwater.module.iot.controller.admin.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourcePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourceRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourceSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataSourceDO;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotRealtimeDataSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 实时数据采集源")
@RestController
@RequestMapping("/iot/realtime-data-source")
@Validated
public class IotRealtimeDataSourceController {

    @Resource
    private IotRealtimeDataSourceService sourceService;

    @PostMapping("/create")
    @Operation(summary = "创建采集源")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:create')")
    public CommonResult<Long> createSource(@Valid @RequestBody IotRealtimeDataSourceSaveReqVO createReqVO) {
        return success(sourceService.createSource(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采集源")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:update')")
    public CommonResult<Boolean> updateSource(@Valid @RequestBody IotRealtimeDataSourceSaveReqVO updateReqVO) {
        sourceService.updateSource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采集源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:delete')")
    public CommonResult<Boolean> deleteSource(@RequestParam("id") Long id) {
        sourceService.deleteSource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采集源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:query')")
    public CommonResult<IotRealtimeDataSourceRespVO> getSource(@RequestParam("id") Long id) {
        IotRealtimeDataSourceDO source = sourceService.getSource(id);
        return success(BeanUtils.toBean(source, IotRealtimeDataSourceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采集源分页")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:query')")
    public CommonResult<PageResult<IotRealtimeDataSourceRespVO>> getSourcePage(@Valid IotRealtimeDataSourcePageReqVO pageReqVO) {
        PageResult<IotRealtimeDataSourceDO> pageResult = sourceService.getSourcePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotRealtimeDataSourceRespVO.class));
    }

}