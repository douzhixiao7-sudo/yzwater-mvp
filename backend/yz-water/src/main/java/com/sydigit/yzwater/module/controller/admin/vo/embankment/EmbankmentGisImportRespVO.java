package com.sydigit.yzwater.module.controller.admin.vo.embankment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 堤防 GIS 导入结果
 */
@Data
public class EmbankmentGisImportRespVO {

    @Schema(description = "有效数据行数")
    private Integer rowCount = 0;

    @Schema(description = "堤防名称分组数")
    private Integer nameGroupCount = 0;

    @Schema(description = "成功处理的堤防数量")
    private Integer successCount = 0;

    @Schema(description = "补建基础表数量")
    private Integer createFacilityCount = 0;

    @Schema(description = "更新数量")
    private Integer updateCount = 0;

    @Schema(description = "跳过数量")
    private Integer skipCount = 0;

    @Schema(description = "结果提示")
    private String message;

    @Schema(description = "失败或跳过明细")
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

        @Schema(description = "Excel 行号")
        private Integer rowNumber;

        @Schema(description = "堤防名称")
        private String embankmentName;

        @Schema(description = "处理动作（skip/update）")
        private String action;

        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "提示信息")
        private String message;
    }
}
