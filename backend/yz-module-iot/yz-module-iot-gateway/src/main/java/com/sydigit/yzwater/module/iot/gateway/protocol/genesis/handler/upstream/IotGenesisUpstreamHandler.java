package com.sydigit.yzwater.module.iot.gateway.protocol.genesis.handler.upstream;

import cn.hutool.core.collection.CollUtil;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisPointRespDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.sydigit.yzwater.module.iot.core.mq.message.IotDeviceMessage;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.client.IotGenesisRealtimeDataItem;
import com.sydigit.yzwater.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IoT GENESIS64 上行数据处理器
 */
@Slf4j
public class IotGenesisUpstreamHandler {

    private final IotDeviceMessageService messageService;
    private final String serverId;

    public IotGenesisUpstreamHandler(IotDeviceMessageService messageService, String serverId) {
        this.messageService = messageService;
        this.serverId = serverId;
    }

    public void handleCollectResult(IotGenesisDeviceConfigRespDTO config, List<IotGenesisRealtimeDataItem> dataItems) {
        if (CollUtil.isEmpty(dataItems) || CollUtil.isEmpty(config.getPoints())) {
            return;
        }
        try {
            Map<String, IotGenesisPointRespDTO> pointMap = convertMap(config.getPoints(),
                    IotGenesisPointRespDTO::getPointName, Function.identity());
            Map<String, Object> params = new LinkedHashMap<>();
            for (IotGenesisRealtimeDataItem dataItem : dataItems) {
                IotGenesisPointRespDTO point = pointMap.get(dataItem.getPointName());
                if (point == null || Objects.isNull(dataItem.getValue())) {
                    continue;
                }
                params.put(point.getIdentifier(), dataItem.getValue());
            }
            if (params.isEmpty()) {
                return;
            }
            IotDeviceMessage message = IotDeviceMessage.requestOf(
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);
            messageService.sendDeviceMessage(message, config.getProductKey(), config.getDeviceName(), serverId);
        } catch (Exception e) {
            log.error("[handleCollectResult][处理采集结果失败, deviceId={}]", config.getDeviceId(), e);
        }
    }

}
