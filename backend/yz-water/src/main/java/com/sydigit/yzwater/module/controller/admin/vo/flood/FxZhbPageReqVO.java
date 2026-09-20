package com.sydigit.yzwater.module.controller.admin.vo.flood;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 防汛指挥部分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 防汛指挥部分页查询 Request VO")
public class FxZhbPageReqVO extends PageParam {

    @Schema(description = "指挥部名称（模糊查询）")
    private String name;

    @Schema(description = "区划代码")
    private String areaCode;
}
