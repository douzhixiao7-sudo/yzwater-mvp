package com.sydigit.yzwater.module.dal.mysql.gis.dto;

import lombok.Data;

/**
 * 缓冲范围内设施基础信息行
 */
@Data
public class GisFacilityBaseRangeRow {

    private Long facilityId;

    private String facilityType;

    private String adminRegionCode;
}
