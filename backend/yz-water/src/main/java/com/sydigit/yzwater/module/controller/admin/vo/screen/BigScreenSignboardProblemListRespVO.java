package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 大屏统计 - 公示牌扫码问题列表返回
 */
@Data
public class BigScreenSignboardProblemListRespVO {

    @Schema(description = "问题总数（按筛选条件统计）")
    private Long totalCount;

    @Schema(description = "问题列表")
    private List<BigScreenSignboardProblemItemRespVO> list;
}

