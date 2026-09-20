package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 巡检标准 Response VO")
@Data
public class IotInspectionStandardRespVO {

    @Schema(description = "标准 ID")
    private Long id;

    @Schema(description = "标准名称")
    private String standardName;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "建议周期")
    private String suggestCycleUnit;

    @Schema(description = "建议周期值")
    private Integer suggestCycleValue;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "适用对象数量")
    private Integer targetCount;

    @Schema(description = "检查项数量")
    private Integer itemCount;

    @Schema(description = "适用对象类型列表")
    private List<String> targetTypes;

    @Schema(description = "适用对象名称列表")
    private List<String> targetNames;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "适用对象列表")
    private List<IotInspectionStandardTargetRespVO> targets;

}
