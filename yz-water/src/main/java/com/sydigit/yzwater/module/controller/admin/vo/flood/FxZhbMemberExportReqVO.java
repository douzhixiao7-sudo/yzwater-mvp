package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛指挥部成员导出查询入参
 */
@Data
@Schema(description = "管理后台 - 防汛指挥部成员导出查询 Request VO")
public class FxZhbMemberExportReqVO {

    @Schema(description = "指挥部ID")
    private String commandDepartmentId;

    @Schema(description = "姓名（模糊查询）")
    private String name;

    @Schema(description = "职务（模糊查询）")
    private String title;
}
