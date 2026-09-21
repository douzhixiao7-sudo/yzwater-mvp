package com.sydigit.yzwater.module.service.pond;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 坑塘几何辅助：中心点计算等
 */
public final class WaterPondGeometryHelper {

    private static final int COORD_SCALE = 8;

    private WaterPondGeometryHelper() {
    }

    public static BigDecimal computeCenterLon(Geometry geometry) {
        Point center = resolveCentroid(geometry);
        return center == null ? null : scaleCoord(center.getX());
    }

    public static BigDecimal computeCenterLat(Geometry geometry) {
        Point center = resolveCentroid(geometry);
        return center == null ? null : scaleCoord(center.getY());
    }

    public static void applyCenterToPond(com.sydigit.yzwater.module.dal.dataobject.pond.YzWaterPondDO pond,
                                         Geometry geometry) {
        if (pond == null) {
            return;
        }
        if (geometry == null || geometry.isEmpty()) {
            pond.setCenterLon(null);
            pond.setCenterLat(null);
            return;
        }
        pond.setCenterLon(computeCenterLon(geometry));
        pond.setCenterLat(computeCenterLat(geometry));
    }

    private static Point resolveCentroid(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return null;
        }
        Geometry centroid = geometry.getCentroid();
        if (centroid instanceof Point point) {
            return point;
        }
        return geometry.getCentroid();
    }

    private static BigDecimal scaleCoord(double value) {
        return BigDecimal.valueOf(value).setScale(COORD_SCALE, RoundingMode.HALF_UP);
    }
}
