package com.sydigit.yzwater.module.service.dto;

import com.sydigit.yzwater.module.enums.GeometryTypeEnum;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用空间要素 DTO
 *
 * @author Lijun
 */
@Data
public class GeometryFeatureDTO {

    /**
     * 要素名称
     */
    private String featureName;

    /**
     * 业务分类
     */
    private String bizType;

    /**
     * 属性信息
     */
    private Map<String, Object> properties = new LinkedHashMap<>();

    /**
     * 几何数据
     */
    private Geometry geometry;

    /**
     * 数据使用的 SRID
     */
    private Integer srid;

    /**
     * 几何类型
     */
    private GeometryTypeEnum geometryType;

    /**
     * 原始文件名
     */
    private String sourceFile;
}

