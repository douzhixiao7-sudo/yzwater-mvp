package com.sydigit.yzwater.module.dal.mybatis.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PostgreSQL bigint[] 数组类型处理器
 */
public class LongArrayTypeHandler extends BaseTypeHandler<Long[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Long[] parameter, JdbcType jdbcType) throws SQLException {
        Array array = ps.getConnection().createArrayOf("int8", parameter);
        ps.setArray(i, array);
    }

    @Override
    public Long[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toLongArray(rs.getArray(columnName));
    }

    @Override
    public Long[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toLongArray(rs.getArray(columnIndex));
    }

    @Override
    public Long[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toLongArray(cs.getArray(columnIndex));
    }

    private Long[] toLongArray(Array array) throws SQLException {
        if (array == null) {
            return null;
        }
        Object value = array.getArray();
        if (value instanceof Long[] longs) {
            return longs;
        }
        Object[] objects = (Object[]) value;
        Long[] result = new Long[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                result[i] = null;
            } else if (objects[i] instanceof Number number) {
                result[i] = number.longValue();
            } else {
                result[i] = Long.parseLong(objects[i].toString());
            }
        }
        return result;
    }
}
