package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareIoTypeEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareUsageTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 备件出入库新增/修改 Request VO")
@Data
public class IotSpareIoSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "备件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "备件不能为空")
    private Long spareId;

    @Schema(description = "出入库类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "IN")
    @NotBlank(message = "出入库类型不能为空")
    @InEnum(value = IotSpareIoTypeEnum.class, message = "出入库类型必须是 {value}")
    private String ioType;

    @Schema(description = "出入库时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "出入库时间不能为空")
    private LocalDateTime ioTime;

    @Schema(description = "出入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "出入库数量不能为空")
    @Min(value = 1, message = "出入库数量必须大于 0")
    private Integer ioQty;

    @Schema(description = "出库用途类型", example = "fault")
    @InEnum(value = IotSpareUsageTypeEnum.class, message = "出库用途类型必须是 {value}")
    private String usageType;

    @Schema(description = "用途关联 ID", example = "123")
    private Long usageId;

    @Schema(description = "备注")
    private String remark;

}
