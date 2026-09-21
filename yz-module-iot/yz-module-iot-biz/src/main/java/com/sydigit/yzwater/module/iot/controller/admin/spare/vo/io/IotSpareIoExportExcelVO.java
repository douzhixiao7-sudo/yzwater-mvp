package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io;

import com.sydigit.yzwater.framework.excel.core.annotations.DictFormat;
import com.sydigit.yzwater.framework.excel.core.convert.DictConvert;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 备件出入库 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class IotSpareIoExportExcelVO {

    @ExcelProperty("备件名称")
    private String spareName;

    @ExcelProperty("备件规格")
    private String spareSpec;

    @ExcelProperty("备件型号")
    private String spareModel;

    @ExcelProperty(value = "出入库类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SPARE_IO_TYPE)
    private String ioType;

    @ExcelProperty("出入库时间")
    private LocalDateTime ioTime;

    @ExcelProperty("出入库数量")
    private Integer ioQty;

    @ExcelProperty(value = "出库用途类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SPARE_USAGE_TYPE)
    private String usageType;

    @ExcelProperty("用途关联 ID")
    private Long usageId;

    @ExcelProperty("操作人")
    private String operatorName;

    @ExcelProperty(value = "审批状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SPARE_IO_AUDIT_STATUS)
    private String auditStatus;

    @ExcelProperty("审批人")
    private String auditUserName;

    @ExcelProperty("审批时间")
    private LocalDateTime auditTime;

    @ExcelProperty("审批备注")
    private String auditRemark;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
