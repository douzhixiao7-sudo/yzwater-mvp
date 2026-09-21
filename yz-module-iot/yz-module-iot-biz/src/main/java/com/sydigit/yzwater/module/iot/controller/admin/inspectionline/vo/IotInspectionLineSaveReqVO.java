package com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "IoT - 巡检线路新增/修改 Request VO")
@Data
public class IotInspectionLineSaveReqVO {

    @Schema(description = "线路 ID", example = "1024")
    private Long id;

    @Schema(description = "所属闸站", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属闸站不能为空")
    private String stationId;

    @Schema(description = "线路名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "线路名称不能为空")
    private String lineName;

    @Schema(description = "巡检类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "巡检类型不能为空")
    private String inspectionType;

    @Schema(description = "区域类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "区域类型不能为空")
    private String areaType;

    @Schema(description = "线路描述")
    private String lineDesc;

    @Schema(description = "起点经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "起点经度不能为空")
    private BigDecimal startLongitude;

    @Schema(description = "起点纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "起点纬度不能为空")
    private BigDecimal startLatitude;

    @Schema(description = "终点经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "终点经度不能为空")
    private BigDecimal endLongitude;

    @Schema(description = "终点纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "终点纬度不能为空")
    private BigDecimal endLatitude;

    @Schema(description = "线路总长度（米）")
    private BigDecimal totalLengthMeter;

    @Schema(description = "状态（0 启用，1 停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "点位列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "线路点位不能为空")
    @Valid
    private List<IotInspectionLinePointSaveReqVO> points;

}
