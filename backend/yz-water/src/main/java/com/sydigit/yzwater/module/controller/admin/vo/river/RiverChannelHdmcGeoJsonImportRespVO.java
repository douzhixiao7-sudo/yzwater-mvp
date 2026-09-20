package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 河道 GeoJSON 导入响应（按 properties.hdmc 匹配现有河道并更新几何）
 */
@Data
public class RiverChannelHdmcGeoJsonImportRespVO {

    @Schema(description = "要素总数")
    private Integer featureCount;

    @Schema(description = "成功处理数量")
    private Integer successCount;

    @Schema(description = "新增数量")
    private Integer createCount;

    @Schema(description = "更新数量")
    private Integer updateCount;

    @Schema(description = "失败/跳过数量")
    private Integer failCount;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "逐条处理结果（仅返回失败与跳过项）")
    private List<Item> items;

    public List<Item> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void addItem(Item item) {
        if (item == null) {
            return;
        }
        getItems().add(item);
    }

    @Data
    public static class Item {

        @Schema(description = "要素名称（可为空）")
        private String featureName;

        @Schema(description = "河道名称（properties.hdmc）")
        private String riverName;

        @Schema(description = "处理动作（create/update/skip）")
        private String action;

        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "失败原因/提示")
        private String message;
    }
}
