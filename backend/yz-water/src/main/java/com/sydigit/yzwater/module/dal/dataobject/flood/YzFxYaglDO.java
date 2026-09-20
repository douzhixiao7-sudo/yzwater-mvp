package com.sydigit.yzwater.module.dal.dataobject.flood;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

/**
 * 预案管理实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "预案管理实体")
@TableName(value = "yz_fx_yagl", autoResultMap = true)
@TenantIgnore
public class YzFxYaglDO extends BaseDO {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @Schema(description = "预案名称")
    @TableField("name")
    private String name;

    @Schema(description = "文件 URL 数组")
    @TableField(value = "files", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] files;

    @Schema(description = "排序号")
    @TableField("sort")
    private Integer sort;
}
