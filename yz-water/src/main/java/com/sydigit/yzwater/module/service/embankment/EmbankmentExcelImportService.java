package com.sydigit.yzwater.module.service.embankment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.embankment.EmbankmentSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 堤防 Excel 导入服务
 *
 * <p>适配模板《堤防数据260328.xlsx》，导入时始终新增，保留历史同名数据。</p>
 */
@Service
@Validated
@RequiredArgsConstructor
public class EmbankmentExcelImportService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final Pattern FIRST_NUMBER_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");

    private final EmbankmentService embankmentService;
    private final YzRiverChannelMapper riverChannelMapper;
    private final SystemAreaMapper systemAreaMapper;
    private final DictDataCommonApi dictDataApi;

    public EmbankmentImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }
        List<EmbankmentImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), EmbankmentImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(1)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        EmbankmentImportRespVO respVO = new EmbankmentImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        Map<String, Long> riverChannelIdByName = loadRiverChannelIdByName(rows);
        Map<String, String> areaIdByName = loadAreaIdByName(rows);
        Map<String, String> riverBankSideMap = loadDictValueMapByLabel(ZdConstants.ZD_HLAB);
        Map<String, String> embankmentLevelMap = loadDictValueMapByLabel(ZdConstants.ZD_DFJB);

        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            EmbankmentImportExcelVO row = rows.get(i);
            int excelRowNo = i + 2;
            if (row == null || isEmptyRow(row)) {
                continue;
            }
            try {
                String embankmentName = normalizeText(row.getEmbankmentName());
                if (StrUtil.isBlank(embankmentName)) {
                    respVO.addError("第" + excelRowNo + "行失败：堤防名称不能为空");
                    continue;
                }

                ParsedImportRow parsedRow = parseRow(row, areaIdByName, riverChannelIdByName,
                        riverBankSideMap, embankmentLevelMap, excelRowNo);
                EmbankmentSaveReqVO createReq = buildCreateReq(embankmentName, parsedRow);
                embankmentService.createEmbankmentForImport(createReq);
                successCount++;
            } catch (Exception ex) {
                respVO.addError("第" + excelRowNo + "行失败：" + StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
            }
        }
        respVO.setSuccessCount(successCount);
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private Map<String, Long> loadRiverChannelIdByName(List<EmbankmentImportExcelVO> rows) {
        Set<String> riverNames = rows.stream()
                .map(EmbankmentImportExcelVO::getRiverName)
                .map(this::normalizeText)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (CollUtil.isEmpty(riverNames)) {
            return Map.of();
        }
        List<YzRiverChannelDO> riverChannels = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName)
                .eq(YzRiverChannelDO::getDeleted, 0)
                .in(YzRiverChannelDO::getRiverName, riverNames));
        Map<String, Long> result = new HashMap<>();
        for (YzRiverChannelDO riverChannel : riverChannels) {
            if (riverChannel == null || riverChannel.getId() == null) {
                continue;
            }
            String riverName = normalizeText(riverChannel.getRiverName());
            if (StrUtil.isBlank(riverName)) {
                continue;
            }
            result.putIfAbsent(riverName, riverChannel.getId());
        }
        return result;
    }

    private Map<String, String> loadAreaIdByName(List<EmbankmentImportExcelVO> rows) {
        List<String> nameList = rows.stream()
                .map(EmbankmentImportExcelVO::getDivisionName)
                .map(this::normalizeText)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(nameList)) {
            return Map.of();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectByNames(nameList);
        Map<String, String> result = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String areaName = normalizeText(area.getName());
            if (StrUtil.isBlank(areaName)) {
                continue;
            }
            result.putIfAbsent(areaName, String.valueOf(area.getId()));
        }
        return result;
    }

    private Map<String, String> loadDictValueMapByLabel(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        if (CollUtil.isEmpty(list)) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO dictData : list) {
            if (dictData == null) {
                continue;
            }
            String value = normalizeText(dictData.getValue());
            if (StrUtil.isBlank(value)) {
                continue;
            }
            String label = normalizeText(dictData.getLabel());
            if (StrUtil.isNotBlank(label)) {
                result.putIfAbsent(label, value);
            }
            result.putIfAbsent(value, value);
        }
        return result;
    }

    private ParsedImportRow parseRow(EmbankmentImportExcelVO row,
                                     Map<String, String> areaIdByName,
                                     Map<String, Long> riverChannelIdByName,
                                     Map<String, String> riverBankSideMap,
                                     Map<String, String> embankmentLevelMap,
                                     int excelRowNo) {
        ParsedImportRow parsedRow = new ParsedImportRow();
        parsedRow.setDivisionCode(resolveAreaId(areaIdByName, row.getDivisionName(), "所在行政区", excelRowNo));
        parsedRow.setRiverChannelId(resolveRiverChannelId(riverChannelIdByName, row.getRiverName(), excelRowNo));
        parsedRow.setRiverBankSide(resolveDictValue(riverBankSideMap, row.getRiverBankSide(), "所在岸别", excelRowNo));
        parsedRow.setEmbankmentLevel(resolveDictValue(embankmentLevelMap, row.getEmbankmentLevel(), "堤防等级", excelRowNo));
        parsedRow.setFloodStandard(normalizeText(row.getFloodStandard()));
        parsedRow.setLengthM(parseDecimal(row.getLengthM(), "堤防长度", excelRowNo));
        parsedRow.setDesignHighTide(normalizeText(row.getDesignHighTide()));
        parsedRow.setCrestElevation(parseDecimalOrFirstNumber(row.getCrestElevation(), "堤顶高程", excelRowNo));
        parsedRow.setStartPoint(normalizeText(row.getStartPoint()));
        parsedRow.setEndPoint(normalizeText(row.getEndPoint()));
        return parsedRow;
    }

    private EmbankmentSaveReqVO buildCreateReq(String embankmentName, ParsedImportRow parsedRow) {
        EmbankmentSaveReqVO reqVO = new EmbankmentSaveReqVO();
        reqVO.setEmbankmentCode(String.valueOf(SNOWFLAKE.nextId()));
        reqVO.setEmbankmentName(embankmentName);
        if (StrUtil.isNotBlank(parsedRow.getDivisionCode())) {
            reqVO.setDivisionCode(List.of(parsedRow.getDivisionCode()));
        }
        reqVO.setRiverChannelId(parsedRow.getRiverChannelId());
        reqVO.setRiverBankSide(parsedRow.getRiverBankSide());
        reqVO.setEmbankmentLevel(parsedRow.getEmbankmentLevel());
        reqVO.setFloodStandard(parsedRow.getFloodStandard());
        reqVO.setLengthM(parsedRow.getLengthM());
        reqVO.setDesignHighTide(parsedRow.getDesignHighTide());
        reqVO.setCrestElevation(parsedRow.getCrestElevation());
        reqVO.setStartPoint(parsedRow.getStartPoint());
        reqVO.setEndPoint(parsedRow.getEndPoint());
        return reqVO;
    }

    private boolean isEmptyRow(EmbankmentImportExcelVO row) {
        return StrUtil.isAllBlank(
                normalizeText(row.getEmbankmentName()),
                normalizeText(row.getDivisionName()),
                normalizeText(row.getRiverName()),
                normalizeText(row.getRiverBankSide()),
                normalizeText(row.getEmbankmentLevel()),
                normalizeText(row.getFloodStandard()),
                normalizeText(row.getLengthM()),
                normalizeText(row.getDesignHighTide()),
                normalizeText(row.getCrestElevation()),
                normalizeText(row.getStartPoint()),
                normalizeText(row.getEndPoint())
        );
    }

    private String normalizeText(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        normalized = StrUtil.removePrefix(normalized, "\"");
        normalized = StrUtil.removeSuffix(normalized, "\"");
        normalized = StrUtil.removePrefix(normalized, "'");
        normalized = StrUtil.removeSuffix(normalized, "'");
        normalized = StrUtil.trimToNull(normalized);
        if (normalized == null) {
            return null;
        }
        if ("-".equals(normalized) || "/".equals(normalized) || "无".equals(normalized) || "空".equals(normalized)) {
            return null;
        }
        return normalized;
    }

    private String resolveAreaId(Map<String, String> areaIdByName, String raw, String columnName, int excelRowNo) {
        String areaName = normalizeText(raw);
        if (StrUtil.isBlank(areaName)) {
            return null;
        }
        String areaId = areaIdByName.get(areaName);
        if (StrUtil.isBlank(areaId)) {
            throw new IllegalArgumentException(columnName + "未匹配行政区（第 " + excelRowNo + " 行）：" + areaName);
        }
        return areaId;
    }

    private Long resolveRiverChannelId(Map<String, Long> riverChannelIdByName, String raw, int excelRowNo) {
        String riverName = normalizeText(raw);
        if (StrUtil.isBlank(riverName)) {
            return null;
        }
        Long riverChannelId = riverChannelIdByName.get(riverName);
        return riverChannelId;
    }

    private String resolveDictValue(Map<String, String> dictMap, String raw, String columnName, int excelRowNo) {
        String label = normalizeText(raw);
        if (StrUtil.isBlank(label)) {
            return null;
        }
        String value = dictMap.get(label);
        if (StrUtil.isBlank(value)) {
            throw new IllegalArgumentException(columnName + "未匹配字典值（第 " + excelRowNo + " 行）：" + label);
        }
        return value;
    }

    private BigDecimal parseDecimal(String text, String columnName, int excelRowNo) {
        String normalized = normalizeText(text);
        if (normalized == null) {
            return null;
        }
        String value = normalized.replace(",", "").replace("，", "");
        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException(columnName + "数值格式错误（第 " + excelRowNo + " 行）：" + text);
        }
    }

    private BigDecimal parseDecimalOrFirstNumber(String text, String columnName, int excelRowNo) {
        String normalized = normalizeText(text);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized.replace(",", "").replace("，", ""));
        } catch (Exception ignore) {
            // 兼容模板样例中的区间值（如 9.56~9.74），当前字段仍是数值型，只能取首个数值入库。
        }
        Matcher matcher = FIRST_NUMBER_PATTERN.matcher(normalized);
        if (matcher.find()) {
            return new BigDecimal(matcher.group());
        }
        throw new IllegalArgumentException(columnName + "数值格式错误（第 " + excelRowNo + " 行）：" + text);
    }

    @lombok.Data
    private static class ParsedImportRow {
        private String divisionCode;
        private Long riverChannelId;
        private String riverBankSide;
        private String embankmentLevel;
        private String floodStandard;
        private BigDecimal lengthM;
        private String designHighTide;
        private BigDecimal crestElevation;
        private String startPoint;
        private String endPoint;
    }
}
