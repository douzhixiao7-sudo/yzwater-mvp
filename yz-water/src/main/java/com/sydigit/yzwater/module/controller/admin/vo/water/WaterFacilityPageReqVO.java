package com.sydigit.yzwater.module.controller.admin.vo.water;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 水利设施分页查询请求
 */
@Schema(description = "仪征管理后台 - 水利设施分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class WaterFacilityPageReqVO extends PageParam {

    @Schema(description = "设施名称（模糊匹配）")
    private String facilityName;

    @Schema(description = "设施类别")
    private String facilityType;

    @Schema(description = "生态类型（gis_stlx 字典值，对应 attributes.sthd）")
    private String ecoType;
}
