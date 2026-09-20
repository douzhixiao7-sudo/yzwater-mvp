package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelSaveReqVO;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 河道 Excel 导入服务（模板字段已排除字典、布尔、编码、图片、数组等字段）
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChannelExcelImportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");

    private final RiverChannelService riverChannelService;

    public RiverChannelImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }
        List<RiverChannelImportExcelVO> rows;
        try {
            rows = ExcelUtils.read(file, RiverChannelImportExcelVO.class);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        RiverChannelImportRespVO respVO = new RiverChannelImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            RiverChannelImportExcelVO row = rows.get(i);
            int excelRowNo = i + 2;
            if (row == null || isEmptyRow(row)) {
                respVO.addSkip("第" + excelRowNo + "行跳过：空行");
                continue;
            }
            try {
                RiverChannelSaveReqVO reqVO = buildReqVO(row);
                if (StrUtil.isBlank(reqVO.getRiverName())) {
                    respVO.addFailure("第" + excelRowNo + "行失败：河道名称不能为空");
                    continue;
                }
                // 按需求：导入河道默认不划分河段
                reqVO.setSections(Collections.emptyList());
                reqVO.setRiverSectionCount(0);
                riverChannelService.createRiverChannel(reqVO);
                successCount++;
            } catch (Exception ex) {
                respVO.addFailure("第" + excelRowNo + "行失败：" + StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
            }
        }
        respVO.setSuccessCount(successCount);
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private RiverChannelSaveReqVO buildReqVO(RiverChannelImportExcelVO row) {
        RiverChannelSaveReqVO reqVO = new RiverChannelSaveReqVO();
        reqVO.setRiverName(normalizeText(row.getRiverName()));
        reqVO.setLengthKm(parseDecimal(row.getLengthKm()));
        reqVO.setCatchmentKm2(parseDecimal(row.getCatchmentKm2()));
        reqVO.setAverageSlope(parseDecimal(row.getAverageSlope()));
        reqVO.setCentroidLongitude(parseDecimal(row.getCentroidLongitude()));
        reqVO.setCentroidLatitude(parseDecimal(row.getCentroidLatitude()));
        reqVO.setRiverEndLongitude(parseDecimal(row.getRiverEndLongitude()));
        reqVO.setRiverEndLatitude(parseDecimal(row.getRiverEndLatitude()));
        reqVO.setRiverSourceLongitude(parseDecimal(row.getRiverSourceLongitude()));
        reqVO.setRiverSourceLatitude(parseDecimal(row.getRiverSourceLatitude()));
        reqVO.setHistoricalMaxWaterLevel(parseDecimal(row.getHistoricalMaxWaterLevel()));
        reqVO.setMaxWaterLevelDate(parseDateTime(row.getMaxWaterLevelDate()));
        reqVO.setLowestWaterLevelDate(parseDateTime(row.getLowestWaterLevelDate()));
        reqVO.setHistoricalMinWaterLevel(parseDecimal(row.getHistoricalMinWaterLevel()));
        reqVO.setAverageAnnualRunoff(parseDecimal(row.getAverageAnnualRunoff()));
        reqVO.setSourceMountainRange(normalizeText(row.getSourceMountainRange()));
        reqVO.setRiverTerminus(normalizeText(row.getRiverTerminus()));
        reqVO.setRiverEntrance(normalizeText(row.getRiverEntrance()));
        reqVO.setRiverOrigin(normalizeText(row.getRiverOrigin()));
        reqVO.setStartPoint(normalizeText(row.getStartPoint()));
        reqVO.setEndPoint(normalizeText(row.getEndPoint()));
        reqVO.setAssociatedFacilities(normalizeText(row.getAssociatedFacilities()));
        reqVO.setManagementUnit(normalizeText(row.getManagementUnit()));
        reqVO.setResponsibilities(normalizeText(row.getResponsibilities()));
        reqVO.setRemarks(normalizeText(row.getRemarks()));
        return reqVO;
    }

    private boolean isEmptyRow(RiverChannelImportExcelVO row) {
        return StrUtil.isAllBlank(
                normalizeText(row.getRiverName()),
                normalizeText(row.getLengthKm()),
                normalizeText(row.getCatchmentKm2()),
                normalizeText(row.getRemarks())
        );
    }

    private String normalizeText(String text) {
        return StrUtil.trimToNull(text);
    }

    private BigDecimal parseDecimal(String text) {
        String normalized = normalizeText(text);
        if (normalized == null) {
            return null;
        }
        String value = normalized.replace(",", "").replace("，", "");
        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("数值格式错误：" + text);
        }
    }

    private LocalDateTime parseDateTime(String text) {
        String normalized = normalizeText(text);
        if (normalized == null) {
            return null;
        }
        String value = normalized
                .replace("/", "-")
                .replace(".", "-")
                .replace("年", "-")
                .replace("月", "-")
                .replace("日", " ");
        value = value.replaceAll("\\s+", " ").trim();
        value = value.replaceAll("-+", "-");
        try {
            if (value.length() <= 10) {
                LocalDate localDate = LocalDate.parse(value, DATE_FORMATTER);
                return LocalDateTime.of(localDate, LocalTime.MIN);
            }
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (Exception ex) {
            throw new IllegalArgumentException("时间格式错误：" + text);
        }
    }
}
