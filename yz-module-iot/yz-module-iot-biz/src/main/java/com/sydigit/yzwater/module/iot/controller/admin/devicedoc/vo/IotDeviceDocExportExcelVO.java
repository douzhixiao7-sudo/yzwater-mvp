package com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.sydigit.yzwater.framework.excel.core.annotations.DictFormat;
import com.sydigit.yzwater.framework.excel.core.convert.DictConvert;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 设备技术资料 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotDeviceDocExportExcelVO {

    @ExcelProperty("设备编号")
    private String deviceCode;

    @ExcelProperty("设备名称")
    private String deviceName;

    @ExcelProperty("设备型号")
    private String equipmentModel;

    @ExcelProperty(value = "设备类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.DEVICE_TYPE)
    private String deviceType;

    @ExcelProperty(value = "资料类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.DEVICE_DOC_TYPE)
    private String docType;

    @ExcelProperty("资料名称")
    private String docName;

    @ExcelProperty("资料格式")
    private String fileFormat;

    @ExcelProperty("上传人")
    private String creator;

    @ExcelProperty("上传时间")
    private LocalDateTime createTime;

    @ExcelProperty("资料备注")
    private String remark;
}
