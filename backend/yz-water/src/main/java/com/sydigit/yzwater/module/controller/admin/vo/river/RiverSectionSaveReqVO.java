package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 河段保存请求
 */
@Schema(description = "仪征管理后台 - 河段保存请求")
@Data
public class RiverSectionSaveReqVO {

    @Schema(description = "河段ID，新增为空，编辑必填")
    private Long id;

    @Schema(description = "基础表ID，新增为空，编辑时传回原值")
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
