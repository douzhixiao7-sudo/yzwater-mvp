package com.sydigit.yzwater.module.iot.controller.admin.shiftteam;

import com.sydigit.yzwater.framework.apilog.core.annotation.ApiAccessLog;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamExportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftteam.vo.IotShiftTeamSaveReqVO;
import com.sydigit.yzwater.module.iot.service.shiftteam.IotShiftTeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 班组 Controller
 */
@Tag(name = "IoT - 班组管理")
@RestController
@RequestMapping("/iot/shift-team")
@Validated
public class IotShiftTeamController {

    @Resource
    private IotShiftTeamService shiftTeamService;

    @PostMapping("/create")
    @Operation(summary = "创建班组")
    @PreAuthorize("@ss.hasPermission('iot:shift-team:create')")
    public CommonResult<Long> createShiftTeam(@Valid @RequestBody IotShiftTeamSaveReqVO createReqVO) {
        return success(shiftTeamService.createShiftTeam(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新班组")
    @PreAuthorize("@ss.hasPermission('iot:shift-team:update')")
    public CommonResult<Boolean> updateShiftTeam(@Valid @RequestBody IotShiftTeamSaveReqVO updateReqVO) {
        shiftTeamService.updateShiftTeam(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除班组")
    @Parameter(name = "id", description = "班组 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-team:delete')")
    public CommonResult<Boolean> deleteShiftTeam(@RequestParam("id") Long id) {
        shiftTeamService.deleteShiftTeam(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取班组详情")
    @Parameter(name = "id", description = "班组 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:shift-team:get')")
    public CommonResult<IotShiftTeamRespVO> getShiftTeam(@RequestParam(value = "id", required = false) Long id) {
        if (id == null || id <= 0) {
            return success(null);
        }
        return success(shiftTeamService.getShiftTeam(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询班组")
    @PreAuthorize("@ss.hasPermission('iot:shift-team:query')")
    public CommonResult<PageResult<IotShiftTeamRespVO>> getShiftTeamPage(@Valid IotShiftTeamPageReqVO pageReqVO) {
        return success(shiftTeamService.getShiftTeamPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出班组 Excel")
    @PreAuthorize("@ss.hasPermission('iot:shift-team:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportShiftTeamExcel(@Valid IotShiftTeamPageReqVO exportReqVO,
                                     HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotShiftTeamRespVO> list = shiftTeamService.getShiftTeamList(exportReqVO);
        if (list.isEmpty()) {
            ExcelUtils.write(response, "班组管理.xls", "班组管理", IotShiftTeamExportExcelVO.class, Collections.emptyList());
            return;
        }
        List<IotShiftTeamExportExcelVO> excelList = CollectionUtils.convertList(list, item -> {
            IotShiftTeamExportExcelVO excelVO = new IotShiftTeamExportExcelVO();
            excelVO.setTeamNo(item.getTeamNo());
            excelVO.setTeamName(item.getTeamName());
            excelVO.setLeaderUserName(item.getLeaderUserName());
            excelVO.setMemberCount(item.getMemberCount());
            excelVO.setRemark(item.getRemark());
            excelVO.setCreator(item.getCreator());
            excelVO.setCreateTime(item.getCreateTime());
            return excelVO;
        });
        ExcelUtils.write(response, "班组管理.xls", "班组管理", IotShiftTeamExportExcelVO.class, excelList);
    }
}
