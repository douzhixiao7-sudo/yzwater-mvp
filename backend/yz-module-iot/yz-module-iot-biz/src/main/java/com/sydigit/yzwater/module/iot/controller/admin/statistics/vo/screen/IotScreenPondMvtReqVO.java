package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "大屏 - 坑塘 MVT 瓦片过滤条件（与台账筛选字段对齐）")
@Data
public class IotScreenPondMvtReqVO {

    @Schema(description = "行政区划 id（含子级 village_code）", example = "321081104")
    private Long areaId;

    @Schema(description = "行政区划代码（可选；areaId 为空时生效，含子级）", example = "321081100")
    private String villageCode;

    @Schema(description = "资源名称 / 坑塘名称（模糊）")
    private String resourceName;

    @Schema(description = "名称别名（等同 resourceName）")
    private String name;

    @Schema(description = "关键字别名（等同 resourceName）")
    private String keyword;

    @Schema(description = "资源编号（模糊）")
    private String resourceCode;

    @Schema(description = "坐落位置（模糊）")
    private String locationDesc;

    @Schema(description = "权属单位名（模糊）")
    private String ownerUnit;

    @Schema(description = "土地权属（精确）")
    private String ownershipType;

    @Schema(description = "资源类型（精确）")
    private String resourceType;

    @Schema(description = "国土地类（模糊）")
    private String landType;

    @Schema(description = "使用状态（精确）")
    private String usageStatus;

    @Schema(description = "资源性质（精确）")
    private String resourceNature;

    @Schema(description = "占用情况（精确）")
    private String occupationStatus;

    @Schema(description = "行政区名（模糊）")
    private String villageName;

    @Schema(description = "备注（模糊）")
    private String remark;

    @Schema(description = "调查员（模糊）")
    private String surveyor;

    @Schema(description = "东至（模糊）")
    private String eastTo;

    @Schema(description = "南至（模糊）")
    private String southTo;

    @Schema(description = "西至（模糊）")
    private String westTo;

    @Schema(description = "北至（模糊）")
    private String northTo;

    @Schema(description = "面积（亩）最小值")
    private BigDecimal areaMuMin;

    @Schema(description = "面积（亩）最大值")
    private BigDecimal areaMuMax;

    @Schema(description = "实测面积（㎡）最小值")
    private BigDecimal areaSqmMin;

    @Schema(description = "实测面积（㎡）最大值")
    private BigDecimal areaSqmMax;

    @Schema(description = "占农登权面积最小值")
    private BigDecimal occupyFarmAreaMin;

    @Schema(description = "占农登权面积最大值")
    private BigDecimal occupyFarmAreaMax;

    @Schema(description = "缓冲区中心经度（与 centerLat、bufferRadiusM 同时传才生效）", example = "119.184")
    private BigDecimal centerLon;

    @Schema(description = "缓冲区中心纬度", example = "32.272")
    private BigDecimal centerLat;

    @Schema(description = "缓冲区半径（米）", example = "500")
    private BigDecimal bufferRadiusM;

}
