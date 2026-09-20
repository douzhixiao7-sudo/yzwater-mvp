package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 风险隐患点保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 风险隐患点保存 Request VO")
public class FxTaskSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "工程名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工程名称不能为空")
    private String name;

    @Schema(description = "险工位置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "险工位置不能为空")
    private String addr;

    @Schema(description = "所属河道 ID")
    private Long riverChannelId;

    @Schema(description = "所属河道名称（只读回显）")
    private String riverChannelName;

    @Schema(description = "防汛等级")
    private String level;

    @Schema(description = "险情描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "险情描述不能为空")
    private String content;

    @Schema(description = "应对措施")
    private String counterMeasures;

    @Schema(description = "附件 URL 数组")
    private List<String> files;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "地图几何 GeoJSON（Point / LineString / MultiLineString）")
    private String geometryGeoJson;
}
