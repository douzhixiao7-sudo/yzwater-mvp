package com.sydigit.yzwater.module.dal.mysql.problem.dto;

import lombok.Data;

/**
 * 问题状态统计行
 */
@Data
public class ProblemFeedbackStatusCountRow {

    private Integer status;

    private Long count;
}
