package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 水库河长保存项
 */
@Schema(description = "仪征管理后台 - 水库河长保存项")
@Data
public class ReservoirHeadItemSaveReqVO {

    @Schema(description = "河长级别(字典: zd_hzjb)")
    private String headLevel;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "河长工作单位")
    private String headUnit;

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长电话")
    private String headContact;

    @Schema(description = "河长职责")
    private String responsibilities;

    @Schema(description = "行政区划（可为空，多选）")
    private List<String> administrativeRegion;

    @Schema(description = "备注")
    private String remarks;
}
