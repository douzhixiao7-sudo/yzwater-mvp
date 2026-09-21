package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 备件出入库分页 Request VO")
@Data
public class IotSpareIoPageReqVO extends PageParam {

    @Schema(description = "备件 ID", example = "1024")
    private Long spareId;

    @Schema(description = "出入库类型", example = "IN")
    private String ioType;

    @Schema(description = "审批状态", example = "pending")
    private String auditStatus;

    @Schema(description = "出库用途类型", example = "fault")
    private String usageType;

    @Schema(description = "设备类型", example = "1")
    private String deviceType;

    @Schema(description = "关联设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "操作人", example = "张三")
    private String operatorName;

    @Schema(description = "出入库时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] ioTime;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
