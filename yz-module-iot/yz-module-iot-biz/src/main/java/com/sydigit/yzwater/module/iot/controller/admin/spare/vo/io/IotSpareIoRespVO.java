package com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "IoT - 备件出入库记录 Response VO")
@Data
public class IotSpareIoRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "备件 ID", example = "1024")
    private Long spareId;

    @Schema(description = "备件名称")
    private String spareName;

    @Schema(description = "备件规格")
    private String spareSpec;

    @Schema(description = "备件型号")
    private String spareModel;

    @Schema(description = "存放位置")
    private String storageLocation;

    @Schema(description = "最低库存阈值")
    private Integer minStock;

    @Schema(description = "备件图片")
    private java.util.List<String> spareImages;

    @Schema(description = "出入库类型")
    private String ioType;

    @Schema(description = "出入库时间")
    private LocalDateTime ioTime;

    @Schema(description = "出入库数量")
    private Integer ioQty;

    @Schema(description = "出库用途类型")
    private String usageType;

    @Schema(description = "用途关联 ID")
    private Long usageId;

    @Schema(description = "操作人用户 ID")
    private Long operatorUserId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "审批状态")
    private String auditStatus;

    @Schema(description = "审批人用户 ID")
    private Long auditUserId;

    @Schema(description = "审批人姓名")
    private String auditUserName;

    @Schema(description = "审批时间")
    private LocalDateTime auditTime;

    @Schema(description = "审批备注")
    private String auditRemark;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
