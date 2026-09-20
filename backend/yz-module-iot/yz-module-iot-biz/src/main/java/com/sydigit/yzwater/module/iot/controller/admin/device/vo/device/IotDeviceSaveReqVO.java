package com.sydigit.yzwater.module.iot.controller.admin.device.vo.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.product.IotLocationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "管理后台 - IoT 设备新增/修改 Request VO")
@Data
public class IotDeviceSaveReqVO {

    @Schema(description = "设备编号", example = "177")
    private Long id;

    @Schema(description = "设备标识（系统内部）", requiredMode = Schema.RequiredMode.AUTO, example = "device-001")
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @Schema(description = "所属站点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "设备名称", example = "闸站1#启闭机")
    private String nickname;

    @Schema(description = "设备编码", example = "SB-0001")
    private String serialNumber;

    @Schema(description = "设备图片（多张）", example = "[\"https://iocoder.cn/1.png\"]")
    private String[] picUrl;

    @Schema(description = "设备型号", example = "XH-100")
    private String equipmentModel;

    @Schema(description = "生产厂家", example = "某某厂家")
    private String manufacturer;

    @Schema(description = "安装日期", example = "2025-01-01")
    private LocalDate installDate;

    @Schema(description = "使用状态（在用/维修中/停用）", example = "in_use")
    @NotBlank(message = "使用状态不能为空")
    private String useStatus;

    @Schema(description = "上次养护时间", example = "2025-01-20 10:00:00")
    private LocalDateTime lastMaintainTime;

    @Schema(description = "养护周期（天）", example = "30")
    private Integer maintainCycleDays;

    @Schema(description = "负责人用户ID", example = "100")
    private Long ownerUserId;

    @Schema(description = "负责人姓名", example = "张三")
    private String ownerName;

    @Schema(description = "负责人联系方式", example = "13800000000")
    private String ownerPhone;

    @Schema(description = "所属部门ID", example = "10")
    private Long deptId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "设备二维码")
    @JsonIgnore
    private String qrCode;

    @Schema(description = "设备分组编号数组", example = "1,2")
    private Set<Long> groupIds;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    @NotNull(message = "产品不能为空")
    private Long productId;

    @Schema(description = "设备分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备分类不能为空")
    private Integer deviceType;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备配置", example = "{\"abc\": \"efg\"}")
    private String config;

    @Schema(description = "定位类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @InEnum(value = IotLocationTypeEnum.class, message = "定位方式必须是 {value}")
    private Integer locationType;

    @Schema(description = "设备位置的纬度", example = "16380")
    private BigDecimal latitude;

    @Schema(description = "设备位置的经度", example = "16380")
    private BigDecimal longitude;

    @Schema(description = "设备位置", example = "闸站管理房北侧")
    private String address;

}
