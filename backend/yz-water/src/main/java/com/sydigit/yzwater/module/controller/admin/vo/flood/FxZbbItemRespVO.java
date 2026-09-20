package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 值班表明细 Response VO
 */
@Data
@Schema(description = "管理后台 - 值班表明细 Response VO")
public class FxZbbItemRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "星期（1-7，对应周一到周日）")
    private Integer weekDay;

    @Schema(description = "值班长姓名")
    private String dutyChiefName;

    @Schema(description = "值班长电话")
    private String dutyChiefMobile;

    @Schema(description = "白班人员姓名")
    private String sectionChiefName;

    @Schema(description = "白班人员电话")
    private String sectionChiefMobile;

    @Schema(description = "夜班人员姓名")
    private String dutyStaffName;

    @Schema(description = "夜班人员电话")
    private String dutyStaffMobile;
}
