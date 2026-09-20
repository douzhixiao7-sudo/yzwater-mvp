package com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 巡检线路 Response VO")
@Data
public class IotInspectionLineRespVO {

    @Schema(description = "线路 ID")
    private Long id;

    @Schema(description = "所属闸站")
    private String stationId;

    @Schema(description = "线路名称")
    private String lineName;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "区域类型")
    private String areaType;

    @Schema(description = "线路描述")
    private String lineDesc;

    @Schema(description = "起点经度")
    private BigDecimal startLongitude;

    @Schema(description = "起点纬度")
    private BigDecimal startLatitude;

    @Schema(description = "终点经度")
    private BigDecimal endLongitude;

    @Schema(description = "终点纬度")
    private BigDecimal endLatitude;

    @Schema(description = "线路总长度（米）")
    private BigDecimal totalLengthMeter;

    @Schema(description = "点位数量")
    private Integer pointCount;

    @Schema(description = "使用次数")
    private Long useCount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "点位列表")
    private List<IotInspectionLinePointRespVO> points;

}
