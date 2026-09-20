package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 备件台账新增/修改 Request VO")
@Data
public class IotSpareSaveReqVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "备件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "闸门轴承")
    @NotBlank(message = "备件名称不能为空")
    private String spareName;

    @Schema(description = "备件规格", requiredMode = Schema.RequiredMode.REQUIRED, example = "M12")
    @NotBlank(message = "备件规格不能为空")
    private String spareSpec;

    @Schema(description = "备件型号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XH-100")
    @NotBlank(message = "备件型号不能为空")
    private String spareModel;

    @Schema(description = "备件类型", example = "electrical")
    private String spareType;

    @Schema(description = "所属站点", example = "station-1")
    private String stationId;

    @Schema(description = "关联设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "生产厂家", example = "某某厂家")
    private String manufacturer;

    @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量必须大于等于 0")
    private Integer stockQty;

    @Schema(description = "最低库存阈值", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "最低库存阈值不能为空")
    @Min(value = 0, message = "最低库存阈值必须大于等于 0")
    private Integer minStock;

    @Schema(description = "存放位置", example = "库房 A 区")
    private String storageLocation;

    @Schema(description = "库管员", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "库管员不能为空")
    private String keeperName;

    @Schema(description = "备件图片", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "备件图片不能为空")
    private List<String> spareImages;

    @Schema(description = "备注")
    private String remark;

}
