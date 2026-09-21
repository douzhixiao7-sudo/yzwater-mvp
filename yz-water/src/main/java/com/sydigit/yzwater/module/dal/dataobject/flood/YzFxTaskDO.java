package com.sydigit.yzwater.module.dal.dataobject.flood;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.GeometryTypeHandler;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.locationtech.jts.geom.Geometry;

/**
 * 风险隐患点实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "风险隐患点实体")
@TableName(value = "yz_fx_task", autoResultMap = true)
@TenantIgnore
public class YzFxTaskDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "代码")
    @TableField("code")
    private String code;

    @Schema(description = "工程名称")
    @TableField("name")
    private String name;

    @Schema(description = "险工位置")
    @TableField("addr")
    private String addr;

    @Schema(description = "所属河道 ID")
    @TableField(value = "river_channel_id", updateStrategy = FieldStrategy.ALWAYS)
    private Long riverChannelId;

    @Schema(description = "所属河道名称")
    @TableField(value = "river_channel_name", updateStrategy = FieldStrategy.ALWAYS)
    private String riverChannelName;

    @Schema(description = "防汛等级")
    @TableField("level")
    private String level;

    @Schema(description = "险情描述")
    @TableField("content")
    private String content;

    @Schema(description = "应对措施")
    @TableField(value = "counter_measures", updateStrategy = FieldStrategy.ALWAYS)
    private String counterMeasures;

    @Schema(description = "附件 URL 数组")
    @TableField(value = "files", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] files;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "地图几何（点/线，SRID=4490）")
    @TableField(value = "geom", jdbcType = JdbcType.OTHER, typeHandler = GeometryTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Geometry geom;

    @Schema(description = "地图节点样式元数据（JSON）")
    @TableField(value = "geom_meta", updateStrategy = FieldStrategy.ALWAYS)
    private String geomMeta;
}
