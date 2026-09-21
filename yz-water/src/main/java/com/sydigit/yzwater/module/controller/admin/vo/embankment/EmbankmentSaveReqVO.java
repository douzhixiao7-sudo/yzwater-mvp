package com.sydigit.yzwater.module.controller.admin.vo.embankment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 堤防新增/编辑请求
 */
@Schema(description = "仪征管理后台 - 堤防新增/编辑请求")
@Data
public class EmbankmentSaveReqVO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "堤防名称")
    @NotBlank(message = "堤防名称不能为空")
    private String embankmentName;

    @Schema(description = "堤防代码，新增时可不传，由后台自动生成")
    private String embankmentCode;

    @Schema(description = "区划代码")
    private List<String> divisionCode;

    @Schema(description = "所在河道ID（可为空；选择河段时会自动校验所属河道）")
    private Long riverChannelId;

    @Schema(description = "所在河段ID（可为空）")
    private Long riverSectionId;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "河流岸别(字典: zd_hlab)")
    private String riverBankSide;

    @Schema(description = "堤防跨界情况")
    private String crossBoundaryStatus;

    @Schema(description = "堤防类型(字典: zd_dflx)")
    private String embankmentType;

    @Schema(description = "堤防形式(字典: zd_dfxs)")
    private String embankmentForm;

    @Schema(description = "堤防级别(字典: zd_dfjb)")
    private String embankmentLevel;

    @Schema(description = "防洪标准")
    private String floodStandard;

    @Schema(description = "设计重现期(年)")
    private Integer designReturnPeriod;

    @Schema(description = "堤防长度(m)")
    private BigDecimal lengthM;

    @Schema(description = "标准长度(m)")
    private BigDecimal standardLengthM;

    @Schema(description = "高程系统")
    private String elevationSystem;

    @Schema(description = "设计高潮位(m)")
    private String designHighTide;

    @Schema(description = "堤防最大高度(m)")
    private BigDecimal maxHeight;

    @Schema(description = "堤防最小高度(m)")
    private BigDecimal minHeight;

    @Schema(description = "堤防最大宽度(m)")
    private BigDecimal maxWidth;

    @Schema(description = "堤防最小宽度(m)")
    private BigDecimal minWidth;

    @Schema(description = "堤顶高程(m)")
    private BigDecimal crestElevation;

    @Schema(description = "起点")
    private String startPoint;

    @Schema(description = "终点")
    private String endPoint;

    @Deprecated
    @Schema(description = "堤顶起点高程(m)")
    private BigDecimal crestStartElevation;

    @Deprecated
    @Schema(description = "堤顶终点高程(m)")
    private BigDecimal crestEndElevation;

    @Schema(description = "工程任务")
    private String projectTask;

    @Schema(description = "终点所在位置")
    private String endLocation;

    @Schema(description = "工程建设情况")
    private String constructionStatus;

    @Schema(description = "归口管理部门")
    private String managementDepartment;

    @Schema(description = "堤防图片URL列表（最多 5 张）")
    private List<String> embankmentImages;

    @Schema(description = "备注")
    private String remarks;
}
