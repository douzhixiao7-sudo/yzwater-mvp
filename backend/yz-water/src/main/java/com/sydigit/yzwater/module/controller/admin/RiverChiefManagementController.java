package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementDetailRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefManagementSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefUserSyncRespVO;
import com.sydigit.yzwater.module.service.river.RiverChiefManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 河长管理（统一维护河道/河段/水库的河长信息）
 */
@Tag(name = "仪征管理后台 - 河长管理")
@RestController
@RequestMapping("/river/chief-management")
@Validated
public class RiverChiefManagementController {

    private final RiverChiefManagementService riverChiefManagementService;

    public RiverChiefManagementController(RiverChiefManagementService riverChiefManagementService) {
        this.riverChiefManagementService = riverChiefManagementService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询河长（仅当前有效：effectiveTo 为空）")
    public CommonResult<PageResult<RiverChiefManagementPageRespVO>> getPage(@Valid RiverChiefManagementPageReqVO reqVO) {
        return success(riverChiefManagementService.getPage(reqVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询河长详情")
    public CommonResult<RiverChiefManagementDetailRespVO> getDetail(@PathVariable("id") Long id) {
        return success(riverChiefManagementService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增河长（按关联对象生成新版本）")
    public CommonResult<Long> create(@Valid @RequestBody RiverChiefManagementSaveReqVO reqVO) {
        return success(riverChiefManagementService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑河长（支持重新关联河道/河段/水库，按关联对象生成新版本）")
    public CommonResult<Boolean> update(@Valid @RequestBody RiverChiefManagementSaveReqVO reqVO) {
        riverChiefManagementService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除河长（仅删除当前有效记录：按关联对象生成新版本）")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        riverChiefManagementService.delete(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出河长 Excel（仅当前有效：effectiveTo 为空）")
    public void exportExcel(@Valid RiverChiefManagementPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<RiverChiefManagementExportExcelVO> list = riverChiefManagementService.getExportList(reqVO);
        ExcelUtils.write(response, "河长管理.xls", "数据", RiverChiefManagementExportExcelVO.class, list);
    }

    @PostMapping("/sync-users")
    @Operation(summary = "同步当前生效河长账号（创建/更新用户并按级别赋权）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<RiverChiefUserSyncRespVO> syncUsers() {
        return success(riverChiefManagementService.syncCurrentChiefUsers());
    }
}
