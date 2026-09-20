package com.sydigit.yzwater.module.iot.service.device;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisConfigSaveReqVO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotProtocolTypeEnum;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisConfigDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceGenesisConfigMapper;
import com.sydigit.yzwater.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * IoT 设备 GENESIS64 连接配置 Service 实现类
 */
@Service
@Validated
public class IotDeviceGenesisConfigServiceImpl implements IotDeviceGenesisConfigService {

    @Resource
    private IotDeviceGenesisConfigMapper genesisConfigMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotProductService productService;

    @Override
    public void saveDeviceGenesisConfig(IotDeviceGenesisConfigSaveReqVO saveReqVO) {
        IotDeviceDO device = deviceService.validateDeviceExists(saveReqVO.getDeviceId());
        IotProductDO product = productService.getProduct(device.getProductId());
        Assert.notNull(product, "产品不存在");
        validateGenesisConfigByProtocolType(saveReqVO, product.getProtocolType());

        IotDeviceGenesisConfigDO existConfig = genesisConfigMapper.selectByDeviceId(saveReqVO.getDeviceId());
        if (existConfig == null) {
            IotDeviceGenesisConfigDO genesisConfig = BeanUtils.toBean(saveReqVO, IotDeviceGenesisConfigDO.class,
                    o -> o.setProductId(device.getProductId()));
            genesisConfigMapper.insert(genesisConfig);
            return;
        }
        IotDeviceGenesisConfigDO updateObj = BeanUtils.toBean(saveReqVO, IotDeviceGenesisConfigDO.class,
                o -> o.setId(existConfig.getId()).setProductId(device.getProductId()));
        genesisConfigMapper.updateById(updateObj);
    }

    @Override
    public IotDeviceGenesisConfigDO getDeviceGenesisConfig(Long id) {
        return genesisConfigMapper.selectById(id);
    }

    @Override
    public IotDeviceGenesisConfigDO getDeviceGenesisConfigByDeviceId(Long deviceId) {
        return genesisConfigMapper.selectByDeviceId(deviceId);
    }

    @Override
    public List<IotDeviceGenesisConfigDO> getDeviceGenesisConfigList(IotGenesisDeviceConfigListReqDTO listReqDTO) {
        return genesisConfigMapper.selectList(listReqDTO);
    }

    private void validateGenesisConfigByProtocolType(IotDeviceGenesisConfigSaveReqVO saveReqVO, String protocolType) {
        Assert.isTrue(IotProtocolTypeEnum.GENESIS64_HTTP.getType().equals(protocolType),
                "只有 GENESIS64 HTTP 协议的设备才允许配置 GENESIS64 采集");
        Assert.isTrue(StrUtil.isNotBlank(saveReqVO.getBaseUrl()), "GENESIS64 接口地址不能为空");
        Assert.isTrue(StrUtil.isNotBlank(saveReqVO.getUsername()), "GENESIS64 用户名不能为空");
        Assert.isTrue(StrUtil.isNotBlank(saveReqVO.getPassword()), "GENESIS64 密码不能为空");
        Assert.notNull(saveReqVO.getTimeout(), "请求超时时间不能为空");
        Assert.isTrue(saveReqVO.getTimeout() > 0, "请求超时时间必须大于 0");
        Assert.notNull(saveReqVO.getCollectInterval(), "采集频率不能为空");
        Assert.isTrue(saveReqVO.getCollectInterval() > 0, "采集频率必须大于 0");
    }

}
