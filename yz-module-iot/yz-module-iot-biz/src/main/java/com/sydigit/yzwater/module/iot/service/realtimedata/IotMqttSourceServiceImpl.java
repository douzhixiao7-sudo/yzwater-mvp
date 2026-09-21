package com.sydigit.yzwater.module.iot.service.realtimedata;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourcePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourceSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotMqttSourceDO;
import com.sydigit.yzwater.module.iot.dal.mysql.realtimedata.IotMqttSourceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertMap;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.MQTT_SOURCE_CODE_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.MQTT_SOURCE_NOT_EXISTS;

/**
 * IoT MQTT 数据源 Service 实现类
 */
@Service
@Validated
public class IotMqttSourceServiceImpl implements IotMqttSourceService {

    @Resource
    private IotMqttSourceMapper mqttSourceMapper;

    @Override
    public Long createSource(IotMqttSourceSaveReqVO createReqVO) {
        validateCodeUnique(createReqVO.getCode(), null);
        IotMqttSourceDO source = BeanUtils.toBean(createReqVO, IotMqttSourceDO.class);
        mqttSourceMapper.insert(source);
        return source.getId();
    }

    @Override
    public void updateSource(IotMqttSourceSaveReqVO updateReqVO) {
        validateSourceExists(updateReqVO.getId());
        validateCodeUnique(updateReqVO.getCode(), updateReqVO.getId());
        mqttSourceMapper.updateById(BeanUtils.toBean(updateReqVO, IotMqttSourceDO.class));
    }

    @Override
    public void deleteSource(Long id) {
        validateSourceExists(id);
        mqttSourceMapper.deleteById(id);
    }

    @Override
    public IotMqttSourceDO getSource(Long id) {
        return mqttSourceMapper.selectById(id);
    }

    @Override
    public PageResult<IotMqttSourceDO> getSourcePage(IotMqttSourcePageReqVO pageReqVO) {
        return mqttSourceMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotMqttSourceDO> getSourceList(Boolean enabled) {
        return mqttSourceMapper.selectListByEnabled(enabled);
    }

    @Override
    public Map<Long, IotMqttSourceDO> getSourceMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(mqttSourceMapper.selectListByIds(ids), IotMqttSourceDO::getId);
    }

    @Override
    public IotMqttSourceDO validateSourceExists(Long id) {
        IotMqttSourceDO source = mqttSourceMapper.selectById(id);
        if (source == null) {
            throw exception(MQTT_SOURCE_NOT_EXISTS);
        }
        return source;
    }

    private void validateCodeUnique(String code, Long id) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        IotMqttSourceDO source = mqttSourceMapper.selectByCode(code);
        if (source == null) {
            return;
        }
        if (id == null || !source.getId().equals(id)) {
            throw exception(MQTT_SOURCE_CODE_EXISTS);
        }
    }

}
