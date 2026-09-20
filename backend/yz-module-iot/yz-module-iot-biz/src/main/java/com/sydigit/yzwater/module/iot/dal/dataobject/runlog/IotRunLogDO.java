package com.sydigit.yzwater.module.iot.dal.dataobject.runlog;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.sydigit.yzwater.module.iot.dal.mybatis.typehandler.StringArrayTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

/**
 * 运行日志 DO
 */
@TableName(value = "yz_run_log", autoResultMap = true)
@KeySequence("yz_run_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotRunLogDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 记录编号
     */
    private String logNo;

    /**
     * 任务名称
     */
    private String logTitle;

    /**
     * 值班班组ID
     */
    private Long teamId;

    /**
     * 值班班组名称
     */
    private String teamName;

    /**
     * 记录人用户ID
     */
    private Long recorderUserId;

    /**
     * 记录人名称
     */
    private String recorderUserName;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 运行开始时间
     */
    private LocalDateTime runStartTime;

    /**
     * 运行结束时间
     */
    private LocalDateTime runEndTime;

    /**
     * 检查时段
     */
    private String checkPeriod;

    /**
     * 所属站点
     */
    private String stationId;

    /**
     * 巡检类型
     */
    private String inspectionType;

    /**
     * 巡检标准ID
     */
    private Long inspectionStandardId;

    /**
     * 巡检标准名称快照
     */
    private String inspectionStandardName;

    /**
     * 巡检结果明细(JSON)
     */
    private String inspectionResultItemsJson;

    /**
     * 设备名称（兼容历史字段）
     */
    private String deviceName;

    /**
     * 运行参数（兼容历史字段）
     */
    private String runParamsText;

    /**
     * 事件描述（兼容历史字段）
     */
    private String eventDesc;

    /**
     * 附件列表
     */
    @TableField(value = "attachment_file_ids", jdbcType = JdbcType.ARRAY, typeHandler = StringArrayTypeHandler.class)
    private String[] attachmentFileIds;

    /**
     * 关联调度指令ID
     */
    private Long dispatchInstructionId;

    /**
     * 关联调令编号快照
     */
    private String dispatchInstructionNo;

    /**
     * 关联调令名称快照
     */
    private String dispatchInstructionName;

    /**
     * 备注
     */
    private String remark;
}

