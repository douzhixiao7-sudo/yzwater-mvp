package com.sydigit.yzwater.module.util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

/**
 * 几何工具：统一输出带 SRID 的 WKT，便于前端做地图展示。
 */
public final class GeometryWktUtils {

    private static final WKTWriter WKT_WRITER = new WKTWriter();

    private GeometryWktUtils() {
    }

    /**
     * Geometry 转换为带 SRID 的 WKT 字符串。
     * <p>
     * 示例：SRID=4490;POINT(119.1 32.3)
     *
     * @param geometry 几何对象
     * @return 带 SRID 前缀的 WKT；geometry 为空时返回 null
     */
    public static String toWktWithSrid(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        String wkt = WKT_WRITER.write(geometry);
        int srid = geometry.getSRID();
        if (srid > 0) {
            return "SRID=" + srid + ";" + wkt;
        }
        return wkt;
    }
}

