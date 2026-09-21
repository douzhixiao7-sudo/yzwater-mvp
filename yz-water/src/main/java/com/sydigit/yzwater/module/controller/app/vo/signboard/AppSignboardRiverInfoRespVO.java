package com.sydigit.yzwater.module.controller.app.vo.signboard;

import com.sydigit.yzwater.module.controller.admin.vo.river.RiverChannelQrSnapshotVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手机端 - 扫码查询公示牌关联信息响应
 */
@Schema(description = "手机端 - 扫码查询公示牌关联信息响应")
@Data
public class AppSignboardRiverInfoRespVO {

    @Schema(description = "公示牌ID（主记录）")
    private Long signboardId;

    @Schema(description = "公示牌ID列表（同一码可能存在多条关联记录时返回）")
    private List<Long> signboardIds;

    @Schema(description = "公示牌二维码标识")
    private String qrCode;

    @Schema(description = "公示牌名称")
    private String signboardName;

    @Schema(description = "公示牌代码")
    private String signboardCode;

    @Schema(description = "行政区划")
    private String adminRegion;

    @Schema(description = "维护单位（字典label列表）")
    private List<String> maintenanceUnitLabels;

    @Schema(description = "关联河道ID")
    private Long riverChannelId;

    @Schema(description = "关联河段ID")
    private Long riverSectionId;

    @Schema(description = "关联水库ID")
    private Long waterReservoirId;

    @Schema(description = "所在河道名称（兼容历史字段）")
    private String riverChannelName;

    @Schema(description = "所在河段名称（兼容历史字段）")
    private String riverSectionName;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "具体位置")
    private String specificLocation;

    @Schema(description = "公示牌图片URL列表")
    private List<String> signboardImages;

   /* @Schema(description = "关联的河道与河段信息快照（兼容历史字段，默认取第一条河道快照）")
    private RiverChannelQrSnapshotVO river;*/

    @Schema(description = "关联的河道与河段信息快照列表（支持多河道）")
    private List<RiverChannelQrSnapshotVO> rivers;

    @Schema(description = "公示牌直接关联的河段列表（支持多河段）")
    private List<AppSignboardRiverSectionRespVO> riverSections;

    @Schema(description = "关联的水库列表（支持多水库）")
    private List<AppSignboardReservoirRespVO> waterReservoirs;
}
