package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadExcelImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverHeadExcelImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 河长信息 Excel 导入服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverHeadExcelImportService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private final YzRiverChannelMapper riverChannelMapper;
    private final YzRiverChannelManagementMapper managementMapper;
    private final DictDataCommonApi dictDataApi;

    /**
     * 导入河长信息（仅读取 sheet1）
     *
     * <p>匹配规则：</p>
     * <ul>
     *   <li>根据“河道名称”精确匹配河道</li>
     *   <li>河道级别：将河道表 riverLevel 对应字典 zd_hljb 的 label 前两个字符，与 Excel 的“河道级别”前两个字符一致即匹配</li>
     *   <li>河长级别：若 Excel 的“河长级别”包含字典 zd_hzjb 的 label，则写入该字典对应 value</li>
     * </ul>
     */
    @Transactional(rollbackFor = Exception.class)
    public RiverHeadExcelImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<RiverHeadExcelImportExcelVO> rows;
        try {
            // 仅读取第一个 sheet（sheet1）
            rows = ExcelUtils.readSheet(file, RiverHeadExcelImportExcelVO.class, 0);
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        RiverHeadExcelImportRespVO respVO = new RiverHeadExcelImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        // 预加载字典：河道级别、河长级别
        Map<String, String> riverLevelValueToLabel = loadDictValueToLabelMap(ZdConstants.ZD_HLJB);
        List<DictDataRespDTO> headLevelDict = dictDataApi.getDictDataList(ZdConstants.ZD_HZJB);

        // 预加载河道：按河道名称批量查询，避免逐行访问数据库
        Set<String> riverNames = rows.stream()
                .map(RiverHeadExcelImportExcelVO::getRiverName)
                .map(StrUtil::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (riverNames.isEmpty()) {
            respVO.addFailure("Excel 中未读取到任何有效的河道名称");
            return respVO;
        }

        List<YzRiverChannelDO> channelList = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getId, YzRiverChannelDO::getRiverName, YzRiverChannelDO::getRiverLevel)
                .in(YzRiverChannelDO::getRiverName, riverNames));
        Map<String, List<YzRiverChannelDO>> channelByName = channelList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(YzRiverChannelDO::getRiverName));

        // 先完成匹配与校验，避免执行更新/插入后再失败导致回滚难以定位
        Map<Long, List<HeadRow>> headsByChannelId = new HashMap<>();
        Map<Long, String> channelNameById = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            // Excel 第一行是表头，数据行从第 2 行开始
            int excelRowNo = i + 2;
            RiverHeadExcelImportExcelVO row = rows.get(i);
            if (row == null) {
                respVO.addFailure("第" + excelRowNo + "行失败：空行");
                continue;
            }
            String riverName = StrUtil.trim(row.getRiverName());
            String riverLevelText = StrUtil.trim(row.getRiverLevel());
            String headLevelText = StrUtil.trim(row.getHeadLevel());
            String headName = StrUtil.trim(row.getHeadName());
            String headPosition = StrUtil.trim(row.getHeadPosition());
            String headContact = StrUtil.trim(row.getHeadContact());

            if (StrUtil.isBlank(riverName)) {
                respVO.addFailure("第" + excelRowNo + "行失败：河道名称为空");
                continue;
            }
            if (StrUtil.isBlank(riverLevelText)) {
                respVO.addFailure("第" + excelRowNo + "行失败：河道级别为空");
                continue;
            }
            List<YzRiverChannelDO> candidates = channelByName.getOrDefault(riverName, List.of());
            if (candidates.isEmpty()) {
                respVO.addSkip("第" + excelRowNo + "行跳过：未找到河道【" + riverName + "】");
                continue;
            }
            String excelLevelPrefix = prefix2(riverLevelText);
            YzRiverChannelDO matched = candidates.stream()
                    .filter(ch -> StrUtil.isNotBlank(ch.getRiverLevel()))
                    .filter(ch -> StrUtil.equals(prefix2(riverLevelValueToLabel.get(ch.getRiverLevel())), excelLevelPrefix))
                    .sorted(Comparator.comparing(YzRiverChannelDO::getId))
                    .findFirst()
                    .orElse(null);
            if (matched == null) {
                respVO.addSkip("第" + excelRowNo + "行跳过：河道【" + riverName + "】级别不匹配");
                continue;
            }

            String headLevelValue = resolveHeadLevelValue(headLevelText, headLevelDict);
            if (StrUtil.isBlank(headLevelValue)) {
                respVO.addFailure("第" + excelRowNo + "行失败：河长级别【" + StrUtil.blankToDefault(headLevelText, "-") + "】未匹配到字典值");
                continue;
            }
            if (StrUtil.isBlank(headName)) {
                respVO.addFailure("第" + excelRowNo + "行失败：河长姓名为空");
                continue;
            }

            Long channelId = matched.getId();
            channelNameById.putIfAbsent(channelId, matched.getRiverName());
            headsByChannelId.computeIfAbsent(channelId, k -> new ArrayList<>())
                    .add(new HeadRow(headLevelValue, headName, headPosition, StrUtil.isBlank(headContact) ? null : headContact));
        }

        if (headsByChannelId.isEmpty()) {
            return respVO;
        }

        LocalDateTime now = LocalDateTime.now();
        int inserted = 0;
        for (Map.Entry<Long, List<HeadRow>> entry : headsByChannelId.entrySet()) {
            Long channelId = entry.getKey();
            List<HeadRow> headRows = entry.getValue();
            if (channelId == null || CollUtil.isEmpty(headRows)) {
                continue;
            }

            // 旧版本失效（仅处理“河道层级”河长：river_section_id 为空）
            managementMapper.update(null, new LambdaUpdateWrapper<YzRiverChannelManagementDO>()
                    .eq(YzRiverChannelManagementDO::getRiverChannelId, channelId)
                    .eq(YzRiverChannelManagementDO::getIsCurrent, 1)
                    .isNull(YzRiverChannelManagementDO::getRiverSectionId)
                    .set(YzRiverChannelManagementDO::getEffectiveTo, now)
                    .set(YzRiverChannelManagementDO::getIsCurrent, 0));

            Integer nextVersionNo = selectNextVersionNo(channelId);
            String sectionName = channelNameById.get(channelId);
            for (HeadRow head : headRows) {
                YzRiverChannelManagementDO mgmt = new YzRiverChannelManagementDO();
                mgmt.setId(SNOWFLAKE.nextId());
                mgmt.setRiverChannelId(channelId);
                mgmt.setRiverSectionId(null);
                mgmt.setReferenceId(channelId);
                mgmt.setReferenceType(ReferenceTypeConstants.RIVER);
                mgmt.setSectionName(sectionName);
                mgmt.setHeadLevel(head.headLevelValue);
                mgmt.setHeadName(head.headName);
                mgmt.setHeadPosition(head.headPosition);
                if (StrUtil.isNotBlank(head.headContact)) {
                    mgmt.setHeadContact(head.headContact);
                }
                mgmt.setVersionNo(nextVersionNo);
                mgmt.setEffectiveFrom(now);
                mgmt.setEffectiveTo(null);
                mgmt.setIsCurrent(1);
                managementMapper.insert(mgmt);
                inserted++;
            }
        }

        respVO.setSuccessCount(inserted);
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private Map<String, String> loadDictValueToLabelMap(String dictType) {
        List<DictDataRespDTO> list = dictDataApi.getDictDataList(dictType);
        Map<String, String> result = new HashMap<>();
        for (DictDataRespDTO dict : list) {
            if (dict == null || StrUtil.isBlank(dict.getValue())) {
                continue;
            }
            result.put(dict.getValue(), StrUtil.blankToDefault(dict.getLabel(), dict.getValue()));
        }
        return result;
    }

    private String resolveHeadLevelValue(String headLevelText, List<DictDataRespDTO> dict) {
        if (StrUtil.isBlank(headLevelText) || CollUtil.isEmpty(dict)) {
            return null;
        }
        for (DictDataRespDTO item : dict) {
            if (item == null) {
                continue;
            }
            String label = StrUtil.trim(item.getLabel());
            String value = StrUtil.trim(item.getValue());
            if (StrUtil.isBlank(label) || StrUtil.isBlank(value)) {
                continue;
            }
            if (headLevelText.contains(label)) {
                return value;
            }
        }
        return null;
    }

    private String prefix2(String text) {
        String s = StrUtil.trim(text);
        if (StrUtil.isBlank(s)) {
            return "";
        }
        return s.length() <= 2 ? s : s.substring(0, 2);
    }

    /**
     * 计算“河道层级”下一次版本号（riverSectionId 为空）
     */
    private Integer selectNextVersionNo(Long riverChannelId) {
        YzRiverChannelManagementDO max = managementMapper.selectOne(new LambdaQueryWrapper<YzRiverChannelManagementDO>()
                .eq(YzRiverChannelManagementDO::getRiverChannelId, riverChannelId)
                .isNull(YzRiverChannelManagementDO::getRiverSectionId)
                .orderByDesc(YzRiverChannelManagementDO::getVersionNo)
                .last("LIMIT 1"));
        Integer maxNo = (max == null || max.getVersionNo() == null) ? 0 : max.getVersionNo();
        return maxNo + 1;
    }

    private static final class HeadRow {
        private final String headLevelValue;
        private final String headName;
        private final String headPosition;
        private final String headContact;

        private HeadRow(String headLevelValue, String headName, String headPosition, String headContact) {
            this.headLevelValue = headLevelValue;
            this.headName = headName;
            this.headPosition = headPosition;
            this.headContact = headContact;
        }
    }
}
