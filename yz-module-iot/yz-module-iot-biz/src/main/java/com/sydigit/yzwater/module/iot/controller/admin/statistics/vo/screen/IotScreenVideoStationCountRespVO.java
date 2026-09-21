package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 大屏站点视频监控数量 Response VO")
@Data
public class IotScreenVideoStationCountRespVO {

    @Schema(description = "视频区域编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "root00000000")
    private String regionIndexCode;

    @Schema(description = "站点名称（来自视频区域名称）", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号泵站")
    private String stationName;

    @Schema(description = "监控设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long deviceCount;

    @Schema(description = "在线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Long onlineCount;

    @Schema(description = "离线设备数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long offlineCount;

    @Schema(description = "本站点设备总数占比（单位：%）", requiredMode = Schema.RequiredMode.REQUIRED, example = "25.00")
    private BigDecimal totalRatio;
}
