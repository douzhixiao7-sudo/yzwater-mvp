package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 值班表保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 值班表保存 Request VO")
public class FxZbbSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "开始日期不能为空")
    private String startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "结束日期不能为空")
    private String endDate;

    @Schema(description = "值班说明")
    private String description;

    @Schema(description = "值班明细")
    @Valid
    private List<Item> items;

    @Data
    @Schema(description = "管理后台 - 值班表明细保存 Request VO")
    public static class Item {

        @Schema(description = "主键 ID（编辑时传）")
        private String id;

        @Schema(description = "星期（1-7，对应周一到周日）", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "星期不能为空")
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
}
