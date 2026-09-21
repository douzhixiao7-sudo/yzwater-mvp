package com.sydigit.yzwater.module.iot.controller.admin.spare;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckUpdateReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareCheckDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import com.sydigit.yzwater.module.iot.service.spare.IotSpareCheckService;
import com.sydigit.yzwater.module.iot.service.spare.IotSpareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertMap;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "IoT - 备件盘点")
@RestController
@RequestMapping("/iot/spare-check")
@Validated
public class IotSpareCheckController {

    @Resource
    private IotSpareCheckService spareCheckService;
    @Resource
    private IotSpareService spareService;

    @PostMapping("/create")
    @Operation(summary = "创建盘点记录")
    @PreAuthorize("@ss.hasPermission('iot:spare-check:create')")
    public CommonResult<Long> createCheck(@Valid @RequestBody IotSpareCheckSaveReqVO createReqVO) {
        return success(spareCheckService.createCheck(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改盘点记录")
    @PreAuthorize("@ss.hasPermission('iot:spare-check:update')")
    public CommonResult<Boolean> updateCheck(@Valid @RequestBody IotSpareCheckUpdateReqVO updateReqVO) {
        spareCheckService.updateCheck(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点记录")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:spare-check:delete')")
    public CommonResult<Boolean> deleteCheck(@RequestParam("id") Long id) {
        spareCheckService.deleteCheck(id);
        return success(true);
    }

    @PutMapping("/apply")
    @Operation(summary = "审批盘点结果")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:spare-check:apply')")
    public CommonResult<Boolean> applyCheck(@RequestParam("id") Long id) {
        spareCheckService.applyCheckResult(id);
        return success(true);
    }

    @PutMapping("/reverse-apply")
    @Operation(summary = "反审批盘点结果")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('iot:spare-check:apply')")
    public CommonResult<Boolean> reverseApplyCheck(@RequestParam("id") Long id) {
        spareCheckService.reverseApplyCheckResult(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取盘点记录")
    @Parameter(name = "id", description = "主键", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:spare-check:query')")
    public CommonResult<IotSpareCheckRespVO> getCheck(@RequestParam("id") Long id) {
        IotSpareCheckDO check = spareCheckService.getCheck(id);
        return success(convertResp(check));
    }

    @GetMapping("/page")
    @Operation(summary = "盘点记录分页")
    @PreAuthorize("@ss.hasPermission('iot:spare-check:query')")
    public CommonResult<PageResult<IotSpareCheckRespVO>> getCheckPage(@Valid IotSpareCheckPageReqVO pageReqVO) {
        PageResult<IotSpareCheckDO> pageResult = spareCheckService.getCheckPage(pageReqVO);
        Map<Long, IotSpareDO> spareMap = getSpareMap(pageResult.getList());
        List<IotSpareCheckRespVO> list = convertList(pageResult.getList(), check -> convertResp(check, spareMap));
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    private Map<Long, IotSpareDO> getSpareMap(List<IotSpareCheckDO> list) {
        return convertMap(spareService.getSpareListByIds(convertSet(list, IotSpareCheckDO::getSpareId)), IotSpareDO::getId);
    }

    private IotSpareCheckRespVO convertResp(IotSpareCheckDO check) {
        if (check == null) {
            return null;
        }
        IotSpareCheckRespVO respVO = BeanUtils.toBean(check, IotSpareCheckRespVO.class);
        IotSpareDO spare = spareService.getSpare(check.getSpareId());
        if (spare != null) {
            respVO.setSpareName(spare.getSpareName());
            respVO.setSpareSpec(spare.getSpareSpec());
            respVO.setSpareModel(spare.getSpareModel());
        }
        return respVO;
    }

    private IotSpareCheckRespVO convertResp(IotSpareCheckDO check, Map<Long, IotSpareDO> spareMap) {
        IotSpareCheckRespVO respVO = BeanUtils.toBean(check, IotSpareCheckRespVO.class);
        IotSpareDO spare = spareMap.get(check.getSpareId());
        if (spare != null) {
            respVO.setSpareName(spare.getSpareName());
            respVO.setSpareSpec(spare.getSpareSpec());
            respVO.setSpareModel(spare.getSpareModel());
        }
        return respVO;
    }

}
