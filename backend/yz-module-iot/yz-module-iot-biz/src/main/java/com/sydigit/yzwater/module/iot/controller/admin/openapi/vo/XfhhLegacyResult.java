package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 幸福河湖开放接口兼容返回对象
 *
 * 保持旧系统对外返回的 code / msg / data 结构。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "幸福河湖开放接口兼容返回对象")
public class XfhhLegacyResult<T> {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "消息", example = "OK")
    private String msg;

    @Schema(description = "返回数据")
    private T data;

    /**
     * 成功返回
     */
    public static <T> XfhhLegacyResult<T> ok() {
        return new XfhhLegacyResult<>(200, "OK", null);
    }

    /**
     * 成功返回
     */
    public static <T> XfhhLegacyResult<T> ok(T data) {
        return new XfhhLegacyResult<>(200, "OK", data);
    }

    /**
     * 失败返回
     */
    public static <T> XfhhLegacyResult<T> error() {
        return new XfhhLegacyResult<>(500, "未知异常，请联系管理员", null);
    }

    /**
     * 失败返回
     */
    public static <T> XfhhLegacyResult<T> error(String msg) {
        return new XfhhLegacyResult<>(500, msg, null);
    }
}
