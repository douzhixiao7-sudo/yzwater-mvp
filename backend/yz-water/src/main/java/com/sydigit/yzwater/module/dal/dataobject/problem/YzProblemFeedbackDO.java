package com.sydigit.yzwater.module.dal.dataobject.problem;

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

/**
 * 问题反馈表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问题反馈表实体")
@TableName(value = "yz_problem_feedback", autoResultMap = true)
@TenantIgnore
public class YzProblemFeedbackDO extends BaseDO {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @Schema(description = "反馈类型（字典值，参考字典表 zd_fklx）")
    @TableField("feedback_type")
    private String feedbackType;

    @Schema(description = "反馈内容")
    @TableField("feedback_content")
    private String feedbackContent;

    @Schema(description = "上传的图片/视频，存储为文件路径的数组")
    @TableField(value = "uploaded_files", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] uploadedFiles;

    @Schema(description = "是否实名反馈（0-匿名，1-实名）")
    @TableField("is_real_name")
    private Boolean realName;

    @Schema(description = "姓名（实名反馈时为必填）")
    @TableField("name")
    private String name;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;


    @Schema(description = "问题具体位置")
    @TableField("issue_specific_location")
    private String issueSpecificLocation;

    @Schema(description = "手机号码（实名反馈时为必填）")
    @TableField("phone_number")
    private String phoneNumber;

    @Schema(description = "公示牌ID（已废弃）")
    @Deprecated
    @TableField("public_notice_id")
    private Long publicNoticeId;

    @Schema(description = "问题状态（0-待受理, 1-已驳回, 2-处理中, 3-待核验, 4-已办结）")
    @TableField("status")
    private Integer status;


    @Schema(description = "区划代码")
    @TableField(value = "division_code", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] divisionCode;

    @Schema(description = "关联对象ID")
    @TableField("reference_id")
    private Long referenceId;

    @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）")
    @TableField("reference_type")
    private String referenceType;

}
