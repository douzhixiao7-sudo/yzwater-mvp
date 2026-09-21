package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "仪征管理后台 - 河长信息详情 Response VO")
@Data
public class RiverChiefInfoDetailRespVO {

    @Schema(description = "主键 ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长级别")
    private String headLevel;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "工作单位")
    private String headUnit;

    @Schema(description = "联系电话")
    private String headContact;

    @Schema(description = "河长职责")
    private String responsibilities;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "行政区划（可为空，多选）")
    private List<String> administrativeRegion;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveFrom;

    @Schema(description = "关联设施列表")
    private List<RiverChiefInfoFacilityRespVO> facilities;

    @Schema(description = "合并展示维度下全部成员记录 ID（姓名+河长级别+职务相同）")
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> memberIds;
}
