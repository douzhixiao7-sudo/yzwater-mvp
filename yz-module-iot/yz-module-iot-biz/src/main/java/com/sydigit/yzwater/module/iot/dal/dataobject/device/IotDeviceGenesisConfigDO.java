package com.sydigit.yzwater.module.iot.dal.dataobject.device;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备 GENESIS64 连接配置 DO
 */
@TableName("iot_device_genesis_config")
@KeySequence("iot_device_genesis_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceGenesisConfigDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 产品编号
     *
     * 关联 {@link IotProductDO#getId()}
     */
    private Long productId;

    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;

    /**
     * GENESIS64 实时数据接口地址
     */
    private String baseUrl;

    private String username;

    private String password;

    /**
     * 请求超时时间，单位毫秒
     */
    private Integer timeout;

    /**
     * 采集间隔，单位毫秒
     */
    private Integer collectInterval;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
