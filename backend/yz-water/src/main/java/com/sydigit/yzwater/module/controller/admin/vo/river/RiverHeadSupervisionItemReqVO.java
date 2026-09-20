package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河段监督单位保存项
 */
@Schema(description = "仪征管理后台 - 河段监督单位保存项")
@Data
public class RiverHeadSupervisionItemReqVO {

    @Schema(description = "监督单位")
    private String supervisionUnit;

    @Schema(description = "监督电话")
    private String supervisionContact;
}
