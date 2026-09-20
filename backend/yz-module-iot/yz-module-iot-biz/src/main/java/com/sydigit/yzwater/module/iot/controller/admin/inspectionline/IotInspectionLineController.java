package com.sydigit.yzwater.module.iot.controller.admin.inspectionline;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePointOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLinePointRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLineRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo.IotInspectionLineSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLineDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLinePointDO;
import com.sydigit.yzwater.module.iot.service.inspectionline.IotInspectionLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "IoT - 巡检线路")
@RestController
@RequestMapping("/iot/inspection-line")
@Validated
public class IotInspectionLineController {

    @Resource
    private IotInspectionLineService lineService;

    /**
     * 创建巡检线路。
     *
     * @param createReqVO 创建请求
     * @return 线路 ID
     */
    @PostMapping("/create")
    @Operation(summary = "创建巡检线路")
    @PreAuthorize("@ss.hasPermission('iot:inspection-line:create')")
    public CommonResult<Long> createLine(@Valid @RequestBody IotInspectionLineSaveReqVO createReqVO) {
        return success(lineService.createLine(createReqVO));
    }

    /**
     * 更新巡检线路。
     *
     * @param updateReqVO 更新请求
     * @return 是否成功
     */
    @PutMapping("/update")
    @Operation(summary = "更新巡检线路")
    @PreAuthorize("@ss.hasPermission('iot:inspection-line:update')")
    public CommonResult<Boolean> updateLine(@Valid @RequestBody IotInspectionLineSaveReqVO updateReqVO) {
        lineService.updateLine(updateReqVO);
        return success(true);
    }

    /**
     * 删除巡检线路。
     *
     * @param id 线路 ID
     * @return 是否成功
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除巡检线路")
    @Parameter(name = "id", description = "线路 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-line:delete')")
    public CommonResult<Boolean> deleteLine(@RequestParam("id") Long id) {
        lineService.deleteLine(id);
        return success(true);
    }

    /**
     * 获取巡检线路详情。
     *
     * @param id 线路 ID
     * @return 线路详情
     */
    @GetMapping("/get")
    @Operation(summary = "获取巡检线路详情")
    @Parameter(name = "id", description = "线路 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:inspection-line:query')")
    public CommonResult<IotInspectionLineRespVO> getLine(@RequestParam("id") Long id) {
        IotInspectionLineDO line = lineService.getLine(id);
        return success(convertResp(line));
    }

    /**
     * 分页查询巡检线路。
     *
     * @param pageReqVO 分页查询参数
     * @return 分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询巡检线路")
    @PreAuthorize("@ss.hasPermission('iot:inspection-line:query')")
    public CommonResult<PageResult<IotInspectionLineRespVO>> getLinePage(@Valid IotInspectionLinePageReqVO pageReqVO) {
        PageResult<IotInspectionLineDO> pageResult = lineService.getLinePage(pageReqVO);
        List<IotInspectionLineRespVO> list = CollectionUtils.convertList(pageResult.getList(), this::convertSimpleResp);
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    /**
     * 查询可选设备点位列表。
     */
    @GetMapping("/point-options")
    @Operation(summary = "查询巡检线路可选设备点位")
    @PreAuthorize("@ss.hasPermission('iot:inspection-line:query')")
    public CommonResult<List<IotInspectionLinePointOptionRespVO>> getPointOptions(
            @RequestParam("stationId") String stationId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return success(lineService.listPointOptions(stationId, keyword, limit));
    }

    /**
     * 转换线路详情响应，并组装点位列表。
     */
    private IotInspectionLineRespVO convertResp(IotInspectionLineDO line) {
        if (line == null) {
            return null;
        }
        IotInspectionLineRespVO respVO = BeanUtils.toBean(line, IotInspectionLineRespVO.class);
        List<IotInspectionLinePointDO> points = lineService.getLinePointList(line.getId());
        respVO.setPoints(BeanUtils.toBean(points, IotInspectionLinePointRespVO.class));
        return respVO;
    }

    /**
     * 转换分页场景响应，默认不加载点位详情。
     */
    private IotInspectionLineRespVO convertSimpleResp(IotInspectionLineDO line) {
        IotInspectionLineRespVO respVO = BeanUtils.toBean(line, IotInspectionLineRespVO.class);
        respVO.setPoints(Collections.emptyList());
        return respVO;
    }

}
