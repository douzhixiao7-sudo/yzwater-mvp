package com.sydigit.yzwater.module.system.service.dict;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.system.dal.redis.dict.DictDataRedisDAO;
import com.sydigit.yzwater.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import com.sydigit.yzwater.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import com.sydigit.yzwater.module.system.dal.dataobject.dict.DictDataDO;
import com.sydigit.yzwater.module.system.dal.dataobject.dict.DictTypeDO;
import com.sydigit.yzwater.module.system.dal.mysql.dict.DictDataMapper;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.system.enums.ErrorCodeConstants.*;

/**
 * 字典数据 Service 实现类
 *
 * @author ruoyi
 */
@Service
@Slf4j
public class DictDataServiceImpl implements DictDataService {

    /**
     * 排序 dictType > sort
     */
    private static final Comparator<DictDataDO> COMPARATOR_TYPE_AND_SORT = Comparator
            .comparing(DictDataDO::getDictType)
            .thenComparingInt(DictDataDO::getSort);

    @Resource
    private DictTypeService dictTypeService;

    @Resource
    private DictDataMapper dictDataMapper;

    @Resource
    private DictDataRedisDAO dictDataRedisDAO;

    @Override
    public List<DictDataDO> getDictDataList(Integer status, String dictType) {
        String type = StrUtil.trimToNull(dictType);
        if (type != null) {
            List<DictDataDO> list = getDictDataListByDictType(type);
            if (status == null) {
                return list;
            }
            return list.stream()
                    .filter(item -> Objects.equals(status, item.getStatus()))
                    .sorted(Comparator.comparing(DictDataDO::getSort))
                    .collect(Collectors.toList());
        }
        List<DictDataDO> list = dictDataMapper.selectListByStatusAndDictType(status, dictType);
        list.sort(COMPARATOR_TYPE_AND_SORT);
        return list;
    }

    @Override
    public PageResult<DictDataDO> getDictDataPage(DictDataPageReqVO pageReqVO) {
        return dictDataMapper.selectPage(pageReqVO);
    }

