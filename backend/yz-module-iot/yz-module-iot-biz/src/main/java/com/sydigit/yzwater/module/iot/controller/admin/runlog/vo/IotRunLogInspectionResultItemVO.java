package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 运行日志巡检结果项
 */
@Schema(description = "IoT - 运行日志巡检结果项")
@Data
public class IotRunLogInspectionResultItemVO {

    @Schema(description = "检查项ID")
    private Long itemId;

    @Schema(description = "检查项名称")
    private String itemName;

    @Schema(description = "适用对象类型", example = "device")
    private String targetType;

    @Schema(description = "适用对象ID")
    private Long targetId;

    @Schema(description = "适用对象名称")
    private String targetName;

    @Schema(description = "检查结果等级值")
    private String checkResult;

    @Schema(description = "检查备注")
    private String checkRemark;

    @Schema(description = "记录项")
    private List<IotRunLogInspectionResultRecordVO> records;
}
