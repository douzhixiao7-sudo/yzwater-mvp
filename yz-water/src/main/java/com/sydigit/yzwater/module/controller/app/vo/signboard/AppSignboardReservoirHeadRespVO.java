package com.sydigit.yzwater.module.controller.app.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 手机端-公示牌扫码返回的水库河长信息
 */
@Schema(description = "手机端-公示牌扫码返回的水库河长信息")
@Data
public class AppSignboardReservoirHeadRespVO {

    @Schema(description = "河长记录ID")
    private Long id;

    @Schema(description = "河长级别(字典: zd_hzjb)")
    private String headLevel;

    @Schema(description = "河长级别(字典标签)")
    private String headLevelLabel;

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "河长工作单位")
    private String headUnit;

    @Schema(description = "河长联系方式")
    private String headContact;

    @Schema(description = "河长职责")
    private String responsibilities;

    @Schema(description = "备注")
    private String remarks;
}

