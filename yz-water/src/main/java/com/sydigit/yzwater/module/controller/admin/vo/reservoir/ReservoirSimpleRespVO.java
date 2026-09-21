package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 水库下拉简要信息
 */
@Schema(description = "仪征管理后台 - 水库简要信息")
@Data
public class ReservoirSimpleRespVO {

    @Schema(description = "水库ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "水库名称")
    private String reservoirName;
}
