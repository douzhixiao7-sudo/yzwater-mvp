package com.sydigit.yzwater.module.iot.dal.dataobject.device;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备位置 DO
 */
@TableName("iot_device_location")
@KeySequence("iot_device_location_seq") // 用于 Oracle、PostgreSQL 等数据库的主键自增
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceLocationDO extends BaseDO {

    /**
     * 位置编号
     */
    @TableId
    private Long id;
    /**
     * 父级编号
     */
    private Long parentId;
    /**
     * 位置名称
     */
    private String name;
    /**
     * 排序
     */
    private Integer sort;

}
