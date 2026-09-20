package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelAreaExcelImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelAreaExcelImportRespVO;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 河道流经地区/乡镇 Excel 导入服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChannelAreaExcelImportService {

    private final YzRiverChannelMapper riverChannelMapper;
    private final SystemAreaMapper systemAreaMapper;

    /**
     * 导入河道流经地区/乡镇（仅读取 sheet1，从第 0 行开始）
     */
    @Transactional(rollbackFor = Exception.class)
    public RiverChannelAreaExcelImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<RiverChannelAreaExcelImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), RiverChannelAreaExcelImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(0)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        RiverChannelAreaExcelImportRespVO respVO = new RiverChannelAreaExcelImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        List<ImportRow> validRows = new ArrayList<>();
        Set<String> riverNames = new HashSet<>();
        Set<String> areaNames = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            int excelRowNo = i + 1;
            RiverChannelAreaExcelImportExcelVO row = rows.get(i);
            if (row == null) {
                respVO.addFailure("第" + excelRowNo + "行失败：空行");
                continue;
            }
            String riverName = StrUtil.trim(row.getRiverName());
            String areaText = StrUtil.trim(row.getAreaName());
            if (StrUtil.isBlank(riverName)) {
                respVO.addFailure("第" + excelRowNo + "行失败：河道名称为空");
                continue;
            }
            List<String> splitAreas = splitAreaNames(areaText);
            if (splitAreas.isEmpty()) {
                respVO.addFailure("第" + excelRowNo + "行失败：流经地区/乡镇为空");
                continue;
            }
            validRows.add(new ImportRow(excelRowNo, riverName, splitAreas));
            riverNames.add(riverName);
            areaNames.addAll(splitAreas);
        }

        if (validRows.isEmpty()) {
            return respVO;
        }

        Map<String, List<YzRiverChannelDO>> channelByName = loadRiverChannels(riverNames);
        Map<Long, YzRiverChannelDO> channelById = channelByName.values().stream()
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(YzRiverChannelDO::getId, item -> item, (a, b) -> a));
        Map<String, String> areaNameToId = loadAreaIdMapByName(areaNames);

        Map<Long, LinkedHashSet<String>> areaIdsByChannelId = new HashMap<>();
        for (ImportRow row : validRows) {
            List<YzRiverChannelDO> channels = channelByName.getOrDefault(row.riverName, List.of());
            if (channels.isEmpty()) {
                respVO.addSkip("第" + row.excelRowNo + "行跳过：未找到河道【" + row.riverName + "】");
                continue;
            }
            List<String> matchedIds = new ArrayList<>();
            for (String areaName : row.areaNames) {
                String normalizedName = normalizeAreaName(areaName);
                String areaId = areaNameToId.get(normalizedName);
                if (StrUtil.isBlank(areaId)) {
                    respVO.addFailure("第" + row.excelRowNo + "行失败：区划【" + areaName + "】未匹配到编码");
                    continue;
                }
                matchedIds.add(areaId);
            }
            if (matchedIds.isEmpty()) {
                respVO.addFailure("第" + row.excelRowNo + "行失败：未匹配到任何有效区划");
                continue;
            }
            for (YzRiverChannelDO channel : channels) {
                if (channel == null || channel.getId() == null) {
                    continue;
                }
                LinkedHashSet<String> set = areaIdsByChannelId
                        .computeIfAbsent(channel.getId(), k -> new LinkedHashSet<>());
                set.addAll(matchedIds);
            }
        }

        int updated = 0;
        for (Map.Entry<Long, LinkedHashSet<String>> entry : areaIdsByChannelId.entrySet()) {
            Long channelId = entry.getKey();
            LinkedHashSet<String> incoming = entry.getValue();
            YzRiverChannelDO channel = channelById.get(channelId);
            if (channel == null) {
                continue;
            }
            String[] newFlowAreas = mergeArray(channel.getFlowAreas(), incoming);
            String[] newTown = mergeArray(channel.getTown(), incoming);
            if (Arrays.equals(channel.getFlowAreas(), newFlowAreas)
                    && Arrays.equals(channel.getTown(), newTown)) {
                continue;
            }
            riverChannelMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelDO>()
                    .eq(YzRiverChannelDO::getId, channelId)
                    .set(YzRiverChannelDO::getFlowAreas, newFlowAreas)
                    .set(YzRiverChannelDO::getTown, newTown));
            updated++;
        }
        respVO.setSuccessCount(updated);
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private Map<String, List<YzRiverChannelDO>> loadRiverChannels(Set<String> riverNames) {
        if (CollUtil.isEmpty(riverNames)) {
            return Map.of();
        }
        List<YzRiverChannelDO> channelList = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName,
                        YzRiverChannelDO::getFlowAreas, YzRiverChannelDO::getTown)
                .eq(YzRiverChannelDO::getDeleted, 0)
                .in(YzRiverChannelDO::getRiverName, riverNames));
        return channelList.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(item.getRiverName()))
                .collect(Collectors.groupingBy(YzRiverChannelDO::getRiverName));
    }

    private Map<String, String> loadAreaIdMapByName(Set<String> names) {
        if (CollUtil.isEmpty(names)) {
            return Map.of();
        }
        List<String> nameList = names.stream()
                .map(this::normalizeAreaName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (nameList.isEmpty()) {
            return Map.of();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectByNames(nameList);
        Map<String, String> result = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String name = normalizeAreaName(area.getName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            result.putIfAbsent(name, String.valueOf(area.getId()));
        }
        return result;
    }

    private List<String> splitAreaNames(String text) {
        String raw = StrUtil.trim(text);
        if (StrUtil.isBlank(raw)) {
            return List.of();
        }
        String[] parts = raw.split("[,，;；、/]");
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String part : parts) {
            String name = normalizeAreaName(part);
            if (StrUtil.isNotBlank(name)) {
                set.add(name);
            }
        }
        if (set.isEmpty()) {
            String name = normalizeAreaName(raw);
            if (StrUtil.isNotBlank(name)) {
                set.add(name);
            }
        }
        return new ArrayList<>(set);
    }

    private String normalizeAreaName(String text) {
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

    private String[] mergeArray(String[] origin, LinkedHashSet<String> incoming) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (origin != null) {
            for (String item : origin) {
                String normalized = StrUtil.trimToNull(item);
                if (StrUtil.isNotBlank(normalized)) {
                    merged.add(normalized);
                }
            }
        }
        if (incoming != null) {
            for (String item : incoming) {
                String normalized = StrUtil.trimToNull(item);
                if (StrUtil.isNotBlank(normalized)) {
                    merged.add(normalized);
                }
            }
        }
        return merged.isEmpty() ? null : merged.toArray(new String[0]);
    }

    private static final class ImportRow {
        private final int excelRowNo;
        private final String riverName;
        private final List<String> areaNames;

        private ImportRow(int excelRowNo, String riverName, List<String> areaNames) {
            this.excelRowNo = excelRowNo;
            this.riverName = riverName;
            this.areaNames = areaNames;
        }
    }
}
