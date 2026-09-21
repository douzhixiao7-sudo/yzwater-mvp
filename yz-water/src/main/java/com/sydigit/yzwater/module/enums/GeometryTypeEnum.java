package com.sydigit.yzwater.module.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * 空间要素类型枚举
 *
 * @author Lijun
 */
@Getter
@AllArgsConstructor
public enum GeometryTypeEnum {

    POINT("POINT", "点要素"),
    LINESTRING("LINESTRING", "线要素"),
    POLYGON("POLYGON", "面要素");

    private final String dbType;
    private final String label;

    public static GeometryTypeEnum fromGeometry(Geometry geometry) {
        if (geometry instanceof Point || geometry instanceof MultiPoint) {
            return POINT;
        }
        if (geometry instanceof LineString || geometry instanceof MultiLineString) {
            return LINESTRING;
        }
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return POLYGON;
        }
        throw new IllegalArgumentException("暂不支持的几何类型：" + geometry.getGeometryType());
    }

    public static GeometryTypeEnum fromDbType(String dbType) {
        if (dbType == null) {
            throw new IllegalArgumentException("几何类型不能为空");
        }
        for (GeometryTypeEnum value : values()) {
            if (value.getDbType().equalsIgnoreCase(dbType)) {
                return value;
            }
        }
        throw new IllegalArgumentException("未知的几何类型：" + dbType);
    }
}
