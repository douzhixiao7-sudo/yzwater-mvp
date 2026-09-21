package com.sydigit.yzwater.module.iot.gateway.protocol.genesis.manager;

import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotProtocolTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * IoT GENESIS64 配置缓存服务
 */
@RequiredArgsConstructor
@Slf4j
public class IotGenesisConfigCacheService {

    private final IotDeviceCommonApi deviceApi;

    private final Map<Long, IotGenesisDeviceConfigRespDTO> configCache = new ConcurrentHashMap<>();
    private final Set<Long> knownDeviceIds = ConcurrentHashMap.newKeySet();

    public List<IotGenesisDeviceConfigRespDTO> refreshConfig() {
        try {
            CommonResult<List<IotGenesisDeviceConfigRespDTO>> result = deviceApi.getGenesisDeviceConfigList(
                    new IotGenesisDeviceConfigListReqDTO().setStatus(CommonStatusEnum.ENABLE.getStatus())
                            .setProtocolType(IotProtocolTypeEnum.GENESIS64_HTTP.getType()));
            if (result == null) {
                log.warn("[refreshConfig][GENESIS64 RPC 接口返回空结果，已跳过本轮刷新，请检查主程序 "
                        + "/rpc-api/iot/genesis/config-list 接口是否可用]");
                return null;
            }
            if (!result.isSuccess()) {
                log.warn("[refreshConfig][GENESIS64 RPC 接口返回失败，code={} msg={}，已跳过本轮刷新，"
                                + "请检查主程序是否已升级并开放 /rpc-api/iot/genesis/config-list 接口]",
                        result.getCode(), result.getMsg());
                return null;
            }
            List<IotGenesisDeviceConfigRespDTO> configs = result.getData();
            for (IotGenesisDeviceConfigRespDTO config : configs) {
                configCache.put(config.getDeviceId(), config);
            }
            return configs;
        } catch (Exception e) {
            log.error("[refreshConfig][刷新 GENESIS64 配置失败]", e);
            return null;
        }
    }

    public IotGenesisDeviceConfigRespDTO getConfig(Long deviceId) {
        return configCache.get(deviceId);
    }

    public Set<Long> cleanupRemovedDevices(List<IotGenesisDeviceConfigRespDTO> currentConfigs) {
        Set<Long> currentDeviceIds = convertSet(currentConfigs, IotGenesisDeviceConfigRespDTO::getDeviceId);
        Set<Long> removedDeviceIds = new HashSet<>(knownDeviceIds);
        removedDeviceIds.removeAll(currentDeviceIds);
        for (Long deviceId : removedDeviceIds) {
            log.info("[cleanupRemovedDevices][清理已删除 GENESIS64 设备: {}]", deviceId);
            configCache.remove(deviceId);
        }
        knownDeviceIds.clear();
        knownDeviceIds.addAll(currentDeviceIds);
        return removedDeviceIds;
    }

}
