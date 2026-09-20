package com.sydigit.yzwater.module.controller.admin.vo.river;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 河长管理分页查询请求
 *
 * <p>仅查询当前有效记录：effectiveTo 为空</p>
 */
@Schema(description = "仪征管理后台 - 河长管理分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RiverChiefManagementPageReqVO extends PageParam {

    @Schema(description = "河长名称（支持模糊查询）")
    private String headName;

    @Schema(description = "河长级别（字典：zd_hzjb）")
    private String headLevel;

    @Schema(description = "行政区划（可为空，多选）")
    private List<String> administrativeRegion;

    @Schema(description = "关联设施类型（river/river_section/reservoir）")
    private String referenceType;

    @Schema(description = "关联设施名称（模糊查询）")
    private String referenceName;
}
