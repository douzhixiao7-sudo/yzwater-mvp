package com.sydigit.yzwater.module.controller.admin.vo.pond;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Schema(description = "坑塘分页查询")
@Data
@EqualsAndHashCode(callSuper = true)
public class WaterPondPageReqVO extends PageParam {

    @Schema(description = "资源名称（模糊）")
    private String resourceName;

    @Schema(description = "资源编号（模糊）")
    private String resourceCode;

    @Schema(description = "行政区划代码（含下属子级，如选镇可查该镇下全部坑塘）")
    private String villageCode;

    @Schema(description = "行政区名（模糊）")
    private String villageName;

    @Schema(description = "坐落位置（模糊，建议到组一级）")
    private String locationDesc;

    @Schema(description = "权属单位名（模糊）")
    private String ownerUnit;

    @Schema(description = "土地权属")
    private String ownershipType;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "国土地类（模糊）")
    private String landType;

    @Schema(description = "使用状态")
    private String usageStatus;

    @Schema(description = "资源性质")
    private String resourceNature;

    @Schema(description = "占用情况")
    private String occupationStatus;

    @Schema(description = "备注（模糊）")
    private String remark;

    @Schema(description = "调查员姓名（模糊）")
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
