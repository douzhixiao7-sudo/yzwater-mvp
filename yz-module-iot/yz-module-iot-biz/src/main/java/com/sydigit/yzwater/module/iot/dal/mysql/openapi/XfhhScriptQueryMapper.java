package com.sydigit.yzwater.module.iot.dal.mysql.openapi;

import com.sydigit.yzwater.module.iot.framework.xfhh.core.annotation.XfhhMysqlDS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 幸福河湖脚本 SQL 迁移 Mapper。
 */
@Mapper
@XfhhMysqlDS
public interface XfhhScriptQueryMapper {

    @Select("""
            select
                a.station_id as stationId,
                a.station_name as stationName,
                a.location as location,
                a.latitude as latitude,
                a.longitude as longitude
            from yz_xfhh_monitoring_station a
            where a.del_flag = '0'
            """)
    List<Map<String, Object>> selectMonitoringStations();

    @Select("""
            select
                a.gauge_id as gaugeId,
                a.station_id as stationId,
                a.gauge_code as gaugeCode,
                a.model as model,
                date_format(a.install_date, '%Y-%m-%d %H:%i:%s') as installDate,
                a.status as status,
                a.warning_level as warningLevel,
                a.danger_level as dangerLevel
            from yz_xfhh_water_gauge a
            where a.del_flag = '0'
            """)
    List<Map<String, Object>> selectWaterGauges();

    @Select({"<script>",
            "select",
            " a.data_id as dataId,",
            " a.gauge_id as gaugeId,",
            " a.water_level as waterLevel,",
            " date_format(a.record_time, '%Y-%m-%d %H:%i:%s') as recordTime,",
            " a.data_source as dataSource,",
            " a.is_alert as isAlert,",
            " a.wd as wd,",
            " a.sd as sd,",
            " a.zlkg as zlkg,",
            " a.dl as dl,",
            " a.jgjl as jgjl,",
            " a.ldjl as ldjl",
            "from yz_xfhh_water_level_data a",
            "where a.del_flag = '0'",
            "<if test='gaugeId != null'> and a.gauge_id = #{gaugeId} </if>",
            "and a.record_time <![CDATA[ >= ]]> #{bgnRecordTime}",
            "<if test='endRecordTime != null and endRecordTime != \"\"'>",
            "  and a.record_time <![CDATA[ <= ]]> #{endRecordTime}",
            "</if>",
            "order by a.record_time",
            "</script>"})
    List<Map<String, Object>> selectWaterLevelData(@Param("gaugeId") Long gaugeId,
                                                   @Param("bgnRecordTime") String bgnRecordTime,
                                                   @Param("endRecordTime") String endRecordTime);

    @Select({"<script>",
            "select",
            " a.alert_id as alertId,",
            " a.gauge_id as gaugeId,",
            " a.alert_type as alertType,",
            " a.alert_value as alertValue,",
            " date_format(a.alert_time, '%Y-%m-%d %H:%i:%s') as alertTime,",
            " a.handled as handled",
            "from yz_xfhh_alert_log a",
            "where a.del_flag = '0'",
            "<if test='gaugeId != null'> and a.gauge_id = #{gaugeId} </if>",
            "and a.alert_time <![CDATA[ >= ]]> #{bgnAlertTime}",
            "<if test='endAlertTime != null and endAlertTime != \"\"'>",
            "  and a.alert_time <![CDATA[ <= ]]> #{endAlertTime}",
            "</if>",
            "order by a.alert_time",
            "</script>"})
    List<Map<String, Object>> selectAlertLogs(@Param("gaugeId") Long gaugeId,
                                              @Param("bgnAlertTime") String bgnAlertTime,
                                              @Param("endAlertTime") String endAlertTime);

    @Select("""
            select
                a.station_id as stationId,
                a.station_name as stationName,
                a.location as location,
                a.latitude as latitude,
                a.longitude as longitude,
                a.water_body_type as waterBodyType
            from yz_xfhh_water_quality_station a
            where a.del_flag = '0'
            """)
    List<Map<String, Object>> selectWaterQualityStations();

