package com.sydigit.yzwater.module.dal.dataobject.geoBase;

import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水库基础数据实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "水库基础数据实体")
@TableName("yz_base_reservoir")
@TenantIgnore
@Deprecated
public class YzBaseReservoirDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "创建人")
    @TableField("create_by")
    private String createBy;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "修改人")
    @TableField("modify_by")
    private String modifyBy;

    @Schema(description = "修改时间")
    @TableField("modify_time")
    private LocalDateTime modifyTime;

    @Schema(description = "删除标记：1表示删除，0表示未删除")
    @TableField("del_flag")
    private Integer delFlag;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private String tenantId;

    @Schema(description = "代码")
    @TableField("code")
    private String code;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "规模")
    @TableField("gm")
    private String gm;

    @Schema(description = "所在乡镇")
    @TableField("szxz")
    private String szxz;

    @Schema(description = "管理单位")
    @TableField("gldw")
    private String gldw;

    @Schema(description = "区划代码")
    @TableField("division_code")
    private String divisionCode;

    @Schema(description = "经度")
    @TableField("longitude")
    private Double longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private Double latitude;

    @Schema(description = "坝高（米）")
    @TableField("height")
    private BigDecimal height;

    @Schema(description = "坝长（米）")
    @TableField("length")
    private BigDecimal length;

    @Schema(description = "总库容")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "集水面积")
    @TableField("jsmj")
    private BigDecimal jsmj;

    @Schema(description = "兴利库容")
    @TableField("xlkr")
    private BigDecimal xlkr;

    @Schema(description = "调洪库容")
    @TableField("thkr")
    private BigDecimal thkr;

    @Schema(description = "死库容")
    @TableField("skr")
    private BigDecimal skr;

    @Schema(description = "设计水位")
    @TableField("sjsw")
    private BigDecimal sjsw;

    @Schema(description = "兴利水位")
    @TableField("xlsw")
    private BigDecimal xlsw;

    @Schema(description = "限制水位")
    @TableField("xxsw")
    private BigDecimal xxsw;

    @Schema(description = "死水位")
    @TableField("ssw")
    private BigDecimal ssw;

    @Schema(description = "设计灌溉面积")
    @TableField("sjggmj")
    private BigDecimal sjggmj;

    @Schema(description = "实际灌溉面积")
    @TableField("ggmj")
    private BigDecimal ggmj;

    @Schema(description = "年供水量")
    @TableField("ngsl")
    private BigDecimal ngsl;

    @Schema(description = "宜渔面积")
    @TableField("yymj")
    private BigDecimal yymj;

    @Schema(description = "周长")
    @TableField("shape_length")
    private BigDecimal shapeLength;

    @Schema(description = "面积")
    @TableField("shape_area")
    private BigDecimal shapeArea;

    @Schema(description = "挡水主坝类型")
    @TableField("type")
    private String type;

    @Schema(description = "地图标注（GeoJSON 格式）")
    @TableField("geo")
    private String geo;
}
