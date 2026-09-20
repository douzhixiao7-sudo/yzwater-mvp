package com.sydigit.yzwater.module.controller.admin.vo.signboard;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 公示牌分页查询请求
 */
@Schema(description = "仪征管理后台 - 公示牌分页查询请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class SignboardPageReqVO extends PageParam {

    @Schema(description = "公示牌名称，支持模糊")
    private String signboardName;

    @Schema(description = "公示牌代码，支持模糊")
    private String signboardCode;

    @Schema(description = "关联河道/河段名称关键词，支持模糊")
    private String riverKeyword;

    @Schema(description = "责任单位，支持模糊")
    private String maintenanceUnit;

    @Schema(description = "公示牌等级(字典: zd_hljb，可多选)")
    private List<String> signboardLevel;

    @Schema(description = "是否大屏展示（1=展示，0=不展示）")
    private Integer isScreenDisplay;
}
