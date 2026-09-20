package com.sydigit.yzwater.module.service.signboard;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcelFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ReferenceTypeConstants;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardExcelImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardExcelImportRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.signboard.SignboardSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelBfDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardBfDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelBfMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardBfMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.dataobject.dict.DictDataDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import com.sydigit.yzwater.module.system.service.dict.DictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公示牌 Excel 导入服务。
 */
@Service
@Validated
@RequiredArgsConstructor
public class SignboardBfExcelImportService {

    /** 默认导入为河长制公示牌。 */
    private static final String DEFAULT_SIGNBOARD_TYPE = "river_chief_mechanism";
    private static final String RELATION_TYPE_RIVER = "河道";
    private static final String RELATION_TYPE_RESERVOIR = "水库";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(-?\\d+(?:\\.\\d+)?)");

    private final SystemAreaMapper systemAreaMapper;
    private final DictDataService dictDataService;
    private final SignboardBfService signboardService;
    private final YzRiverChannelBfMapper riverChannelMapper;
    private final YzWaterReservoirBfMapper waterReservoirMapper;
    private final YzSignboardBfMapper signboardMapper;

    /**
     * 导入公示牌信息，只读取第一个 sheet。
     *
     * <p>模板规则：第 1 行为表头，第 2 行起为数据；按 A~J 列读取，其中 D 列跳过，E 列为公示牌等级。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public SignboardExcelImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }

        List<SignboardExcelImportExcelVO> rows;
        try {
            rows = FastExcelFactory.read(file.getInputStream(), SignboardExcelImportExcelVO.class, null)
                    .autoCloseStream(false)
                    .headRowNumber(1)
                    .sheet(0)
                    .doReadSync();
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "Excel 解析失败"));
        }

        SignboardExcelImportRespVO respVO = new SignboardExcelImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());

        AreaContext areaContext = initAreaContext();
        FacilityContext facilityContext = initFacilityContext();
        DictContext dictContext = initMaintenanceDictContext();
        Map<String, String> signboardLevelMap = initSignboardLevelMap();
        Set<String> existingSignboardCodes = initExistingSignboardCodes();
        Set<String> importedSignboardCodes = new HashSet<>();

        int successCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            int excelRowNo = i + 2;
            SignboardExcelImportExcelVO row = rows.get(i);
            if (row == null || isEmptyRow(row)) {
                respVO.addSkip("第 " + excelRowNo + " 行跳过：整行为空或关键字段均为空");
                continue;
            }

            try {
                SignboardSaveReqVO saveReqVO = buildSaveReq(row, areaContext, facilityContext, dictContext, signboardLevelMap);
                String signboardCode = saveReqVO.getSignboardCode();
                String signboardName = saveReqVO.getSignboardName();
                if (existingSignboardCodes.contains(signboardCode)) {
                    respVO.addSkip("第 " + excelRowNo + " 行跳过：公示牌【" + signboardName + "】编号【"
                            + signboardCode + "】重复，系统中已存在");
                    continue;
                }
                if (importedSignboardCodes.contains(signboardCode)) {
                    respVO.addSkip("第 " + excelRowNo + " 行跳过：公示牌【" + signboardName + "】编号【"
                            + signboardCode + "】重复，本次导入文件内已出现");
                    continue;
                }
                signboardService.createSignboard(saveReqVO);
                importedSignboardCodes.add(signboardCode);
                successCount++;
            } catch (Exception ex) {
                respVO.addFailure("第 " + excelRowNo + " 行失败：" + StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
            }
        }

        respVO.setSuccessCount(successCount);
        return respVO;
    }

    private SignboardSaveReqVO buildSaveReq(SignboardExcelImportExcelVO row,
                                            AreaContext areaContext,
                                            FacilityContext facilityContext,
                                            DictContext dictContext,
                                            Map<String, String> signboardLevelMap) {
        String signboardName = normalizeText(row.getSignboardName());
        if (StrUtil.isBlank(signboardName)) {
            throw new IllegalArgumentException("公示牌名称为空");
        }

        String signboardCode = normalizeText(row.getSignboardCode());
        if (StrUtil.isBlank(signboardCode)) {
            throw new IllegalArgumentException("编号为空");
        }

        String adminRegionId = resolveAreaId(normalizeText(row.getAdminRegionName()), areaContext);
        CoordinatePair coordinatePair = parseCoordinatePair(row.getLongitudeLatitudeText());
        String maintenanceValue = resolveOrCreateMaintenanceValue(row.getMaintenanceUnit(), dictContext);
        String signboardLevelValue = resolveSignboardLevelValue(row.getSignboardLevelLabel(), signboardLevelMap);
        RelationBinding relationBinding = resolveRelationBinding(signboardName, row.getRelationType(), facilityContext);

        SignboardSaveReqVO saveReqVO = new SignboardSaveReqVO();
        saveReqVO.setSignboardName(signboardName);
        saveReqVO.setSignboardCode(signboardCode);
        saveReqVO.setSignboardType(DEFAULT_SIGNBOARD_TYPE);
        saveReqVO.setSignboardLevel(signboardLevelValue);
        saveReqVO.setAdminRegion(adminRegionId);
        saveReqVO.setSpecificLocation(normalizeText(row.getSpecificLocation()));
        saveReqVO.setLongitude(coordinatePair.longitude);
        saveReqVO.setLatitude(coordinatePair.latitude);
        if (StrUtil.isNotBlank(maintenanceValue)) {
            saveReqVO.setMaintenanceUnit(List.of(maintenanceValue));
        }
        if (relationBinding != null) {
            if (ReferenceTypeConstants.RIVER.equals(relationBinding.referenceType)) {
                saveReqVO.setRiverChannelId(relationBinding.referenceId);
            } else if (ReferenceTypeConstants.RESERVOIR.equals(relationBinding.referenceType)) {
                saveReqVO.setWaterReservoirId(relationBinding.referenceId);
            }
        }
        return saveReqVO;
    }

    private AreaContext initAreaContext() {
        List<SystemAreaDO> areas = systemAreaMapper.selectList(new LambdaQueryWrapper<SystemAreaDO>()
                .select(SystemAreaDO::getId, SystemAreaDO::getName)
                .eq(SystemAreaDO::getDeleted, 0)
                .orderByAsc(SystemAreaDO::getId));
        AreaContext context = new AreaContext();
        context.exactNameMap = new HashMap<>();
        for (SystemAreaDO area : areas) {
            if (area == null || area.getId() == null) {
                continue;
            }
            String name = normalizeText(area.getName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            context.exactNameMap.computeIfAbsent(name, key -> new ArrayList<>())
                    .add(new AreaCandidate(String.valueOf(area.getId()), name));
        }
        return context;
    }

    private Set<String> initExistingSignboardCodes() {
        List<YzSignboardBfDO> signboards = signboardMapper.selectList(new LambdaQueryWrapper<YzSignboardBfDO>()
                .select(YzSignboardBfDO::getSignboardCode)
                .isNotNull(YzSignboardBfDO::getSignboardCode));
        Set<String> result = new HashSet<>();
        for (YzSignboardBfDO item : signboards) {
            if (item == null) {
                continue;
            }
            String signboardCode = normalizeText(item.getSignboardCode());
            if (StrUtil.isNotBlank(signboardCode)) {
                result.add(signboardCode);
            }
        }
        return result;
    }

    private FacilityContext initFacilityContext() {
        FacilityContext context = new FacilityContext();
        context.riverMap = new HashMap<>();
        context.reservoirMap = new HashMap<>();

        List<YzRiverChannelBfDO> riverChannels = riverChannelMapper.selectList(new LambdaQueryWrapper<YzRiverChannelBfDO>()
                .select(YzRiverChannelBfDO::getId, YzRiverChannelBfDO::getRiverName)
                .isNotNull(YzRiverChannelBfDO::getRiverName)
                .orderByAsc(YzRiverChannelBfDO::getId));
        for (YzRiverChannelBfDO item : riverChannels) {
            if (item == null || item.getId() == null) {
                continue;
            }
            String name = normalizeText(item.getRiverName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            context.riverMap.computeIfAbsent(name, key -> new ArrayList<>())
                    .add(new FacilityCandidate(item.getId(), name));
        }

        List<YzWaterReservoirBfDO> reservoirs = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirBfDO>()
                .select(YzWaterReservoirBfDO::getId, YzWaterReservoirBfDO::getReservoirName)
                .isNotNull(YzWaterReservoirBfDO::getReservoirName)
                .orderByAsc(YzWaterReservoirBfDO::getId));
        for (YzWaterReservoirBfDO item : reservoirs) {
            if (item == null || item.getId() == null) {
                continue;
            }
            String name = normalizeText(item.getReservoirName());
            if (StrUtil.isBlank(name)) {
                continue;
            }
            context.reservoirMap.computeIfAbsent(name, key -> new ArrayList<>())
                    .add(new FacilityCandidate(item.getId(), name));
        }
        return context;
    }

    private DictContext initMaintenanceDictContext() {
        List<DictDataDO> dictList = dictDataService.getDictDataList(null, ZdConstants.ZD_WHDW);
        DictContext context = new DictContext();
        context.labelToValue = new HashMap<>();

        int maxValue = 1;
        int maxSort = 0;
        for (DictDataDO item : dictList) {
            if (item == null) {
                continue;
            }
            String label = normalizeText(item.getLabel());
            String value = normalizeText(item.getValue());
            if (StrUtil.isNotBlank(label) && StrUtil.isNotBlank(value)) {
                context.labelToValue.putIfAbsent(label, value);
            }
            if (item.getSort() != null) {
                maxSort = Math.max(maxSort, item.getSort());
            }
            Integer numericValue = parseInteger(value);
            if (numericValue != null) {
                maxValue = Math.max(maxValue, numericValue);
            }
        }
        context.nextValue = Math.max(2, maxValue + 1);
        context.nextSort = maxSort + 1;
        return context;
    }

    private Map<String, String> initSignboardLevelMap() {
        List<DictDataDO> dictList = dictDataService.getDictDataList(null, ZdConstants.ZD_HLJB);
        Map<String, String> result = new HashMap<>();
        for (DictDataDO item : dictList) {
            if (item == null) {
                continue;
            }
            String label = normalizeText(item.getLabel());
            String value = normalizeText(item.getValue());
            if (StrUtil.isNotBlank(label) && StrUtil.isNotBlank(value)) {
                result.putIfAbsent(label, value);
            }
        }
        return result;
    }

    private String resolveAreaId(String areaName, AreaContext areaContext) {
        String normalized = normalizeText(areaName);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        List<AreaCandidate> exactMatches = areaContext.exactNameMap.getOrDefault(normalized, List.of());
        if (exactMatches.size() == 1) {
            return exactMatches.get(0).id;
        }
        if (exactMatches.size() > 1) {
            throw new IllegalArgumentException("行政区划【" + normalized + "】匹配到多条记录");
        }

        List<AreaCandidate> fuzzyMatches = new ArrayList<>();
        for (List<AreaCandidate> candidates : areaContext.exactNameMap.values()) {
            for (AreaCandidate candidate : candidates) {
                if (candidate == null || StrUtil.isBlank(candidate.name)) {
                    continue;
                }
                if (StrUtil.contains(candidate.name, normalized) || StrUtil.contains(normalized, candidate.name)) {
                    fuzzyMatches.add(candidate);
                }
            }
        }
        if (fuzzyMatches.size() == 1) {
            return fuzzyMatches.get(0).id;
        }
        if (fuzzyMatches.size() > 1) {
            throw new IllegalArgumentException("行政区划【" + normalized + "】匹配到多条记录");
        }
        throw new IllegalArgumentException("行政区划【" + normalized + "】未匹配到 system_area.name");
    }

    private RelationBinding resolveRelationBinding(String signboardName, String relationTypeText, FacilityContext facilityContext) {
        String relationType = normalizeText(relationTypeText);
        if (StrUtil.isBlank(relationType)) {
            return null;
        }

        if (StrUtil.contains(relationType, RELATION_TYPE_RIVER)) {
            return buildRelationBinding(signboardName, facilityContext.riverMap.getOrDefault(signboardName, List.of()), RELATION_TYPE_RIVER,
                    ReferenceTypeConstants.RIVER);
        }
        if (StrUtil.contains(relationType, RELATION_TYPE_RESERVOIR)) {
            return buildRelationBinding(signboardName, facilityContext.reservoirMap.getOrDefault(signboardName, List.of()), RELATION_TYPE_RESERVOIR,
                    ReferenceTypeConstants.RESERVOIR);
        }
        return null;
    }

    private RelationBinding buildRelationBinding(String signboardName,
                                                 List<FacilityCandidate> candidates,
                                                 String displayType,
                                                 String referenceType) {
        if (CollUtil.isEmpty(candidates)) {
            return null;
        }
        if (candidates.size() > 1) {
            return null;
        }
        return new RelationBinding(referenceType, candidates.get(0).id);
    }

    private String resolveOrCreateMaintenanceValue(String maintenanceText, DictContext context) {
        String normalized = normalizeText(maintenanceText);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        String exactValue = context.labelToValue.get(normalized);
        if (StrUtil.isNotBlank(exactValue)) {
            return exactValue;
        }

        for (Map.Entry<String, String> entry : context.labelToValue.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            if (StrUtil.isBlank(label) || StrUtil.isBlank(value)) {
                continue;
            }
            if (StrUtil.contains(normalized, label) || StrUtil.contains(label, normalized)) {
                return value;
            }
        }

        DictDataSaveReqVO createReq = new DictDataSaveReqVO();
        createReq.setDictType(ZdConstants.ZD_WHDW);
        createReq.setLabel(normalized);
        createReq.setValue(String.valueOf(context.nextValue));
        createReq.setSort(context.nextSort);
        createReq.setStatus(CommonStatusEnum.ENABLE.getStatus());
        createReq.setRemark("公示牌 Excel 导入自动新增");
        dictDataService.createDictData(createReq);

        String createdValue = createReq.getValue();
        context.labelToValue.put(normalized, createdValue);
        context.nextValue++;
        context.nextSort++;
        return createdValue;
    }

    private String resolveSignboardLevelValue(String signboardLevelText, Map<String, String> signboardLevelMap) {
        String normalized = normalizeText(signboardLevelText);
        if (StrUtil.isBlank(normalized)) {
            return null;
        }
        String exactValue = signboardLevelMap.get(normalized);
        if (StrUtil.isNotBlank(exactValue)) {
            return exactValue;
        }
        for (Map.Entry<String, String> entry : signboardLevelMap.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            if (StrUtil.isBlank(label) || StrUtil.isBlank(value)) {
                continue;
            }
            if (StrUtil.contains(normalized, label) || StrUtil.contains(label, normalized)) {
                return value;
            }
        }
        throw new IllegalArgumentException("公示牌等级【" + normalized + "】未匹配到字典 zd_hljb.label");
    }

    private CoordinatePair parseCoordinatePair(String coordinateText) {
        CoordinatePair empty = new CoordinatePair(null, null);
        String raw = normalizeCoordinateText(coordinateText);
        if (StrUtil.isBlank(raw)) {
            return empty;
        }

        List<String> parts = splitCoordinateParts(raw);
        if (parts.size() < 2) {
            return empty;
        }

        BigDecimal longitude = parseSingleCoordinate(parts.get(0));
        BigDecimal latitude = parseSingleCoordinate(parts.get(1));
        if (longitude == null || latitude == null) {
            return empty;
        }
        return new CoordinatePair(longitude, latitude);
    }

    private List<String> splitCoordinateParts(String raw) {
        if (StrUtil.containsAny(raw, ',', '，')) {
            List<String> commaParts = splitAndTrim(raw, "[,，]");
            if (commaParts.size() >= 2) {
                return List.of(commaParts.get(0), commaParts.get(1));
            }
        }

        if (raw.contains("\n") || raw.contains("\r")) {
            List<String> lineParts = splitAndTrim(raw, "\\r?\\n+");
            if (lineParts.size() >= 2) {
                return List.of(lineParts.get(0), lineParts.get(1));
            }
        }

        List<String> spaceParts = splitAndTrim(raw, "\\s+");
        if (spaceParts.size() >= 2) {
            return List.of(spaceParts.get(0), spaceParts.get(1));
        }
        return List.of();
    }

    private List<String> splitAndTrim(String text, String separatorRegex) {
        if (StrUtil.isBlank(text)) {
            return List.of();
        }
        String[] arr = text.split(separatorRegex);
        List<String> result = new ArrayList<>(arr.length);
        for (String item : arr) {
            String normalized = normalizeCoordinateText(item);
            if (StrUtil.isNotBlank(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private BigDecimal parseSingleCoordinate(String text) {
        String raw = normalizeCoordinateText(text);
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        int sign = 1;
        String upper = raw.toUpperCase(Locale.ROOT);
        if (upper.startsWith("-")
                || upper.contains("W")
                || upper.contains("S")
                || upper.contains("西")
                || upper.contains("南")) {
            sign = -1;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(upper);
        List<BigDecimal> numbers = new ArrayList<>();
        while (matcher.find()) {
            try {
                numbers.add(new BigDecimal(matcher.group(1)));
            } catch (Exception ignored) {
                // 忽略非法数字片段
            }
        }
        if (numbers.isEmpty()) {
            return null;
        }

        BigDecimal result;
        if (numbers.size() == 1) {
            result = numbers.get(0).abs();
        } else {
            BigDecimal degree = numbers.get(0).abs();
            BigDecimal minute = numbers.get(1).abs();
            BigDecimal second = numbers.size() >= 3 ? numbers.get(2).abs() : BigDecimal.ZERO;
            result = degree
                    .add(minute.divide(BigDecimal.valueOf(60), 10, RoundingMode.HALF_UP))
                    .add(second.divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP));
        }
        return result.multiply(BigDecimal.valueOf(sign)).setScale(6, RoundingMode.HALF_UP);
    }

    private boolean isEmptyRow(SignboardExcelImportExcelVO row) {
        return StrUtil.isAllBlank(
                normalizeText(row.getSignboardName()),
                normalizeText(row.getRelationType()),
                normalizeText(row.getSignboardLevelLabel()),
                normalizeText(row.getAdminRegionName()),
                normalizeText(row.getSpecificLocation()),
                normalizeCoordinateText(row.getLongitudeLatitudeText()),
                normalizeText(row.getMaintenanceUnit()),
                normalizeText(row.getSignboardCode())
        );
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
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
        if (StrUtil.equalsAnyIgnoreCase(normalized, "null", "n/a", "na")) {
            return null;
        }
        return normalized;
    }

    private String normalizeCoordinateText(String text) {
        String normalized = normalizeText(text);
        if (normalized == null) {
            return null;
        }
        return normalized
                .replace('，', ',')
                .replace('\u00A0', ' ')
                .replace('\t', ' ')
                .trim();
    }

    private Integer parseInteger(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private static final class AreaContext {
        private Map<String, List<AreaCandidate>> exactNameMap;
    }

    private static final class AreaCandidate {
        private final String id;
        private final String name;

        private AreaCandidate(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final class FacilityContext {
        private Map<String, List<FacilityCandidate>> riverMap;
        private Map<String, List<FacilityCandidate>> reservoirMap;
    }

    private static final class FacilityCandidate {
        private final Long id;
        private final String name;

        private FacilityCandidate(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final class RelationBinding {
        private final String referenceType;
        private final Long referenceId;

        private RelationBinding(String referenceType, Long referenceId) {
            this.referenceType = referenceType;
            this.referenceId = referenceId;
        }
    }

    private static final class CoordinatePair {
        private final BigDecimal longitude;
        private final BigDecimal latitude;

        private CoordinatePair(BigDecimal longitude, BigDecimal latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }

    private static final class DictContext {
        private Map<String, String> labelToValue;
        private int nextValue;
        private int nextSort;
    }
}
