package com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 设备技术资料分页 Request VO")
@Data
public class IotDeviceDocPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1024")
    private Long deviceId;

    @Schema(description = "设备编号", example = "DEV-0001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "闸门电机")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private String deviceType;

    @Schema(description = "资料类型", example = "manual")
    private String docType;

    @Schema(description = "资料名称", example = "操作手册")
    private String docName;

    @Schema(description = "资料格式", example = "pdf")
    private String fileFormat;

    @Schema(description = "上传时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
