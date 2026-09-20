package com.sydigit.yzwater.module.controller.admin.vo.river;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河长2 Excel 导入行
 */
@Data
@ExcelIgnoreUnannotated
@Schema(description = "仪征管理后台 - 河长2 Excel 导入行")
public class RiverChiefLatestV2ImportExcelVO {

    @ExcelProperty(index = 0)
    private String index;

    @ExcelProperty(index = 1)
    private String facilityName;

    @ExcelProperty(index = 2)
    private String sectionName;

    @ExcelProperty(index = 3)
    private String townshipName;

    @ExcelProperty(index = 4)
    private String facilityTypeLabel;

    @ExcelProperty(index = 5)
    private String riverLevelLabel;

    @ExcelProperty(index = 6)
    private String provincialHeadName;

    @ExcelProperty(index = 7)
    private String provincialHeadPosition;

    @ExcelProperty(index = 8)
    private String cityHeadName;

    @ExcelProperty(index = 9)
    private String cityHeadPosition;

    @ExcelProperty(index = 10)
    private String countyHeadName;

    @ExcelProperty(index = 11)
    private String countyHeadPosition;

    @ExcelProperty(index = 12)
    private String townshipHeadName;

    @ExcelProperty(index = 13)
    private String townshipHeadContact;

    @ExcelProperty(index = 14)
    private String townshipHeadPosition;

    @ExcelProperty(index = 15)
    private String villageHead1Name;

    @ExcelProperty(index = 16)
    private String villageHead1Contact;

    @ExcelProperty(index = 17)
    private String villageHead1Position;

    @ExcelProperty(index = 18)
    private String villageHead2Name;

    @ExcelProperty(index = 19)
    private String villageHead2Contact;

    @ExcelProperty(index = 20)
    private String villageHead2Position;

    @ExcelProperty(index = 21)
    private String villageHead3Name;

    @ExcelProperty(index = 22)
    private String villageHead3Contact;

    @ExcelProperty(index = 23)
    private String villageHead3Position;

    @ExcelProperty(index = 24)
    private String villageHead4Name;

    @ExcelProperty(index = 25)
    private String villageHead4Contact;

    @ExcelProperty(index = 26)
    private String villageHead4Position;

    @ExcelProperty(index = 27)
    private String villageHead5Name;

    @ExcelProperty(index = 28)
    private String villageHead5Contact;

    @ExcelProperty(index = 29)
    private String villageHead5Position;

    @ExcelProperty(index = 30)
    private String villageHead6Name;

    @ExcelProperty(index = 31)
    private String villageHead6Contact;

    @ExcelProperty(index = 32)
    private String villageHead6Position;

    @ExcelProperty(index = 33)
    private String villageHead7Name;

    @ExcelProperty(index = 34)
    private String villageHead7Contact;

    @ExcelProperty(index = 35)
    private String villageHead7Position;

    @ExcelProperty(index = 36)
    private String villageHead8Name;

    @ExcelProperty(index = 37)
    private String villageHead8Contact;

    @ExcelProperty(index = 38)
    private String villageHead8Position;

    @ExcelProperty(index = 39)
    private String villageHead9Name;

    @ExcelProperty(index = 40)
    private String villageHead9Contact;

    @ExcelProperty(index = 41)
    private String villageHead9Position;

    @ExcelProperty(index = 42)
    private String villageHead10Name;

    @ExcelProperty(index = 43)
    private String villageHead10Contact;

    @ExcelProperty(index = 44)
    private String villageHead10Position;
}
