package com.sydigit.yzwater.module.controller.admin.vo.gis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "仪征管理后台 - 缓冲区查询详情返回")
@Data
public class GisBufferQueryRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "中心点经度")
    private BigDecimal centerLongitude;

    @Schema(description = "中心点纬度")
    private BigDecimal centerLatitude;

    @Schema(description = "缓冲区半径(米)")
    private BigDecimal radiusMeters;

    @Schema(description = "缓冲区面积(平方米)")
    private BigDecimal bufferAreaM2;

    @Schema(description = "缓冲区几何GeoJSON")
    private String bufferGeoJson;

    @Schema(description = "设施类型列表(zd_sslb)")
    private List<String> facilityTypes;

    @Schema(description = "设施数量")
    private Integer facilityCount;

    @Schema(description = "分类型统计")
    private Map<String, Integer> facilityTypeCount;

    @Schema(description = "涉及行政区列表")
    private List<GisBufferQueryAreaRespVO> adminAreas;

    @Schema(description = "统计时间")
    private LocalDateTime statsTime;

    @Schema(description = "设施列表")
    private List<GisBufferQueryFacilityRespVO> facilities;
}
