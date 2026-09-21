package com.sydigit.yzwater.module.system.service.area.dto;

import lombok.Data;

import java.util.List;

/**
 * 行政区划树节点
 */
@Data
public class SystemAreaNode {

    private Long id;

    private String name;

    /**
     * 行政区划级别（与 system_area.type 一致）
     */
    private Integer type;

    private Integer sort;

    private List<SystemAreaNode> children;
}
