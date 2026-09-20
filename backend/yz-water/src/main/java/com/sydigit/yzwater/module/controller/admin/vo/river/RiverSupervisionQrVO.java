package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河段监督信息（二维码数据）
 */
@Schema(description = "仪征管理后台 - 河段监督信息（二维码数据）")
@Data
public class RiverSupervisionQrVO {

    @Schema(description = "监督记录ID")
    private Long id;

    @Schema(description = "所属河段ID")
    private Long riverSectionId;

    @Schema(description = "关联河长ID")
    private Long riverChannelManagementId;

    @Schema(description = "监督单位")
    private String supervisionUnit;

    @Schema(description = "监督电话")
    private String supervisionContact;
}
