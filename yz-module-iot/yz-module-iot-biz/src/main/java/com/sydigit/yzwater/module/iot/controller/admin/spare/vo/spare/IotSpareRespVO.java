package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 备件台账 Response VO")
@Data
public class IotSpareRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "备件名称", example = "闸门轴承")
    private String spareName;

    @Schema(description = "备件规格", example = "M12")
    private String spareSpec;

    @Schema(description = "备件型号", example = "XH-100")
    private String spareModel;

    @Schema(description = "备件类型", example = "electrical")
    private String spareType;

    @Schema(description = "所属站点", example = "station-1")
    private String stationId;

    @Schema(description = "关联设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "生产厂家")
    private String manufacturer;

    @Schema(description = "库存数量", example = "10")
    private Integer stockQty;

    @Schema(description = "最低库存阈值", example = "2")
    private Integer minStock;

    @Schema(description = "存放位置")
    private String storageLocation;

    @Schema(description = "库管员")
    private String keeperName;

    @Schema(description = "备件图片")
    private List<String> spareImages;

    @Schema(description = "是否预警")
    private Boolean warning;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
