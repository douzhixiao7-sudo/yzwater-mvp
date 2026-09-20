package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 幸福河湖开放接口通用数据源入参
 *
 * 对接文档路径中的首段资源编号改为显式入参传递。
 */
@Data
@Schema(description = "幸福河湖开放接口通用数据源入参")
public class XfhhOpenDataIdReqVO {

    @Schema(description = "外部平台数据源编号", example = "fb8cd0fe-addf-4364-82a1-8a0b11a9440c")
    private String id;
}
