package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 大屏统计 - 河长统计返回
 */
@Data
@Schema(description = "仪征管理后台 - 大屏统计 - 河长统计返回")
public class BigScreenRiverHeadStatsRespVO {

    @Schema(description = "河长总数（按当前版本记录统计）")
    private Long totalCount;

    @Schema(description = "总河长人数（按当前有效总河长记录统计）")
    private Long totalChiefCount;

    @Schema(description = "按河长级别(字典: zd_hzjb)统计")
    private List<BigScreenDictCountItemVO> levelStats;

    @Schema(description = "省级河长人数（根据字典标签匹配）")
    private BigScreenDictCountItemVO province;

    @Schema(description = "市级河长人数（根据字典标签匹配）")
    private BigScreenDictCountItemVO city;

    @Schema(description = "县级河长人数（根据字典标签匹配）")
    private BigScreenDictCountItemVO county;

    @Schema(description = "乡镇级河长人数（根据字典标签匹配）")
    private BigScreenDictCountItemVO town;

    @Schema(description = "村级河长人数（根据字典标签匹配）")
    private BigScreenDictCountItemVO village;
}
