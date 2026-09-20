package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 巡检标准分页 Request VO")
@Data
public class IotInspectionStandardPageReqVO extends PageParam {

    @Schema(description = "所属站点（仅设备对象过滤时生效）")
    private String stationId;

    @Schema(description = "标准名称")
    private String standardName;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "适用对象类型（device 或 zd_sslb.value）")
    private String targetType;

    @Schema(description = "状态（0 启用，1 停用）")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "标准ID集合（内部查询使用）", hidden = true)
    private List<Long> standardIds;

}
