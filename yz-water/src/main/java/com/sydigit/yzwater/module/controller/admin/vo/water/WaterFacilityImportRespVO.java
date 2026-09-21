package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 水利对象导入结果 VO
 */
@Data
public class WaterFacilityImportRespVO {

    @Schema(description = "处理的文件数量")
    private Integer fileCount;

    @Schema(description = "成功写入的基础记录数")
    private Long facilityCount;

    @Schema(description = "成功写入的空间记录数")
    private Long geometryCount;

    @Schema(description = "文件级导入结果")
    private List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    @Data
    public static class Item {

        @Schema(description = "文件名")
        private String fileName;

        @Schema(description = "导入是否成功")
        private Boolean success;

        @Schema(description = "成功写入的要素数量")
        private Integer successCount;

        @Schema(description = "失败原因")
        private String message;
    }
}
