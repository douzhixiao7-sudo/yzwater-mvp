package com.sydigit.yzwater.module.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZbbItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZbbListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZbbSaveReqVO;
import com.sydigit.yzwater.module.service.flood.FxZbbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 值班安排
 */
@Tag(name = "管理后台 - 值班安排")
@RestController
@RequestMapping("/fx-zbb")
@Validated
public class FxZbbController {

    private static final List<WeekDay> WEEK_DAYS = List.of(
            new WeekDay(1, "星期一"),
            new WeekDay(2, "星期二"),
            new WeekDay(3, "星期三"),
            new WeekDay(4, "星期四"),
            new WeekDay(5, "星期五"),
            new WeekDay(6, "星期六"),
            new WeekDay(0, "星期日")
    );

    private final FxZbbService zbbService;

    public FxZbbController(FxZbbService zbbService) {
        this.zbbService = zbbService;
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询值班表")
    public CommonResult<List<FxZbbListRespVO>> getList() {
        return success(zbbService.getList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询值班表详情")
    public CommonResult<FxZbbSaveReqVO> getDetail(@PathVariable("id") String id) {
        return success(zbbService.getDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增值班表")
    public CommonResult<String> create(@Valid @RequestBody FxZbbSaveReqVO reqVO) {
        return success(zbbService.create(reqVO));
    }

    @PutMapping
    @Operation(summary = "编辑值班表")
    public CommonResult<Boolean> update(@Valid @RequestBody FxZbbSaveReqVO reqVO) {
        zbbService.update(reqVO);
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除值班表")
    public CommonResult<Boolean> delete(@PathVariable("id") String id) {
        zbbService.delete(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出值班表 Excel")
    public void exportExcel(@RequestParam(value = "id", required = false) String id,
                            HttpServletResponse response) throws IOException {
        List<FxZbbListRespVO> list = zbbService.getExportList(id);
        try (Workbook workbook = new XSSFWorkbook()) {
            if (list.isEmpty()) {
                workbook.createSheet("数据");
            } else {
                int index = 1;
                for (FxZbbListRespVO item : list) {
                    createSheet(workbook, item, index++);
                }
            }
            String fileName = buildFileName(list, id);
            response.addHeader("Content-Disposition",
                    "attachment;filename=" + HttpUtils.encodeUtf8(fileName));
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
            workbook.write(response.getOutputStream());
        }
    }

    private String buildFileName(List<FxZbbListRespVO> list, String id) {
        if (StrUtil.isNotBlank(id) && list != null && !list.isEmpty()) {
            FxZbbListRespVO item = list.get(0);
            String start = StrUtil.blankToDefault(item.getStartDate(), "开始日期");
            String end = StrUtil.blankToDefault(item.getEndDate(), "结束日期");
            return "值班安排_" + start + "_" + end + ".xlsx";
        }
        return "值班安排.xlsx";
    }

    private void createSheet(Workbook workbook, FxZbbListRespVO data, int index) {
        String name = buildSheetName(data, index);
        Sheet sheet = workbook.createSheet(name);
        sheet.setColumnWidth(0, 12 * 256);
        for (int i = 1; i <= 7; i++) {
            sheet.setColumnWidth(i, 18 * 256);
        }

        CellStyle titleStyle = buildTitleStyle(workbook);
        CellStyle descStyle = buildDescStyle(workbook);
        CellStyle headerStyle = buildHeaderStyle(workbook);
        CellStyle cellStyle = buildCellStyle(workbook);

        int rowIndex = 0;
        rowIndex = createMergedRow(sheet, rowIndex, "值班安排", titleStyle);
        rowIndex = createMergedRow(sheet, rowIndex,
                "排班日期：" + formatRange(data.getStartDate(), data.getEndDate()), descStyle);
        rowIndex = createMergedRow(sheet, rowIndex,
                "值班说明：" + StrUtil.blankToDefault(data.getDescription(), "-"), descStyle);

        Row headerRow = sheet.createRow(rowIndex++);
        createCell(headerRow, 0, "日期", headerStyle);
        int colIndex = 1;
        for (WeekDay day : WEEK_DAYS) {
            createCell(headerRow, colIndex++, day.label(), headerStyle);
        }

        Map<Integer, FxZbbItemRespVO> dayMap = buildDayMap(data.getItems());
        rowIndex = createRoleRow(sheet, rowIndex, "值班长", dayMap, cellStyle, RoleType.DUTY_CHIEF);
        rowIndex = createRoleRow(sheet, rowIndex, "白班人员", dayMap, cellStyle, RoleType.SECTION_CHIEF);
        createRoleRow(sheet, rowIndex, "夜班人员", dayMap, cellStyle, RoleType.DUTY_STAFF);
    }

    private String buildSheetName(FxZbbListRespVO data, int index) {
        String start = StrUtil.trimToEmpty(data.getStartDate());
        String end = StrUtil.trimToEmpty(data.getEndDate());
        String base = start.isEmpty() && end.isEmpty()
                ? "值班安排" + index
                : StrUtil.format("{}_{}", start.isEmpty() ? "开始" : start, end.isEmpty() ? "结束" : end);
        return base.length() > 28 ? base.substring(0, 28) : base;
    }

    private Map<Integer, FxZbbItemRespVO> buildDayMap(List<FxZbbItemRespVO> items) {
        Map<Integer, FxZbbItemRespVO> map = new HashMap<>();
        if (items == null) {
            return map;
        }
        for (FxZbbItemRespVO item : items) {
            if (item == null || item.getWeekDay() == null) {
                continue;
            }
            int key = item.getWeekDay() == 7 ? 0 : item.getWeekDay();
            map.put(key, item);
        }
        return map;
    }

    private int createRoleRow(Sheet sheet,
                              int rowIndex,
                              String roleName,
                              Map<Integer, FxZbbItemRespVO> dayMap,
                              CellStyle cellStyle,
                              RoleType roleType) {
        Row row = sheet.createRow(rowIndex++);
        row.setHeightInPoints(48);
        createCell(row, 0, roleName, cellStyle);
        int colIndex = 1;
        for (WeekDay day : WEEK_DAYS) {
            FxZbbItemRespVO item = dayMap.get(day.value());
            String text = buildRoleText(item, roleType);
            createCell(row, colIndex++, text, cellStyle);
        }
        return rowIndex;
    }

    private String buildRoleText(FxZbbItemRespVO item, RoleType roleType) {
        if (item == null) {
            return "";
        }
        String name;
        String phone;
        if (roleType == RoleType.DUTY_CHIEF) {
            name = item.getDutyChiefName();
            phone = item.getDutyChiefMobile();
        } else if (roleType == RoleType.SECTION_CHIEF) {
            name = item.getSectionChiefName();
            phone = item.getSectionChiefMobile();
        } else {
            name = item.getDutyStaffName();
            phone = item.getDutyStaffMobile();
        }
        name = StrUtil.trimToEmpty(name);
        phone = StrUtil.trimToEmpty(phone);
        if (name.isEmpty() && phone.isEmpty()) {
            return "";
        }
        if (name.isEmpty()) {
            return phone;
        }
        if (phone.isEmpty()) {
            return name;
        }
        return name + "\n" + phone;
    }

    private int createMergedRow(Sheet sheet, int rowIndex, String text, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(22);
        Cell cell = createCell(row, 0, text, style);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 7));
        cell.setCellStyle(style);
        return rowIndex + 1;
    }

    private CellStyle buildTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle buildDescStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle buildHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = buildCellStyle(workbook);
        style.setFont(font);
        return style;
    }

    private CellStyle buildCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private Cell createCell(Row row, int colIndex, String text, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellStyle(style);
        cell.setCellValue(text == null ? "" : text);
        return cell;
    }

    private String formatRange(String start, String end) {
        String startText = StrUtil.blankToDefault(start, "-");
        String endText = StrUtil.blankToDefault(end, "-");
        return startText + " 至 " + endText;
    }

    private static class WeekDay {

        private final int value;
        private final String label;

        private WeekDay(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int value() {
            return value;
        }

        public String label() {
            return label;
        }
    }

    private enum RoleType {
        DUTY_CHIEF,
        SECTION_CHIEF,
        DUTY_STAFF
    }
}
