package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "IoT - 备件台账精简 Response VO")
@Data
public class IotSpareSimpleRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "备件名称", example = "闸门轴承")
    private String spareName;

    @Schema(description = "备件规格", example = "M12")
    private String spareSpec;

    @Schema(description = "备件型号", example = "XH-100")
    private String spareModel;

}
