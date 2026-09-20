package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计-防汛抗旱 FY图层响应
 */
@Data
public class BigScreenFloodDroughtFyImageRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "图层类型")
    private String type;

    @Schema(description = "天气时间（yyyyMMddHHmm）")
    private String weatherTime;

    @Schema(description = "图层图片完整URL")
    private String weatherImage;

    @Schema(description = "文件名")
    private String fileName;
}
