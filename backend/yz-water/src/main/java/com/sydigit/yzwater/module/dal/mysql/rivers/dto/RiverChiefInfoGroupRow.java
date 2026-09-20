package com.sydigit.yzwater.module.dal.mysql.rivers.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 河长信息分页分组行：按「规范化姓名 + 河长级别 + 规范化职务」聚合（总河长不参与；不按 effective_from 拆分）
 */
@Data
public class RiverChiefInfoGroupRow {

    /**
     * 分组代表记录 ID（用于详情/编辑/删除入口）
     */
    private Long id;

    private String headName;

    private String headLevel;

    private String headPosition;

    private String headUnit;

    private String headContact;

    private String[] administrativeRegion;

    private LocalDateTime effectiveFrom;
}

