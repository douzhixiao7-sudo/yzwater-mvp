package com.sydigit.yzwater.module.iot.dal.dataobject.doc;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备技术资料 DO
 */
@TableName(value = "yz_equipment_doc")
@KeySequence("yz_equipment_doc_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceDocDO extends TenantBaseDO {

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
     * 设备编号（快照）
     */
    private String deviceCode;
    /**
     * 设备名称（快照）
     */
    private String deviceName;
    /**
     * 设备型号（快照）
     */
    private String equipmentModel;
    /**
     * 设备类型（快照）
     */
    private String deviceType;
    /**
     * 产品分类（快照）
     */
    private String productCategory;
    /**
     * 资料类型（字典值）
     */
    private String docType;
    /**
     * 资料名称
     */
    private String docName;
    /**
     * 文件 ID
     */
    private Long fileId;
    /**
     * 文件地址
     */
    private String fileUrl;
    /**
     * 文件格式
     */
    private String fileFormat;
    /**
     * 资料备注
     */
    private String remark;
}
