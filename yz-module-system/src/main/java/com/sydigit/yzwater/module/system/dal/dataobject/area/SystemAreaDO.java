package com.sydigit.yzwater.module.system.dal.dataobject.area;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 行政区划表
 *
 * 说明：
 * - id 使用行政区划编码（例如 321081）
 * - gemo 为 PostGIS geometry 类型（SRID=4490），在本表中以数据库字段存储；
 *   在 Java 层使用 WKT 字符串进行读写转换（字段 {@link #gemo} 不参与 MyBatis-Plus 的自动映射）
 */
@TableName("system_area")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TenantIgnore
public class SystemAreaDO extends BaseDO {

    /**
     * 行政区划编码
     */
    @TableId(type = IdType.INPUT)
    private Long id;
    /**
     * 父级行政区划编码
     */
    private Long parentId;
    /**
     * 名称
     */
    private String name;
    /**
     * 类型（对应 Area.type）
     */
    private Integer type;
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 空间信息（WKT 字符串），对应数据库字段 gemo（SRID=4490）
     *
     * 注意：为了避免引入额外几何依赖，该字段不参与 MyBatis-Plus 自动映射；
     * 需要通过 Mapper 自定义 SQL 使用 ST_AsText / ST_GeomFromText 进行读写。
     */
    @TableField(exist = false)
    private String gemo;

    /**
     * 空间信息（GeoJSON 字符串），对应数据库字段 gemo（SRID=4490）
     *
     * 注意：该字段同样不参与 MyBatis-Plus 自动映射，仅用于 Mapper 自定义 SQL 查询返回。
     */
    @TableField(exist = false)
    private String gemoGeoJson;

}
