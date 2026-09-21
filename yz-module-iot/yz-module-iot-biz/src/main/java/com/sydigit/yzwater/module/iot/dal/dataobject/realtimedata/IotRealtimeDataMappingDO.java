package com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata;

import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IoT 实时数据点位映射 DO
 */
@TableName("iot_realtime_data_mapping")
@KeySequence("iot_realtime_data_mapping_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotRealtimeDataMappingDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 采集源编号
     */
    private Long sourceId;
    /**
     * 点位名称
     */
    private String pointName;
    /**
     * 设备编号
     */
    private Long deviceId;
    /**
     * 物模型标识符
     */
    private String identifier;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 同步状态
     */
    private String syncStatus;
    /**
     * 同步说明
     */
    private String syncMessage;
    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

}