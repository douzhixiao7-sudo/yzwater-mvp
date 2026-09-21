package com.sydigit.yzwater.module.iot.framework.tdengine.core.typehandler;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * TDengine TIMESTAMP 类型处理器，统一把毫秒时间戳转换为 Timestamp
 */
public class TdengineTimestampTypeHandler extends BaseTypeHandler<Object> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        Timestamp timestamp = toTimestamp(parameter);
        if (timestamp == null) {
            ps.setNull(i, java.sql.Types.TIMESTAMP);
            return;
        }
        ps.setTimestamp(i, timestamp);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getTimestamp(columnName);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getTimestamp(columnIndex);
    }

    private Timestamp toTimestamp(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return (Timestamp) value;
        }
        if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        }
        if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        }
        if (value instanceof Number) {
            return new Timestamp(((Number) value).longValue());
        }
        String text = Convert.toStr(value);
        if (StrUtil.isBlank(text)) {
            return null;
        }
        if (StrUtil.isNumeric(text)) {
            return new Timestamp(Convert.toLong(text));
        }
        LocalDateTime dateTime = LocalDateTimeUtil.parse(text);
        return dateTime == null ? null : Timestamp.valueOf(dateTime);
    }
}
