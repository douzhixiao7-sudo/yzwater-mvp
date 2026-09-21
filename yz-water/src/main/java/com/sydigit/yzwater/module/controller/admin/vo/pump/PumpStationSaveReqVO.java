package com.sydigit.yzwater.module.controller.admin.vo.pump;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * 泵站新增/编辑请求
 */
@Schema(description = "仪征管理后台 - 泵站新增/编辑请求")
@Data
public class PumpStationSaveReqVO {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @Schema(description = "泵站代码")
    private String pumpStationCode;

    @Schema(description = "泵站名称")
    @NotBlank(message = "泵站名称不能为空")
    private String pumpStationName;

    @Schema(description = "区划代码（来源：/system/area/tree 的 id，可多选）")
    private List<String> divisionCode;

    @Schema(description = "泵站类型（字典：zd_bzlx）")
    private String pumpStationType;

    @Schema(description = "具体位置")
    private String pumpStationPosition;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "工程等别（字典：zd_gcdb）")
    private String engineeringGrade;

    @Schema(description = "闸站规模")
    private String engineeringScale;

    @Schema(description = "防洪设计标准")
    private String floodControlDesignStandard;

    @Schema(description = "装机功率(KW)")
    private BigDecimal installedCapacityKw;

    @Schema(description = "装机流量(m3/s)")
    private BigDecimal capacityFlow;

    @Schema(description = "机组数量")
    private Integer unitCount;

    @Schema(description = "常水位(m)")
    private BigDecimal normalWaterLevel;

    @Schema(description = "防办预降水位(m)")
    private BigDecimal preDropWaterLevel;

    @Schema(description = "最低运行水位(m)")
    private BigDecimal minimumOperatingWaterLevel;

    @Schema(description = "单机组功率(KW)")
    private BigDecimal singleUnitPower;

    @Schema(description = "自排流量(m3/s)")
    private BigDecimal selfFlow;

    @Schema(description = "抽引流量(m3/s)")
    private BigDecimal installedFlow;

    @Schema(description = "抽排流量(m3/s)")
    private BigDecimal pumpingFlow;

    @Schema(description = "建设时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate constructionTime;

    @Schema(description = "归口管理部门（字典：zd_gldw，可多选）")
    private List<String> managementDepartment;

    @Schema(description = "泵站图片（最多 5 张）")
    private List<String> pumpStationImages;

    @Schema(description = "泵站概览")
    private String pumpStationOverview;
}
