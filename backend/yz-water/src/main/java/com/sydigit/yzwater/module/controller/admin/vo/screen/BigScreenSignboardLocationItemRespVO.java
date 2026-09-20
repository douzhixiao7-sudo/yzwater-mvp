package com.sydigit.yzwater.module.controller.admin.vo.screen;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏统计 - 公示牌点位返回项
 */
@Data
public class BigScreenSignboardLocationItemRespVO {

    @Schema(description = "公示牌主键ID")
    private Long id;

    @Schema(description = "公示牌名称")
    private String name;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "具体位置")
    private String specificLocation;

    @Schema(description = "二维码标识字段")
    private String qrCode;

    @Schema(description = "关联对象ID")
    private Long referenceId;

    @Schema(description = "未办结问题数量（问题状态任务 status != 4）")
    private Long unfinishedProblemCount;
}
