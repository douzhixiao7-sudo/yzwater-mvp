package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 历史河长信息分页查询请求
 *
 * <p>说明：该页面为只读展示，用于按关联设施查看当前河长与历史变更记录。</p>
 */
@Schema(description = "仪征管理后台 - 历史河长信息分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RiverChiefHistoryPageReqVO extends PageParam {

    @Schema(description = "关联设施类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "关联设施名称关键字（按设施名称模糊匹配）")
    private String referenceName;

    @Schema(description = "河长级别（字典：zd_hzjb，仅过滤“当前河长”）")
    private String headLevel;

    @Schema(description = "河长姓名（模糊匹配，仅过滤“当前河长”）")
    private String headName;
}
