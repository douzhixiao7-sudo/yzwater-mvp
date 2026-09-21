package com.sydigit.yzwater.module.controller.admin;

import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.framework.excel.core.handler.ColumnWidthMatchStyleStrategy;
import com.sydigit.yzwater.framework.excel.core.handler.SelectSheetWriteHandler;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyExportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyListReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcyListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZzbcySaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxZzbcyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 防汛抗旱组织部成员
 */
@Tag(name = "管理后台 - 防汛抗旱组织部成员")
@RestController
@RequestMapping("/fx-zzbcy")
@Validated
public class FxZzbcyController {

    private final FxZzbcyService zzbcyService;

    public FxZzbcyController(FxZzbcyService zzbcyService) {
        this.zzbcyService = zzbcyService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询防汛抗旱组织部成员（按岗位正序，支持姓名和职务模糊查询）")
    public CommonResult<List<FxZzbcyListRespVO>> getList(@Validated @ModelAttribute FxZzbcyListReqVO reqVO) {
        return success(zzbcyService.getList(reqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出防汛抗旱组织部成员 Excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<FxZzbcyExportExcelVO> list = zzbcyService.getExportList();
        List<org.apache.poi.ss.util.CellRangeAddress> mergeRegions = buildMergeRegions(list);
        FastExcelFactory.write(response.getOutputStream(), FxZzbcyExportExcelVO.class)
                .autoCloseStream(false)
                .registerWriteHandler(new ColumnWidthMatchStyleStrategy())
                .registerWriteHandler(new SelectSheetWriteHandler(FxZzbcyExportExcelVO.class))
                .registerConverter(new LongStringConverter())
                .registerWriteHandler(new MergeCellWriteHandler(mergeRegions))
                .registerWriteHandler(new PositionCellStyleWriteHandler())
                .sheet("数据")
                .doWrite(list);
        response.addHeader("Content-Disposition",
                "attachment;filename=" + HttpUtils.encodeUtf8("防汛抗旱组织部成员.xls"));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "下载防汛抗旱组织部成员导入模板")
    public void getImportTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "防汛抗旱组织部成员导入模板.xls", "数据",
                FxZzbcyImportExcelVO.class, Collections.emptyList());
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传 Excel 并导入防汛抗旱组织部成员")
    public CommonResult<FxZzbcyImportRespVO> importExcel(@RequestPart("file") MultipartFile file) {
        return success(zzbcyService.importExcel(file));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询防汛抗旱组织部成员详情")
    public CommonResult<FxZzbcySaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(zzbcyService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增防汛抗旱组织部成员")
    public CommonResult<String> create(@Valid @RequestBody FxZzbcySaveReqVO reqVO) {
        return success(zzbcyService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑防汛抗旱组织部成员")
    public CommonResult<Boolean> update(@Valid @RequestBody FxZzbcySaveReqVO reqVO) {
        zzbcyService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除防汛抗旱组织部成员")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        zzbcyService.delete(id);
        return success(true);
    }

    private List<org.apache.poi.ss.util.CellRangeAddress> buildMergeRegions(List<FxZzbcyExportExcelVO> list) {
        List<org.apache.poi.ss.util.CellRangeAddress> regions = new ArrayList<>();
        if (list == null || list.size() < 2) {
            return regions;
        }
        int startIndex = 0;
        String lastPosition = normalizeLabel(list.get(0).getPositionLabel());
        for (int i = 1; i < list.size(); i++) {
            String current = normalizeLabel(list.get(i).getPositionLabel());
            if (lastPosition.equals(current)) {
                continue;
            }
            addMergeRegion(regions, startIndex, i - 1);
            startIndex = i;
            lastPosition = current;
        }
        addMergeRegion(regions, startIndex, list.size() - 1);
        return regions;
    }

    private String normalizeLabel(String value) {
        return value == null ? "" : value.trim();
    }

    private void addMergeRegion(List<org.apache.poi.ss.util.CellRangeAddress> regions, int start, int end) {
        if (end <= start) {
            return;
        }
        regions.add(new org.apache.poi.ss.util.CellRangeAddress(start + 1, end + 1, 0, 0));
    }

    private static class MergeCellWriteHandler implements SheetWriteHandler {

        private final List<org.apache.poi.ss.util.CellRangeAddress> mergeRegions;

        private MergeCellWriteHandler(List<org.apache.poi.ss.util.CellRangeAddress> mergeRegions) {
            this.mergeRegions = mergeRegions == null ? List.of() : mergeRegions;
        }

        @Override
        public void afterSheetCreate(cn.idev.excel.write.metadata.holder.WriteWorkbookHolder writeWorkbookHolder,
                                     cn.idev.excel.write.metadata.holder.WriteSheetHolder writeSheetHolder) {
            if (mergeRegions.isEmpty()) {
                return;
            }
            for (org.apache.poi.ss.util.CellRangeAddress region : mergeRegions) {
                writeSheetHolder.getSheet().addMergedRegionUnsafe(region);
            }
        }
    }

    private static class PositionCellStyleWriteHandler implements CellWriteHandler {

        private final Map<Short, CellStyle> styleCache = new HashMap<>();

        @Override
        public void afterCellDispose(WriteSheetHolder writeSheetHolder,
                                     WriteTableHolder writeTableHolder,
                                     List<WriteCellData<?>> cellDataList,
                                     Cell cell,
                                     Head head,
                                     Integer relativeRowIndex,
                                     Boolean isHead) {
            if (cell == null || cell.getColumnIndex() != 0) {
                return;
            }
            CellStyle style = cell.getCellStyle();
            short key = style == null ? -1 : style.getIndex();
            CellStyle cached = styleCache.get(key);
            if (cached == null) {
                cached = cell.getSheet().getWorkbook().createCellStyle();
                if (style != null) {
                    cached.cloneStyleFrom(style);
                }
                cached.setVerticalAlignment(VerticalAlignment.CENTER);
                styleCache.put(key, cached);
            }
            cell.setCellStyle(cached);
        }
    }
}
