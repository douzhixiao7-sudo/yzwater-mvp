package com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata;

import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 实时数据采集源 DO
 */
@TableName("iot_realtime_data_source")
@KeySequence("iot_realtime_data_source_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotRealtimeDataSourceDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 采集源名称
     */
    private String name;
    /**
     * 采集源编码
     */
    private String code;
    /**
     * 是否启用
     */
    private Boolean enabled;
    /**
     * 拉取地址
     */
    private String url;
    /**
     * Basic Auth 用户名
     */
    private String username;
    /**
     * Basic Auth 密码
     */
    private String password;
    /**
     * 请求体 JSON
     */
    private String requestBody;
    /**
     * 调度 Cron
     */
    private String cron;
    /**
     * 备注
     */
    private String remark;

}