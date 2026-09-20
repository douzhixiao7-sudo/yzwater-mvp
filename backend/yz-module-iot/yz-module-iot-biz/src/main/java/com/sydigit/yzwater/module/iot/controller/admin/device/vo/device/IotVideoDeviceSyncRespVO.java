package com.sydigit.yzwater.module.iot.controller.admin.device.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 视频摄像头同步到 IoT 设备结果
 */
@Data
public class IotVideoDeviceSyncRespVO {

    @Schema(description = "视频区域远端总数")
    private Integer regionRemoteCount;

    @Schema(description = "视频区域新增数量")
    private Integer regionInsertedCount;

    @Schema(description = "视频区域更新数量")
    private Integer regionUpdatedCount;

    @Schema(description = "视频区域删除数量")
    private Integer regionDeletedCount;

    @Schema(description = "视频摄像头远端总数")
    private Integer cameraRemoteCount;

    @Schema(description = "视频摄像头新增数量")
    private Integer cameraInsertedCount;

    @Schema(description = "视频摄像头更新数量")
    private Integer cameraUpdatedCount;

    @Schema(description = "视频摄像头删除数量")
    private Integer cameraDeletedCount;

    @Schema(description = "本次应同步到 IoT 的目标设备数量")
    private Integer targetCount;

    @Schema(description = "本次跳过数量（区域未映射或关键字段缺失）")
    private Integer skippedCount;

    @Schema(description = "IoT 设备新增数量")
    private Integer insertedCount;

    @Schema(description = "IoT 设备更新数量")
    private Integer updatedCount;

    @Schema(description = "IoT 设备删除数量")
    private Integer deletedCount;
}

