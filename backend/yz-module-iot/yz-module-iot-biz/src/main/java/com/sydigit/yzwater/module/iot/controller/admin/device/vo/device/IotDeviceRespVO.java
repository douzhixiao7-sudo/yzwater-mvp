package com.sydigit.yzwater.module.iot.controller.admin.device.vo.device;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.sydigit.yzwater.framework.excel.core.annotations.DictFormat;
import com.sydigit.yzwater.framework.excel.core.convert.DictConvert;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static com.sydigit.yzwater.module.iot.enums.DictTypeConstants.DEVICE_SITE;
import static com.sydigit.yzwater.module.iot.enums.DictTypeConstants.DEVICE_STATE;

@Schema(description = "管理后台 - IoT 设备 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotDeviceRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    @ExcelProperty("设备编号")
    private Long id;

    @Schema(description = "设备标识（系统内部）", requiredMode = Schema.RequiredMode.REQUIRED, example = "device-001")
    @ExcelProperty("设备标识")
    private String deviceName;

    @Schema(description = "所属站点", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "所属站点", converter = DictConvert.class)
    @DictFormat(DEVICE_SITE)
    private String stationId;

    @Schema(description = "设备名称", example = "闸站1#启闭机")
    @ExcelProperty("设备名称")
    private String nickname;

    @Schema(description = "设备编码", example = "SB-0001")
    @ExcelProperty("设备编码")
    private String serialNumber;

    @Schema(description = "设备图片（多张）", example = "[\"https://iocoder.cn/1.png\"]")
    private String[] picUrl;

    @Schema(description = "设备型号", example = "XH-100")
    @ExcelProperty("设备型号")
    private String equipmentModel;

    @Schema(description = "生产厂家", example = "某某厂家")
    @ExcelProperty("生产厂家")
    private String manufacturer;

    @Schema(description = "安装日期", example = "2025-01-01")
    @ExcelProperty("安装日期")
    private LocalDate installDate;

    @Schema(description = "使用状态（在用/维修中/停用）", example = "in_use")
    @ExcelProperty(value = "使用状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.DEVICE_USE_STATUS)
    private String useStatus;

    @Schema(description = "上次养护时间", example = "2025-01-20 10:00:00")
    @ExcelProperty("上次养护时间")
    private LocalDateTime lastMaintainTime;

    @Schema(description = "养护周期（天）", example = "30")
    @ExcelProperty("养护周期（天）")
    private Integer maintainCycleDays;

    @Schema(description = "负责人用户ID", example = "100")
    private Long ownerUserId;

    @Schema(description = "负责人姓名", example = "张三")
    @ExcelProperty("负责人")
    private String ownerName;

    @Schema(description = "负责人联系方式", example = "13800000000")
    @ExcelProperty("联系方式")
    private String ownerPhone;

    @Schema(description = "所属部门ID", example = "10")
    private Long deptId;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "设备二维码")
    private String qrCode;

    @Schema(description = "设备分组编号数组", example = "1,2")
    private Set<Long> groupIds;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26202")
    @ExcelProperty("产品编号")
    private Long productId;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品 Key")
    private String productKey;

    @Schema(description = "设备分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("设备类型")
    private Integer deviceType;

    @Schema(description = "网关设备 ID", example = "16380")
    private Long gatewayId;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "设备状态", converter = DictConvert.class)
    @DictFormat(DEVICE_STATE)
    private Integer state;

    @Schema(description = "最后上线时间")
    @ExcelProperty("最后上线时间")
    private LocalDateTime onlineTime;

    @Schema(description = "最后离线时间")
    @ExcelProperty("最后离线时间")
    private LocalDateTime offlineTime;

    @Schema(description = "设备激活时间")
    @ExcelProperty("设备激活时间")
    private LocalDateTime activeTime;

    @Schema(description = "设备密钥，用于设备认证")
    @ExcelProperty("设备密钥")
    private String deviceSecret;

    @Schema(description = "认证类型（如一机一密、动态注册）", example = "2")
    @ExcelProperty("认证类型（如一机一密、动态注册）")
    private String authType;

    @Schema(description = "设备配置", example = "{\"abc\": \"efg\"}")
    private String config;

    @Schema(description = "定位方式", example = "2")
    @ExcelProperty(value = "定位方式", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.LOCATION_TYPE)
    private Integer locationType;

    @Schema(description = "设备位置的纬度", example = "45.000000")
    private BigDecimal latitude;

    @Schema(description = "设备位置的经度", example = "45.000000")
    private BigDecimal longitude;

    @Schema(description = "设备位置", example = "闸站管理房北侧")
    @ExcelProperty("设备位置")
    private String address;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
