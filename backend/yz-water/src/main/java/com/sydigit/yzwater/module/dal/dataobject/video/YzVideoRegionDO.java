package com.sydigit.yzwater.module.dal.dataobject.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频区域实体
 */
@Data
@Schema(description = "视频区域实体")
@TableName("yz_video_regions")
@TenantIgnore
public class YzVideoRegionDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "监控区域编码")
    @TableId(value = "index_code", type = IdType.INPUT)
    private String indexCode;

    @Schema(description = "监控区域名称")
    @TableField("name")
    private String name;

    @Schema(description = "父级监控区域编码")
    @TableField("parent_index_code")
    private String parentIndexCode;

    @Schema(description = "区域树编码")
    @TableField("tree_code")
    private String treeCode;
}

