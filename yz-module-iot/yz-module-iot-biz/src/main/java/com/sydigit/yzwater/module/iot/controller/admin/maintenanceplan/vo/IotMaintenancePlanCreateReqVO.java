package com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.maintenanceplan.IotMaintenancePlanTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "IoT - 养护计划创建 Request VO")
@Data
public class IotMaintenancePlanCreateReqVO {

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备不能为空")
    private Long deviceId;

    @Schema(description = "计划养护日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-03")
    @NotNull(message = "计划养护日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate planDate;

    @Schema(description = "养护类型", example = "periodic")
    @InEnum(value = IotMaintenancePlanTypeEnum.class, message = "养护类型必须是 {value}")
    private String maintainType;

    @Schema(description = "养护项目")
    private String maintainItems;

    @Schema(description = "备注")
    private String remark;
}
