package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 河段对应的河长与监督详情
 */
@Schema(description = "仪征管理后台 - 河段河长与监督详情")
@Data
public class RiverManagementSectionDetailVO {

    @Schema(description = "河段ID")
    private Long sectionId;

    @Schema(description = "河段名称")
    private String sectionName;

    @Schema(description = "河长列表")
    private List<RiverChannelManagementDetailVO> heads;

    @Schema(description = "监督列表")
    private List<RiverChannelSupervisionDetailVO> supervisions;
}
