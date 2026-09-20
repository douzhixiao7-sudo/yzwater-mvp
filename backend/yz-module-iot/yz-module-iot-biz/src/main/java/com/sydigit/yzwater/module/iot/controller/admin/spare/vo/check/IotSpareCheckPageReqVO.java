package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 备件盘点分页 Request VO")
@Data
public class IotSpareCheckPageReqVO extends PageParam {

    @Schema(description = "备件 ID", example = "1024")
    private Long spareId;

    @Schema(description = "盘点结果", example = "normal")
    private String resultStatus;

    @Schema(description = "是否已反馈")
    private Boolean applied;

    @Schema(description = "盘点时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] checkTime;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
