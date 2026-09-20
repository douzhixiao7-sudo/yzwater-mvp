package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 巡检任务提交结果检查项目明细 VO")
@Data
public class IotInspectionTaskSubmitResultItemVO {

    @Schema(description = "检查项ID", example = "1001")
    private Long itemId;

    @Schema(description = "检查项名称", example = "水位标尺检查")
    private String itemName;

    @Schema(description = "巡检对象ID", example = "2001")
    private Long targetId;

    @Schema(description = "巡检对象名称", example = "一号闸门")
    private String targetName;

    @Schema(description = "检查结果等级", example = "qualified")
    private String checkResult;

    @Schema(description = "检查备注", example = "现场检查无明显异常")
    private String checkRemark;

    @Schema(description = "附件地址列表")
    private List<String> attachments;

    @Schema(description = "记录项明细")
    @Valid
    private List<IotInspectionTaskSubmitResultRecordVO> records;
}
