package com.sydigit.yzwater.module.iot.framework.tdengine.core.typehandler;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * TDengine NCHAR 字段类型处理器，统一把二进制值转换成 UTF-8 字符串
 */
public class TdengineNcharTypeHandler extends BaseTypeHandler<Object> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        String normalized = normalize(parameter);
        try {
            // 优先使用 NCHAR 绑定，避免 WS 驱动把字符串误当成二进制
            ps.setNString(i, normalized);
        } catch (SQLFeatureNotSupportedException | AbstractMethodError ex) {
            ps.setString(i, normalized);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }

    private String normalize(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof byte[]) {
            return StrUtil.removePrefix(new String((byte[]) value, StandardCharsets.UTF_8), "\uFEFF");
        }
        if (value instanceof Byte[]) {
            Byte[] bytes = (Byte[]) value;
            byte[] raw = new byte[bytes.length];
            for (int index = 0; index < bytes.length; index++) {
                raw[index] = bytes[index];
            }
            return StrUtil.removePrefix(new String(raw, StandardCharsets.UTF_8), "\uFEFF");
        }
        if (value instanceof CharSequence) {
            return StrUtil.removePrefix(value.toString(), "\uFEFF");
        }
        return JsonUtils.toJsonString(value);
    }
}
