package com.sydigit.yzwater.module.iot.service.statistics;

import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenDeviceOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenDeviceStartStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateSummaryRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringPressValueRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringFlowValueRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringRuntimeStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenGateMetricSnapshotRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenOverviewCountRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPumpStationGateDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPumpStationHostDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenAreaNodeRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenAreaRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPondFilterOptionsRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPondMvtReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPondPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPondPageRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenPumpStationRunningStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenReservoirCapacityRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenReservoirSummaryRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStationHostStatusRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStationOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStreamWaterListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenStreamWaterRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenVideoStationCountRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenWorkConditionLatestRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenWorkConditionListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenWorkConditionRespVO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * IoT 大屏统计 Service
 */
public interface IotScreenStatisticsService {

    /**
     * 获取大屏总览数量
     */
    IotScreenOverviewCountRespVO getOverviewCount();

    /**
     * 获取水库容量列表（名称 + totalCapacity）
     */
    List<IotScreenReservoirCapacityRespVO> getReservoirCapacityList();

    /**
     * 获取水库总库容/总集水面积/总灌溉面积
     */
    IotScreenReservoirSummaryRespVO getReservoirSummary();

    /**
     * 获取站点下拉（字典：iot_zd_sbzd）
     */
    List<IotScreenStationOptionRespVO> getStationOptions();

    /**
     * 获取各站点主机运行/停止数量
     */
    List<IotScreenStationHostStatusRespVO> getStationHostStatusList();

    /**
     * 按泵站名称获取主机详情（主机名称、主机状态、有功功率、采集时间）
     */
    List<IotScreenPumpStationHostDetailRespVO> getPumpStationHostDetailList(String pumpStationName);

    /**
     * 获取工程监测设备总数/关闭总数/运行总数
     */
    IotScreenEngineeringDeviceStatusRespVO getEngineeringDeviceStatusSummary(String stationId);

    /**
     * 获取工程监测设备状态列表（设备类型仅支持 7 或 3）
     */
    List<IotScreenEngineeringDeviceStatusListRespVO> getEngineeringDeviceStatusList(
            @Valid IotScreenEngineeringDeviceStatusListReqVO reqVO);

    /**
     * 获取工程监测开机次数/累计运行时间/本次运行时间
     */
    List<IotScreenEngineeringRuntimeStatRespVO> getEngineeringRuntimeStatSummary(String stationId);

    /**
     * 获取工程监测闸门总数/全关总数/全开总数
     */
    IotScreenEngineeringGateSummaryRespVO getEngineeringGateStatusSummary(String stationId);

    /**
     * 获取工程监测闸门状态明细列表
     */
    List<IotScreenEngineeringGateDetailRespVO> getEngineeringGateStatusList(String stationId);

    /**
     * 获取工程监测设备（deviceType=9）压力值明细列表
     */
    List<IotScreenEngineeringPressValueRespVO> getEngineeringPressValueList(String stationId);

    /**
     * 获取工程监测流量计实时值列表（物模型 flowMeterWaterSpeedValue）
     */
    List<IotScreenEngineeringFlowValueRespVO> getEngineeringFlowValueList(String stationId);

    /**
     * 获取设备下拉（设备类型=6）
     */
    List<IotScreenDeviceOptionRespVO> getDeviceOptions(String stationId, Integer deviceType);

    /**
     * 获取设备启停统计（设备类型=3）
     */
    List<IotScreenDeviceStartStatRespVO> getDeviceStartStatList(String stationId);

    /**
     * 按泵站名称获取闸站全开统计（总数、开机台数）
     */
    IotScreenPumpStationRunningStatRespVO getPumpStationRunningStat(String pumpStationName);

    /**
     * 按泵站名称获取闸门详情（闸门名称、状态、有功功率、采集时间）
     */
    List<IotScreenPumpStationGateDetailRespVO> getPumpStationGateDetailList(String pumpStationName);

    /**
     * 按泵站名称获取闸站全开数统计（总数、开机台数）
     */
    IotScreenPumpStationRunningStatRespVO getPumpStationGateOpenStat(String pumpStationName);

    /**
     * 获取工情统计（所属站点 + 设备名称 + 时间范围）
     */
    List<IotScreenWorkConditionRespVO> getWorkConditionStatList(@Valid IotScreenWorkConditionListReqVO reqVO);

    IotScreenWorkConditionLatestRespVO getLatestWorkConditionStat(String stationId, Long deviceId);

    /**
     * 根据设备ID获取闸门电参快照（deviceType=7）
     */
    IotScreenGateMetricSnapshotRespVO getGateMetricSnapshot(Long deviceId);

    /**
     * 获取 streamWater 列表
     */
    List<IotScreenStreamWaterRespVO> getStreamWaterList(@Valid IotScreenStreamWaterListReqVO reqVO);

    /**
     * 按 zd_slss 设施类型查询分类数据
     *
     * @param values 设施类型值（支持多值）
     * @return 按字典 label 分组的数据
     */
    Map<String, List<Map<String, Object>>> getFacilityBySlss(List<String> values);

    /**
     * 按 zd_slss 设施类型查询分类数据
     *
     * @param values 设施类型值（支持多值）
     * @param areaId 可选，行政区划 id；坑塘（14）按 village_code 过滤以提升性能
     */
    Map<String, List<Map<String, Object>>> getFacilityBySlss(List<String> values, Long areaId);

    /**
     * 大屏行政区划树（默认仪征市下级：镇 → 村）。仅结构，不含面。
     *
     * @param rootId 根节点 id；空则默认 321081（仪征市）
     */
    List<IotScreenAreaNodeRespVO> getScreenAreaTree(Long rootId);

    /**
     * 大屏行政区划详情（含 gemo / gemoGeoJson）。点击区划后按需查询面，与一张图 /system/area/get 一致。
     *
     * @param id 区划 id
     */
    IotScreenAreaRespVO getScreenArea(Long id);

    /**
     * 坑塘（zd_slss=14）分页查询，支持区划与视口 bbox 过滤。
     */
    IotScreenPondPageRespVO getPondPage(IotScreenPondPageReqVO reqVO);

    /**
     * 坑塘详情（按主键 id）。返回中文字段 + 可渲染的结构化对象。
     */
    Map<String, Object> getPondDetail(Long id, Boolean includeGeometry);

    /**
     * 坑塘矢量瓦片（MVT）。按 Web Mercator 瓦片坐标 z/x/y 现查现切，支持多维过滤。
     *
     * @param z      缩放级别 0~22
     * @param x      瓦片列号
     * @param y      瓦片行号
     * @param reqVO  可选过滤：区划 / 名称 / 编号 / 位置 / 权属 / 缓冲区等
     * @return MVT 二进制；无数据时返回空数组
     */
    byte[] getPondMvtTile(int z, int x, int y, IotScreenPondMvtReqVO reqVO);

    /**
     * 坑塘筛选下拉选项（土地权属/资源类型/使用状态/资源性质/占用情况）
     */
    IotScreenPondFilterOptionsRespVO getPondFilterOptions();

    /**
     * 获取各视频站点监控设备数量
     */
    List<IotScreenVideoStationCountRespVO> getVideoCameraStationCountList();
}
