package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "仪征管理后台 - 河长信息编辑 Request VO")
@Data
public class RiverChiefInfoUpdateReqVO {

    @Schema(description = "代表记录 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID 不能为空")
    private Long id;

    @Schema(description = "河长姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "河长姓名不能为空")
    private String headName;

    @Schema(description = "河长级别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "河长级别不能为空")
    private String headLevel;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "工作单位")
    private String headUnit;

    @Schema(description = "联系电话")
    private String headContact;

    @Schema(description = "河长职责（当关联设施包含水库时生效）")
    private String responsibilities;

    @Schema(description = "行政区划")
    private List<String> administrativeRegion;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "生效时间（前端无需传，后端自动写入当前时间）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate effectiveFrom;

    @Schema(description = "关联设施列表（传空数组表示取消全部关联）")
    @Valid
    private List<RiverChiefInfoFacilitySaveReqVO> facilities;
}
