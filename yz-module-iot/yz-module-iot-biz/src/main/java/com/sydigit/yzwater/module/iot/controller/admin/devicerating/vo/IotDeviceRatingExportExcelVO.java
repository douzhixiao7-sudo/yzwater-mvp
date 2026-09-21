package com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.sydigit.yzwater.framework.excel.core.annotations.DictFormat;
import com.sydigit.yzwater.framework.excel.core.convert.DictConvert;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "IoT - 设备评级 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotDeviceRatingExportExcelVO {

    @ExcelProperty("设备 ID")
    private Long deviceId;

    @ExcelProperty("评级时间")
    private LocalDate ratingTime;

    @ExcelProperty("评级人")
    private String ratingUserName;

    @ExcelProperty("评级依据")
    private String ratingBasis;

    @ExcelProperty(value = "评级结果", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.DEVICE_RATING_RESULT)
    private String ratingResult;

    @ExcelProperty("整改建议")
    private String rectifyAdvice;

    @ExcelProperty("整改期限")
    private LocalDate rectifyDeadline;

    @ExcelProperty("附件")
    private String attachments;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
