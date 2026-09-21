package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 河段详情返回
 */
@Schema(description = "仪征管理后台 - 河段详情返回")
@Data
public class RiverSectionDetailRespVO {

    @Schema(description = "河段ID")
    private Long id;

    @Schema(description = "河道ID")
    private Long riverChannelId;

    @Schema(description = "基础表ID")
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

    @Schema(description = "备注")
    private String remarks;
}
