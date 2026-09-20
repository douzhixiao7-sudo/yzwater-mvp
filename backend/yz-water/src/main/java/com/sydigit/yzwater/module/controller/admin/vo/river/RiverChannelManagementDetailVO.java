package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 河长详情
 */
@Schema(description = "仪征管理后台 - 河长详情")
@Data
public class RiverChannelManagementDetailVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "河段ID")
    private Long riverSectionId;

    @Schema(description = "河段名称")
    private String sectionName;

    @Schema(description = "河长级别")
    private String headLevel;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长电话")
    private String headContact;

    @Schema(description = "河长职责")
    private String responsibilities;

    @Schema(description = "行政区划（可为空）")
    private List<String> administrativeRegion;
}
