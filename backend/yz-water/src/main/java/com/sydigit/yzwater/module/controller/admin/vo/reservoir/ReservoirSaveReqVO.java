package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 水库新增/编辑请求
 */
@Schema(description = "仪征管理后台 - 水库新增/编辑请求")
@Data
public class ReservoirSaveReqVO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "水库关联的设施基础表ID（返回值字段）")
    private Long facilityId;

    @Schema(description = "GIS几何数据（GeoJSON，仅geometry，不包含Feature）")
    private String geometryGeoJson;

    @Schema(description = "GIS几何类型（POINT/LINESTRING/POLYGON等，返回值字段）")
    private String geomType;

    @Schema(description = "GIS坐标系SRID（为空默认4490）")
    private Integer srid;

    @Schema(description = "水库编码")
    private String reservoirCode;

    @Schema(description = "水库名称")
    @NotBlank(message = "水库名称不能为空")
    private String reservoirName;

    @Schema(description = "规模（字典表：zd_skgm）")
    @NotBlank(message = "规模不能为空")
    private String reservoirScale;

    @Schema(description = "所在乡镇（行政区划编码）")
    private String[] township;

    @Schema(description = "所在乡镇名称（用于同步写入基础表 adminRegion）")
    private String townshipName;

    @Schema(description = "所在地点（可到村/组/坐标等）")
    private String location;

    @Schema(description = "管理单位（字典表：zd_gldw）")
    private List<String> managementUnit;

    @Schema(description = "水库照片")
    private List<String> reservoirPhotos;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "主管部门")
    private String supervisingDepartment;

    @Schema(description = "水库性质（字典表：zd_skxz）")
    private String reservoirNature;

    @Schema(description = "灌溉面积（亩）")
    private BigDecimal irrigationArea;

    @Schema(description = "设计灌溉面积（亩）")
    private BigDecimal designIrrigationArea;

    @Schema(description = "实际灌溉面积（亩）")
    private String actualIrrigationArea;

    @Schema(description = "保护面积（亩）")
    private BigDecimal protectionArea;

    @Schema(description = "下游主要设施")
    private String downstreamFacilities;

    @Schema(description = "供水对象（城镇/农村/工业等）")
    private String waterSupplyTarget;

    @Schema(description = "集水面积（平方公里）")
    private BigDecimal catchmentArea;

    @Schema(description = "高程基准面（黄海/国家85等）")
    private String elevationDatum;

    @Schema(description = "设计（复核）抗震烈度")
    private String seismicIntensity;

    @Schema(description = "竣工日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate completionDate;

    @Schema(description = "除险加固开工年月")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate reinforcementStartDate;

    @Schema(description = "除险加固竣工年月")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate reinforcementEndDate;

    @Schema(description = "除险加固日期（兼容历史字段）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime reinforcementDate;

    @Schema(description = "设计洪水标准（文字/等级）")
    private String designFloodStandard;

    @Schema(description = "校核洪水标准（文字/等级）")
    private String verifiedFloodStandard;

    @Schema(description = "重现期设计（年）")
    private Integer designReturnPeriod;

    @Schema(description = "重现期校核（年）")
    private Integer checkReturnPeriod;

    @Schema(description = "总库容（m3）")
    private BigDecimal totalCapacity;

    @Schema(description = "兴利库容（m3）")
    private BigDecimal activeCapacity;

    @Schema(description = "调洪库容（m3）")
    private BigDecimal floodControlCapacity;

    @Schema(description = "死库容（m3）")
    private BigDecimal deadCapacity;

    @Schema(description = "校核水位/校核洪水位（m）")
    private BigDecimal verifiedFloodLevel;

    @Schema(description = "设计水位/设计洪水位（m）")
    private BigDecimal designFloodLevel;

    @Schema(description = "兴利水位（m）")
    private BigDecimal normalOperatingLevel;

    @Schema(description = "汛限水位（m）")
    private BigDecimal floodLimitLevel;

    @Schema(description = "死水位（m）")
    private BigDecimal deadLevel;

    @Schema(description = "坝顶高程（m）")
    private String damCrestElevation;

    @Schema(description = "坝顶宽度（m）")
    private String damTopWidth;

    @Schema(description = "坝顶高度/坝顶相对高（m）")
    private BigDecimal damTopHeight;

    @Schema(description = "最大坝高（m）")
    private BigDecimal maxDamHeight;

    @Schema(description = "坝顶长度（m）")
    private String damTopLength;

    @Schema(description = "挡浪墙顶高程（m）")
    private BigDecimal waveWallCrestElevation;

    @Schema(description = "坝顶路面结构型式")
    private String damRoadSurfaceType;

    @Schema(description = "防渗处理结构型式")
    private String seepageControlType;

    @Schema(description = "防渗处理起止桩号")
    private String seepagePileRange;

    @Schema(description = "防渗处理起止高程")
    private String seepageElevRange;

    @Schema(description = "迎水坡型式")
    private String upstreamSlopeType;

    @Schema(description = "迎水坡起止高程")
    private String upstreamSlopeElevation;

    @Schema(description = "迎水坡坡比")
    private String upstreamSlopeRatio;

    @Schema(description = "背水坡坡比")
    private String downstreamSlopeRatio;

    @Schema(description = "护坡结构型式")
    private String slopeProtectionType;

    @Schema(description = "护坡起止高程")
    private String slopeProtectionElevRange;

    @Schema(description = "背水坡护坝地高程（m）")
    private BigDecimal downstreamSlopeElevation;

    @Schema(description = "背水坡护坝地宽（m）")
    private BigDecimal downstreamSlopeWidth;

    @Schema(description = "溢洪道型式")
    private String spillwayType;

    @Schema(description = "溢洪道控制方式（有闸/开敞式）")
    private String spillwayControlType;

    @Schema(description = "溢洪道有无交通桥")
    private Boolean spillwayHasBridge;

    @Schema(description = "溢洪道堰顶高程（m）")
    private BigDecimal spillwayCrestElevation;

    @Schema(description = "溢洪道底高程（m）")
    private BigDecimal spillwayBottomElevation;

    @Schema(description = "溢洪道底宽（孔*宽）（m）")
    private String spillwayBottomWidth;

    @Schema(description = "溢洪道最大流量（m3/s）")
    private BigDecimal spillwayMaxDischarge;

    @Schema(description = "排洪河道名称")
    private String floodChannelName;

    @Schema(description = "排洪河道安全泄量（m3/s）")
    private String floodChannelSafeDischarge;

    @Schema(description = "灌溉/放水涵洞结构型式")
    private String culvertType;

    @Schema(description = "灌溉涵洞断面尺寸（宽*高）（m）")
    private String culvertSectionSize;

    @Schema(description = "灌溉涵洞闸门型式")
    private String culvertGateType;

    @Schema(description = "灌溉涵洞设计流量（m3/s）")
    private BigDecimal culvertDesignDischarge;

    @Schema(description = "涵洞出口底高程（m）")
    private BigDecimal culvertExitElevation;

    @Schema(description = "涵洞直径（m）")
    private BigDecimal culvertDiameter;

    @Schema(description = "涵洞高度（m）")
    private BigDecimal culvertHeight;

    @Schema(description = "年供水量（万m3）")
    private BigDecimal annualWaterSupply;

    @Schema(description = "宜鱼面积（亩）")
    private BigDecimal fisheryArea;

    @Schema(description = "是否水源地（饮用水源地）")
    private Boolean waterSource;

    @Schema(description = "河长职责")
    private String responsibilities;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "跳转链接")
    private String jumpUrl;
}
