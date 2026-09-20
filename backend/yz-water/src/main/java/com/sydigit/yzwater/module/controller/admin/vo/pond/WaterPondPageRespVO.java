package com.sydigit.yzwater.module.controller.admin.vo.pond;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "坑塘分页项（台账字段）")
@Data
public class WaterPondPageRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "设施基础表 ID")
    private Long facilityId;

    @Schema(description = "土地权属")
    private String ownershipType;

    @Schema(description = "占农登权面积")
    private BigDecimal occupyFarmArea;

    @Schema(description = "行政区代码")
    private String villageCode;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "四至东")
    private String eastTo;

    @Schema(description = "四至南")
    private String southTo;

    @Schema(description = "四至西")
    private String westTo;

    @Schema(description = "四至北")
    private String northTo;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "坐落位置（建议到组一级）")
    private String locationDesc;

    @Schema(description = "行政区名")
    private String villageName;

    @Schema(description = "实测面积（㎡）")
    private BigDecimal areaSqm;

    @Schema(description = "资源编号")
    private String resourceCode;

    @Schema(description = "权属单位名")
    private String ownerUnit;

    @Schema(description = "使用状态")
    private String usageStatus;

    @Schema(description = "资源性质")
    private String resourceNature;

    @Schema(description = "占用情况")
    private String occupationStatus;

    @Schema(description = "调查员姓名")
    private String surveyor;

    @Schema(description = "调查员联系方式")
    private String surveyorPhone;

    @Schema(description = "中心点经度")
    private BigDecimal centerLon;

    @Schema(description = "中心点纬度")
    private BigDecimal centerLat;

    @Schema(description = "面积（亩）")
    private BigDecimal areaMu;

    @Schema(description = "国土地类")
    private String landType;
}
