package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 河道 GeoJSON 导入响应
 */
@Data
public class RiverChannelGeoJsonImportRespVO {

    @Schema(description = "要素总数")
    private Integer featureCount;

    @Schema(description = "成功导入数量")
    private Integer successCount;

    @Schema(description = "失败/跳过数量")
    private Integer failCount;

    @Schema(description = "基础设施新增数量")
    private Integer facilityCount;

    @Schema(description = "河道新增数量")
    private Integer riverChannelCount;

    @Schema(description = "河长记录新增数量")
    private Integer managementCount;

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

        @Schema(description = "河道编号（hdbh）")
        private String riverCode;

        @Schema(description = "河道名称（hdmc）")
        private String riverName;

        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "失败原因/提示")
        private String message;
    }
}

