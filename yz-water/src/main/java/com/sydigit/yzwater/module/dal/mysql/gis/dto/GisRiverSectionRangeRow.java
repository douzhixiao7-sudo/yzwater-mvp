package com.sydigit.yzwater.module.dal.mysql.gis.dto;

import lombok.Data;

/**
 * 缓冲区内河段查询结果
 */
@Data
public class GisRiverSectionRangeRow {

    private Long id;

    private Long riverChannelId;

    private String sectionName;

    private String riverName;

    private Integer riverSectionCount;

    private String geomType;

    private String geomWkt;

    private Double distanceM;
}
