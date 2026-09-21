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
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefLatestImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChiefLatestImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelManagementDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelManagementMapper;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 最新河长 Excel 导入服务。
 */
@Service
@Validated
@RequiredArgsConstructor
public class RiverChiefLatestExcelImportService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private static final String FACILITY_TYPE_RIVER = "river";
    private static final String FACILITY_TYPE_RESERVOIR = "reservoir";

    private final YzRiverChannelMapper riverChannelMapper;
    private final YzWaterReservoirMapper waterReservoirMapper;
    private final YzWaterFacilityBaseMapper facilityBaseMapper;
    private final YzRiverChannelManagementMapper managementMapper;
    private final SystemAreaMapper systemAreaMapper;
    private final DictDataCommonApi dictDataApi;

    @Transactional(rollbackFor = Exception.class)
    public RiverChiefLatestImportRespVO importLatestExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (!isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<RiverChiefLatestImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), RiverChiefLatestImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(4)
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
        Map<String, String> headLevelValueByLabel = loadDictValueByLabel(ZdConstants.ZD_HZJB);
        Map<String, YzRiverChannelDO> riverByName = loadRiverByNameMap(rows);
        Map<String, YzWaterReservoirDO> reservoirByName = loadReservoirByNameMap(rows);

        for (int i = 0; i < rows.size(); i++) {
            int excelRowNo = i + 5;
            RiverChiefLatestImportExcelVO row = rows.get(i);
            if (row == null || isEmptyRow(row)) {
                respVO.addSkip("第 " + excelRowNo + " 行跳过：整行为空");
                continue;
            }

            List<ImportedHead> importedHeads;
            try {
                importedHeads = buildImportedHeads(row, headLevelValueByLabel);
            } catch (Exception ex) {
                respVO.addFailure(1, "第 " + excelRowNo + " 行失败：" + buildErrorMessage(ex));
                continue;
            }
            if (CollUtil.isEmpty(importedHeads)) {
                respVO.addSkip("第 " + excelRowNo + " 行跳过：未读取到有效河长信息");
                continue;
            }
            respVO.addTotal(importedHeads.size());

            try {
                String facilityName = requireText(row.getFacilityName(), "第 2 列河道/水库名称");
                String townshipName = normalizeText(row.getTownshipName());
                String areaId = resolveAreaId(townshipName, areaIdByName);
                String[] administrativeRegion = StrUtil.isBlank(areaId) ? null : new String[]{areaId};
                String facilityLevelLabel = normalizeText(row.getFacilityLevelLabel());
                if (StrUtil.equals(facilityLevelLabel, "村级")) {
                    overwriteUnboundVillageGroup(facilityName, administrativeRegion, importedHeads);
                    respVO.addSuccess(importedHeads.size());
                    continue;
                }

                String facilityTypeLabel = requireText(row.getFacilityTypeLabel(), "第 4 列河道/水库标识");
                FacilityRef facilityRef = resolveOrCreateFacility(
                        facilityName,
                        facilityTypeLabel,
                        facilityLevelLabel,
                        townshipName,
                        administrativeRegion,
                        riverLevelValueByLabel,
                        riverByName,
                        reservoirByName
                );
                appendFacilityGroup(facilityRef, importedHeads);
                respVO.addSuccess(importedHeads.size());
            } catch (Exception ex) {
                respVO.addFailure(importedHeads.size(), "第 " + excelRowNo + " 行失败：" + buildErrorMessage(ex));
            }
        }
        return respVO;
    }

    private Map<String, String> loadAreaIdByNameMap(List<RiverChiefLatestImportExcelVO> rows) {
        Set<String> names = new LinkedHashSet<>();
        for (RiverChiefLatestImportExcelVO row : rows) {
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
        Map<String, String> result = new HashMap<>();
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

    private Map<String, YzRiverChannelDO> loadRiverByNameMap(List<RiverChiefLatestImportExcelVO> rows) {
        Set<String> names = new LinkedHashSet<>();
        for (RiverChiefLatestImportExcelVO row : rows) {
            if (row == null || !isRiverLabel(row.getFacilityTypeLabel())) {
                continue;
            }
            if (StrUtil.equals(normalizeText(row.getFacilityLevelLabel()), "村级")) {
                continue;
            }
            String facilityName = normalizeText(row.getFacilityName());
            if (StrUtil.isNotBlank(facilityName)) {
                names.add(facilityName);
            }
        }
        if (CollUtil.isEmpty(names)) {
            return new HashMap<>();
        }
        List<YzRiverChannelDO> list = riverChannelMapper.selectList(new QueryWrapper<YzRiverChannelDO>()
                .in("river_name", names)
                .eq("deleted", 0));
        Map<String, YzRiverChannelDO> result = new HashMap<>();
        for (YzRiverChannelDO item : list) {
            String key = normalizeText(item == null ? null : item.getRiverName());
            if (StrUtil.isNotBlank(key) && item.getId() != null) {
                result.putIfAbsent(key, item);
            }
        }
        return result;
    }

    private Map<String, YzWaterReservoirDO> loadReservoirByNameMap(List<RiverChiefLatestImportExcelVO> rows) {
        Set<String> names = new LinkedHashSet<>();
        for (RiverChiefLatestImportExcelVO row : rows) {
            if (row == null || !isReservoirLabel(row.getFacilityTypeLabel())) {
                continue;
            }
            if (StrUtil.equals(normalizeText(row.getFacilityLevelLabel()), "村级")) {
                continue;
            }
            String facilityName = normalizeText(row.getFacilityName());
            if (StrUtil.isNotBlank(facilityName)) {
                names.add(facilityName);
            }
        }
        if (CollUtil.isEmpty(names)) {
            return new HashMap<>();
        }
        List<YzWaterReservoirDO> list = waterReservoirMapper.selectList(new QueryWrapper<YzWaterReservoirDO>()
                .in("reservoir_name", names)
                .eq("deleted", 0));
        Map<String, YzWaterReservoirDO> result = new HashMap<>();
        for (YzWaterReservoirDO item : list) {
            String key = normalizeText(item == null ? null : item.getReservoirName());
            if (StrUtil.isNotBlank(key) && item.getId() != null) {
                result.putIfAbsent(key, item);
            }
        }
        return result;
    }

    private List<ImportedHead> buildImportedHeads(RiverChiefLatestImportExcelVO row,
                                                  Map<String, String> headLevelValueByLabel) {
        List<ImportedHead> result = new ArrayList<>();
        addHead(result, row.getCountyHeadName(), null, row.getCountyHeadPosition(), "县级", headLevelValueByLabel);
        addHead(result, row.getTownshipHeadName(), row.getTownshipHeadContact(), row.getTownshipHeadPosition(),
                "镇级", headLevelValueByLabel);
        addHead(result, row.getVillageHead1Name(), row.getVillageHead1Contact(), row.getVillageHead1Position(),
                "村级", headLevelValueByLabel);
        addHead(result, row.getVillageHead2Name(), row.getVillageHead2Contact(), row.getVillageHead2Position(),
                "村级", headLevelValueByLabel);
        addHead(result, row.getVillageHead3Name(), row.getVillageHead3Contact(), row.getVillageHead3Position(),
                "村级", headLevelValueByLabel);
        addHead(result, row.getVillageHead4Name(), row.getVillageHead4Contact(), row.getVillageHead4Position(),
                "村级", headLevelValueByLabel);
        addHead(result, row.getVillageHead5Name(), row.getVillageHead5Contact(), row.getVillageHead5Position(),
                "村级", headLevelValueByLabel);
        addHead(result, row.getVillageHead6Name(), row.getVillageHead6Contact(), row.getVillageHead6Position(),
                "村级", headLevelValueByLabel);
        addHead(result, row.getVillageHead7Name(), row.getVillageHead7Contact(), row.getVillageHead7Position(),
                "村级", headLevelValueByLabel);
        return result;
    }

    private void addHead(List<ImportedHead> container,
                         String headName,
                         String headContact,
                         String headPosition,
                         String headLevelLabel,
                         Map<String, String> headLevelValueByLabel) {
        String normalizedName = normalizeText(headName);
        if (StrUtil.isBlank(normalizedName)) {
            return;
        }
        String headLevel = resolveRequiredDictValue(headLevelLabel, headLevelValueByLabel, "河长级别");
        container.add(new ImportedHead(
                normalizedName,
                headLevel,
                normalizeText(headPosition),
                normalizeText(headContact)
        ));
    }

    private FacilityRef resolveOrCreateFacility(String facilityName,
                                                String facilityTypeLabel,
                                                String facilityLevelLabel,
                                                String townshipName,
                                                String[] administrativeRegion,
                                                Map<String, String> riverLevelValueByLabel,
                                                Map<String, YzRiverChannelDO> riverByName,
                                                Map<String, YzWaterReservoirDO> reservoirByName) {
        String normalizedName = normalizeText(facilityName);
        if (isRiverLabel(facilityTypeLabel)) {
            YzRiverChannelDO river = riverByName.get(normalizedName);
            if (river == null || river.getId() == null) {
                river = createRiverChannel(normalizedName, administrativeRegion, facilityLevelLabel, riverLevelValueByLabel);
                riverByName.put(normalizedName, river);
            }
            return FacilityRef.forRiver(river);
        }
        if (isReservoirLabel(facilityTypeLabel)) {
            YzWaterReservoirDO reservoir = reservoirByName.get(normalizedName);
            if (reservoir == null || reservoir.getId() == null) {
                reservoir = createReservoir(normalizedName, townshipName, administrativeRegion);
                reservoirByName.put(normalizedName, reservoir);
            }
            return FacilityRef.forReservoir(reservoir);
        }
        throw new IllegalArgumentException("第 4 列河道/水库标识不合法：" + facilityTypeLabel);
    }

    private YzRiverChannelDO createRiverChannel(String riverName,
                                                String[] administrativeRegion,
                                                String riverLevelLabel,
                                                Map<String, String> riverLevelValueByLabel) {
        String riverLevel = resolveRequiredDictValue(riverLevelLabel, riverLevelValueByLabel, "河道级别");
        String riverCode = String.valueOf(SNOWFLAKE.nextId());
        Long facilityId = SNOWFLAKE.nextId();

        YzWaterFacilityBaseDO facilityBase = new YzWaterFacilityBaseDO();
        facilityBase.setId(facilityId);
        facilityBase.setFacilityCode(riverCode);
        facilityBase.setFacilityName(riverName);
        facilityBase.setFacilityType(FACILITY_TYPE_RIVER);
        facilityBase.setSourceType("system");
        facilityBaseMapper.insert(facilityBase);

        YzRiverChannelDO river = new YzRiverChannelDO();
        river.setId(SNOWFLAKE.nextId());
        river.setFacilityId(facilityId);
        river.setRiverCode(riverCode);
        river.setRiverName(riverName);
        river.setTown(administrativeRegion);
        river.setRiverLevel(riverLevel);
        river.setIsProvincialBackbone(0);
        riverChannelMapper.insert(river);
        return river;
    }

    private YzWaterReservoirDO createReservoir(String reservoirName,
                                               String townshipName,
                                               String[] administrativeRegion) {
        String reservoirCode = String.valueOf(SNOWFLAKE.nextId());
        Long facilityId = SNOWFLAKE.nextId();

        YzWaterFacilityBaseDO facilityBase = new YzWaterFacilityBaseDO();
        facilityBase.setId(facilityId);
        facilityBase.setFacilityCode(reservoirCode);
        facilityBase.setFacilityName(reservoirName);
        facilityBase.setFacilityType(FACILITY_TYPE_RESERVOIR);
        facilityBase.setAdminRegionCode(firstAreaCode(administrativeRegion));
        facilityBase.setAdminRegion(normalizeText(townshipName));
        facilityBase.setSourceType("system");
        facilityBaseMapper.insert(facilityBase);

        YzWaterReservoirDO reservoir = new YzWaterReservoirDO();
        reservoir.setId(SNOWFLAKE.nextId());
        reservoir.setFacilityId(facilityId);
        reservoir.setReservoirCode(reservoirCode);
        reservoir.setReservoirName(reservoirName);
        reservoir.setTownship(administrativeRegion);
        waterReservoirMapper.insert(reservoir);
        return reservoir;
    }

    private void appendFacilityGroup(FacilityRef facilityRef, List<ImportedHead> importedHeads) {
        LocalDateTime now = LocalDateTime.now();
        int nextVersionNo = selectNextVersionNo(buildFacilityVersionQuery(facilityRef));
        for (ImportedHead head : importedHeads) {
            insertManagementRecord(facilityRef, head, nextVersionNo, now);
        }
    }

    private void overwriteUnboundVillageGroup(String villageName,
                                              String[] administrativeRegion,
                                              List<ImportedHead> importedHeads) {
        String normalizedVillageName = requireText(villageName, "第 2 列河道/水库名称");
        LocalDateTime now = LocalDateTime.now();
        expireCurrent(buildCurrentUnboundUpdate(normalizedVillageName, administrativeRegion, now));
        int nextVersionNo = selectNextVersionNo(buildUnboundVersionQuery(normalizedVillageName, administrativeRegion));
        FacilityRef facilityRef = FacilityRef.forVillage(normalizedVillageName).withAdministrativeRegion(administrativeRegion);
        for (ImportedHead head : importedHeads) {
            insertManagementRecord(facilityRef, head, nextVersionNo, now);
        }
    }

    private void insertManagementRecord(FacilityRef facilityRef,
                                        ImportedHead importedHead,
                                        int versionNo,
                                        LocalDateTime effectiveFrom) {
        YzRiverChannelManagementDO record = new YzRiverChannelManagementDO();
        record.setId(SNOWFLAKE.nextId());
        record.setRiverChannelId(facilityRef.riverChannelId);
        record.setRiverSectionId(facilityRef.riverSectionId);
        record.setWaterReservoirId(facilityRef.waterReservoirId);
        record.setReferenceType(facilityRef.referenceType);
        record.setReferenceId(facilityRef.referenceId);
        record.setSectionName(facilityRef.sectionName);
        record.setHeadLevel(importedHead.headLevel);
        record.setHeadName(importedHead.headName);
        record.setHeadPosition(importedHead.headPosition);
        record.setHeadContact(importedHead.headContact);
        record.setAdministrativeRegion(facilityRef.administrativeRegion);
        record.setVersionNo(versionNo);
        record.setEffectiveFrom(effectiveFrom);
        record.setEffectiveTo(null);
        record.setIsCurrent(1);
        managementMapper.insert(record);
    }

    private void expireCurrent(UpdateWrapper<YzRiverChannelManagementDO> wrapper) {
        managementMapper.update(null, wrapper);
    }

    private int selectNextVersionNo(QueryWrapper<YzRiverChannelManagementDO> wrapper) {
        List<YzRiverChannelManagementDO> records = managementMapper.selectList(wrapper);
        int maxVersionNo = 0;
        for (YzRiverChannelManagementDO item : records) {
            if (item != null && item.getVersionNo() != null) {
                maxVersionNo = Math.max(maxVersionNo, item.getVersionNo());
            }
        }
        return maxVersionNo + 1;
    }

    private QueryWrapper<YzRiverChannelManagementDO> buildFacilityVersionQuery(FacilityRef facilityRef) {
        QueryWrapper<YzRiverChannelManagementDO> wrapper = new QueryWrapper<>();
        applyFacilityCondition(wrapper, facilityRef);
        return wrapper;
    }

    private UpdateWrapper<YzRiverChannelManagementDO> buildCurrentUnboundUpdate(String villageName,
                                                                                String[] administrativeRegion,
                                                                                LocalDateTime now) {
        UpdateWrapper<YzRiverChannelManagementDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("is_current", 1)
                .isNull("effective_to")
                .isNull("reference_type")
                .isNull("reference_id")
                .eq("section_name", villageName)
                .set("is_current", 0)
                .set("effective_to", now);
        applyAdministrativeRegionCondition(wrapper, administrativeRegion);
        return wrapper;
    }

    private QueryWrapper<YzRiverChannelManagementDO> buildUnboundVersionQuery(String villageName,
                                                                              String[] administrativeRegion) {
        QueryWrapper<YzRiverChannelManagementDO> wrapper = new QueryWrapper<>();
        wrapper.isNull("reference_type")
                .isNull("reference_id")
                .eq("section_name", villageName);
        applyAdministrativeRegionCondition(wrapper, administrativeRegion);
        return wrapper;
    }

    private void applyFacilityCondition(QueryWrapper<YzRiverChannelManagementDO> wrapper, FacilityRef facilityRef) {
        if (StrUtil.isNotBlank(facilityRef.referenceType)) {
            wrapper.eq("reference_type", facilityRef.referenceType);
        } else {
            wrapper.isNull("reference_type");
        }
        if (facilityRef.referenceId != null) {
            wrapper.eq("reference_id", facilityRef.referenceId);
        } else {
            wrapper.isNull("reference_id");
        }
    }

    private void applyFacilityCondition(UpdateWrapper<YzRiverChannelManagementDO> wrapper, FacilityRef facilityRef) {
        if (StrUtil.isNotBlank(facilityRef.referenceType)) {
            wrapper.eq("reference_type", facilityRef.referenceType);
        } else {
            wrapper.isNull("reference_type");
        }
        if (facilityRef.referenceId != null) {
            wrapper.eq("reference_id", facilityRef.referenceId);
        } else {
            wrapper.isNull("reference_id");
        }
    }

    private void applyAdministrativeRegionCondition(QueryWrapper<YzRiverChannelManagementDO> wrapper,
                                                    String[] administrativeRegion) {
        String literal = toPostgresTextArrayLiteral(administrativeRegion);
        if (literal == null) {
            wrapper.isNull("administrative_region");
            return;
        }
        wrapper.apply("administrative_region && {0}::text[]", literal);
    }

    private void applyAdministrativeRegionCondition(UpdateWrapper<YzRiverChannelManagementDO> wrapper,
                                                    String[] administrativeRegion) {
        String literal = toPostgresTextArrayLiteral(administrativeRegion);
        if (literal == null) {
            wrapper.isNull("administrative_region");
            return;
        }
        wrapper.apply("administrative_region && {0}::text[]", literal);
    }

    private String toPostgresTextArrayLiteral(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        List<String> normalizedValues = new ArrayList<>();
        for (String value : values) {
            String normalized = normalizeText(value);
            if (StrUtil.isNotBlank(normalized)) {
                normalizedValues.add("\"" + normalized.replace("\"", "\\\"") + "\"");
            }
        }
        return normalizedValues.isEmpty() ? null : "{" + String.join(",", normalizedValues) + "}";
    }

    private boolean isEmptyRow(RiverChiefLatestImportExcelVO row) {
        return StrUtil.isAllBlank(
                row.getFacilityName(),
                row.getTownshipName(),
                row.getFacilityTypeLabel(),
                row.getFacilityLevelLabel(),
                row.getCountyHeadName(),
                row.getTownshipHeadName(),
                row.getVillageHead1Name(),
                row.getVillageHead2Name(),
                row.getVillageHead3Name(),
                row.getVillageHead4Name(),
                row.getVillageHead5Name(),
                row.getVillageHead6Name(),
                row.getVillageHead7Name()
        );
    }

    private boolean isRiverLabel(String label) {
        return StrUtil.equals(normalizeText(label), "河道");
    }

    private boolean isReservoirLabel(String label) {
        return StrUtil.equals(normalizeText(label), "水库");
    }

    private String resolveAreaId(String townshipName, Map<String, String> areaIdByName) {
        String normalized = normalizeText(townshipName);
        return StrUtil.isBlank(normalized) ? null : areaIdByName.get(normalized);
    }

    private String resolveRequiredDictValue(String label, Map<String, String> valueByLabel, String fieldName) {
        String value = resolveDictValue(label, valueByLabel);
        if (StrUtil.isBlank(value)) {
            throw new IllegalArgumentException(fieldName + "未匹配到字典值：" + label);
        }
        return value;
    }

    private String resolveDictValue(String label, Map<String, String> valueByLabel) {
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

    private String firstAreaCode(String[] administrativeRegion) {
        return administrativeRegion == null || administrativeRegion.length == 0 ? null : administrativeRegion[0];
    }

    private String buildErrorMessage(Exception ex) {
        return StrUtil.blankToDefault(ex.getMessage(), ex.getClass().getSimpleName());
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

    private record ImportedHead(String headName, String headLevel, String headPosition, String headContact) {
    }

    private static final class FacilityRef {

        private final String referenceType;
        private final Long referenceId;
        private final Long riverChannelId;
        private final Long riverSectionId;
        private final Long waterReservoirId;
        private final String sectionName;
        private final String[] administrativeRegion;

        private FacilityRef(String referenceType,
                            Long referenceId,
                            Long riverChannelId,
                            Long riverSectionId,
                            Long waterReservoirId,
                            String sectionName,
                            String[] administrativeRegion) {
            this.referenceType = referenceType;
            this.referenceId = referenceId;
            this.riverChannelId = riverChannelId;
            this.riverSectionId = riverSectionId;
            this.waterReservoirId = waterReservoirId;
            this.sectionName = sectionName;
            this.administrativeRegion = administrativeRegion;
        }

        private static FacilityRef forRiver(YzRiverChannelDO river) {
            return new FacilityRef(
                    ReferenceTypeConstants.RIVER,
                    river.getId(),
                    river.getId(),
                    null,
                    null,
                    river.getRiverName(),
                    river.getTown()
            );
        }

        private static FacilityRef forReservoir(YzWaterReservoirDO reservoir) {
            return new FacilityRef(
                    ReferenceTypeConstants.RESERVOIR,
                    reservoir.getId(),
                    null,
                    null,
                    reservoir.getId(),
                    reservoir.getReservoirName(),
                    reservoir.getTownship()
            );
        }

        private static FacilityRef forVillage(String villageName) {
            return new FacilityRef(null, null, null, null, null, villageName, null);
        }

        private FacilityRef withAdministrativeRegion(String[] administrativeRegion) {
            return new FacilityRef(referenceType, referenceId, riverChannelId, riverSectionId,
                    waterReservoirId, sectionName, administrativeRegion);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FacilityRef other)) {
                return false;
            }
            return Objects.equals(referenceType, other.referenceType)
                    && Objects.equals(referenceId, other.referenceId)
                    && Objects.equals(sectionName, other.sectionName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(referenceType, referenceId, sectionName);
        }
    }
}
