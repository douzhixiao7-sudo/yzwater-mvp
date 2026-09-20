package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏统计-视频监控 设备统计汇总响应
 */
@Data
@Schema(description = "大屏统计-视频监控 设备统计汇总响应")
public class BigScreenVideoDeviceSummaryRespVO {

    @Schema(description = "设备总数")
    private Integer deviceTotal;

    @Schema(description = "在线总数")
    private Integer onlineTotal;

    @Schema(description = "离线总数")
    private Integer offlineTotal;

    @Schema(description = "实时设备在线率，单位%")
    private BigDecimal realTimeDeviceOnlineRate;
}
