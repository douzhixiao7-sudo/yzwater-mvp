package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "仪征管理后台 - 河长管理新增/编辑 Request VO")
@Data
public class RiverChiefManagementSaveReqVO {

    @Schema(description = "主键ID（编辑必填）")
    private Long id;

    @Schema(description = "关联设施类型（river：河道；river_section：河段；reservoir：水库）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联设施类型不能为空")
    private String referenceType;

    @Schema(description = "关联设施ID（河道ID/河段ID/水库ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联设施ID不能为空")
    private Long referenceId;

    @Schema(description = "河长姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "河长姓名不能为空")
    private String headName;

    @Schema(description = "河长级别（字典：zd_hzjb）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "河长级别不能为空")
    private String headLevel;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "河长工作单位")
    private String headUnit;

    @Schema(description = "河长联系电话")
    private String headContact;

    @Schema(description = "河长职责")
    private String responsibilities;

    @Schema(description = "行政区划（可为空，多选）")
    private List<String> administrativeRegion;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "生效时间（前端无需传，后端自动写入当前时间）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate effectiveFrom;
}
