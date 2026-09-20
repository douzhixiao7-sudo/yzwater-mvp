package com.sydigit.yzwater.module.iot.service.openapi;

import com.sydigit.yzwater.module.iot.controller.admin.openapi.XfhhOpenApiConstants;
import com.sydigit.yzwater.module.iot.dal.mysql.openapi.XfhhScriptQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 幸福河湖 MySQL 查询服务实现。
 */
@Service
@Validated
@RequiredArgsConstructor
public class XfhhMysqlQueryServiceImpl implements XfhhMysqlQueryService {

    private final XfhhScriptQueryMapper scriptQueryMapper;

    @Override
    public List<Map<String, Object>> list(String code, Map<String, Object> params) {
        Map<String, Object> safeParams = params == null ? Collections.emptyMap() : params;
        return switch (code) {
            case XfhhOpenApiConstants.CODE_MONITORING_STATION_LIST -> scriptQueryMapper.selectMonitoringStations();
            case XfhhOpenApiConstants.CODE_WATER_GAUGE_LIST -> scriptQueryMapper.selectWaterGauges();
            case XfhhOpenApiConstants.CODE_WATER_LEVEL_DATA_LIST -> scriptQueryMapper.selectWaterLevelData(
                    toLong(safeParams.get("gaugeId")),
                    toStringValue(safeParams.get("bgnRecordTime")),
                    toStringValue(safeParams.get("endRecordTime")));
            case XfhhOpenApiConstants.CODE_ALERT_LOG_LIST -> scriptQueryMapper.selectAlertLogs(
                    toLong(safeParams.get("gaugeId")),
                    toStringValue(safeParams.get("bgnAlertTime")),
                    toStringValue(safeParams.get("endAlertTime")));
            case XfhhOpenApiConstants.CODE_WATER_QUALITY_STATION_LIST -> scriptQueryMapper.selectWaterQualityStations();
            case XfhhOpenApiConstants.CODE_WATER_QUALITY_PARAMETER_LIST -> scriptQueryMapper.selectWaterQualityParameters();
            case XfhhOpenApiConstants.CODE_WATER_QUALITY_DATA_LIST -> scriptQueryMapper.selectWaterQualityData(
                    toLong(safeParams.get("stationId")),
                    toStringValue(safeParams.get("bgnRecordTime")),
                    toStringValue(safeParams.get("endRecordTime")));
            case XfhhOpenApiConstants.CODE_WATER_QUALITY_ALERT_LIST -> scriptQueryMapper.selectWaterQualityAlerts(
                    toLong(safeParams.get("stationId")),
                    toStringValue(safeParams.get("bgnAlertTime")),
                    toStringValue(safeParams.get("endAlertTime")));
            case XfhhOpenApiConstants.CODE_WATER_QUALITY_THRESHOLD_LIST -> scriptQueryMapper.selectWaterQualityThresholds();
            case XfhhOpenApiConstants.CODE_VIDEO_REGIONS_LIST -> scriptQueryMapper.selectVideoRegions();
            case XfhhOpenApiConstants.CODE_VIDEO_CAMERAS_LIST -> scriptQueryMapper.selectVideoCameras(
                    toStringValue(safeParams.get("parentId")));
            case XfhhOpenApiConstants.CODE_VIDEO_CAMERAS_BY_REGION_LIST -> scriptQueryMapper.selectVideoCamerasByRegion(
                    resolveRegionIndexCode(safeParams));
            default -> throw new IllegalArgumentException("不支持的接口编码：" + code);
        };
    }

    private String resolveRegionIndexCode(Map<String, Object> params) {
        String regionIndexCode = toStringValue(params.get("regionIndexCode"));
        if (regionIndexCode == null || regionIndexCode.isBlank()) {
            return XfhhOpenApiConstants.DEFAULT_REGION_INDEX_CODE;
        }
        return regionIndexCode;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Long.parseLong(text);
    }

    private String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
