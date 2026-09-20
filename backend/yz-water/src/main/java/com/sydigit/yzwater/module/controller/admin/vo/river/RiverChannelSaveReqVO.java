package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 河道保存请求
 */
@Schema(description = "仪征管理后台 - 河道保存请求")
@Data
public class RiverChannelSaveReqVO {

    @Schema(description = "主键ID，新增为空")
    private Long id;

    @Schema(description = "关联基础表 water_facility_base.id")
    private Long facilityId;

    @Schema(description = "河道编码")
    private String riverCode;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河道长度(km)")
    private BigDecimal lengthKm;

    @Schema(description = "流域面积(平方公里)")
    private BigDecimal catchmentKm2;

    @Schema(description = "河道平均比降")
    private BigDecimal averageSlope;

    @Schema(description = "所在流域(字典: zd_szly)")
    private String basinType;

    @Schema(description = "生态类型(字典: zd_stlx)")
    private String ecologyType;

    @Schema(description = "跨界类别(字典: zd_kjlb)")
    private String transboundaryType;

    @Schema(description = "防洪标准(字典: zd_fhbz)")
    private String floodStandard;

    @Schema(description = "堤防等级(字典: zd_dfdj)")
    private String embankmentLevel;

    @Schema(description = "堤防长度(km)")
    private BigDecimal embankmentLength;

    @Schema(description = "中心点经度")
    private BigDecimal centroidLongitude;

    @Schema(description = "中心点纬度")
    private BigDecimal centroidLatitude;

    @Schema(description = "河口经度")
    private BigDecimal riverEndLongitude;

    @Schema(description = "河口纬度")
    private BigDecimal riverEndLatitude;

    @Schema(description = "河源经度")
    private BigDecimal riverSourceLongitude;

    @Schema(description = "河源纬度")
    private BigDecimal riverSourceLatitude;

    @Schema(description = "流经地区")
    private String[] flowAreas;

    @Schema(description = "历史最高水位(m)")
    private BigDecimal historicalMaxWaterLevel;

    @Schema(description = "最高水位时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime maxWaterLevelDate;

    @Schema(description = "最低水位时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lowestWaterLevelDate;

    @Schema(description = "历史最低水位(m)")
    private BigDecimal historicalMinWaterLevel;

    @Schema(description = "年均径流量")
    private BigDecimal averageAnnualRunoff;

    @Schema(description = "发源山系")
    private String sourceMountainRange;

    @Schema(description = "河流归宿")
    private String riverTerminus;

    @Schema(description = "河流级别(字典: zd_hljb)")
    private String riverLevel;

    @Schema(description = "是否省级骨干河道(0-否 1-是)")
    private Integer isProvincialBackbone;

    @Schema(description = "河口位置")
    private String riverEntrance;

    @Schema(description = "河源位置")
    private String riverOrigin;

    @Schema(description = "起点")
    private String startPoint;

    @Schema(description = "终点")
    private String endPoint;

    @Schema(description = "河道类型(多选字典: zd_hdlx)")
    private String[] riverType;

    @Schema(description = "河道照片")
    private String[] riverPhotos;

    @Schema(description = "河道水质情况(字典: zd_hdszqk)")
    private String waterQualityStatus;

    @Schema(description = "关联设施")
    private String associatedFacilities;

    @Schema(description = "河段划分数量")
    private Integer riverSectionCount;

    @Schema(description = "所属乡镇")
    private String[] town;

    @Schema(description = "管理单位")
    private String managementUnit;

    @Schema(description = "河长职责")
    private String responsibilities;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "河段列表")
    private List<RiverSectionSaveReqVO> sections;
}
