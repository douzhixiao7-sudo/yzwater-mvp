package com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "IoT - 巡检线路点位 Response VO")
@Data
public class IotInspectionLinePointRespVO {

    @Schema(description = "点位 ID")
    private Long id;

    @Schema(description = "点位顺序")
    private Integer pointSort;

    @Schema(description = "点位名称")
    private String pointName;

    @Schema(description = "点位类型")
    private Integer pointType;

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "位置 ID")
    private Long locationId;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "备注")
    private String remark;

}
