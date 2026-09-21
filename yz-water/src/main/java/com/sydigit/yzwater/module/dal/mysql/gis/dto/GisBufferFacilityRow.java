package com.sydigit.yzwater.module.dal.mysql.gis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GisBufferFacilityRow {

    private Long facilityId;

    private String facilityType;

    private String facilityName;

    private String adminRegionCode;

    private String geomType;

    private BigDecimal longitude;

    private BigDecimal latitude;
}
