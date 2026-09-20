package com.sydigit.yzwater.module.service.flood;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrBatchSaveReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrListRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZrrSaveReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZrrDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxZrrMapper;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.dal.mysql.area.SystemAreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 防汛责任人服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class FxZrrService {

    private static final String TYPE_CITY = "1";
    private static final String TYPE_PARK = "2";

    private final YzFxZrrMapper zrrMapper;
    private final SystemAreaMapper systemAreaMapper;

    /**
     * 列表查询（按类型）
     */
    public List<FxZrrListRespVO> getList(String type) {
        String normalized = normalizeType(type);
        List<YzFxZrrDO> list = zrrMapper.selectListByType(normalized);
        return list.stream()
                .filter(Objects::nonNull)
                .map(this::buildListResp)
                .toList();
    }

    /**
     * 详情
     */
    public FxZrrSaveReqVO getDetail(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("责任人ID不能为空");
        }
        YzFxZrrDO exists = zrrMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "责任人不存在或已删除");
        }
        FxZrrSaveReqVO vo = new FxZrrSaveReqVO();
        vo.setId(exists.getId());
        vo.setType(exists.getType());
        vo.setDivisionCode(exists.getDivisionCode());
        vo.setAdministrativeName(exists.getAdministrativeName());
        vo.setAdministrativeTitle(exists.getAdministrativeTitle());
        vo.setTechnicalName(exists.getTechnicalName());
        vo.setTechnicalTitle(exists.getTechnicalTitle());
        vo.setSort(exists.getSort());
        return vo;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public String create(FxZrrSaveReqVO reqVO) {
        String type = normalizeType(reqVO.getType());
        String id = generateId();
        YzFxZrrDO insert = new YzFxZrrDO();
        insert.setId(id);
        fillFields(insert, reqVO, type, 1);
        zrrMapper.insert(insert);
        return id;
    }

    /**
     * 编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(FxZrrSaveReqVO reqVO) {
        String id = StrUtil.trimToNull(reqVO.getId());
        if (id == null) {
            throw ServiceExceptionUtil.invalidParamException("责任人ID不能为空");
        }
        YzFxZrrDO exists = zrrMapper.selectById(id);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "责任人不存在或已删除");
        }
        String type = normalizeType(reqVO.getType());
        YzFxZrrDO update = new YzFxZrrDO();
        update.setId(id);
        fillFields(update, reqVO, type, exists.getSort() == null ? 1 : exists.getSort());
        zrrMapper.updateById(update);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        String key = StrUtil.trimToNull(id);
        if (key == null) {
            throw ServiceExceptionUtil.invalidParamException("责任人ID不能为空");
        }
        YzFxZrrDO exists = zrrMapper.selectById(key);
        if (exists == null) {
            throw ServiceExceptionUtil.exception0(1_009_000_092, "责任人不存在或已删除");
        }
        zrrMapper.deleteById(key);
    }

    /**
     * 批量保存（用于园区或市级批量编辑）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(FxZrrBatchSaveReqVO reqVO) {
        String type = normalizeType(reqVO.getType());
        List<FxZrrBatchSaveReqVO.Item> items = normalizeItems(reqVO.getItems());
        List<YzFxZrrDO> existing = zrrMapper.selectListByType(type);
        Map<String, YzFxZrrDO> existsMap = existing.stream()
                .filter(Objects::nonNull)
                .filter(item -> StrUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(YzFxZrrDO::getId, item -> item, (a, b) -> a));
        Set<String> keepIds = new HashSet<>();

        for (int i = 0; i < items.size(); i++) {
            FxZrrBatchSaveReqVO.Item item = items.get(i);
            String itemId = StrUtil.trimToNull(item.getId());
            int defaultSort = i + 1;
            if (itemId != null && existsMap.containsKey(itemId)) {
                YzFxZrrDO update = new YzFxZrrDO();
                update.setId(itemId);
                fillFields(update, item, type, defaultSort);
                zrrMapper.updateById(update);
                keepIds.add(itemId);
                continue;
            }
            YzFxZrrDO insert = new YzFxZrrDO();
            insert.setId(generateId());
            fillFields(insert, item, type, defaultSort);
            zrrMapper.insert(insert);
        }

        if (!existing.isEmpty()) {
            List<String> removeIds = existing.stream()
                    .map(YzFxZrrDO::getId)
                    .filter(StrUtil::isNotBlank)
                    .filter(id -> !keepIds.contains(id))
                    .toList();
            if (!removeIds.isEmpty()) {
                zrrMapper.deleteBatchIds(removeIds);
            }
        }
    }

    private FxZrrListRespVO buildListResp(YzFxZrrDO item) {
        FxZrrListRespVO vo = new FxZrrListRespVO();
        vo.setId(item.getId());
        vo.setType(item.getType());
        vo.setDivisionCode(item.getDivisionCode());
        vo.setAdministrativeName(item.getAdministrativeName());
        vo.setAdministrativeTitle(item.getAdministrativeTitle());
        vo.setTechnicalName(item.getTechnicalName());
        vo.setTechnicalTitle(item.getTechnicalTitle());
        return vo;
    }

    private void fillFields(YzFxZrrDO target, FxZrrSaveReqVO source, String type, int defaultSort) {
        target.setType(type);
        target.setDivisionCode(resolveDivisionCode(type, source.getDivisionCode()));
        target.setAdministrativeName(StrUtil.trimToNull(source.getAdministrativeName()));
        target.setAdministrativeTitle(StrUtil.trimToNull(source.getAdministrativeTitle()));
        target.setTechnicalName(StrUtil.trimToNull(source.getTechnicalName()));
        target.setTechnicalTitle(StrUtil.trimToNull(source.getTechnicalTitle()));
        Integer sort = source.getSort();
        target.setSort(sort == null ? defaultSort : sort);
    }

    private void fillFields(YzFxZrrDO target, FxZrrBatchSaveReqVO.Item source, String type, int defaultSort) {
        target.setType(type);
        target.setDivisionCode(resolveDivisionCode(type, source.getDivisionCode()));
        target.setAdministrativeName(StrUtil.trimToNull(source.getAdministrativeName()));
        target.setAdministrativeTitle(StrUtil.trimToNull(source.getAdministrativeTitle()));
        target.setTechnicalName(StrUtil.trimToNull(source.getTechnicalName()));
        target.setTechnicalTitle(StrUtil.trimToNull(source.getTechnicalTitle()));
        Integer sort = source.getSort();
        target.setSort(sort == null ? defaultSort : sort);
    }

    private List<FxZrrBatchSaveReqVO.Item> normalizeItems(List<FxZrrBatchSaveReqVO.Item> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<FxZrrBatchSaveReqVO.Item> result = new ArrayList<>();
        for (FxZrrBatchSaveReqVO.Item item : items) {
            if (item == null) {
                continue;
            }
            boolean empty = StrUtil.isBlank(item.getDivisionCode())
                    && StrUtil.isBlank(item.getAdministrativeName())
                    && StrUtil.isBlank(item.getAdministrativeTitle())
                    && StrUtil.isBlank(item.getTechnicalName())
                    && StrUtil.isBlank(item.getTechnicalTitle());
            if (empty) {
                continue;
            }
            result.add(item);
        }
        return result;
    }

    private String normalizeType(String type) {
        String normalized = StrUtil.trimToNull(type);
        if (normalized == null) {
            throw ServiceExceptionUtil.invalidParamException("类型不能为空");
        }
        if (!TYPE_CITY.equals(normalized) && !TYPE_PARK.equals(normalized)) {
            throw ServiceExceptionUtil.invalidParamException("类型不合法");
        }
        return normalized;
    }

    private String resolveDivisionCode(String type, String divisionCode) {
        String trimmed = StrUtil.trimToNull(divisionCode);
        if (TYPE_PARK.equals(type)) {
            if (trimmed == null) {
                throw ServiceExceptionUtil.invalidParamException("园区区划代码不能为空");
            }
            return trimmed;
        }
        return null;
    }

    private String generateId() {
        return IdUtil.fastSimpleUUID();
    }

    public Map<String, String> loadDivisionNameMap(List<FxZrrListRespVO> list) {
        Map<String, String> result = new HashMap<>();
        if (list == null || list.isEmpty()) {
            return result;
        }
        Set<Long> ids = new HashSet<>();
        for (FxZrrListRespVO item : list) {
            if (item == null || StrUtil.isBlank(item.getDivisionCode())) {
                continue;
            }
            String code = item.getDivisionCode().trim();
            if (result.containsKey(code)) {
                continue;
            }
            Long id = parseAreaId(code);
            if (id == null) {
                result.putIfAbsent(code, code);
                continue;
            }
            ids.add(id);
        }
        if (!ids.isEmpty()) {
            List<SystemAreaDO> areas = systemAreaMapper.selectBatchIds(ids);
            Set<Long> found = new HashSet<>();
            for (SystemAreaDO area : areas) {
                if (area == null || area.getId() == null) {
                    continue;
                }
                found.add(area.getId());
                String key = String.valueOf(area.getId());
                String name = StrUtil.trimToNull(area.getName());
                result.putIfAbsent(key, name == null ? key : name);
            }
            for (Long id : ids) {
                String key = String.valueOf(id);
                if (!found.contains(id)) {
                    result.putIfAbsent(key, key);
                }
            }
        }
        return result;
    }

    private Long parseAreaId(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        try {
            return Long.parseLong(code.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
