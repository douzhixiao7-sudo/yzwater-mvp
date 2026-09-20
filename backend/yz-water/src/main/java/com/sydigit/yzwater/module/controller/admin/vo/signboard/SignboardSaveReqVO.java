package com.sydigit.yzwater.module.controller.admin.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 公示牌新增/编辑请求
 */
@Schema(description = "仪征管理后台 - 公示牌新增/编辑请求")
@Data
public class SignboardSaveReqVO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "公示牌代码")
    @NotBlank(message = "公示牌代码不能为空")
    private String signboardCode;

    @Schema(description = "公示牌名称")
    @NotBlank(message = "公示牌名称不能为空")
    private String signboardName;

    @Schema(description = "公示牌类型(字典: zd_gsplx)")
    private String signboardType;

    @Schema(description = "公示牌等级(字典: zd_hljb)")
    private String signboardLevel;

    @Schema(description = "是否大屏展示（1=展示，0=不展示）")
    private Integer isScreenDisplay;

    @Schema(description = "关联河道ID（可选）")
    private Long riverChannelId;

    @Schema(description = "关联河段ID（可选）")
    private Long riverSectionId;

    @Schema(description = "关联水库ID（可选）")
    private Long waterReservoirId;

    @Schema(description = "关联水库名称（详情回显用）")
    private String waterReservoirName;

    @Schema(description = "二维码标识字段")
    private String qrCode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "具体位置")
    private String specificLocation;

    @Schema(description = "行政区划")
    private String adminRegion;

    @Schema(description = "维护单位（字典：zd_whdw），支持多选")
    private List<String> maintenanceUnit;

    @Schema(description = "责任人")
    private String responsiblePerson;

    @Schema(description = "管理单位（字典：zd_gldw），支持多选")
    private List<String> managementUnit;

    @Schema(description = "权属单位（字典：zd_qsdw），支持多选")
    private List<String> ownershipUnit;

    @Schema(description = "公示牌图片URL列表（最多 5 张）")
    private List<String> signboardImages;

    @Schema(description = "公示牌内容")
    private String content;

    @Schema(description = "备注")
    private String remarks;
}
