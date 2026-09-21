package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 河长信息（二维码数据）
 */
@Schema(description = "仪征管理后台 - 河长信息（二维码数据）")
@Data
public class RiverHeadQrVO {

    @Schema(description = "河长记录ID")
    private Long id;

    @Schema(description = "所属河段ID，空表示河道层级")
    private Long riverSectionId;

    @Schema(description = "河段名称")
    private String sectionName;

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

    @Schema(description = "监督单位列表")
    private List<RiverSupervisionQrVO> supervisions;
}
