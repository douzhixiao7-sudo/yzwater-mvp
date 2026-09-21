package com.sydigit.yzwater.module.controller.admin.vo.embankment;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 堤防分页查询请求
 */
@Schema(description = "仪征管理后台 - 堤防分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class EmbankmentPageReqVO extends PageParam {

    @Schema(description = "堤防代码，支持模糊匹配")
    private String embankmentCode;

    @Schema(description = "堤防名称，支持模糊匹配")
    private String embankmentName;

    @Schema(description = "堤防级别(字典: zd_dfjb)")
    private String embankmentLevel;

    @Schema(description = "堤防类型(字典: zd_dflx)")
    private String embankmentType;
}

