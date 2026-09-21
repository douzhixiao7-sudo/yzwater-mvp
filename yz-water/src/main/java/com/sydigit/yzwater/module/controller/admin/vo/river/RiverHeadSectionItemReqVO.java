package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 河段对应的河长与监督信息
 */
@Schema(description = "仪征管理后台 - 河段河长保存项")
@Data
public class RiverHeadSectionItemReqVO {

    @Schema(description = "河段ID，可为空表示未划分河段")
    private Long sectionId;

    @Schema(description = "关联对象ID（河道为 riverChannelId；河段为 sectionId）")
    private Long referenceId;

    @Schema(description = "关联对象类型（river：河道；river_section：河段）")
    private String referenceType;

    @Schema(description = "河段名称")
    private String sectionName;

    @Schema(description = "河长列表")
    private List<RiverHeadItemReqVO> heads;

    @Schema(description = "监督单位列表")
    private List<RiverHeadSupervisionItemReqVO> supervisions;
}
