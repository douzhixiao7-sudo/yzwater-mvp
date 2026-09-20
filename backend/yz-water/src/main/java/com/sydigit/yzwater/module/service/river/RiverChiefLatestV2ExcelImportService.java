package com.sydigit.yzwater.module.service.river;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefLatestImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefLatestV2ImportExcelVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverSectionDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverSectionMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 河长2 Excel 导入服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChiefLatestV2ExcelImportService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private static final String HEAD_LEVEL_PROVINCIAL = "provincial";
    private static final String HEAD_LEVEL_CITY = "city";
    private static final String HEAD_LEVEL_COUNTY = "county";
    private static final String HEAD_LEVEL_TOWNSHIP = "township";
    private static final String HEAD_LEVEL_VILLAGE = "village";

    private final YzRiverChannelMapper riverChannelMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzRiverSectionMapper riverSectionMapper;
    private final YzRiverChannelManagementMapper managementMapper;
    private final SystemAreaMapper systemAreaMapper;
    private final DictDataCommonApi dictDataApi;

    @Transactional(rollbackFor = Exception.class)
    public RiverChiefLatestImportRespVO importLatestExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        if (!isExcelFile(file.getOriginalFilename())) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<RiverChiefLatestV2ImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), RiverChiefLatestV2ImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(1)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }

        RiverChiefLatestImportRespVO respVO = new RiverChiefLatestImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }

        Map<String, String> areaIdByName = loadAreaIdByNameMap(rows);
        Map<String, String> riverLevelValueByLabel = loadDictValueByLabel(ZdConstants.ZD_HLJB);
        Map<String, List<YzRiverChannelDO>> riverMap = loadRiverMap(rows);
        Map<String, List<YzWaterReservoirDO>> reservoirMap = loadReservoirMap(rows);
        Map<String, List<YzRiverSectionDO>> sectionMap = loadSectionMap(rows, riverMap);

        for (int i = 0; i < rows.size(); i++) {
            int excelRowNo = i + 2;
            RiverChiefLatestV2ImportExcelVO row = rows.get(i);
            if (row == null || isEmptyRow(row)) {
                respVO.addSkip("第" + excelRowNo + "行跳过：整行为空");
                continue;
            }

            List<ImportedHead> heads = buildImportedHeads(row);
            if (CollUtil.isEmpty(heads)) {
                respVO.addSkip("第" + excelRowNo + "行跳过：未读取到有效河长信息");
                continue;
            }
            respVO.addTotal(heads.size());

            try {
                String facilityName = requireText(row.getFacilityName(), "第2列河道/水库名称");
                String townshipName = normalizeText(row.getTownshipName());
                String areaId = resolveAreaId(townshipName, areaIdByName);
                String[] administrativeRegion = StrUtil.isBlank(areaId) ? null : new String[]{areaId};
                String facilityTypeLabel = requireText(row.getFacilityTypeLabel(), "第5列河道/水库");
                FacilityRef facilityRef;
                if (isRiverLabel(facilityTypeLabel)) {
                    YzRiverChannelDO river = resolveRiver(facilityName, areaId, riverMap);
                    updateRiverByExcel(river, row.getRiverLevelLabel(), administrativeRegion, riverLevelValueByLabel);
                    String sectionName = normalizeText(row.getSectionName());
                    if (StrUtil.isNotBlank(sectionName)) {
                        YzRiverSectionDO section = resolveSection(river.getId(), sectionName, sectionMap);
                        facilityRef = FacilityRef.forRiverSection(river, section, administrativeRegion);
                    } else {
                        facilityRef = FacilityRef.forRiver(river, administrativeRegion);
                    }
                } else if (isReservoirLabel(facilityTypeLabel)) {
                    YzWaterReservoirDO reservoir = resolveReservoir(facilityName, areaId, reservoirMap);
                    updateReservoirByExcel(reservoir, administrativeRegion);
                    facilityRef = FacilityRef.forReservoir(reservoir, administrativeRegion);
                } else {
                    throw new IllegalArgumentException("第5列河道/水库标识非法：" + facilityTypeLabel);
                }
                overwriteCurrentGroup(facilityRef, heads);
                respVO.addSuccess(heads.size());
            } catch (Exception ex) {
                respVO.addFailure(heads.size(), "第" + excelRowNo + "行失败：" + buildErrorMessage(ex));
            }
        }
        return respVO;
    }

    private Map<String, String> loadAreaIdByNameMap(List<RiverChiefLatestV2ImportExcelVO> rows) {
        Set<String> names = new LinkedHashSet<>();
        for (RiverChiefLatestV2ImportExcelVO row : rows) {
            String townshipName = normalizeText(row == null ? null : row.getTownshipName());
            if (StrUtil.isNotBlank(townshipName)) {
                names.add(townshipName);
            }
        }
        if (CollUtil.isEmpty(names)) {
            return Map.of();
        }
        List<SystemAreaDO> areas = systemAreaMapper.selectByNames(new ArrayList<>(names));
        Map<String, String> result = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String name = normalizeText(area.getName());
            if (StrUtil.isNotBlank(name)) {
                result.putIfAbsent(name, String.valueOf(area.getId()));
            }
        }
        return result;
    }

    private Map<String, String> loadDictValueByLabel(String dictType) {
        List<DictDataRespDTO> dictList = dictDataApi.getDictDataList(dictType);
        Map<String, String> result = new LinkedHashMap<>();
        for (DictDataRespDTO item : dictList) {
            if (item == null || StrUtil.isBlank(item.getValue())) {
                continue;
            }
            String label = normalizeText(item.getLabel());
            if (StrUtil.isNotBlank(label)) {
                result.putIfAbsent(label, item.getValue());
            }
        }
        return result;
    }

    private Map<String, List<YzRiverChannelDO>> loadRiverMap(List<RiverChiefLatestV2ImportExcelVO> rows) {
        Set<String> names = new LinkedHashSet<>();
        for (RiverChiefLatestV2ImportExcelVO row : rows) {
            if (row == null || !isRiverLabel(row.getFacilityTypeLabel())) {
                continue;
            }
            String facilityName = normalizeText(row.getFacilityName());
            if (StrUtil.isNotBlank(facilityName)) {
                names.add(facilityName);
            }
        }
        if (CollUtil.isEmpty(names)) {
            return Collections.emptyMap();
        }
        List<YzRiverChannelDO> rivers = riverChannelMapper.selectList(new QueryWrapper<YzRiverChannelDO>()
                .in("river_name", names)
                .eq("deleted", 0));
        Map<String, List<YzRiverChannelDO>> result = new HashMap<>();
        for (YzRiverChannelDO river : rivers) {
            String key = normalizeText(river == null ? null : river.getRiverName());
            if (StrUtil.isBlank(key)) {
                continue;
            }
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(river);
        }
        return result;
    }

    private Map<String, List<YzWaterReservoirDO>> loadReservoirMap(List<RiverChiefLatestV2ImportExcelVO> rows) {
        Set<String> names = new LinkedHashSet<>();
        for (RiverChiefLatestV2ImportExcelVO row : rows) {
            if (row == null || !isReservoirLabel(row.getFacilityTypeLabel())) {
                continue;
            }
            String facilityName = normalizeText(row.getFacilityName());
            if (StrUtil.isNotBlank(facilityName)) {
                names.add(facilityName);
            }
        }
        if (CollUtil.isEmpty(names)) {
            return Collections.emptyMap();
        }
        List<YzWaterReservoirDO> reservoirs = waterReservoirMapper.selectList(new QueryWrapper<YzWaterReservoirDO>()
                .in("reservoir_name", names)
                .eq("deleted", 0));
        Map<String, List<YzWaterReservoirDO>> result = new HashMap<>();
        for (YzWaterReservoirDO reservoir : reservoirs) {
            String key = normalizeText(reservoir == null ? null : reservoir.getReservoirName());
            if (StrUtil.isBlank(key)) {
                continue;
            }
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(reservoir);
        }
        return result;
    }

    private Map<String, List<YzRiverSectionDO>> loadSectionMap(List<RiverChiefLatestV2ImportExcelVO> rows,
                                                               Map<String, List<YzRiverChannelDO>> riverMap) {
        Set<Long> riverIds = new LinkedHashSet<>();
        Set<String> sectionNames = new LinkedHashSet<>();
        for (RiverChiefLatestV2ImportExcelVO row : rows) {
            if (row == null || !isRiverLabel(row.getFacilityTypeLabel())) {
                continue;
            }
            String sectionName = normalizeText(row.getSectionName());
            if (StrUtil.isBlank(sectionName)) {
                continue;
            }
            sectionNames.add(sectionName);
            String riverName = normalizeText(row.getFacilityName());
            for (YzRiverChannelDO river : riverMap.getOrDefault(riverName, List.of())) {
                if (river != null && river.getId() != null) {
                    riverIds.add(river.getId());
                }
            }
        }
        if (CollUtil.isEmpty(riverIds) || CollUtil.isEmpty(sectionNames)) {
            return Collections.emptyMap();
        }
        List<YzRiverSectionDO> sections = riverSectionMapper.selectList(new QueryWrapper<YzRiverSectionDO>()
                .in("river_channel_id", riverIds)
                .in("section_name", sectionNames)
                .eq("deleted", 0));
        Map<String, List<YzRiverSectionDO>> result = new HashMap<>();
        for (YzRiverSectionDO section : sections) {
            if (section == null || section.getId() == null || section.getRiverChannelId() == null) {
                continue;
            }
            String sectionName = normalizeText(section.getSectionName());
            if (StrUtil.isBlank(sectionName)) {
                continue;
            }
            result.computeIfAbsent(buildSectionKey(section.getRiverChannelId(), sectionName), key -> new ArrayList<>())
                    .add(section);
        }
        return result;
    }

    private List<ImportedHead> buildImportedHeads(RiverChiefLatestV2ImportExcelVO row) {
        List<ImportedHead> result = new ArrayList<>();
        addHead(result, row.getProvincialHeadName(), null, row.getProvincialHeadPosition(), HEAD_LEVEL_PROVINCIAL);
        addHead(result, row.getCityHeadName(), null, row.getCityHeadPosition(), HEAD_LEVEL_CITY);
        addHead(result, row.getCountyHeadName(), null, row.getCountyHeadPosition(), HEAD_LEVEL_COUNTY);
        addHead(result, row.getTownshipHeadName(), row.getTownshipHeadContact(), row.getTownshipHeadPosition(),
                HEAD_LEVEL_TOWNSHIP);
        addHead(result, row.getVillageHead1Name(), row.getVillageHead1Contact(), row.getVillageHead1Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead2Name(), row.getVillageHead2Contact(), row.getVillageHead2Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead3Name(), row.getVillageHead3Contact(), row.getVillageHead3Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead4Name(), row.getVillageHead4Contact(), row.getVillageHead4Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead5Name(), row.getVillageHead5Contact(), row.getVillageHead5Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead6Name(), row.getVillageHead6Contact(), row.getVillageHead6Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead7Name(), row.getVillageHead7Contact(), row.getVillageHead7Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead8Name(), row.getVillageHead8Contact(), row.getVillageHead8Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead9Name(), row.getVillageHead9Contact(), row.getVillageHead9Position(),
                HEAD_LEVEL_VILLAGE);
        addHead(result, row.getVillageHead10Name(), row.getVillageHead10Contact(), row.getVillageHead10Position(),
                HEAD_LEVEL_VILLAGE);
        return result;
    }

    private void addHead(List<ImportedHead> container,
                         String headName,
                         String headContact,
                         String headPosition,
                         String headLevel) {
        String normalizedName = normalizeText(headName);
        if (StrUtil.isBlank(normalizedName)) {
            return;
        }
        container.add(new ImportedHead(
                normalizedName,
                headLevel,
                normalizeText(headPosition),
                normalizeText(headContact)
        ));
    }

    private YzRiverChannelDO resolveRiver(String facilityName,
                                          String areaId,
                                          Map<String, List<YzRiverChannelDO>> riverMap) {
        List<YzRiverChannelDO> candidates = riverMap.getOrDefault(normalizeText(facilityName), List.of());
        if (CollUtil.isEmpty(candidates)) {
            throw new IllegalArgumentException("未找到河道：" + facilityName);
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        return resolveByArea(candidates, areaId, facilityName, "河道",
                candidate -> candidate == null ? null : candidate.getTown());
    }

    private YzWaterReservoirDO resolveReservoir(String facilityName,
                                                String areaId,
                                                Map<String, List<YzWaterReservoirDO>> reservoirMap) {
        List<YzWaterReservoirDO> candidates = reservoirMap.getOrDefault(normalizeText(facilityName), List.of());
        if (CollUtil.isEmpty(candidates)) {
            throw new IllegalArgumentException("未找到水库：" + facilityName);
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        return resolveByArea(candidates, areaId, facilityName, "水库",
                candidate -> candidate == null ? null : candidate.getTownship());
    }

    private <T> T resolveByArea(List<T> candidates,
                                String areaId,
                                String facilityName,
                                String facilityType,
                                AreaExtractor<T> areaExtractor) {
        if (StrUtil.isBlank(areaId)) {
            throw new IllegalArgumentException(facilityType + "名称重名且所属乡镇为空，无法唯一匹配：" + facilityName);
        }
        List<T> matched = new ArrayList<>();
        for (T candidate : candidates) {
            String[] areas = areaExtractor.apply(candidate);
            if (containsArea(areas, areaId)) {
                matched.add(candidate);
            }
        }
        if (matched.isEmpty()) {
            throw new IllegalArgumentException(facilityType + "名称重名，按所属乡镇未匹配到记录：" + facilityName);
        }
        if (matched.size() > 1) {
            throw new IllegalArgumentException(facilityType + "名称重名，按所属乡镇仍无法唯一匹配：" + facilityName);
        }
        return matched.get(0);
    }

    private YzRiverSectionDO resolveSection(Long riverId,
                                            String sectionName,
                                            Map<String, List<YzRiverSectionDO>> sectionMap) {
        List<YzRiverSectionDO> candidates = sectionMap.getOrDefault(buildSectionKey(riverId, sectionName), List.of());
        if (CollUtil.isEmpty(candidates)) {
            throw new IllegalArgumentException("未找到河段：" + sectionName);
        }
        if (candidates.size() > 1) {
            throw new IllegalArgumentException("河段重名且无法唯一匹配：" + sectionName);
        }
        return candidates.get(0);
    }

    private void updateRiverByExcel(YzRiverChannelDO river,
                                    String riverLevelLabel,
                                    String[] administrativeRegion,
                                    Map<String, String> riverLevelValueByLabel) {
        String riverLevel = resolveNullableDictValue(riverLevelLabel, riverLevelValueByLabel);
        if (StrUtil.isNotBlank(normalizeText(riverLevelLabel)) && StrUtil.isBlank(riverLevel)) {
            throw new IllegalArgumentException("河道级别未匹配到字典值：" + riverLevelLabel);
        }
        YzRiverChannelDO update = new YzRiverChannelDO();
        update.setId(river.getId());
        update.setTown(administrativeRegion);
        update.setRiverLevel(riverLevel);
        riverChannelMapper.updateById(update);
    }

    private void updateReservoirByExcel(YzWaterReservoirDO reservoir, String[] administrativeRegion) {
        YzWaterReservoirDO update = new YzWaterReservoirDO();
        update.setId(reservoir.getId());
        update.setTownship(administrativeRegion);
        waterReservoirMapper.updateById(update);
    }

    private void overwriteCurrentGroup(FacilityRef facilityRef, List<ImportedHead> heads) {
        LocalDateTime now = LocalDateTime.now();
        expireCurrent(buildCurrentFacilityUpdate(facilityRef, now));
        int nextVersionNo = selectNextVersionNo(buildFacilityVersionQuery(facilityRef));
        for (ImportedHead head : heads) {
            YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
            record.setId(SNOWFLAKE.nextId());
            record.setRiverChannelId(facilityRef.riverChannelId);
            record.setRiverSectionId(facilityRef.riverSectionId);
            record.setWaterReservoirId(facilityRef.waterReservoirId);
            record.setReferenceType(facilityRef.referenceType);
            record.setReferenceId(facilityRef.referenceId);
            record.setSectionName(facilityRef.sectionName);
            record.setAdministrativeRegion(facilityRef.administrativeRegion);
            record.setHeadLevel(head.headLevel);
            record.setHeadName(head.headName);
            record.setHeadPosition(head.headPosition);
            record.setHeadContact(head.headContact);
            record.setVersionNo(nextVersionNo);
            record.setEffectiveFrom(now);
            record.setEffectiveTo(null);
            record.setIsCurrent(1);
            managementMapper.insert(record);
        }
    }

    private void expireCurrent(UpdateWrapper<YzRiverChannelManagementDO> wrapper) {
        managementMapper.update(null, wrapper);
    }

    private int selectNextVersionNo(QueryWrapper<YzRiverChannelManagementDO> wrapper) {
        List<YzRiverChannelManagementDO> list = managementMapper.selectList(wrapper);
        int maxVersionNo = 0;
        for (YzRiverChannelManagementDO item : list) {
            if (item != null && item.getVersionNo() != null) {
                maxVersionNo = Math.max(maxVersionNo, item.getVersionNo());
            }
        }
        return maxVersionNo + 1;
    }

    private UpdateWrapper<YzRiverChannelManagementDO> buildCurrentFacilityUpdate(FacilityRef facilityRef,
                                                                                  LocalDateTime now) {
        UpdateWrapper<YzRiverChannelManagementDO> wrapper = new UpdateWrapper<>();
        applyFacilityCondition(wrapper, facilityRef);
        wrapper.eq("is_current", 1)
                .isNull("effective_to")
                .set("is_current", 0)
                .set("effective_to", now);
        return wrapper;
    }

    private QueryWrapper<YzRiverChannelManagementDO> buildFacilityVersionQuery(FacilityRef facilityRef) {
        QueryWrapper<YzRiverChannelManagementDO> wrapper = new QueryWrapper<>();
        applyFacilityCondition(wrapper, facilityRef);
        return wrapper;
    }

    private void applyFacilityCondition(QueryWrapper<YzRiverChannelManagementDO> wrapper, FacilityRef facilityRef) {
        wrapper.eq("reference_type", facilityRef.referenceType)
                .eq("reference_id", facilityRef.referenceId);
    }

    private void applyFacilityCondition(UpdateWrapper<YzRiverChannelManagementDO> wrapper, FacilityRef facilityRef) {
        wrapper.eq("reference_type", facilityRef.referenceType)
                .eq("reference_id", facilityRef.referenceId);
    }

    private boolean containsArea(String[] areas, String areaId) {
        if (areas == null || areas.length == 0 || StrUtil.isBlank(areaId)) {
            return false;
        }
        for (String area : areas) {
            if (StrUtil.equals(normalizeText(area), areaId)) {
                return true;
            }
        }
        return false;
    }

    private String resolveAreaId(String townshipName, Map<String, String> areaIdByName) {
        String normalized = normalizeText(townshipName);
        return StrUtil.isBlank(normalized) ? null : areaIdByName.get(normalized);
    }

    private String resolveNullableDictValue(String label, Map<String, String> valueByLabel) {
        String normalized = normalizeText(label);
        if (StrUtil.isBlank(normalized) || valueByLabel.isEmpty()) {
            return null;
        }
        String exact = valueByLabel.get(normalized);
        if (StrUtil.isNotBlank(exact)) {
            return exact;
        }
        for (Map.Entry<String, String> entry : valueByLabel.entrySet()) {
            if (StrUtil.contains(normalized, entry.getKey()) || StrUtil.contains(entry.getKey(), normalized)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String requireText(String text, String fieldName) {
        String normalized = normalizeText(text);
        if (StrUtil.isBlank(normalized)) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return normalized;
    }

    private String buildErrorMessage(Exception ex) {
        return StrUtil.blankToDefault(ex.getMessage(), ex.getClass().getSimpleName());
    }

    private boolean isRiverLabel(String label) {
        return StrUtil.equals(normalizeText(label), "河道");
    }

    private boolean isReservoirLabel(String label) {
        return StrUtil.equals(normalizeText(label), "水库");
    }

    private boolean isEmptyRow(RiverChiefLatestV2ImportExcelVO row) {
        return StrUtil.isAllBlank(
                row.getFacilityName(),
                row.getSectionName(),
                row.getTownshipName(),
                row.getFacilityTypeLabel(),
                row.getRiverLevelLabel(),
                row.getProvincialHeadName(),
                row.getCityHeadName(),
                row.getCountyHeadName(),
                row.getTownshipHeadName(),
                row.getVillageHead1Name(),
                row.getVillageHead2Name(),
                row.getVillageHead3Name(),
                row.getVillageHead4Name(),
                row.getVillageHead5Name(),
                row.getVillageHead6Name(),
                row.getVillageHead7Name(),
                row.getVillageHead8Name(),
                row.getVillageHead9Name(),
                row.getVillageHead10Name()
        );
    }

    private boolean isExcelFile(String filename) {
        if (StrUtil.isBlank(filename)) {
            return false;
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private String normalizeText(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.replace('\u3000', ' ');
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return StrUtil.trimToNull(normalized);
    }

    private String buildSectionKey(Long riverId, String sectionName) {
        return riverId + "#" + normalizeText(sectionName);
    }

    private record ImportedHead(String headName, String headLevel, String headPosition, String headContact) {
    }

    private record FacilityRef(String referenceType,
                               Long referenceId,
                               Long riverChannelId,
                               Long riverSectionId,
                               Long waterReservoirId,
                               String sectionName,
                               String[] administrativeRegion) {

        private static FacilityRef forRiver(YzRiverChannelDO river, String[] administrativeRegion) {
            return new FacilityRef(ReferenceTypeConstants.RIVER, river.getId(), river.getId(), null, null,
                    river.getRiverName(), administrativeRegion);
        }

        private static FacilityRef forRiverSection(YzRiverChannelDO river,
                                                   YzRiverSectionDO section,
                                                   String[] administrativeRegion) {
            return new FacilityRef(ReferenceTypeConstants.RIVER_SECTION, section.getId(), river.getId(),
                    section.getId(), null, section.getSectionName(), administrativeRegion);
        }

        private static FacilityRef forReservoir(YzWaterReservoirDO reservoir, String[] administrativeRegion) {
            return new FacilityRef(ReferenceTypeConstants.RESERVOIR, reservoir.getId(), null, null,
                    reservoir.getId(), reservoir.getReservoirName(), administrativeRegion);
        }
    }

    @FunctionalInterface
    private interface AreaExtractor<T> {

        String[] apply(T value);
    }
}
