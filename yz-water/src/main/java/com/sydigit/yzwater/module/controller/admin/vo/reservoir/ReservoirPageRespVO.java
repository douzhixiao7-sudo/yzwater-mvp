package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 水库分页响应
 */
@Schema(description = "仪征管理后台 - 水库分页响应")
@Data
public class ReservoirPageRespVO {

    @Schema(description = "主键 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "水库编码")
    private String reservoirCode;

    @Schema(description = "水库名称")
    private String reservoirName;

    @Schema(description = "规模（字典：zd_skgm）")
    private String reservoirScale;

    @Schema(description = "管理单位（字典：zd_gldw）")
    private String[] managementUnit;

    @Schema(description = "所在乡镇（行政区划编码）")
    private String[] township;

    @Schema(description = "所在乡镇名称（中文）")
    private String townshipName;

    @Schema(description = "水库性质（字典：zd_skxz）")
    private String reservoirNature;

    @Schema(description = "集水面积（平方公里）")
    private BigDecimal catchmentArea;

    @Schema(description = "总库容（m³）")
    private BigDecimal totalCapacity;

    @Schema(description = "兴利库容（m³）")
    private BigDecimal activeCapacity;

    @Schema(description = "兴利水位（m）")
    private BigDecimal normalOperatingLevel;

    @Schema(description = "汛限水位（m）")
    private BigDecimal floodLimitLevel;

    @Schema(description = "设计水位（m）")
    private BigDecimal designFloodLevel;

    @Schema(description = "校核水位（m）")
    private BigDecimal verifiedFloodLevel;

    @Schema(description = "坝顶高程（m）")
    private String damCrestElevation;

    @Schema(description = "最大坝高（m）")
    private BigDecimal maxDamHeight;

    @Schema(description = "坝顶长度（m）")
    private String damTopLength;

    @Schema(description = "死水位（m）")
    private BigDecimal deadLevel;

    @Schema(description = "跳转链接")
    private String jumpUrl;
}