    @Select("""
            select
                a.id as id,
                a.param_id as paramId,
                a.param_name as paramName,
                a.unit as unit,
                a.standard_range as standardRange,
                a.description as description
            from yz_xfhh_water_quality_parameter a
            where a.del_flag = '0'
            """)
    List<Map<String, Object>> selectWaterQualityParameters();

    @Select({"<script>",
            "select",
            " a.id as dataId,",
            " a.station_id as stationId,",
            " a.param_id as paramId,",
            " a.value as value,",
            " date_format(a.record_time, '%Y-%m-%d %H:%i:%s') as recordTime,",
            " a.data_source as dataSource,",
            " a.is_abnormal as isAbnormal",
            "from yz_xfhh_water_quality_data a",
            "where a.del_flag = '0'",
            "<if test='stationId != null'> and a.station_id = #{stationId} </if>",
            "and a.record_time <![CDATA[ >= ]]> #{bgnRecordTime}",
            "<if test='endRecordTime != null and endRecordTime != \"\"'>",
            "  and a.record_time <![CDATA[ <= ]]> #{endRecordTime}",
            "</if>",
            "order by a.record_time",
            "</script>"})
    List<Map<String, Object>> selectWaterQualityData(@Param("stationId") Long stationId,
                                                     @Param("bgnRecordTime") String bgnRecordTime,
                                                     @Param("endRecordTime") String endRecordTime);

    @Select({"<script>",
            "select",
            " a.alert_id as alertId,",
            " a.station_id as stationId,",
            " a.param_id as paramId,",
            " a.measure_value as measureValue,",
            " a.threshold_type as thresholdType,",
            " a.threshold_value as thresholdValue,",
            " date_format(a.alert_time, '%Y-%m-%d %H:%i:%s') as alertTime,",
            " a.handled as handled",
            "from yz_xfhh_water_quality_alert a",
            "where a.del_flag = '0'",
            "<if test='stationId != null'> and a.station_id = #{stationId} </if>",
            "and a.alert_time <![CDATA[ >= ]]> #{bgnAlertTime}",
            "<if test='endAlertTime != null and endAlertTime != \"\"'>",
            "  and a.alert_time <![CDATA[ <= ]]> #{endAlertTime}",
            "</if>",
            "order by a.alert_time",
            "</script>"})
    List<Map<String, Object>> selectWaterQualityAlerts(@Param("stationId") Long stationId,
                                                       @Param("bgnAlertTime") String bgnAlertTime,
                                                       @Param("endAlertTime") String endAlertTime);

    @Select("""
            select
                a.id as id,
                a.threshold_id as thresholdId,
                a.station_id as stationId,
                a.param_id as paramId,
                a.min_value as minValue,
                a.max_value as `maxValue`,
                date_format(a.effective_date, '%Y-%m-%d %H:%i:%s') as effectiveDate
            from yz_xfhh_water_quality_threshold a
            where a.del_flag = '0'
            """)
    List<Map<String, Object>> selectWaterQualityThresholds();

    @Select("""
            select
                a.index_code as id,
                a.name as name,
                a.parent_index_code as parentId,
                a.tree_code as treeCode
            from yz_video_regions a
            order by a.tree_code asc
            """)
    List<Map<String, Object>> selectVideoRegions();

    @Select("""
            select
                a.camera_index_code as cameraIndexCode,
                a.camera_name as cameraName,
                a.online as online,
                a.lon as lon,
                a.lat as lat
            from yz_video_cameras a
            where a.region_index_code = #{regionIndexCode}
            """)
    List<Map<String, Object>> selectVideoCamerasByRegion(@Param("regionIndexCode") String regionIndexCode);

    @Select({"<script>",
            "select",
            " a.camera_index_code as id,",
            " a.camera_name as name,",
            " a.region_index_code as parentId,",
            " b.name as regionName,",
            " a.channel_no as channelNo,",
            " a.online as online,",
            " a.lon as lon,",
            " a.lat as lat",
            "from yz_video_cameras a",
            "inner join yz_video_regions b on b.index_code = a.region_index_code",
            "<where>",
            "  <if test='parentId != null and parentId != \"\"'>",
            "    a.region_index_code = #{parentId}",
            "  </if>",
            "</where>",
            "order by a.channel_no asc",
            "</script>"})
    List<Map<String, Object>> selectVideoCameras(@Param("parentId") String parentId);
}
