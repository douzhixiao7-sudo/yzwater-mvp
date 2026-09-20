package com.sydigit.yzwater.module.controller.admin.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公示牌分页查询响应
 */
@Schema(description = "仪征管理后台 - 公示牌分页查询响应")
@Data
public class SignboardPageRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公示牌代码")
    private String signboardCode;

    @Schema(description = "公示牌名称")
    private String signboardName;

    @Schema(description = "公示牌等级(字典: zd_hljb)")
    private String signboardLevel;

    @Schema(description = "公示牌等级名称")
    private String signboardLevelLabel;

    @Schema(description = "是否大屏展示（1=展示，0=不展示）")
    private Integer isScreenDisplay;

    @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "关联河道名称")
    private String riverChannelName;

    @Schema(description = "关联河段名称")
    private String riverSectionName;

    @Schema(description = "关联河道/河段名称（优先展示河道）")
    private String riverName;

    @Schema(description = "关联水库名称")
    private String waterReservoirName;

    @Schema(description = "具体位置")
    private String specificLocation;

    @Schema(description = "行政区划")
    private String adminRegion;

    @Schema(description = "责任单位")
    private String maintenanceUnit;

    @Schema(description = "责任人")
    private String responsiblePerson;

    @Schema(description = "管理单位")
    private String managementUnit;

    @Schema(description = "权属单位")
    private String ownershipUnit;
}
