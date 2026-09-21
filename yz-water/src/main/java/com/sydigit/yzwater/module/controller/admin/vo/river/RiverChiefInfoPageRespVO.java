package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "仪征管理后台 - 河长信息分页 Response VO")
@Data
public class RiverChiefInfoPageRespVO {

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

    @Schema(description = "关联设施展示文本（多个用、分隔，格式：类型：名称）")
    private String facilitySummary;

    @Schema(description = "行政区划（可为空，多选）")
    private List<String> administrativeRegion;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveFrom;

    @Schema(description = "合并展示维度下全部成员记录 ID（姓名+河长级别+职务相同的多条）")
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> memberIds;
}
