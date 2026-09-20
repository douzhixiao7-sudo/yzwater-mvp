package com.sydigit.yzwater.module.controller.admin.vo.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * FY 图层采集请求
 */
@Data
@Schema(description = "管理后台 - FY 图层采集请求")
public class WeatherFyCollectReqVO {

    @Schema(description = "图层类型列表：fengyun2/fengyun4/himawari8；为空时采集全部")
    private List<String> types;

}

