package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryPageReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryPageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefHistoryTimelineItemRespVO;
import com.sydigit.yzwater.module.service.river.RiverChiefHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 仪征管理后台 - 历史河长信息（只读）
 */
@Tag(name = "仪征管理后台 - 历史河长信息")
@RestController
@RequestMapping("/river/chief-history")
@Validated
public class RiverChiefHistoryController {

    private final RiverChiefHistoryService riverChiefHistoryService;

    public RiverChiefHistoryController(RiverChiefHistoryService riverChiefHistoryService) {
        this.riverChiefHistoryService = riverChiefHistoryService;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询历史河长信息（按设施聚合，仅当前河长）")
    public CommonResult<PageResult<RiverChiefHistoryPageRespVO>> getPage(@Valid RiverChiefHistoryPageReqVO reqVO) {
        return success(riverChiefHistoryService.getPage(reqVO));
    }

    @GetMapping("/timeline")
    @Operation(summary = "查询某个设施下的河长变更时间轴（当前 + 历史）")
    public CommonResult<List<RiverChiefHistoryTimelineItemRespVO>> getTimeline(String referenceType, Long referenceId) {
        return success(riverChiefHistoryService.getTimeline(referenceType, referenceId));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出历史河长信息 Excel（按设施聚合，仅当前河长）")
    public void exportExcel(@Valid RiverChiefHistoryPageReqVO reqVO, HttpServletResponse response) throws IOException {
        List<RiverChiefHistoryExportExcelVO> list = riverChiefHistoryService.getExportList(reqVO);
        ExcelUtils.write(response, "历史河长信息.xls", "数据", RiverChiefHistoryExportExcelVO.class, list);
    }
}

