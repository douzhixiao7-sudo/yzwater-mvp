package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 巡检标准适用对象 Response VO")
@Data
public class IotInspectionStandardTargetRespVO {

    @Schema(description = "适用对象 ID")
    private Long id;

    @Schema(description = "适用对象类型（device 或 zd_sslb.value）")
    private String targetType;

    @Schema(description = "适用对象业务 ID")
    private Long targetId;

    @Schema(description = "适用对象名称")
    private String targetName;

    @Schema(description = "所属站点（仅 device 类型使用）")
    private String stationId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "检查项数量")
    private Integer itemCount;

    @Schema(description = "检查结果等级配置")
    private List<IotInspectionCheckResultConfigVO> checkResultConfigs;

    @Schema(description = "检查项列表")
    private List<IotInspectionStandardItemRespVO> items;

}
