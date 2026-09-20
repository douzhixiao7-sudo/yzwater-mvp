package com.sydigit.yzwater.module.iot.service.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourcePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourceSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotMqttSourceDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IoT MQTT 数据源 Service 接口
 */
public interface IotMqttSourceService {

    Long createSource(@Valid IotMqttSourceSaveReqVO createReqVO);

    void updateSource(@Valid IotMqttSourceSaveReqVO updateReqVO);

    void deleteSource(Long id);

    IotMqttSourceDO getSource(Long id);

    PageResult<IotMqttSourceDO> getSourcePage(IotMqttSourcePageReqVO pageReqVO);

    List<IotMqttSourceDO> getSourceList(Boolean enabled);

    Map<Long, IotMqttSourceDO> getSourceMap(Collection<Long> ids);

    IotMqttSourceDO validateSourceExists(Long id);

}
