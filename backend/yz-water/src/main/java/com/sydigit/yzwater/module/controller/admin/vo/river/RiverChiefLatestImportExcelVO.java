package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 最新河长信息导入 Excel 行
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 最新河长信息导入 Excel 行")
public class RiverChiefLatestImportExcelVO {

    @ExcelProperty(index = 0)
    private String index;

    @ExcelProperty(index = 1)
    private String facilityName;

    @ExcelProperty(index = 2)
    private String townshipName;

    @ExcelProperty(index = 3)
    private String facilityTypeLabel;

    @ExcelProperty(index = 4)
    private String facilityLevelLabel;

    @ExcelProperty(index = 5)
    private String countyHeadName;

    @ExcelProperty(index = 6)
    private String countyHeadPosition;

    @ExcelProperty(index = 7)
    private String townshipHeadName;

    @ExcelProperty(index = 8)
    private String townshipHeadContact;

    @ExcelProperty(index = 9)
    private String townshipHeadPosition;

    @ExcelProperty(index = 10)
    private String villageHead1Name;

    @ExcelProperty(index = 11)
    private String villageHead1Contact;

    @ExcelProperty(index = 12)
    private String villageHead1Position;

    @ExcelProperty(index = 13)
    private String villageHead2Name;

    @ExcelProperty(index = 14)
    private String villageHead2Contact;

    @ExcelProperty(index = 15)
    private String villageHead2Position;

    @ExcelProperty(index = 16)
    private String villageHead3Name;

    @ExcelProperty(index = 17)
    private String villageHead3Contact;

    @ExcelProperty(index = 18)
    private String villageHead3Position;

    @ExcelProperty(index = 19)
    private String villageHead4Name;

    @ExcelProperty(index = 20)
    private String villageHead4Contact;

    @ExcelProperty(index = 21)
    private String villageHead4Position;

    @ExcelProperty(index = 22)
    private String villageHead5Name;

    @ExcelProperty(index = 23)
    private String villageHead5Contact;

    @ExcelProperty(index = 24)
    private String villageHead5Position;

    @ExcelProperty(index = 25)
    private String villageHead6Name;

    @ExcelProperty(index = 26)
    private String villageHead6Contact;

    @ExcelProperty(index = 27)
    private String villageHead6Position;

    @ExcelProperty(index = 28)
    private String villageHead7Name;

    @ExcelProperty(index = 29)
    private String villageHead7Contact;

    @ExcelProperty(index = 30)
    private String villageHead7Position;
}
