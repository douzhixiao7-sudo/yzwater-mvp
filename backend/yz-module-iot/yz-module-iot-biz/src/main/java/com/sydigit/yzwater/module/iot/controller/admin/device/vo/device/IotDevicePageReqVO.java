package com.sydigit.yzwater.module.iot.controller.admin.device.vo.device;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceStateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备分页 Request VO")
@Data
public class IotDevicePageReqVO extends PageParam {

    @Schema(description = "设备标识（系统内部）", example = "device-001")
    private String deviceName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "设备名称", example = "闸站1#启闭机")
    private String nickname;

    @Schema(description = "设备编码", example = "SB-0001")
    private String serialNumber;

    @Schema(description = "使用状态（在用/维修中/停用）", example = "in_use")
    private String useStatus;

    @Schema(description = "产品编号", example = "26202")
    private Long productId;

    @Schema(description = "设备分类", example = "1")
    private Integer deviceType;

    @Schema(description = "设备状态", example = "1")
    @InEnum(IotDeviceStateEnum.class)
    private Integer status;

    @Schema(description = "设备分组编号", example = "1024")
    private Long groupId;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

}
