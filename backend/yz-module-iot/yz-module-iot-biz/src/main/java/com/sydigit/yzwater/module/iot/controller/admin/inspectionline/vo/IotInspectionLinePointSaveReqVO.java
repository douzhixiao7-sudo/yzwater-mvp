package com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "IoT - 巡检线路点位新增/修改 Request VO")
@Data
public class IotInspectionLinePointSaveReqVO {

    @Schema(description = "点位 ID", example = "1024")
    private Long id;

    @Schema(description = "点位顺序", example = "1")
    private Integer pointSort;

    @Schema(description = "点位名称", example = "1号泵站")
    private String pointName;

    @Schema(description = "点位类型（1 设备，2 位置，3 自定义坐标）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "点位类型不能为空")
    private Integer pointType;

    @Schema(description = "设备 ID（pointType=1 时使用）", example = "1001")
    private Long deviceId;

    @Schema(description = "位置 ID（pointType=2 时使用）", example = "2001")
    private Long locationId;

    @Schema(description = "经度（pointType=3 时使用）")
    private BigDecimal longitude;

    @Schema(description = "纬度（pointType=3 时使用）")
    private BigDecimal latitude;

    @Schema(description = "备注")
    private String remark;

}
