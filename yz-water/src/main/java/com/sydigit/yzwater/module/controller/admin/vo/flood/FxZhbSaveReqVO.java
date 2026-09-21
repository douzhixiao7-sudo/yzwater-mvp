package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 防汛指挥部新增/编辑入参
 */
@Data
@Schema(description = "管理后台 - 防汛指挥部新增/编辑 Request VO")
public class FxZhbSaveReqVO {

    @Schema(description = "主键（编辑时必填）")
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "指挥部名称不能为空")
    private String name;

    @Schema(description = "地址")
    private String addr;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "传真")
    private String fax;

    @Schema(description = "区划代码")
    private String areaCode;

    @Schema(description = "邮政编码")
    private String zipCode;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "成员列表")
    @Valid
    private List<FxZhbMemberSaveReqVO> members;
}
