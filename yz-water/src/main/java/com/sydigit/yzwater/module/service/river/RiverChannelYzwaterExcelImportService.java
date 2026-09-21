package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelYzwaterExcelImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelYzwaterExcelImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * yzwater.xlsx 河道信息导入服务（仅更新，不新增）
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChannelYzwaterExcelImportService {

    private static final String ECOLOGY_YES_VALUE = "sthd";
    private static final String ECOLOGY_NO_VALUE = "bfsthd";

    private final YzRiverChannelMapper riverChannelMapper;
    private final DictDataCommonApi dictDataApi;
    private final SystemAreaMapper systemAreaMapper;

    @Transactional(rollbackFor = Exception.class)
    public RiverChannelYzwaterExcelImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<RiverChannelYzwaterExcelImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), RiverChannelYzwaterExcelImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(1)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        RiverChannelYzwaterExcelImportRespVO respVO = new RiverChannelYzwaterExcelImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        Set<String> riverNames = rows.stream()
                .map(RiverChannelYzwaterExcelImportExcelVO::getRiverName)
                .map(this::normalizeText)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(riverNames)) {
            respVO.addFailure("Excel 中未读取到任何有效的河道名称");
            return respVO;
        }

        Map<String, List<YzRiverChannelDO>> channelByName = loadRiverChannels(riverNames);
        Map<String, String> riverLevelLabelToValue = loadRiverLevelLabelToValueMap();
        List<AreaCandidate> areaCandidates = loadAreaCandidates();
        Map<String, String> areaIdByExactName = areaCandidates.stream()
                .collect(Collectors.toMap(
                        it -> it.name,
                        it -> it.id,
                        (a, b) -> a,
                        HashMap::new
                ));

        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            int excelRowNo = i + 2;
            RiverChannelYzwaterExcelImportExcelVO row = rows.get(i);
            if (row == null) {
                respVO.addFailure("第" + excelRowNo + "行失败：空行");
                continue;
            }

            String riverName = normalizeText(row.getRiverName());
            if (StrUtil.isBlank(riverName)) {
                respVO.addFailure("第" + excelRowNo + "行失败：河道名称为空");
                continue;
            }

            List<YzRiverChannelDO> matchedChannels = channelByName.getOrDefault(riverName, List.of());
            if (CollUtil.isEmpty(matchedChannels)) {
                respVO.addSkip("第" + excelRowNo + "行跳过：未找到河道【" + riverName + "】");
                continue;
            }

            BigDecimal catchmentKm2 = parseDecimal(row.getCatchmentKm2());
            if (StrUtil.isNotBlank(normalizeText(row.getCatchmentKm2())) && catchmentKm2 == null) {
                respVO.addFailure("第" + excelRowNo + "行失败：流域面积格式错误【" + row.getCatchmentKm2() + "】");
                continue;
            }

            BigDecimal lengthKm = parseDecimal(row.getLengthKm());
            if (StrUtil.isNotBlank(normalizeText(row.getLengthKm())) && lengthKm == null) {
                respVO.addFailure("第" + excelRowNo + "行失败：长度格式错误【" + row.getLengthKm() + "】");
                continue;
            }

            String riverLevelValue = resolveRiverLevelValue(riverLevelLabelToValue, row.getRiverLevel());
            String townId = resolveAreaIdByExactName(areaIdByExactName, row.getTown());
            String ecologyTypeValue = resolveEcologyTypeValue(row.getEcologyType());
            String[] flowAreaIds = resolveFlowAreaIds(areaCandidates, row.getFlowAreas());
            String managementUnit = normalizeText(row.getManagementUnit());

            boolean hasAnyUpdateField = StrUtil.isNotBlank(riverLevelValue)
                    || StrUtil.isNotBlank(townId)
                    || StrUtil.isNotBlank(ecologyTypeValue)
                    || (flowAreaIds != null && flowAreaIds.length > 0)
                    || catchmentKm2 != null
                    || lengthKm != null
                    || StrUtil.isNotBlank(managementUnit);
            if (!hasAnyUpdateField) {
                respVO.addFailure("第" + excelRowNo + "行失败：未解析到可更新字段");
                continue;
            }

            int rowUpdated = 0;
            for (YzRiverChannelDO channel : matchedChannels) {
                if (channel == null || channel.getId() == null) {
                    continue;
                }
                LambdaUpdateWrapper<YzRiverChannelDO> updateWrapper = new LambdaUpdateWrapper<YzRiverChannelDO>()
                        .eq(YzRiverChannelDO::getId, channel.getId());
                if (StrUtil.isNotBlank(riverLevelValue)) {
                    updateWrapper.set(YzRiverChannelDO::getRiverLevel, riverLevelValue);
                }
                if (StrUtil.isNotBlank(townId)) {
                    updateWrapper.set(YzRiverChannelDO::getTown, new String[]{townId});
                }
                if (StrUtil.isNotBlank(ecologyTypeValue)) {
                    updateWrapper.set(YzRiverChannelDO::getEcologyType, ecologyTypeValue);
                }
                if (flowAreaIds != null && flowAreaIds.length > 0) {
                    updateWrapper.set(YzRiverChannelDO::getFlowAreas, flowAreaIds);
                }
                if (catchmentKm2 != null) {
                    updateWrapper.set(YzRiverChannelDO::getCatchmentKm2, catchmentKm2);
                }
                if (lengthKm != null) {
                    updateWrapper.set(YzRiverChannelDO::getLengthKm, lengthKm);
                }
                if (StrUtil.isNotBlank(managementUnit)) {
                    updateWrapper.set(YzRiverChannelDO::getManagementUnit, managementUnit);
                }
                rowUpdated += riverChannelMapper.update(null, updateWrapper);
            }

            if (rowUpdated <= 0) {
                respVO.addFailure("第" + excelRowNo + "行失败：未更新到任何河道记录");
                continue;
            }
            successCount += rowUpdated;
        }

        respVO.setSuccessCount(successCount);
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private Map<String, List<YzRiverChannelDO>> loadRiverChannels(Set<String> riverNames) {
        List<YzRiverChannelDO> channels = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName)
                .eq(YzRiverChannelDO::getDeleted, 0)
                .in(YzRiverChannelDO::getRiverName, riverNames));
        return channels.stream()
                .filter(Objects::nonNull)
                .filter(it -> StrUtil.isNotBlank(it.getRiverName()))
                .collect(Collectors.groupingBy(YzRiverChannelDO::getRiverName));
    }

    private Map<String, String> loadRiverLevelLabelToValueMap() {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(ZdConstants.ZD_HLJB);
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO item : list) {
            if (item == null || StrUtil.isBlank(item.getValue())) {
                continue;
            }
            String label = normalizeText(item.getLabel());
            if (StrUtil.isBlank(label)) {
                continue;
            }
            result.putIfAbsent(label, item.getValue());
        }
        return result;
    }

    private List<AreaCandidate> loadAreaCandidates() {
        List<SystemAreaDO> areas = systemAreaMapper.selectList(new LambdaQueryWrapper<SystemAreaDO>()
                .select(SystemAreaDO::getId, SystemAreaDO::getName)
                .eq(SystemAreaDO::getDeleted, 0)
                .orderByAsc(SystemAreaDO::getId));
        List<AreaCandidate> result = new ArrayList<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String name = normalizeText(area.getName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            result.add(new AreaCandidate(String.valueOf(area.getId()), name));
        }
        return result;
    }

    private String resolveRiverLevelValue(Map<String, String> levelMap, String levelLabelText) {
        String normalized = normalizeText(levelLabelText);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        String exact = levelMap.get(normalized);
        if (StrUtil.isNotBlank(exact)) {
            return exact;
        }
        for (Map.Entry<String, String> entry : levelMap.entrySet()) {
            String label = entry.getKey();
            if (StrUtil.isBlank(label) || StrUtil.isBlank(entry.getValue())) {
                continue;
            }
            if (StrUtil.contains(label, normalized) || StrUtil.contains(normalized, label)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String resolveAreaIdByExactName(Map<String, String> areaIdByName, String areaNameText) {
        String normalized = normalizeText(areaNameText);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        return areaIdByName.get(normalized);
    }

    private String resolveEcologyTypeValue(String ecologyText) {
        String normalized = normalizeText(ecologyText);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        if (StrUtil.equalsAny(normalized, "是", "Y", "y", "yes", "YES")) {
            return ECOLOGY_YES_VALUE;
        }
        if (StrUtil.equalsAny(normalized, "否", "N", "n", "no", "NO")) {
            return ECOLOGY_NO_VALUE;
        }
        return null;
    }

    private String[] resolveFlowAreaIds(List<AreaCandidate> areas, String flowAreasText) {
        List<String> names = splitAreaNames(flowAreasText);
        if (CollUtil.isEmpty(names) || CollUtil.isEmpty(areas)) {
            return null;
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (String name : names) {
            String areaId = matchAreaIdByFirstThree(areas, name);
            if (StrUtil.isNotBlank(areaId)) {
                ids.add(areaId);
            }
        }
        return ids.isEmpty() ? null : ids.toArray(new String[0]);
    }

    private String matchAreaIdByFirstThree(List<AreaCandidate> areas, String sourceName) {
        String normalized = normalizeText(sourceName);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        for (AreaCandidate area : areas) {
            if (isFirstThreeMatched(normalized, area.name)) {
                return area.id;
            }
        }
        return null;
    }

    private boolean isFirstThreeMatched(String left, String right) {
        int compareLen = Math.min(3, Math.min(left.length(), right.length()));
        if (compareLen <= 0) {
            return false;
        }
        return StrUtil.equals(left.substring(0, compareLen), right.substring(0, compareLen));
    }

    private List<String> splitAreaNames(String text) {
        String raw = normalizeText(text);
        if (StrUtil.isBlank(raw)) {
            return List.of();
        }
        String[] parts = raw.split("[、,，;；]");
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String part : parts) {
            String normalized = normalizeText(part);
            if (StrUtil.isNotBlank(normalized)) {
                set.add(normalized);
            }
        }
        if (set.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(set);
    }

    private BigDecimal parseDecimal(String text) {
        String normalized = normalizeText(text);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        String numeric = normalized.replace(",", "").replace("，", "");
        try {
            return new BigDecimal(numeric);
        } catch (NumberFormatException ex) {
            return null;
        }
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
        return StrUtil.trimToNull(normalized);
    }

    private static final class AreaCandidate {
        private final String id;
        private final String name;

        private AreaCandidate(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
