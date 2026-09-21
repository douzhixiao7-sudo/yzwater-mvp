package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 员工排班导入结果
 */
@Schema(description = "IoT - 员工排班导入 Response VO")
@Data
@Builder
public class IotShiftScheduleImportRespVO {

    @Schema(description = "成功导入数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer successCount;

    @Schema(description = "失败数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer failureCount;

    @Schema(description = "失败明细", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> failureMessages;
}
