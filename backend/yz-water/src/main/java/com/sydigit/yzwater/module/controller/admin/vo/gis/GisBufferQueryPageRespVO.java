package com.sydigit.yzwater.module.controller.admin.vo.gis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "仪征管理后台 - 缓冲区查询分页返回")
@Data
public class GisBufferQueryPageRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "缓冲区半径(米)")
    private BigDecimal radiusMeters;

    @Schema(description = "缓冲区面积(平方米)")
    private BigDecimal bufferAreaM2;

    @Schema(description = "设施数量")
    private Integer facilityCount;

    @Schema(description = "统计时间")
    private LocalDateTime statsTime;

    @Schema(description = "涉及行政区名称列表")
    private List<String> adminAreaNames;

    @Schema(description = "设施类型列表(zd_sslb)")
    private List<String> facilityTypes;
}
