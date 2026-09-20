package com.sydigit.yzwater.module.controller.app.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手机端 - 扫码查询公示牌信息返回（不含图片）
 */
@Schema(description = "手机端 - 扫码查询公示牌信息返回（不含图片）")
@Data
public class AppSignboardInfoRespVO {

    @Schema(description = "公示牌ID")
    private Long signboardId;

    @Schema(description = "公示牌二维码标识")
    private String qrCode;

    @Schema(description = "公示牌代码")
    private String signboardCode;

    @Schema(description = "公示牌名称")
    private String signboardName;

    @Schema(description = "公示牌类型（字典label）")
    private String signboardTypeLabel;

    @Schema(description = "所在河道名称")
    private String riverChannelName;

    @Schema(description = "所在河段名称")
    private String riverSectionName;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "具体位置")
    private String specificLocation;

    @Schema(description = "行政区划")
    private String adminRegion;

    @Schema(description = "维护单位（字典label列表）")
    private List<String> maintenanceUnitLabels;

    @Schema(description = "责任人")
    private String responsiblePerson;

    @Schema(description = "管理单位（字典label列表）")
    private List<String> managementUnitLabels;

    @Schema(description = "权属单位（字典label列表）")
    private List<String> ownershipUnitLabels;

    @Schema(description = "公示牌内容")
    private String content;

    @Schema(description = "备注")
    private String remarks;
}

