package com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.sydigit.yzwater.framework.excel.core.annotations.DictFormat;
import com.sydigit.yzwater.framework.excel.core.convert.DictConvert;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 设备事故 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotDeviceAccidentExportExcelVO {

    @ExcelProperty("设备ID")
    private Long deviceId;

    @ExcelProperty("事故发生时间")
    private LocalDateTime accidentTime;

    @ExcelProperty("事故地点")
    private String accidentLocation;

    @ExcelProperty(value = "事故类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.DEVICE_ACCIDENT_TYPE)
    private String accidentType;

    @ExcelProperty("事故描述")
    private String accidentDesc;

    @ExcelProperty("处理结果")
    private String handleResult;

    @ExcelProperty("损失评估")
    private String lossAssessment;

    @ExcelProperty("责任人")
    private String responsibleName;

    @ExcelProperty("附件")
    private String attachments;

    @ExcelProperty("登记人")
    private String creator;

    @ExcelProperty("登记时间")
    private LocalDateTime createTime;

    @ExcelProperty("修改时间")
    private LocalDateTime updateTime;
}