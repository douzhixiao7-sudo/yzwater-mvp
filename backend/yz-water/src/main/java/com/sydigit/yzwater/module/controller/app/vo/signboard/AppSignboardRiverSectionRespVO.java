package com.sydigit.yzwater.module.controller.app.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 手机端 - 公示牌扫码直接关联河段信息
 */
@Schema(description = "手机端 - 公示牌扫码直接关联河段信息")
@Data
public class AppSignboardRiverSectionRespVO {

    @Schema(description = "河段ID")
    private Long id;

    @Schema(description = "关联河道ID")
    private Long riverChannelId;

    @Schema(description = "关联基础设施ID（yz_water_facility_base.id）")
    private Long facilityId;

    @Schema(description = "河段名称")
    private String sectionName;

    @Schema(description = "起点")
    private String startPoint;

    @Schema(description = "终点")
    private String endPoint;

    @Schema(description = "起点经度")
    private BigDecimal startLongitude;

    @Schema(description = "起点纬度")
    private BigDecimal startLatitude;

    @Schema(description = "终点经度")
    private BigDecimal endLongitude;

    @Schema(description = "终点纬度")
    private BigDecimal endLatitude;

    @Schema(description = "几何类型（POINT/LINESTRING/POLYGON 等）")
    private String geomType;

    @Schema(description = "几何SRID")
    private Integer srid;

    @Schema(description = "几何WKT（附带SRID前缀，示例：SRID=4490;LINESTRING(...)）")
    private String geomWkt;
}

