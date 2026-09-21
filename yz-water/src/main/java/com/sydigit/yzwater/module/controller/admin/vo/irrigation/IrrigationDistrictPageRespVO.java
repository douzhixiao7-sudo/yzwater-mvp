package com.sydigit.yzwater.module.controller.admin.vo.irrigation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 灌区分页返回
 */
@Data
@Schema(description = "管理后台 - 灌区分页返回 Response VO")
public class IrrigationDistrictPageRespVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "关联基础表ID")
    private Long facilityId;

    @Schema(description = "灌区编码")
    private String irrigationDistrictCode;

    @Schema(description = "灌区名称")
    private String irrigationDistrictName;

    @Schema(description = "所在流域")
    private String basinCode;

    @Schema(description = "行政区划（text[]）")
    private List<String> divisionCode;

    @Schema(description = "设计灌溉面积（万亩）")
    private BigDecimal designIrrigationArea;

    @Schema(description = "实际可灌面积")
    private BigDecimal actualIrrigableArea;

    @Schema(description = "实际基本农田面积(k㎡)")
    private BigDecimal basicFarmlandAreaKm2;

    @Schema(description = "是否生态红线(0 否 1 是)")
    private Integer isEcologicalRedLine;

    @Schema(description = "是否开发边界(0 否 1 是)")
    private Integer isDevelopmentBoundary;

    @Schema(description = "干渠长度(单位 m)")
    private BigDecimal mainCanalLengthM;

    @Schema(description = "负责人")
    private String leaderName;

    @Schema(description = "联系电话")
    private String leaderPhone;

    @Schema(description = "管理单位（text[]，字典：zd_gldw）")
    private List<String> managementUnit;

    @Schema(description = "灌区类型（字典：zd_gqlx）")
    private String irrigationDistrictType;
}
