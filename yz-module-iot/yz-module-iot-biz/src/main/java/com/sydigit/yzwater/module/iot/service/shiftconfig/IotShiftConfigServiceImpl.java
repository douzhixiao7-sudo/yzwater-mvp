package com.sydigit.yzwater.module.iot.service.shiftconfig;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo.IotShiftConfigSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftconfig.IotShiftConfigDO;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftconfig.IotShiftConfigMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftconfig.IotShiftConfigScheduleRefMapper;
import com.sydigit.yzwater.module.system.api.user.AdminUserApi;
import com.sydigit.yzwater.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_CONFIG_CROSS_DAY_REQUIRED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_CONFIG_DELETE_FORBIDDEN_REFERENCED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_CONFIG_NAME_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_CONFIG_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_CONFIG_TIME_INVALID;

/**
 * 班次配置 Service 实现
 */
@Service
@Validated
public class IotShiftConfigServiceImpl implements IotShiftConfigService {

    private static final String SHIFT_NO_PREFIX = "BC-";

    @Resource
    private IotShiftConfigMapper shiftConfigMapper;
    @Resource
    private IotShiftConfigScheduleRefMapper shiftScheduleMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createShiftConfig(IotShiftConfigSaveReqVO createReqVO) {
        String shiftName = trimToNull(createReqVO.getShiftName());
        validateShiftNameUnique(null, shiftName);
        validateShiftTime(createReqVO.getStartTime(), createReqVO.getEndTime(), createReqVO.getCrossDay());

        IotShiftConfigDO config = new IotShiftConfigDO();
        config.setShiftNo(generateShiftNo());
        config.setShiftName(shiftName);
        config.setStationId(trimToEmpty(createReqVO.getStationId()));
        config.setStartTime(createReqVO.getStartTime());
        config.setEndTime(createReqVO.getEndTime());
        config.setCrossDay(toCrossDay(createReqVO.getCrossDay()));
        config.setStatus(0);
        config.setRemark(trimToEmpty(createReqVO.getRemark()));
        shiftConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShiftConfig(IotShiftConfigSaveReqVO updateReqVO) {
        IotShiftConfigDO existed = validateShiftConfigExists(updateReqVO.getId());
        String shiftName = trimToNull(updateReqVO.getShiftName());
        validateShiftNameUnique(existed.getId(), shiftName);
        validateShiftTime(updateReqVO.getStartTime(), updateReqVO.getEndTime(), updateReqVO.getCrossDay());

        IotShiftConfigDO updateObj = new IotShiftConfigDO();
        updateObj.setId(existed.getId());
        updateObj.setShiftName(shiftName);
        updateObj.setStationId(trimToEmpty(updateReqVO.getStationId()));
        updateObj.setStartTime(updateReqVO.getStartTime());
        updateObj.setEndTime(updateReqVO.getEndTime());
        updateObj.setCrossDay(toCrossDay(updateReqVO.getCrossDay()));
        updateObj.setRemark(trimToEmpty(updateReqVO.getRemark()));
        shiftConfigMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShiftConfig(Long id) {
        validateShiftConfigExists(id);
        validateCanDelete(id);
        shiftConfigMapper.deleteById(id);
    }

    @Override
    public IotShiftConfigRespVO getShiftConfig(Long id) {
        IotShiftConfigDO config = validateShiftConfigExists(id);
        return buildResp(config, buildCreatorMap(Collections.singletonList(config)));
    }

    @Override
    public PageResult<IotShiftConfigRespVO> getShiftConfigPage(IotShiftConfigPageReqVO pageReqVO) {
        PageResult<IotShiftConfigDO> pageResult = shiftConfigMapper.selectPage(pageReqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(pageResult.getList());
        List<IotShiftConfigRespVO> respList = CollectionUtils.convertList(pageResult.getList(), item -> buildResp(item, creatorMap));
        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public List<IotShiftConfigRespVO> getShiftConfigList(IotShiftConfigPageReqVO reqVO) {
        List<IotShiftConfigDO> list = shiftConfigMapper.selectListByReqVO(reqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(list);
        return CollectionUtils.convertList(list, item -> buildResp(item, creatorMap));
    }

    /**
     * 校验班次存在
     */
    private IotShiftConfigDO validateShiftConfigExists(Long id) {
        IotShiftConfigDO config = shiftConfigMapper.selectById(id);
        if (config == null) {
            throw exception(SHIFT_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    /**
     * 校验班次名称唯一
     */
    private void validateShiftNameUnique(Long id, String shiftName) {
        IotShiftConfigDO existed = shiftConfigMapper.selectByShiftName(shiftName);
        if (existed == null) {
            return;
        }
        if (!Objects.equals(existed.getId(), id)) {
            throw exception(SHIFT_CONFIG_NAME_EXISTS);
        }
    }

    /**
     * 校验班次时间规则
     */
    private void validateShiftTime(LocalTime startTime, LocalTime endTime, Boolean crossDay) {
        if (startTime == null || endTime == null) {
            throw exception(SHIFT_CONFIG_TIME_INVALID);
        }
        if (endTime.isAfter(startTime)) {
            return;
        }
        if (!Boolean.TRUE.equals(crossDay)) {
            throw exception(SHIFT_CONFIG_CROSS_DAY_REQUIRED);
        }
    }

    /**
     * 校验是否可删除
     */
    private void validateCanDelete(Long shiftId) {
        if (!Boolean.TRUE.equals(shiftScheduleMapper.existsShiftScheduleTable())) {
            return;
        }
        Long count = shiftScheduleMapper.selectCountByShiftId(shiftId);
        if (count != null && count > 0) {
            throw exception(SHIFT_CONFIG_DELETE_FORBIDDEN_REFERENCED);
        }
    }

    /**
     * 生成班次编号（BC-001 递增）
     */
    private String generateShiftNo() {
        Integer maxSeq = shiftConfigMapper.selectMaxShiftNoSeq(SHIFT_NO_PREFIX, SHIFT_NO_PREFIX.length());
        int nextSeq = maxSeq == null ? 1 : maxSeq + 1;
        return SHIFT_NO_PREFIX + String.format("%03d", nextSeq);
    }

    /**
     * 构建响应对象
     */
    private IotShiftConfigRespVO buildResp(IotShiftConfigDO config, Map<Long, AdminUserRespDTO> creatorMap) {
        IotShiftConfigRespVO respVO = new IotShiftConfigRespVO();
        respVO.setId(config.getId());
        respVO.setShiftNo(config.getShiftNo());
        respVO.setShiftName(config.getShiftName());
        respVO.setStationId(config.getStationId());
        respVO.setStartTime(config.getStartTime());
        respVO.setEndTime(config.getEndTime());
        respVO.setCrossDay(Objects.equals(config.getCrossDay(), 1));
        respVO.setRemark(config.getRemark());
        respVO.setCreator(resolveCreatorName(config.getCreator(), creatorMap));
        respVO.setCreateTime(config.getCreateTime());
        return respVO;
    }

    /**
     * 批量构建创建人映射
     */
    private Map<Long, AdminUserRespDTO> buildCreatorMap(List<IotShiftConfigDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> creatorIds = new LinkedHashSet<>();
        for (IotShiftConfigDO config : list) {
            Long creatorId = parseUserId(config == null ? null : config.getCreator());
            if (creatorId != null) {
                creatorIds.add(creatorId);
            }
        }
        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserApi.getUserMap(creatorIds);
    }

    /**
     * 解析创建人中文名称
     */
    private String resolveCreatorName(String creator, Map<Long, AdminUserRespDTO> creatorMap) {
        Long creatorId = parseUserId(creator);
        if (creatorId == null) {
            return trimToEmpty(creator);
        }
        AdminUserRespDTO user = creatorMap.get(creatorId);
        if (user == null) {
            return trimToEmpty(creator);
        }
        String nickname = trimToNull(user.getNickname());
        return nickname == null ? String.valueOf(creatorId) : nickname;
    }

    private Long parseUserId(String creator) {
        String value = trimToNull(creator);
        if (value == null || !StrUtil.isNumeric(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    private Integer toCrossDay(Boolean crossDay) {
        return Boolean.TRUE.equals(crossDay) ? 1 : 0;
    }

    private String trimToNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private String trimToEmpty(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "" : normalized;
    }
}
