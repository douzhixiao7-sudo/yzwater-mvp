package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛指挥部分页出参
 */
@Data
@Schema(description = "管理后台 - 防汛指挥部分页 Response VO")
public class FxZhbPageRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
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
}
