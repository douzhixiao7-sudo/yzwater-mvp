package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 河道导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 河道导出 Excel VO")
public class RiverChannelExportExcelVO {

    @ExcelProperty("河道编码")
    private String riverCode;

    @ExcelProperty("河道名称")
    private String riverName;

    @ExcelProperty("河道长度(km)")
    private BigDecimal lengthKm;

    @ExcelProperty("流域面积(km2)")
    private BigDecimal catchmentKm2;

    @ExcelProperty("河道平均比降")
    private BigDecimal averageSlope;

    @ExcelProperty("所在流域")
    private String basinTypeLabel;

    @ExcelProperty("生态类型")
    private String ecologyTypeLabel;

    @ExcelProperty("跨界类别")
    private String transboundaryTypeLabel;

    @ExcelProperty("防洪标准")
    private String floodStandardLabel;

    @ExcelProperty("堤防等级")
    private String embankmentLevelLabel;

    @ExcelProperty("堤防长度(km)")
    private BigDecimal embankmentLength;

    @ExcelProperty("中心点经度")
    private BigDecimal centroidLongitude;

    @ExcelProperty("中心点纬度")
    private BigDecimal centroidLatitude;

    @ExcelProperty("河口经度")
    private BigDecimal riverEndLongitude;

    @ExcelProperty("河口纬度")
    private BigDecimal riverEndLatitude;

    @ExcelProperty("河源经度")
    private BigDecimal riverSourceLongitude;

    @ExcelProperty("河源纬度")
    private BigDecimal riverSourceLatitude;

    @ExcelProperty("流经地区")
    private String flowAreas;

    @ExcelProperty("历史最高水位(m)")
    private BigDecimal historicalMaxWaterLevel;

    @ExcelProperty("最高水位时间")
    private LocalDateTime maxWaterLevelDate;

    @ExcelProperty("最低水位时间")
    private LocalDateTime lowestWaterLevelDate;

    @ExcelProperty("历史最低水位(m)")
    private BigDecimal historicalMinWaterLevel;

    @ExcelProperty("平均年径流量")
    private BigDecimal averageAnnualRunoff;

    @ExcelProperty("发源山系")
    private String sourceMountainRange;

    @ExcelProperty("河流归宿")
    private String riverTerminus;

    @ExcelProperty("河流级别")
    private String riverLevelLabel;

    @ExcelProperty("河口位置")
    private String riverEntrance;

    @ExcelProperty("河源位置")
    private String riverOrigin;

    @ExcelProperty("河道类型")
    private String riverTypeLabel;

    @ExcelProperty("起点")
    private String startPoint;

    @ExcelProperty("终点")
    private String endPoint;

   /* @ExcelProperty("河道照片")
    private String riverPhotos;*/

    @ExcelProperty("河道水质情况")
    private String waterQualityStatusLabel;

   /* @ExcelProperty("关联设施")
    private String associatedFacilities;*/

    @ExcelProperty("河段划分数量")
    private Integer riverSectionCount;

    @ExcelProperty("所属乡镇")
    private String town;

    @ExcelProperty("管理单位")
    private String managementUnit;

    @ExcelProperty("备注")
    private String remarks;
}
