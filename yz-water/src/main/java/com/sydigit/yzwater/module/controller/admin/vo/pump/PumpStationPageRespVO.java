package com.sydigit.yzwater.module.controller.admin.vo.pump;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 泵站分页响应
 */
@Schema(description = "仪征管理后台 - 泵站分页响应")
@Data
public class PumpStationPageRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "泵站名称")
    private String pumpStationName;

    @Schema(description = "泵站代码")
    private String pumpStationCode;

    @Schema(description = "泵站类型标签")
    private String pumpStationTypeLabel;

    @Schema(description = "行政区划代码列表")
    private List<String> divisionCode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "装机功率(KW)")
    private BigDecimal installedCapacityKw;

    @Schema(description = "装机流量(m3/s)")
    private BigDecimal capacityFlow;

    @Schema(description = "机组数量")
    private Integer unitCount;

    @Schema(description = "自排流量(m3/s)")
    private BigDecimal selfFlow;

    @Schema(description = "抽引流量(m3/s)")
    private BigDecimal installedFlow;

    @Schema(description = "抽排流量(m3/s)")
    private BigDecimal pumpingFlow;

    @Schema(description = "常水位(m)")
    private BigDecimal normalWaterLevel;

    @Schema(description = "防办预降水位(m)")
    private BigDecimal preDropWaterLevel;

    @Schema(description = "最低运行水位(m)")
    private BigDecimal minimumOperatingWaterLevel;

    @Schema(description = "单机组功率(KW)")
    private BigDecimal singleUnitPower;

    @Schema(description = "备注（泵站概览）")
    private String pumpStationOverview;
}
