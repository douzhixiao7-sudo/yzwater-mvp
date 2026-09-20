package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare;

import com.sydigit.yzwater.framework.excel.core.annotations.DictFormat;
import com.sydigit.yzwater.framework.excel.core.convert.DictConvert;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 备件台账 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotSpareExportExcelVO {

    @ExcelProperty("备件名称")
    private String spareName;

    @ExcelProperty("备件规格")
    private String spareSpec;

    @ExcelProperty("备件型号")
    private String spareModel;

    @ExcelProperty(value = "备件类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SPARE_TYPE)
    private String spareType;

    @ExcelProperty("生产厂家")
    private String manufacturer;

    @ExcelProperty("库存数量")
    private Integer stockQty;

    @ExcelProperty("最低库存阈值")
    private Integer minStock;

    @ExcelProperty("存放位置")
    private String storageLocation;

    @ExcelProperty("库管员")
    private String keeperName;

    @ExcelProperty("备件图片")
    private String spareImages;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
