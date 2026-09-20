package com.sydigit.yzwater.module.util;

/**
 * CGCS2000 三度带高斯克吕格坐标转换工具
 */
public final class GaussKrugerCoordinateUtils {

    private static final double CGCS2000_A = 6378137.0;
    private static final double CGCS2000_INV_F = 298.257222101;

    private GaussKrugerCoordinateUtils() {
    }

    /**
     * 将 CGCS2000 三度带高斯克吕格投影坐标反算为 4490 经纬度。
     *
     * @param easting         东向坐标（X）
     * @param northing        北向坐标（Y）
     * @param centralMeridian 中央经线，单位：度
     * @return [经度, 纬度]
     */
    public static double[] inverseGaussKrugerTo4490(double easting, double northing, double centralMeridian) {
        double f = 1.0 / CGCS2000_INV_F;
        double e2 = 2 * f - f * f;
        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double ep2 = e2 / (1 - e2);
        double e1 = (1 - Math.sqrt(1 - e2)) / (1 + Math.sqrt(1 - e2));
        double k0 = 1.0;
        double falseEasting = 500000.0;

        double x = easting - falseEasting;
        double y = northing;
        double m = y / k0;
        double mu = m / (CGCS2000_A * (1 - e2 / 4 - 3 * e4 / 64 - 5 * e6 / 256));

        double phi1 = mu
                + (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32) * Math.sin(2 * mu)
                + (21 * e1 * e1 / 16 - 55 * Math.pow(e1, 4) / 32) * Math.sin(4 * mu)
                + (151 * Math.pow(e1, 3) / 96) * Math.sin(6 * mu)
                + (1097 * Math.pow(e1, 4) / 512) * Math.sin(8 * mu);

        double sinPhi1 = Math.sin(phi1);
        double cosPhi1 = Math.cos(phi1);
        double tanPhi1 = Math.tan(phi1);

        double c1 = ep2 * cosPhi1 * cosPhi1;
        double t1 = tanPhi1 * tanPhi1;
        double n1 = CGCS2000_A / Math.sqrt(1 - e2 * sinPhi1 * sinPhi1);
        double r1 = CGCS2000_A * (1 - e2) / Math.pow(1 - e2 * sinPhi1 * sinPhi1, 1.5);
        double d = x / (n1 * k0);

        double lat = phi1 - (n1 * tanPhi1 / r1) * (
                d * d / 2
                        - (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * ep2) * Math.pow(d, 4) / 24
                        + (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * ep2 - 3 * c1 * c1)
                        * Math.pow(d, 6) / 720
        );
        double lon = Math.toRadians(centralMeridian) + (
                d
                        - (1 + 2 * t1 + c1) * Math.pow(d, 3) / 6
                        + (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * ep2 + 24 * t1 * t1)
                        * Math.pow(d, 5) / 120
        ) / cosPhi1;

        return new double[]{Math.toDegrees(lon), Math.toDegrees(lat)};
    }
}
