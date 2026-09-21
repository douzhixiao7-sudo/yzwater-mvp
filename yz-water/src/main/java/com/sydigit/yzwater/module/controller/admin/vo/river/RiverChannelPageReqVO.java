package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 河道分页查询请求
 */
@Schema(description = "仪征管理后台 - 河道分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class RiverChannelPageReqVO extends PageParam {

    @Schema(description = "河道编码，支持模糊")
    private String riverCode;

    @Schema(description = "河道名称，支持模糊")
    private String riverName;

    @Schema(description = "河道级别(字典: zd_hljb，可多选)")
    private List<String> riverLevel;

    @Schema(description = "生态类型(字典: zd_stlx)")
    private String ecologyType;

    @Schema(description = "是否省级骨干河道(0-否 1-是)")
    private Integer isProvincialBackbone;
}
