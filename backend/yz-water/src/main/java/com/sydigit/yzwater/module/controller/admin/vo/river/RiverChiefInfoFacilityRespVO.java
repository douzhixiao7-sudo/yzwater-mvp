package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "仪征管理后台 - 河长信息详情-关联设施 Response VO")
@Data
public class RiverChiefInfoFacilityRespVO {

    @Schema(description = "关联设施类型（river/river_section/reservoir）")
    private String referenceType;

    @Schema(description = "关联设施类型名称")
    private String referenceTypeLabel;

    @Schema(description = "关联设施 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long referenceId;

    @Schema(description = "关联设施名称")
    private String referenceName;
}
