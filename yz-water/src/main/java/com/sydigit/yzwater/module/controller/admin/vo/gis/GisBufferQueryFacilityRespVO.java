package com.sydigit.yzwater.module.controller.admin.vo.gis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "仪征管理后台 - 缓冲区设施快照")
@Data
public class GisBufferQueryFacilityRespVO {

    @Schema(description = "设施基础表ID")
    private Long facilityId;

    @Schema(description = "设施类型(zd_sslb)")
    private String facilityType;

    @Schema(description = "设施名称")
    private String facilityName;

    @Schema(description = "行政区划编码")
    private String adminRegionCode;

    @Schema(description = "几何类型")
    private String geomType;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
