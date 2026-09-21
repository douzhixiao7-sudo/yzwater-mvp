package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 河道导入模板行
 */
@Data
public class RiverChannelImportExcelVO {

    @ExcelProperty("河道名称")
    private String riverName;

    @ExcelProperty("河道长度(km)")
    private String lengthKm;

    @ExcelProperty("流域面积(平方公里)")
    private String catchmentKm2;

    @ExcelProperty("河道平均比降")
    private String averageSlope;

    @ExcelProperty("中心点经度")
    private String centroidLongitude;

    @ExcelProperty("中心点纬度")
    private String centroidLatitude;

    @ExcelProperty("河口经度")
    private String riverEndLongitude;

    @ExcelProperty("河口纬度")
    private String riverEndLatitude;

    @ExcelProperty("河源经度")
    private String riverSourceLongitude;

    @ExcelProperty("河源纬度")
    private String riverSourceLatitude;

    @ExcelProperty("历史最高水位(m)")
    private String historicalMaxWaterLevel;

    @ExcelProperty("最高水位时间")
    private String maxWaterLevelDate;

    @ExcelProperty("最低水位时间")
    private String lowestWaterLevelDate;

    @ExcelProperty("历史最低水位(m)")
    private String historicalMinWaterLevel;

    @ExcelProperty("年均径流量")
    private String averageAnnualRunoff;

    @ExcelProperty("发源山系")
    private String sourceMountainRange;

    @ExcelProperty("河流归宿")
    private String riverTerminus;

    @ExcelProperty("河口位置")
    private String riverEntrance;

    @ExcelProperty("河源位置")
    private String riverOrigin;

    @ExcelProperty("起点")
    private String startPoint;

    @ExcelProperty("终点")
    private String endPoint;

    @ExcelProperty("关联设施")
    private String associatedFacilities;

    @ExcelProperty("管理单位")
    private String managementUnit;

    @ExcelProperty("河长职责")
    private String responsibilities;

    @ExcelProperty("备注")
    private String remarks;
}
