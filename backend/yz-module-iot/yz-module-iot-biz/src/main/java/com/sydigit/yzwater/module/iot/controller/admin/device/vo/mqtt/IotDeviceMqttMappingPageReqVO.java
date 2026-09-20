package com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 设备 MQTT 属性映射分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceMqttMappingPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "属性标识符", example = "rise")
    private String identifier;

    @Schema(description = "属性名称", example = "上升信号")
    private String name;

    @Schema(description = "报文字段名", example = "ZMQBJ3_rise")
    private String payloadKey;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
