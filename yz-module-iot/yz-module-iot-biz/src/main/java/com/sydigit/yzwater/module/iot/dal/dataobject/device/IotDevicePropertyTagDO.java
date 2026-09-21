package com.sydigit.yzwater.module.iot.dal.dataobject.device;

import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备属性标签 DO
 */
@TableName("iot_device_property_tag")
@KeySequence("iot_device_property_tag_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDevicePropertyTagDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 物模型属性标识符
     */
    private String identifier;
    /**
     * 标签名称
     */
    private String tagName;

}
