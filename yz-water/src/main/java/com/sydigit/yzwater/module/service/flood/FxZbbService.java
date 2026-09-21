package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZbbItemRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZbbListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZbbSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZbbDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZbbListDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZbbListMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZbbMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 值班表服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxZbbService {

    private final YzFxZbbMapper zbbMapper;
    private final YzFxZbbListMapper zbbListMapper;

    /**
     * 列表查询
     */
    public List<FxZbbListRespVO> getList() {
        List<YzFxZbbDO> headers = zbbMapper.selectListOrderByDateDesc();
        if (headers == null || headers.isEmpty()) {
            return List.of();
        }
        List<String> ids = headers.stream()
                .filter(Objects::nonNull)
                .map(YzFxZbbDO::getId)
                .filter(StrUtil::isNotBlank)
                .toList();
        Map<String, List<YzFxZbbListDO>> itemMap = loadItemsMap(ids);
        List<FxZbbListRespVO> result = new ArrayList<>(headers.size());
        for (YzFxZbbDO header : headers) {
            if (header == null) {
                continue;
            }
            List<YzFxZbbListDO> items = itemMap.getOrDefault(header.getId(), List.of());
            result.add(buildListResp(header, items));
        }
        return result;
    }

    /**
     * 导出列表（可按 ID 过滤）
     */
    public List<FxZbbListRespVO> getExportList(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            return getList();
        }
        YzFxZbbDO header = zbbMapper.selectById(key);
        if (header == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "值班表不存在或已删除");
        }
        List<YzFxZbbListDO> items = zbbListMapper.selectListByZbbId(key);
        return List.of(buildListResp(header, items));
    }

    /**
     * 详情
     */
    public FxZbbSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("值班表ID不能为空");
        }
        YzFxZbbDO header = zbbMapper.selectById(key);
        if (header == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "值班表不存在或已删除");
        }
        List<YzFxZbbListDO> items = zbbListMapper.selectListByZbbId(key);
        return buildSaveReq(header, items);
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxZbbSaveReqVO reqVO) {
        String id = generateId();
        YzFxZbbDO insert = new YzFxZbbDO();
        insert.setId(id);
        fillHeaderFields(insert, reqVO);
        zbbMapper.insert(insert);
        saveItems(id, reqVO.getItems());
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxZbbSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("值班表ID不能为空");
        }
        YzFxZbbDO header = zbbMapper.selectById(id);
        if (header == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "值班表不存在或已删除");
        }
        YzFxZbbDO update = new YzFxZbbDO();
        update.setId(id);
        fillHeaderFields(update, reqVO);
        zbbMapper.updateById(update);
        if (hasItemContent(reqVO.getItems())) {
            zbbListMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<YzFxZbbListDO>()
                    .eq(YzFxZbbListDO::getZbbId, id));
            saveItems(id, reqVO.getItems());
        }
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("值班表ID不能为空");
        }
        YzFxZbbDO header = zbbMapper.selectById(key);
        if (header == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "值班表不存在或已删除");
        }
        zbbMapper.deleteById(key);
        zbbListMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<YzFxZbbListDO>()
                .eq(YzFxZbbListDO::getZbbId, key));
    }

    private Map<String, List<YzFxZbbListDO>> loadItemsMap(List<String> zbbIds) {
        if (zbbIds == null || zbbIds.isEmpty()) {
            return Map.of();
        }
        List<YzFxZbbListDO> items = zbbListMapper.selectListByZbbIds(zbbIds);
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        Map<String, List<YzFxZbbListDO>> result = new HashMap<>();
        for (YzFxZbbListDO item : items) {
            if (item == null || StrUtil.isBlank(item.getZbbId())) {
                continue;
            }
            result.computeIfAbsent(item.getZbbId(), ignored -> new ArrayList<>()).add(item);
        }
        return result;
    }

    private FxZbbListRespVO buildListResp(YzFxZbbDO header, List<YzFxZbbListDO> items) {
        FxZbbListRespVO vo = new FxZbbListRespVO();
        vo.setId(header.getId());
        vo.setStartDate(header.getStartDate());
        vo.setEndDate(header.getEndDate());
        vo.setDescription(header.getDescription());
        vo.setItems(buildItemRespList(items));
        return vo;
    }

    private FxZbbSaveReqVO buildSaveReq(YzFxZbbDO header, List<YzFxZbbListDO> items) {
        FxZbbSaveReqVO vo = new FxZbbSaveReqVO();
        vo.setId(header.getId());
        vo.setStartDate(header.getStartDate());
        vo.setEndDate(header.getEndDate());
        vo.setDescription(header.getDescription());
        vo.setItems(buildItemSaveList(items));
        return vo;
    }

    private List<FxZbbItemRespVO> buildItemRespList(List<YzFxZbbListDO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .map(this::buildItemResp)
                .sorted(Comparator.comparingInt(item -> toWeekSortValue(item.getWeekDay())))
                .toList();
    }

    private FxZbbItemRespVO buildItemResp(YzFxZbbListDO item) {
        FxZbbItemRespVO vo = new FxZbbItemRespVO();
        vo.setId(item.getId());
        vo.setWeekDay(normalizeWeekDayForView(item.getWeekDay()));
        vo.setDutyChiefName(item.getDutyChiefName());
        vo.setDutyChiefMobile(item.getDutyChiefMobile());
        vo.setSectionChiefName(item.getSectionChiefName());
        vo.setSectionChiefMobile(item.getSectionChiefMobile());
        vo.setDutyStaffName(item.getDutyStaffName());
        vo.setDutyStaffMobile(item.getDutyStaffMobile());
        return vo;
    }

    private List<FxZbbSaveReqVO.Item> buildItemSaveList(List<YzFxZbbListDO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<FxZbbSaveReqVO.Item> result = new ArrayList<>();
        for (YzFxZbbListDO item : items) {
            if (item == null) {
                continue;
            }
            FxZbbSaveReqVO.Item vo = new FxZbbSaveReqVO.Item();
            vo.setId(item.getId());
            vo.setWeekDay(normalizeWeekDayForView(item.getWeekDay()));
            vo.setDutyChiefName(item.getDutyChiefName());
            vo.setDutyChiefMobile(item.getDutyChiefMobile());
            vo.setSectionChiefName(item.getSectionChiefName());
            vo.setSectionChiefMobile(item.getSectionChiefMobile());
            vo.setDutyStaffName(item.getDutyStaffName());
            vo.setDutyStaffMobile(item.getDutyStaffMobile());
            result.add(vo);
        }
        result.sort(Comparator.comparingInt(item -> toWeekSortValue(item.getWeekDay())));
        return result;
    }

    private void fillHeaderFields(YzFxZbbDO target, FxZbbSaveReqVO source) {
        target.setStartDate(StrUtil.trimToNull(source.getStartDate()));
        target.setEndDate(StrUtil.trimToNull(source.getEndDate()));
        target.setDescription(StrUtil.trimToNull(source.getDescription()));
    }

    private void saveItems(String zbbId, List<FxZbbSaveReqVO.Item> items) {
        List<YzFxZbbListDO> insertList = buildItemInsertList(zbbId, items);
        if (insertList.isEmpty()) {
            return;
        }
        zbbListMapper.insertBatch(insertList);
    }

    private List<YzFxZbbListDO> buildItemInsertList(String zbbId, List<FxZbbSaveReqVO.Item> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        Map<Integer, FxZbbSaveReqVO.Item> uniqueMap = new HashMap<>();
        for (FxZbbSaveReqVO.Item item : items) {
            if (item == null) {
                continue;
            }
            Integer weekDay = normalizeWeekDay(item.getWeekDay());
            if (weekDay == null) {
                continue;
            }
            FxZbbSaveReqVO.Item clone = new FxZbbSaveReqVO.Item();
            clone.setWeekDay(weekDay);
            clone.setDutyChiefName(item.getDutyChiefName());
            clone.setDutyChiefMobile(item.getDutyChiefMobile());
            clone.setSectionChiefName(item.getSectionChiefName());
            clone.setSectionChiefMobile(item.getSectionChiefMobile());
            clone.setDutyStaffName(item.getDutyStaffName());
            clone.setDutyStaffMobile(item.getDutyStaffMobile());
            uniqueMap.put(weekDay, clone);
        }
        if (uniqueMap.isEmpty()) {
            return List.of();
        }
        List<Integer> orderedDays = uniqueMap.keySet().stream().sorted().toList();
        List<YzFxZbbListDO> result = new ArrayList<>(orderedDays.size());
        for (Integer weekDay : orderedDays) {
            FxZbbSaveReqVO.Item item = uniqueMap.get(weekDay);
            YzFxZbbListDO insert = new YzFxZbbListDO();
            insert.setId(generateId());
            insert.setZbbId(zbbId);
            insert.setWeekDay(weekDay);
            insert.setDutyChiefName(StrUtil.trimToNull(item.getDutyChiefName()));
            insert.setDutyChiefMobile(StrUtil.trimToNull(item.getDutyChiefMobile()));
            insert.setSectionChiefName(StrUtil.trimToNull(item.getSectionChiefName()));
            insert.setSectionChiefMobile(StrUtil.trimToNull(item.getSectionChiefMobile()));
            insert.setDutyStaffName(StrUtil.trimToNull(item.getDutyStaffName()));
            insert.setDutyStaffMobile(StrUtil.trimToNull(item.getDutyStaffMobile()));
            insert.setSort(toWeekSortValue(weekDay));
            result.add(insert);
        }
        return result;
    }

    private Integer normalizeWeekDay(Integer value) {
        if (value == null) {
            return null;
        }
        if (value == 7) {
            return 0;
        }
        if (value < 0 || value > 6) {
            return null;
        }
        return value;
    }

    private boolean hasItemContent(List<FxZbbSaveReqVO.Item> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (FxZbbSaveReqVO.Item item : items) {
            if (item == null) {
                continue;
            }
            boolean hasText = StrUtil.isNotBlank(item.getDutyChiefName())
                    || StrUtil.isNotBlank(item.getDutyChiefMobile())
                    || StrUtil.isNotBlank(item.getSectionChiefName())
                    || StrUtil.isNotBlank(item.getSectionChiefMobile())
                    || StrUtil.isNotBlank(item.getDutyStaffName())
                    || StrUtil.isNotBlank(item.getDutyStaffMobile());
            if (hasText) {
                return true;
            }
        }
        return false;
    }

    private Integer normalizeWeekDayForView(Integer value) {
        if (value == null) {
            return null;
        }
        if (value == 7) {
            return 0;
        }
        return value;
    }

    private int toWeekSortValue(Integer weekDay) {
        if (weekDay == null) {
            return Integer.MAX_VALUE;
        }
        return weekDay == 0 ? 7 : weekDay;
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }
}
