package com.sydigit.yzwater.module.system.service.area.dto;

import lombok.Data;

import java.util.List;

/**
 * 行政区划 GeoJSON 导入结果
 */
@Data
public class SystemAreaGeoJsonImportResult {

    private int total;
    private int created;
    private int updated;
    private int skipped;
    private List<String> errors;
}

