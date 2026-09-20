package com.sydigit.yzwater.module.iot.dal.dataobject.accident;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.sydigit.yzwater.module.iot.dal.mybatis.typehandler.StringArrayTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 设备事故 DO
 */
@TableName(value = "yz_equipment_accident", autoResultMap = true)
@KeySequence("yz_equipment_accident_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceAccidentDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;
    /**
     * 设备 ID
     */
    private Long deviceId;
    /**
     * 事故发生时间
     */
    private LocalDateTime accidentTime;
    /**
     * 事故地点
     */
    private String accidentLocation;
    /**
     * 事故类型
     */
    private String accidentType;
    /**
     * 事故描述
     */
    private String accidentDesc;
    /**
     * 处理结果
     */
    private String handleResult;
    /**
     * 损失评估
     */
    private String lossAssessment;
    /**
     * 责任人用户 ID
     */
    private Long responsibleUserId;
    /**
     * 责任人
     */
    private String responsibleName;
    /**
     * 附件
     */
    @TableField(value = "attachments", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] attachments;
}