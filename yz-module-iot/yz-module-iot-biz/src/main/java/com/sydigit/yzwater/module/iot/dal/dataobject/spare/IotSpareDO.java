package com.sydigit.yzwater.module.iot.dal.dataobject.spare;

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

/**
 * 备件台账 DO
 */
@TableName(value = "yz_equipment_spare", autoResultMap = true)
@KeySequence("yz_equipment_spare_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotSpareDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;
    /**
     * 备件名称
     */
    private String spareName;
    /**
     * 备件规格
     */
    private String spareSpec;
    /**
     * 备件型号
     */
    private String spareModel;
    /**
     * 备件类型（字典值）
     */
    private String spareType;
    /**
     * 所属站点
     */
    private String stationId;
    /**
     * 关联设备 ID
     */
    private Long deviceId;
    /**
     * 生产厂家
     */
    private String manufacturer;
    /**
     * 库存数量
     */
    private Integer stockQty;
    /**
     * 最低库存阈值
     */
    private Integer minStock;
    /**
     * 存放位置
     */
    private String storageLocation;
    /**
     * 库管员
     */
    private String keeperName;
    /**
     * 备件图片（多张）
     */
    @TableField(value = "spare_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] spareImages;
    /**
     * 备注
     */
    private String remark;

}
