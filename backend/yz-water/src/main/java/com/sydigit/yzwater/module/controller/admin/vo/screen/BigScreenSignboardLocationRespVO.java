package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 大屏统计 - 公示牌点位汇总返回
 */
@Data
public class BigScreenSignboardLocationRespVO {

    @Schema(description = "公示牌总数（按筛选条件统计）")
    private Long totalCount;

    @Schema(description = "公示牌点位列表")
    private List<BigScreenSignboardLocationItemRespVO> list;
}

