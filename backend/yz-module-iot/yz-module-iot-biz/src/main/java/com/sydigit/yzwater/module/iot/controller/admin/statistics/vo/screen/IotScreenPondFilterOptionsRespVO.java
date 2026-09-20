package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "大屏 - 坑塘筛选下拉选项（库内去重，对应台账筛选面板）")
@Data
public class IotScreenPondFilterOptionsRespVO {

    @Schema(description = "土地权属")
    private List<String> ownershipTypeList = new ArrayList<>();

    @Schema(description = "资源类型")
    private List<String> resourceTypeList = new ArrayList<>();

    @Schema(description = "使用状态")
    private List<String> usageStatusList = new ArrayList<>();

    @Schema(description = "资源性质")
    private List<String> resourceNatureList = new ArrayList<>();

    @Schema(description = "占用情况")
    private List<String> occupationStatusList = new ArrayList<>();

}
