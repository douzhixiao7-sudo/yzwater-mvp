package com.sydigit.yzwater.module.controller.admin.vo.irrigation;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 灌区分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 灌区分页查询 Request VO")
public class IrrigationDistrictPageReqVO extends PageParam {

    @Schema(description = "关联基础表ID")
    private Long facilityId;

    @Schema(description = "灌区编码（模糊查询）")
    private String irrigationDistrictCode;

    @Schema(description = "灌区名称（模糊查询）")
    private String irrigationDistrictName;

    @Schema(description = "所在流域")
    private String basinCode;

    @Schema(description = "行政区划（单个节点筛选，数组中包含该值即可命中）")
    private String divisionCode;

    @Schema(description = "是否生态红线(0 否 1 是)")
    private Integer isEcologicalRedLine;

    @Schema(description = "是否开发边界(0 否 1 是)")
    private Integer isDevelopmentBoundary;

    @Schema(description = "负责人（模糊查询）")
    private String leaderName;

    @Schema(description = "联系电话（模糊查询）")
    private String leaderPhone;

    @Schema(description = "灌区类型（字典：zd_gqlx）")
    private String irrigationDistrictType;
}
