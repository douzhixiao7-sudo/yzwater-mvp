package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 河段信息（二维码数据）
 */
@Schema(description = "仪征管理后台 - 河段信息（二维码数据）")
@Data
public class RiverSectionQrVO {

    @Schema(description = "河段ID")
    private Long id;

    @Schema(description = "关联基础设施ID")
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

    @Schema(description = "河段几何类型")
    private String geomType;

    @Schema(description = "河段几何 SRID")
    private Integer srid;

    @Schema(description = "河段几何 WKT，附带 SRID 前缀")
    private String geomWkt;

    @Schema(description = "河段对应的河长信息")
    private List<RiverHeadQrVO> heads;
}
