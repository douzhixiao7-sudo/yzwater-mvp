package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 水利设施修改请求
 */
@Schema(description = "仪征管理后台 - 水利设施修改请求")
@Data
public class WaterFacilityUpdateReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "主键不能为空")
    private Long id;

    @Schema(description = "设施名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设施名称不能为空")
    private String facilityName;

    @Schema(description = "设施类别字典值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设施类别不能为空")
    private String facilityType;

    @Schema(description = "行政区域名称")
    private String adminRegion;

    @Schema(description = "行政区域代码")
    private String adminRegionCode;

    @Schema(description = "管理单位")
    private String manageUnit;

    @Schema(description = "数据来源")
    private String sourceType;

    @Schema(description = "扩展属性")
    private Map<String, Object> attributes;
}
