package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调度方案 Response VO
 */
@Schema(description = "IoT - 调度方案 Response VO")
@Data
public class IotDispatchPlanRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "方案编号（系统自动生成唯一标识）", example = "7F5A2C1D9E4B4F7AA1D8E6C3B2A9F0D1")
    private String planNo;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "方案类型")
    private String planType;

    @Schema(description = "方案类型名称")
    private String planTypeName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "编制人用户 ID")
    private Long prepareUserId;

    @Schema(description = "编制人")
    private String prepareUserName;

    @Schema(description = "编制单位")
    private String prepareOrgName;

    @Schema(description = "编制时间")
    private LocalDateTime prepareTime;

    @Schema(description = "方案状态")
    private Integer planStatus;

    @Schema(description = "方案状态名称")
    private String planStatusName;

    @Schema(description = "核心建议目标")
    private String coreTarget;

    @Schema(description = "涉及工程")
    private String projectName;

    @Schema(description = "预期效果分析")
    private String expectedEffect;

    @Schema(description = "编制说明")
    private String prepareDesc;

    @Schema(description = "附件 URL 列表")
    private List<String> attachments;

    @Schema(description = "操作对象数量")
    private Integer objectCount;

    @Schema(description = "被调度指令使用次数")
    private Long usedCount;

    @Schema(description = "操作对象名称列表")
    private List<String> objectNames;

    @Schema(description = "操作对象明细")
    private List<IotDispatchPlanObjectRespVO> objects;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
