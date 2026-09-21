package com.sydigit.yzwater.module.dal.mysql.gis.dto;

import lombok.Data;

/**
 * 缓冲范围内设施类型统计行
 */
@Data
public class GisFacilityTypeCountRow {

    private String facilityType;

    private Long count;
}
