package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 备件台账分页 Request VO")
@Data
public class IotSparePageReqVO extends PageParam {

    @Schema(description = "备件名称", example = "闸门轴承")
    private String spareName;

    @Schema(description = "备件规格", example = "M12")
    private String spareSpec;

    @Schema(description = "备件型号", example = "XH-100")
    private String spareModel;

    @Schema(description = "备件类型", example = "electrical")
    private String spareType;

    @Schema(description = "设备类型", example = "1")
    private String deviceType;

    @Schema(description = "关联设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "存放位置", example = "库房 A 区")
    private String storageLocation;

    @Schema(description = "库管员", example = "张三")
    private String keeperName;

    @Schema(description = "是否预警")
    private Boolean warning;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
