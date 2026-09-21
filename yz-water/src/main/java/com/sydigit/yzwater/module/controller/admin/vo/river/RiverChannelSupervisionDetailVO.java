package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 监督单位详情
 */
@Schema(description = "仪征管理后台 - 监督单位详情")
@Data
public class RiverChannelSupervisionDetailVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "河段ID")
    private Long riverSectionId;

    @Schema(description = "河长管理ID")
    private Long riverChannelManagementId;

    @Schema(description = "监督单位")
    private String supervisionUnit;

    @Schema(description = "监督电话")
    private String supervisionContact;
}
