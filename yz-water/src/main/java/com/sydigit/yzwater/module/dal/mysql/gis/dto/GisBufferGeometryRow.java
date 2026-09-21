package com.sydigit.yzwater.module.dal.mysql.gis.dto;

import lombok.Data;
import org.locationtech.jts.geom.Geometry;

import java.math.BigDecimal;

@Data
public class GisBufferGeometryRow {

    private Geometry bufferGeom;

    private BigDecimal areaM2;
}
