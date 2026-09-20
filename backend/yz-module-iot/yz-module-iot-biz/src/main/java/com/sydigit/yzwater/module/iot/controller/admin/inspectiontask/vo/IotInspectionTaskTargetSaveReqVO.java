package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "IoT - 巡检任务对象新增 Request VO")
@Data
public class IotInspectionTaskTargetSaveReqVO {

    @Schema(description = "对象 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "巡检对象不能为空")
    private Long targetId;

    @Schema(description = "对象名称（前端回填，可为空）")
    private String targetName;

    @Schema(description = "对象排序")
    private Integer targetSort;

}
