package com.sydigit.yzwater.module.dal.mysql.gis.dto;

import lombok.Data;

/**
 * 缓冲区内河道查询结果
 */
@Data
public class GisRiverChannelRangeRow {

    private Long id;

    private String riverName;

    private Integer riverSectionCount;

    private String geomType;

    private String geomWkt;

    private Double distanceM;
}
