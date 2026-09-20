package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - IoT 大屏坑塘分页 Response VO")
@Data
public class IotScreenPondPageRespVO {

    @Schema(description = "坑塘列表")
    private List<Map<String, Object>> list;

    @Schema(description = "符合条件的总数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer pageNo;

    @Schema(description = "每页条数")
    private Integer pageSize;

    @Schema(description = "是否还有下一页")
    private Boolean hasMore;

}
