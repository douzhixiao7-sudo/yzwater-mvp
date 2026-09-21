package com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 设备 GENESIS64 点位配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceGenesisPointPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "属性标识符", example = "switch01")
    private String identifier;

    @Schema(description = "属性名称", example = "开关量01")
    private String name;

    @Schema(description = "GENESIS64 点位名称", example = "modbus:3号闸.开入量06")
    private String pointName;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
