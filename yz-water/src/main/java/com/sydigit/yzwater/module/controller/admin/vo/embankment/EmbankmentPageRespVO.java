package com.sydigit.yzwater.module.controller.admin.vo.embankment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 堤防分页响应
 */
@Schema(description = "仪征管理后台 - 堤防分页响应")
@Data
public class EmbankmentPageRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "堤防代码")
    private String embankmentCode;

    @Schema(description = "堤防名称")
    private String embankmentName;

    @Schema(description = "河流岸别标签")
    private String riverBankSideLabel;

    @Schema(description = "堤防级别标签")
    private String embankmentLevelLabel;

    @Schema(description = "堤防类型标签")
    private String embankmentTypeLabel;

    @Schema(description = "堤防形式标签")
    private String embankmentFormLabel;

    @Schema(description = "设计高潮位(m)")
    private String designHighTide;

    @Schema(description = "堤防长度(m)")
    private BigDecimal lengthM;

    @Schema(description = "起点")
    private String startPoint;

    @Schema(description = "终点")
    private String endPoint;

    @Schema(description = "归口管理部门")
    private String managementDepartment;
}

