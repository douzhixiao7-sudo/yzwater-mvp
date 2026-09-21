package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 河道分页响应
 */
@Schema(description = "仪征管理后台 - 河道分页响应")
@Data
public class RiverChannelPageRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "河道编码")
    private String riverCode;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河道级别名称")
    private String riverLevelLabel;

    @Schema(description = "河道类型名称，逗号分隔")
    private String riverTypeLabel;

    @Schema(description = "所在流域名称")
    private String basinTypeLabel;

    @Schema(description = "起点至终点")
    private String startEndLocation;

    @Schema(description = "河道长度(km)")
    private BigDecimal lengthKm;

    @Schema(description = "流域面积(平方公里)")
    private BigDecimal catchmentKm2;

    @Schema(description = "管理单位")
    private String managementUnit;

    @Schema(description = "是否省级骨干河道(0-否 1-是)")
    private Integer isProvincialBackbone;
}
