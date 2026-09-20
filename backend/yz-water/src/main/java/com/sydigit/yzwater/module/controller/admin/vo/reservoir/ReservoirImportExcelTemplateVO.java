package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 水库导入模板行
 */
@Data
public class ReservoirImportExcelTemplateVO {

    @ExcelProperty("水库名称")
    private String reservoirName;

    @ExcelProperty("所在乡镇名称")
    private String townshipName;

    @ExcelProperty("所在地点")
    private String location;

    @ExcelProperty("经度")
    private String longitude;

    @ExcelProperty("纬度")
    private String latitude;

    @ExcelProperty("主管部门")
    private String supervisingDepartment;

    @ExcelProperty("灌溉面积（亩）")
    private String irrigationArea;

    @ExcelProperty("设计灌溉面积（亩）")
    private String designIrrigationArea;

    @ExcelProperty("实际灌溉面积（亩）")
    private String actualIrrigationArea;

    @ExcelProperty("保护面积（亩）")
    private String protectionArea;

    @ExcelProperty("下游主要设施")
    private String downstreamFacilities;

    @ExcelProperty("供水对象")
    private String waterSupplyTarget;

    @ExcelProperty("集水面积（平方公里）")
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

    @ExcelProperty("重现期设计（年）")
    private String designReturnPeriod;

    @ExcelProperty("重现期校核（年）")
    private String checkReturnPeriod;

    @ExcelProperty("总库容（m3）")
    private String totalCapacity;

    @ExcelProperty("兴利库容（m3）")
    private String activeCapacity;

    @ExcelProperty("调洪库容（m3）")
    private String floodControlCapacity;

    @ExcelProperty("死库容（m3）")
    private String deadCapacity;

    @ExcelProperty("校核水位（m）")
    private String verifiedFloodLevel;

    @ExcelProperty("设计水位（m）")
    private String designFloodLevel;

    @ExcelProperty("兴利水位（m）")
    private String normalOperatingLevel;

    @ExcelProperty("汛限水位（m）")
    private String floodLimitLevel;

    @ExcelProperty("死水位（m）")
    private String deadLevel;

    @ExcelProperty("备注")
    private String remarks;
}
