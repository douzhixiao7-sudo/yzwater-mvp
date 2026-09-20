package com.sydigit.yzwater.module.controller.admin.vo.gis;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "仪征管理后台 - 缓冲区查询创建请求")
@Data
public class GisBufferQueryCreateReqVO {

    @Schema(description = "中心点经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "中心点经度不能为空")
    private BigDecimal centerLongitude;

    @Schema(description = "中心点纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "中心点纬度不能为空")
    private BigDecimal centerLatitude;

    @Schema(description = "缓冲区半径(米)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "缓冲区半径不能为空")
    @DecimalMin(value = "0.01", message = "缓冲区半径必须大于0")
    private BigDecimal radiusMeters;

    @Schema(description = "设施类型列表(zd_sslb)")
    private List<String> facilityTypes;

    @Schema(description = "是否全选")
    private Boolean selectAll;
}
