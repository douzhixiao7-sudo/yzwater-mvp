package com.sydigit.yzwater.module.iot.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sydigit.yzwater.framework.common.biz.system.dict.DictDataCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.dict.dto.DictDataRespDTO;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenDeviceOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenDeviceStartStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusListRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringDeviceStatusRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringGateSummaryRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringPressValueRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringFlowValueRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenEngineeringRuntimeStatRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenGateMetricSnapshotRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen.IotScreenMetricValueRespVO;
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
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.statistics.IotWeatherStationMapper;
import com.sydigit.yzwater.module.iot.dal.tdengine.IotDevicePropertyMapper;
import com.sydigit.yzwater.module.iot.enums.DictTypeConstants;
import com.sydigit.yzwater.module.iot.framework.tdengine.core.TDengineTableField;
import com.sydigit.yzwater.module.iot.service.device.property.IotDevicePropertyService;
import com.sydigit.yzwater.module.config.video.YzVideoScreenExcludeResolver;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoCameraDO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoRegionDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFloodPreventionMaterialWarehouseDO;
import com.sydigit.yzwater.module.dal.dataobject.geoBase.YzWaterFacilityBaseDO;
import com.sydigit.yzwater.module.dal.dataobject.irrigation.YzIrrigationDistrictDO;
import com.sydigit.yzwater.module.dal.dataobject.problem.YzProblemFeedbackDO;
import com.sydigit.yzwater.module.dal.dataobject.pump.YzPumpStationDO;
import com.sydigit.yzwater.module.dal.dataobject.reservoir.YzWaterReservoirDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzRiverChannelDO;
import com.sydigit.yzwater.module.dal.dataobject.rivers.YzSignboardBfDO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationSsDO;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFxWzMapper;
import com.sydigit.yzwater.module.dal.mysql.flood.YzFloodPreventionMaterialWarehouseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterFacilityBaseMapper;
import com.sydigit.yzwater.module.dal.mysql.geoBase.YzWaterReservoirMapper;
import com.sydigit.yzwater.module.dal.mysql.irrigation.YzIrrigationDistrictMapper;
import com.sydigit.yzwater.module.dal.mysql.pond.YzWaterPondMapper;
import com.sydigit.yzwater.module.controller.admin.vo.pond.WaterPondPageReqVO;
import com.sydigit.yzwater.module.dal.mysql.problem.YzProblemFeedbackMapper;
import com.sydigit.yzwater.module.dal.mysql.pump.YzPumpStationMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzRiverChannelMapper;
import com.sydigit.yzwater.module.dal.mysql.rivers.YzSignboardBfMapper;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoCameraMapper;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoRegionMapper;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherStationSsMapper;
import com.sydigit.yzwater.module.system.dal.dataobject.area.SystemAreaDO;
import com.sydigit.yzwater.module.system.service.area.SystemAreaService;
import com.sydigit.yzwater.module.system.service.area.dto.SystemAreaNode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.Date;

import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;
import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * IoT 大屏统计 Service 实现类
 */
@Service
@Validated
public class IotScreenStatisticsServiceImpl implements IotScreenStatisticsService {

