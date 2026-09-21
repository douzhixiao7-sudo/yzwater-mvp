package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 备件盘点记录 Response VO")
@Data
public class IotSpareCheckRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "备件 ID", example = "1024")
    private Long spareId;

    @Schema(description = "备件名称")
    private String spareName;

    @Schema(description = "备件规格")
    private String spareSpec;

    @Schema(description = "备件型号")
    private String spareModel;

    @Schema(description = "盘点时间")
    private LocalDateTime checkTime;

    @Schema(description = "系统库存")
    private Integer systemQty;

    @Schema(description = "实盘数量")
    private Integer actualQty;

    @Schema(description = "差异数量")
    private Integer diffQty;

    @Schema(description = "盘点结果")
    private String resultStatus;

    @Schema(description = "盘点人")
    private String checkerUserName;

    @Schema(description = "是否已反馈")
    private Boolean applied;

    @Schema(description = "反馈人")
    private String applyUserName;

    @Schema(description = "反馈时间")
    private LocalDateTime applyTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
