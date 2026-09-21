package com.sydigit.yzwater.module.iot.controller.admin.product.vo.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * IoT 产品 Excel 导入结果 VO
 */
@Data
public class IotProductImportRespVO {

    @Schema(description = "总行数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalCount;

    @Schema(description = "成功行数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer successCount;

    @Schema(description = "失败行数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer failureCount;

    @Schema(description = "创建的产品名称列表")
    private List<String> createdProductNames;

    @Schema(description = "创建的设备名称列表")
    private List<String> createdDeviceNames;

    @Schema(description = "创建的物模型标识符列表")
    private List<String> createdThingModelIdentifiers;

    @Schema(description = "失败行号与原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<Integer, String> failureMessages;

}