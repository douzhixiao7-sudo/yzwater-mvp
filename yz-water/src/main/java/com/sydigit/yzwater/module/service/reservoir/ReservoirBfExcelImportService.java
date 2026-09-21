package com.sydigit.yzwater.module.service.reservoir;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.reservoir.ReservoirYzBasicInfoImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.ReservoirImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirBfDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirBfMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 水库 Excel 导入服务（按名称匹配更新）
 *
 * <p>仅用于 /reservoir/import-excel，以“扬州仪征中小型水库基本资料表（0326）”列结构为准。</p>
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReservoirBfExcelImportService {

    private final DictDataCommonApi dictDataCommonApi;
    private final YzWaterReservoirBfMapper reservoirMapper;
    private final SystemAreaMapper systemAreaMapper;

    public ReservoirImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<ReservoirYzBasicInfoImportExcelVO> rows;
        try {
            // 第 1 行标题，第 2 行表头，第 3 行起数据
            rows = FastExcelFactory.read(file.getInputStream(), ReservoirYzBasicInfoImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(2)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        ReservoirImportRespVO respVO = new ReservoirImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        Set<String> reservoirNames = rows.stream()
                .map(ReservoirYzBasicInfoImportExcelVO::getReservoirName)
                .map(this::normalizeText)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(reservoirNames)) {
            respVO.addError("Excel 中未读取到有效的水库名称");
            return respVO;
        }

        Map<String, List<YzWaterReservoirBfDO>> reservoirByName = loadReservoirByName(reservoirNames);
        Map<String, String> scaleMap = loadDictValueMapByLabel(ZdConstants.ZD_SKGM);
        Map<String, String> managementUnitMap = loadDictValueMapByLabel(ZdConstants.ZD_GLDW);
        Map<String, String> natureMap = loadDictValueMapByLabel(ZdConstants.ZD_SKXZ);
        Map<String, String> areaIdByName = loadAreaIdMapByName(rows);

        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            ReservoirYzBasicInfoImportExcelVO row = rows.get(i);
            int excelRowNo = i + 3;
            if (row == null || isEmptyRow(row)) {
                continue;
            }

            try {
                String reservoirName = normalizeText(row.getReservoirName());
                if (StrUtil.isBlank(reservoirName)) {
                    respVO.addError("第 " + excelRowNo + " 行失败：水库名称不能为空");
                    continue;
                }

                List<YzWaterReservoirBfDO> matchedReservoirs = reservoirByName.getOrDefault(reservoirName, List.of());
                if (CollUtil.isEmpty(matchedReservoirs)) {
                    respVO.addError("第 " + excelRowNo + " 行失败：未找到水库【" + reservoirName + "】");
                    continue;
                }

                String reservoirScale = resolveDictValue(scaleMap, row.getReservoirScale(), "规模", excelRowNo);
                String[] township = resolveAreaIds(row.getTownship(), areaIdByName, "所在乡镇", excelRowNo);
                String[] managementUnit = resolveDictValues(managementUnitMap, row.getManagementUnit(), "管理单位", excelRowNo);
                String reservoirNature = resolveDictValue(natureMap, row.getReservoirNature(), "水库性质", excelRowNo);

                BigDecimal catchmentArea = parseDecimal(row.getCatchmentArea(), "集水面积", excelRowNo);
                BigDecimal totalCapacity = parseDecimal(row.getTotalCapacity(), "总库容", excelRowNo);
                BigDecimal activeCapacity = parseDecimal(row.getActiveCapacity(), "兴利库容", excelRowNo);
                BigDecimal verifiedFloodLevel = parseDecimal(row.getVerifiedFloodLevel(), "校核水位", excelRowNo);
                BigDecimal designFloodLevel = parseDecimal(row.getDesignFloodLevel(), "设计水位", excelRowNo);
                BigDecimal normalOperatingLevel = parseDecimal(row.getNormalOperatingLevel(), "兴利水位", excelRowNo);
                BigDecimal floodLimitLevel = parseDecimal(row.getFloodLimitLevel(), "汛限水位", excelRowNo);
                BigDecimal maxDamHeight = parseDecimal(row.getMaxDamHeight(), "最大坝高", excelRowNo);
                String damCrestElevation = normalizeText(row.getDamCrestElevation());
                String damTopWidth = normalizeText(row.getDamTopWidth());
                String damTopLength = normalizeText(row.getDamTopLength());

                int rowUpdated = 0;
                for (YzWaterReservoirBfDO reservoir : matchedReservoirs) {
                    if (reservoir == null || reservoir.getId() == null) {
                        continue;
                    }
                    rowUpdated += reservoirMapper.updateImportFieldsById(
                            reservoir.getId(),
                            reservoirScale,
                            township,
                            managementUnit,
                            reservoirNature,
                            catchmentArea,
                            totalCapacity,
                            activeCapacity,
                            verifiedFloodLevel,
                            designFloodLevel,
                            normalOperatingLevel,
                            floodLimitLevel,
                            // 第13列“汛限水位”需同步到两个字段；空值时也一并置空
                            floodLimitLevel,
                            damCrestElevation,
                            damTopWidth,
                            maxDamHeight,
                            damTopLength
                    );
                }

                if (rowUpdated <= 0) {
                    respVO.addError("第 " + excelRowNo + " 行失败：未更新到任何水库记录");
                    continue;
                }
                successCount += rowUpdated;
            } catch (Exception ex) {
                respVO.addError("第 " + excelRowNo + " 行失败：" + StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
            }
        }

        respVO.setSuccessCount(successCount);
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private Map<String, List<YzWaterReservoirBfDO>> loadReservoirByName(Set<String> reservoirNames) {
        if (CollUtil.isEmpty(reservoirNames)) {
            return Map.of();
        }
        List<YzWaterReservoirBfDO> reservoirs = reservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .select(YzWaterReservoirBfDO::getId, YzWaterReservoirBfDO::getReservoirName)
                .eq(YzWaterReservoirBfDO::getDeleted, 0)
                .in(YzWaterReservoirBfDO::getReservoirName, reservoirNames));
        return reservoirs.stream()
                .filter(Objects::nonNull)
                .filter(it -> StrUtil.isNotBlank(it.getReservoirName()))
                .collect(Collectors.groupingBy(it -> normalizeText(it.getReservoirName())));
    }

    private Map<String, String> loadAreaIdMapByName(List<ReservoirYzBasicInfoImportExcelVO> rows) {
        Set<String> areaNames = new LinkedHashSet<>();
        for (ReservoirYzBasicInfoImportExcelVO row : rows) {
            if (row == null) {
                continue;
            }
            areaNames.addAll(splitMultiValues(row.getTownship()));
        }
        if (CollUtil.isEmpty(areaNames)) {
            return Map.of();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectByNames(new ArrayList<>(areaNames));
        Map<String, String> result = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String name = normalizeText(area.getName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            result.putIfAbsent(name, String.valueOf(area.getId()));
        }
        return result;
    }

    private Map<String, String> loadDictValueMapByLabel(String dictType) {
        List<DictDataRespDTO> list = dictDataCommonApi.getDictDataList(dictType);
        if (CollUtil.isEmpty(list)) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO item : list) {
            String value = normalizeText(item == null ? null : item.getValue());
            if (StrUtil.isBlank(value)) {
                continue;
            }
            String label = normalizeText(item.getLabel());
            if (StrUtil.isNotBlank(label)) {
                result.putIfAbsent(label, value);
            }
            // 兼容 Excel 直接填写字典 value 的场景
            result.putIfAbsent(value, value);
        }
        return result;
    }

    private String resolveDictValue(Map<String, String> dictMap, String raw, String columnName, int excelRowNo) {
        String normalized = normalizeText(raw);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        String value = dictMap.get(normalized);
        if (StrUtil.isBlank(value)) {
            throw new IllegalArgumentException(columnName + "未匹配字典值（第 " + excelRowNo + " 行）：" + normalized);
        }
        return value;
    }

    private String[] resolveDictValues(Map<String, String> dictMap, String raw, String columnName, int excelRowNo) {
        List<String> labels = splitMultiValues(raw);
        if (CollUtil.isEmpty(labels)) {
            return null;
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String label : labels) {
            String value = dictMap.get(label);
            if (StrUtil.isBlank(value)) {
                throw new IllegalArgumentException(columnName + "未匹配字典值（第 " + excelRowNo + " 行）：" + label);
            }
            values.add(value);
        }
        return values.isEmpty() ? null : values.toArray(new String[0]);
    }

    private String[] resolveAreaIds(String raw, Map<String, String> areaIdByName, String columnName, int excelRowNo) {
        List<String> areaNames = splitMultiValues(raw);
        if (CollUtil.isEmpty(areaNames)) {
            return null;
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (String areaName : areaNames) {
            String areaId = areaIdByName.get(areaName);
            if (StrUtil.isBlank(areaId)) {
                throw new IllegalArgumentException(columnName + "未匹配行政区（第 " + excelRowNo + " 行）：" + areaName);
            }
            ids.add(areaId);
        }
        return ids.isEmpty() ? null : ids.toArray(new String[0]);
    }

    private List<String> splitMultiValues(String raw) {
        String normalized = normalizeText(raw);
        if (StrUtil.isBlank(normalized)) {
            return List.of();
        }
        String[] parts = normalized.split("[,，;；、/|]");
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String part : parts) {
            String item = normalizeText(part);
            if (StrUtil.isNotBlank(item)) {
                result.add(item);
            }
        }
        if (result.isEmpty()) {
            return List.of(normalized);
        }
        return new ArrayList<>(result);
    }

    private boolean isEmptyRow(ReservoirYzBasicInfoImportExcelVO row) {
        return StrUtil.isAllBlank(
                normalizeText(row.getReservoirName()),
                normalizeText(row.getReservoirScale()),
                normalizeText(row.getTownship()),
                normalizeText(row.getManagementUnit()),
                normalizeText(row.getReservoirNature()),
                normalizeText(row.getCatchmentArea()),
                normalizeText(row.getTotalCapacity()),
                normalizeText(row.getActiveCapacity()),
                normalizeText(row.getVerifiedFloodLevel()),
                normalizeText(row.getDesignFloodLevel()),
                normalizeText(row.getNormalOperatingLevel()),
                normalizeText(row.getFloodLimitLevel()),
                normalizeText(row.getDamCrestElevation()),
                normalizeText(row.getDamTopWidth()),
                normalizeText(row.getMaxDamHeight()),
                normalizeText(row.getDamTopLength())
        );
    }

    private String normalizeText(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() >= 2) {
            if ((normalized.startsWith("{") && normalized.endsWith("}"))
                    || (normalized.startsWith("[") && normalized.endsWith("]"))) {
                normalized = StrUtil.trimToNull(normalized.substring(1, normalized.length() - 1));
            }
        }
        normalized = StrUtil.removePrefix(normalized, "\"");
        normalized = StrUtil.removeSuffix(normalized, "\"");
        normalized = StrUtil.removePrefix(normalized, "'");
        normalized = StrUtil.removeSuffix(normalized, "'");
        normalized = StrUtil.trimToNull(normalized);
        if (normalized == null) {
            return null;
        }
        // 常见占位符视为“未提供”，按当前导入规则会写入 null（置空）
        if (normalized.matches("^[/\\\\\\-—–_]+$")) {
            return null;
        }
        if (StrUtil.equalsAnyIgnoreCase(normalized, "null", "n/a", "na")) {
            return null;
        }
        return normalized;
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
}
