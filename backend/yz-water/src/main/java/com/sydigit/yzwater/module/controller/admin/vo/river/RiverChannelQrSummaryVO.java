package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 河道基础信息（二维码数据）
 */
@Schema(description = "仪征管理后台 - 河道基础信息（二维码数据）")
@Data
public class RiverChannelQrSummaryVO {

    @Schema(description = "河道ID")
    private Long id;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河道长度(km)")
    private BigDecimal lengthKm;

    @Schema(description = "河道起点")
    private String startPoint;

    @Schema(description = "河道终点")
    private String endPoint;

    @Schema(description = "流域面积(平方公里)")
    private BigDecimal catchmentKm2;

    @Schema(description = "所在流域(字典值)")
    private String basinType;

    @Schema(description = "所在流域(字典标签)")
    private String basinTypeLabel;

    @Schema(description = "河道类型(多选字典值)")
    private String[] riverType;

    @Schema(description = "河道类型(字典标签，多个用“、”分隔)")
    private String riverTypeLabel;

    @Schema(description = "生态河道类型(字典值)")
    private String ecologyType;

    @Schema(description = "生态河道类型(字典标签)")
    private String ecologyTypeLabel;

    @Schema(description = "河道级别(字典值)")
    private String riverLevel;

    @Schema(description = "河道级别(字典标签)")
    private String riverLevelLabel;

    @Schema(description = "河道概况")
    private String remarks;

    @Schema(description = "流经地区")
    private String flowAreas;

    @Schema(description = "历史最高水位(m)")
    private BigDecimal historicalMaxWaterLevel;

    @Schema(description = "历史最低水位(m)")
    private BigDecimal historicalMinWaterLevel;

    @Schema(description = "河道基础设施ID")
    private Long facilityId;

    @Schema(description = "河道几何类型")
    private String geomType;

    @Schema(description = "河道几何的 SRID")
    private Integer srid;

    @Schema(description = "河道几何 WKT，附带 SRID 前缀")
    private String geomWkt;
}
