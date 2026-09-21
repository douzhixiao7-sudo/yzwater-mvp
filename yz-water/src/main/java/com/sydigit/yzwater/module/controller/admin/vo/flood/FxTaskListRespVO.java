package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 风险隐患点列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 风险隐患点列表 Response VO")
public class FxTaskListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "工程名称")
    private String name;

    @Schema(description = "险工位置")
    private String addr;

    @Schema(description = "所属河道 ID")
    private Long riverChannelId;

    @Schema(description = "所属河道名称")
    private String riverChannelName;

    @Schema(description = "防汛等级")
    private String level;

    @Schema(description = "险情描述")
    private String content;

    @Schema(description = "应对措施")
    private String counterMeasures;

    @Schema(description = "附件 URL 数组")
    private List<String> files;

    @Schema(description = "排序号")
    private Integer sort;
}
