package com.sydigit.yzwater.module.dal.mysql.gis.dto;

import lombok.Data;

/**
 * 缓冲区内水库查询结果
 */
@Data
public class GisReservoirRangeRow {

    private Long id;

    private String reservoirName;

    private String geomType;

    private String geomWkt;

    private Double distanceM;
}
