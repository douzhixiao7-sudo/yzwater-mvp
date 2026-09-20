package com.sydigit.yzwater.module.controller.admin.vo.pond;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "坑塘筛选下拉选项（来自库内去重值）")
@Data
public class WaterPondFilterOptionsRespVO {

    @Schema(description = "使用状态")
    private List<String> usageStatusList = new ArrayList<>();

    @Schema(description = "资源性质")
    private List<String> resourceNatureList = new ArrayList<>();

    @Schema(description = "土地权属")
    private List<String> ownershipTypeList = new ArrayList<>();

    @Schema(description = "占用情况")
    private List<String> occupationStatusList = new ArrayList<>();

    @Schema(description = "资源类型")
    private List<String> resourceTypeList = new ArrayList<>();
}
