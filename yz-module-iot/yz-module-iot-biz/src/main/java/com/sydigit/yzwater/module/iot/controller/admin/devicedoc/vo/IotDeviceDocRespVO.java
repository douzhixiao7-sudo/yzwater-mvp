package com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 设备技术资料 Response VO")
@Data
public class IotDeviceDocRespVO {

    @Schema(description = "主键ID", example = "1024")
    private Long id;

    @Schema(description = "设备ID", example = "1024")
    private Long deviceId;

    @Schema(description = "设备编号")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备型号")
    private String equipmentModel;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "产品分类")
    private String productCategory;

    @Schema(description = "资料类型")
    private String docType;

    @Schema(description = "资料名称")
    private String docName;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件地址")
    private String fileUrl;

    @Schema(description = "资料格式")
    private String fileFormat;

    @Schema(description = "资料备注")
    private String remark;

    @Schema(description = "上传人")
    private String creator;

    @Schema(description = "上传时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人")
    private String updater;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
