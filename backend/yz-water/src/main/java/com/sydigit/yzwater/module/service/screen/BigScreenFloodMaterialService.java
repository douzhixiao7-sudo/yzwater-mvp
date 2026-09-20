package com.sydigit.yzwater.module.service.screen;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.module.constants.ZdConstants;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenDictLabelValueRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialRescuePlanRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialTypeItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodMaterialWarehouseItemRespVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxQxdwDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxQxdwMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxWzMapper;
import com.sydigit.yzwater.module.service.flood.FxWzService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 大屏统计 - 防汛物资
 */
@Service
@Validated
@RequiredArgsConstructor
public class BigScreenFloodMaterialService {

    private final YzFxWzMapper wzMapper;
    private final FxWzService fxWzService;
    private final YzFloodPreventionMaterialWarehouseMapper warehouseMapper;
    private final YzFxQxdwMapper qxdwMapper;
    private final DictDataCommonApi dictDataApi;

    /**
     * 储备单位下拉
     */
    public List<BigScreenDictLabelValueRespVO> getWarehouseOptions() {
        List<YzFloodPreventionMaterialWarehouseDO> warehouseList = warehouseMapper.selectList();
        return warehouseList.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getId() != null)
                .sorted((a, b) -> {
                    String aName = StrUtil.blankToDefault(a.getWarehouseName(), "");
                    String bName = StrUtil.blankToDefault(b.getWarehouseName(), "");
                    return aName.compareToIgnoreCase(bName);
                })
                .map(item -> {
                    BigScreenDictLabelValueRespVO vo = new BigScreenDictLabelValueRespVO();
                    vo.setValue(String.valueOf(item.getId()));
                    vo.setLabel(StrUtil.blankToDefault(item.getWarehouseName(), String.valueOf(item.getId())));
                    return vo;
                })
                .toList();
    }

    /**
     * 按储备单位或防汛物资 ID 查询物资。
     * warehouseId 优先按物资 ID 解析，并返回同仓库地址/坐标下的全部物资；否则按储备单位 ID 查询。
     */
    public List<BigScreenFloodMaterialWarehouseItemRespVO> getMaterialListByWarehouse(String warehouseId) {
        String warehouseKey = StrUtil.trimToNull(warehouseId);
        List<YzFxWzDO> materialList;
        if (warehouseKey == null) {
            materialList = wzMapper.selectListOrderBySort();
        } else if (fxWzService.findMaterialById(warehouseKey) != null) {
            materialList = fxWzService.listMaterialsAtSameLocation(warehouseKey);
        } else {
            materialList = wzMapper.selectListByWarehouseId(warehouseKey);
        }
        if (materialList.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, YzFloodPreventionMaterialWarehouseDO> warehouseMap = buildWarehouseMap();
        int[] index = {1};
        return materialList.stream()
                .filter(Objects::nonNull)
                .map(item -> buildWarehouseItem(item, warehouseMap.get(item.getUnitId()), index[0]++))
                .toList();
    }

    /**
     * 查询全部防汛物资，返回格式保持“按物资类型查询”接口兼容。
     */
    public List<BigScreenFloodMaterialTypeItemRespVO> getMaterialListByType() {
        List<YzFxWzDO> materialList = wzMapper.selectListOrderBySort();
        if (materialList.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, String> unitLabelMap = loadDictLabelMap(ZdConstants.ZD_WZDW);
        Map<String, YzFloodPreventionMaterialWarehouseDO> warehouseMap = buildWarehouseMap();
        return materialList.stream()
                .filter(Objects::nonNull)
                .map(item -> buildMaterialTypeItem(item, warehouseMap.get(item.getUnitId()), unitLabelMap))
                .toList();
    }

    /**
     * 抢险队伍计划
     */
    public List<BigScreenFloodMaterialRescuePlanRespVO> getRescuePlanList() {
        List<YzFxQxdwDO> planList = qxdwMapper.selectListOrderBySort();
        return planList.stream()
                .filter(Objects::nonNull)
                .map(this::buildRescuePlanItem)
                .toList();
    }

    private Map<String, YzFloodPreventionMaterialWarehouseDO> buildWarehouseMap() {
        List<YzFloodPreventionMaterialWarehouseDO> warehouseList = warehouseMapper.selectList();
        Map<String, YzFloodPreventionMaterialWarehouseDO> map = new LinkedHashMap<>();
        for (YzFloodPreventionMaterialWarehouseDO item : warehouseList) {
            if (item == null || item.getId() == null) {
                continue;
            }
            map.put(String.valueOf(item.getId()), item);
        }
        return map;
    }

    private BigScreenFloodMaterialWarehouseItemRespVO buildWarehouseItem(YzFxWzDO item,
                                                                         YzFloodPreventionMaterialWarehouseDO warehouse,
                                                                         int serialNo) {
        BigScreenFloodMaterialWarehouseItemRespVO vo = new BigScreenFloodMaterialWarehouseItemRespVO();
        vo.setSerialNo(serialNo);
        vo.setMaterialName(item.getMaterialName());
        vo.setQuantity(item.getQuantity());
        vo.setWarehouseName(resolveWarehouseDisplayName(item, warehouse));
        vo.setWarehouseAddress(StrUtil.trimToNull(item.getWarehouseAddress()));
        vo.setMaterialId(item.getId());
        vo.setLongitude(item.getLongitude() != null ? item.getLongitude()
                : warehouse == null ? null : warehouse.getLongitude());
        vo.setLatitude(item.getLatitude() != null ? item.getLatitude()
                : warehouse == null ? null : warehouse.getLatitude());
        return vo;
    }

    private BigScreenFloodMaterialTypeItemRespVO buildMaterialTypeItem(YzFxWzDO item,
                                                                       YzFloodPreventionMaterialWarehouseDO warehouse,
                                                                       Map<String, String> unitLabelMap) {
        BigScreenFloodMaterialTypeItemRespVO vo = new BigScreenFloodMaterialTypeItemRespVO();
        vo.setMaterialName(item.getMaterialName());
        vo.setQuantity(item.getQuantity());
        vo.setUnitLabel(resolveDictLabel(item.getUnit(), unitLabelMap));
        vo.setIsDelegateStorage(item.getIsDelegateStorage());
        vo.setStorageUnit(resolveWarehouseName(item, warehouse));
        vo.setLeaderName(warehouse == null ? "" : StrUtil.blankToDefault(warehouse.getLeaderName(), ""));
        vo.setLeaderPhone(warehouse == null ? "" : StrUtil.blankToDefault(warehouse.getLeaderPhone(), ""));
        return vo;
    }

    private BigScreenFloodMaterialRescuePlanRespVO buildRescuePlanItem(YzFxQxdwDO item) {
        BigScreenFloodMaterialRescuePlanRespVO vo = new BigScreenFloodMaterialRescuePlanRespVO();
        vo.setUnitName(StrUtil.blankToDefault(item.getUnitName(), "-"));
        vo.setPlanCount(item.getPlanCount() == null ? 0 : item.getPlanCount());
        return vo;
    }

    private String resolveWarehouseName(YzFxWzDO material, YzFloodPreventionMaterialWarehouseDO warehouse) {
        if (warehouse != null && StrUtil.isNotBlank(warehouse.getWarehouseName())) {
            return warehouse.getWarehouseName();
        }
        return StrUtil.blankToDefault(material.getStorageUnit(), "-");
    }

    private String resolveWarehouseDisplayName(YzFxWzDO material, YzFloodPreventionMaterialWarehouseDO warehouse) {
        String address = StrUtil.trimToNull(material.getWarehouseAddress());
        if (address != null) {
            return address;
        }
        return resolveWarehouseName(material, warehouse);
    }

    private String resolveDictLabel(String value, Map<String, String> labelMap) {
        if (StrUtil.isBlank(value)) {
            return "-";
        }
        return StrUtil.blankToDefault(labelMap.get(value), value);
    }

    private Map<String, String> loadDictLabelMap(String dictType) {
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
}
