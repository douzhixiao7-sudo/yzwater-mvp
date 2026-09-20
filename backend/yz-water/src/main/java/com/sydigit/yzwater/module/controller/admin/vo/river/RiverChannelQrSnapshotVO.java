package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 河道二维码数据快照
 */
@Schema(description = "仪征管理后台 - 河道二维码数据快照")
@Data
public class RiverChannelQrSnapshotVO {

    @Schema(description = "河道基础信息")
    private RiverChannelQrSummaryVO channel;

    @Schema(description = "未绑定河段的河长信息列表")
    private List<RiverHeadQrVO> channelHeads;

    @Schema(description = "河段列表（含河长与监督信息）")
    private List<RiverSectionQrVO> sections;
}
