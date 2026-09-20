package com.sydigit.yzwater.module.controller.app.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 手机端 - 公示牌扫码关联水库信息
 */
@Schema(description = "手机端 - 公示牌扫码关联水库信息")
@Data
public class AppSignboardReservoirRespVO {

    @Schema(description = "水库ID")
    private Long id;

    @Schema(description = "水库编码")
    private String reservoirCode;

    @Schema(description = "水库名称")
    private String reservoirName;

    @Schema(description = "水库规模（字典：zd_skgm）")
    private String reservoirScale;

    @Schema(description = "所在乡镇")
    private String township;

    @Schema(description = "所在地点")
    private String location;

    @Schema(description = "管理单位（字典：zd_gldw）")
    private String[] managementUnit;

    @Schema(description = "水库河长信息列表")
    private List<AppSignboardReservoirHeadRespVO> heads;

    @Schema(description = "水库照片URL列表")
    private List<String> reservoirPhotos;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "主管部门")
    private String supervisingDepartment;

    @Schema(description = "水库性质（字典：zd_skxz）")
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
    private LocalDate completionDate;

    @Schema(description = "除险加固开工年月")
    private LocalDate reinforcementStartDate;

    @Schema(description = "除险加固竣工年月")
    private LocalDate reinforcementEndDate;

/*    @Schema(description = "除险加固日期（兼容历史字段）")
    private LocalDateTime reinforcementDate;*/

    @Schema(description = "设计洪水标准（文字/等级）")
    private String designFloodStandard;

    @Schema(description = "校核洪水标准（文字/等级）")
    private String verifiedFloodStandard;

    @Schema(description = "重现期设计（年）")
    private Integer designReturnPeriod;

    @Schema(description = "重现期校核（年）")
    private Integer checkReturnPeriod;

    @Schema(description = "总库容（m³）")
    private BigDecimal totalCapacity;

    @Schema(description = "兴利库容（m³）")
    private BigDecimal activeCapacity;

    @Schema(description = "调洪库容（m³）")
    private BigDecimal floodControlCapacity;

    @Schema(description = "死库容（m³）")
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

    @Schema(description = "溢洪道最大流量（m³/s）")
    private BigDecimal spillwayMaxDischarge;

    @Schema(description = "排洪河道名称")
    private String floodChannelName;

    @Schema(description = "排洪河道安全泄量（m³/s）")
    private String floodChannelSafeDischarge;

    @Schema(description = "灌溉/放水涵洞结构型式")
    private String culvertType;

    @Schema(description = "灌溉涵洞断面尺寸（宽*高）（m）")
    private String culvertSectionSize;

    @Schema(description = "灌溉涵洞闸门型式")
    private String culvertGateType;

    @Schema(description = "灌溉涵洞设计流量（m³/s）")
    private BigDecimal culvertDesignDischarge;

    @Schema(description = "涵洞出口底高程（m）")
    private BigDecimal culvertExitElevation;

    @Schema(description = "涵洞直径（m）")
    private BigDecimal culvertDiameter;

    @Schema(description = "涵洞高度（m）")
    private BigDecimal culvertHeight;

    @Schema(description = "年供水量（万m³）")
    private BigDecimal annualWaterSupply;

    @Schema(description = "宜鱼面积（亩）")
    private BigDecimal fisheryArea;

    @Schema(description = "是否水源地（饮用水源地）")
    private Boolean waterSource;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "几何类型（POINT/LINESTRING/POLYGON 等）")
    private String geomType;

    @Schema(description = "几何SRID")
    private Integer srid;

    @Schema(description = "几何WKT（附带SRID前缀，示例：SRID=4490;POINT(...)）")
    private String geomWkt;
}
