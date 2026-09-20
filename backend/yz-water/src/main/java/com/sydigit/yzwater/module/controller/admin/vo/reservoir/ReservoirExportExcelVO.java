package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 水库导出 Excel VO
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 水库导出 Excel VO")
public class ReservoirExportExcelVO {

    @ExcelProperty("水库编码")
    private String reservoirCode;

    @ExcelProperty("水库名称")
    private String reservoirName;

    @ExcelProperty("规模")
    private String reservoirScaleLabel;

    @ExcelProperty("管理单位")
    private String managementUnitLabel;

    @ExcelProperty("水库性质")
    private String reservoirNatureLabel;

    @ExcelProperty("总库容(m3)")
    private BigDecimal totalCapacity;

    @ExcelProperty("兴利库容(m3)")
    private BigDecimal activeCapacity;

    @ExcelProperty("死水位(m)")
    private BigDecimal deadLevel;

    @ExcelProperty("经度")
    private BigDecimal longitude;

    @ExcelProperty("纬度")
    private BigDecimal latitude;

    @ExcelProperty("所在乡镇")
    private String townshipName;

    @ExcelProperty("集水面积(km²)")
    private BigDecimal catchmentArea;

    @ExcelProperty("兴利水位(m)")
    private BigDecimal normalOperatingLevel;

    @ExcelProperty("汛限水位(m)")
    private BigDecimal floodLimitLevel;

    @ExcelProperty("设计水位(m)")
    private BigDecimal designFloodLevel;

    @ExcelProperty("校核水位(m)")
    private BigDecimal verifiedFloodLevel;

    @ExcelProperty("坝顶高程(m)")
    private String damCrestElevation;

    @ExcelProperty("最大坝高(m)")
    private BigDecimal maxDamHeight;

    @ExcelProperty("坝顶长度(m)")
    private String damTopLength;
}
