package com.sydigit.yzwater.module.dal.mysql.problem.dto;

import lombok.Data;

/**
 * 问题反馈类型统计行
 */
@Data
public class ProblemFeedbackTypeCountRow {

    private String feedbackType;

    private Long count;
}
