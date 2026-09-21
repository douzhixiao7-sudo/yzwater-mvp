package com.sydigit.yzwater.module.controller.admin.vo.pump;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 泵站分页查询请求
 */
@Schema(description = "仪征管理后台 - 泵站分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class PumpStationPageReqVO extends PageParam {

    @Schema(description = "泵站名称（支持模糊匹配）")
    private String pumpStationName;

    @Schema(description = "泵站类型（字典：zd_bzlx）")
    private String pumpStationType;

    @Schema(description = "区划代码（来源：/system/area/tree 的 id）")
    private String divisionCode;
}

