package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 巡检标准适用对象新增/修改 Request VO")
@Data
public class IotInspectionStandardTargetSaveReqVO {

    @Schema(description = "适用对象 ID", example = "1024")
    private Long id;

    @Schema(description = "适用对象类型（device 或 zd_sslb.value）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "适用对象类型不能为空")
    private String targetType;

    @Schema(description = "适用对象业务 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "适用对象不能为空")
    private Long targetId;

    @Schema(description = "所属站点（仅 device 类型必填）")
    private String stationId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "检查结果等级配置")
    @Valid
    private List<IotInspectionCheckResultConfigVO> checkResultConfigs;

    @Schema(description = "检查项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检查项不能为空")
    @Valid
    private List<IotInspectionStandardItemSaveReqVO> items;

}
