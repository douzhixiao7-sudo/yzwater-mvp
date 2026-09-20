package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 河长批量保存请求
 */
@Schema(description = "仪征管理后台 - 河长批量保存请求")
@Data
public class RiverHeadBatchSaveReqVO {

    @Schema(description = "河道ID")
    @NotNull(message = "河道ID不能为空")
    private Long riverChannelId;

    @Schema(description = "河段及河长信息")
    private List<RiverHeadSectionItemReqVO> sections;
}
