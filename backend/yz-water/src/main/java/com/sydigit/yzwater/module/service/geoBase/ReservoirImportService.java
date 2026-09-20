package com.sydigit.yzwater.module.service.geoBase;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.lang.Snowflake;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.excel.core.util.ExcelUtils;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.water.ReservoirImportExcelVO;
import com.sydigit.yzwater.module.controller.admin.vo.water.ReservoirImportRespVO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.enums.ErrorCodeConstants;
import com.sydigit.yzwater.module.util.ManagementUnitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 水库 Excel 导入服务
 */
@Service
@Validated
@Slf4j
public class ReservoirImportService {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-M-d");
    private static final String FACILITY_TYPE_RESERVOIR = "reservoir";

    private final YzWaterFacilityBaseMapper baseMapper;
    private final YzWaterReservoirMapper reservoirMapper;
    private final DictDataCommonApi dictDataApi;

    public ReservoirImportService(YzWaterFacilityBaseMapper baseMapper,
                                  YzWaterReservoirMapper reservoirMapper,
                                  DictDataCommonApi dictDataApi) {
        this.baseMapper = baseMapper;
        this.reservoirMapper = reservoirMapper;
        this.dictDataApi = dictDataApi;
    }

    /**
     * 导入水库基础与业务信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservoirImportRespVO importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_FILE_EMPTY);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !isExcelFile(filename)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_TYPE_INVALID);
        }
        List<ReservoirImportExcelVO> rows;
        try {
            rows = ExcelUtils.read(file, ReservoirImportExcelVO.class);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_EXCEL_PARSE_ERROR,
                    StrUtil.blankToDefault(ex.getMessage(), "文件格式错误"));
        }
        ReservoirImportRespVO respVO = new ReservoirImportRespVO();
        if (CollUtil.isEmpty(rows)) {
            return respVO;
        }
        respVO.setTotalCount(rows.size());
        Map<String, String> scaleDict = loadDictMap(ZdConstants.ZD_SKGM);
        Map<String, String> natureDict = loadDictMap(ZdConstants.ZD_SKXZ);

        List<YzWaterFacilityBaseDO> baseList = new ArrayList<>(rows.size());
        List<YzWaterReservoirDO> reservoirList = new ArrayList<>(rows.size());
        int rowIndex = 1;
        for (int i = 0; i < rows.size() - 1; i++) {
            int currentRow = rowIndex++;
            ReservoirImportExcelVO row = rows.get(i + 1);
            try {
                Long baseId = SNOWFLAKE.nextId();
                YzWaterFacilityBaseDO base = buildBase(row, baseId);
                YzWaterReservoirDO reservoir = buildReservoir(row, baseId, scaleDict, natureDict);
                baseList.add(base);
                reservoirList.add(reservoir);
            } catch (Exception ex) {
                log.warn("导入水库数据第 {} 行失败", currentRow, ex);
                respVO.addError("第" + currentRow + "行失败：" + StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
            }
        }
        if (CollUtil.isNotEmpty(baseList)) {
            baseMapper.insertBatch(baseList);
            reservoirMapper.insertBatch(reservoirList);
            respVO.setSuccessCount(baseList.size());
        }
        return respVO;
    }

    private boolean isExcelFile(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private Map<String, String> loadDictMap(String dictType) {
        Map<String, String> map = new HashMap<>();
        List<DictDataRespDTO> dictDataList = dictDataApi.getDictDataList(dictType);
        for (DictDataRespDTO dictData : dictDataList) {
            String value = StrUtil.blankToDefault(dictData.getValue(), "").trim();
            String label = StrUtil.blankToDefault(dictData.getLabel(), "").trim();
            if (StrUtil.isBlank(value)) {
                continue;
            }
            map.put(normalizeDictKey(value), value);
            if (StrUtil.isNotBlank(label)) {
                map.put(normalizeDictKey(label), value);
            }
        }
        return map;
    }

    private String normalizeDictKey(String text) {
        return StrUtil.blankToDefault(text, "").trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化导入文本：去除首尾空格，遇到“/”或“＿”等占位符时视为空值
     */
    private String normalizeText(String text) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String normalized = text.trim();
        if (normalized.contains("/") || normalized.contains("＿") || normalized.contains("_") || normalized.contains("—")) {
            return null;
        }
        return normalized;
    }

    private YzWaterFacilityBaseDO buildBase(ReservoirImportExcelVO row, Long baseId) {
        YzWaterFacilityBaseDO base = new YzWaterFacilityBaseDO();
        base.setId(baseId);
        base.setFacilityCode(IdUtil.fastSimpleUUID());
        base.setFacilityName(normalizeText(row.getReservoirName()));
        base.setFacilityType(FACILITY_TYPE_RESERVOIR);
        base.setAdminRegion(normalizeText(row.getTownship()));
        base.setManageUnit(normalizeText(row.getManagementUnit()));
        base.setSourceType("import");
        return base;
    }

    private YzWaterReservoirDO buildReservoir(ReservoirImportExcelVO row,
                                              Long baseId,
                                              Map<String, String> scaleDict,
                                              Map<String, String> natureDict) {
        YzWaterReservoirDO reservoir = new YzWaterReservoirDO();
        reservoir.setId(SNOWFLAKE.nextId());
        reservoir.setFacilityId(baseId);
        reservoir.setReservoirCode(IdUtil.fastSimpleUUID());
        reservoir.setReservoirName(normalizeText(row.getReservoirName()));
        reservoir.setReservoirScale(mapDictValue(scaleDict, row.getReservoirScale(), ZdConstants.ZD_SKGM, "规模"));
        String township = normalizeText(row.getTownship());
        reservoir.setTownship(township == null ? null : new String[]{township});
        // managementUnit 已升级为 text[]，这里将 Excel 单元格内容拆分后按数组存储
        reservoir.setManagementUnit(ManagementUnitUtils.parseManagementUnit(row.getManagementUnit()));
        reservoir.setReservoirNature(mapDictValue(natureDict, row.getReservoirNature(), ZdConstants.ZD_SKXZ, "水库性质"));
        reservoir.setCatchmentArea(toBigDecimal(row.getCatchmentArea(), "集水面积"));
        reservoir.setElevationDatum(normalizeText(row.getElevationDatum()));
        reservoir.setSeismicIntensity(normalizeText(row.getSeismicIntensity()));
        reservoir.setCompletionDate(toLocalDate(row.getCompletionDate(), "竣工日期"));
        reservoir.setReinforcementStartDate(toLocalDate(row.getReinforcementStartDate(), "除险加固开工年月"));
        reservoir.setReinforcementEndDate(toLocalDate(row.getReinforcementEndDate(), "除险加固竣工年月"));
        reservoir.setDesignReturnPeriod(toInteger(row.getDesignReturnPeriod(), "重现期设计(年)"));
        reservoir.setCheckReturnPeriod(toInteger(row.getCheckReturnPeriod(), "重现期校核(年)"));
        reservoir.setTotalCapacity(toBigDecimal(row.getTotalCapacity(), "总库容"));
        reservoir.setActiveCapacity(toBigDecimal(row.getActiveCapacity(), "兴利库容"));
        reservoir.setFloodControlCapacity(toBigDecimal(row.getFloodControlCapacity(), "调洪库容"));
        reservoir.setDeadCapacity(toBigDecimal(normalizeText(row.getDeadCapacity()),"死库容"));
        reservoir.setVerifiedFloodLevel(toBigDecimal(row.getVerifiedFloodLevel(), "校核水位"));
        reservoir.setDesignFloodLevel(toBigDecimal(row.getDesignFloodLevel(), "设计水位"));
        reservoir.setNormalOperatingLevel(toBigDecimal(row.getNormalOperatingLevel(), "兴利水位"));
        reservoir.setFloodLimitLevel(toBigDecimal(row.getFloodLimitLevel(), "汛限水位"));
        reservoir.setDeadLevel(toBigDecimal(normalizeText(row.getDeadLevel()), "死水位"));
        reservoir.setDamCrestElevation(normalizeText(row.getDamCrestElevation()));
        reservoir.setDamTopWidth(normalizeText(row.getDamTopWidth()));
        reservoir.setMaxDamHeight(toBigDecimal(row.getMaxDamHeight(), "最大坝高"));
        reservoir.setDamTopLength(row.getDamTopLength());
        reservoir.setWaveWallCrestElevation(toBigDecimal(row.getWaveWallCrestElevation(), "挡浪墙顶高程"));
        reservoir.setDamRoadSurfaceType(normalizeText(row.getDamRoadSurfaceType()));
        reservoir.setSeepageControlType(normalizeText(row.getSeepageControlType()));
        reservoir.setSeepagePileRange(normalizeText(row.getSeepagePileRange()));
        reservoir.setSeepageElevRange(normalizeText(row.getSeepageElevRange()));
        reservoir.setSlopeProtectionType(normalizeText(row.getSlopeProtectionType()));
        reservoir.setSlopeProtectionElevRange(normalizeText(row.getSlopeProtectionElevRange()));
        reservoir.setSpillwayControlType(normalizeText(row.getSpillwayControlType()));
        reservoir.setSpillwayHasBridge(toBoolean(row.getSpillwayHasBridge(), "溢洪道有无交通桥"));
        reservoir.setSpillwayCrestElevation(toBigDecimal(row.getSpillwayCrestElevation(), "溢洪道堰顶高程"));
        reservoir.setSpillwayBottomWidth(normalizeText(row.getSpillwayBottomWidth()));
        reservoir.setSpillwayMaxDischarge(toBigDecimal(row.getSpillwayMaxDischarge(), "溢洪道最大流量"));
        reservoir.setFloodChannelName(normalizeText(row.getFloodChannelName()));
        reservoir.setFloodChannelSafeDischarge(normalizeText(row.getFloodChannelSafeDischarge()));
        reservoir.setCulvertType(normalizeText(row.getCulvertType()));
        reservoir.setCulvertSectionSize(normalizeText(row.getCulvertSectionSize()));
        reservoir.setCulvertGateType(normalizeText(row.getCulvertGateType()));
        reservoir.setCulvertDesignDischarge(toBigDecimal(row.getCulvertDesignDischarge(), "灌溉涵洞设计流量"));
        reservoir.setDesignIrrigationArea(toBigDecimal(row.getDesignIrrigationArea(), "设计灌溉面积"));
        reservoir.setActualIrrigationArea(row.getActualIrrigationArea());
        reservoir.setAnnualWaterSupply(toBigDecimal(row.getAnnualWaterSupply(), "年供水量"));
        reservoir.setFisheryArea(toBigDecimal(row.getFisheryArea(), "宜鱼面积"));
        reservoir.setWaterSource(toBoolean(row.getWaterSource(), "是否水源地"));
        reservoir.setWaterSupplyTarget(normalizeText(row.getWaterSupplyTarget()));
        return reservoir;
    }

    private String mapDictValue(Map<String, String> dict, String input, String dictType, String column) {
        if (StrUtil.isBlank(input)) {
            return null;
        }
        String key = normalizeDictKey(input);
        String value = dict.get(key);
        if (value == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.YZ_DICT_VALUE_NOT_FOUND, dictType, column + ":" + input);
        }
        return value;
    }

    private BigDecimal toBigDecimal(String text, String column) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return new BigDecimal(text.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(column + " 数值格式错误：" + text);
        }
    }

    private Integer toInteger(String text, String column) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        try {
            return new BigDecimal(text.trim()).intValue();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(column + " 数值格式错误：" + text);
        }
    }

    private LocalDate toLocalDate(String text, String column) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String normalized = text.trim()
                .replace("/", "-")
                .replace(".", "-")
                .replace("年", "-")
                .replace("月", "-")
                .replace("日", "-");
        normalized = normalized.replaceAll("-+", "-");
        normalized = StrUtil.removePrefix(normalized, "-");
        normalized = StrUtil.removeSuffix(normalized, "-");
        try {
            if (StrUtil.count(normalized, "-") == 1) {
                normalized = normalized + "-01";
            }
            return LocalDate.parse(normalized, DATE_FORMAT);
        } catch (Exception ignore) {
            // 继续尝试数字日期
        }
        try {
            double excelDate = Double.parseDouble(normalized);
            return LocalDate.of(1899, 12, 30).plusDays((long) excelDate);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(column + " 日期格式无法解析：" + text);
        }
    }

    private Boolean toBoolean(String text, String column) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        String value = text.trim().toLowerCase(Locale.ROOT);
        if (StrUtil.equalsAny(value, "是", "有", "y", "yes", "true", "1")) {
            return true;
        }
        if (StrUtil.equalsAny(value, "否", "无", "n", "no", "false", "0")) {
            return false;
        }
        throw new IllegalArgumentException(column + " 值无法识别：" + text);
    }
}
