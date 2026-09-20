package com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 巡检线路 DO
 */
@TableName("yz_equipment_inspection_line")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionLineDO extends TenantBaseDO {

    /**
     * 线路 ID
     */
    @TableId
    private Long id;

    /**
     * 所属闸站
     */
    private String stationId;

    /**
     * 线路名称
     */
    private String lineName;

    /**
     * 巡检类型
     */
    private String inspectionType;

    /**
     * 区域类型
     */
    private String areaType;

    /**
     * 线路描述
     */
    private String lineDesc;

    /**
     * 起点经度
     */
    private BigDecimal startLongitude;

    /**
     * 起点纬度
     */
    private BigDecimal startLatitude;

    /**
     * 终点经度
     */
    private BigDecimal endLongitude;

    /**
     * 终点纬度
     */
    private BigDecimal endLatitude;

    /**
     * 线路总长度（米）
     */
    private BigDecimal totalLengthMeter;

    /**
     * 点位数量（冗余）
     */
    private Integer pointCount;

    /**
     * 使用次数（冗余）
     */
    private Long useCount;

    /**
     * 状态（0 启用，1 停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
