package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 大屏统计 - 河道列表查询返回
 */
@Data
public class BigScreenRiverChannelWithSectionsListRespVO {

    @Schema(description = "河道总数（按筛选条件统计）")
    private Long totalCount;

    @Schema(description = "河道列表")
    private List<BigScreenRiverChannelWithSectionsRespVO> list;
}
