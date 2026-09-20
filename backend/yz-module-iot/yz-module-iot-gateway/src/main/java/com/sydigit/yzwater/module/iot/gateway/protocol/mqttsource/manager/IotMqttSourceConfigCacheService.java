package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.manager;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotProtocolTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;

/**
 * IoT MQTT Source 配置缓存服务
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttSourceConfigCacheService {

    private final IotDeviceCommonApi deviceApi;

    private final Map<Long, List<IotMqttDeviceConfigRespDTO>> sourceConfigCache = new ConcurrentHashMap<>();
    private final Set<Long> knownSourceIds = ConcurrentHashMap.newKeySet();

    public Map<Long, List<IotMqttDeviceConfigRespDTO>> refreshConfig() {
        try {
            CommonResult<List<IotMqttDeviceConfigRespDTO>> result = deviceApi.getMqttDeviceConfigList(
                    new IotMqttDeviceConfigListReqDTO()
                            .setStatus(CommonStatusEnum.ENABLE.getStatus())
                            .setSourceEnabled(Boolean.TRUE)
                            .setProtocolType(IotProtocolTypeEnum.MQTT_SOURCE.getType()));
            if (result == null) {
                log.warn("[refreshConfig][MQTT Source RPC 接口返回空结果，已跳过本轮刷新，请检查主程序 "
                        + "/rpc-api/iot/mqtt/config-list 接口是否可用]");
                return null;
            }
            if (!result.isSuccess()) {
                log.warn("[refreshConfig][MQTT Source RPC 接口返回失败，code={} msg={}，已跳过本轮刷新，"
                                + "请检查主程序是否已升级并开放 /rpc-api/iot/mqtt/config-list 接口]",
                        result.getCode(), result.getMsg());
                return null;
            }
            Map<Long, List<IotMqttDeviceConfigRespDTO>> sourceConfigMap = CollectionUtils.convertMultiMap(
                    result.getData(), IotMqttDeviceConfigRespDTO::getSourceId);
            sourceConfigCache.putAll(sourceConfigMap);
            return sourceConfigMap;
        } catch (Exception e) {
            log.error("[refreshConfig][刷新 MQTT Source 配置失败]", e);
            return null;
        }
    }

    public List<IotMqttDeviceConfigRespDTO> getConfigsBySourceId(Long sourceId) {
        return sourceConfigCache.get(sourceId);
    }

    public Set<Long> cleanupRemovedSources(Set<Long> currentSourceIds) {
        Set<Long> removedSourceIds = new HashSet<>(knownSourceIds);
        removedSourceIds.removeAll(currentSourceIds);
        for (Long sourceId : removedSourceIds) {
            log.info("[cleanupRemovedSources][清理已删除 MQTT Source 数据源: {}, deviceCount={}]", sourceId,
                    convertList(sourceConfigCache.remove(sourceId), IotMqttDeviceConfigRespDTO::getDeviceId).size());
        }
        knownSourceIds.clear();
        knownSourceIds.addAll(currentSourceIds);
        return removedSourceIds;
    }

}
