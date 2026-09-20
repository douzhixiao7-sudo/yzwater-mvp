package com.sydigit.yzwater.module.dal.mysql.rivers.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史河长分页按设施聚合的分组行
 */
@Data
public class RiverChiefFacilityGroupRow {

    private String referenceType;

    private Long referenceId;

    private LocalDateTime effectiveFrom;
}

