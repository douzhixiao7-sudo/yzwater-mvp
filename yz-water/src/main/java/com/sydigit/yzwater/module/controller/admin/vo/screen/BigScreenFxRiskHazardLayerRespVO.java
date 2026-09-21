package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 大屏统计 - 风险隐患点 GIS 图层
 */
@Data
public class BigScreenFxRiskHazardLayerRespVO {

    public static final String LAYER_CODE = "fx_risk_hazard";
    public static final String LAYER_NAME = "风险隐患点";

    @Schema(description = "图层编码（固定 fx_risk_hazard）")
    private String layerCode;

    @Schema(description = "图层名称（固定「风险隐患点」）")
    private String layerName;

    @Schema(description = "隐患点任务数量")
    private Integer itemCount;

    @Schema(description = "隐患点列表（含业务字段、隐患点坐标与完整物资调运路线）")
    private List<BigScreenFxRiskHazardItemRespVO> items;
}
