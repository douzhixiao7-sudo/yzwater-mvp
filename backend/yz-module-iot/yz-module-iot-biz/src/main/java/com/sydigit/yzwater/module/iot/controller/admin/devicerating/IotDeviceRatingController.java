package com.sydigit.yzwater.module.iot.controller.admin.devicerating;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.rating.IotDeviceRatingDO;
import com.sydigit.yzwater.module.iot.service.devicerating.IotDeviceRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 设备评级")
@RestController
@RequestMapping("/iot/device-rating")
@Validated
public class IotDeviceRatingController {

    @Resource
    private IotDeviceRatingService deviceRatingService;

    @PostMapping("/create")
    @Operation(summary = "创建设备评级")
    @PreAuthorize("@ss.hasPermission('iot:device-rating:create')")
    public CommonResult<Long> createDeviceRating(@Valid @RequestBody IotDeviceRatingSaveReqVO createReqVO) {
        return success(deviceRatingService.createDeviceRating(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备评级")
    @PreAuthorize("@ss.hasPermission('iot:device-rating:update')")
    public CommonResult<Boolean> updateDeviceRating(@Valid @RequestBody IotDeviceRatingSaveReqVO updateReqVO) {
        deviceRatingService.updateDeviceRating(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备评级")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device-rating:delete')")
    public CommonResult<Boolean> deleteDeviceRating(@RequestParam("id") Long id) {
        deviceRatingService.deleteDeviceRating(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取设备评级")
    @Parameter(name = "id", description = "主键", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-rating:query')")
    public CommonResult<IotDeviceRatingRespVO> getDeviceRating(@RequestParam("id") Long id) {
        IotDeviceRatingDO rating = deviceRatingService.getDeviceRating(id);
        return success(convertResp(rating));
    }

    @GetMapping("/page")
    @Operation(summary = "设备评级分页")
    @PreAuthorize("@ss.hasPermission('iot:device-rating:query')")
    public CommonResult<PageResult<IotDeviceRatingRespVO>> getDeviceRatingPage(@Valid IotDeviceRatingPageReqVO pageReqVO) {
        PageResult<IotDeviceRatingDO> pageResult = deviceRatingService.getDeviceRatingPage(pageReqVO);
        List<IotDeviceRatingRespVO> list = CollectionUtils.convertList(pageResult.getList(), this::convertResp);
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备评级 Excel")
    @PreAuthorize("@ss.hasPermission('iot:device-rating:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeviceRatingExcel(@Valid IotDeviceRatingPageReqVO exportReqVO,
                                        HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDeviceRatingDO> list = deviceRatingService.getDeviceRatingList(exportReqVO);
        List<IotDeviceRatingExportExcelVO> excelList = CollectionUtils.convertList(list, this::convertExport);
        ExcelUtils.write(response, "设备评级.xls", "设备评级", IotDeviceRatingExportExcelVO.class, excelList);
    }

    private IotDeviceRatingRespVO convertResp(IotDeviceRatingDO rating) {
        if (rating == null) {
            return null;
        }
        IotDeviceRatingRespVO respVO = BeanUtils.toBean(rating, IotDeviceRatingRespVO.class);
        if (rating.getAttachments() != null) {
            respVO.setAttachments(Arrays.asList(rating.getAttachments()));
        } else {
            respVO.setAttachments(Collections.emptyList());
        }
        return respVO;
    }

    private IotDeviceRatingExportExcelVO convertExport(IotDeviceRatingDO rating) {
        IotDeviceRatingExportExcelVO respVO = BeanUtils.toBean(rating, IotDeviceRatingExportExcelVO.class);
        if (rating.getAttachments() != null && rating.getAttachments().length > 0) {
            respVO.setAttachments(String.join(",", rating.getAttachments()));
        }
        return respVO;
    }
}
