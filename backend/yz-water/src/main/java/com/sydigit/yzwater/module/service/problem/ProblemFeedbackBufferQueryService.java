package com.sydigit.yzwater.module.service.problem;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackBufferQueryReqVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackBufferQueryRespVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackBufferQueryReservoirRespVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackBufferQueryRiverRespVO;
import com.sydigit.yzwater.module.controller.app.vo.problem.AppProblemFeedbackBufferQuerySectionRespVO;
import com.sydigit.yzwater.module.dal.mysql.gis.YzGisBufferQueryMapper;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisReservoirRangeRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisRiverChannelRangeRow;
import com.sydigit.yzwater.module.dal.mysql.gis.dto.GisRiverSectionRangeRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 手机端 App 专属：缓冲区查询从 BF 备份表做空间检索（{@code yz_river_channel_bf}、
 * {@code yz_river_section_bf}、{@code yz_water_reservoir_bf}）。
 *
 * <p>河道/河段按 {@code river_level} 排除镇级（{@code 6j}）与村级（{@code 7j}）；</p>
 * <p>水库仅展示关联公示牌等级为省/市/县级（排除镇、村级公示牌）的水库。</p>
 */
@Service
@RequiredArgsConstructor
public class ProblemFeedbackBufferQueryService {

    private final YzGisBufferQueryMapper gisBufferQueryMapper;

    public AppProblemFeedbackBufferQueryRespVO query(AppProblemFeedbackBufferQueryReqVO reqVO) {
        BigDecimal longitude = reqVO.getLongitude();
        BigDecimal latitude = reqVO.getLatitude();
        BigDecimal radius = reqVO.getRadiusM();
        if (longitude == null || latitude == null || radius == null) {
            throw ServiceExceptionUtil.invalidParamException("中心点经纬度与半径不能为空");
        }
        if (radius.compareTo(BigDecimal.ZERO) <= 0) {
            throw ServiceExceptionUtil.invalidParamException("查询半径必须大于0");
        }

        List<GisRiverChannelRangeRow> riverRows = gisBufferQueryMapper
                .selectRiverChannelBfInRange(longitude, latitude, radius);
        List<GisRiverSectionRangeRow> sectionRows = gisBufferQueryMapper
                .selectRiverSectionBfInRange(longitude, latitude, radius);
        List<GisReservoirRangeRow> reservoirRows = gisBufferQueryMapper
                .selectReservoirBfInRange(longitude, latitude, radius);

        AppProblemFeedbackBufferQueryRespVO respVO = new AppProblemFeedbackBufferQueryRespVO();
        respVO.setLongitude(longitude);
        respVO.setLatitude(latitude);
        respVO.setRadiusM(radius.setScale(2, RoundingMode.HALF_UP));
        respVO.setRivers(buildRiverRespList(riverRows));
        respVO.setRiverSections(buildSectionRespList(sectionRows));
        respVO.setReservoirs(buildReservoirRespList(reservoirRows));
        return respVO;
    }

    private List<AppProblemFeedbackBufferQueryRiverRespVO> buildRiverRespList(List<GisRiverChannelRangeRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<AppProblemFeedbackBufferQueryRiverRespVO> result = new ArrayList<>(rows.size());
        for (GisRiverChannelRangeRow row : rows) {
            if (row == null || row.getId() == null) {
                continue;
            }
            AppProblemFeedbackBufferQueryRiverRespVO item = new AppProblemFeedbackBufferQueryRiverRespVO();
            item.setId(row.getId());
            item.setRiverName(row.getRiverName());
            item.setGeomType(row.getGeomType());
            item.setGeomWkt(row.getGeomWkt());
            item.setDistanceM(row.getDistanceM());
            result.add(item);
        }
        return result;
    }

    private List<AppProblemFeedbackBufferQuerySectionRespVO> buildSectionRespList(List<GisRiverSectionRangeRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<AppProblemFeedbackBufferQuerySectionRespVO> result = new ArrayList<>(rows.size());
        for (GisRiverSectionRangeRow row : rows) {
            if (row == null || row.getId() == null) {
                continue;
            }
            AppProblemFeedbackBufferQuerySectionRespVO item = new AppProblemFeedbackBufferQuerySectionRespVO();
            item.setId(row.getId());
            item.setRiverChannelId(row.getRiverChannelId());
            item.setSectionName(row.getSectionName());
            item.setDisplayName(buildSectionDisplayName(row));
            item.setGeomType(row.getGeomType());
            item.setGeomWkt(row.getGeomWkt());
            item.setDistanceM(row.getDistanceM());
            result.add(item);
        }
        return result;
    }

    private String buildSectionDisplayName(GisRiverSectionRangeRow row) {
        String sectionName = StrUtil.blankToDefault(row.getSectionName(), "");
        String riverName = StrUtil.blankToDefault(row.getRiverName(), "");
        Integer sectionCount = row.getRiverSectionCount();
        if (sectionCount != null && sectionCount > 0 && StrUtil.isNotBlank(riverName)) {
            if (StrUtil.isNotBlank(sectionName)) {
                return riverName + "/" + sectionName;
            }
            return riverName;
        }
        return StrUtil.blankToDefault(sectionName, riverName);
    }

    private List<AppProblemFeedbackBufferQueryReservoirRespVO> buildReservoirRespList(List<GisReservoirRangeRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<AppProblemFeedbackBufferQueryReservoirRespVO> result = new ArrayList<>(rows.size());
        for (GisReservoirRangeRow row : rows) {
            if (row == null || row.getId() == null) {
                continue;
            }
            AppProblemFeedbackBufferQueryReservoirRespVO item = new AppProblemFeedbackBufferQueryReservoirRespVO();
            item.setId(row.getId());
            item.setReservoirName(row.getReservoirName());
            item.setGeomType(row.getGeomType());
            item.setGeomWkt(row.getGeomWkt());
            item.setDistanceM(row.getDistanceM());
            result.add(item);
        }
        return result;
    }
}
