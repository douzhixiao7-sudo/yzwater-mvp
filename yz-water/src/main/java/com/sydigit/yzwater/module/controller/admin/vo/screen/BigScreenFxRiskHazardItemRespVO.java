package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 风险隐患点（单条任务，含业务信息与 GIS）
 */
@Data
public class BigScreenFxRiskHazardItemRespVO {

    @Schema(description = "任务 ID")
    private String taskId;

    @Schema(description = "任务代码")
    private String code;

    @Schema(description = "工程名称")
    private String name;

    @Schema(description = "所属河道 ID")
    private String riverChannelId;

    @Schema(description = "所属河道名称")
    private String riverChannelName;

    @Schema(description = "险工位置")
    private String addr;

    @Schema(description = "险情描述")
    private String content;

    @Schema(description = "应对措施")
    private String counterMeasures;

    @Schema(description = "防汛等级字典值（zd_fxdj）")
    private String level;

    @Schema(description = "防汛等级展示名")
    private String levelLabel;

    @Schema(description = "隐患点经度（EPSG:4490）")
    private BigDecimal hazardLongitude;

    @Schema(description = "隐患点纬度（EPSG:4490）")
    private BigDecimal hazardLatitude;

    @Schema(description = "隐患点标注名称")
    private String hazardPointName;

    @Schema(description = "抢险物资调运路线（结构化，便于大屏按线渲染）")
    private List<BigScreenFxRiskHazardMaterialRouteItemRespVO> materialRoutes;

    @Schema(description = "完整路线 GeoJSON（Point / LineString / MultiLineString，含 vertexMarkers、vertexLabels 等扩展字段）")
    private String geometryGeoJson;
}
