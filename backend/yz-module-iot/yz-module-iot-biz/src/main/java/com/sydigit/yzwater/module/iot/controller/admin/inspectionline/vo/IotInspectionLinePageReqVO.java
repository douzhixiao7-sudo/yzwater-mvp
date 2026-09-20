package com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 巡检线路分页 Request VO")
@Data
public class IotInspectionLinePageReqVO extends PageParam {

    @Schema(description = "所属闸站")
    private String stationId;

    @Schema(description = "线路名称")
    private String lineName;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "区域类型")
    private String areaType;

    @Schema(description = "最小点位数")
    private Integer minPointCount;

    @Schema(description = "最大点位数")
    private Integer maxPointCount;

    @Schema(description = "最小使用次数")
    private Long minUseCount;

    @Schema(description = "最大使用次数")
    private Long maxUseCount;

    @Schema(description = "状态（0 启用，1 停用）")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
