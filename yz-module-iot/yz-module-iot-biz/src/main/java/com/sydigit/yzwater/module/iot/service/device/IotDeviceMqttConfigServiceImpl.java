package com.sydigit.yzwater.module.iot.service.device;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttConfigSaveReqVO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotProtocolTypeEnum;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttConfigDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMqttConfigMapper;
import com.sydigit.yzwater.module.iot.service.product.IotProductService;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotMqttSourceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * IoT 设备 MQTT 配置 Service 实现类
 */
@Service
@Validated
public class IotDeviceMqttConfigServiceImpl implements IotDeviceMqttConfigService {

    @Resource
    private IotDeviceMqttConfigMapper deviceMqttConfigMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotProductService productService;

    @Resource
    private IotMqttSourceService mqttSourceService;

    @Override
    public void saveDeviceMqttConfig(IotDeviceMqttConfigSaveReqVO saveReqVO) {
        IotDeviceDO device = deviceService.validateDeviceExists(saveReqVO.getDeviceId());
        IotProductDO product = productService.getProduct(device.getProductId());
        Assert.notNull(product, "产品不存在");
        validateMqttConfigByProtocolType(saveReqVO, product.getProtocolType());
        mqttSourceService.validateSourceExists(saveReqVO.getSourceId());

        IotDeviceMqttConfigDO existConfig = deviceMqttConfigMapper.selectByDeviceId(saveReqVO.getDeviceId());
        if (existConfig == null) {
            IotDeviceMqttConfigDO mqttConfig = BeanUtils.toBean(saveReqVO, IotDeviceMqttConfigDO.class,
                    o -> o.setProductId(device.getProductId()));
            deviceMqttConfigMapper.insert(mqttConfig);
            return;
        }
        IotDeviceMqttConfigDO updateObj = BeanUtils.toBean(saveReqVO, IotDeviceMqttConfigDO.class,
                o -> o.setId(existConfig.getId()).setProductId(device.getProductId()));
        deviceMqttConfigMapper.updateById(updateObj);
    }

    @Override
    public IotDeviceMqttConfigDO getDeviceMqttConfig(Long id) {
        return deviceMqttConfigMapper.selectById(id);
    }

    @Override
    public IotDeviceMqttConfigDO getDeviceMqttConfigByDeviceId(Long deviceId) {
        return deviceMqttConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public List<IotDeviceMqttConfigDO> getDeviceMqttConfigList(IotMqttDeviceConfigListReqDTO listReqDTO) {
        return deviceMqttConfigMapper.selectList(listReqDTO);
    }

    private void validateMqttConfigByProtocolType(IotDeviceMqttConfigSaveReqVO saveReqVO, String protocolType) {
        Assert.isTrue(IotProtocolTypeEnum.MQTT_SOURCE.getType().equals(protocolType),
                "只有 MQTT Source 协议的设备才允许配置 MQTT 采集");
        Assert.isTrue(StrUtil.isNotBlank(saveReqVO.getTopic()), "订阅主题不能为空");
        Assert.isTrue(StrUtil.isNotBlank(saveReqVO.getPayloadMode()), "负载模式不能为空");
        if (StrUtil.isNotBlank(saveReqVO.getReportTimeKey())) {
            Assert.isTrue(StrUtil.isNotBlank(saveReqVO.getReportTimeFormat()), "配置报文时间字段后，时间格式不能为空");
        }
    }

}