    private static final Integer STREAM_WATER_DEVICE_TYPE = 6;
    private static final Integer START_STAT_DEVICE_TYPE = 3;
    private static final Integer ENGINEERING_TYPE_MAIN_UNIT = 3;
    private static final Integer GATE_DEVICE_TYPE = 7;
    private static final Integer PRESS_VALUE_DEVICE_TYPE = 9;
    private static final Integer STATION_HOST_DEVICE_TYPE_FIVE = 5;
    private static final Integer STATION_HOST_DEVICE_TYPE_EIGHT = 8;
    private static final Integer STATION_HOST_DEVICE_TYPE_TEN = 10;
    private static final Integer STATION_HOST_DEVICE_TYPE_THREE = 3;
    /**
     * 字典 iot_zd_sbzd 现行键值：
     * 0=十二圩，1=潘家河，2=沙河，7=金斗河，8=卧虎闸
     * 3=历史十二圩键值（兼容旧数据/测试）
     */
    private static final String STATION_SHI_ER_WEI = "0";
    private static final String STATION_PAN_JIA_HE = "1";
    private static final String STATION_SHA_HE = "2";
    private static final String STATION_JIN_DOU_HE = "7";
    private static final String STATION_WO_HU = "8";
    private static final String STATION_LEGACY_SHI_ER_WEI = "3";
    private static final String PUMP_STATION_NAME_JIN_DOU_HE = "金斗河闸站";
    private static final String PUMP_STATION_NAME_PAN_JIA_HE = "潘家河闸站";
    private static final String PUMP_STATION_NAME_SHI_ER_WEI = "十二圩闸站";
    private static final String PUMP_STATION_NAME_SHA_HE = "沙河闸站";
    private static final Integer PROVINCIAL_BACKBONE_YES = 1;
    private static final int RESERVOIR_SUMMARY_SCALE = 2;
    private static final int STREAM_WATER_LEVEL_SCALE = 2;
    private static final String DEFAULT_TIME_RANGE = "24h";
    private static final String MAIN_UNIT_RUNNING_IDENTIFIER = "isMainUnitRunning";
    private static final String IS_RUNNING_IDENTIFIER = "isRunning";
    private static final String RUN_IDENTIFIER = "run";
    private static final String STREAM_WATER_IDENTIFIER = "streamWater";
    private static final String STREAM_WATER_IDENTIFIER_STATION_THREE = "sw_pv";
    private static final String START_COUNT_IDENTIFIER = "startCount";
    private static final String START_DURATION_IDENTIFIER = "startDuration";
    private static final String RUN_HOURS_IDENTIFIER = "runHours";
    private static final String TOTAL_START_DURATION_IDENTIFIER = "totalStartDuration";
    private static final String START_COUNT_IDENTIFIER_STATION_THREE = "year_ci";
    private static final String TOTAL_START_DURATION_IDENTIFIER_STATION_THREE = "year_lj";
    private static final String START_DURATION_IDENTIFIER_STATION_THREE = "lj";
    private static final String SOFT_START_FAULT_IDENTIFIER = "isSoftStartFault";
    private static final String FAILURE_IDENTIFIER = "isFailure";
    private static final String GATE_FAILURE_IDENTIFIER = "isGateFailure";
    private static final String GATE_CLOSE_ALL_IDENTIFIER = "isGateCloseAll";
    private static final String GATE_OPEN_ALL_IDENTIFIER = "isGateOpenAll";
    private static final String GATE_CLOSE_ALL_IDENTIFIER_STATION_THREE = "zmqbj_sp2";
    private static final String GATE_OPEN_ALL_IDENTIFIER_STATION_THREE = "zmqbj_sp1";
    private static final String GATE_UP_IDENTIFIER = "isGateUp";
    private static final String GATE_DOWN_IDENTIFIER = "isGateDown";
    private static final String GATE_LOAD_WEIGHT_R_IDENTIFIER = "isGateLoadWeightR";
    private static final String GATE_LOAD_WEIGHT_L_IDENTIFIER = "isGateLoadWeightL";
    private static final String GATE_UP_IDENTIFIER_STATION_THREE = "zmqbj_rise";
    private static final String GATE_DOWN_IDENTIFIER_STATION_THREE = "zmqbj_drop";
    private static final String GATE_LOAD_WEIGHT_R_IDENTIFIER_STATION_THREE = "zmqbj_yfhfk_av";
    private static final String GATE_LOAD_WEIGHT_L_IDENTIFIER_STATION_THREE = "zmqbj_zfhfk_av";
    private static final String FLOODGATE_LEFT_LOAD_IDENTIFIER = "floodgateLeftLoad";
    private static final String GATE_LOAD_WEIGHT_B_IDENTIFIER = "isGateLoadWeightB";
    private static final String GATE_LOAD_WEIGHT_IDENTIFIER = "isGateLoadWeight";
    private static final String FLOODGATE_OPENING_IDENTIFIER = "floodgateOpening";
    private static final String GATE_RISING_IDENTIFIER = "isGateRising";
    private static final String GATE_LOWERING_IDENTIFIER = "isGateLowering";
    private static final String GATE_STOPPED_IDENTIFIER = "isGateStopped";
    /** 卧虎闸等站点：停止位标识 */
    private static final String GATE_STOP_IDENTIFIER = "isGateStop";
    private static final String GATE_POWER_ON_IDENTIFIER = "isGatePowerOn";
    /** 右荷重：旧站 isGateLoadWeightR / 卧虎闸 rightLoad */
    private static final List<String> GATE_LOAD_WEIGHT_R_IDENTIFIERS = List.of(
            GATE_LOAD_WEIGHT_R_IDENTIFIER, "rightLoad", "gateRightLoad");
    /** 左荷重：旧站 isGateLoadWeightL / 卧虎闸 leftLoad */
    private static final List<String> GATE_LOAD_WEIGHT_L_IDENTIFIERS = List.of(
            GATE_LOAD_WEIGHT_L_IDENTIFIER, "leftLoad", "gateLeftLoad", FLOODGATE_LEFT_LOAD_IDENTIFIER);
    private static final String PRESS_VALUE_IDENTIFIER = "pressValue";
    private static final String PRESS_VALUE_IDENTIFIER_STATION_THREE = "yyp";
    /** 扬压力：旧站 pressValue / 卧虎闸 pressureValue / 十二圩 yyp */
    private static final List<String> PRESS_VALUE_IDENTIFIERS = List.of(
            PRESS_VALUE_IDENTIFIER, "pressureValue", PRESS_VALUE_IDENTIFIER_STATION_THREE);
    /** 卧虎闸等流速仪实时流量物模型标识 */
    private static final String FLOW_METER_VALUE_IDENTIFIER = "flowMeterWaterSpeedValue";
    private static final List<String> FLOW_VALUE_IDENTIFIERS = List.of(
            FLOW_METER_VALUE_IDENTIFIER, "flowRate", "flow", "ssll");
    private static final int FLOW_VALUE_SCALE = 3;
    private static final String VIDEO_ONLINE_STATUS = "1";
    private static final String WEATHER_SURF_HOURS_KEY = "SurfEle_Hours";
    private static final String WEATHER_DAILY_RAINFALL_KEY = "PRE_24h";
    private static final String WEATHER_HOURLY_RAINFALL_KEY = "PRE_1h";
    private static final int WEATHER_BUSINESS_DAY_START_HOUR = 8;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int HISTORY_LOOKBACK_YEARS = 30;
    private static final List<String> ACTIVE_POWER_IDENTIFIERS = List.of("activePower", "activePowerKw", "kw", "p");
    private static final List<String> REACTIVE_POWER_IDENTIFIERS = List.of("reactivePower", "reactivePowerKvar",
            "kvar", "q");
    private static final List<String> POWER_FACTOR_IDENTIFIERS = List.of("powerFactor", "pf", "PF");
    private static final List<String> FREQUENCY_IDENTIFIERS = List.of("frequency", "hz", "f");
    private static final List<String> AB_VOLTAGE_IDENTIFIERS = List.of(
            "u_ab", "uAb", "uab", "abVoltage", "uabVoltage");
    private static final List<String> BC_VOLTAGE_IDENTIFIERS = List.of(
            "u_bc", "uBc", "ubc", "bcVoltage", "ubcVoltage");
    private static final List<String> CA_VOLTAGE_IDENTIFIERS = List.of(
            "u_ca", "uCa", "uca", "uAc", "uac", "caVoltage", "ucaVoltage");
    private static final List<String> A_CURRENT_IDENTIFIERS = List.of(
            "i_a", "ia", "iA", "aCurrent", "iaCurrent");
    private static final List<String> B_CURRENT_IDENTIFIERS = List.of(
            "i_b", "ib", "iB", "bCurrent", "ibCurrent");
    private static final List<String> C_CURRENT_IDENTIFIERS = List.of(
            "i_c", "ic", "iC", "cCurrent", "icCurrent");
    private static final List<String> GATE_SNAPSHOT_AB_VOLTAGE_IDENTIFIERS = List.of("u_ab", "uab", "uAb");
    private static final List<String> GATE_SNAPSHOT_BC_VOLTAGE_IDENTIFIERS = List.of("u_bc", "ubc", "uBc");
    /** 含卧虎闸 uAc（与常见 uCa 同义） */
    private static final List<String> GATE_SNAPSHOT_CA_VOLTAGE_IDENTIFIERS = List.of(
            "u_ca", "uca", "uCa", "uAc", "uac");
    private static final List<String> GATE_SNAPSHOT_A_CURRENT_IDENTIFIERS = List.of("i_a", "ia", "iA");
    private static final List<String> GATE_SNAPSHOT_B_CURRENT_IDENTIFIERS = List.of("i_b", "ib", "iB");
    private static final List<String> GATE_SNAPSHOT_C_CURRENT_IDENTIFIERS = List.of("i_c", "ic", "iC");
    private static final List<String> GATE_SNAPSHOT_ACTIVE_POWER_IDENTIFIERS = List.of("p", "P", "activePower");
    private static final List<String> GATE_SNAPSHOT_REACTIVE_POWER_IDENTIFIERS = List.of("q", "Q", "reactivePower");
    private static final List<String> A_STATOR_TEMP1_IDENTIFIERS = List.of(
            "aStatorTemp1", "a_stator_temp1");
    private static final List<String> B_STATOR_TEMP1_IDENTIFIERS = List.of(
            "bStatorTemp1", "b_stator_temp1");
    private static final List<String> C_STATOR_TEMP1_IDENTIFIERS = List.of(
            "cStatorTemp1", "c_stator_temp1");
    private static final List<String> A_STATOR_TEMP2_IDENTIFIERS = List.of(
            "aStatorTemp2", "a_stator_temp2", "stator_temp1");
    private static final List<String> B_STATOR_TEMP2_IDENTIFIERS = List.of(
            "bStatorTemp2", "b_stator_temp2");
    private static final List<String> C_STATOR_TEMP2_IDENTIFIERS = List.of(
            "cStatorTemp2", "c_stator_temp2");
    private static final List<String> WATER_LEVEL_IDENTIFIERS = List.of(
            "streamWater", "waterVal", "sw_pv", "waterLevel");
    private static final List<String> PRESS_VALUE_WORK_IDENTIFIERS = List.of(
            "pressValue", "pressureValue", "yyp");
    private static final List<String> GATE_OPEN_ALL_WORK_IDENTIFIERS = List.of(
            "isGateOpenAll", "zmqbj_sp1");
    private static final List<String> GATE_CLOSE_ALL_WORK_IDENTIFIERS = List.of(
            "isGateCloseAll", "zmqbj_sp2");
    private static final List<String> GATE_UP_WORK_IDENTIFIERS = List.of(
            "isGateUp", "isGateRising", "zmqbj_rise");
    private static final List<String> GATE_DOWN_WORK_IDENTIFIERS = List.of(
            "isGateDown", "isGateLowering", "zmqbj_drop");
    private static final List<String> GATE_FAILURE_WORK_IDENTIFIERS = List.of(
            "isGateFailure");
    private static final List<String> GATE_POWER_ON_WORK_IDENTIFIERS = List.of(
            "isGatePowerOn");
    private static final List<String> FLOODGATE_OPENING_WORK_IDENTIFIERS = List.of(
            "floodgateOpening");
    private static final List<List<String>> WORK_CONDITION_IDENTIFIER_GROUPS = List.of(
            ACTIVE_POWER_IDENTIFIERS, REACTIVE_POWER_IDENTIFIERS, POWER_FACTOR_IDENTIFIERS, FREQUENCY_IDENTIFIERS,
            AB_VOLTAGE_IDENTIFIERS, BC_VOLTAGE_IDENTIFIERS, CA_VOLTAGE_IDENTIFIERS, A_CURRENT_IDENTIFIERS,
            B_CURRENT_IDENTIFIERS, C_CURRENT_IDENTIFIERS,
            A_STATOR_TEMP1_IDENTIFIERS, B_STATOR_TEMP1_IDENTIFIERS, C_STATOR_TEMP1_IDENTIFIERS,
            A_STATOR_TEMP2_IDENTIFIERS, B_STATOR_TEMP2_IDENTIFIERS, C_STATOR_TEMP2_IDENTIFIERS,
            GATE_OPEN_ALL_WORK_IDENTIFIERS, GATE_CLOSE_ALL_WORK_IDENTIFIERS, GATE_UP_WORK_IDENTIFIERS,
            GATE_DOWN_WORK_IDENTIFIERS, GATE_FAILURE_WORK_IDENTIFIERS, GATE_POWER_ON_WORK_IDENTIFIERS,
            FLOODGATE_OPENING_WORK_IDENTIFIERS, WATER_LEVEL_IDENTIFIERS, PRESS_VALUE_WORK_IDENTIFIERS);
    private static final String DICT_TYPE_SLSS = "zd_slss";
    private static final String DICT_TYPE_PUMP_STATION_TYPE = "zd_bzlx";
    /** 装机功率：数据库存 MW，对外展示与泵站管理接口一致为 KW */
    private static final BigDecimal PUMP_STATION_KW_PER_MW = BigDecimal.valueOf(1000);
    private static final String DICT_TYPE_MATERIAL_UNIT = "zd_wzdw";
    private static final String ECOLOGY_TYPE_STHD = "sthd";
    private static final String ECOLOGY_TYPE_BFSTHD = "bfsthd";
    private static final String ECOLOGY_TYPE_FSTHD = "fsthd";
    private static final Set<String> SUPPORTED_SLSS_VALUES = Set.of(
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14");
    private static final int POND_DEFAULT_PAGE_NO = 1;
    private static final int POND_DEFAULT_PAGE_SIZE = 500;
    private static final int POND_MAX_PAGE_SIZE = 1000;
    private static final int POND_MVT_MIN_ZOOM = 0;
    private static final int POND_MVT_MAX_ZOOM = 22;
    /** 大屏区划树默认根：仪征市 */
    private static final Long SCREEN_AREA_DEFAULT_ROOT_ID = 321081L;
    private static final List<String> WATER_SYSTEM_RIVER_NAMES = List.of(
            "长江", "胥浦河", "仪扬河", "龙河", "沿山河", "乌塔沟", "秦栏河", "公道引水河");

    private static final Map<Character, Integer> CHINESE_NUMBER_MAP = Map.ofEntries(
            Map.entry('\u96f6', 0), Map.entry('\u3007', 0), Map.entry('\u4e00', 1), Map.entry('\u4e8c', 2),
            Map.entry('\u4e24', 2), Map.entry('\u4e09', 3), Map.entry('\u56db', 4), Map.entry('\u4e94', 5),
            Map.entry('\u516d', 6), Map.entry('\u4e03', 7), Map.entry('\u516b', 8), Map.entry('\u4e5d', 9));
    private static final Map<Character, Integer> CHINESE_NUMBER_UNIT_MAP = Map.ofEntries(
            Map.entry('\u5341', 10), Map.entry('\u767e', 100), Map.entry('\u5343', 1000), Map.entry('\u4e07', 10000));

    @Resource
    private DictDataCommonApi dictDataCommonApi;
    @Resource
    private IotDeviceMapper deviceMapper;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotDevicePropertyMapper devicePropertyMapper;
    @Resource
    private YzWaterReservoirMapper waterReservoirMapper;
    @Resource
    private YzRiverChannelMapper riverChannelMapper;
    @Resource
    private YzWaterFacilityBaseMapper waterFacilityBaseMapper;
    @Resource
    private YzPumpStationMapper pumpStationMapper;
    @Resource
    private YzFloodPreventionMaterialWarehouseMapper floodWarehouseMapper;
    @Resource
    private YzFxWzMapper fxWzMapper;
    @Resource
    private YzIrrigationDistrictMapper irrigationDistrictMapper;
    @Resource
    private YzWaterPondMapper waterPondMapper;
    @Resource
    private YzSignboardBfMapper signboardBfMapper;
    @Resource
    private YzProblemFeedbackMapper problemFeedbackMapper;
    @Resource
    private IotWeatherStationMapper weatherStationMapper;
    @Resource
    private YzWeatherStationSsMapper weatherStationSsMapper;
    @Resource
    private YzVideoCameraMapper yzVideoCameraMapper;
    @Resource
    private YzVideoRegionMapper yzVideoRegionMapper;
    @Resource
    private YzVideoScreenExcludeResolver yzVideoScreenExcludeResolver;
    @Resource
    private SystemAreaService systemAreaService;

    @Override
    public IotScreenOverviewCountRespVO getOverviewCount() {
        IotScreenOverviewCountRespVO respVO = new IotScreenOverviewCountRespVO();
        respVO.setReservoirCount(defaultCount(waterReservoirMapper.selectCount()));
        respVO.setPumpStationCount(defaultCount(pumpStationMapper.selectCount()));
        respVO.setProvincialBackboneRiverCount(defaultCount(
                riverChannelMapper.selectCount(YzRiverChannelDO::getIsProvincialBackbone, PROVINCIAL_BACKBONE_YES)));
        return respVO;
    }

    @Override
    public IotScreenReservoirSummaryRespVO getReservoirSummary() {
        Map<String, Object> summaryMap = waterReservoirMapper.selectAllCapacityAreaSummary();
        IotScreenReservoirSummaryRespVO respVO = new IotScreenReservoirSummaryRespVO();
        respVO.setTotalCapacity(roundToScale(getMapBigDecimal(summaryMap, "total_capacity")));
        respVO.setCatchmentArea(roundToScale(getMapBigDecimal(summaryMap, "catchment_area")));
        respVO.setIrrigationArea(roundToScale(getMapBigDecimal(summaryMap, "irrigation_area")));
        return respVO;
    }

    @Override
    public List<IotScreenReservoirCapacityRespVO> getReservoirCapacityList() {
        List<YzWaterReservoirDO> reservoirList = waterReservoirMapper.selectList(
                new LambdaQueryWrapper<YzWaterReservoirDO>()
                        .select(YzWaterReservoirDO::getId,
                                YzWaterReservoirDO::getReservoirName,
                                YzWaterReservoirDO::getTotalCapacity,
                                YzWaterReservoirDO::getUpdateTime)
                        .orderByDesc(YzWaterReservoirDO::getUpdateTime)
                        .orderByDesc(YzWaterReservoirDO::getId));
        if (CollUtil.isEmpty(reservoirList)) {
            return Collections.emptyList();
        }
        return convertList(reservoirList, item -> {
            IotScreenReservoirCapacityRespVO respVO = new IotScreenReservoirCapacityRespVO();
            respVO.setReservoirName(item.getReservoirName());
            respVO.setTotalCapacity(item.getTotalCapacity());
            return respVO;
        });
    }

    @Override
    public List<IotScreenStationOptionRespVO> getStationOptions() {
        List<DictDataRespDTO> dictDataList = dictDataCommonApi.getDictDataList(DictTypeConstants.DEVICE_SITE);
        if (CollUtil.isEmpty(dictDataList)) {
            return Collections.emptyList();
        }
        return dictDataList.stream()
                .filter(dictData -> Objects.equals(dictData.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                .filter(dictData -> StrUtil.isNotBlank(dictData.getLabel()) && StrUtil.isNotBlank(dictData.getValue()))
                .map(dictData -> {
                    IotScreenStationOptionRespVO respVO = new IotScreenStationOptionRespVO();
                    respVO.setLabel(dictData.getLabel());
                    respVO.setValue(dictData.getValue());
                    return respVO;
                })
                .sorted(Comparator.comparing(IotScreenStationOptionRespVO::getLabel))
                .collect(Collectors.toList());
    }

    @Override
    public List<IotScreenStationHostStatusRespVO> getStationHostStatusList() {
        List<DictDataRespDTO> dictDataList = dictDataCommonApi.getDictDataList(DictTypeConstants.DEVICE_SITE);
        if (CollUtil.isEmpty(dictDataList)) {
            return Collections.emptyList();
        }
        return dictDataList.stream()
                .filter(dictData -> Objects.equals(dictData.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                .filter(dictData -> StrUtil.isNotBlank(dictData.getLabel()) && StrUtil.isNotBlank(dictData.getValue()))
                .map(this::buildStationHostStatus)
                .sorted(Comparator.comparing(IotScreenStationHostStatusRespVO::getStationName))
                .collect(Collectors.toList());
    }

    @Override
    public List<IotScreenPumpStationHostDetailRespVO> getPumpStationHostDetailList(String pumpStationName) {
        String normalizedPumpStationName = StrUtil.trimToNull(pumpStationName);
        if (normalizedPumpStationName == null) {
            return Collections.emptyList();
        }
        if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_SHI_ER_WEI)) {
            return buildPumpStationHostDetails(STATION_SHI_ER_WEI, STATION_HOST_DEVICE_TYPE_THREE,
                    STATION_HOST_DEVICE_TYPE_THREE, RUN_IDENTIFIER);
        }
        if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_JIN_DOU_HE)) {
            return buildPumpStationHostDetails(STATION_JIN_DOU_HE, ENGINEERING_TYPE_MAIN_UNIT,
                    STATION_HOST_DEVICE_TYPE_FIVE, IS_RUNNING_IDENTIFIER);
        }
        if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_PAN_JIA_HE)) {
            return buildPumpStationHostDetails(STATION_PAN_JIA_HE, STATION_HOST_DEVICE_TYPE_EIGHT,
                    STATION_HOST_DEVICE_TYPE_EIGHT, IS_RUNNING_IDENTIFIER);
        }
        if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_SHA_HE)) {
            return buildPumpStationHostDetails(STATION_SHA_HE, STATION_HOST_DEVICE_TYPE_FIVE,
                    STATION_HOST_DEVICE_TYPE_FIVE, MAIN_UNIT_RUNNING_IDENTIFIER);
        }
        return Collections.emptyList();
    }

    @Override
    public IotScreenEngineeringDeviceStatusRespVO getEngineeringDeviceStatusSummary(String stationId) {
        String normalizedStationId = StrUtil.trimToEmpty(stationId);
        Map<String, String> stationLabelMap = buildStationLabelMap();
        IotScreenStationHostStatusRespVO hostStatusRespVO = getStationHostStatusList().stream()
                .filter(item -> StrUtil.equals(item.getStationId(), normalizedStationId))
                .findFirst()
                .orElse(null);
        long runningCount = hostStatusRespVO == null ? 0L : defaultCount(hostStatusRespVO.getRunningCount());
        long closedCount = hostStatusRespVO == null ? 0L : defaultCount(hostStatusRespVO.getStopCount());

        IotScreenEngineeringDeviceStatusRespVO respVO = new IotScreenEngineeringDeviceStatusRespVO();
        respVO.setStationId(normalizedStationId);
        respVO.setStationName(StrUtil.blankToDefault(stationLabelMap.get(normalizedStationId), normalizedStationId));
        respVO.setRunningCount(runningCount);
        respVO.setClosedCount(closedCount);
        respVO.setTotalCount(runningCount + closedCount);
        return respVO;
    }

    @Override
    public List<IotScreenEngineeringDeviceStatusListRespVO> getEngineeringDeviceStatusList(
            @Valid IotScreenEngineeringDeviceStatusListReqVO reqVO) {
        String stationId = StrUtil.trimToEmpty(reqVO.getStationId());
        Integer deviceType = reqVO.getDeviceType();
        if (!Objects.equals(deviceType, GATE_DEVICE_TYPE) && !Objects.equals(deviceType, ENGINEERING_TYPE_MAIN_UNIT)) {
            throw invalidParamException("设备类型仅支持 7 或 3");
        }

        List<IotDeviceDO> deviceList = queryDevices(stationId, deviceType, null);
        if (CollUtil.isEmpty(deviceList)) {
            return Collections.emptyList();
        }
        String deviceTypeName = resolveDeviceTypeName(deviceType);

        if (Objects.equals(deviceType, GATE_DEVICE_TYPE)) {
            List<IotScreenEngineeringDeviceStatusListRespVO> result =
                    buildGateDeviceStatusList(deviceList, deviceTypeName, reqVO);
            sortEngineeringDeviceStatusList(result);
            return result;
        }
        List<IotScreenEngineeringDeviceStatusListRespVO> result =
                buildMainUnitStatusList(stationId, deviceList, deviceTypeName, reqVO.getIsRunning());
        sortEngineeringDeviceStatusList(result);
        return result;
    }

    @Override
    public List<IotScreenEngineeringRuntimeStatRespVO> getEngineeringRuntimeStatSummary(String stationId) {
        String normalizedStationId = StrUtil.trimToEmpty(stationId);
        Map<String, String> stationLabelMap = buildStationLabelMap();
        String stationName = StrUtil.blankToDefault(stationLabelMap.get(normalizedStationId), normalizedStationId);
        List<IotDeviceDO> hostDeviceList = queryDevices(normalizedStationId, START_STAT_DEVICE_TYPE, null);
        if (CollUtil.isEmpty(hostDeviceList)) {
            return Collections.emptyList();
        }

        if (isShiErWeiStation(normalizedStationId)) {
            List<IotScreenEngineeringRuntimeStatRespVO> result = new ArrayList<>();
            for (IotDeviceDO hostDevice : hostDeviceList) {
                Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(hostDevice.getId());
                result.add(buildRuntimeStatRespVO(
                        normalizedStationId,
                        stationName,
                        hostDevice.getId(),
                        hostDevice.getDeviceName(),
                        toLongValue(extractPropertyValue(latestProperties, START_COUNT_IDENTIFIER_STATION_THREE)),
                        toBigDecimalValue(extractPropertyValue(latestProperties,
                                TOTAL_START_DURATION_IDENTIFIER_STATION_THREE)),
                        toBigDecimalValue(extractPropertyValue(latestProperties,
                                START_DURATION_IDENTIFIER_STATION_THREE)),
                        extractPropertyUpdateTime(latestProperties,
                                List.of(START_DURATION_IDENTIFIER_STATION_THREE))));
            }
            sortEngineeringRuntimeStatList(result);
            return result;
        }

        if (STATION_SHA_HE.equals(normalizedStationId) || STATION_JIN_DOU_HE.equals(normalizedStationId)) {
            List<IotScreenEngineeringRuntimeStatRespVO> result = new ArrayList<>();
            for (IotDeviceDO hostDevice : hostDeviceList) {
                Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(hostDevice.getId());
                result.add(buildRuntimeStatRespVO(
                        normalizedStationId,
                        stationName,
                        hostDevice.getId(),
                        hostDevice.getDeviceName(),
                        toLongValue(extractPropertyValue(latestProperties, START_COUNT_IDENTIFIER)),
                        toBigDecimalValue(extractPropertyValue(latestProperties, TOTAL_START_DURATION_IDENTIFIER)),
                        toBigDecimalValue(extractPropertyValue(latestProperties, START_DURATION_IDENTIFIER)),
                        extractPropertyUpdateTime(latestProperties, List.of(START_DURATION_IDENTIFIER))));
            }
            sortEngineeringRuntimeStatList(result);
            return result;
        }

        if (STATION_PAN_JIA_HE.equals(normalizedStationId)) {
            List<IotDeviceDO> pumpDeviceList = queryDevices(normalizedStationId, STATION_HOST_DEVICE_TYPE_EIGHT, null);
            List<IotScreenEngineeringRuntimeStatRespVO> result = new ArrayList<>();
            for (IotDeviceDO hostDevice : hostDeviceList) {
                Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(hostDevice.getId());
                BigDecimal runHours = toBigDecimalValue(extractPropertyValue(latestProperties, RUN_HOURS_IDENTIFIER));
                long startCount = 0L;
                IotDeviceDO matchedPumpDevice = matchPumpDeviceByHostNamePrefix(hostDevice.getDeviceName(), pumpDeviceList);
                if (matchedPumpDevice != null) {
                    Map<String, IotDevicePropertyDO> pumpLatestProperties = devicePropertyService.getLatestDeviceProperties(matchedPumpDevice.getId());
                    if (isRunningStatus(extractPropertyValue(pumpLatestProperties, IS_RUNNING_IDENTIFIER))) {
                        startCount = 1L;
                    }
                }
                result.add(buildRuntimeStatRespVO(
                        normalizedStationId,
                        stationName,
                        hostDevice.getId(),
                        hostDevice.getDeviceName(),
                        startCount,
                        runHours,
                        runHours,
                        extractPropertyUpdateTime(latestProperties, List.of(RUN_HOURS_IDENTIFIER))));
            }
            sortEngineeringRuntimeStatList(result);
            return result;
        }

        return Collections.emptyList();
    }

    @Override
    public IotScreenEngineeringGateSummaryRespVO getEngineeringGateStatusSummary(String stationId) {
        String normalizedStationId = StrUtil.trimToEmpty(stationId);
        Map<String, String> stationLabelMap = buildStationLabelMap();
        String stationName = StrUtil.blankToDefault(stationLabelMap.get(normalizedStationId), normalizedStationId);
        List<IotDeviceDO> gateDeviceList = queryDevices(normalizedStationId, GATE_DEVICE_TYPE, null);
        if (CollUtil.isEmpty(gateDeviceList)) {
            return buildGateSummaryRespVO(normalizedStationId, stationName, 0L, 0L, 0L);
        }

        long gateCloseAllCount = 0L;
        long gateOpenAllCount = 0L;
        String gateCloseAllIdentifier = resolveGateCloseAllIdentifier(normalizedStationId);
        String gateOpenAllIdentifier = resolveGateOpenAllIdentifier(normalizedStationId);
        for (IotDeviceDO gateDevice : gateDeviceList) {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(gateDevice.getId());
            if (isRunningStatus(extractPropertyValue(latestProperties, gateCloseAllIdentifier))) {
                gateCloseAllCount++;
            }
            if (isRunningStatus(extractPropertyValue(latestProperties, gateOpenAllIdentifier))) {
                gateOpenAllCount++;
            }
        }
        return buildGateSummaryRespVO(normalizedStationId, stationName, (long) gateDeviceList.size(),
                gateCloseAllCount, gateOpenAllCount);
    }

    @Override
    public List<IotScreenEngineeringGateDetailRespVO> getEngineeringGateStatusList(String stationId) {
        String normalizedStationId = StrUtil.trimToEmpty(stationId);
        Map<String, String> stationLabelMap = buildStationLabelMap();
        String stationName = StrUtil.blankToDefault(stationLabelMap.get(normalizedStationId), normalizedStationId);
        List<IotDeviceDO> gateDeviceList = queryDevices(normalizedStationId, GATE_DEVICE_TYPE, null);
        if (CollUtil.isEmpty(gateDeviceList)) {
            return Collections.emptyList();
        }

        List<IotScreenEngineeringGateDetailRespVO> result = convertList(gateDeviceList, gateDevice -> {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(gateDevice.getId());
            String gateCloseAllIdentifier = resolveGateCloseAllIdentifier(normalizedStationId);
            String gateOpenAllIdentifier = resolveGateOpenAllIdentifier(normalizedStationId);
            String gateUpIdentifier = resolveGateUpIdentifierForStatusList(normalizedStationId);
            String gateDownIdentifier = resolveGateDownIdentifierForStatusList(normalizedStationId);
            List<String> gateLoadWeightRIdentifiers = resolveGateLoadWeightRIdentifiersForStatusList(normalizedStationId);
            List<String> gateLoadWeightLIdentifiers = resolveGateLoadWeightLIdentifiersForStatusList(normalizedStationId);
            IotScreenEngineeringGateDetailRespVO respVO = new IotScreenEngineeringGateDetailRespVO();
            respVO.setStationId(normalizedStationId);
            respVO.setStationName(stationName);
            respVO.setDeviceId(gateDevice.getId());
            respVO.setDeviceName(gateDevice.getDeviceName());
            respVO.setIsGateFailure(extractPropertyValue(latestProperties, GATE_FAILURE_IDENTIFIER));
            respVO.setIsGateCloseAll(extractPropertyValue(latestProperties, gateCloseAllIdentifier));
            respVO.setIsGateOpenAll(extractPropertyValue(latestProperties, gateOpenAllIdentifier));
            respVO.setIsGateUp(extractPropertyValue(latestProperties, gateUpIdentifier));
            respVO.setIsGateDown(extractPropertyValue(latestProperties, gateDownIdentifier));
            respVO.setIsGateStop(extractPropertyValue(latestProperties,
                    List.of(GATE_STOP_IDENTIFIER, GATE_STOPPED_IDENTIFIER)));
            respVO.setIa(extractPropertyValue(latestProperties, A_CURRENT_IDENTIFIERS));
            respVO.setIb(extractPropertyValue(latestProperties, B_CURRENT_IDENTIFIERS));
            respVO.setIc(extractPropertyValue(latestProperties, C_CURRENT_IDENTIFIERS));
            respVO.setUab(extractPropertyValue(latestProperties, AB_VOLTAGE_IDENTIFIERS));
            respVO.setUbc(extractPropertyValue(latestProperties, BC_VOLTAGE_IDENTIFIERS));
            respVO.setUca(extractPropertyValue(latestProperties, CA_VOLTAGE_IDENTIFIERS));
            respVO.setP(extractPropertyValue(latestProperties, GATE_SNAPSHOT_ACTIVE_POWER_IDENTIFIERS));
            respVO.setQ(extractPropertyValue(latestProperties, GATE_SNAPSHOT_REACTIVE_POWER_IDENTIFIERS));
            respVO.setPf(extractPropertyValue(latestProperties, POWER_FACTOR_IDENTIFIERS));
            Object gateLoadWeightR = extractPropertyValue(latestProperties, gateLoadWeightRIdentifiers);
            Object gateLoadWeightL = extractPropertyValue(latestProperties, gateLoadWeightLIdentifiers);
            Object floodgateLeftLoad = extractPropertyValue(latestProperties, FLOODGATE_LEFT_LOAD_IDENTIFIER);
            if (floodgateLeftLoad == null) {
                floodgateLeftLoad = gateLoadWeightL;
            }
            respVO.setIsGateLoadWeightR(gateLoadWeightR);
            respVO.setIsGateLoadWeightL(gateLoadWeightL);
            respVO.setFloodgateLeftLoad(floodgateLeftLoad);
            // isGateLoadWeightB / isGateLoadWeight 保持原字段并持续返回（不做 station 分支限制）
            respVO.setIsGateLoadWeightB(extractPropertyValue(latestProperties, GATE_LOAD_WEIGHT_B_IDENTIFIER));
            respVO.setIsGateLoadWeight(extractPropertyValue(latestProperties, GATE_LOAD_WEIGHT_IDENTIFIER));
            if (STATION_PAN_JIA_HE.equals(normalizedStationId) || STATION_SHA_HE.equals(normalizedStationId)) {
                respVO.setFloodgateOpening(extractPropertyValue(latestProperties, FLOODGATE_OPENING_IDENTIFIER));
            }
            LocalDateTime collectTime = resolveLatestPropertyUpdateTime(latestProperties);
            if (collectTime != null) {
                respVO.setCollectTime(collectTime.format(TIME_FORMATTER));
            }
            return respVO;
        });
        sortEngineeringGateStatusList(result);
        return result;
    }

    @Override
    public List<IotScreenEngineeringPressValueRespVO> getEngineeringPressValueList(String stationId) {
        String normalizedStationId = StrUtil.trimToEmpty(stationId);
        List<IotDeviceDO> deviceList = queryDevices(normalizedStationId, PRESS_VALUE_DEVICE_TYPE, null);
        if (CollUtil.isEmpty(deviceList)) {
            return Collections.emptyList();
        }
        String pressValueIdentifier = isShiErWeiStation(normalizedStationId)
                ? PRESS_VALUE_IDENTIFIER_STATION_THREE
                : null;
        List<String> pressIdentifiers = pressValueIdentifier == null
                ? PRESS_VALUE_IDENTIFIERS
                : List.of(pressValueIdentifier, PRESS_VALUE_IDENTIFIER, "pressureValue");

        return convertList(deviceList, device -> {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(device.getId());
            IotScreenEngineeringPressValueRespVO respVO = new IotScreenEngineeringPressValueRespVO();
            respVO.setDeviceId(device.getId());
            respVO.setDeviceName(device.getDeviceName());
            respVO.setPressValue(roundPressValue(extractPropertyValue(latestProperties, pressIdentifiers)));
            LocalDateTime collectTime = extractPropertyUpdateTime(latestProperties, pressIdentifiers);
            if (collectTime == null) {
                collectTime = resolveLatestPropertyUpdateTime(latestProperties);
            }
            if (collectTime != null) {
                respVO.setCollectTime(collectTime.format(TIME_FORMATTER));
            }
            return respVO;
        });
    }

    @Override
    public List<IotScreenEngineeringFlowValueRespVO> getEngineeringFlowValueList(String stationId) {
        String normalizedStationId = StrUtil.trimToEmpty(stationId);
        Integer flowDeviceType = resolveFlowDeviceType();
        List<IotDeviceDO> deviceList = flowDeviceType == null
                ? queryDevices(normalizedStationId, null, null)
                : queryDevices(normalizedStationId, flowDeviceType, null);
        if (CollUtil.isEmpty(deviceList) && flowDeviceType != null) {
            // 字典类型未挂到设备时，回退全站设备再按名称/属性识别
            deviceList = queryDevices(normalizedStationId, null, null);
        }
        if (CollUtil.isEmpty(deviceList)) {
            return Collections.emptyList();
        }

        List<IotScreenEngineeringFlowValueRespVO> result = new ArrayList<>();
        for (IotDeviceDO device : deviceList) {
            if (device == null || StrUtil.isBlank(device.getDeviceName())) {
                continue;
            }
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(device.getId());
            Object flowValue = extractPropertyValue(latestProperties, FLOW_VALUE_IDENTIFIERS);
            boolean nameLooksLikeFlow = isFlowDeviceName(device.getDeviceName());
            boolean typeLooksLikeFlow = flowDeviceType != null && Objects.equals(device.getDeviceType(), flowDeviceType);
            if (flowValue == null && !nameLooksLikeFlow && !typeLooksLikeFlow) {
                continue;
            }
            IotScreenEngineeringFlowValueRespVO respVO = new IotScreenEngineeringFlowValueRespVO();
            respVO.setDeviceId(device.getId());
            respVO.setDeviceName(device.getDeviceName());
            respVO.setDeviceType(device.getDeviceType());
            respVO.setDeviceTypeName(resolveDeviceTypeName(device.getDeviceType()));
            respVO.setFlowValue(roundFlowValue(flowValue));
            LocalDateTime updateTime = extractPropertyUpdateTime(latestProperties, FLOW_VALUE_IDENTIFIERS);
            if (updateTime != null) {
                respVO.setTime(updateTime.format(TIME_FORMATTER));
            }
            result.add(respVO);
        }
        result.sort(Comparator.comparing(IotScreenEngineeringFlowValueRespVO::getDeviceName,
                Comparator.nullsLast(String::compareTo)));
        return result;
    }

    @Override
    public List<IotScreenDeviceOptionRespVO> getDeviceOptions(String stationId, Integer deviceType) {
        // 兼容大屏：不传 deviceType 默认水位计(6)；传 -1 表示该站全部类型（工情页）
        Integer queryDeviceType;
        if (deviceType == null) {
            queryDeviceType = STREAM_WATER_DEVICE_TYPE;
        } else if (deviceType == -1) {
            queryDeviceType = null;
        } else {
            queryDeviceType = deviceType;
        }
        List<IotDeviceDO> deviceList = queryDevices(stationId, queryDeviceType, null);
        if (CollUtil.isEmpty(deviceList)) {
            return Collections.emptyList();
        }

        Map<String, IotScreenDeviceOptionRespVO> optionMap = new LinkedHashMap<>();
        deviceList.forEach(device -> {
            String deviceName = StrUtil.trimToNull(device.getDeviceName());
            if (deviceName == null) {
                return;
            }
            // 同名设备保留首次出现；全类型场景下附带类型信息供前端筛选
            optionMap.computeIfAbsent(deviceName, name -> {
                IotScreenDeviceOptionRespVO respVO = new IotScreenDeviceOptionRespVO();
                respVO.setLabel(name);
                respVO.setValue(name);
                respVO.setDeviceType(device.getDeviceType());
                respVO.setDeviceTypeName(resolveDeviceTypeName(device.getDeviceType()));
                return respVO;
            });
        });
        return new ArrayList<>(optionMap.values());
    }

    @Override
    public List<IotScreenDeviceStartStatRespVO> getDeviceStartStatList(String stationId) {
        List<IotDeviceDO> deviceList = queryDevices(stationId, START_STAT_DEVICE_TYPE, null);
        if (CollUtil.isEmpty(deviceList)) {
            return Collections.emptyList();
        }

        return convertList(deviceList, device -> {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(device.getId());
            IotScreenDeviceStartStatRespVO respVO = new IotScreenDeviceStartStatRespVO();
            respVO.setDeviceId(device.getId());
            respVO.setDeviceName(device.getDeviceName());
            respVO.setStartCount(extractPropertyValue(latestProperties, START_COUNT_IDENTIFIER));
            respVO.setTotalStartDuration(extractPropertyValue(latestProperties, TOTAL_START_DURATION_IDENTIFIER));
            return respVO;
        });
    }

    @Override
    public IotScreenPumpStationRunningStatRespVO getPumpStationRunningStat(String pumpStationName) {
        String normalizedPumpStationName = StrUtil.trimToEmpty(pumpStationName);
        IotScreenPumpStationRunningStatRespVO respVO = new IotScreenPumpStationRunningStatRespVO();
        respVO.setPumpStationName(normalizedPumpStationName);
        respVO.setTotalCount(0L);
        respVO.setRunningCount(0L);

        String stationId;
        Integer deviceType;
        String statusIdentifier;
        switch (normalizedPumpStationName) {
            case PUMP_STATION_NAME_JIN_DOU_HE -> {
                stationId = STATION_JIN_DOU_HE;
                deviceType = STATION_HOST_DEVICE_TYPE_FIVE;
                statusIdentifier = IS_RUNNING_IDENTIFIER;
            }
            case PUMP_STATION_NAME_PAN_JIA_HE -> {
                stationId = STATION_PAN_JIA_HE;
                deviceType = STATION_HOST_DEVICE_TYPE_EIGHT;
                statusIdentifier = IS_RUNNING_IDENTIFIER;
            }
            case PUMP_STATION_NAME_SHA_HE -> {
                stationId = STATION_SHA_HE;
                deviceType = STATION_HOST_DEVICE_TYPE_FIVE;
                statusIdentifier = MAIN_UNIT_RUNNING_IDENTIFIER;
            }
            case PUMP_STATION_NAME_SHI_ER_WEI -> {
                stationId = STATION_SHI_ER_WEI;
                deviceType = STATION_HOST_DEVICE_TYPE_THREE;
                statusIdentifier = RUN_IDENTIFIER;
            }
            default -> {
                return respVO;
            }
        }

        List<IotDeviceDO> deviceList = queryDevices(stationId, deviceType, null);
        if (CollUtil.isEmpty(deviceList)) {
            return respVO;
        }
        respVO.setTotalCount((long) deviceList.size());

        long runningCount = 0L;
        for (IotDeviceDO device : deviceList) {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(device.getId());
            Object statusValue = extractPropertyValue(latestProperties, statusIdentifier);
            if (isRunningStatus(statusValue)) {
                runningCount++;
            }
        }
        respVO.setRunningCount(runningCount);
        return respVO;
    }

    @Override
    public IotScreenPumpStationRunningStatRespVO getPumpStationGateOpenStat(String pumpStationName) {
        String normalizedPumpStationName = StrUtil.trimToEmpty(pumpStationName);
        String stationId;
        switch (normalizedPumpStationName) {
            case PUMP_STATION_NAME_JIN_DOU_HE -> stationId = STATION_JIN_DOU_HE;
            case PUMP_STATION_NAME_PAN_JIA_HE -> stationId = STATION_PAN_JIA_HE;
            case PUMP_STATION_NAME_SHA_HE -> stationId = STATION_SHA_HE;
            case PUMP_STATION_NAME_SHI_ER_WEI -> stationId = STATION_SHI_ER_WEI;
            default -> {
                return null;
            }
        }

        IotScreenPumpStationRunningStatRespVO respVO = new IotScreenPumpStationRunningStatRespVO();
        respVO.setPumpStationName(normalizedPumpStationName);
        respVO.setTotalCount(0L);
        respVO.setRunningCount(0L);

        List<IotDeviceDO> gateDeviceList = queryDevices(stationId, GATE_DEVICE_TYPE, null);
        if (CollUtil.isEmpty(gateDeviceList)) {
            return respVO;
        }
        respVO.setTotalCount((long) gateDeviceList.size());

        long runningCount = 0L;
        for (IotDeviceDO gateDevice : gateDeviceList) {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(gateDevice.getId());
            Object isGateOpenAll = extractPropertyValue(latestProperties, GATE_OPEN_ALL_IDENTIFIER);
            if (isRunningStatus(isGateOpenAll)) {
                runningCount++;
            }
        }
        respVO.setRunningCount(runningCount);
        return respVO;
    }

    @Override
    public List<IotScreenPumpStationGateDetailRespVO> getPumpStationGateDetailList(String pumpStationName) {
        String normalizedPumpStationName = StrUtil.trimToNull(pumpStationName);
        if (normalizedPumpStationName == null) {
            return Collections.emptyList();
        }

        String stationId;
        String gateUpIdentifier;
        String gateDownIdentifier;
        if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_SHI_ER_WEI)) {
            stationId = STATION_SHI_ER_WEI;
            gateUpIdentifier = GATE_UP_IDENTIFIER_STATION_THREE;
            gateDownIdentifier = GATE_DOWN_IDENTIFIER_STATION_THREE;
        } else if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_JIN_DOU_HE)) {
            stationId = STATION_JIN_DOU_HE;
            gateUpIdentifier = GATE_UP_IDENTIFIER;
            gateDownIdentifier = GATE_DOWN_IDENTIFIER;
        } else if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_PAN_JIA_HE)) {
            stationId = STATION_PAN_JIA_HE;
            gateUpIdentifier = GATE_UP_IDENTIFIER;
            gateDownIdentifier = GATE_DOWN_IDENTIFIER;
        } else if (StrUtil.equals(normalizedPumpStationName, PUMP_STATION_NAME_SHA_HE)) {
            stationId = STATION_SHA_HE;
            gateUpIdentifier = GATE_RISING_IDENTIFIER;
            gateDownIdentifier = GATE_LOWERING_IDENTIFIER;
        } else {
            return Collections.emptyList();
        }

        List<IotDeviceDO> gateDeviceList = queryDevices(stationId, GATE_DEVICE_TYPE, null);
        if (CollUtil.isEmpty(gateDeviceList)) {
            return Collections.emptyList();
        }

        return convertList(gateDeviceList, gateDevice -> {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(gateDevice.getId());
            if (latestProperties == null) {
                latestProperties = Collections.emptyMap();
            }

            Object isGateOpenAll = extractPropertyValue(latestProperties, GATE_OPEN_ALL_IDENTIFIER);
            Object isGateCloseAll = extractPropertyValue(latestProperties, GATE_CLOSE_ALL_IDENTIFIER);
            Object isGateUp = extractPropertyValue(latestProperties, gateUpIdentifier);
            Object isGateDown = extractPropertyValue(latestProperties, gateDownIdentifier);
            Object activePower = extractPropertyValue(latestProperties, ACTIVE_POWER_IDENTIFIERS);
            LocalDateTime collectTime = extractPropertyUpdateTime(latestProperties, ACTIVE_POWER_IDENTIFIERS);
            if (collectTime == null) {
                collectTime = extractPropertyUpdateTime(latestProperties,
                        List.of(GATE_OPEN_ALL_IDENTIFIER, GATE_CLOSE_ALL_IDENTIFIER, gateUpIdentifier, gateDownIdentifier));
            }

            IotScreenPumpStationGateDetailRespVO respVO = new IotScreenPumpStationGateDetailRespVO();
            respVO.setGateName(gateDevice.getDeviceName());
            respVO.setIsGateOpenAll(isGateOpenAll);
            respVO.setIsGateCloseAll(isGateCloseAll);
            respVO.setIsGateUp(isGateUp);
            respVO.setIsGateDown(isGateDown);
            respVO.setActivePower(activePower);
            respVO.setCollectTime(collectTime == null ? null : collectTime.format(TIME_FORMATTER));
            return respVO;
        });
    }

    @Override
    public List<IotScreenWorkConditionRespVO> getWorkConditionStatList(@Valid IotScreenWorkConditionListReqVO reqVO) {
        String selectedStationId = StrUtil.trimToNull(reqVO.getStationId());
        if (selectedStationId == null) {
            return Collections.emptyList();
        }
        String selectedDeviceName = StrUtil.trimToNull(reqVO.getDeviceName());
        List<String> deviceNames = selectedDeviceName == null ? null : List.of(selectedDeviceName);
        List<IotDeviceDO> deviceList = queryDevices(selectedStationId, reqVO.getDeviceType(), deviceNames);
        if (CollUtil.isEmpty(deviceList)) {
            return Collections.emptyList();
        }

        boolean hasTimeRange = reqVO.getStartTime() != null || reqVO.getEndTime() != null;
        LocalDateTime[] queryTimes = hasTimeRange ? resolveQueryTimes(reqVO.getStartTime(), reqVO.getEndTime(), null)
                : null;
        Map<String, String> stationLabelMap = buildStationLabelMap();
        Map<Long, Set<String>> productFieldCache = new HashMap<>();

        List<IotScreenWorkConditionRespVO> result = new ArrayList<>();
        for (IotDeviceDO device : deviceList) {
            if (device == null) {
                continue;
            }
            if (!hasTimeRange) {
                result.add(buildLatestWorkConditionForDevice(selectedStationId, device, stationLabelMap));
                continue;
            }
            if (Objects.equals(device.getDeviceType(), ENGINEERING_TYPE_MAIN_UNIT)) {
                result.addAll(listMainUnitWorkConditionByRange(selectedStationId, device, queryTimes,
                        productFieldCache, stationLabelMap));
            } else {
                result.addAll(listWorkConditionByBatchRange(device, queryTimes, productFieldCache, stationLabelMap));
            }
        }
        result.sort(Comparator
                .comparing(IotScreenWorkConditionRespVO::getTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(IotScreenWorkConditionRespVO::getDeviceName, Comparator.nullsLast(String::compareTo)));
        return result;
    }

    private IotScreenWorkConditionRespVO buildLatestWorkConditionForDevice(String selectedStationId,
                                                                            IotDeviceDO device,
                                                                            Map<String, String> stationLabelMap) {
        IotDeviceDO hostDevice = Objects.equals(device.getDeviceType(), ENGINEERING_TYPE_MAIN_UNIT)
                ? findWorkConditionHostDevice(selectedStationId, device)
                : null;
        String stationName = StrUtil.blankToDefault(stationLabelMap.get(selectedStationId), selectedStationId);
        Map<String, IotDevicePropertyDO> deviceLatestProperties = safeGetLatestProperties(device.getId());
        Map<String, IotDevicePropertyDO> hostLatestProperties = hostDevice == null
                ? Collections.emptyMap()
                : safeGetLatestProperties(hostDevice.getId());

        IotScreenWorkConditionRespVO respVO = new IotScreenWorkConditionRespVO();
        respVO.setStationId(selectedStationId);
        respVO.setStationName(stationName);
        respVO.setDeviceId(device.getId());
        respVO.setDeviceName(device.getDeviceName());
        respVO.setDeviceType(device.getDeviceType());
        respVO.setDeviceTypeName(resolveDeviceTypeName(device.getDeviceType()));
        respVO.setTime(resolveWorkConditionTimeUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, null));
        respVO.setActivePowerKw(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, ACTIVE_POWER_IDENTIFIERS, null));
        respVO.setReactivePowerKvar(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, REACTIVE_POWER_IDENTIFIERS, null));
        respVO.setPowerFactor(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, POWER_FACTOR_IDENTIFIERS, null));
        respVO.setFrequency(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, FREQUENCY_IDENTIFIERS, null));
        respVO.setAbVoltage(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, AB_VOLTAGE_IDENTIFIERS, null));
        respVO.setBcVoltage(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, BC_VOLTAGE_IDENTIFIERS, null));
        respVO.setCaVoltage(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, CA_VOLTAGE_IDENTIFIERS, null));
        respVO.setACurrent(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, A_CURRENT_IDENTIFIERS, null));
        respVO.setBCurrent(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, B_CURRENT_IDENTIFIERS, null));
        respVO.setCCurrent(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, C_CURRENT_IDENTIFIERS, null));
        respVO.setAStatorTemp1(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, A_STATOR_TEMP1_IDENTIFIERS, null));
        respVO.setBStatorTemp1(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, B_STATOR_TEMP1_IDENTIFIERS, null));
        respVO.setCStatorTemp1(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, C_STATOR_TEMP1_IDENTIFIERS, null));
        respVO.setAStatorTemp2(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, A_STATOR_TEMP2_IDENTIFIERS, null));
        respVO.setBStatorTemp2(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, B_STATOR_TEMP2_IDENTIFIERS, null));
        respVO.setCStatorTemp2(extractWorkConditionValueUnion(device, deviceLatestProperties,
                hostDevice, hostLatestProperties, C_STATOR_TEMP2_IDENTIFIERS, null));
        respVO.setIsGateOpenAll(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), GATE_OPEN_ALL_WORK_IDENTIFIERS, null));
        respVO.setIsGateCloseAll(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), GATE_CLOSE_ALL_WORK_IDENTIFIERS, null));
        respVO.setIsGateUp(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), GATE_UP_WORK_IDENTIFIERS, null));
        respVO.setIsGateDown(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), GATE_DOWN_WORK_IDENTIFIERS, null));
        respVO.setIsGateFailure(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), GATE_FAILURE_WORK_IDENTIFIERS, null));
        respVO.setIsGatePowerOn(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), GATE_POWER_ON_WORK_IDENTIFIERS, null));
        respVO.setFloodgateOpening(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), FLOODGATE_OPENING_WORK_IDENTIFIERS, null));
        respVO.setWaterLevel(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), WATER_LEVEL_IDENTIFIERS, null));
        respVO.setPressValue(extractWorkConditionValueUnion(device, deviceLatestProperties,
                null, Collections.emptyMap(), PRESS_VALUE_WORK_IDENTIFIERS, null));
        return respVO;
    }

    private List<IotScreenWorkConditionRespVO> listMainUnitWorkConditionByRange(String selectedStationId,
                                                                                 IotDeviceDO mainUnitDevice,
                                                                                 LocalDateTime[] queryTimes,
                                                                                 Map<Long, Set<String>> productFieldCache,
                                                                                 Map<String, String> stationLabelMap) {
        IotDeviceDO hostDevice = findWorkConditionHostDevice(selectedStationId, mainUnitDevice);
        String stationName = StrUtil.blankToDefault(stationLabelMap.get(selectedStationId), selectedStationId);
        Map<String, String> mainUnitMetricColumnMap = resolveWorkConditionMetricColumnMap(
                mainUnitDevice.getProductId(), productFieldCache);
        Map<String, String> hostMetricColumnMap = hostDevice == null
                ? Collections.emptyMap()
                : resolveWorkConditionMetricColumnMap(hostDevice.getProductId(), productFieldCache);
        List<String> mainUnitColumns = resolveMetricQueryColumns(mainUnitMetricColumnMap);
        List<String> hostColumns = resolveMetricQueryColumns(hostMetricColumnMap);
        if (CollUtil.isEmpty(mainUnitColumns) && CollUtil.isEmpty(hostColumns)) {
            return List.of(buildLatestWorkConditionForDevice(selectedStationId, mainUnitDevice, stationLabelMap));
        }
        List<Map<String, Object>> mainUnitRows = queryWorkConditionRows(mainUnitDevice.getId(), queryTimes,
                mainUnitColumns);
        List<Map<String, Object>> hostRows = hostDevice == null
                ? Collections.emptyList()
                : queryWorkConditionRows(hostDevice.getId(), queryTimes, hostColumns);
        List<IotScreenWorkConditionRespVO> merged = mergeWorkConditionRowsByTime(selectedStationId, stationName,
                mainUnitDevice, mainUnitRows, mainUnitMetricColumnMap, hostRows, hostMetricColumnMap);
        if (CollUtil.isEmpty(merged)) {
            return List.of(buildLatestWorkConditionForDevice(selectedStationId, mainUnitDevice, stationLabelMap));
        }
        return merged;
    }

    @Override
    public IotScreenWorkConditionLatestRespVO getLatestWorkConditionStat(String stationId, Long deviceId) {
        String selectedStationId = StrUtil.trimToNull(stationId);
        if (selectedStationId == null || deviceId == null) {
            return null;
        }
        IotDeviceDO selectedDevice = deviceMapper.selectById(deviceId);
        if (selectedDevice == null) {
            return null;
        }
        if (!StrUtil.equals(selectedStationId, StrUtil.trimToNull(selectedDevice.getStationId()))) {
            return null;
        }
        IotScreenWorkConditionListReqVO reqVO = new IotScreenWorkConditionListReqVO();
        reqVO.setStationId(selectedStationId);
        reqVO.setDeviceName(selectedDevice.getDeviceName());
        List<IotScreenWorkConditionRespVO> list = getWorkConditionStatList(reqVO);
        return buildLatestWorkConditionResp(CollUtil.isEmpty(list) ? null : list.get(0));
    }

    private IotScreenWorkConditionLatestRespVO buildLatestWorkConditionResp(IotScreenWorkConditionRespVO source) {
        if (source == null) {
            return null;
        }
        IotScreenWorkConditionLatestRespVO respVO = new IotScreenWorkConditionLatestRespVO();
        respVO.setStationId(source.getStationId());
        respVO.setStationName(source.getStationName());
        respVO.setDeviceId(source.getDeviceId());
        respVO.setDeviceName(source.getDeviceName());
        respVO.setTime(source.getTime());
        respVO.setActivePowerKw(buildWorkConditionMetricValue("有功功率(Kw)", ACTIVE_POWER_IDENTIFIERS,
                source.getActivePowerKw()));
        respVO.setReactivePowerKvar(buildWorkConditionMetricValue("无功功率(Kvar)", REACTIVE_POWER_IDENTIFIERS,
                source.getReactivePowerKvar()));
        respVO.setPowerFactor(buildWorkConditionMetricValue("功率因素", POWER_FACTOR_IDENTIFIERS,
                source.getPowerFactor()));
        respVO.setFrequency(buildWorkConditionMetricValue("频率", FREQUENCY_IDENTIFIERS,
                source.getFrequency()));
        respVO.setAbVoltage(buildWorkConditionMetricValue("AB相电压", AB_VOLTAGE_IDENTIFIERS,
                source.getAbVoltage()));
        respVO.setBcVoltage(buildWorkConditionMetricValue("BC相电压", BC_VOLTAGE_IDENTIFIERS,
                source.getBcVoltage()));
        respVO.setCaVoltage(buildWorkConditionMetricValue("CA相电压", CA_VOLTAGE_IDENTIFIERS,
                source.getCaVoltage()));
        respVO.setACurrent(buildWorkConditionMetricValue("A相电流", A_CURRENT_IDENTIFIERS,
                source.getACurrent()));
        respVO.setBCurrent(buildWorkConditionMetricValue("B相电流", B_CURRENT_IDENTIFIERS,
                source.getBCurrent()));
        respVO.setCCurrent(buildWorkConditionMetricValue("C相电流", C_CURRENT_IDENTIFIERS,
                source.getCCurrent()));
        respVO.setAStatorTemp1(buildWorkConditionMetricValue("主机定子U1温度", A_STATOR_TEMP1_IDENTIFIERS,
                source.getAStatorTemp1()));
        respVO.setBStatorTemp1(buildWorkConditionMetricValue("主机定子V1温度", B_STATOR_TEMP1_IDENTIFIERS,
                source.getBStatorTemp1()));
        respVO.setCStatorTemp1(buildWorkConditionMetricValue("主机定子W1温度", C_STATOR_TEMP1_IDENTIFIERS,
                source.getCStatorTemp1()));
        respVO.setAStatorTemp2(buildWorkConditionMetricValue("主机定子U2温度", A_STATOR_TEMP2_IDENTIFIERS,
                source.getAStatorTemp2()));
        respVO.setBStatorTemp2(buildWorkConditionMetricValue("主机定子V2温度", B_STATOR_TEMP2_IDENTIFIERS,
                source.getBStatorTemp2()));
        respVO.setCStatorTemp2(buildWorkConditionMetricValue("主机定子W2温度", C_STATOR_TEMP2_IDENTIFIERS,
                source.getCStatorTemp2()));
        return respVO;
    }

    private IotScreenMetricValueRespVO buildWorkConditionMetricValue(String propertyName,
                                                                      List<String> identifiers,
                                                                      Object value) {
        IotScreenMetricValueRespVO metricValue = new IotScreenMetricValueRespVO();
        metricValue.setPropertyName(propertyName);
        metricValue.setIdentifier(CollUtil.isEmpty(identifiers) ? null : identifiers.get(0));
        metricValue.setValue(value);
        return metricValue;
    }

    @Override
    public IotScreenGateMetricSnapshotRespVO getGateMetricSnapshot(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null || !Objects.equals(device.getDeviceType(), GATE_DEVICE_TYPE)) {
            return null;
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusYears(HISTORY_LOOKBACK_YEARS);
        IotScreenGateMetricSnapshotRespVO respVO = new IotScreenGateMetricSnapshotRespVO();
        respVO.setDeviceId(device.getId());
        respVO.setDeviceName(device.getDeviceName());
        respVO.setIa(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_A_CURRENT_IDENTIFIERS, "A相电流"));
        respVO.setIb(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_B_CURRENT_IDENTIFIERS, "B相电流"));
        respVO.setIc(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_C_CURRENT_IDENTIFIERS, "C相电流"));
        respVO.setP(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_ACTIVE_POWER_IDENTIFIERS, "有功功率"));
        respVO.setQ(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_REACTIVE_POWER_IDENTIFIERS, "无功功率"));
        respVO.setUab(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_AB_VOLTAGE_IDENTIFIERS, "AB相电压"));
        respVO.setUbc(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_BC_VOLTAGE_IDENTIFIERS, "BC相电压"));
        respVO.setUca(resolveGateMetricValue(deviceId, startTime, endTime,
                GATE_SNAPSHOT_CA_VOLTAGE_IDENTIFIERS, "CA相电压"));
        return respVO;
    }

    @Override
    public List<IotScreenStreamWaterRespVO> getStreamWaterList(@Valid IotScreenStreamWaterListReqVO reqVO) {
        List<IotDeviceDO> deviceList = queryDevices(reqVO.getStationId(), STREAM_WATER_DEVICE_TYPE,
                reqVO.getDeviceNames());
        if (CollUtil.isEmpty(deviceList)) {
            return Collections.emptyList();
        }

        LocalDateTime[] times = resolveStreamWaterQueryTimes(reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getTimeRange());
        Map<String, String> stationLabelMap = buildStationLabelMap();
        List<String> streamWaterIdentifiers = resolveStreamWaterIdentifiers(reqVO.getStationId());

        List<IotScreenStreamWaterRespVO> result = new ArrayList<>();
        for (IotDeviceDO device : deviceList) {
            List<IotDevicePropertyRespVO> historyList = queryStreamWaterHistory(device.getId(), times,
                    streamWaterIdentifiers);
            if (CollUtil.isEmpty(historyList)) {
                continue;
            }
            for (IotDevicePropertyRespVO history : historyList) {
                IotScreenStreamWaterRespVO item = new IotScreenStreamWaterRespVO();
                item.setTime(formatStreamWaterTime(history.getUpdateTime()));
                item.setWaterLevel(roundStreamWaterLevel(history.getValue()));
                item.setStationId(device.getStationId());
                item.setStationName(StrUtil.blankToDefault(stationLabelMap.get(device.getStationId()),
                        device.getStationId()));
                item.setDeviceId(device.getId());
                item.setDeviceName(device.getDeviceName());
                result.add(item);
            }
        }
        result.sort(Comparator.comparing(IotScreenStreamWaterRespVO::getTime,
                Comparator.nullsLast(String::compareTo)));
        return result;
    }

    /**
     * 十二圩水位物模型为 sw_pv；当前字典键值为 0（历史曾用 3）。其它站默认 streamWater，并兼容 waterVal。
     */
    private List<String> resolveStreamWaterIdentifiers(String stationId) {
        // 十二圩水位物模型为 sw_pv；现行字典键 0，历史键 3
        if (isShiErWeiStation(stationId)) {
            return List.of(STREAM_WATER_IDENTIFIER_STATION_THREE, STREAM_WATER_IDENTIFIER, "waterVal", "waterLevel");
        }
        return List.of(STREAM_WATER_IDENTIFIER, "waterVal", STREAM_WATER_IDENTIFIER_STATION_THREE, "waterLevel");
    }

    private List<IotDevicePropertyRespVO> queryStreamWaterHistory(Long deviceId, LocalDateTime[] times,
                                                                  List<String> identifiers) {
        if (deviceId == null || times == null || CollUtil.isEmpty(identifiers)) {
            return Collections.emptyList();
        }
        for (String identifier : identifiers) {
            String normalizedIdentifier = StrUtil.trimToNull(identifier);
            if (normalizedIdentifier == null) {
                continue;
            }
            IotDevicePropertyHistoryListReqVO historyReqVO = new IotDevicePropertyHistoryListReqVO();
            historyReqVO.setDeviceId(deviceId);
            historyReqVO.setIdentifier(normalizedIdentifier);
            historyReqVO.setTimes(times);
            try {
                List<IotDevicePropertyRespVO> historyList = devicePropertyService.getHistoryDevicePropertyList(historyReqVO);
                if (CollUtil.isNotEmpty(historyList)) {
                    return historyList;
                }
            } catch (Exception exception) {
                // 站混绑/物模型不一致时，列不存在则尝试下一候选标识
                if (isInvalidColumnException(exception) || isTableNotExistsException(exception)) {
                    continue;
                }
                throw exception;
            }
        }
        return Collections.emptyList();
    }


    private boolean isShiErWeiStation(String stationId) {
        String normalized = StrUtil.trimToEmpty(stationId);
        return STATION_SHI_ER_WEI.equals(normalized) || STATION_LEGACY_SHI_ER_WEI.equals(normalized);
    }

    private String resolveGateCloseAllIdentifier(String stationId) {
        if (isShiErWeiStation(stationId)) {
            return GATE_CLOSE_ALL_IDENTIFIER_STATION_THREE;
        }
        return GATE_CLOSE_ALL_IDENTIFIER;
    }

    private String resolveGateOpenAllIdentifier(String stationId) {
        if (isShiErWeiStation(stationId)) {
            return GATE_OPEN_ALL_IDENTIFIER_STATION_THREE;
        }
        return GATE_OPEN_ALL_IDENTIFIER;
    }

    private String resolveGateUpIdentifierForStatusList(String stationId) {
        if (isShiErWeiStation(stationId)) {
            return GATE_UP_IDENTIFIER_STATION_THREE;
        }
        if (STATION_SHA_HE.equals(stationId)) {
            return GATE_RISING_IDENTIFIER;
        }
        return GATE_UP_IDENTIFIER;
    }

    private String resolveGateDownIdentifierForStatusList(String stationId) {
        if (isShiErWeiStation(stationId)) {
            return GATE_DOWN_IDENTIFIER_STATION_THREE;
        }
        if (STATION_SHA_HE.equals(stationId)) {
            return GATE_LOWERING_IDENTIFIER;
        }
        return GATE_DOWN_IDENTIFIER;
    }

    private List<String> resolveGateLoadWeightRIdentifiersForStatusList(String stationId) {
        if (isShiErWeiStation(stationId)) {
            return List.of(GATE_LOAD_WEIGHT_R_IDENTIFIER_STATION_THREE);
        }
        return GATE_LOAD_WEIGHT_R_IDENTIFIERS;
    }

    private List<String> resolveGateLoadWeightLIdentifiersForStatusList(String stationId) {
        if (isShiErWeiStation(stationId)) {
            return List.of(GATE_LOAD_WEIGHT_L_IDENTIFIER_STATION_THREE);
        }
        return GATE_LOAD_WEIGHT_L_IDENTIFIERS;
    }

    @Override
    public Map<String, List<Map<String, Object>>> getFacilityBySlss(List<String> values) {
        return getFacilityBySlss(values, null);
    }

    @Override
    public Map<String, List<Map<String, Object>>> getFacilityBySlss(List<String> values, Long areaId) {
        List<String> normalizedValues = normalizeSlssValues(values);
        if (CollUtil.isEmpty(normalizedValues)) {
            return Collections.emptyMap();
        }

        Map<String, String> slssLabelMap = loadDictLabelMap(DICT_TYPE_SLSS);
        Map<String, String> pumpStationTypeLabelMap = loadDictLabelMap(DICT_TYPE_PUMP_STATION_TYPE);
        Map<String, String> materialUnitLabelMap = loadDictLabelMap(DICT_TYPE_MATERIAL_UNIT);
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (String value : normalizedValues) {
            String label = StrUtil.blankToDefault(slssLabelMap.get(value), value);
            result.put(label, queryFacilityItemsBySlssValue(value, pumpStationTypeLabelMap, materialUnitLabelMap, areaId));
        }
        return result;
    }

    @Override
    public List<IotScreenAreaNodeRespVO> getScreenAreaTree(Long rootId) {
        Long root = rootId == null ? SCREEN_AREA_DEFAULT_ROOT_ID : rootId;
        List<SystemAreaNode> children;
        try {
            children = systemAreaService.getAreaTreeChildren(root);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        return convertScreenAreaNodes(children);
    }

    @Override
    public IotScreenAreaRespVO getScreenArea(Long id) {
        if (id == null) {
            return null;
        }
        SystemAreaDO area;
        try {
            area = systemAreaService.get(id);
        } catch (Exception ex) {
            return null;
        }
        return convertScreenArea(area);
    }

    private IotScreenAreaRespVO convertScreenArea(SystemAreaDO area) {
        if (area == null || area.getId() == null) {
            return null;
        }
        IotScreenAreaRespVO vo = new IotScreenAreaRespVO();
        vo.setId(Long.toString(area.getId()));
        if (area.getParentId() != null && area.getParentId() > 0) {
            vo.setParentId(Long.toString(area.getParentId()));
        }
        vo.setName(StrUtil.blankToDefault(area.getName(), ""));
        vo.setType(area.getType());
        vo.setSort(area.getSort());
        vo.setGemo(area.getGemo());
        vo.setGemoGeoJson(area.getGemoGeoJson());
        return vo;
    }

    private List<IotScreenAreaNodeRespVO> convertScreenAreaNodes(List<SystemAreaNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotScreenAreaNodeRespVO> result = new ArrayList<>(nodes.size());
        for (SystemAreaNode node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            IotScreenAreaNodeRespVO item = new IotScreenAreaNodeRespVO();
            item.setId(Long.toString(node.getId()));
            item.setName(StrUtil.blankToDefault(node.getName(), ""));
            item.setType(node.getType());
            item.setChildren(convertScreenAreaNodes(node.getChildren()));
            result.add(item);
        }
        return result;
    }

    @Override
    public IotScreenPondPageRespVO getPondPage(IotScreenPondPageReqVO reqVO) {
        if (reqVO == null) {
            throw invalidParamException("坑塘查询分页参数不能为空");
        }
        // 按 id 查详情时走单条逻辑，分页参数可不传
        if (reqVO.getId() != null) {
            Map<String, Object> detail = getPondDetail(reqVO.getId(), reqVO.getIncludeGeometry());
            IotScreenPondPageRespVO resp = new IotScreenPondPageRespVO();
            if (detail == null || detail.isEmpty()) {
                resp.setList(Collections.emptyList());
                resp.setTotal(0L);
                resp.setPageNo(1);
                resp.setPageSize(1);
                resp.setHasMore(false);
                return resp;
            }
            resp.setList(Collections.singletonList(detail));
            resp.setTotal(1L);
            resp.setPageNo(1);
            resp.setPageSize(1);
            resp.setHasMore(false);
            return resp;
        }
        if (reqVO.getPageNo() == null || reqVO.getPageNo() < 1) {
            throw invalidParamException("pageNo 必传，且从 1 开始");
        }
        if (reqVO.getPageSize() == null || reqVO.getPageSize() < 1) {
            throw invalidParamException("pageSize 必传，且至少为 1");
        }
        int pageNo = reqVO.getPageNo();
        int pageSize = normalizePondPageSize(reqVO.getPageSize());
        String[] villageCodes = buildPondVillageCodeScope(reqVO.getAreaId(), reqVO.getVillageCode());
        boolean includeGeometry = Boolean.TRUE.equals(reqVO.getIncludeGeometry());
        WaterPondPageReqVO query = toWaterPondPageReqFromList(reqVO);

        Long total = waterPondMapper.countScreenPondPage(null, villageCodes,
                reqVO.getMinLon(), reqVO.getMinLat(), reqVO.getMaxLon(), reqVO.getMaxLat(), query);
        total = total == null ? 0L : total;

        List<Map<String, Object>> rows = Collections.emptyList();
        if (total > 0) {
            int offset = (pageNo - 1) * pageSize;
            rows = waterPondMapper.selectScreenPondPage(null, villageCodes,
                    reqVO.getMinLon(), reqVO.getMinLat(), reqVO.getMaxLon(), reqVO.getMaxLat(),
                    includeGeometry, offset, pageSize, query);
        }
        PondAreaLabelIndex areaIndex = loadPondAreaLabelIndex();
        List<Map<String, Object>> list = CollUtil.isEmpty(rows)
                ? Collections.emptyList()
                : convertList(rows, row -> convertScreenPondRow(row, areaIndex));

        IotScreenPondPageRespVO resp = new IotScreenPondPageRespVO();
        resp.setList(list);
        resp.setTotal(total);
        resp.setPageNo(pageNo);
        resp.setPageSize(pageSize);
        resp.setHasMore((long) pageNo * pageSize < total);
        return resp;
    }

    @Override
    public Map<String, Object> getPondDetail(Long id, Boolean includeGeometry) {
        if (id == null) {
            throw invalidParamException("坑塘 id 不能为空");
        }
        // 大屏已用 MVT 画面，详情默认不带 GeoJSON；仅 includeGeometry=true 时查询（仍不写入响应，见 convert）
        boolean withGeometry = Boolean.TRUE.equals(includeGeometry);
        List<Map<String, Object>> rows = waterPondMapper.selectScreenPondPage(
                id, null, null, null, null, null, withGeometry, 0, 1, null);
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyMap();
        }
        return convertScreenPondRow(rows.get(0), loadPondAreaLabelIndex());
    }

    @Override
    public byte[] getPondMvtTile(int z, int x, int y, IotScreenPondMvtReqVO reqVO) {
        validatePondMvtTileCoord(z, x, y);
        IotScreenPondMvtReqVO filter = reqVO == null ? new IotScreenPondMvtReqVO() : reqVO;
        String[] villageCodes = buildPondVillageCodeScope(filter.getAreaId(), filter.getVillageCode());
        WaterPondPageReqVO query = toWaterPondPageReq(filter);
        BigDecimal bufferRadiusM = normalizeBufferRadiusM(filter.getBufferRadiusM());
        BigDecimal centerLon = filter.getCenterLon();
        BigDecimal centerLat = filter.getCenterLat();
        if (bufferRadiusM == null || centerLon == null || centerLat == null) {
            centerLon = null;
            centerLat = null;
            bufferRadiusM = null;
        }
        Map<String, Object> row = waterPondMapper.selectPondMvtTile(
                z, x, y, villageCodes, query, centerLon, centerLat, bufferRadiusM);
        return extractMvtBytes(row);
    }

    @Override
    public IotScreenPondFilterOptionsRespVO getPondFilterOptions() {
        IotScreenPondFilterOptionsRespVO resp = new IotScreenPondFilterOptionsRespVO();
        resp.setOwnershipTypeList(defaultList(waterPondMapper.selectDistinctOwnershipType()));
        resp.setResourceTypeList(defaultList(waterPondMapper.selectDistinctResourceType()));
        resp.setUsageStatusList(defaultList(waterPondMapper.selectDistinctUsageStatus()));
        resp.setResourceNatureList(defaultList(waterPondMapper.selectDistinctResourceNature()));
        resp.setOccupationStatusList(defaultList(waterPondMapper.selectDistinctOccupationStatus()));
        return resp;
    }

    private List<String> defaultList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private WaterPondPageReqVO toWaterPondPageReqFromList(IotScreenPondPageReqVO filter) {
        WaterPondPageReqVO query = new WaterPondPageReqVO();
        if (filter == null) {
            return query;
        }
        // 名称：优先 resourceName，兼容前端传 name / keyword
        String resourceName = StrUtil.trim(filter.getResourceName());
        if (StrUtil.isBlank(resourceName)) {
            resourceName = StrUtil.trim(filter.getName());
        }
        if (StrUtil.isBlank(resourceName)) {
            resourceName = StrUtil.trim(filter.getKeyword());
        }
        query.setResourceName(resourceName);
        query.setResourceCode(StrUtil.trim(filter.getResourceCode()));
        query.setLocationDesc(StrUtil.trim(filter.getLocationDesc()));
        query.setOwnerUnit(StrUtil.trim(filter.getOwnerUnit()));
        query.setOwnershipType(StrUtil.trim(filter.getOwnershipType()));
        query.setResourceType(StrUtil.trim(filter.getResourceType()));
        query.setLandType(StrUtil.trim(filter.getLandType()));
        query.setUsageStatus(StrUtil.trim(filter.getUsageStatus()));
        query.setResourceNature(StrUtil.trim(filter.getResourceNature()));
        query.setOccupationStatus(StrUtil.trim(filter.getOccupationStatus()));
        query.setVillageName(StrUtil.trim(filter.getVillageName()));
        query.setRemark(StrUtil.trim(filter.getRemark()));
        query.setSurveyor(StrUtil.trim(filter.getSurveyor()));
        query.setEastTo(StrUtil.trim(filter.getEastTo()));
        query.setSouthTo(StrUtil.trim(filter.getSouthTo()));
        query.setWestTo(StrUtil.trim(filter.getWestTo()));
        query.setNorthTo(StrUtil.trim(filter.getNorthTo()));
        query.setAreaMuMin(filter.getAreaMuMin());
        query.setAreaMuMax(filter.getAreaMuMax());
        query.setAreaSqmMin(filter.getAreaSqmMin());
        query.setAreaSqmMax(filter.getAreaSqmMax());
        query.setOccupyFarmAreaMin(filter.getOccupyFarmAreaMin());
        query.setOccupyFarmAreaMax(filter.getOccupyFarmAreaMax());
        return query;
    }

    private WaterPondPageReqVO toWaterPondPageReq(IotScreenPondMvtReqVO filter) {
        WaterPondPageReqVO query = new WaterPondPageReqVO();
        if (filter == null) {
            return query;
        }
        String resourceName = StrUtil.trim(filter.getResourceName());
        if (StrUtil.isBlank(resourceName)) {
            resourceName = StrUtil.trim(filter.getName());
        }
        if (StrUtil.isBlank(resourceName)) {
            resourceName = StrUtil.trim(filter.getKeyword());
        }
        query.setResourceName(resourceName);
        query.setResourceCode(StrUtil.trim(filter.getResourceCode()));
        query.setLocationDesc(StrUtil.trim(filter.getLocationDesc()));
        query.setOwnerUnit(StrUtil.trim(filter.getOwnerUnit()));
        query.setOwnershipType(StrUtil.trim(filter.getOwnershipType()));
        query.setResourceType(StrUtil.trim(filter.getResourceType()));
        query.setLandType(StrUtil.trim(filter.getLandType()));
        query.setUsageStatus(StrUtil.trim(filter.getUsageStatus()));
        query.setResourceNature(StrUtil.trim(filter.getResourceNature()));
        query.setOccupationStatus(StrUtil.trim(filter.getOccupationStatus()));
        query.setVillageName(StrUtil.trim(filter.getVillageName()));
        query.setRemark(StrUtil.trim(filter.getRemark()));
        query.setSurveyor(StrUtil.trim(filter.getSurveyor()));
        query.setEastTo(StrUtil.trim(filter.getEastTo()));
        query.setSouthTo(StrUtil.trim(filter.getSouthTo()));
        query.setWestTo(StrUtil.trim(filter.getWestTo()));
        query.setNorthTo(StrUtil.trim(filter.getNorthTo()));
        query.setAreaMuMin(filter.getAreaMuMin());
        query.setAreaMuMax(filter.getAreaMuMax());
        query.setAreaSqmMin(filter.getAreaSqmMin());
        query.setAreaSqmMax(filter.getAreaSqmMax());
        query.setOccupyFarmAreaMin(filter.getOccupyFarmAreaMin());
        query.setOccupyFarmAreaMax(filter.getOccupyFarmAreaMax());
        return query;
    }

    private BigDecimal normalizeBufferRadiusM(BigDecimal radiusM) {
        if (radiusM == null || radiusM.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        // 防止过大半径拖垮瓦片查询（约 50km）
        BigDecimal max = BigDecimal.valueOf(50_000L);
        return radiusM.compareTo(max) > 0 ? max : radiusM;
    }

    private byte[] extractMvtBytes(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return new byte[0];
        }
        Object tile = getMapValueByKeys(row, "tile");
        if (tile == null) {
            tile = row.values().iterator().next();
        }
        if (tile == null) {
            return new byte[0];
        }
        if (tile instanceof byte[] bytes) {
            return bytes;
        }
        if (tile instanceof Byte[] boxed) {
            byte[] bytes = new byte[boxed.length];
            for (int i = 0; i < boxed.length; i++) {
                bytes[i] = boxed[i] == null ? 0 : boxed[i];
            }
            return bytes;
        }
        // 部分驱动会以 hex 字符串返回 bytea
        if (tile instanceof String text) {
            String hex = text.startsWith("\\x") ? text.substring(2) : text;
            if (hex.isEmpty()) {
                return new byte[0];
            }
            try {
                int len = hex.length();
                byte[] bytes = new byte[len / 2];
                for (int i = 0; i < len; i += 2) {
                    bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
                }
                return bytes;
            } catch (Exception ignored) {
                return new byte[0];
            }
        }
        return new byte[0];
    }

    private void validatePondMvtTileCoord(int z, int x, int y) {
        if (z < POND_MVT_MIN_ZOOM || z > POND_MVT_MAX_ZOOM) {
            throw invalidParamException("瓦片缩放级别 z 必须在 {0}~{1} 之间", POND_MVT_MIN_ZOOM, POND_MVT_MAX_ZOOM);
        }
        int maxIndex = (1 << z) - 1;
        if (x < 0 || x > maxIndex || y < 0 || y > maxIndex) {
            throw invalidParamException("瓦片坐标 x/y 超出缩放级别 {0} 的合法范围 0~{1}", z, maxIndex);
        }
    }

    private int normalizePondPageSize(Integer pageSize) {
        return Math.min(pageSize, POND_MAX_PAGE_SIZE);
    }

    @Override
    public List<IotScreenVideoStationCountRespVO> getVideoCameraStationCountList() {
        Set<String> excludedScreenRegions = yzVideoScreenExcludeResolver.excludedRegionIndexCodes();
        List<YzVideoRegionDO> regionList = yzVideoRegionMapper.selectList(new LambdaQueryWrapper<YzVideoRegionDO>()
                .select(YzVideoRegionDO::getIndexCode, YzVideoRegionDO::getName)
                .orderByAsc(YzVideoRegionDO::getName)
                .orderByAsc(YzVideoRegionDO::getIndexCode));
        if (CollUtil.isNotEmpty(excludedScreenRegions) && CollUtil.isNotEmpty(regionList)) {
            regionList = regionList.stream()
                    .filter(region -> !excludedScreenRegions.contains(StrUtil.trim(region.getIndexCode())))
                    .collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(regionList)) {
            return Collections.emptyList();
        }

        List<YzVideoCameraDO> cameraList = yzVideoCameraMapper.selectList(new LambdaQueryWrapper<YzVideoCameraDO>()
                .select(YzVideoCameraDO::getRegionIndexCode, YzVideoCameraDO::getOnline));
        Map<String, Long> totalCountByRegion = new HashMap<>();
        Map<String, Long> onlineCountByRegion = new HashMap<>();
        if (CollUtil.isNotEmpty(cameraList)) {
            for (YzVideoCameraDO camera : cameraList) {
                String regionIndexCode = camera.getRegionIndexCode();
                if (StrUtil.isBlank(regionIndexCode)) {
                    continue;
                }
                if (CollUtil.isNotEmpty(excludedScreenRegions)
                        && excludedScreenRegions.contains(StrUtil.trim(regionIndexCode))) {
                    continue;
                }
                totalCountByRegion.merge(regionIndexCode, 1L, Long::sum);
                if (isVideoOnlineStatus(camera.getOnline())) {
                    onlineCountByRegion.merge(regionIndexCode, 1L, Long::sum);
                }
            }
        }

        List<IotScreenVideoStationCountRespVO> respVOList = convertList(regionList, region -> {
            IotScreenVideoStationCountRespVO respVO = new IotScreenVideoStationCountRespVO();
            respVO.setRegionIndexCode(region.getIndexCode());
            respVO.setStationName(region.getName());
            long deviceCount = totalCountByRegion.getOrDefault(region.getIndexCode(), 0L);
            long onlineCount = onlineCountByRegion.getOrDefault(region.getIndexCode(), 0L);
            long offlineCount = Math.max(deviceCount - onlineCount, 0L);
            respVO.setDeviceCount(deviceCount);
            respVO.setOnlineCount(onlineCount);
            respVO.setOfflineCount(offlineCount);
            return respVO;
        });

        long totalDeviceCount = respVOList.stream()
                .map(IotScreenVideoStationCountRespVO::getDeviceCount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        for (IotScreenVideoStationCountRespVO respVO : respVOList) {
            if (totalDeviceCount <= 0 || respVO.getDeviceCount() == null || respVO.getDeviceCount() <= 0) {
                respVO.setTotalRatio(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                continue;
            }
            BigDecimal ratio = BigDecimal.valueOf(respVO.getDeviceCount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalDeviceCount), 2, RoundingMode.HALF_UP);
            respVO.setTotalRatio(ratio);
        }
        return respVOList;
    }

    private boolean isVideoOnlineStatus(String onlineStatus) {
        return StrUtil.equals(VIDEO_ONLINE_STATUS, onlineStatus)
                || StrUtil.equalsIgnoreCase("true", onlineStatus)
                || StrUtil.equalsIgnoreCase("online", onlineStatus);
    }

    private List<IotScreenEngineeringDeviceStatusListRespVO> buildGateDeviceStatusList(
            List<IotDeviceDO> gateDeviceList, String deviceTypeName, IotScreenEngineeringDeviceStatusListReqVO reqVO) {
        List<IotScreenEngineeringDeviceStatusListRespVO> result = new ArrayList<>();
        for (IotDeviceDO device : gateDeviceList) {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(device.getId());
            Object isGateOpenAll = extractPropertyValue(latestProperties, GATE_OPEN_ALL_IDENTIFIER);
            Object isGateCloseAll = extractPropertyValue(latestProperties, GATE_CLOSE_ALL_IDENTIFIER);
            Object isGatePowerOn = extractPropertyValue(latestProperties, GATE_POWER_ON_IDENTIFIER);
            if (!matchStatusFilter(reqVO.getIsGateOpenAll(), isGateOpenAll)
                    || !matchStatusFilter(reqVO.getIsGateCloseAll(), isGateCloseAll)
                    || !matchStatusFilter(reqVO.getIsGatePowerOn(), isGatePowerOn)) {
                continue;
            }
            IotScreenEngineeringDeviceStatusListRespVO respVO = initDeviceStatusRespVO(device, deviceTypeName);
            respVO.setIsGateOpenAll(isGateOpenAll);
            respVO.setIsGateCloseAll(isGateCloseAll);
            respVO.setIsGatePowerOn(isGatePowerOn);
            respVO.setIsGateUp(resolveGateUpValue(latestProperties));
            respVO.setIsGateFailure(resolveGateFailureValue(latestProperties));
            respVO.setIsGateDown(resolveGateDownValue(latestProperties));
            result.add(respVO);
        }
        return result;
    }

    private Object resolveGateUpValue(Map<String, IotDevicePropertyDO> latestProperties) {
        return extractPropertyValue(latestProperties, List.of(GATE_UP_IDENTIFIER, GATE_RISING_IDENTIFIER));
    }

    private Object resolveGateFailureValue(Map<String, IotDevicePropertyDO> latestProperties) {
        return extractPropertyValue(latestProperties, List.of(GATE_FAILURE_IDENTIFIER, GATE_STOPPED_IDENTIFIER));
    }

    private Object resolveGateDownValue(Map<String, IotDevicePropertyDO> latestProperties) {
        return extractPropertyValue(latestProperties, List.of(GATE_DOWN_IDENTIFIER, GATE_LOWERING_IDENTIFIER));
    }

    private List<IotScreenEngineeringDeviceStatusListRespVO> buildMainUnitStatusList(String stationId,
                                                                                      List<IotDeviceDO> mainUnitList,
                                                                                      String deviceTypeName,
                                                                                      Integer filterIsRunning) {
        Integer sourceDeviceType = null;
        String runningIdentifier = null;
        String failureIdentifier = null;
        if (STATION_SHA_HE.equals(stationId)) {
            sourceDeviceType = STATION_HOST_DEVICE_TYPE_FIVE;
            runningIdentifier = MAIN_UNIT_RUNNING_IDENTIFIER;
            failureIdentifier = SOFT_START_FAULT_IDENTIFIER;
        } else if (STATION_JIN_DOU_HE.equals(stationId)) {
            sourceDeviceType = STATION_HOST_DEVICE_TYPE_FIVE;
            runningIdentifier = IS_RUNNING_IDENTIFIER;
            failureIdentifier = FAILURE_IDENTIFIER;
        } else if (STATION_PAN_JIA_HE.equals(stationId)) {
            sourceDeviceType = STATION_HOST_DEVICE_TYPE_EIGHT;
            runningIdentifier = IS_RUNNING_IDENTIFIER;
            failureIdentifier = SOFT_START_FAULT_IDENTIFIER;
        }

        List<IotDeviceDO> sourceDeviceList = sourceDeviceType == null
                ? Collections.emptyList()
                : queryDevices(stationId, sourceDeviceType, null);
        List<IotScreenEngineeringDeviceStatusListRespVO> result = new ArrayList<>();
        for (IotDeviceDO mainUnit : mainUnitList) {
            Map<String, IotDevicePropertyDO> sourceLatestProperties =
                    resolveMainUnitSourceLatestProperties(mainUnit, sourceDeviceList);
            Map<String, IotDevicePropertyDO> selfLatestProperties = isShiErWeiStation(stationId)
                    ? devicePropertyService.getLatestDeviceProperties(mainUnit.getId())
                    : Collections.emptyMap();
            int isRunning = resolveMainUnitRunningStatus(stationId, sourceLatestProperties, runningIdentifier);
            if (filterIsRunning != null && filterIsRunning.intValue() != isRunning) {
                continue;
            }
            IotScreenEngineeringDeviceStatusListRespVO respVO = initDeviceStatusRespVO(mainUnit, deviceTypeName);
            respVO.setIsRunning(isRunning);
            respVO.setIsFailure(resolveMainUnitFailureStatus(stationId, sourceLatestProperties, failureIdentifier));
            if (isShiErWeiStation(stationId)) {
                respVO.setP(extractPropertyValue(selfLatestProperties, ACTIVE_POWER_IDENTIFIERS));
            }
            result.add(respVO);
        }
        return result;
    }

    private Map<String, IotDevicePropertyDO> resolveMainUnitSourceLatestProperties(IotDeviceDO mainUnit,
                                                                                    List<IotDeviceDO> sourceDeviceList) {
        if (mainUnit == null || CollUtil.isEmpty(sourceDeviceList)) {
            return Collections.emptyMap();
        }
        IotDeviceDO matchedDevice = matchPumpDeviceByHostNamePrefix(mainUnit.getDeviceName(), sourceDeviceList);
        if (matchedDevice == null) {
            return Collections.emptyMap();
        }
        Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService
                .getLatestDeviceProperties(matchedDevice.getId());
        return latestProperties == null ? Collections.emptyMap() : latestProperties;
    }

    private int resolveMainUnitRunningStatus(String stationId, Map<String, IotDevicePropertyDO> sourceLatestProperties,
                                             String runningIdentifier) {
        if (isShiErWeiStation(stationId) || StrUtil.isBlank(runningIdentifier)) {
            return 0;
        }
        Object statusValue = extractPropertyValue(sourceLatestProperties, runningIdentifier);
        return isRunningStatus(statusValue) ? 1 : 0;
    }

    private Object resolveMainUnitFailureStatus(String stationId, Map<String, IotDevicePropertyDO> sourceLatestProperties,
                                                String failureIdentifier) {
        if (isShiErWeiStation(stationId) || StrUtil.isBlank(failureIdentifier)) {
            return null;
        }
        return extractPropertyValue(sourceLatestProperties, failureIdentifier);
    }

    private boolean matchStatusFilter(Integer expectedStatus, Object actualValue) {
        if (expectedStatus == null) {
            return true;
        }
        if (actualValue == null) {
            return expectedStatus.intValue() == 0;
        }
        if (actualValue instanceof Number numberValue) {
            return numberValue.intValue() == expectedStatus.intValue();
        }
        String normalizedValue = StrUtil.trimToNull(String.valueOf(actualValue));
        if (normalizedValue == null) {
            return expectedStatus.intValue() == 0;
        }
        try {
            return Integer.parseInt(normalizedValue) == expectedStatus.intValue();
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private IotScreenEngineeringDeviceStatusListRespVO initDeviceStatusRespVO(IotDeviceDO device, String deviceTypeName) {
        IotScreenEngineeringDeviceStatusListRespVO respVO = new IotScreenEngineeringDeviceStatusListRespVO();
        respVO.setDeviceName(device.getDeviceName());
        respVO.setDeviceTypeName(deviceTypeName);
        respVO.setLongitude(device.getLongitude());
        respVO.setLatitude(device.getLatitude());
        return respVO;
    }

    private void sortEngineeringDeviceStatusList(List<IotScreenEngineeringDeviceStatusListRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.sort((left, right) -> compareDeviceNameForDisplay(left.getDeviceName(), right.getDeviceName()));
    }

    private void sortEngineeringRuntimeStatList(List<IotScreenEngineeringRuntimeStatRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.sort((left, right) -> compareDeviceNameForDisplay(left.getDeviceName(), right.getDeviceName()));
    }

    private void sortEngineeringGateStatusList(List<IotScreenEngineeringGateDetailRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        list.sort((left, right) -> compareDeviceNameForDisplay(left.getDeviceName(), right.getDeviceName()));
    }

    private int compareDeviceNameForDisplay(String leftDeviceName, String rightDeviceName) {
        Integer leftPrefixNumber = extractDeviceNamePrefixNumber(leftDeviceName);
        Integer rightPrefixNumber = extractDeviceNamePrefixNumber(rightDeviceName);
        if (leftPrefixNumber != null && rightPrefixNumber != null) {
            int numberCompareResult = Integer.compare(leftPrefixNumber, rightPrefixNumber);
            if (numberCompareResult != 0) {
                return numberCompareResult;
            }
        } else if (leftPrefixNumber != null) {
            return -1;
        } else if (rightPrefixNumber != null) {
            return 1;
        }
        return compareTextByLocale(leftDeviceName, rightDeviceName);
    }

    private Integer extractDeviceNamePrefixNumber(String deviceName) {
        String normalizedDeviceName = StrUtil.trimToNull(deviceName);
        if (normalizedDeviceName == null) {
            return null;
        }
        if (normalizedDeviceName.startsWith("\u7b2c") && normalizedDeviceName.length() > 1) {
            normalizedDeviceName = normalizedDeviceName.substring(1);
        }
        Integer arabicPrefixNumber = tryParseArabicPrefixNumber(normalizedDeviceName);
        if (arabicPrefixNumber != null) {
            return arabicPrefixNumber;
        }
        return tryParseChinesePrefixNumber(normalizedDeviceName);
    }

    private Integer tryParseArabicPrefixNumber(String deviceName) {
        StringBuilder digitBuilder = new StringBuilder();
        for (int i = 0; i < deviceName.length(); i++) {
            char current = deviceName.charAt(i);
            if (current >= '\uFF10' && current <= '\uFF19') {
                current = (char) (current - 65248);
            }
            if (!Character.isDigit(current)) {
                break;
            }
            digitBuilder.append(current);
        }
        if (digitBuilder.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digitBuilder.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer tryParseChinesePrefixNumber(String deviceName) {
        int chinesePrefixEndIndex = 0;
        while (chinesePrefixEndIndex < deviceName.length()
                && isChineseNumberChar(deviceName.charAt(chinesePrefixEndIndex))) {
            chinesePrefixEndIndex++;
        }
        if (chinesePrefixEndIndex <= 0) {
            return null;
        }
        return parseChineseNumber(deviceName.substring(0, chinesePrefixEndIndex));
    }

    private Integer parseChineseNumber(String chineseNumberText) {
        if (StrUtil.isBlank(chineseNumberText)) {
            return null;
        }
        int result = 0;
        int section = 0;
        int currentNumber = 0;
        boolean matched = false;
        for (int i = 0; i < chineseNumberText.length(); i++) {
            char currentChar = chineseNumberText.charAt(i);
            Integer numberValue = CHINESE_NUMBER_MAP.get(currentChar);
            if (numberValue != null) {
                currentNumber = numberValue;
                matched = true;
                if (i == chineseNumberText.length() - 1) {
                    section += currentNumber;
                }
                continue;
            }
            Integer unitValue = CHINESE_NUMBER_UNIT_MAP.get(currentChar);
            if (unitValue == null) {
                return null;
            }
            matched = true;
            if (currentNumber == 0) {
                currentNumber = 1;
            }
            if (unitValue == 10000) {
                result += (section + currentNumber) * unitValue;
                section = 0;
            } else {
                section += currentNumber * unitValue;
            }
            currentNumber = 0;
        }
        if (!matched) {
            return null;
        }
        return result + section;
    }

    private boolean isChineseNumberChar(char value) {
        return CHINESE_NUMBER_MAP.containsKey(value) || CHINESE_NUMBER_UNIT_MAP.containsKey(value);
    }

    private int compareTextByLocale(String leftText, String rightText) {
        String normalizedLeftText = StrUtil.trimToEmpty(leftText);
        String normalizedRightText = StrUtil.trimToEmpty(rightText);
        return Collator.getInstance(Locale.CHINA).compare(normalizedLeftText, normalizedRightText);
    }

    private List<IotDeviceDO> queryDevices(String stationId, Integer deviceType, Collection<String> deviceNames) {
        List<String> normalizedDeviceNames = normalizeDeviceNames(deviceNames);
        return deviceMapper.selectListByStationAndTypeAndNames(stationId, deviceType,
                normalizedDeviceNames);
    }

    private IotDeviceDO findWorkConditionDevice(String selectedStationId, String selectedDeviceName) {
        List<IotDeviceDO> deviceList = queryDevices(selectedStationId, null, List.of(selectedDeviceName));
        return CollUtil.isEmpty(deviceList) ? null : deviceList.get(0);
    }

    private IotDeviceDO findWorkConditionHostDevice(String selectedStationId, IotDeviceDO mainUnitDevice) {
        Integer hostDeviceType = resolveWorkConditionHostDeviceType(selectedStationId);
        if (hostDeviceType == null) {
            return null;
        }
        List<IotDeviceDO> hostDeviceList = queryDevices(selectedStationId, hostDeviceType, null);
        if (CollUtil.isEmpty(hostDeviceList)) {
            return null;
        }
        IotDeviceDO matchedHost = matchPumpDeviceByHostNamePrefix(mainUnitDevice.getDeviceName(), hostDeviceList);
        return matchedHost != null ? matchedHost : hostDeviceList.get(0);
    }

    private Integer resolveWorkConditionHostDeviceType(String selectedStationId) {
        if (StrUtil.equals(selectedStationId, STATION_PAN_JIA_HE)) {
            return STATION_HOST_DEVICE_TYPE_EIGHT;
        }
        if (StrUtil.equals(selectedStationId, STATION_SHA_HE)) {
            return STATION_HOST_DEVICE_TYPE_TEN;
        }
        if (StrUtil.equals(selectedStationId, STATION_JIN_DOU_HE)) {
            return STATION_HOST_DEVICE_TYPE_FIVE;
        }
        return null;
    }

    private Map<String, IotDevicePropertyDO> safeGetLatestProperties(Long deviceId) {
        if (deviceId == null) {
            return Collections.emptyMap();
        }
        Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(deviceId);
        return latestProperties == null ? Collections.emptyMap() : latestProperties;
    }

    private Object extractWorkConditionValueUnion(IotDeviceDO mainUnitDevice,
                                                  Map<String, IotDevicePropertyDO> mainUnitLatestProperties,
                                                  IotDeviceDO hostDevice,
                                                  Map<String, IotDevicePropertyDO> hostLatestProperties,
                                                  Collection<String> identifiers,
                                                  LocalDateTime[] queryTimes) {
        Object mainValue = mainUnitDevice == null ? null : extractWorkConditionValue(mainUnitDevice.getId(),
                mainUnitLatestProperties, identifiers, queryTimes);
        if (mainValue != null) {
            return mainValue;
        }
        if (hostDevice == null) {
            return null;
        }
        return extractWorkConditionValue(hostDevice.getId(), hostLatestProperties, identifiers, queryTimes);
    }

    private String resolveWorkConditionTimeUnion(IotDeviceDO mainUnitDevice,
                                                 Map<String, IotDevicePropertyDO> mainUnitLatestProperties,
                                                 IotDeviceDO hostDevice,
                                                 Map<String, IotDevicePropertyDO> hostLatestProperties,
                                                 LocalDateTime[] queryTimes) {
        String mainTime = mainUnitDevice == null ? null : resolveWorkConditionTime(mainUnitDevice.getId(),
                mainUnitLatestProperties, queryTimes);
        String hostTime = hostDevice == null ? null : resolveWorkConditionTime(hostDevice.getId(),
                hostLatestProperties, queryTimes);
        return pickLatestTime(mainTime, hostTime);
    }

    private String pickLatestTime(String firstTime, String secondTime) {
        String normalizedFirstTime = StrUtil.trimToNull(firstTime);
        String normalizedSecondTime = StrUtil.trimToNull(secondTime);
        if (normalizedFirstTime == null) {
            return normalizedSecondTime;
        }
        if (normalizedSecondTime == null) {
            return normalizedFirstTime;
        }
        return normalizedFirstTime.compareTo(normalizedSecondTime) >= 0 ? normalizedFirstTime : normalizedSecondTime;
    }

    private Map<String, String> resolveWorkConditionMetricColumnMap(Long productId,
                                                                     Map<Long, Set<String>> productFieldCache) {
        if (productId == null) {
            return Collections.emptyMap();
        }
        Set<String> availableColumns = getProductPropertyFields(productId, productFieldCache);
        if (CollUtil.isEmpty(availableColumns)) {
            return Collections.emptyMap();
        }
        Map<String, String> metricColumnMap = new LinkedHashMap<>();
        metricColumnMap.put("activePowerKw", resolveWorkConditionColumn(ACTIVE_POWER_IDENTIFIERS, availableColumns));
        metricColumnMap.put("reactivePowerKvar", resolveWorkConditionColumn(REACTIVE_POWER_IDENTIFIERS, availableColumns));
        metricColumnMap.put("powerFactor", resolveWorkConditionColumn(POWER_FACTOR_IDENTIFIERS, availableColumns));
        metricColumnMap.put("frequency", resolveWorkConditionColumn(FREQUENCY_IDENTIFIERS, availableColumns));
        metricColumnMap.put("abVoltage", resolveWorkConditionColumn(AB_VOLTAGE_IDENTIFIERS, availableColumns));
        metricColumnMap.put("bcVoltage", resolveWorkConditionColumn(BC_VOLTAGE_IDENTIFIERS, availableColumns));
        metricColumnMap.put("caVoltage", resolveWorkConditionColumn(CA_VOLTAGE_IDENTIFIERS, availableColumns));
        metricColumnMap.put("aCurrent", resolveWorkConditionColumn(A_CURRENT_IDENTIFIERS, availableColumns));
        metricColumnMap.put("bCurrent", resolveWorkConditionColumn(B_CURRENT_IDENTIFIERS, availableColumns));
        metricColumnMap.put("cCurrent", resolveWorkConditionColumn(C_CURRENT_IDENTIFIERS, availableColumns));
        metricColumnMap.put("aStatorTemp1", resolveWorkConditionColumn(A_STATOR_TEMP1_IDENTIFIERS, availableColumns));
        metricColumnMap.put("bStatorTemp1", resolveWorkConditionColumn(B_STATOR_TEMP1_IDENTIFIERS, availableColumns));
        metricColumnMap.put("cStatorTemp1", resolveWorkConditionColumn(C_STATOR_TEMP1_IDENTIFIERS, availableColumns));
        metricColumnMap.put("aStatorTemp2", resolveWorkConditionColumn(A_STATOR_TEMP2_IDENTIFIERS, availableColumns));
        metricColumnMap.put("bStatorTemp2", resolveWorkConditionColumn(B_STATOR_TEMP2_IDENTIFIERS, availableColumns));
        metricColumnMap.put("cStatorTemp2", resolveWorkConditionColumn(C_STATOR_TEMP2_IDENTIFIERS, availableColumns));
        metricColumnMap.put("isGateOpenAll", resolveWorkConditionColumn(GATE_OPEN_ALL_WORK_IDENTIFIERS, availableColumns));
        metricColumnMap.put("isGateCloseAll", resolveWorkConditionColumn(GATE_CLOSE_ALL_WORK_IDENTIFIERS, availableColumns));
        metricColumnMap.put("isGateUp", resolveWorkConditionColumn(GATE_UP_WORK_IDENTIFIERS, availableColumns));
        metricColumnMap.put("isGateDown", resolveWorkConditionColumn(GATE_DOWN_WORK_IDENTIFIERS, availableColumns));
        metricColumnMap.put("isGateFailure", resolveWorkConditionColumn(GATE_FAILURE_WORK_IDENTIFIERS, availableColumns));
        metricColumnMap.put("isGatePowerOn", resolveWorkConditionColumn(GATE_POWER_ON_WORK_IDENTIFIERS, availableColumns));
        metricColumnMap.put("floodgateOpening", resolveWorkConditionColumn(FLOODGATE_OPENING_WORK_IDENTIFIERS, availableColumns));
        metricColumnMap.put("waterLevel", resolveWorkConditionColumn(WATER_LEVEL_IDENTIFIERS, availableColumns));
        metricColumnMap.put("pressValue", resolveWorkConditionColumn(PRESS_VALUE_WORK_IDENTIFIERS, availableColumns));
        return metricColumnMap;
    }

    private List<String> resolveMetricQueryColumns(Map<String, String> metricColumnMap) {
        if (metricColumnMap == null || metricColumnMap.isEmpty()) {
            return Collections.emptyList();
        }
        return metricColumnMap.values().stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<IotScreenWorkConditionRespVO> mergeWorkConditionRowsByTime(String stationId,
                                                                             String stationName,
                                                                             IotDeviceDO mainUnitDevice,
                                                                             List<Map<String, Object>> mainUnitRows,
                                                                             Map<String, String> mainUnitMetricColumnMap,
                                                                             List<Map<String, Object>> hostRows,
                                                                             Map<String, String> hostMetricColumnMap) {
        if (mainUnitDevice == null) {
            return Collections.emptyList();
        }
        Map<Long, Map<String, Object>> mainUnitRowByTime = toWorkConditionRowByTimeMap(mainUnitRows);
        Map<Long, Map<String, Object>> hostRowByTime = toWorkConditionRowByTimeMap(hostRows);
        TreeSet<Long> mergedTimestamps = new TreeSet<>(Comparator.reverseOrder());
        mergedTimestamps.addAll(mainUnitRowByTime.keySet());
        mergedTimestamps.addAll(hostRowByTime.keySet());
        if (mergedTimestamps.isEmpty()) {
            return Collections.emptyList();
        }

        List<IotScreenWorkConditionRespVO> result = new ArrayList<>(mergedTimestamps.size());
        for (Long timestamp : mergedTimestamps) {
            Map<String, Object> mainUnitRow = mainUnitRowByTime.getOrDefault(timestamp, Collections.emptyMap());
            Map<String, Object> hostRow = hostRowByTime.getOrDefault(timestamp, Collections.emptyMap());
            IotScreenWorkConditionRespVO respVO = new IotScreenWorkConditionRespVO();
            respVO.setStationId(stationId);
            respVO.setStationName(stationName);
            respVO.setDeviceId(mainUnitDevice.getId());
            respVO.setDeviceName(mainUnitDevice.getDeviceName());
            respVO.setTime(formatTime(timestamp));
            respVO.setActivePowerKw(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("activePowerKw")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("activePowerKw"))));
            respVO.setReactivePowerKvar(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("reactivePowerKvar")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("reactivePowerKvar"))));
            respVO.setPowerFactor(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("powerFactor")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("powerFactor"))));
            respVO.setFrequency(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("frequency")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("frequency"))));
            respVO.setAbVoltage(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("abVoltage")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("abVoltage"))));
            respVO.setBcVoltage(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("bcVoltage")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("bcVoltage"))));
            respVO.setCaVoltage(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("caVoltage")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("caVoltage"))));
            respVO.setACurrent(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("aCurrent")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("aCurrent"))));
            respVO.setBCurrent(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("bCurrent")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("bCurrent"))));
            respVO.setCCurrent(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("cCurrent")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("cCurrent"))));
            respVO.setAStatorTemp1(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("aStatorTemp1")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("aStatorTemp1"))));
            respVO.setBStatorTemp1(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("bStatorTemp1")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("bStatorTemp1"))));
            respVO.setCStatorTemp1(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("cStatorTemp1")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("cStatorTemp1"))));
            respVO.setAStatorTemp2(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("aStatorTemp2")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("aStatorTemp2"))));
            respVO.setBStatorTemp2(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("bStatorTemp2")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("bStatorTemp2"))));
            respVO.setCStatorTemp2(firstPresentValue(
                    getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("cStatorTemp2")),
                    getWorkConditionMetricValue(hostRow, hostMetricColumnMap.get("cStatorTemp2"))));
            respVO.setDeviceType(mainUnitDevice.getDeviceType());
            respVO.setDeviceTypeName(resolveDeviceTypeName(mainUnitDevice.getDeviceType()));
            respVO.setIsGateOpenAll(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("isGateOpenAll")));
            respVO.setIsGateCloseAll(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("isGateCloseAll")));
            respVO.setIsGateUp(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("isGateUp")));
            respVO.setIsGateDown(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("isGateDown")));
            respVO.setIsGateFailure(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("isGateFailure")));
            respVO.setIsGatePowerOn(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("isGatePowerOn")));
            respVO.setFloodgateOpening(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("floodgateOpening")));
            respVO.setWaterLevel(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("waterLevel")));
            respVO.setPressValue(getWorkConditionMetricValue(mainUnitRow, mainUnitMetricColumnMap.get("pressValue")));
            result.add(respVO);
        }
        return result;
    }

    private Map<Long, Map<String, Object>> toWorkConditionRowByTimeMap(List<Map<String, Object>> rows) {
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyMap();
        }
        Map<Long, Map<String, Object>> rowByTime = new HashMap<>();
        for (Map<String, Object> row : rows) {
            if (row == null || row.isEmpty()) {
                continue;
            }
            Long timestamp = resolveEpochMilli(getCaseInsensitiveMapValue(row, "update_time"));
            if (timestamp == null) {
                continue;
            }
            Long secondAlignedTimestamp = alignEpochMilliToSecond(timestamp);
            Map<String, Object> existedRow = rowByTime.get(secondAlignedTimestamp);
            if (existedRow == null) {
                rowByTime.put(secondAlignedTimestamp, new HashMap<>(row));
                continue;
            }
            mergeWorkConditionRowByPresentValue(existedRow, row);
        }
        return rowByTime;
    }

    private Long alignEpochMilliToSecond(Long epochMilli) {
        if (epochMilli == null) {
            return null;
        }
        return (epochMilli / 1000L) * 1000L;
    }

    private void mergeWorkConditionRowByPresentValue(Map<String, Object> targetRow, Map<String, Object> sourceRow) {
        if (targetRow == null || sourceRow == null || sourceRow.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> sourceEntry : sourceRow.entrySet()) {
            String sourceKey = StrUtil.trimToNull(sourceEntry.getKey());
            if (sourceKey == null) {
                continue;
            }
            Object sourceValue = sourceEntry.getValue();
            if (!isPresentValue(sourceValue)) {
                continue;
            }
            Object targetValue = getCaseInsensitiveMapValue(targetRow, sourceKey);
            if (!isPresentValue(targetValue)) {
                targetRow.put(sourceKey, sourceValue);
            }
        }
    }

    private Object getWorkConditionMetricValue(Map<String, Object> row, String column) {
        if (row == null || row.isEmpty() || StrUtil.isBlank(column)) {
            return null;
        }
        return getCaseInsensitiveMapValue(row, column);
    }

    private List<String> normalizeDeviceNames(Collection<String> deviceNames) {
        return deviceNames == null ? null : deviceNames.stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private LocalDateTime[] resolveQueryTimes(LocalDateTime startTime, LocalDateTime endTime, String timeRange) {
        if (startTime != null || endTime != null) {
            if (startTime == null || endTime == null) {
                throw invalidParamException("开始时间和结束时间需同时传入");
            }
            if (endTime.isBefore(startTime)) {
                throw invalidParamException("结束时间不能早于开始时间");
            }
            return new LocalDateTime[] {startTime, endTime};
        }
        int hours = resolveHours(timeRange);
        LocalDateTime defaultEndTime = LocalDateTime.now();
        LocalDateTime defaultStartTime = defaultEndTime.minusHours(hours);
        return new LocalDateTime[] {defaultStartTime, defaultEndTime};
    }

    private int resolveHours(String timeRange) {
        String normalized = StrUtil.blankToDefault(StrUtil.trimToNull(timeRange), DEFAULT_TIME_RANGE);
        return switch (normalized) {
            case "48h" -> 48;
            case "72h" -> 72;
            default -> 24;
        };
    }

    private Map<String, String> buildStationLabelMap() {
        List<DictDataRespDTO> dictDataList = dictDataCommonApi.getDictDataList(DictTypeConstants.DEVICE_SITE);
        if (CollUtil.isEmpty(dictDataList)) {
            return Collections.emptyMap();
        }
        return dictDataList.stream()
                .filter(dictData -> StrUtil.isNotBlank(dictData.getLabel()) && StrUtil.isNotBlank(dictData.getValue()))
                .collect(Collectors.toMap(DictDataRespDTO::getValue, DictDataRespDTO::getLabel,
                        (oldValue, newValue) -> oldValue));
    }

    private String resolveDeviceTypeName(Integer deviceType) {
        if (deviceType == null) {
            return "";
        }
        List<DictDataRespDTO> dictDataList = dictDataCommonApi.getDictDataList(DictTypeConstants.DEVICE_TYPE);
        if (CollUtil.isEmpty(dictDataList)) {
            return String.valueOf(deviceType);
        }
        String deviceTypeValue = String.valueOf(deviceType);
        for (DictDataRespDTO dictData : dictDataList) {
            if (!Objects.equals(dictData.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                continue;
            }
            if (StrUtil.equals(StrUtil.trimToEmpty(dictData.getValue()), deviceTypeValue)
                    && StrUtil.isNotBlank(dictData.getLabel())) {
                return dictData.getLabel();
            }
        }
        return deviceTypeValue;
    }

    private String formatTime(Long epochMilli) {
        if (epochMilli == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMilli)
                .atZone(BUSINESS_TIME_ZONE)
                .toLocalDateTime()
                .format(TIME_FORMATTER);
    }

    private String formatStreamWaterTime(Long epochMilli) {
        if (epochMilli == null) {
            return null;
        }
        LocalDateTime time = Instant.ofEpochMilli(epochMilli)
                .atZone(BUSINESS_TIME_ZONE)
                .toLocalDateTime();
        int roundedMinute = (time.getMinute() / 5) * 5;
        return time
                .withMinute(roundedMinute)
                .withSecond(0)
                .withNano(0)
                .format(TIME_FORMATTER);
    }

    private Object extractPropertyValue(Map<String, IotDevicePropertyDO> properties, String identifier) {
        String normalizedIdentifier = StrUtil.trimToNull(identifier);
        if (properties == null || properties.isEmpty() || normalizedIdentifier == null) {
            return null;
        }
        return extractPropertyValue(properties, List.of(normalizedIdentifier));
    }

    private Object extractPropertyValue(Map<String, IotDevicePropertyDO> properties, Collection<String> identifiers) {
        if (properties == null || properties.isEmpty() || CollUtil.isEmpty(identifiers)) {
            return null;
        }
        for (String identifier : identifiers) {
            String normalizedIdentifier = StrUtil.trimToNull(identifier);
            if (normalizedIdentifier == null) {
                continue;
            }
            for (String candidateIdentifier : buildIdentifierCandidates(normalizedIdentifier)) {
                IotDevicePropertyDO property = properties.get(candidateIdentifier);
                if (property != null) {
                    return property.getValue();
                }
                for (Map.Entry<String, IotDevicePropertyDO> entry : properties.entrySet()) {
                    String key = StrUtil.trimToNull(entry.getKey());
                    if (isIdentifierEquivalent(key, candidateIdentifier) && entry.getValue() != null) {
                        return entry.getValue().getValue();
                    }
                }
            }
        }
        return null;
    }

    private IotScreenMetricValueRespVO resolveGateMetricValue(Long deviceId,
                                                              LocalDateTime startTime,
                                                              LocalDateTime endTime,
                                                              Collection<String> identifiers,
                                                              String propertyName) {
        IotScreenMetricValueRespVO metricValue = new IotScreenMetricValueRespVO();
        metricValue.setPropertyName(propertyName);
        MetricMatchResult metricMatchResult = queryLatestMetricMatch(deviceId, startTime, endTime, identifiers);
        if (metricMatchResult != null) {
            metricValue.setIdentifier(metricMatchResult.identifier);
            metricValue.setValue(metricMatchResult.value);
        }
        return metricValue;
    }

    private MetricMatchResult queryLatestMetricMatch(Long deviceId,
                                                     LocalDateTime startTime,
                                                     LocalDateTime endTime,
                                                     Collection<String> identifiers) {
        if (deviceId == null || startTime == null || endTime == null || CollUtil.isEmpty(identifiers)) {
            return null;
        }
        for (String identifier : identifiers) {
            String normalizedIdentifier = StrUtil.trimToNull(identifier);
            if (normalizedIdentifier == null) {
                continue;
            }
            for (String candidateIdentifier : buildIdentifierCandidates(normalizedIdentifier)) {
                IotDevicePropertyRespVO latestProperty = queryLatestHistoryProperty(
                        deviceId, candidateIdentifier, startTime, endTime);
                if (latestProperty != null) {
                    return new MetricMatchResult(candidateIdentifier, latestProperty.getValue());
                }
            }
        }
        return null;
    }

    private IotDevicePropertyRespVO queryLatestHistoryProperty(Long deviceId,
                                                               String identifier,
                                                               LocalDateTime startTime,
                                                               LocalDateTime endTime) {
        if (deviceId == null || StrUtil.isBlank(identifier) || startTime == null || endTime == null) {
            return null;
        }
        IotDevicePropertyHistoryListReqVO reqVO = new IotDevicePropertyHistoryListReqVO();
        reqVO.setDeviceId(deviceId);
        reqVO.setIdentifier(identifier);
        reqVO.setTimes(new LocalDateTime[] {startTime, endTime});
        reqVO.setLimit(1);
        List<IotDevicePropertyRespVO> historyList;
        try {
            historyList = devicePropertyService.getHistoryDevicePropertyList(reqVO);
        } catch (Exception exception) {
            if (isInvalidColumnException(exception) || isTableNotExistsException(exception)) {
                return null;
            }
            throw exception;
        }
        return CollUtil.isEmpty(historyList) ? null : historyList.get(0);
    }

    private Object firstPresentValue(Object... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (Object value : values) {
            if (isPresentValue(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isPresentValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof CharSequence charSequence) {
            return StrUtil.isNotBlank(charSequence.toString());
        }
        return true;
    }

    private List<IotScreenPumpStationHostDetailRespVO> buildPumpStationHostDetails(String stationId,
                                                                                    Integer hostDeviceType,
                                                                                    Integer statusDeviceType,
                                                                                    String statusIdentifier) {
        List<IotDeviceDO> hostDeviceList = queryDevices(stationId, hostDeviceType, null);
        if (CollUtil.isEmpty(hostDeviceList)) {
            return Collections.emptyList();
        }
        List<IotDeviceDO> statusDeviceList = Objects.equals(hostDeviceType, statusDeviceType)
                ? hostDeviceList
                : queryDevices(stationId, statusDeviceType, null);

        return convertList(hostDeviceList, hostDevice -> {
            Map<String, IotDevicePropertyDO> hostLatestProperties =
                    devicePropertyService.getLatestDeviceProperties(hostDevice.getId());
            if (hostLatestProperties == null) {
                hostLatestProperties = Collections.emptyMap();
            }
            Map<String, IotDevicePropertyDO> statusLatestProperties = resolveHostStatusProperties(
                    hostDevice, hostDeviceType, statusDeviceType, hostLatestProperties, statusDeviceList);
            Object statusValue = extractPropertyValue(statusLatestProperties, statusIdentifier);
            Object activePower = extractPropertyValue(hostLatestProperties, ACTIVE_POWER_IDENTIFIERS);
            LocalDateTime collectTime = extractPropertyUpdateTime(hostLatestProperties, ACTIVE_POWER_IDENTIFIERS);
            if (collectTime == null) {
                collectTime = extractPropertyUpdateTime(statusLatestProperties, List.of(statusIdentifier));
            }

            IotScreenPumpStationHostDetailRespVO respVO = new IotScreenPumpStationHostDetailRespVO();
            respVO.setHostName(hostDevice.getDeviceName());
            respVO.setHostStatus(statusValue);
            respVO.setActivePower(activePower);
            respVO.setCollectTime(collectTime == null ? null : collectTime.format(TIME_FORMATTER));
            return respVO;
        });
    }

    private Map<String, IotDevicePropertyDO> resolveHostStatusProperties(IotDeviceDO hostDevice,
                                                                          Integer hostDeviceType,
                                                                          Integer statusDeviceType,
                                                                          Map<String, IotDevicePropertyDO> hostLatestProperties,
                                                                          List<IotDeviceDO> statusDeviceList) {
        if (Objects.equals(hostDeviceType, statusDeviceType)) {
            return hostLatestProperties;
        }
        IotDeviceDO matchedStatusDevice = matchPumpDeviceByHostNamePrefix(hostDevice.getDeviceName(), statusDeviceList);
        if (matchedStatusDevice == null) {
            return Collections.emptyMap();
        }
        Map<String, IotDevicePropertyDO> statusLatestProperties =
                devicePropertyService.getLatestDeviceProperties(matchedStatusDevice.getId());
        return statusLatestProperties == null ? Collections.emptyMap() : statusLatestProperties;
    }

    private IotScreenStationHostStatusRespVO buildStationHostStatus(DictDataRespDTO stationDictData) {
        String stationValue = StrUtil.trimToEmpty(stationDictData.getValue());
        IotScreenStationHostStatusRespVO respVO = new IotScreenStationHostStatusRespVO();
        respVO.setStationId(stationValue);
        respVO.setStationName(stationDictData.getLabel());

        Integer queryDeviceType;
        String statusIdentifier;
        switch (stationValue) {
            case STATION_SHA_HE -> {
                queryDeviceType = STATION_HOST_DEVICE_TYPE_FIVE;
                statusIdentifier = MAIN_UNIT_RUNNING_IDENTIFIER;
            }
            case STATION_JIN_DOU_HE -> {
                queryDeviceType = STATION_HOST_DEVICE_TYPE_FIVE;
                statusIdentifier = IS_RUNNING_IDENTIFIER;
            }
            case STATION_PAN_JIA_HE -> {
                queryDeviceType = STATION_HOST_DEVICE_TYPE_EIGHT;
                statusIdentifier = IS_RUNNING_IDENTIFIER;
            }
            case STATION_SHI_ER_WEI, STATION_LEGACY_SHI_ER_WEI -> {
                queryDeviceType = STATION_HOST_DEVICE_TYPE_THREE;
                statusIdentifier = RUN_IDENTIFIER;
            }
            default -> {
                respVO.setRunningCount(0L);
                respVO.setStopCount(0L);
                return respVO;
            }
        }

        List<IotDeviceDO> deviceList = queryDevices(stationValue, queryDeviceType, null);
        if (CollUtil.isEmpty(deviceList)) {
            respVO.setRunningCount(0L);
            respVO.setStopCount(0L);
            return respVO;
        }

        long runningCount = 0L;
        for (IotDeviceDO device : deviceList) {
            Map<String, IotDevicePropertyDO> latestProperties = devicePropertyService.getLatestDeviceProperties(device.getId());
            Object statusValue = extractPropertyValue(latestProperties, statusIdentifier);
            if (isRunningStatus(statusValue)) {
                runningCount++;
            }
        }
        respVO.setRunningCount(runningCount);
        respVO.setStopCount((long) deviceList.size() - runningCount);
        return respVO;
    }

    private boolean isRunningStatus(Object statusValue) {
        if (statusValue == null) {
            return false;
        }
        if (statusValue instanceof Number numberValue) {
            return numberValue.intValue() == 1;
        }
        String normalizedValue = StrUtil.trimToNull(String.valueOf(statusValue));
        return "1".equals(normalizedValue);
    }

    private Long toLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        BigDecimal decimalValue = parseBigDecimal(value);
        return decimalValue == null ? 0L : decimalValue.longValue();
    }

    /**
     * 兼容 TDengine 时间字段返回的多种类型，并统一转换为毫秒时间戳。
     */
    private Long resolveEpochMilli(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number numberValue) {
            return normalizeEpochMilli(numberValue.longValue());
        }
        if (value instanceof Date dateValue) {
            return dateValue.getTime();
        }
        if (value instanceof LocalDateTime localDateTimeValue) {
            return localDateTimeValue.atZone(BUSINESS_TIME_ZONE).toInstant().toEpochMilli();
        }
        if (value instanceof Instant instantValue) {
            return instantValue.toEpochMilli();
        }
        String text = StrUtil.trimToNull(String.valueOf(value));
        if (text == null) {
            return null;
        }
        if (StrUtil.isNumeric(text)) {
            return normalizeEpochMilli(Long.parseLong(text));
        }
        try {
            return OffsetDateTime.parse(text).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            // ignore
        }
        try {
            return OffsetDateTime.parse(StrUtil.replace(text, " ", "T")).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            // ignore
        }
        String normalizedText = StrUtil.replace(text, "T", " ");
        try {
            return java.sql.Timestamp.valueOf(normalizedText).getTime();
        } catch (IllegalArgumentException ignored) {
            // ignore
        }
        try {
            return Instant.parse(text).toEpochMilli();
        } catch (DateTimeParseException ignored) {
            // ignore
        }
        return null;
    }

    private Long normalizeEpochMilli(Long rawTimestamp) {
        if (rawTimestamp == null) {
            return null;
        }
        long abs = Math.abs(rawTimestamp);
        if (abs >= 1_000_000_000_000_000_000L) {
            return rawTimestamp / 1_000_000L; // 纳秒 -> 毫秒
        }
        if (abs >= 1_000_000_000_000_000L) {
            return rawTimestamp / 1_000L; // 微秒 -> 毫秒
        }
        if (abs <= 9_999_999_999L) {
            return rawTimestamp * 1_000L; // 秒 -> 毫秒
        }
        return rawTimestamp; // 毫秒
    }

    private BigDecimal toBigDecimalValue(Object value) {
        BigDecimal decimalValue = parseBigDecimal(value);
        return decimalValue == null ? BigDecimal.ZERO : decimalValue;
    }

    private IotDeviceDO matchPumpDeviceByHostNamePrefix(String hostDeviceName, List<IotDeviceDO> pumpDeviceList) {
        if (CollUtil.isEmpty(pumpDeviceList)) {
            return null;
        }
        Integer hostPrefixNumber = extractDeviceNamePrefixNumber(hostDeviceName);
        if (hostPrefixNumber != null) {
            for (IotDeviceDO pumpDevice : pumpDeviceList) {
                Integer pumpPrefixNumber = extractDeviceNamePrefixNumber(pumpDevice.getDeviceName());
                if (Objects.equals(hostPrefixNumber, pumpPrefixNumber)) {
                    return pumpDevice;
                }
            }
        }
        String normalizedHostName = normalizeDeviceNameForPrefixMatchSafe(hostDeviceName);
        if (normalizedHostName == null) {
            return null;
        }
        String prefix = getDeviceNamePrefix(normalizedHostName);
        for (IotDeviceDO pumpDevice : pumpDeviceList) {
            String pumpDeviceName = normalizeDeviceNameForPrefixMatchSafe(pumpDevice.getDeviceName());
            if (pumpDeviceName == null) {
                continue;
            }
            if (pumpDeviceName.startsWith(prefix) || pumpDeviceName.contains(prefix)) {
                return pumpDevice;
            }
        }
        return null;
    }

    /**
     * 名称匹配归一化：将 # / ＃ / 号 与数字表达（1-6 / 一-六）视为等价。
     */
    private String normalizeDeviceNameForPrefixMatch(String deviceName) {
        String normalizedName = StrUtil.trimToNull(deviceName);
        if (normalizedName == null) {
            return null;
        }
        normalizedName = StrUtil.replace(normalizedName, "＃", "#");
        normalizedName = StrUtil.replace(normalizedName, "号", "#");
        normalizedName = normalizedName.replaceAll("\\s+", "");
        normalizedName = normalizeNumberAlias(normalizedName);
        return StrUtil.trimToNull(normalizedName);
    }

    private String normalizeNumberAlias(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '一', '１' -> builder.append('1');
                case '二', '２' -> builder.append('2');
                case '三', '３' -> builder.append('3');
                case '四', '４' -> builder.append('4');
                case '五', '５' -> builder.append('5');
                case '六', '６' -> builder.append('6');
                default -> builder.append(current);
            }
        }
        return builder.toString();
    }

    private String getDeviceNamePrefix(String deviceName) {
        return deviceName.length() >= 2 ? deviceName.substring(0, 2) : deviceName;
    }

    private IotScreenEngineeringGateSummaryRespVO buildGateSummaryRespVO(String stationId, String stationName,
                                                                          Long gateTotalCount, Long gateCloseAllCount,
                                                                          Long gateOpenAllCount) {
        IotScreenEngineeringGateSummaryRespVO respVO = new IotScreenEngineeringGateSummaryRespVO();
        respVO.setStationId(stationId);
        respVO.setStationName(stationName);
        respVO.setGateTotalCount(defaultCount(gateTotalCount));
        respVO.setGateCloseAllCount(defaultCount(gateCloseAllCount));
        respVO.setGateOpenAllCount(defaultCount(gateOpenAllCount));
        return respVO;
    }

    private IotScreenEngineeringRuntimeStatRespVO buildRuntimeStatRespVO(String stationId, String stationName,
                                                                         Long deviceId, String deviceName,
                                                                         Long startCount, BigDecimal totalStartDuration,
                                                                         BigDecimal startDuration,
                                                                         LocalDateTime startDurationCollectTime) {
        IotScreenEngineeringRuntimeStatRespVO respVO = new IotScreenEngineeringRuntimeStatRespVO();
        respVO.setStationId(stationId);
        respVO.setStationName(stationName);
        respVO.setDeviceId(deviceId);
        respVO.setDeviceName(deviceName);
        respVO.setStartCount(defaultCount(startCount));
        respVO.setTotalStartDuration(roundDurationValue(totalStartDuration));
        respVO.setStartDuration(roundDurationValue(startDuration));
        respVO.setStartDurationCollectTime(startDurationCollectTime == null
                ? null : startDurationCollectTime.format(TIME_FORMATTER));
        return respVO;
    }

    private BigDecimal roundDurationValue(BigDecimal value) {
        BigDecimal normalizedValue = value == null ? BigDecimal.ZERO : value;
        return normalizedValue.setScale(2, RoundingMode.HALF_UP);
    }

    private List<IotScreenWorkConditionRespVO> listWorkConditionByBatchRange(IotDeviceDO device,
                                                                  LocalDateTime[] queryTimes,
                                                                  Map<Long, Set<String>> productFieldCache,
                                                                  Map<String, String> stationLabelMap) {
        if (device == null || queryTimes == null || queryTimes.length < 2) {
            return Collections.emptyList();
        }
        Map<String, String> metricColumnMap = resolveWorkConditionMetricColumnMap(device.getProductId(), productFieldCache);
        LinkedHashSet<String> queryColumnSet = new LinkedHashSet<>(resolveMetricQueryColumns(metricColumnMap));
        if (queryColumnSet.isEmpty()) {
            return List.of(buildLatestWorkConditionForDevice(device.getStationId(), device, stationLabelMap));
        }

        List<Map<String, Object>> rows;
        try {
            rows = queryWorkConditionRows(device.getId(), queryTimes, new ArrayList<>(queryColumnSet));
        } catch (Exception exception) {
            if (isTableNotExistsException(exception)) {
                return List.of(buildLatestWorkConditionForDevice(device.getStationId(), device, stationLabelMap));
            }
            throw exception;
        }
        if (CollUtil.isEmpty(rows)) {
            // 历史无点：回退最新属性，保证闸门电压/功率/状态能展示
            return List.of(buildLatestWorkConditionForDevice(device.getStationId(), device, stationLabelMap));
        }

        String stationName = StrUtil.blankToDefault(stationLabelMap.get(device.getStationId()), device.getStationId());
        List<IotScreenWorkConditionRespVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            IotScreenWorkConditionRespVO respVO = new IotScreenWorkConditionRespVO();
            respVO.setStationId(device.getStationId());
            respVO.setStationName(stationName);
            respVO.setDeviceId(device.getId());
            respVO.setDeviceName(device.getDeviceName());
            respVO.setDeviceType(device.getDeviceType());
            respVO.setDeviceTypeName(resolveDeviceTypeName(device.getDeviceType()));
            respVO.setTime(formatTime(resolveEpochMilli(getCaseInsensitiveMapValue(row, "update_time"))));
            respVO.setActivePowerKw(getWorkConditionMetricValue(row, metricColumnMap.get("activePowerKw")));
            respVO.setReactivePowerKvar(getWorkConditionMetricValue(row, metricColumnMap.get("reactivePowerKvar")));
            respVO.setPowerFactor(getWorkConditionMetricValue(row, metricColumnMap.get("powerFactor")));
            respVO.setFrequency(getWorkConditionMetricValue(row, metricColumnMap.get("frequency")));
            respVO.setAbVoltage(getWorkConditionMetricValue(row, metricColumnMap.get("abVoltage")));
            respVO.setBcVoltage(getWorkConditionMetricValue(row, metricColumnMap.get("bcVoltage")));
            respVO.setCaVoltage(getWorkConditionMetricValue(row, metricColumnMap.get("caVoltage")));
            respVO.setACurrent(getWorkConditionMetricValue(row, metricColumnMap.get("aCurrent")));
            respVO.setBCurrent(getWorkConditionMetricValue(row, metricColumnMap.get("bCurrent")));
            respVO.setCCurrent(getWorkConditionMetricValue(row, metricColumnMap.get("cCurrent")));
            respVO.setAStatorTemp1(getWorkConditionMetricValue(row, metricColumnMap.get("aStatorTemp1")));
            respVO.setBStatorTemp1(getWorkConditionMetricValue(row, metricColumnMap.get("bStatorTemp1")));
            respVO.setCStatorTemp1(getWorkConditionMetricValue(row, metricColumnMap.get("cStatorTemp1")));
            respVO.setAStatorTemp2(getWorkConditionMetricValue(row, metricColumnMap.get("aStatorTemp2")));
            respVO.setBStatorTemp2(getWorkConditionMetricValue(row, metricColumnMap.get("bStatorTemp2")));
            respVO.setCStatorTemp2(getWorkConditionMetricValue(row, metricColumnMap.get("cStatorTemp2")));
            respVO.setIsGateOpenAll(getWorkConditionMetricValue(row, metricColumnMap.get("isGateOpenAll")));
            respVO.setIsGateCloseAll(getWorkConditionMetricValue(row, metricColumnMap.get("isGateCloseAll")));
            respVO.setIsGateUp(getWorkConditionMetricValue(row, metricColumnMap.get("isGateUp")));
            respVO.setIsGateDown(getWorkConditionMetricValue(row, metricColumnMap.get("isGateDown")));
            respVO.setIsGateFailure(getWorkConditionMetricValue(row, metricColumnMap.get("isGateFailure")));
            respVO.setIsGatePowerOn(getWorkConditionMetricValue(row, metricColumnMap.get("isGatePowerOn")));
            respVO.setFloodgateOpening(getWorkConditionMetricValue(row, metricColumnMap.get("floodgateOpening")));
            respVO.setWaterLevel(getWorkConditionMetricValue(row, metricColumnMap.get("waterLevel")));
            respVO.setPressValue(getWorkConditionMetricValue(row, metricColumnMap.get("pressValue")));
            result.add(respVO);
        }
        return result;
    }

    private IotScreenWorkConditionRespVO buildEmptyWorkConditionResp(IotDeviceDO device,
                                                                      Map<String, String> stationLabelMap) {
        IotScreenWorkConditionRespVO respVO = new IotScreenWorkConditionRespVO();
        respVO.setStationId(device.getStationId());
        respVO.setStationName(StrUtil.blankToDefault(stationLabelMap.get(device.getStationId()),
                device.getStationId()));
        respVO.setDeviceId(device.getId());
        respVO.setDeviceName(device.getDeviceName());
        respVO.setDeviceType(device.getDeviceType());
        respVO.setDeviceTypeName(resolveDeviceTypeName(device.getDeviceType()));
        return respVO;
    }

    private List<Map<String, Object>> queryWorkConditionRows(Long deviceId, LocalDateTime[] queryTimes,
                                                             Collection<String> queryColumns) {
        if (deviceId == null || queryTimes == null || queryTimes.length < 2 || CollUtil.isEmpty(queryColumns)) {
            return Collections.emptyList();
        }
        List<String> columns = queryColumns.stream()
                .map(StrUtil::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(columns)) {
            return Collections.emptyList();
        }
        long startTs = queryTimes[0].atZone(BUSINESS_TIME_ZONE).toInstant().toEpochMilli();
        long endTs = queryTimes[1].atZone(BUSINESS_TIME_ZONE).toInstant().toEpochMilli();
        try {
            return devicePropertyMapper.selectColumnsInRange(deviceId, columns, startTs, endTs);
        } catch (Exception exception) {
            if (isTableNotExistsException(exception)) {
                return Collections.emptyList();
            }
            throw exception;
        }
    }

    private Set<String> getProductPropertyFields(Long productId, Map<Long, Set<String>> productFieldCache) {
        if (productId == null) {
            return Collections.emptySet();
        }
        return productFieldCache.computeIfAbsent(productId, id -> {
            List<TDengineTableField> tableFieldList;
            try {
                tableFieldList = devicePropertyMapper.getProductPropertySTableFieldList(id);
            } catch (Exception exception) {
                if (isTableNotExistsException(exception)) {
                    return Collections.emptySet();
                }
                throw exception;
            }
            if (CollUtil.isEmpty(tableFieldList)) {
                return Collections.emptySet();
            }
            return tableFieldList.stream()
                    .map(TDengineTableField::getField)
                    .map(StrUtil::trimToNull)
                    .filter(Objects::nonNull)
                    .map(field -> StrUtil.removeAll(field, '`', '"').toLowerCase(Locale.ROOT))
                    .filter(field -> !StrUtil.equalsAny(field, TDengineTableField.FIELD_TS, "report_time", "device_id"))
                    .collect(Collectors.toSet());
        });
    }

    private String resolveWorkConditionColumn(Collection<String> identifiers, Set<String> availableColumns) {
        if (CollUtil.isEmpty(identifiers) || CollUtil.isEmpty(availableColumns)) {
            return null;
        }
        for (String identifier : identifiers) {
            String normalizedIdentifier = StrUtil.trimToNull(identifier);
            if (normalizedIdentifier == null) {
                continue;
            }
            for (String candidateIdentifier : buildIdentifierCandidates(normalizedIdentifier)) {
                String normalizedCandidate = StrUtil.trimToNull(candidateIdentifier);
                if (normalizedCandidate == null) {
                    continue;
                }
                String directColumn = normalizedCandidate.toLowerCase(Locale.ROOT);
                if (availableColumns.contains(directColumn)) {
                    return directColumn;
                }
                String underlineColumn = StrUtil.toUnderlineCase(normalizedCandidate).toLowerCase(Locale.ROOT);
                if (availableColumns.contains(underlineColumn)) {
                    return underlineColumn;
                }
            }
        }
        return null;
    }

    private String normalizeDeviceNameForPrefixMatchSafe(String deviceName) {
        String normalizedName = StrUtil.trimToNull(deviceName);
        if (normalizedName == null) {
            return null;
        }
        normalizedName = normalizedName.replace('\uFF03', '#');
        normalizedName = StrUtil.replace(normalizedName, "\u53F7", "#");
        normalizedName = normalizedName.replaceAll("\\s+", "");
        normalizedName = normalizeNumberAliasSafe(normalizedName);
        return StrUtil.trimToNull(normalizedName);
    }

    private String normalizeNumberAliasSafe(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current >= '\uFF10' && current <= '\uFF19') {
                builder.append((char) (current - 65248));
                continue;
            }
            switch (current) {
                case '\u4E00', '\u58F9' -> builder.append('1');
                case '\u4E8C', '\u8D30', '\u4E24' -> builder.append('2');
                case '\u4E09', '\u53C1' -> builder.append('3');
                case '\u56DB', '\u8086' -> builder.append('4');
                case '\u4E94', '\u4F0D' -> builder.append('5');
                case '\u516D', '\u9646' -> builder.append('6');
                case '\u4E03', '\u67D2' -> builder.append('7');
                case '\u516B', '\u634C' -> builder.append('8');
                case '\u4E5D', '\u7396' -> builder.append('9');
                case '\u96F6', '\u3007' -> builder.append('0');
                default -> builder.append(current);
            }
        }
        return builder.toString();
    }

    private Object getCaseInsensitiveMapValue(Map<String, Object> data, String key) {
        String normalizedKey = StrUtil.trimToNull(key);
        if (data == null || data.isEmpty() || normalizedKey == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String entryKey = StrUtil.trimToNull(entry.getKey());
            if (isIdentifierEquivalent(entryKey, normalizedKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 工情统计在传入时间范围时，优先取时间范围内最新值；未传时间范围时沿用缓存最新值。
     */
    private Object extractWorkConditionValue(Long deviceId, Map<String, IotDevicePropertyDO> latestProperties,
                                             Collection<String> identifiers, LocalDateTime[] queryTimes) {
        if (queryTimes == null) {
            return extractPropertyValue(latestProperties, identifiers);
        }
        if (CollUtil.isEmpty(identifiers)) {
            return null;
        }
        for (String identifier : identifiers) {
            String normalizedIdentifier = StrUtil.trimToNull(identifier);
            if (normalizedIdentifier == null) {
                continue;
            }
            for (String candidateIdentifier : buildIdentifierCandidates(normalizedIdentifier)) {
                IotDevicePropertyHistoryListReqVO historyReqVO = new IotDevicePropertyHistoryListReqVO();
                historyReqVO.setDeviceId(deviceId);
                historyReqVO.setIdentifier(candidateIdentifier);
                historyReqVO.setTimes(queryTimes);
                historyReqVO.setLimit(1);
                List<IotDevicePropertyRespVO> historyList;
                try {
                    historyList = devicePropertyService.getHistoryDevicePropertyList(historyReqVO);
                } catch (Exception exception) {
                    // 部分设备指标字段命名不统一，不存在的列继续尝试下一个候选标识
                    if (isInvalidColumnException(exception)) {
                        continue;
                    }
                    throw exception;
                }
                if (CollUtil.isNotEmpty(historyList)) {
                    return historyList.get(0).getValue();
                }
            }
        }
        return null;
    }

    /**
     * 工情列表时间列：未传时间范围时取缓存指标最新更新时间；传时间范围时取范围内最新指标时间。
     */
    private String resolveWorkConditionTime(Long deviceId, Map<String, IotDevicePropertyDO> latestProperties,
                                            LocalDateTime[] queryTimes) {
        if (queryTimes == null) {
            LocalDateTime latestUpdateTime = null;
            for (List<String> identifiers : WORK_CONDITION_IDENTIFIER_GROUPS) {
                LocalDateTime candidateUpdateTime = extractPropertyUpdateTime(latestProperties, identifiers);
                if (candidateUpdateTime != null
                        && (latestUpdateTime == null || candidateUpdateTime.isAfter(latestUpdateTime))) {
                    latestUpdateTime = candidateUpdateTime;
                }
            }
            return latestUpdateTime == null ? null : latestUpdateTime.format(TIME_FORMATTER);
        }
        Long latestEpochMilli = null;
        for (List<String> identifiers : WORK_CONDITION_IDENTIFIER_GROUPS) {
            Long candidateEpochMilli = queryLatestWorkConditionUpdateTime(deviceId, identifiers, queryTimes);
            if (candidateEpochMilli != null && (latestEpochMilli == null || candidateEpochMilli > latestEpochMilli)) {
                latestEpochMilli = candidateEpochMilli;
            }
        }
        return formatTime(latestEpochMilli);
    }

    private LocalDateTime extractPropertyUpdateTime(Map<String, IotDevicePropertyDO> properties,
                                                    Collection<String> identifiers) {
        if (properties == null || properties.isEmpty() || CollUtil.isEmpty(identifiers)) {
            return null;
        }
        for (String identifier : identifiers) {
            String normalizedIdentifier = StrUtil.trimToNull(identifier);
            if (normalizedIdentifier == null) {
                continue;
            }
            for (String candidateIdentifier : buildIdentifierCandidates(normalizedIdentifier)) {
                IotDevicePropertyDO property = properties.get(candidateIdentifier);
                if (property != null && property.getUpdateTime() != null) {
                    return property.getUpdateTime();
                }
                for (Map.Entry<String, IotDevicePropertyDO> entry : properties.entrySet()) {
                    String key = StrUtil.trimToNull(entry.getKey());
                    IotDevicePropertyDO entryProperty = entry.getValue();
                    if (isIdentifierEquivalent(key, candidateIdentifier)
                            && entryProperty != null && entryProperty.getUpdateTime() != null) {
                        return entryProperty.getUpdateTime();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 取设备全部最新属性中的最大更新时间，作为采集时间兜底。
     */
    private LocalDateTime resolveLatestPropertyUpdateTime(Map<String, IotDevicePropertyDO> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        LocalDateTime latest = null;
        for (IotDevicePropertyDO property : properties.values()) {
            if (property == null || property.getUpdateTime() == null) {
                continue;
            }
            if (latest == null || property.getUpdateTime().isAfter(latest)) {
                latest = property.getUpdateTime();
            }
        }
        return latest;
    }

    private Long queryLatestWorkConditionUpdateTime(Long deviceId, Collection<String> identifiers,
                                                    LocalDateTime[] queryTimes) {
        if (CollUtil.isEmpty(identifiers) || queryTimes == null) {
            return null;
        }
        for (String identifier : identifiers) {
            String normalizedIdentifier = StrUtil.trimToNull(identifier);
            if (normalizedIdentifier == null) {
                continue;
            }
            for (String candidateIdentifier : buildIdentifierCandidates(normalizedIdentifier)) {
                IotDevicePropertyHistoryListReqVO historyReqVO = new IotDevicePropertyHistoryListReqVO();
                historyReqVO.setDeviceId(deviceId);
                historyReqVO.setIdentifier(candidateIdentifier);
                historyReqVO.setTimes(queryTimes);
                historyReqVO.setLimit(1);
                List<IotDevicePropertyRespVO> historyList;
                try {
                    historyList = devicePropertyService.getHistoryDevicePropertyList(historyReqVO);
                } catch (Exception exception) {
                    if (isInvalidColumnException(exception)) {
                        continue;
                    }
                    throw exception;
                }
                if (CollUtil.isNotEmpty(historyList)) {
                    return historyList.get(0).getUpdateTime();
                }
            }
        }
        return null;
    }

    private List<String> buildIdentifierCandidates(String identifier) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        String normalizedIdentifier = StrUtil.trimToNull(identifier);
        if (normalizedIdentifier == null) {
            return Collections.emptyList();
        }
        appendIdentifierVariants(candidates, normalizedIdentifier);
        appendIdentifierVariants(candidates, StrUtil.toUnderlineCase(normalizedIdentifier));
        appendIdentifierVariants(candidates, StrUtil.toCamelCase(normalizedIdentifier));
        appendIdentifierVariants(candidates, StrUtil.replace(normalizedIdentifier, "_", ""));
        return new ArrayList<>(candidates);
    }

    private void appendIdentifierVariants(Set<String> candidates, String identifier) {
        String normalizedIdentifier = StrUtil.trimToNull(identifier);
        if (normalizedIdentifier == null) {
            return;
        }
        candidates.add(normalizedIdentifier);
        candidates.add(StrUtil.lowerFirst(normalizedIdentifier));
        candidates.add(StrUtil.upperFirst(normalizedIdentifier));
        candidates.add(normalizedIdentifier.toLowerCase(Locale.ROOT));
        candidates.add(normalizedIdentifier.toUpperCase(Locale.ROOT));
    }

    private boolean isIdentifierEquivalent(String left, String right) {
        String normalizedLeft = normalizeIdentifierKey(left);
        String normalizedRight = normalizeIdentifierKey(right);
        if (normalizedLeft == null || normalizedRight == null) {
            return false;
        }
        return StrUtil.equalsIgnoreCase(normalizedLeft, normalizedRight);
    }

    private String normalizeIdentifierKey(String identifier) {
        String normalizedIdentifier = StrUtil.trimToNull(identifier);
        if (normalizedIdentifier == null) {
            return null;
        }
        return StrUtil.replace(normalizedIdentifier, "_", "").toLowerCase(Locale.ROOT);
    }

    private static class MetricMatchResult {
        private final String identifier;
        private final Object value;

        private MetricMatchResult(String identifier, Object value) {
            this.identifier = identifier;
            this.value = value;
        }
    }

    private boolean isInvalidColumnException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = StrUtil.trimToEmpty(current.getMessage());
            if (StrUtil.containsIgnoreCase(message, "invalid column name")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isTableNotExistsException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = StrUtil.trimToEmpty(current.getMessage());
            if (StrUtil.containsIgnoreCase(message, "table does not exist")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Long defaultCount(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal roundToScale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(RESERVOIR_SUMMARY_SCALE, RoundingMode.HALF_UP);
        }
        return value.setScale(RESERVOIR_SUMMARY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal getMapBigDecimal(Map<String, Object> dataMap, String key) {
        if (dataMap == null || dataMap.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Object value = dataMap.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimalValue) {
            return decimalValue;
        }
        if (value instanceof Number numberValue) {
            return new BigDecimal(numberValue.toString());
        }
        String strValue = StrUtil.trimToNull(value.toString());
        if (strValue == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(strValue);
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * stream-water 接口时间参数规则：
     * 1. timeRange 与 startTime/endTime 互斥；
     * 2. 仅传 start/end 时按自定义区间；
     * 3. 都不传时默认按 24h。
     */
    private LocalDateTime[] resolveStreamWaterQueryTimes(LocalDateTime startTime, LocalDateTime endTime,
                                                         String timeRange) {
        boolean hasTimeRange = StrUtil.isNotBlank(StrUtil.trimToNull(timeRange));
        boolean hasStartOrEnd = startTime != null || endTime != null;
        if (hasTimeRange && hasStartOrEnd) {
            throw invalidParamException("timeRange 与 startTime、endTime 不能同时传入");
        }
        if (hasStartOrEnd) {
            return resolveQueryTimes(startTime, endTime, null);
        }
        String finalTimeRange = hasTimeRange ? timeRange : DEFAULT_TIME_RANGE;
        return resolveQueryTimes(null, null, finalTimeRange);
    }

    private Object roundStreamWaterLevel(Object rawValue) {
        BigDecimal numericValue = parseBigDecimal(rawValue);
        if (numericValue == null) {
            return rawValue;
        }
        return numericValue.setScale(STREAM_WATER_LEVEL_SCALE, RoundingMode.HALF_UP);
    }

    private List<String> normalizeSlssValues(List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            if (StrUtil.isBlank(value)) {
                continue;
            }
            String normalizedValue = StrUtil.replace(value, "，", ",");
            Arrays.stream(normalizedValue.split(","))
                    .map(StrUtil::trimToEmpty)
                    .filter(StrUtil::isNotBlank)
                    .filter(SUPPORTED_SLSS_VALUES::contains)
                    .forEach(normalized::add);
        }
        return new ArrayList<>(normalized);
    }

    private Map<String, String> loadDictLabelMap(String dictType) {
        List<DictDataRespDTO> dictDataList = dictDataCommonApi.getDictDataList(dictType);
        if (CollUtil.isEmpty(dictDataList)) {
            return Collections.emptyMap();
        }
        return dictDataList.stream()
                .filter(dictData -> Objects.equals(dictData.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                .filter(dictData -> StrUtil.isNotBlank(dictData.getValue()))
                .collect(Collectors.toMap(DictDataRespDTO::getValue,
                        dictData -> StrUtil.blankToDefault(dictData.getLabel(), dictData.getValue()),
                        (oldValue, newValue) -> oldValue));
    }

    private List<Map<String, Object>> queryFacilityItemsBySlssValue(String value,
                                                                    Map<String, String> pumpStationTypeLabelMap,
                                                                    Map<String, String> materialUnitLabelMap,
                                                                    Long areaId) {
        return switch (value) {
            case "0" -> queryRiverItems(wrapper -> wrapper.in(YzRiverChannelDO::getRiverName, WATER_SYSTEM_RIVER_NAMES));
            case "1" -> queryRiverItems(null);
            case "2" -> queryPumpStationItems(pumpStationTypeLabelMap);
            case "3" -> queryReservoirItems();
            case "4" -> queryVideoCameraItems();
            case "5" -> queryRiverItems(wrapper -> wrapper.eq(YzRiverChannelDO::getIsProvincialBackbone,
                    PROVINCIAL_BACKBONE_YES));
            case "6" -> queryRiverItems(wrapper -> wrapper.eq(YzRiverChannelDO::getEcologyType, ECOLOGY_TYPE_STHD));
            case "7" -> queryRiverItems(wrapper -> wrapper.eq(YzRiverChannelDO::getEcologyType, ECOLOGY_TYPE_BFSTHD));
            case "8" -> queryRiverItems(wrapper -> wrapper.eq(YzRiverChannelDO::getEcologyType, ECOLOGY_TYPE_FSTHD));
            case "9" -> queryFloodWarehouseItems(materialUnitLabelMap);
            case "10" -> queryWeatherStationItems();
            case "11" -> queryIrrigationDistrictItems();
            case "12" -> querySignboardItems();
            case "13" -> queryProblemFeedbackItems();
            case "14" -> queryPondItems(areaId);
            default -> Collections.emptyList();
        };
    }

    private List<Map<String, Object>> queryVideoCameraItems() {
        Set<String> excludedScreenRegions = yzVideoScreenExcludeResolver.excludedRegionIndexCodes();
        List<YzVideoCameraDO> cameraList = yzVideoCameraMapper.selectList(new LambdaQueryWrapper<YzVideoCameraDO>()
                .select(YzVideoCameraDO::getCameraName,
                        YzVideoCameraDO::getCameraIndexCode,
                        YzVideoCameraDO::getRegionIndexCode,
                        YzVideoCameraDO::getLon,
                        YzVideoCameraDO::getLat,
                        YzVideoCameraDO::getOnline)
                .orderByAsc(YzVideoCameraDO::getCameraName)
                .orderByDesc(YzVideoCameraDO::getCameraIndexCode));
        if (CollUtil.isNotEmpty(excludedScreenRegions) && CollUtil.isNotEmpty(cameraList)) {
            cameraList = cameraList.stream()
                    .filter(camera -> {
                        String regionIndexCode = camera.getRegionIndexCode();
                        if (StrUtil.isBlank(regionIndexCode)) {
                            return true;
                        }
                        return !excludedScreenRegions.contains(StrUtil.trim(regionIndexCode));
                    })
                    .collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(cameraList)) {
            return Collections.emptyList();
        }
        return convertList(cameraList, camera -> buildMapItem(
                "cameraName", camera.getCameraName(),
                "cameraIndexCode", camera.getCameraIndexCode(),
                "regionIndexCode", camera.getRegionIndexCode(),
                "lon", camera.getLon(),
                "lat", camera.getLat(),
                "online", camera.getOnline()));
    }

    private List<Map<String, Object>> queryRiverItems(Consumer<LambdaQueryWrapper<YzRiverChannelDO>> customizer) {
        LambdaQueryWrapper<YzRiverChannelDO> queryWrapper = new LambdaQueryWrapper<YzRiverChannelDO>()
                .select(YzRiverChannelDO::getFacilityId,
                        YzRiverChannelDO::getRiverName,
                        YzRiverChannelDO::getLengthKm,
                        YzRiverChannelDO::getCatchmentKm2)
                .orderByAsc(YzRiverChannelDO::getRiverName)
                .orderByDesc(YzRiverChannelDO::getId);
        if (customizer != null) {
            customizer.accept(queryWrapper);
        }
        List<YzRiverChannelDO> riverList = riverChannelMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(riverList)) {
            return Collections.emptyList();
        }
        Map<Long, String> gisDataMap = buildGisDataMap(convertList(riverList, YzRiverChannelDO::getFacilityId));
        return convertList(riverList, river -> {
            Map<String, Object> item = buildMapItem(
                    "riverName", river.getRiverName(),
                    "lengthKm", river.getLengthKm(),
                    "catchmentKm2", river.getCatchmentKm2());
            appendGisData(item, river.getFacilityId(), gisDataMap);
            return item;
        });
    }

    private List<Map<String, Object>> queryPumpStationItems(Map<String, String> pumpStationTypeLabelMap) {
        List<YzPumpStationDO> pumpStationList = pumpStationMapper.selectList(new LambdaQueryWrapper<YzPumpStationDO>()
                .select(YzPumpStationDO::getFacilityId,
                        YzPumpStationDO::getPumpStationName,
                        YzPumpStationDO::getLongitude,
                        YzPumpStationDO::getLatitude,
                        YzPumpStationDO::getPumpStationType,
                        YzPumpStationDO::getSelfFlow,
                        YzPumpStationDO::getInstalledFlow,
                        YzPumpStationDO::getPumpingFlow,
                        YzPumpStationDO::getCapacityFlow,
                        YzPumpStationDO::getUnitCount,
                        YzPumpStationDO::getInstalledCapacity)
                .orderByAsc(YzPumpStationDO::getPumpStationName)
                .orderByDesc(YzPumpStationDO::getId));
        if (CollUtil.isEmpty(pumpStationList)) {
            return Collections.emptyList();
        }
        Map<Long, String> gisDataMap = buildGisDataMap(convertList(pumpStationList, YzPumpStationDO::getFacilityId));
        return convertList(pumpStationList, pumpStation -> {
            String pumpStationTypeValue = StrUtil.trimToEmpty(pumpStation.getPumpStationType());
            String pumpStationTypeLabel = StrUtil.blankToDefault(
                    pumpStationTypeLabelMap.get(pumpStationTypeValue), pumpStationTypeValue);
            Map<String, Object> item = buildMapItem(
                    "pumpStationName", pumpStation.getPumpStationName(),
                    "longitude", pumpStation.getLongitude(),
                    "latitude", pumpStation.getLatitude(),
                    "pumpStationType", pumpStationTypeLabel,
                    "selfFlow", pumpStation.getSelfFlow(),
                    "installedFlow", pumpStation.getInstalledFlow(),
                    "pumpingFlow", pumpStation.getPumpingFlow(),
                    "capacityFlow", pumpStation.getCapacityFlow(),
                    "unitCount", pumpStation.getUnitCount(),
                    // 字段名保持 installedCapacity 以兼容大屏前端；值为 KW（与泵站详情 installedCapacityKw 一致）
                    "installedCapacity", toPumpStationInstalledCapacityKw(pumpStation.getInstalledCapacity()));
            appendGisData(item, pumpStation.getFacilityId(), gisDataMap);
            return item;
        });
    }

    private BigDecimal toPumpStationInstalledCapacityKw(BigDecimal installedCapacityMw) {
        if (installedCapacityMw == null) {
            return null;
        }
        return installedCapacityMw.multiply(PUMP_STATION_KW_PER_MW).setScale(2, RoundingMode.HALF_UP);
    }

    private List<Map<String, Object>> queryReservoirItems() {
        List<YzWaterReservoirDO> reservoirList = waterReservoirMapper.selectList(new LambdaQueryWrapper<YzWaterReservoirDO>()
                .select(YzWaterReservoirDO::getFacilityId,
                        YzWaterReservoirDO::getReservoirName,
                        YzWaterReservoirDO::getTotalCapacity,
                        YzWaterReservoirDO::getCatchmentArea,
                        YzWaterReservoirDO::getIrrigationArea,
                        YzWaterReservoirDO::getLongitude,
                        YzWaterReservoirDO::getLatitude)
                .orderByAsc(YzWaterReservoirDO::getReservoirName)
                .orderByDesc(YzWaterReservoirDO::getId));
        if (CollUtil.isEmpty(reservoirList)) {
            return Collections.emptyList();
        }
        Map<Long, String> gisDataMap = buildGisDataMap(convertList(reservoirList, YzWaterReservoirDO::getFacilityId));
        return convertList(reservoirList, reservoir -> {
            Map<String, Object> item = buildMapItem(
                    "reservoirName", reservoir.getReservoirName(),
                    "totalCapacity", reservoir.getTotalCapacity(),
                    "catchmentArea", reservoir.getCatchmentArea(),
                    "irrigationArea", reservoir.getIrrigationArea(),
                    "longitude", reservoir.getLongitude(),
                    "latitude", reservoir.getLatitude());
            appendGisData(item, reservoir.getFacilityId(), gisDataMap);
            return item;
        });
    }

    private List<Map<String, Object>> queryFloodWarehouseItems(Map<String, String> materialUnitLabelMap) {
        List<YzFloodPreventionMaterialWarehouseDO> warehouseList =
                floodWarehouseMapper.selectList(new LambdaQueryWrapper<YzFloodPreventionMaterialWarehouseDO>()
                        .select(YzFloodPreventionMaterialWarehouseDO::getId,
                                YzFloodPreventionMaterialWarehouseDO::getWarehouseName,
                                YzFloodPreventionMaterialWarehouseDO::getSpecificLocation,
                                YzFloodPreventionMaterialWarehouseDO::getLongitude,
                                YzFloodPreventionMaterialWarehouseDO::getLatitude,
                                YzFloodPreventionMaterialWarehouseDO::getIsDelegateStorage)
                        .orderByAsc(YzFloodPreventionMaterialWarehouseDO::getWarehouseName)
                        .orderByDesc(YzFloodPreventionMaterialWarehouseDO::getId));
        if (CollUtil.isEmpty(warehouseList)) {
            return Collections.emptyList();
        }
        List<String> warehouseIds = convertList(warehouseList, warehouse -> String.valueOf(warehouse.getId()));
        List<YzFxWzDO> materialList = fxWzMapper.selectList(new LambdaQueryWrapper<YzFxWzDO>()
                .select(YzFxWzDO::getUnitId, YzFxWzDO::getMaterialName, YzFxWzDO::getQuantity, YzFxWzDO::getUnit,
                        YzFxWzDO::getIsDelegateStorage,
                        YzFxWzDO::getLongitude, YzFxWzDO::getLatitude, YzFxWzDO::getWarehouseAddress,
                        YzFxWzDO::getContactPerson, YzFxWzDO::getContactInfo)
                .in(CollUtil.isNotEmpty(warehouseIds), YzFxWzDO::getUnitId, warehouseIds)
                .orderByAsc(YzFxWzDO::getSort)
                .orderByAsc(YzFxWzDO::getMaterialName)
                .orderByDesc(YzFxWzDO::getCreateTime));
        Map<String, List<Map<String, Object>>> materialMapByWarehouseId = new HashMap<>();
        if (CollUtil.isNotEmpty(materialList)) {
            for (YzFxWzDO material : materialList) {
                String warehouseId = StrUtil.trimToEmpty(material.getUnitId());
                if (StrUtil.isBlank(warehouseId)) {
                    continue;
                }
                String unitValue = StrUtil.trimToNull(material.getUnit());
                String unitLabel = unitValue == null ? null
                        : StrUtil.blankToDefault(materialUnitLabelMap.get(unitValue), unitValue);
                materialMapByWarehouseId.computeIfAbsent(warehouseId, key -> new ArrayList<>()).add(buildMapItem(
                        "materialName", material.getMaterialName(),
                        "quantity", material.getQuantity(),
                        "unit", unitLabel,
                        "isDelegateStorage", material.getIsDelegateStorage(),
                        "longitude", material.getLongitude(),
                        "latitude", material.getLatitude(),
                        "warehouseAddress", material.getWarehouseAddress(),
                        "contactPerson", material.getContactPerson(),
                        "contactInfo", material.getContactInfo()));
            }
        }
        return convertList(warehouseList, warehouse -> buildMapItem(
                "warehouseName", warehouse.getWarehouseName(),
                "specificLocation", warehouse.getSpecificLocation(),
                "longitude", warehouse.getLongitude(),
                "latitude", warehouse.getLatitude(),
                "isDelegateStorage", warehouse.getIsDelegateStorage(),
                "materials", materialMapByWarehouseId.getOrDefault(String.valueOf(warehouse.getId()),
                        Collections.emptyList())));
    }

    private List<Map<String, Object>> queryWeatherStationItems() {
        List<Map<String, Object>> weatherList = weatherStationMapper.selectSimpleList();
        if (CollUtil.isEmpty(weatherList)) {
            return Collections.emptyList();
        }
        Map<String, YzWeatherStationSsDO> stationSsMap = loadWeatherStationSsMap(weatherList);
        return convertList(weatherList, weather -> {
            String stationSsId = StrUtil.toStringOrNull(getMapValueByKeys(weather, "station_ss_id", "stationSsId"));
            YzWeatherStationSsDO stationSs = stationSsMap.get(stationSsId);
            return buildMapItem(
                    "station_name", getMapValueByKeys(weather, "station_name", "stationName"),
                    "lat", getMapValueByKeys(weather, "lat"),
                    "lon", getMapValueByKeys(weather, "lon"),
                    "station_ss_id", stationSsId,
                    "dailyRainfall", resolveDailyRainfall(stationSs),
                    "updateTime", stationSs == null ? null : stationSs.getCreateTime());
        });
    }

    private List<Map<String, Object>> queryIrrigationDistrictItems() {
        List<YzIrrigationDistrictDO> irrigationDistrictList =
                irrigationDistrictMapper.selectList(new LambdaQueryWrapper<YzIrrigationDistrictDO>()
                        .select(YzIrrigationDistrictDO::getFacilityId,
                                YzIrrigationDistrictDO::getIrrigationDistrictName,
                                YzIrrigationDistrictDO::getMainCanalLengthM)
                        .orderByAsc(YzIrrigationDistrictDO::getIrrigationDistrictName)
                        .orderByDesc(YzIrrigationDistrictDO::getId));
        if (CollUtil.isEmpty(irrigationDistrictList)) {
            return Collections.emptyList();
        }
        Map<Long, String> gisDataMap = buildGisDataMap(
                convertList(irrigationDistrictList, YzIrrigationDistrictDO::getFacilityId));
        return convertList(irrigationDistrictList, irrigationDistrict -> {
            Map<String, Object> item = buildMapItem(
                    "irrigationDistrictName", irrigationDistrict.getIrrigationDistrictName(),
                    "mainCanalLengthM", irrigationDistrict.getMainCanalLengthM());
            appendGisData(item, irrigationDistrict.getFacilityId(), gisDataMap);
            return item;
        });
    }

    private List<Map<String, Object>> queryPondItems(Long areaId) {
        IotScreenPondPageReqVO req = new IotScreenPondPageReqVO();
        req.setAreaId(areaId);
        req.setPageNo(POND_DEFAULT_PAGE_NO);
        req.setPageSize(POND_DEFAULT_PAGE_SIZE);
        IotScreenPondPageRespVO page = getPondPage(req);
        return page.getList() == null ? Collections.emptyList() : page.getList();
    }

    private Map<String, Object> convertScreenPondRow(Map<String, Object> row) {
        return convertScreenPondRow(row, loadPondAreaLabelIndex());
    }

    private Map<String, Object> convertScreenPondRow(Map<String, Object> row, PondAreaLabelIndex areaIndex) {
        Object id = getMapValueByKeys(row, "id");
        Object facilityId = getMapValueByKeys(row, "facility_id", "facilityId");
        Object resourceCode = getMapValueByKeys(row, "resource_code", "resourceCode");
        Object resourceName = getMapValueByKeys(row, "resource_name", "resourceName");
        Object locationDesc = getMapValueByKeys(row, "location_desc", "locationDesc");
        Object villageCode = getMapValueByKeys(row, "village_code", "villageCode");
        Object villageName = resolvePondVillageLabel(
                getMapValueByKeys(row, "village_name", "villageName"), villageCode, areaIndex);
        if (locationDesc == null || StrUtil.isBlank(String.valueOf(locationDesc))) {
            locationDesc = villageName;
        }
        Object ownerUnit = getMapValueByKeys(row, "owner_unit", "ownerUnit");
        Object ownerPerson = getMapValueByKeys(row, "owner_person", "ownerPerson");
        Object ownershipType = getMapValueByKeys(row, "ownership_type", "ownershipType");
        Object landType = getMapValueByKeys(row, "land_type", "landType");
        Object areaSqm = getMapValueByKeys(row, "area_sqm", "areaSqm");
        Object areaMu = getMapValueByKeys(row, "area_mu", "areaMu");
        Object occupyFarmArea = getMapValueByKeys(row, "occupy_farm_area", "occupyFarmArea");
        Object eastTo = getMapValueByKeys(row, "east_to", "eastTo");
        Object southTo = getMapValueByKeys(row, "south_to", "southTo");
        Object westTo = getMapValueByKeys(row, "west_to", "westTo");
        Object northTo = getMapValueByKeys(row, "north_to", "northTo");
        Object usageStatus = getMapValueByKeys(row, "usage_status", "usageStatus");
        Object resourceNature = getMapValueByKeys(row, "resource_nature", "resourceNature");
        Object occupationStatus = getMapValueByKeys(row, "occupation_status", "occupationStatus");
        Object surveyor = getMapValueByKeys(row, "surveyor");
        Object surveyorPhone = getMapValueByKeys(row, "surveyor_phone", "surveyorPhone");
        Object remark = getMapValueByKeys(row, "remark");
        Object resourceType = getMapValueByKeys(row, "resource_type", "resourceType");
        Object centerLon = getMapValueByKeys(row, "center_lon", "centerLon");
        Object centerLat = getMapValueByKeys(row, "center_lat", "centerLat");
        // geometryGeoJson 大屏用 MVT 渲染，接口响应不再返回

        List<String> tags = new ArrayList<>();
        addPondTag(tags, ownershipType);
        addPondTag(tags, resourceType);
        addPondTag(tags, usageStatus);

        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(buildPondDisplayItem("实测面积", areaSqm, "㎡", "areaSqm"));
        stats.add(buildPondDisplayItem("折合亩数", areaMu, "亩", "areaMu"));
        if (occupyFarmArea != null && StrUtil.isNotBlank(String.valueOf(occupyFarmArea))) {
            stats.add(buildPondDisplayItem("占农登权面积", occupyFarmArea, null, "occupyFarmArea"));
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(buildPondDisplayItem("行政区划", villageName, null, "villageName"));
        fields.add(buildPondDisplayItem("行政区划代码", villageCode, null, "villageCode"));
        fields.add(buildPondDisplayItem("坐落位置", locationDesc, null, "locationDesc"));
        fields.add(buildPondDisplayItem("土地权属", ownershipType, null, "ownershipType"));
        fields.add(buildPondDisplayItem("权属单位", ownerUnit, null, "ownerUnit"));
        fields.add(buildPondDisplayItem("权属人", ownerPerson, null, "ownerPerson"));
        fields.add(buildPondDisplayItem("使用状态", usageStatus, null, "usageStatus"));
        fields.add(buildPondDisplayItem("资源类型", resourceType, null, "resourceType"));
        fields.add(buildPondDisplayItem("资源性质", resourceNature, null, "resourceNature"));
        fields.add(buildPondDisplayItem("占用情况", occupationStatus, null, "occupationStatus"));
        fields.add(buildPondDisplayItem("国土地类", landType, null, "landType"));
        fields.add(buildPondDisplayItem("调查员", surveyor, null, "surveyor"));
        fields.add(buildPondDisplayItem("联系方式", surveyorPhone, null, "surveyorPhone"));
        fields.add(buildPondDisplayItem("备注", remark, null, "remark"));

        List<Map<String, Object>> boundary = new ArrayList<>();
        boundary.add(buildPondBoundaryItem("东", "东至", eastTo, "eastTo"));
        boundary.add(buildPondBoundaryItem("南", "南至", southTo, "southTo"));
        boundary.add(buildPondBoundaryItem("西", "西至", westTo, "westTo"));
        boundary.add(buildPondBoundaryItem("北", "北至", northTo, "northTo"));

        Map<String, Object> item = new LinkedHashMap<>();
        // id / facilityId 以字符串输出，避免前端 JS 大整数精度丢失
        item.put("id", toPlainStringId(id));
        item.put("facilityId", toPlainStringId(facilityId));
        item.put("title", "坑塘信息");
        // 中文字段：页面可直接按中文 key 渲染
        item.put("资源名称", resourceName);
        item.put("资源编号", resourceCode);
        item.put("坐落位置", locationDesc);
        item.put("行政区划", villageName);
        item.put("行政区划代码", villageCode);
        item.put("权属单位", ownerUnit);
        item.put("权属人", ownerPerson);
        item.put("土地权属", ownershipType);
        item.put("国土地类", landType);
        item.put("实测面积(㎡)", areaSqm);
        item.put("面积(亩)", areaMu);
        item.put("占农登权面积", occupyFarmArea);
        item.put("东至", eastTo);
        item.put("南至", southTo);
        item.put("西至", westTo);
        item.put("北至", northTo);
        item.put("使用状态", usageStatus);
        item.put("资源性质", resourceNature);
        item.put("占用情况", occupationStatus);
        item.put("调查员", surveyor);
        item.put("联系方式", surveyorPhone);
        item.put("备注", remark);
        item.put("资源类型", resourceType);
        item.put("经度", centerLon);
        item.put("纬度", centerLat);
        // 结构化展示块
        item.put("tags", tags);
        item.put("stats", stats);
        item.put("fields", fields);
        item.put("boundary", boundary);
        // 英文别名：兼容旧前端 / 程序取值
        item.put("resourceName", resourceName);
        item.put("resourceCode", resourceCode);
        item.put("locationDesc", locationDesc);
        item.put("villageName", villageName);
        item.put("villageCode", villageCode);
        item.put("ownerUnit", ownerUnit);
        item.put("ownerPerson", ownerPerson);
        item.put("ownershipType", ownershipType);
        item.put("landType", landType);
        item.put("areaSqm", areaSqm);
        item.put("areaMu", areaMu);
        item.put("occupyFarmArea", occupyFarmArea);
        item.put("eastTo", eastTo);
        item.put("southTo", southTo);
        item.put("westTo", westTo);
        item.put("northTo", northTo);
        item.put("usageStatus", usageStatus);
        item.put("resourceNature", resourceNature);
        item.put("occupationStatus", occupationStatus);
        item.put("surveyor", surveyor);
        item.put("surveyorPhone", surveyorPhone);
        item.put("remark", remark);
        item.put("resourceType", resourceType);
        item.put("longitude", centerLon);
        item.put("latitude", centerLat);
        item.put("centerLon", centerLon);
        item.put("centerLat", centerLat);
        return item;
    }

    private void addPondTag(List<String> tags, Object value) {
        String text = StrUtil.trimToNull(value == null ? null : String.valueOf(value));
        if (text != null && !tags.contains(text)) {
            tags.add(text);
        }
    }

    /** 雪花 id 等大整数转十进制字符串，禁止走 Double 以免精度丢失 */
    private String toPlainStringId(Object id) {
        if (id == null) {
            return null;
        }
        if (id instanceof Long longId) {
            return Long.toString(longId);
        }
        if (id instanceof Integer intId) {
            return Integer.toString(intId);
        }
        if (id instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (id instanceof Number number) {
            // 避免 Double.toString 科学计数；大整数优先用 longValueExact 语义的十进制文本
            try {
                return new BigDecimal(number.toString()).toPlainString();
            } catch (Exception ignored) {
                return String.valueOf(number.longValue());
            }
        }
        String text = StrUtil.trimToNull(String.valueOf(id));
        if (text == null) {
            return null;
        }
        // 去掉可能的科学计数 / 小数点尾巴
        if (text.contains(".") || text.contains("E") || text.contains("e")) {
            try {
                return new BigDecimal(text).toPlainString();
            } catch (Exception ignored) {
                return text;
            }
        }
        return text;
    }

    private Map<String, Object> buildPondDisplayItem(String label, Object value, String unit, String key) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        if (unit != null) {
            item.put("unit", unit);
        }
        item.put("key", key);
        return item;
    }

    private Map<String, Object> buildPondBoundaryItem(String dir, String label, Object value, String key) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("dir", dir);
        item.put("label", label);
        item.put("value", value);
        item.put("key", key);
        return item;
    }

    private String[] buildPondVillageCodeScope(Long areaId) {
        return buildPondVillageCodeScope(areaId, null);
    }

    private String[] buildPondVillageCodeScope(Long areaId, String villageCode) {
        Long rootId = areaId;
        if (rootId == null && StrUtil.isNotBlank(villageCode)) {
            try {
                rootId = new BigDecimal(villageCode.trim()).longValueExact();
            } catch (Exception ignored) {
                return new String[]{villageCode.trim()};
            }
        }
        if (rootId == null) {
            return null;
        }
        List<String> codes = new ArrayList<>();
        codes.add(String.valueOf(rootId));
        try {
            List<SystemAreaNode> children = systemAreaService.getAreaTreeChildren(rootId);
            collectPondAreaIds(children, codes);
        } catch (Exception ignored) {
            // 行政区划子树加载失败时，降级为仅按当前节点查询
        }
        List<String> distinctCodes = codes.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctCodes)) {
            return null;
        }
        return distinctCodes.toArray(new String[0]);
    }

    private void collectPondAreaIds(List<SystemAreaNode> nodes, List<String> result) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (SystemAreaNode node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            result.add(String.valueOf(node.getId()));
            collectPondAreaIds(node.getChildren(), result);
        }
    }

    /**
     * 导入数据常只有 village_code、没有 village_name（源字段是「镇/村」而不是 XZQMC）。
     * 大屏按区划树把编码补成「月塘镇/乌山村委会」，与后台台账 formatAreaLabel 一致。
     */
    private static final class PondAreaLabelIndex {
        private final Map<String, String> nameById = new HashMap<>();
        private final Map<String, String> parentById = new HashMap<>();
    }

    private PondAreaLabelIndex loadPondAreaLabelIndex() {
        PondAreaLabelIndex index = new PondAreaLabelIndex();
        try {
            List<SystemAreaNode> roots = systemAreaService.getAreaTreeChildren(320000L);
            if (CollUtil.isEmpty(roots)) {
                index.nameById.put("321081", "仪征市");
                fillPondAreaLabelIndex(systemAreaService.getAreaTreeChildren(321081L), "321081", index);
            } else {
                fillPondAreaLabelIndex(roots, null, index);
            }
        } catch (Exception ignored) {
            // 区划树加载失败时，仍返回库里的原始名称
        }
        return index;
    }

    private void fillPondAreaLabelIndex(List<SystemAreaNode> nodes, String parentId, PondAreaLabelIndex index) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        for (SystemAreaNode node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            String id = String.valueOf(node.getId());
            index.nameById.put(id, StrUtil.blankToDefault(node.getName(), id));
            if (StrUtil.isNotBlank(parentId)) {
                index.parentById.put(id, parentId);
            }
            fillPondAreaLabelIndex(node.getChildren(), id, index);
        }
    }

    private Object resolvePondVillageLabel(Object storedName, Object villageCode, PondAreaLabelIndex index) {
        String stored = storedName == null ? "" : StrUtil.trimToEmpty(String.valueOf(storedName));
        if ("null".equalsIgnoreCase(stored)) {
            stored = "";
        }
        String code = toPondAreaCode(villageCode);
        String treeLeaf = index == null ? "" : StrUtil.trimToEmpty(index.nameById.get(code));
        String town = resolvePondTownName(code, stored, treeLeaf, index);

        String village = "";
        if (isPondVillageName(treeLeaf)) {
            village = treeLeaf;
        } else if (isPondVillageName(stored)) {
            village = stored;
        } else if (StrUtil.isNotBlank(stored) && !stored.equals(town) && !stored.equals(treeLeaf) && !isPondTownName(stored)) {
            village = stored;
        }

        String combined = combinePondTownVillage(town, village);
        String result = StrUtil.blankToDefault(combined, StrUtil.blankToDefault(treeLeaf, stored));
        return StrUtil.isBlank(result) ? null : result;
    }

    private String resolvePondTownName(String areaCode, String storedName, String treeLeaf, PondAreaLabelIndex index) {
        if (index != null && StrUtil.isNotBlank(areaCode)) {
            String cursor = areaCode;
            Set<String> visited = new LinkedHashSet<>();
            while (StrUtil.isNotBlank(cursor) && visited.add(cursor)) {
                String name = index.nameById.get(cursor);
                if (isPondTownName(name)) {
                    return name;
                }
                cursor = index.parentById.get(cursor);
            }
        }
        if (isPondTownName(treeLeaf)) {
            return treeLeaf;
        }
        if (isPondTownName(storedName)) {
            return storedName;
        }
        return "";
    }

    private boolean isPondTownName(String name) {
        String n = StrUtil.trimToEmpty(name);
        return n.endsWith("镇") || n.endsWith("街道") || n.endsWith("乡") || n.endsWith("办事处");
    }

    private boolean isPondVillageName(String name) {
        String n = StrUtil.trimToEmpty(name);
        return n.endsWith("村") || n.endsWith("社区") || n.endsWith("居委会") || n.endsWith("村委会");
    }

    private String combinePondTownVillage(String town, String village) {
        String t = StrUtil.trimToEmpty(town);
        String v = StrUtil.trimToEmpty(village);
        if (StrUtil.isBlank(t) && StrUtil.isBlank(v)) {
            return "";
        }
        if (StrUtil.isBlank(t)) {
            return v;
        }
        if (StrUtil.isBlank(v) || t.equals(v)) {
            return t;
        }
        if (v.startsWith(t)) {
            return v;
        }
        return t + "/" + v;
    }

    private String toPondAreaCode(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString()).toPlainString();
        }
        String text = StrUtil.trimToEmpty(String.valueOf(value));
        return "null".equalsIgnoreCase(text) ? "" : text;
    }

    private List<Map<String, Object>> querySignboardItems() {
        List<YzSignboardBfDO> signboardList = signboardBfMapper.selectList(new LambdaQueryWrapper<YzSignboardBfDO>()
                .select(YzSignboardBfDO::getId,
                        YzSignboardBfDO::getReferenceId,
                        YzSignboardBfDO::getReferenceType,
                        YzSignboardBfDO::getQrCode,
                        YzSignboardBfDO::getSignboardName,
                        YzSignboardBfDO::getLongitude,
                        YzSignboardBfDO::getLatitude)
                .eq(YzSignboardBfDO::getIsScreenDisplay, 1)
                .orderByDesc(YzSignboardBfDO::getId));
        if (CollUtil.isEmpty(signboardList)) {
            return Collections.emptyList();
        }
        return convertList(signboardList, signboard -> buildMapItem(
                "id", signboard.getId(),
                "referenceId", signboard.getReferenceId(),
                "referenceType", signboard.getReferenceType(),
                "qrCode", signboard.getQrCode(),
                "signboardName", signboard.getSignboardName(),
                "longitude", signboard.getLongitude(),
                "latitude", signboard.getLatitude()));
    }

    private List<Map<String, Object>> queryProblemFeedbackItems() {
        List<YzProblemFeedbackDO> feedbackList = problemFeedbackMapper.selectList(new LambdaQueryWrapper<YzProblemFeedbackDO>()
                .select(YzProblemFeedbackDO::getId,
                        YzProblemFeedbackDO::getLongitude,
                        YzProblemFeedbackDO::getLatitude,
                        YzProblemFeedbackDO::getIssueSpecificLocation,
                        YzProblemFeedbackDO::getReferenceId,
                        YzProblemFeedbackDO::getReferenceType)
                .orderByDesc(YzProblemFeedbackDO::getId));
        if (CollUtil.isEmpty(feedbackList)) {
            return Collections.emptyList();
        }
        return convertList(feedbackList, feedback -> buildMapItem(
                "id", feedback.getId(),
                "longitude", feedback.getLongitude(),
                "latitude", feedback.getLatitude(),
                "issueSpecificLocation", feedback.getIssueSpecificLocation(),
                "referenceId", feedback.getReferenceId(),
                "referenceType", feedback.getReferenceType()));
    }

    private Map<Long, String> buildGisDataMap(List<Long> facilityIds) {
        if (CollUtil.isEmpty(facilityIds)) {
            return Collections.emptyMap();
        }
        List<Long> distinctFacilityIds = facilityIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctFacilityIds)) {
            return Collections.emptyMap();
        }
        List<YzWaterFacilityBaseDO> facilityBaseList = waterFacilityBaseMapper.selectBatchIds(distinctFacilityIds);
        if (CollUtil.isEmpty(facilityBaseList)) {
            return Collections.emptyMap();
        }
        WKTWriter wktWriter = new WKTWriter();
        Map<Long, String> gisDataMap = new HashMap<>(facilityBaseList.size());
        for (YzWaterFacilityBaseDO facilityBase : facilityBaseList) {
            if (facilityBase == null || facilityBase.getId() == null || facilityBase.getGeom() == null) {
                continue;
            }
            gisDataMap.put(facilityBase.getId(), wktWriter.write(facilityBase.getGeom()));
        }
        return gisDataMap;
    }

    private Map<String, Object> buildMapItem(Object... keyValues) {
        Map<String, Object> item = new LinkedHashMap<>();
        if (keyValues == null) {
            return item;
        }
        for (int index = 0; index + 1 < keyValues.length; index += 2) {
            item.put(String.valueOf(keyValues[index]), keyValues[index + 1]);
        }
        return item;
    }

    private void appendGisData(Map<String, Object> item, Long facilityId, Map<Long, String> gisDataMap) {
        if (item == null) {
            return;
        }
        item.put("gis", facilityId == null ? null : gisDataMap.get(facilityId));
    }

    private Object getMapValueByKeys(Map<String, Object> dataMap, String... keys) {
        if (dataMap == null || dataMap.isEmpty() || keys == null || keys.length == 0) {
            return null;
        }
        for (String key : keys) {
            if (StrUtil.isBlank(key)) {
                continue;
            }
            if (dataMap.containsKey(key)) {
                return dataMap.get(key);
            }
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                if (StrUtil.equalsIgnoreCase(entry.getKey(), key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private Map<String, YzWeatherStationSsDO> loadWeatherStationSsMap(List<Map<String, Object>> weatherList) {
        if (CollUtil.isEmpty(weatherList)) {
            return Collections.emptyMap();
        }
        List<String> stationSsIds = weatherList.stream()
                .map(item -> StrUtil.toStringOrNull(getMapValueByKeys(item, "station_ss_id", "stationSsId")))
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(stationSsIds)) {
            return Collections.emptyMap();
        }
        List<YzWeatherStationSsDO> stationSsList = weatherStationSsMapper.selectBatchIds(stationSsIds);
        if (CollUtil.isEmpty(stationSsList)) {
            return Collections.emptyMap();
        }
        return stationSsList.stream()
                .filter(item -> item != null && StrUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(YzWeatherStationSsDO::getId, item -> item, (a, b) -> a));
    }

    private BigDecimal resolveDailyRainfall(YzWeatherStationSsDO stationSs) {
        if (stationSs == null || CollUtil.isEmpty(stationSs.getWeather())) {
            return null;
        }
        List<Map<String, Object>> surfElementList = toMapList(getMapValueByKeys(stationSs.getWeather(), WEATHER_SURF_HOURS_KEY));
        if (CollUtil.isEmpty(surfElementList)) {
            return null;
        }
        BigDecimal rainfallSum = BigDecimal.ZERO;
        boolean hasHourlyRainfall = false;
        for (Map<String, Object> surfElement : surfElementList) {
            BigDecimal pre1h = parseBigDecimal(getMapValueByKeys(surfElement, WEATHER_HOURLY_RAINFALL_KEY));
            if (pre1h != null) {
                rainfallSum = rainfallSum.add(pre1h);
                hasHourlyRainfall = true;
            }
        }
        return hasHourlyRainfall ? rainfallSum.setScale(2, RoundingMode.HALF_UP) : null;
    }

    private LocalDateTime resolveLatestWeatherObservationTime(List<Map<String, Object>> surfElementList) {
        LocalDateTime latestTime = null;
        for (Map<String, Object> surfElement : surfElementList) {
            LocalDateTime observationTime = parseWeatherObservationTime(surfElement);
            if (observationTime != null && (latestTime == null || observationTime.isAfter(latestTime))) {
                latestTime = observationTime;
            }
        }
        return latestTime;
    }

    private LocalDateTime parseWeatherObservationTime(Map<String, Object> surfElement) {
        if (CollUtil.isEmpty(surfElement)) {
            return null;
        }
        Object timeValue = getMapValueByKeys(surfElement,
                "Datetime", "DateTime", "datetime", "observationTime", "obsTime", "observeTime", "time");
        Long epochMilli = resolveEpochMilli(timeValue);
        if (epochMilli == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMilli).atZone(BUSINESS_TIME_ZONE).toLocalDateTime();
    }

    private LocalDateTime resolveWeatherBusinessDayStart(LocalDateTime referenceTime) {
        LocalDate referenceDate = referenceTime.toLocalDate();
        LocalDateTime sameDayStart = referenceDate.atTime(WEATHER_BUSINESS_DAY_START_HOUR, 0, 0);
        if (referenceTime.isBefore(sameDayStart)) {
            return sameDayStart.minusDays(1);
        }
        return sameDayStart;
    }

    private Map<String, Object> resolveLatestSurfElement(Map<String, Object> weatherMap) {
        if (CollUtil.isEmpty(weatherMap)) {
            return Collections.emptyMap();
        }
        Object surfHoursObj = getMapValueByKeys(weatherMap, WEATHER_SURF_HOURS_KEY);
        List<Map<String, Object>> surfElementList = toMapList(surfHoursObj);
        if (CollUtil.isEmpty(surfElementList)) {
            return Collections.emptyMap();
        }
        return surfElementList.get(surfElementList.size() - 1);
    }

    private List<Map<String, Object>> toMapList(Object data) {
        if (data == null) {
            return Collections.emptyList();
        }
        if (data instanceof String jsonText) {
            if (StrUtil.isBlank(jsonText)) {
                return Collections.emptyList();
            }
            try {
                List<Map<String, Object>> parsed = JsonUtils.parseObject(jsonText,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                return parsed == null ? Collections.emptyList() : parsed;
            } catch (Exception ignore) {
                return Collections.emptyList();
            }
        }
        if (!(data instanceof List<?> rawList) || CollUtil.isEmpty(rawList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> converted = new LinkedHashMap<>();
                map.forEach((key, value) -> converted.put(String.valueOf(key), value));
                result.add(converted);
            }
        }
        return result;
    }

    private Object roundPressValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        BigDecimal numericValue = parseBigDecimal(rawValue);
        if (numericValue == null) {
            return rawValue;
        }
        return numericValue.setScale(2, RoundingMode.HALF_UP);
    }

    private Object roundFlowValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        BigDecimal numericValue = parseBigDecimal(rawValue);
        if (numericValue == null) {
            return rawValue;
        }
        return numericValue.setScale(FLOW_VALUE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 从设备类型字典解析「流量计/流速仪」对应 value；找不到则返回 null（调用方回退全站识别）
     */
    private Integer resolveFlowDeviceType() {
        List<DictDataRespDTO> dictDataList = dictDataCommonApi.getDictDataList(DictTypeConstants.DEVICE_TYPE);
        if (CollUtil.isEmpty(dictDataList)) {
            return null;
        }
        for (DictDataRespDTO dictData : dictDataList) {
            if (!Objects.equals(dictData.getStatus(), CommonStatusEnum.ENABLE.getStatus())) {
                continue;
            }
            String label = StrUtil.trimToEmpty(dictData.getLabel());
            if (!label.contains("流量") && !label.contains("流速")) {
                continue;
            }
            String value = StrUtil.trimToNull(dictData.getValue());
            if (value == null) {
                continue;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException ignored) {
                // ignore invalid dict value
            }
        }
        return null;
    }

    private boolean isFlowDeviceName(String deviceName) {
        String text = StrUtil.trimToEmpty(deviceName);
        return text.contains("流量") || text.contains("流速");
    }

    private BigDecimal parseBigDecimal(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof BigDecimal decimalValue) {
            return decimalValue;
        }
        if (rawValue instanceof Number numberValue) {
            return new BigDecimal(numberValue.toString());
        }
        String strValue = StrUtil.trimToNull(rawValue.toString());
        if (strValue == null) {
            return null;
        }
        try {
            return new BigDecimal(strValue);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

}
