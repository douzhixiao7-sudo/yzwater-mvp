package com.sydigit.yzwater.module.dal.mybatis.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import net.postgis.jdbc.PGgeometry;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostGIS 几何字段与 JTS Geometry 的双向转换器
 *
 * <p>该处理器保障 MyBatis 在读取/写入 geometry 类型时不会出现二进制流与 PGgeometry
 * 类型无法解析的情况，方便在 Java 层直接使用 GeoTools 所依赖的 JTS Geometry。</p>
 *
 * @author Lijun
 */
@MappedJdbcTypes(JdbcType.OTHER)
@MappedTypes(Geometry.class)
public class GeometryTypeHandler extends BaseTypeHandler<Geometry> {

    private static final int DEFAULT_SRID = 4326;
    private static final WKTWriter WKT_WRITER = new WKTWriter();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Geometry parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject geometryObject = new PGobject();
        geometryObject.setType("geometry");
        geometryObject.setValue(buildGeometryValue(parameter));
        ps.setObject(i, geometryObject);
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseGeometry(rs.getObject(columnName));
    }

    @Override
    public Geometry getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseGeometry(rs.getObject(columnIndex));
    }

    @Override
    public Geometry getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseGeometry(cs.getObject(columnIndex));
    }

    /**
     * 将 PostGIS 传回的值解析为 Geometry。
     */
    private Geometry parseGeometry(Object dbValue) throws SQLException {
        if (dbValue == null) {
            return null;
        }
        String raw = (dbValue instanceof PGgeometry pggeometry)
                ? pggeometry.getValue()
                : dbValue.toString();
        int srid = DEFAULT_SRID;
        if (raw.startsWith("SRID=")) {
            int splitIndex = raw.indexOf(';');
            if (splitIndex > 5) {
                srid = Integer.parseInt(raw.substring(5, splitIndex));
                raw = raw.substring(splitIndex + 1);
            }
        }
        try {
            Geometry geometry = new WKTReader().read(raw);
            geometry.setSRID(srid);
            return geometry;
        } catch (ParseException ex) {
            throw new SQLException("无法解析 PostGIS 几何字段：" + raw, ex);
        }
    }

    /**
     * 将 Geometry 写入数据库时附带 SRID 信息。
     */
    private String buildGeometryValue(Geometry geometry) {
        int srid = geometry.getSRID() > 0 ? geometry.getSRID() : DEFAULT_SRID;
        return "SRID=" + srid + ";" + WKT_WRITER.write(geometry);
    }
}
