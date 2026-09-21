package com.sydigit.yzwater.module.iot.dal.dataobject.rating;

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

import java.time.LocalDate;

/**
 * 设备评级记录 DO
 */
@TableName(value = "yz_equipment_rating", autoResultMap = true)
@KeySequence("yz_equipment_rating_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceRatingDO extends TenantBaseDO {

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
     * 评级时间
     */
    private LocalDate ratingTime;
    /**
     * 评级人用户 ID
     */
    private Long ratingUserId;
    /**
     * 评级人姓名
     */
    private String ratingUserName;
    /**
     * 评级依据
     */
    private String ratingBasis;
    /**
     * 评级结果
     */
    private String ratingResult;
    /**
     * 整改建议
     */
    private String rectifyAdvice;
    /**
     * 整改期限
     */
    private LocalDate rectifyDeadline;
    /**
     * 附件
     */
    @TableField(value = "attachments", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] attachments;
}
