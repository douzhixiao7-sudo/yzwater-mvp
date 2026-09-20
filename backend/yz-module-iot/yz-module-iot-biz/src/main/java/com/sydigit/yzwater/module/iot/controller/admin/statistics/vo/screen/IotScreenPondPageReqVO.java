package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 大屏坑塘分页 Request VO（与 MVT / 台账筛选字段对齐）")
@Data
public class IotScreenPondPageReqVO {

    @Schema(description = "坑塘主键 id；传入时按单条详情查询，可不传 pageNo/pageSize", example = "10001")
    private Long id;

    @Schema(description = "行政区划 id（可选，按 village_code 过滤，含子级）", example = "321081100")
    private Long areaId;

    @Schema(description = "行政区划代码（可选；areaId 为空时生效，含子级）", example = "321081100")
    private String villageCode;

    @Schema(description = "页码，从 1 开始（列表查询必传；按 id 查详情时可省略）", example = "1")
    private Integer pageNo;

    @Schema(description = "每页条数（列表查询必传，最大 1000；按 id 查详情时可省略）", example = "500")
    private Integer pageSize;

    @Schema(description = "视口最小经度（可选；与 minLat/maxLon/maxLat 同时传入时按中心点过滤）", example = "119.0")
    private BigDecimal minLon;

    @Schema(description = "视口最小纬度（可选）", example = "32.2")
    private BigDecimal minLat;

    @Schema(description = "视口最大经度（可选）", example = "119.3")
    private BigDecimal maxLon;

    @Schema(description = "视口最大纬度（可选）", example = "32.5")
    private BigDecimal maxLat;

    @Schema(description = "是否返回面几何 GeoJSON，默认 false（大屏请用 MVT）", example = "false")
    private Boolean includeGeometry;

    @Schema(description = "资源名称 / 坑塘名称（模糊）")
    private String resourceName;

    @Schema(description = "名称别名（等同 resourceName，兼容前端传 name）")
    private String name;

    @Schema(description = "关键字别名（等同 resourceName，兼容前端传 keyword）")
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

}
