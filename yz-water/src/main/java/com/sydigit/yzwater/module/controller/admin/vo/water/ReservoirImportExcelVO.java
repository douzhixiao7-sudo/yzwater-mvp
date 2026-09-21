package com.sydigit.yzwater.module.controller.admin.vo.water;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 水库导入 Excel 行
 */
@Data
public class ReservoirImportExcelVO {

    @ExcelProperty("水库名称")
    private String reservoirName;

    @ExcelProperty("规模")
    private String reservoirScale;

    @ExcelProperty("所在乡镇")
    private String township;

    @ExcelProperty("管理单位")
    private String managementUnit;

    @ExcelProperty("水库性质")
    private String reservoirNature;

    @ExcelProperty("集水面积(平方公里)")
    private String catchmentArea;

    @ExcelProperty("高程基准面")
    private String elevationDatum;

    @ExcelProperty("设计（复核）抗震烈度")
    private String seismicIntensity;

    @ExcelProperty("竣工日期")
    private String completionDate;

    @ExcelProperty("除险加固开工年月")
    private String reinforcementStartDate;

    @ExcelProperty("除险加固竣工年月")
    private String reinforcementEndDate;

    @ExcelProperty("重现期设计(年)")
    private String designReturnPeriod;

    @ExcelProperty("重现期校核(年)")
    private String checkReturnPeriod;

    @ExcelProperty("总库容")
    private String totalCapacity;

    @ExcelProperty("兴利库容")
    private String activeCapacity;

    @ExcelProperty("调洪库容")
    private String floodControlCapacity;

    @ExcelProperty("死库容")
    private String deadCapacity;

    @ExcelProperty("校核水位")
    private String verifiedFloodLevel;

    @ExcelProperty("设计水位")
    private String designFloodLevel;

    @ExcelProperty("兴利水位")
    private String normalOperatingLevel;

    @ExcelProperty("汛限水位")
    private String floodLimitLevel;

    @ExcelProperty("死水位")
    private String deadLevel;

    @ExcelProperty("坝顶高程")
    private String damCrestElevation;

    @ExcelProperty("坝顶宽度")
    private String damTopWidth;

    @ExcelProperty("最大坝高")
    private String maxDamHeight;

    @ExcelProperty("坝顶长度")
    private String damTopLength;

    @ExcelProperty("挡浪墙顶高程")
    private String waveWallCrestElevation;

    @ExcelProperty("坝顶路面结构型式")
    private String damRoadSurfaceType;

    @ExcelProperty("防渗处理结构型式")
    private String seepageControlType;

    @ExcelProperty("防渗处理起止桩号")
    private String seepagePileRange;

    @ExcelProperty("防渗处理起止高程")
    private String seepageElevRange;

    @ExcelProperty("护坡结构型式")
    private String slopeProtectionType;

    @ExcelProperty("护坡起止高程")
    private String slopeProtectionElevRange;

    @ExcelProperty("溢洪道控制方式（有闸、开敞式）")
    private String spillwayControlType;

    @ExcelProperty("溢洪道有无交通桥")
    private String spillwayHasBridge;

    @ExcelProperty("溢洪道堰顶高程(米)")
    private String spillwayCrestElevation;

    @ExcelProperty("溢洪道底宽(孔*宽)(米)")
    private String spillwayBottomWidth;

    @ExcelProperty("溢洪道最大流量(立方米/秒)")
    private String spillwayMaxDischarge;

    @ExcelProperty("排洪河道名称")
    private String floodChannelName;

    @ExcelProperty("排洪河道安全泄量(立方米/秒)")
    private String floodChannelSafeDischarge;

    @ExcelProperty("灌溉涵洞结构型式")
    private String culvertType;

    @ExcelProperty("灌溉涵洞断面尺寸(宽*高)(米)")
    private String culvertSectionSize;

    @ExcelProperty("灌溉涵洞闸门型式")
    private String culvertGateType;

    @ExcelProperty("灌溉涵洞设计流量m³/s")
    private String culvertDesignDischarge;

    @ExcelProperty("设计灌溉面积")
    private String designIrrigationArea;

    @ExcelProperty("实际灌溉面积")
    private String actualIrrigationArea;

    @ExcelProperty("年供水量 （万立方米）")
    private String annualWaterSupply;

    @ExcelProperty("宜鱼面积(亩)")
    private String fisheryArea;

    @ExcelProperty("是否水源地")
    private String waterSource;

    @ExcelProperty("供水对象")
    private String waterSupplyTarget;
}
