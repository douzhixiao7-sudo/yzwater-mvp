package com.sydigit.yzwater.module.iot.core.biz.dto;

import lombok.Data;

/**
 * IoT GENESIS64 点位响应 DTO
 */
@Data
public class IotGenesisPointRespDTO {

    private Long id;

    private String identifier;

    private String name;

    private String pointName;

    private Integer sort;

}
