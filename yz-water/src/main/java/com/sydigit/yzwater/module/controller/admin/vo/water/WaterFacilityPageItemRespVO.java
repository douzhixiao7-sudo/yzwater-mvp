package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 水利设施分页返回项
 */
@Schema(description = "仪征管理后台 - 水利设施分页返回项")
@Data
public class WaterFacilityPageItemRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "设施编码")
    private String facilityCode;

    @Schema(description = "设施名称")
    private String facilityName;

    @Schema(description = "设施类别")
    private String facilityType;

    @Schema(description = "行政区划名称")
    private String adminRegion;

    @Schema(description = "行政区划编码")
    private String adminRegionCode;

    @Schema(description = "管理单位")
    private String manageUnit;

    @Schema(description = "数据来源")
    private String sourceType;

    @Schema(description = "创建时间")
    private String createTime;
}
