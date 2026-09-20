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
 * 巡检线路点位 DO
 */
@TableName("yz_equipment_inspection_line_point")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionLinePointDO extends TenantBaseDO {

    /**
     * 点位 ID
     */
    @TableId
    private Long id;

    /**
     * 线路 ID
     */
    private Long lineId;

    /**
     * 点位顺序
     */
    private Integer pointSort;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 点位类型（1 设备，2 位置，3 自定义坐标）
     */
    private Integer pointType;

    /**
     * 设备 ID（点位类型为设备时使用）
     */
    private Long deviceId;

    /**
     * 位置 ID（点位类型为位置时使用）
     */
    private Long locationId;

    /**
     * 经度（点位类型为自定义坐标时使用）
     */
    private BigDecimal longitude;

    /**
     * 纬度（点位类型为自定义坐标时使用）
     */
    private BigDecimal latitude;

    /**
     * 所属闸站
     */
    private String stationId;

    /**
     * 备注
     */
    private String remark;

}
