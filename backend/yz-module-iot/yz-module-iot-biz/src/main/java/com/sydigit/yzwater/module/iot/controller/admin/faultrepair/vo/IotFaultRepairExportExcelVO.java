package com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 故障维修工单 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotFaultRepairExportExcelVO {

    @ExcelProperty("工单编号")
    private String orderNo;

    @ExcelProperty("设备名称")
    private String deviceName;

    @ExcelProperty("设备类型")
    private String deviceType;

    @ExcelProperty("故障类型")
    private String faultType;

    @ExcelProperty("故障时间")
    private LocalDateTime faultTime;

    @ExcelProperty("故障现象")
    private String faultSymptom;

    @ExcelProperty("上报人")
    private String reporterName;

    @ExcelProperty("维修人")
    private String repairName;

    @ExcelProperty("处理状态")
    private String status;

    @ExcelProperty("完成时间")
    private LocalDateTime finishTime;

    @ExcelProperty("计划完成时间")
    private LocalDateTime planFinishTime;

    @ExcelProperty("故障图片")
    private String faultImages;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