    @Override
    public DictDataDO getDictData(Long id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public Long createDictData(DictDataSaveReqVO createReqVO) {
        // 校验字典类型有效
        validateDictTypeExists(createReqVO.getDictType());
        // 校验字典数据的值的唯一性
        validateDictDataValueUnique(null, createReqVO.getDictType(), createReqVO.getValue());

        // 插入字典类型
        DictDataDO dictData = BeanUtils.toBean(createReqVO, DictDataDO.class);
        dictDataMapper.insert(dictData);
        // 新增后同步刷新缓存（避免 /river/channel/dict 等高频接口读取到旧数据）
        refreshDictDataCacheAfterCommit(CollUtil.newArrayList(createReqVO.getDictType()));
        return dictData.getId();
    }

    @Override
    public void updateDictData(DictDataSaveReqVO updateReqVO) {
        // 校验自己存在
        DictDataDO before = validateDictDataExists(updateReqVO.getId());
        // 校验字典类型有效
        validateDictTypeExists(updateReqVO.getDictType());
        // 校验字典数据的值的唯一性
        validateDictDataValueUnique(updateReqVO.getId(), updateReqVO.getDictType(), updateReqVO.getValue());

        // 更新字典类型
        DictDataDO updateObj = BeanUtils.toBean(updateReqVO, DictDataDO.class);
        dictDataMapper.updateById(updateObj);

        // 修改后同步刷新缓存：若 dictType 发生变化，需要同时刷新旧类型与新类型
        refreshDictDataCacheAfterCommit(CollUtil.newArrayList(
                before == null ? null : before.getDictType(),
                updateReqVO.getDictType()
        ));
    }

    @Override
    public void deleteDictData(Long id) {
        // 校验是否存在
        DictDataDO dictData = validateDictDataExists(id);

        // 删除字典数据
        dictDataMapper.deleteById(id);
        // 删除后同步刷新缓存
        refreshDictDataCacheAfterCommit(CollUtil.newArrayList(dictData == null ? null : dictData.getDictType()));
    }

    @Override
    public void deleteDictDataList(List<Long> ids) {
        // 先查询受影响的 dictType，再删除并刷新对应缓存
        List<DictDataDO> dictDataList = dictDataMapper.selectByIds(ids);
        dictDataMapper.deleteByIds(ids);
        Set<String> dictTypes = dictDataList.stream()
                .map(DictDataDO::getDictType)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        refreshDictDataCacheAfterCommit(dictTypes);
    }

    @Override
    public long getDictDataCountByDictType(String dictType) {
        return dictDataMapper.selectCountByDictType(dictType);
    }

    @VisibleForTesting
    public void validateDictDataValueUnique(Long id, String dictType, String value) {
        DictDataDO dictData = dictDataMapper.selectByDictTypeAndValue(dictType, value);
        if (dictData == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典数据
        if (id == null) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
        if (!dictData.getId().equals(id)) {
            throw exception(DICT_DATA_VALUE_DUPLICATE);
        }
    }

    @VisibleForTesting
    public DictDataDO validateDictDataExists(Long id) {
        if (id == null) {
            return null;
        }
        DictDataDO dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw exception(DICT_DATA_NOT_EXISTS);
        }
        return dictData;
    }

    @VisibleForTesting
    public void validateDictTypeExists(String type) {
        DictTypeDO dictType = dictTypeService.getDictType(type);
        if (dictType == null) {
            throw exception(DICT_TYPE_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(dictType.getStatus())) {
            throw exception(DICT_TYPE_NOT_ENABLE);
        }
    }

    @Override
    public void validateDictDataList(String dictType, Collection<String> values) {
        if (CollUtil.isEmpty(values)) {
            return;
        }
        Map<String, DictDataDO> dictDataMap = CollectionUtils.convertMap(
                dictDataMapper.selectByDictTypeAndValues(dictType, values), DictDataDO::getValue);
        // 校验
        values.forEach(value -> {
            DictDataDO dictData = dictDataMap.get(value);
            if (dictData == null) {
                throw exception(DICT_DATA_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dictData.getStatus())) {
                throw exception(DICT_DATA_NOT_ENABLE, dictData.getLabel());
            }
        });
    }

    @Override
    public DictDataDO getDictData(String dictType, String value) {
        return dictDataMapper.selectByDictTypeAndValue(dictType, value);
    }

    @Override
    public DictDataDO parseDictData(String dictType, String label) {
        return dictDataMapper.selectByDictTypeAndLabel(dictType, label);
    }

    @Override
    public List<DictDataDO> getDictDataListByDictType(String dictType) {
        String type = StrUtil.trimToNull(dictType);
        if (type == null) {
            return List.of();
        }

        // 先从 Redis 获取（1天有效期），未命中再查库并回填
        List<DictDataDO> cached = dictDataRedisDAO.getDictDataList(type);
        if (cached != null) {
            cached.sort(Comparator.comparing(DictDataDO::getSort));
            return cached;
        }

        List<DictDataDO> list = loadDictDataListFromDB(type);
        dictDataRedisDAO.setDictDataList(type, list);
        return list;
    }

    private List<DictDataDO> loadDictDataListFromDB(String dictType) {
        List<DictDataDO> list = dictDataMapper.selectList(DictDataDO::getDictType, dictType);
        list.sort(Comparator.comparing(DictDataDO::getSort));
        return list;
    }

    private void refreshDictDataCacheAfterCommit(Collection<String> dictTypes) {
        if (CollUtil.isEmpty(dictTypes)) {
            return;
        }
        Runnable refreshTask = () -> dictTypes.stream()
                .map(StrUtil::trimToNull)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .forEach(dictType -> dictDataRedisDAO.setDictDataList(dictType, loadDictDataListFromDB(dictType)));

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    refreshTask.run();
                }
            });
            return;
        }
        refreshTask.run();
    }

}
