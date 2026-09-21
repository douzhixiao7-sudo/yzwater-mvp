package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛指挥部成员分页出参
 */
@Data
@Schema(description = "管理后台 - 防汛指挥部成员分页 Response VO")
public class FxZhbMemberPageRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "指挥部ID")
    private String commandDepartmentId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "职务")
    private String title;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "排序号")
    private Integer sort;
}
