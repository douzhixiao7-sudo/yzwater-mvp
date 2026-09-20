package com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 设备技术资料新增/修改 Request VO")
@Data
public class IotDeviceDocSaveReqVO {

    @Schema(description = "主键ID", example = "1024")
    private Long id;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "关联设备不能为空")
    private Long deviceId;

    @Schema(description = "资料类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "manual")
    @NotBlank(message = "资料类型不能为空")
    private String docType;

    @Schema(description = "资料名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "操作手册")
    @NotBlank(message = "资料名称不能为空")
    private String docName;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "资料文件不能为空")
    private String fileUrl;

    @Schema(description = "文件格式", example = "pdf")
    private String fileFormat;

    @Schema(description = "文件ID", example = "1024")
    private Long fileId;

    @Schema(description = "资料备注", example = "年度检修资料")
    private String remark;

    @Schema(description = "上传时间", example = "2026-02-04 12:02:01")
    @NotNull(message = "上传时间不能为空")
    private LocalDateTime createTime;
}
