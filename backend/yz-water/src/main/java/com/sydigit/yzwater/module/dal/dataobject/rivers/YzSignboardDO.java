package com.sydigit.yzwater.module.dal.dataobject.rivers;

import com.sydigit.yzwater.framework.mybatis.core.dataobject.BaseDO;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.dal.mybatis.typehandler.StringArrayTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 公示牌信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公示牌信息实体")
@TableName(value = "yz_signboard", autoResultMap = true)
@TenantIgnore
public class YzSignboardDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "关联河道ID")
    @TableField("river_channel_id")
    private Long riverChannelId;

    @Schema(description = "关联河段ID")
    @TableField("river_section_id")
    private Long riverSectionId;

    @Schema(description = "关联水库ID")
    @TableField("water_reservoir_id")
    private Long waterReservoirId;

    @Schema(description = "关联对象ID")
    @TableField("reference_id")
    private Long referenceId;

    @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）")
    @TableField("reference_type")
    private String referenceType;


    @Schema(description = "二维码标识字段")
    @TableField("qr_code")
    private String qrCode;

    @Schema(description = "公示牌代码")
    @TableField("signboard_code")
    private String signboardCode;

    @Schema(description = "公示牌名称")
    @TableField("signboard_name")
    private String signboardName;

    @Schema(description = "公示牌类型(字典: zd_gsplx)")
    @TableField("signboard_type")
    private String signboardType;

    @Schema(description = "公示牌等级(字典: zd_hljb)")
    @TableField("signboard_level")
    private String signboardLevel;

    @Schema(description = "是否大屏展示（1=展示，0=不展示）")
    @TableField("is_screen_display")
    private Integer isScreenDisplay;

    @Schema(description = "所在河道名称")
    @TableField("river_channel_name")
    private String riverChannelName;

    @Schema(description = "所在河段名称")
    @TableField("river_section_name")
    private String riverSectionName;

    @Schema(description = "行政区划")
    @TableField("admin_region")
    private String adminRegion;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "具体位置")
    @TableField("specific_location")
    private String specificLocation;

    @Schema(description = "安装日期")
    @TableField("installation_date")
    private LocalDate installationDate;

    @Schema(description = "维护责任单位（字典：zd_whdw）")
    @TableField(value = "maintenance_unit", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] maintenanceUnit;

    @Schema(description = "责任人")
    @TableField("responsible_person")
    private String responsiblePerson;

    @Schema(description = "管理单位（字典：zd_gldw）")
    @TableField(value = "management_unit", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] managementUnit;

    @Schema(description = "权属单位（字典：zd_qsdw）")
    @TableField(value = "ownership_unit", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] ownershipUnit;

    @Schema(description = "公示牌图片(多图URL数组)")
    @TableField(value = "signboard_images", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] signboardImages;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;

    @Schema(description = "公示牌内容")
    @TableField("content")
    private String content;
}
