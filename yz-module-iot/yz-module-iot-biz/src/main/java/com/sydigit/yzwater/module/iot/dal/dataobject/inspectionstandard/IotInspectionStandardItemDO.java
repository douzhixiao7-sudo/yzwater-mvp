package com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 巡检标准检查项 DO
 */
@TableName("yz_equipment_inspection_standard_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionStandardItemDO extends TenantBaseDO {

    /**
     * 检查项 ID
     */
    @TableId
    private Long id;

    /**
     * 标准 ID
     */
    private Long standardId;

    /**
     * 标准适用对象 ID
     */
    private Long targetRefId;

    /**
     * 检查项名称
     */
    private String itemName;

    /**
     * 检查项描述
     */
    private String itemDesc;

    /**
     * 合格规则
     */
    private String qualifiedRule;

    /**
     * 是否需要上传附件（0 否，1 是）
     */
    private Integer needUploadAttachment;

    /**
     * 默认检查结果
     */
    private String defaultResult;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 记录项数量（冗余）
     */
    private Integer recordCount;

}
