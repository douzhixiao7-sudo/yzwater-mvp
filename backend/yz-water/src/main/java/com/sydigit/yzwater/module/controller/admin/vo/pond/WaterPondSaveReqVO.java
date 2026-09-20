package com.sydigit.yzwater.module.controller.admin.vo.pond;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "坑塘详情 / 编辑")
@Data
public class WaterPondSaveReqVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "设施基础表 ID")
    private Long facilityId;

    @Schema(description = "资源编号")
    private String resourceCode;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "坐落位置")
    private String locationDesc;

    @Schema(description = "行政村")
    private String villageName;

    @Schema(description = "行政区划代码")
    private String villageCode;

    @Schema(description = "权属单位")
    private String ownerUnit;

    @Schema(description = "权属人")
    private String ownerPerson;

    @Schema(description = "土地权属")
    private String ownershipType;

    @Schema(description = "国土地类")
    private String landType;

    @Schema(description = "面积（平方米）")
    private BigDecimal areaSqm;

    @Schema(description = "面积（亩）")
    private BigDecimal areaMu;

    @Schema(description = "占农经权面积")
    private BigDecimal occupyFarmArea;

    @Schema(description = "东至")
    private String eastTo;

    @Schema(description = "南至")
    private String southTo;

    @Schema(description = "西至")
    private String westTo;

    @Schema(description = "北至")
    private String northTo;

    @Schema(description = "使用状态")
    private String usageStatus;

    @Schema(description = "资源性质")
    private String resourceNature;

    @Schema(description = "占用情况")
    private String occupationStatus;

    @Schema(description = "调查员")
    private String surveyor;

    @Schema(description = "联系方式")
    private String surveyorPhone;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "中心点经度")
    private BigDecimal centerLon;

    @Schema(description = "中心点纬度")
    private BigDecimal centerLat;

    @Schema(description = "几何 GeoJSON（仅 geometry 对象字符串，WGS84）")
    private String geometryGeoJson;
}
